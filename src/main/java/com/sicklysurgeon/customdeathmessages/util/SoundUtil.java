package com.sicklysurgeon.customdeathmessages.util;

import org.bukkit.Sound;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.function.Consumer;

/**
 * Resolves sound names across Minecraft versions.
 *
 * <p>Minecraft 1.8 - 1.12 used names such as {@code HURT_FLESH} and
 * {@code AMBIENCE_THUNDER}. Since 1.13 the names follow the vanilla key format
 * ({@code entity.player.death}), and in 1.21+ {@link Sound} is no longer an enum.
 * This class tries the current server API first and then translates the most
 * common names to the legacy constants, so the same configuration works on every
 * supported version.</p>
 */
public final class SoundUtil {

    private static final String DEFAULT_SOUND = "entity.player.death";
    private static final String DEFAULT_THUNDER = "entity.lightning_bolt.thunder";

    /** Modern constant -> legacy constant. The reverse mapping is added too. */
    private static final Map<String, String> ALIASES = new HashMap<>();

    /** Intermediate renames, applied only when the literal constant is missing. */
    private static final Map<String, String> CHAINS = new HashMap<>();

    static {
        alias("ENTITY_PLAYER_DEATH", "HURT_FLESH");
        alias("ENTITY_PLAYER_HURT", "HURT_FLESH");
        alias("ENTITY_PLAYER_HURT_DROWN", "HURT_FLESH");
        alias("ENTITY_PLAYER_HURT_ON_FIRE", "HURT_FLESH");
        alias("ENTITY_PLAYER_HURT_FREEZE", "HURT_FLESH");
        alias("ENTITY_PLAYER_BIG_FALL", "FALL_BIG");
        alias("ENTITY_PLAYER_SMALL_FALL", "FALL_SMALL");
        alias("ENTITY_PLAYER_LEVELUP", "LEVEL_UP");
        alias("ENTITY_PLAYER_BURP", "BURP");
        alias("ENTITY_PLAYER_SPLASH", "SPLASH");
        alias("ENTITY_PLAYER_SPLASH_HIGH_SPEED", "SPLASH2");
        alias("ENTITY_PLAYER_SWIM", "SWIM");
        alias("ENTITY_PLAYER_BREATH", "WATER");
        alias("ENTITY_PLAYER_ATTACK_STRONG", "SUCCESSFUL_HIT");
        alias("ENTITY_PLAYER_ATTACK_SWEEP", "SUCCESSFUL_HIT");
        alias("ENTITY_PLAYER_ATTACK_CRIT", "SUCCESSFUL_HIT");
        alias("ENTITY_PLAYER_ATTACK_KNOCKBACK", "SUCCESSFUL_HIT");
        alias("ENTITY_PLAYER_ATTACK_NODAMAGE", "SUCCESSFUL_HIT");
        alias("ENTITY_PLAYER_ATTACK_WEAK", "SUCCESSFUL_HIT");

        // 1.9 - 1.12 used these shorter names before "bolt" was added.
        chain("ENTITY_LIGHTNING_BOLT_THUNDER", "ENTITY_LIGHTNING_THUNDER");
        chain("ENTITY_LIGHTNING_THUNDER", "AMBIENCE_THUNDER");
        chain("ENTITY_LIGHTNING_BOLT_IMPACT", "ENTITY_LIGHTNING_IMPACT");
        chain("ENTITY_LIGHTNING_IMPACT", "EXPLODE");
        alias("ENTITY_GENERIC_EXPLODE", "EXPLODE");
        alias("ENTITY_GENERIC_DRINK", "DRINK");
        alias("ENTITY_GENERIC_EAT", "EAT");
        alias("ENTITY_GENERIC_EXTINGUISH_FIRE", "FIZZ");
        alias("ENTITY_GENERIC_BURN", "FIRE_IGNITE");
        alias("ENTITY_GENERIC_SPLASH", "SPLASH");
        alias("ENTITY_GENERIC_SWIM", "SWIM");
        alias("ENTITY_GENERIC_SMALL_FALL", "FALL_SMALL");
        alias("ENTITY_GENERIC_BIG_FALL", "FALL_BIG");

        alias("ENTITY_WITHER_SPAWN", "WITHER_SPAWN");
        alias("ENTITY_WITHER_DEATH", "WITHER_DEATH");
        alias("ENTITY_WITHER_HURT", "WITHER_HURT");
        alias("ENTITY_WITHER_SHOOT", "WITHER_SHOOT");
        alias("ENTITY_WITHER_AMBIENT", "WITHER_IDLE");

        alias("ENTITY_ENDER_DRAGON_DEATH", "ENDERDRAGON_DEATH");
        alias("ENTITY_ENDER_DRAGON_GROWL", "ENDERDRAGON_GROWL");
        alias("ENTITY_ENDER_DRAGON_HURT", "ENDERDRAGON_HIT");
        alias("ENTITY_ENDER_DRAGON_FLAP", "ENDERDRAGON_WINGS");
        alias("ENTITY_ENDER_DRAGON_AMBIENT", "ENDERDRAGON_GROWL");

        alias("ENTITY_ENDERMAN_TELEPORT", "ENDERMAN_TELEPORT");
        alias("ENTITY_ENDERMAN_HURT", "ENDERMAN_HIT");
        alias("ENTITY_ENDERMAN_DEATH", "ENDERMAN_DEATH");
        alias("ENTITY_ENDERMAN_SCREAM", "ENDERMAN_SCREAM");
        alias("ENTITY_ENDERMAN_AMBIENT", "ENDERMAN_IDLE");
        alias("ENTITY_ENDERMAN_STARE", "ENDERMAN_STARE");
        alias("ENTITY_ENDER_EYE_LAUNCH", "ENDERMAN_TELEPORT");

        alias("ENTITY_CREEPER_PRIMED", "CREEPER_HISS");
        alias("ENTITY_CREEPER_DEATH", "CREEPER_DEATH");

        alias("ENTITY_ARROW_HIT_PLAYER", "SUCCESSFUL_HIT");
        alias("ENTITY_ARROW_HIT", "ARROW_HIT");
        alias("ENTITY_ARROW_SHOOT", "SHOOT_ARROW");

        alias("ENTITY_ITEM_PICKUP", "ITEM_PICKUP");
        alias("ENTITY_ITEM_BREAK", "ITEM_BREAK");
        alias("ENTITY_EXPERIENCE_ORB_PICKUP", "ORB_PICKUP");

        alias("ENTITY_FIREWORK_ROCKET_BLAST", "FIREWORK_BLAST");
        alias("ENTITY_FIREWORK_ROCKET_LARGE_BLAST", "FIREWORK_LARGE_BLAST");
        alias("ENTITY_FIREWORK_ROCKET_TWINKLE", "FIREWORK_TWINKLE");
        alias("ENTITY_FIREWORK_ROCKET_LAUNCH", "FIREWORK_LAUNCH");
        alias("ENTITY_TNT_PRIMED", "FUSE");

        alias("ENTITY_GHAST_SHOOT", "GHAST_FIREBALL");
        alias("ENTITY_GHAST_SCREAM", "GHAST_SCREAM");
        alias("ENTITY_GHAST_DEATH", "GHAST_DEATH");
        alias("ENTITY_GHAST_HURT", "GHAST_SCREAM2");
        alias("ENTITY_GHAST_AMBIENT", "GHAST_MOAN");

        alias("ENTITY_BLAZE_SHOOT", "BLAZE_BREATH");
        alias("ENTITY_BLAZE_AMBIENT", "BLAZE_BREATH");
        alias("ENTITY_BLAZE_HURT", "BLAZE_HIT");
        alias("ENTITY_BLAZE_DEATH", "BLAZE_DEATH");
        alias("ENTITY_BLAZE_BURN", "FIRE");

        alias("ENTITY_ZOMBIE_AMBIENT", "ZOMBIE_IDLE");
        alias("ENTITY_ZOMBIE_HURT", "ZOMBIE_HURT");
        alias("ENTITY_ZOMBIE_DEATH", "ZOMBIE_DEATH");
        alias("ENTITY_ZOMBIE_ATTACK_WOODEN_DOOR", "ZOMBIE_WOOD");
        alias("ENTITY_ZOMBIE_ATTACK_IRON_DOOR", "ZOMBIE_METAL");
        alias("ENTITY_ZOMBIE_BREAK_WOODEN_DOOR", "ZOMBIE_WOODBREAK");
        alias("ENTITY_ZOMBIE_INFECT", "ZOMBIE_INFECT");
        alias("ENTITY_ZOMBIE_VILLAGER_CONVERTED", "ZOMBIE_UNFECT");
        alias("ENTITY_ZOMBIE_STEP", "ZOMBIE_WALK");
        alias("ENTITY_ZOMBIE_PIG_AMBIENT", "ZOMBIE_PIG_IDLE");
        alias("ENTITY_ZOMBIE_PIG_ANGRY", "ZOMBIE_PIG_ANGRY");
        alias("ENTITY_ZOMBIE_PIG_DEATH", "ZOMBIE_PIG_DEATH");
        alias("ENTITY_ZOMBIE_PIG_HURT", "ZOMBIE_PIG_HURT");

        alias("ENTITY_SKELETON_AMBIENT", "SKELETON_IDLE");
        alias("ENTITY_SKELETON_HURT", "SKELETON_HURT");
        alias("ENTITY_SKELETON_DEATH", "SKELETON_DEATH");
        alias("ENTITY_SKELETON_STEP", "SKELETON_WALK");
        alias("ENTITY_SKELETON_SHOOT", "SHOOT_ARROW");

        alias("ENTITY_SPIDER_AMBIENT", "SPIDER_IDLE");
        alias("ENTITY_SPIDER_HURT", "SPIDER_DEATH");
        alias("ENTITY_SPIDER_DEATH", "SPIDER_DEATH");
        alias("ENTITY_SPIDER_STEP", "SPIDER_WALK");

        alias("ENTITY_PIG_AMBIENT", "PIG_IDLE");
        alias("ENTITY_PIG_HURT", "PIG_DEATH");
        alias("ENTITY_PIG_DEATH", "PIG_DEATH");
        alias("ENTITY_PIG_STEP", "PIG_WALK");

        alias("ENTITY_COW_AMBIENT", "COW_IDLE");
        alias("ENTITY_COW_HURT", "COW_HURT");
        alias("ENTITY_COW_DEATH", "COW_HURT");
        alias("ENTITY_COW_STEP", "COW_WALK");

        alias("ENTITY_CHICKEN_AMBIENT", "CHICKEN_IDLE");
        alias("ENTITY_CHICKEN_HURT", "CHICKEN_HURT");
        alias("ENTITY_CHICKEN_DEATH", "CHICKEN_HURT");
        alias("ENTITY_CHICKEN_STEP", "CHICKEN_WALK");
        alias("ENTITY_CHICKEN_EGG", "CHICKEN_EGG_POP");

        alias("ENTITY_SHEEP_AMBIENT", "SHEEP_IDLE");
        alias("ENTITY_SHEEP_HURT", "SHEEP_IDLE");
        alias("ENTITY_SHEEP_DEATH", "SHEEP_IDLE");
        alias("ENTITY_SHEEP_STEP", "SHEEP_WALK");
        alias("ENTITY_SHEEP_SHEAR", "SHEEP_SHEAR");

        alias("ENTITY_HORSE_DEATH", "HORSE_DEATH");
        alias("ENTITY_HORSE_HURT", "HORSE_HIT");
        alias("ENTITY_HORSE_AMBIENT", "HORSE_IDLE");
        alias("ENTITY_HORSE_GALLOP", "HORSE_GALLOP");
        alias("ENTITY_HORSE_JUMP", "HORSE_JUMP");
        alias("ENTITY_HORSE_LAND", "HORSE_LAND");
        alias("ENTITY_HORSE_STEP", "HORSE_SOFT");
        alias("ENTITY_HORSE_ARMOR", "HORSE_ARMOR");
        alias("ENTITY_DONKEY_AMBIENT", "DONKEY_IDLE");
        alias("ENTITY_DONKEY_HURT", "DONKEY_HIT");
        alias("ENTITY_DONKEY_DEATH", "DONKEY_DEATH");
        alias("ENTITY_DONKEY_ANGRY", "DONKEY_ANGRY");

        alias("ENTITY_VILLAGER_AMBIENT", "VILLAGER_IDLE");
        alias("ENTITY_VILLAGER_HURT", "VILLAGER_HIT");
        alias("ENTITY_VILLAGER_DEATH", "VILLAGER_DEATH");
        alias("ENTITY_VILLAGER_YES", "VILLAGER_YES");
        alias("ENTITY_VILLAGER_NO", "VILLAGER_NO");
        alias("ENTITY_VILLAGER_TRADE", "VILLAGER_HAGGLE");

        alias("ENTITY_IRON_GOLEM_DEATH", "IRONGOLEM_DEATH");
        alias("ENTITY_IRON_GOLEM_HURT", "IRONGOLEM_HIT");
        alias("ENTITY_IRON_GOLEM_ATTACK", "IRONGOLEM_THROW");
        alias("ENTITY_IRON_GOLEM_STEP", "IRONGOLEM_WALK");
        alias("ENTITY_IRON_GOLEM_AMBIENT", "IRONGOLEM_WALK");

        alias("BLOCK_ANVIL_LAND", "ANVIL_LAND");
        alias("BLOCK_ANVIL_USE", "ANVIL_USE");
        alias("BLOCK_ANVIL_BREAK", "ANVIL_BREAK");
        alias("BLOCK_ANVIL_FALL", "ANVIL_LAND");
        alias("BLOCK_ANVIL_PLACE", "ANVIL_LAND");

        alias("BLOCK_NOTE_BLOCK_PLING", "NOTE_PLING");
        alias("BLOCK_NOTE_BLOCK_BASS", "NOTE_BASS");
        alias("BLOCK_NOTE_BLOCK_HAT", "NOTE_STICKS");
        alias("BLOCK_NOTE_BLOCK_SNARE", "NOTE_SNARE_DRUM");
        alias("BLOCK_NOTE_BLOCK_BASEDRUM", "NOTE_BASS_DRUM");
        alias("BLOCK_NOTE_BLOCK_BELL", "NOTE_PIANO");
        alias("BLOCK_NOTE_BLOCK_CHIME", "NOTE_PLING");
        alias("BLOCK_NOTE_BLOCK_FLUTE", "NOTE_BASS_GUITAR");
        alias("BLOCK_NOTE_BLOCK_GUITAR", "NOTE_BASS_GUITAR");
        alias("BLOCK_NOTE_BLOCK_XYLOPHONE", "NOTE_PIANO");

        alias("BLOCK_GLASS_BREAK", "GLASS");
        alias("BLOCK_GLASS_PLACE", "GLASS");
        alias("BLOCK_CHEST_OPEN", "CHEST_OPEN");
        alias("BLOCK_CHEST_CLOSE", "CHEST_CLOSE");
        alias("BLOCK_ENDER_CHEST_OPEN", "CHEST_OPEN");
        alias("BLOCK_ENDER_CHEST_CLOSE", "CHEST_CLOSE");
        alias("BLOCK_PORTAL_TRAVEL", "PORTAL_TRAVEL");
        alias("BLOCK_PORTAL_TRIGGER", "PORTAL_TRIGGER");
        alias("BLOCK_PORTAL_AMBIENT", "PORTAL");
        alias("BLOCK_FIRE_AMBIENT", "FIRE");
        alias("BLOCK_FIRE_EXTINGUISH", "FIZZ");
        alias("BLOCK_LAVA_POP", "LAVA_POP");
        alias("BLOCK_LAVA_AMBIENT", "LAVA");
        alias("BLOCK_WATER_AMBIENT", "WATER");

        alias("BLOCK_STONE_BREAK", "DIG_STONE");
        alias("BLOCK_STONE_PLACE", "DIG_STONE");
        alias("BLOCK_STONE_STEP", "STEP_STONE");
        alias("BLOCK_WOOD_BREAK", "DIG_WOOD");
        alias("BLOCK_WOOD_PLACE", "DIG_WOOD");
        alias("BLOCK_WOOD_STEP", "STEP_WOOD");
        alias("BLOCK_GRAVEL_BREAK", "DIG_GRAVEL");
        alias("BLOCK_GRAVEL_PLACE", "DIG_GRAVEL");
        alias("BLOCK_GRAVEL_STEP", "STEP_GRAVEL");
        alias("BLOCK_GRASS_BREAK", "DIG_GRASS");
        alias("BLOCK_GRASS_PLACE", "DIG_GRASS");
        alias("BLOCK_GRASS_STEP", "STEP_GRASS");
        alias("BLOCK_SAND_BREAK", "DIG_SAND");
        alias("BLOCK_SAND_PLACE", "DIG_SAND");
        alias("BLOCK_SAND_STEP", "STEP_SAND");
        alias("BLOCK_SNOW_BREAK", "DIG_SNOW");
        alias("BLOCK_SNOW_PLACE", "DIG_SNOW");
        alias("BLOCK_SNOW_STEP", "STEP_SNOW");
        alias("BLOCK_WOOL_BREAK", "DIG_WOOL");
        alias("BLOCK_WOOL_PLACE", "DIG_WOOL");
        alias("BLOCK_WOOL_STEP", "STEP_WOOL");
        alias("BLOCK_SLIME_BLOCK_BREAK", "SLIME_ATTACK");
        alias("BLOCK_SLIME_BLOCK_PLACE", "SLIME_ATTACK");
        alias("BLOCK_SLIME_BLOCK_STEP", "SLIME_WALK");

        alias("UI_BUTTON_CLICK", "CLICK");
        alias("BLOCK_LEVER_CLICK", "WOOD_CLICK");
        alias("BLOCK_PISTON_EXTEND", "PISTON_EXTEND");
        alias("BLOCK_PISTON_CONTRACT", "PISTON_RETRACT");

        alias("ENTITY_ITEM_FRAME_ADD_ITEM", "ITEM_PICKUP");
        alias("ENTITY_ITEM_FRAME_REMOVE_ITEM", "ITEM_PICKUP");
        alias("ENTITY_ITEM_FRAME_PLACE", "ITEM_PICKUP");
        alias("ENTITY_ITEM_FRAME_BREAK", "ITEM_BREAK");
        alias("ENTITY_PAINTING_PLACE", "ITEM_PICKUP");
        alias("ENTITY_PAINTING_BREAK", "ITEM_BREAK");
        alias("ENTITY_EXPERIENCE_BOTTLE_THROW", "SHOOT_ARROW");
        alias("ENTITY_SPLASH_POTION_BREAK", "SPLASH");
        alias("ENTITY_SPLASH_POTION_THROW", "SHOOT_ARROW");
        alias("ENTITY_LINGERING_POTION_THROW", "SHOOT_ARROW");
        alias("ENTITY_ARMOR_STAND_BREAK", "ITEM_BREAK");
        alias("ENTITY_ARMOR_STAND_PLACE", "DIG_STONE");
        alias("ENTITY_ARMOR_STAND_HIT", "ITEM_BREAK");
        alias("ENTITY_ARMOR_STAND_FALL", "FALL_SMALL");
    }

