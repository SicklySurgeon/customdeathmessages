package com.sicklysurgeon.customdeathmessages.util;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * Small runtime compatibility helpers for APIs that changed between 1.8 and
 * 26.x. Everything is reflective so the same source can be compiled against the
 * 1.8.8 API (Java 8) and against the modern API (Java 21).
 */
public final class VersionCompat {

    private VersionCompat() {
    }

    public static void sendTitle(Player player,
                                 String title,
                                 String subtitle,
                                 int fadeIn,
                                 int stay,
                                 int fadeOut) {
        if (player == null) {
            return;
        }

        try {
            Method method = Player.class.getMethod("sendTitle",
                    String.class, String.class, int.class, int.class, int.class);
            method.invoke(player, title, subtitle, fadeIn, stay, fadeOut);
            return;
        } catch (Throwable ignored) {
        }

        // 1.8 only has the two-argument version. It uses the vanilla timings.
        player.sendTitle(title, subtitle);
    }

    public static void sendActionBar(Player player, String message) {
        if (player == null || message == null) {
            return;
        }

        if (sendBungeeActionBar(player, message)) {
            return;
        }

        sendNmsActionBar(player, message);
    }

    private static boolean sendBungeeActionBar(Player player, String message) {
        try {
            Class<?> chatMessageType = Class.forName("net.md_5.bungee.api.ChatMessageType");
            Object actionBar = chatMessageType.getField("ACTION_BAR").get(null);

            Class<?> textComponent = Class.forName("net.md_5.bungee.api.chat.TextComponent");
            Method fromLegacyText = textComponent.getMethod("fromLegacyText", String.class);
            Object components = fromLegacyText.invoke(null, message);

            Object spigot = Player.class.getMethod("spigot").invoke(player);

            Method send = null;
            for (Method method : spigot.getClass().getMethods()) {
                if (!"sendMessage".equals(method.getName())) {
                    continue;
                }

                Class<?>[] parameters = method.getParameterTypes();
                if (parameters.length == 2
                        && parameters[0].isAssignableFrom(chatMessageType)
                        && parameters[1].isArray()) {
                    send = method;
                    break;
                }
            }

            if (send == null) {
                return false;
            }

            send.invoke(spigot, actionBar, components);
            return true;
        } catch (Throwable ignored) {
            return false;
        }
    }

    private static boolean sendNmsActionBar(Player player, String message) {
        try {
            String version = nmsVersion(player);
            if (version == null) {
                return false;
            }

            Object handle = player.getClass().getMethod("getHandle").invoke(player);

            Class<?> componentClass = Class.forName("net.minecraft.server." + version + ".IChatBaseComponent");
            Class<?> serializerClass = Class.forName("net.minecraft.server." + version + ".IChatBaseComponent$ChatSerializer");
            Object component = serializerClass.getMethod("a", String.class)
                    .invoke(null, legacyToJson(message));

            Class<?> packetClass = Class.forName("net.minecraft.server." + version + ".PacketPlayOutChat");
            Object packet = packetClass.getConstructor(componentClass, byte.class)
                    .newInstance(component, (byte) 2);

            Object connection = findFieldValue(handle, "playerConnection");
            if (connection == null) {
                return false;
            }

            for (Method method : connection.getClass().getMethods()) {
                if ("sendPacket".equals(method.getName()) && method.getParameterTypes().length == 1) {
                    method.invoke(connection, packet);
                    return true;
                }
            }
        } catch (Throwable ignored) {
        }

        return false;
    }

    private static String nmsVersion(Player player) {
        String packageName = player.getClass().getPackage().getName();
        for (String part : packageName.split("\\.")) {
            if (part.matches("v\\d+_\\d+_R\\d+")) {
                return part;
            }
        }
        return null;
    }

    private static Object findFieldValue(Object target, String name) {
        for (Class<?> type = target.getClass(); type != null; type = type.getSuperclass()) {
            try {
                Field field = type.getDeclaredField(name);
                field.setAccessible(true);
                return field.get(target);
            } catch (Throwable ignored) {
            }
        }
        return null;
    }

    private static String legacyToJson(String message) {
        List<String> parts = new ArrayList<>();
        StringBuilder text = new StringBuilder();

        String color = null;
        boolean bold = false;
        boolean italic = false;
        boolean underlined = false;
        boolean strikethrough = false;
        boolean obfuscated = false;

        for (int i = 0; i < message.length(); i++) {
            char current = message.charAt(i);

            if (current != '\u00A7' || i + 1 >= message.length()) {
                text.append(current);
                continue;
            }

            char code = Character.toLowerCase(message.charAt(++i));
            String newColor = colorName(code);

            if (newColor != null) {
                flush(parts, text, color, bold, italic, underlined, strikethrough, obfuscated);
                color = newColor;
                bold = italic = underlined = strikethrough = obfuscated = false;
            } else if (code == 'k') {
                flush(parts, text, color, bold, italic, underlined, strikethrough, obfuscated);
                obfuscated = true;
            } else if (code == 'l') {
                flush(parts, text, color, bold, italic, underlined, strikethrough, obfuscated);
                bold = true;
            } else if (code == 'm') {
                flush(parts, text, color, bold, italic, underlined, strikethrough, obfuscated);
                strikethrough = true;
            } else if (code == 'n') {
                flush(parts, text, color, bold, italic, underlined, strikethrough, obfuscated);
                underlined = true;
            } else if (code == 'o') {
                flush(parts, text, color, bold, italic, underlined, strikethrough, obfuscated);
                italic = true;
            } else if (code == 'r') {
                flush(parts, text, color, bold, italic, underlined, strikethrough, obfuscated);
                color = null;
                bold = italic = underlined = strikethrough = obfuscated = false;
            } else {
                text.append(current).append(message.charAt(i));
            }
        }

        flush(parts, text, color, bold, italic, underlined, strikethrough, obfuscated);

        if (parts.isEmpty()) {
            return "{\"text\":\"\"}";
        }

        return "{\"text\":\"\",\"extra\":[" + String.join(",", parts) + "]}";
    }

