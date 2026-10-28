package com.sicklysurgeon.customdeathmessages.util;

import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.function.Consumer;

/**
 * Cross-version particle support.
 *
 * <p>Minecraft 1.8 has no {@code org.bukkit.Particle} and no
 * {@code World#spawnParticle}; it uses {@link Effect} instead. From 1.9 onwards
 * the particle API exists, but many constants were renamed over the years. The
 * class keeps particle names as strings and resolves them at runtime, so a
 * configuration written for 1.21 can still be used on 1.8.</p>
 */
public final class ParticleUtil {

    public static final String DEFAULT_PARTICLE = "EXPLOSION";
    public static final String DEFAULT_DARKEN_PARTICLE = "CLOUD";

    /** Old / legacy names -> modern Particle constants. */
    private static final Map<String, String> MODERN_ALIASES = new HashMap<>();

    /** Modern Particle constants -> names available in the 1.8 Effect enum. */
    private static final Map<String, String> LEGACY_ALIASES = new HashMap<>();

    private static final Random RANDOM = new Random();

    private static volatile boolean reflectionReady;
    private static volatile Class<?> particleClass;
    private static volatile Method particleValueOf;
    private static volatile Method playerSpawnParticle;
    private static volatile Method worldSpawnParticle;

    private static final Map<String, Object> PARTICLE_CACHE = new HashMap<>();
    private static final Map<String, Effect> EFFECT_CACHE = new HashMap<>();

