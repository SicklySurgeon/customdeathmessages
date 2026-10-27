package com.sicklysurgeon.customdeathmessages.util.tracking;

import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.projectiles.BlockProjectileSource;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

public class DamageTracker {

    public static final class DamageSourceInfo {
        private final Entity responsible;
        private final Entity directSource;
        private final String blockSourceName;
        private final long timestamp;

        public DamageSourceInfo(Entity responsible, Entity directSource) {
            this(responsible, directSource, null);
        }

        public DamageSourceInfo(Entity responsible, Entity directSource, String blockSourceName) {
            this.responsible = responsible;
            this.directSource = directSource;
            this.blockSourceName = blockSourceName;
            this.timestamp = System.currentTimeMillis();
        }

        public Entity getResponsible() {
            return responsible;
        }

        public Entity getDirectSource() {
            return directSource;
        }

        public String getBlockSourceName() {
            return blockSourceName;
        }

        public boolean isExpired(long maxAgeMillis) {
            return System.currentTimeMillis() - timestamp > maxAgeMillis;
        }
    }

    /**
     * A damage source older than this is not used to attribute a death to a killer.
     * Prevents a hit received long before an unrelated death from being reported as the cause.
     */
    private static final long MAX_DAMAGE_AGE_MILLIS = 15_000L;

    private final Map<UUID, DamageSourceInfo> lastDamageSource = new HashMap<>();

    public void trackDamage(Player victim, Entity damager) {
        if (victim == null || damager == null) {
            return;
        }

        Entity responsible = damager;
        Entity directSource = damager;
        String blockSourceName = null;

        if (damager instanceof Projectile) {
            Projectile projectile = (Projectile) damager;
            Object shooter = projectile.getShooter();

            if (shooter instanceof Entity) {
                responsible = (Entity) shooter;
            } else if (shooter instanceof BlockProjectileSource) {
                BlockProjectileSource blockSource = (BlockProjectileSource) shooter;
                responsible = null;
                blockSourceName = formatBlockName(blockSource.getBlock().getType());
            }
        }

        lastDamageSource.put(victim.getUniqueId(),
                new DamageSourceInfo(responsible, directSource, blockSourceName));
    }

    public DamageSourceInfo getLastDamageInfo(Player victim) {
        if (victim == null) {
            return null;
        }

        DamageSourceInfo info = lastDamageSource.get(victim.getUniqueId());
        if (info == null) {
            return null;
        }

        if (info.isExpired(MAX_DAMAGE_AGE_MILLIS)) {
            lastDamageSource.remove(victim.getUniqueId());
            return null;
        }

        return info;
    }

    public Entity getLastDamageSource(Player victim) {
        DamageSourceInfo info = getLastDamageInfo(victim);
        return info == null ? null : info.getResponsible();
    }

    public void clear(Player victim) {
        if (victim != null) {
            lastDamageSource.remove(victim.getUniqueId());
        }
    }

    private String formatBlockName(Material material) {
        if (material == null) {
            return "Unknown";
        }

        String name = material.name().toLowerCase(Locale.ROOT).replace('_', ' ');

        return Arrays.stream(name.split(" "))
                .map(word -> word.isEmpty()
                        ? word
                        : Character.toUpperCase(word.charAt(0)) + word.substring(1))
                .collect(Collectors.joining(" "));
    }
}