    private SoundUtil() {
    }

    /**
     * Tries to resolve a sound. Returns {@code null} when the name is unknown on
     * the current server version.
     */
    public static Sound tryParse(String raw) {
        if (raw == null || raw.trim().isEmpty()) {
            return null;
        }

        String value = raw.trim();
        String constant = toConstant(value);

        Sound sound = valueOf(constant);
        if (sound != null) {
            return sound;
        }

        String alias = ALIASES.get(constant);
        if (alias != null) {
            sound = valueOf(alias);
            if (sound != null) {
                return sound;
            }
        }

        String chained = constant;
        for (int depth = 0; depth < 4; depth++) {
            chained = CHAINS.get(chained);
            if (chained == null) {
                break;
            }

            sound = valueOf(chained);
            if (sound != null) {
                return sound;
            }
        }

        if (constant.startsWith("MINECRAFT_")) {
            sound = valueOf(constant.substring("MINECRAFT_".length()));
            if (sound != null) {
                return sound;
            }
        }

        return fromRegistry(value);
    }

    public static Sound parse(String raw, Sound fallback, Consumer<String> warning) {
        Sound sound = tryParse(raw);
        if (sound != null) {
            return sound;
        }

        if (warning != null) {
            warning.accept("Invalid sound '" + raw + "'. Using default: " + keyName(fallback));
        }

        return fallback != null ? fallback : defaultSound();
    }