    static {
        // 1.8 names that still appear in old configurations.
        modern("EXPLOSION_NORMAL", "POOF");
        modern("EXPLOSION_LARGE", "EXPLOSION");
        modern("EXPLOSION_HUGE", "EXPLOSION_EMITTER");
        modern("SMOKE_NORMAL", "SMOKE");
        modern("SMOKE_LARGE", "LARGE_SMOKE");
        modern("REDSTONE", "DUST");
        modern("VILLAGER_HAPPY", "HAPPY_VILLAGER");
        modern("VILLAGER_ANGRY", "ANGRY_VILLAGER");
        modern("CRIT_MAGIC", "ENCHANTED_HIT");
        modern("SPELL", "EFFECT");
        modern("SPELL_INSTANT", "INSTANT_EFFECT");
        modern("SPELL_MOB", "ENTITY_EFFECT");
        modern("SPELL_MOB_AMBIENT", "AMBIENT_ENTITY_EFFECT");
        modern("SPELL_WITCH", "WITCH");
        modern("FIREWORKS_SPARK", "FIREWORK");
        modern("TOTEM", "TOTEM_OF_UNDYING");
        modern("ENCHANTMENT_TABLE", "ENCHANT");
        modern("WATER_DROP", "DRIPPING_WATER");
        modern("MOBSPAWNER_FLAMES", "FLAME");

        // Reverse mappings for servers that still use the 1.9 - 1.16 names
        // (for example EXPLOSION and EXPLOSION_EMITTER did not exist until 1.17).
        modern("EXPLOSION", "EXPLOSION_LARGE");
        modern("EXPLOSION_EMITTER", "EXPLOSION_HUGE");
        modern("SMOKE", "SMOKE_NORMAL");
        modern("LARGE_SMOKE", "SMOKE_LARGE");
        modern("POOF", "EXPLOSION_NORMAL");
        modern("DUST", "REDSTONE");
        modern("HAPPY_VILLAGER", "VILLAGER_HAPPY");
        modern("ANGRY_VILLAGER", "VILLAGER_ANGRY");
        modern("ENCHANTED_HIT", "CRIT_MAGIC");
        modern("EFFECT", "SPELL");
        modern("INSTANT_EFFECT", "SPELL_INSTANT");
        modern("ENTITY_EFFECT", "SPELL_MOB");
        modern("AMBIENT_ENTITY_EFFECT", "SPELL_MOB_AMBIENT");
        modern("WITCH", "SPELL_WITCH");
        modern("FIREWORK", "FIREWORKS_SPARK");
        modern("TOTEM_OF_UNDYING", "TOTEM");
        modern("ENCHANT", "ENCHANTMENT_TABLE");
        modern("DRIPPING_WATER", "WATER_DROP");

        // Modern names mapped to the small 1.8 Effect set. Unknown modern
        // particles gracefully fall back to CLOUD instead of disappearing.
        legacy("EXPLOSION", "EXPLOSION");
        legacy("EXPLOSION_LARGE", "EXPLOSION_LARGE");
        legacy("EXPLOSION_EMITTER", "EXPLOSION_HUGE");
        legacy("EXPLOSION_NORMAL", "EXPLOSION");
        legacy("POOF", "CLOUD");
        legacy("CLOUD", "CLOUD");
        legacy("SMOKE", "SMOKE");
        legacy("SMOKE_NORMAL", "SMOKE");
        legacy("LARGE_SMOKE", "LARGE_SMOKE");
        legacy("SMOKE_LARGE", "LARGE_SMOKE");
        legacy("FLAME", "FLAME");
        legacy("SOUL_FIRE_FLAME", "FLAME");
        legacy("LAVA", "LAVA_POP");
        legacy("DRIPPING_LAVA", "LAVADRIP");
        legacy("FALLING_LAVA", "LAVADRIP");
        legacy("LANDING_LAVA", "LAVA_POP");
        legacy("DRIPPING_WATER", "WATERDRIP");
        legacy("FALLING_WATER", "WATERDRIP");
        legacy("LANDING_WATER", "WATERDRIP");
        legacy("HEART", "HEART");
        legacy("NOTE", "NOTE");
        legacy("PORTAL", "PORTAL");
        legacy("REVERSE_PORTAL", "PORTAL");
        legacy("ENCHANT", "SPELL");
        legacy("ENCHANTED_HIT", "MAGIC_CRIT");
        legacy("CRIT", "CRIT");
        legacy("DAMAGE_INDICATOR", "CRIT");
        legacy("SWEEP_ATTACK", "CRIT");
        legacy("TOTEM_OF_UNDYING", "HEART");
        legacy("FIREWORK", "FIREWORKS_SPARK");
        legacy("HAPPY_VILLAGER", "HEART");
        legacy("ANGRY_VILLAGER", "VILLAGER_THUNDERCLOUD");
        legacy("WITCH", "WITCH_MAGIC");
        legacy("EFFECT", "SPELL");
        legacy("INSTANT_EFFECT", "INSTANT_SPELL");
        legacy("ENTITY_EFFECT", "SPELL");
        legacy("AMBIENT_ENTITY_EFFECT", "SPELL");
        legacy("DRAGON_BREATH", "CLOUD");
        legacy("END_ROD", "CLOUD");
        legacy("SOUL", "CLOUD");
        legacy("ASH", "SMOKE");
        legacy("WHITE_ASH", "SMOKE");
        legacy("CRIMSON_SPORE", "SMOKE");
        legacy("WARPED_SPORE", "SMOKE");
        legacy("WHITE_SMOKE", "SMOKE");
        legacy("DUST_PLUME", "SMOKE");
        legacy("CHERRY_LEAVES", "CLOUD");
        legacy("SONIC_BOOM", "EXPLOSION");
        legacy("SCULK_SOUL", "CLOUD");
        legacy("SCULK_CHARGE", "CLOUD");
        legacy("SCULK_CHARGE_POP", "CLOUD");
        legacy("SHRIEK", "CLOUD");
        legacy("VIBRATION", "CLOUD");
        legacy("DUST", "CLOUD");
        legacy("REDSTONE", "CLOUD");
        legacy("FALLING_DUST", "CLOUD");
        legacy("BLOCK", "CLOUD");
        legacy("ITEM", "CLOUD");
        legacy("BUBBLE_POP", "CLOUD");
        legacy("CURRENT_DOWN", "CLOUD");
        legacy("BUBBLE_COLUMN_UP", "CLOUD");
        legacy("NAUTILUS", "CLOUD");
        legacy("DOLPHIN", "CLOUD");
        legacy("SNEEZE", "CLOUD");
        legacy("CAMPFIRE_COSY_SMOKE", "CLOUD");
        legacy("CAMPFIRE_SIGNAL_SMOKE", "CLOUD");
        legacy("COMPOSTER", "CLOUD");
        legacy("FLASH", "EXPLOSION");
        legacy("FALLING_HONEY", "CLOUD");
        legacy("LANDING_HONEY", "CLOUD");
        legacy("FALLING_NECTAR", "CLOUD");
        legacy("DRIPPING_HONEY", "CLOUD");
        legacy("DRIPPING_OBSIDIAN_TEAR", "LAVADRIP");
        legacy("FALLING_OBSIDIAN_TEAR", "LAVADRIP");
        legacy("LANDING_OBSIDIAN_TEAR", "LAVA_POP");
        legacy("TRIAL_SPAWNER_DETECTION", "FLAME");
        legacy("TRIAL_SPAWNER_DETECTION_OMINOUS", "FLAME");
        legacy("VAULT_CONNECTION", "PORTAL");
        legacy("DUST_PILLAR", "CLOUD");
        legacy("BLOCK_MARKER", "CLOUD");
        legacy("INFESTED", "SMOKE");
        legacy("ITEM_COBWEB", "CLOUD");
        legacy("OMINOUS_SPAWNING", "SMOKE");
        legacy("RAID_OMEN", "SPELL");
        legacy("TRIAL_OMEN", "SPELL");
        legacy("SMALL_GUST", "CLOUD");
        legacy("GUST", "CLOUD");
        legacy("GUST_EMITTER_LARGE", "CLOUD");
        legacy("GUST_EMITTER_SMALL", "CLOUD");
        legacy("WIND_CHARGE", "CLOUD");
        legacy("NOXIOUS_GAS_CLOUD", "CLOUD");
    }

