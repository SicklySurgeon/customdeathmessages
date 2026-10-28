package com.sicklysurgeon.customdeathmessages.util.effects;

import com.sicklysurgeon.customdeathmessages.CustomDeathMessages;
import com.sicklysurgeon.customdeathmessages.util.broadcast.BroadcastManager;
import com.sicklysurgeon.customdeathmessages.util.config.ConfigManager;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.List;

public class ParticleManager {

    private static final int DARKEN_POINTS = 12;
    private static final double[] DARKEN_OFFSET_X = new double[DARKEN_POINTS];
    private static final double[] DARKEN_OFFSET_Z = new double[DARKEN_POINTS];

    static {
        for (int i = 0; i < DARKEN_POINTS; i++) {
            double angle = Math.toRadians(i * (360.0 / DARKEN_POINTS));
            DARKEN_OFFSET_X[i] = Math.cos(angle) * 1.5;
            DARKEN_OFFSET_Z[i] = Math.sin(angle) * 1.5;
        }
    }

    private final CustomDeathMessages plugin;
    private final BroadcastManager broadcastManager;

    public ParticleManager(CustomDeathMessages plugin) {
        this.plugin = plugin;
        this.broadcastManager = new BroadcastManager(plugin);
    }

    public void spawnDeathParticles(Player player, ConfigManager configManager, boolean isEpic) {
        ConfigManager.BroadcastMode mode = configManager.getBroadcastMode("particle");
        int radius = configManager.getBroadcastRadius("particle");

        spawnDeathParticlesWithMode(player, configManager, isEpic, mode, radius);
    }

    public void spawnDeathParticlesWithMode(Player player,
                                            ConfigManager configManager,
                                            boolean isEpic,
                                            ConfigManager.BroadcastMode mode,
                                            int radius) {
        String particle;
        int count;
        float offsetX, offsetY, offsetZ, extra;

        if (isEpic) {
            particle = plugin.getMessagesManager().getCachedEpicParticle();
            count = plugin.getMessagesManager().getEpicParticleCount();
            offsetX = 1.0f;
            offsetY = 1.0f;
            offsetZ = 1.0f;
            extra = 0.5f;
        } else {
            particle = configManager.getCachedParticle();
            count = configManager.getParticleCount();
            offsetX = configManager.getParticleOffsetX();
            offsetY = configManager.getParticleOffsetY();
            offsetZ = configManager.getParticleOffsetZ();
            extra = configManager.getParticleExtra();
        }

        broadcastManager.broadcastParticles(player, mode, radius, particle, count,
                offsetX, offsetY, offsetZ, extra);
    }

    public void spawnCustomParticles(Player victim,
                                     String particle,
                                     int count,
                                     double offsetX,
                                     double offsetY,
                                     double offsetZ,
                                     double extra,
                                     ConfigManager.BroadcastMode mode,
                                     int radius) {
        broadcastManager.broadcastParticles(victim, mode, radius, particle, count,
                offsetX, offsetY, offsetZ, extra);
    }

    public void spawnParticlesGlobal(Player victim,
                                     String particle,
                                     int count,
                                     double offsetX,
                                     double offsetY,
                                     double offsetZ,
                                     double extra) {
        broadcastManager.broadcastParticles(victim, ConfigManager.BroadcastMode.GLOBAL, 0,
                particle, count, offsetX, offsetY, offsetZ, extra);
    }

    public void spawnParticlesWorld(Player victim,
                                    String particle,
                                    int count,
                                    double offsetX,
                                    double offsetY,
                                    double offsetZ,
                                    double extra) {
        broadcastManager.broadcastParticles(victim, ConfigManager.BroadcastMode.WORLD, 0,
                particle, count, offsetX, offsetY, offsetZ, extra);
    }

    public void spawnParticlesRadius(Player victim,
                                     String particle,
                                     int count,
                                     double offsetX,
                                     double offsetY,
                                     double offsetZ,
                                     double extra,
                                     int radius) {
        broadcastManager.broadcastParticles(victim, ConfigManager.BroadcastMode.RADIUS, radius,
                particle, count, offsetX, offsetY, offsetZ, extra);
    }

    public void spawnParticlesVictimOnly(Player victim,
                                         String particle,
                                         int count,
                                         double offsetX,
                                         double offsetY,
                                         double offsetZ,
                                         double extra) {
        broadcastManager.broadcastParticles(victim, ConfigManager.BroadcastMode.VICTIM_ONLY, 0,
                particle, count, offsetX, offsetY, offsetZ, extra);
    }

    public void spawnDarkenScreenEffect(Player player,
                                        ConfigManager configManager,
                                        CustomDeathMessages plugin) {
        if (!configManager.isPlayDarkenScreenEffect()) {
            return;
        }

        int durationTicks = configManager.getDarkenEffectDurationTicks();
        String particle = configManager.getCachedDarkenParticle();

        ConfigManager.BroadcastMode mode = configManager.getBroadcastMode("darken-effect");
        int radius = configManager.getBroadcastRadius("darken-effect");

        new BukkitRunnable() {
            int ticks = 0;

            @Override
            public void run() {
                if (ticks >= durationTicks || !player.isOnline()) {
                    this.cancel();
                    return;
                }

                ticks++;

                Location base = player.getLocation();
                if (base == null || base.getWorld() == null) {
                    this.cancel();
                    return;
                }

                double baseX = base.getX();
                double baseY = base.getY() + 1.0;
                double baseZ = base.getZ();

                List<Player> recipients = broadcastManager.getRecipients(player, mode, radius);

                // The ring offsets are precomputed, so only the location is built per tick.
                for (int i = 0; i < DARKEN_POINTS; i++) {
                    Location particleLoc = new Location(base.getWorld(),
                            baseX + DARKEN_OFFSET_X[i],
                            baseY,
                            baseZ + DARKEN_OFFSET_Z[i]);

                    broadcastManager.broadcastParticlesTo(recipients, mode, particle,
                            1, 0, 0, 0, 0, particleLoc);
                }
            }
        }.runTaskTimer(plugin, 0L, 1L);
    }
}