    /** Sound used when a player dies. */
    public static Sound defaultSound() {
        Sound sound = tryParse(DEFAULT_SOUND);
        if (sound != null) {
            return sound;
        }

        sound = valueOf("ENTITY_PLAYER_DEATH");
        if (sound != null) {
            return sound;
        }

        sound = valueOf("HURT_FLESH");
        if (sound != null) {
            return sound;
        }

        sound = valueOf("CLICK");
        if (sound != null) {
            return sound;
        }

        // Last resort: the first constant exposed by the server.
        try {
            Method values = Sound.class.getMethod("values");
            Object result = values.invoke(null);
            if (result instanceof Object[] && ((Object[]) result).length > 0) {
                return (Sound) ((Object[]) result)[0];
            }
        } catch (Throwable ignored) {
        }

        return null;
    }

    /** Thunder sound used by epic deaths. */
    public static Sound defaultThunderSound() {
        Sound sound = tryParse(DEFAULT_THUNDER);
        if (sound != null) {
            return sound;
        }

        sound = valueOf("ENTITY_LIGHTNING_BOLT_THUNDER");
        if (sound != null) {
            return sound;
        }

        sound = valueOf("AMBIENCE_THUNDER");
        if (sound != null) {
            return sound;
        }

        return defaultSound();
    }