    private ParticleUtil() {
    }

    public static String tryParse(String raw) {
        if (raw == null || raw.trim().isEmpty()) {
            return null;
        }

        String name = normalize(raw);
        if (name.isEmpty()) {
            return null;
        }

        initReflection();

        if (particleClass != null) {
            if (resolveModern(name) != null) {
                return name;
            }
        }

        if (resolveLegacyExact(name) != null) {
            return name;
        }

        return null;
    }

    public static String parse(String raw, String fallback, Consumer<String> warning) {
        String particle = tryParse(raw);
        if (particle != null) {
            return particle;
        }

        if (warning != null) {
            warning.accept("Invalid particle '" + raw + "'. Using default: " + name(fallback));
        }

        return fallback != null ? fallback : DEFAULT_PARTICLE;
    }

    public static String name(String particle) {
        return particle == null ? "unknown" : particle;
    }

    public static void spawn(Player player,
                             Location location,
                             String particle,
                             int count,
                             double offsetX,
                             double offsetY,
                             double offsetZ,
                             double extra) {
        if (player == null || location == null || location.getWorld() == null || count <= 0) {
            return;
        }

        if (spawnModern(player, location, particle, count, offsetX, offsetY, offsetZ, extra)) {
            return;
        }

        spawnLegacy(location.getWorld(), location, particle, count, offsetX, offsetY, offsetZ);
    }

    public static void spawn(World world,
                             Location location,
                             String particle,
                             int count,
                             double offsetX,
                             double offsetY,
                             double offsetZ,
                             double extra) {
        if (world == null || location == null || count <= 0) {
            return;
        }

        if (spawnModern(world, location, particle, count, offsetX, offsetY, offsetZ, extra)) {
            return;
        }

        spawnLegacy(world, location, particle, count, offsetX, offsetY, offsetZ);
    }

    private static boolean spawnModern(Object target,
                                       Location location,
                                       String particle,
                                       int count,
                                       double offsetX,
                                       double offsetY,
                                       double offsetZ,
                                       double extra) {
        initReflection();

        if (particleClass == null) {
            return false;
        }

        Object resolved = resolveModern(particle);
        if (resolved == null) {
            return false;
        }

        try {
            Method method = target instanceof Player ? playerSpawnParticle : worldSpawnParticle;
            if (method != null) {
                method.invoke(target, resolved, location, count, offsetX, offsetY, offsetZ, extra);
                return true;
            }
        } catch (Throwable ignored) {
        }

        // If Player#spawnParticle is missing, try World#spawnParticle once.
        if (target instanceof Player) {
            try {
                if (worldSpawnParticle != null) {
                    worldSpawnParticle.invoke(location.getWorld(), resolved, location,
                            count, offsetX, offsetY, offsetZ, extra);
                    return true;
                }
            } catch (Throwable ignored) {
            }
        }

        return false;
    }

