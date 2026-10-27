package com.sicklysurgeon.customdeathmessages.util;

import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.Sound;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.function.Consumer;

public final class SoundUtil {

    private static volatile Map<String, Sound> constantCache;

    private SoundUtil() {
    }

    public static Sound tryParse(String raw) {
        if (raw == null || raw.trim().isEmpty()) {
            return null;
        }

        String value = raw.trim();

        try {
            NamespacedKey key = NamespacedKey.fromString(value);
            if (key != null) {
                Sound sound = Registry.SOUNDS.get(key);
                if (sound != null) {
                    return sound;
                }
            }
        } catch (Exception ignored) {
        }

        if (!value.contains(":")) {
            try {
                NamespacedKey key = NamespacedKey.minecraft(value.toLowerCase(Locale.ROOT));
                Sound sound = Registry.SOUNDS.get(key);
                if (sound != null) {
                    return sound;
                }
            } catch (Exception ignored) {
            }
        }

        String upper = value.toUpperCase(Locale.ROOT).replace('-', '_');
        if (upper.matches("[A-Z0-9_]+")) {
            Sound cached = getConstantCache().get(upper);
            if (cached != null) {
                return cached;
            }
        }

        return null;
    }

    public static Sound parse(String raw, Sound fallback, Consumer<String> warning) {
        Sound sound = tryParse(raw);
        if (sound != null) {
            return sound;
        }

        if (warning != null) {
            warning.accept("Invalid sound '" + raw + "'. Using default: " + keyName(fallback));
        }

        return fallback;
    }

    public static String keyName(Sound sound) {
        if (sound == null) {
            return "unknown";
        }

        try {
            NamespacedKey key = getKey(sound);
            return key == null ? sound.toString() : key.toString();
        } catch (Throwable t) {
            return sound.toString();
        }
    }

    private static NamespacedKey getKey(Sound sound) {
        if (sound == null) {
            return null;
        }

        try {
            Method method = Registry.class.getMethod("getKey", Object.class);
            Object key = method.invoke(Registry.SOUNDS, sound);
            if (key instanceof NamespacedKey) {
                return (NamespacedKey) key;
            }
        } catch (Throwable ignored) {
        }

        try {
            Method method = Sound.class.getMethod("getKey");
            Object key = method.invoke(sound);
            if (key instanceof NamespacedKey) {
                return (NamespacedKey) key;
            }
        } catch (Throwable ignored) {
        }

        return null;
    }

    private static Map<String, Sound> getConstantCache() {
        Map<String, Sound> local = constantCache;
        if (local != null) {
            return local;
        }

        synchronized (SoundUtil.class) {
            local = constantCache;
            if (local != null) {
                return local;
            }

            local = new HashMap<>();

            try {
                for (Sound sound : Registry.SOUNDS) {
                    NamespacedKey key = getKey(sound);
                    if (key == null) {
                        continue;
                    }

                    String fullConst = (key.getNamespace() + "_" + key.getKey())
                            .toUpperCase(Locale.ROOT)
                            .replace('.', '_')
                            .replace(':', '_');

                    String noNsConst = key.getKey()
                            .toUpperCase(Locale.ROOT)
                            .replace('.', '_');

                    local.putIfAbsent(fullConst, sound);
                    local.putIfAbsent(noNsConst, sound);
                    local.putIfAbsent("MINECRAFT_" + noNsConst, sound);
                }
            } catch (Throwable ignored) {
            }

            constantCache = local;
            return local;
        }
    }
}