    /** Human-readable name for logs and command feedback. */
    public static String keyName(Sound sound) {
        if (sound == null) {
            return "unknown";
        }

        try {
            Method getKey = Sound.class.getMethod("getKey");
            Object key = getKey.invoke(sound);
            if (key != null) {
                String keyPart = keyPart(key);
                if (keyPart != null && !keyPart.isEmpty()) {
                    return keyPart;
                }
            }
        } catch (Throwable ignored) {
        }

        try {
            Method name = Sound.class.getMethod("name");
            Object result = name.invoke(sound);
            if (result instanceof String) {
                return (String) result;
            }
        } catch (Throwable ignored) {
        }

        return sound.toString();
    }

    private static void alias(String modern, String legacy) {
        ALIASES.putIfAbsent(modern, legacy);
        ALIASES.putIfAbsent(legacy, modern);
    }

    private static void chain(String from, String to) {
        CHAINS.putIfAbsent(from, to);
    }

    private static String toConstant(String value) {
        String result = value;
        if (result.regionMatches(true, 0, "minecraft:", 0, "minecraft:".length())) {
            result = result.substring("minecraft:".length());
        }
        return result.toUpperCase(Locale.ROOT)
                .replace('.', '_')
                .replace('-', '_')
                .replace(':', '_')
                .replace(' ', '_');
    }