    private static void flush(List<String> parts,
                              StringBuilder text,
                              String color,
                              boolean bold,
                              boolean italic,
                              boolean underlined,
                              boolean strikethrough,
                              boolean obfuscated) {
        if (text.length() == 0) {
            return;
        }

        StringBuilder json = new StringBuilder();
        json.append("{\"text\":\"").append(escapeJson(text.toString())).append("\"");

        if (color != null) {
            json.append(",\"color\":\"").append(color).append("\"");
        }
        if (bold) {
            json.append(",\"bold\":true");
        }
        if (italic) {
            json.append(",\"italic\":true");
        }
        if (underlined) {
            json.append(",\"underlined\":true");
        }
        if (strikethrough) {
            json.append(",\"strikethrough\":true");
        }
        if (obfuscated) {
            json.append(",\"obfuscated\":true");
        }

        json.append("}");
        parts.add(json.toString());
        text.setLength(0);
    }

    private static String colorName(char code) {
        switch (code) {
            case '0':
                return "black";
            case '1':
                return "dark_blue";
            case '2':
                return "dark_green";
            case '3':
                return "dark_aqua";
            case '4':
                return "dark_red";
            case '5':
                return "dark_purple";
            case '6':
                return "gold";
            case '7':
                return "gray";
            case '8':
                return "dark_gray";
            case '9':
                return "blue";
            case 'a':
                return "green";
            case 'b':
                return "aqua";
            case 'c':
                return "red";
            case 'd':
                return "light_purple";
            case 'e':
                return "yellow";
            case 'f':
                return "white";
            default:
                return null;
        }
    }

    private static String escapeJson(String value) {
        StringBuilder result = new StringBuilder(value.length() + 8);

        for (int i = 0; i < value.length(); i++) {
            char c = value.charAt(i);
            switch (c) {
                case '"':
                    result.append("\\\"");
                    break;
                case '\\':
                    result.append("\\\\");
                    break;
                case '\n':
                    result.append("\\n");
                    break;
                case '\r':
                    result.append("\\r");
                    break;
                case '\t':
                    result.append("\\t");
                    break;
                default:
                    if (c < 0x20) {
                        result.append(String.format("\\u%04x", (int) c));
                    } else {
                        result.append(c);
                    }
            }
        }

        return result.toString();
    }

    public static String biomeName(Object biome) {
        if (biome == null) {
            return "Unknown";
        }

        try {
            Method getKey = biome.getClass().getMethod("getKey");
            Object key = getKey.invoke(biome);
            String keyPart = keyPart(key);
            if (keyPart != null && !keyPart.isEmpty()) {
                return keyPart;
            }
        } catch (Throwable ignored) {
        }

        try {
            Method name = biome.getClass().getMethod("name");
            Object result = name.invoke(biome);
            if (result instanceof String) {
                return (String) result;
            }
        } catch (Throwable ignored) {
        }

        return String.valueOf(biome);
    }

    public static String potionName(Object potionEffectType) {
        if (potionEffectType == null) {
            return "Unknown";
        }

        try {
            Method getKey = potionEffectType.getClass().getMethod("getKey");
            Object key = getKey.invoke(potionEffectType);
            String keyPart = keyPart(key);
            if (keyPart != null && !keyPart.isEmpty()) {
                return keyPart;
            }
        } catch (Throwable ignored) {
        }

        try {
            Method getName = potionEffectType.getClass().getMethod("getName");
            Object result = getName.invoke(potionEffectType);
            if (result instanceof String) {
                return (String) result;
            }
        } catch (Throwable ignored) {
        }

        return String.valueOf(potionEffectType);
    }

    public static boolean isAir(Material material) {
        if (material == null) {
            return true;
        }

        try {
            Method isAir = Material.class.getMethod("isAir");
            Object result = isAir.invoke(material);
            if (result instanceof Boolean) {
                return (Boolean) result;
            }
        } catch (Throwable ignored) {
        }

        return material == Material.AIR;
    }

    /**
     * Item in the main hand. 1.8 has no getItemInMainHand, but the old
     * getItemInHand still returns the main hand item on every version.
     */
    @SuppressWarnings("deprecation")
    public static ItemStack getItemInMainHand(Player player) {
        if (player == null) {
            return null;
        }

        try {
            PlayerInventory inventory = player.getInventory();
            Method method = PlayerInventory.class.getMethod("getItemInMainHand");
            Object result = method.invoke(inventory);
            if (result instanceof ItemStack) {
                return (ItemStack) result;
            }
        } catch (Throwable ignored) {
        }

        return player.getInventory().getItemInHand();
    }

    private static String keyPart(Object key) {
        if (key == null) {
            return null;
        }

        try {
            Method getKey = key.getClass().getMethod("getKey");
            Object value = getKey.invoke(key);
            if (value instanceof String) {
                return (String) value;
            }
        } catch (Throwable ignored) {
        }

        String value = key.toString();
        int namespace = value.indexOf(':');
        return namespace >= 0 ? value.substring(namespace + 1) : value;
    }
}
