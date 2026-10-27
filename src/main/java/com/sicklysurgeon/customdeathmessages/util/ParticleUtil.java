package com.sicklysurgeon.customdeathmessages.util;

import org.bukkit.Particle;

import java.util.Locale;
import java.util.function.Consumer;

public final class ParticleUtil {

    private ParticleUtil() {
    }

    public static Particle tryParse(String raw) {
        if (raw == null || raw.trim().isEmpty()) {
            return null;
        }

        try {
            return Particle.valueOf(raw.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ignored) {
            return null;
        }
    }

    public static Particle parse(String raw, Particle fallback, Consumer<String> warning) {
        Particle particle = tryParse(raw);
        if (particle != null) {
            return particle;
        }

        if (warning != null) {
            warning.accept("Invalid particle '" + raw + "'. Using default: " + name(fallback));
        }

        return fallback;
    }

    public static String name(Particle particle) {
        return particle == null ? "unknown" : particle.name();
    }
}