    private static Sound valueOf(String constant) {
        if (constant == null || constant.isEmpty()) {
            return null;
        }

        try {
            Method method = Sound.class.getMethod("valueOf", String.class);
            Object result = method.invoke(null, constant);
            return result instanceof Sound ? (Sound) result : null;
        } catch (Throwable ignored) {
            return null;
        }
    }

    private static Sound fromRegistry(String raw) {
        try {
            Class<?> registryClass = Class.forName("org.bukkit.Registry");
            Class<?> keyClass = Class.forName("org.bukkit.NamespacedKey");

            Object registry = registryClass.getField("SOUNDS").get(null);
            if (registry == null) {
                return null;
            }

            String keyString = toKeyString(raw);
            Object key;
            try {
                Method minecraft = keyClass.getMethod("minecraft", String.class);
                key = minecraft.invoke(null, keyString);
            } catch (NoSuchMethodException e) {
                Method fromString = keyClass.getMethod("fromString", String.class);
                key = fromString.invoke(null, "minecraft:" + keyString);
            }

            if (key == null) {
                return null;
            }

            Method get = null;
            for (Method method : registryClass.getMethods()) {
                if (!"get".equals(method.getName()) || method.getParameterTypes().length != 1) {
                    continue;
                }
                if (method.getParameterTypes()[0].isAssignableFrom(keyClass)) {
                    get = method;
                    break;
                }
            }

            if (get == null) {
                return null;
            }

            Object sound = get.invoke(registry, key);
            return sound instanceof Sound ? (Sound) sound : null;
        } catch (Throwable ignored) {
            return null;
        }
    }

    private static String toKeyString(String raw) {
        String value = raw.trim().toLowerCase(Locale.ROOT);
        if (value.startsWith("minecraft:")) {
            value = value.substring("minecraft:".length());
        } else if (value.contains(":")) {
            return value;
        }

        if (value.contains(".")) {
            return value;
        }

        return value.replace('_', '.');
    }

    private static String keyPart(Object key) {
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