    private static void spawnLegacy(World world,
                                    Location location,
                                    String particle,
                                    int count,
                                    double offsetX,
                                    double offsetY,
                                    double offsetZ) {
        Effect effect = resolveLegacy(particle);
        if (effect == null) {
            return;
        }

        for (int i = 0; i < count; i++) {
            double x = (RANDOM.nextDouble() - 0.5D) * 2.0D * offsetX;
            double y = (RANDOM.nextDouble() - 0.5D) * 2.0D * offsetY;
            double z = (RANDOM.nextDouble() - 0.5D) * 2.0D * offsetZ;

            try {
                world.playEffect(location.clone().add(x, y, z), effect, 0);
            } catch (Throwable ignored) {
            }
        }
    }

    private static Object resolveModern(String raw) {
        String name = normalize(raw);

        synchronized (PARTICLE_CACHE) {
            if (PARTICLE_CACHE.containsKey(name)) {
                return PARTICLE_CACHE.get(name);
            }
        }

        Object resolved = particleValueOf(name);
        if (resolved == null) {
            String alias = MODERN_ALIASES.get(name);
            if (alias != null) {
                resolved = particleValueOf(alias);
            }
        }

        synchronized (PARTICLE_CACHE) {
            PARTICLE_CACHE.put(name, resolved);
        }

        return resolved;
    }

    private static Effect resolveLegacy(String raw) {
        String name = normalize(raw);

        synchronized (EFFECT_CACHE) {
            if (EFFECT_CACHE.containsKey(name)) {
                return EFFECT_CACHE.get(name);
            }
        }

        Effect effect = resolveLegacyExact(name);

        synchronized (EFFECT_CACHE) {
            EFFECT_CACHE.put(name, effect);
        }

        return effect;
    }

    private static Effect resolveLegacyExact(String name) {
        String candidate = LEGACY_ALIASES.get(name);
        if (candidate != null) {
            Effect effect = effectValueOf(candidate);
            if (effect != null) {
                return effect;
            }
        }

        Effect effect = effectValueOf(name);
        if (effect != null) {
            return effect;
        }

        // A modern particle without an explicit mapping: use the closest
        // generic effect so the user still sees something.
        return null;
    }

    private static void initReflection() {
        if (reflectionReady) {
            return;
        }

        synchronized (ParticleUtil.class) {
            if (reflectionReady) {
                return;
            }

            try {
                particleClass = Class.forName("org.bukkit.Particle");
                particleValueOf = particleClass.getMethod("valueOf", String.class);
            } catch (Throwable ignored) {
                particleClass = null;
                particleValueOf = null;
            }

            try {
                playerSpawnParticle = Player.class.getMethod("spawnParticle",
                        particleClass, Location.class, int.class,
                        double.class, double.class, double.class, double.class);
            } catch (Throwable ignored) {
                playerSpawnParticle = null;
            }

            try {
                worldSpawnParticle = World.class.getMethod("spawnParticle",
                        particleClass, Location.class, int.class,
                        double.class, double.class, double.class, double.class);
            } catch (Throwable ignored) {
                worldSpawnParticle = null;
            }

            reflectionReady = true;
        }
    }

    private static Object particleValueOf(String name) {
        if (particleValueOf == null) {
            return null;
        }

        try {
            return particleValueOf.invoke(null, name);
        } catch (Throwable ignored) {
            return null;
        }
    }

    private static Effect effectValueOf(String name) {
        try {
            return Effect.valueOf(name);
        } catch (Throwable ignored) {
            return null;
        }
    }

    private static String normalize(String raw) {
        String value = raw.trim().toUpperCase(Locale.ROOT);
        if (value.startsWith("MINECRAFT:")) {
            value = value.substring("MINECRAFT:".length());
        }
        return value.replace('.', '_')
                .replace('-', '_')
                .replace(' ', '_');
    }

    private static void modern(String oldName, String modernName) {
        MODERN_ALIASES.putIfAbsent(oldName, modernName);
    }

    private static void legacy(String modernName, String legacyName) {
        LEGACY_ALIASES.putIfAbsent(modernName, legacyName);
    }
}
