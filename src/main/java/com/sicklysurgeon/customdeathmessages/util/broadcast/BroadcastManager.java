package com.sicklysurgeon.customdeathmessages.util.broadcast;

import com.sicklysurgeon.customdeathmessages.CustomDeathMessages;
import com.sicklysurgeon.customdeathmessages.util.config.ConfigManager;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class BroadcastManager {

    public BroadcastManager(CustomDeathMessages plugin) {
    }

    public List<Player> getRecipients(Player victim, ConfigManager.BroadcastMode mode, int radius) {
        if (victim == null) {
            return new ArrayList<>();
        }

        switch (mode) {
            case GLOBAL:
                return new ArrayList<>(Bukkit.getOnlinePlayers());

            case WORLD:
                return new ArrayList<>(victim.getWorld().getPlayers());

            case RADIUS:
                Location center = victim.getLocation();
                return victim.getWorld().getPlayers().stream()
                        .filter(p -> p.getLocation().distance(center) <= radius)
                        .collect(Collectors.toList());

            case VICTIM_ONLY:
                List<Player> singleList = new ArrayList<>();
                singleList.add(victim);
                return singleList;

            default:
                return new ArrayList<>(victim.getWorld().getPlayers());
        }
    }

    public int getRecipientCount(Player victim, ConfigManager.BroadcastMode mode, int radius) {
        return getRecipients(victim, mode, radius).size();
    }

    public void broadcastSound(Player victim,
                               ConfigManager.BroadcastMode mode,
                               int radius,
                               Sound sound,
                               float volume,
                               float pitch) {
        List<Player> recipients = getRecipients(victim, mode, radius);

        for (Player recipient : recipients) {
            if (recipient.isOnline()) {
                Location playLoc = (mode == ConfigManager.BroadcastMode.GLOBAL
                        || mode == ConfigManager.BroadcastMode.VICTIM_ONLY)
                        ? recipient.getLocation()
                        : victim.getLocation();

                recipient.playSound(playLoc, sound, volume, pitch);
            }
        }
    }

    public void broadcastParticles(Player victim,
                                   ConfigManager.BroadcastMode mode,
                                   int radius,
                                   Particle particle,
                                   int count,
                                   double offsetX,
                                   double offsetY,
                                   double offsetZ,
                                   double extra) {
        if (victim == null) {
            return;
        }

        Location particleLocation = victim.getLocation();

        if (mode == ConfigManager.BroadcastMode.GLOBAL) {
            for (Player recipient : Bukkit.getOnlinePlayers()) {
                if (!recipient.isOnline()) {
                    continue;
                }

                Location target = recipient.getWorld().equals(particleLocation.getWorld())
                        ? particleLocation
                        : recipient.getLocation();

                recipient.spawnParticle(particle, target, count,
                        offsetX, offsetY, offsetZ, extra);
            }
            return;
        }

        if (mode == ConfigManager.BroadcastMode.WORLD) {
            victim.getWorld().spawnParticle(particle, particleLocation, count,
                    offsetX, offsetY, offsetZ, extra);
            return;
        }

        List<Player> recipients = getRecipients(victim, mode, radius);
        for (Player recipient : recipients) {
            if (recipient.isOnline()) {
                recipient.spawnParticle(particle, particleLocation, count,
                        offsetX, offsetY, offsetZ, extra);
            }
        }
    }

    public void broadcastParticles(Player victim,
                                   ConfigManager.BroadcastMode mode,
                                   int radius,
                                   Particle particle,
                                   int count,
                                   double offsetX,
                                   double offsetY,
                                   double offsetZ,
                                   double extra,
                                   Location location) {
        broadcastParticlesTo(getRecipients(victim, mode, radius), mode, particle,
                count, offsetX, offsetY, offsetZ, extra, location);
    }

    /**
     * Sends particles to an already resolved recipient list. Useful for repeating
     * effects (like the darken screen ring) where the recipient lookup should not
     * be repeated every tick.
     */
    public void broadcastParticlesTo(List<Player> recipients,
                                     ConfigManager.BroadcastMode mode,
                                     Particle particle,
                                     int count,
                                     double offsetX,
                                     double offsetY,
                                     double offsetZ,
                                     double extra,
                                     Location location) {
        for (Player recipient : recipients) {
            if (!recipient.isOnline()) {
                continue;
            }

            Location target = location;

            if (mode == ConfigManager.BroadcastMode.GLOBAL
                    && !recipient.getWorld().equals(location.getWorld())) {
                target = recipient.getLocation();
            }

            recipient.spawnParticle(particle, target, count,
                    offsetX, offsetY, offsetZ, extra);
        }
    }

    public void broadcastTitle(Player victim,
                               ConfigManager.BroadcastMode mode,
                               int radius,
                               String title,
                               String subtitle,
                               int fadeIn,
                               int stay,
                               int fadeOut) {
        List<Player> recipients = getRecipients(victim, mode, radius);

        for (Player recipient : recipients) {
            if (recipient.isOnline()) {
                recipient.sendTitle(title, subtitle, fadeIn, stay, fadeOut);
            }
        }
    }

    public void broadcastActionBar(Player victim,
                                   ConfigManager.BroadcastMode mode,
                                   int radius,
                                   String message) {
        List<Player> recipients = getRecipients(victim, mode, radius);

        for (Player recipient : recipients) {
            if (recipient.isOnline()) {
                recipient.spigot().sendMessage(
                        ChatMessageType.ACTION_BAR,
                        TextComponent.fromLegacyText(message)
                );
            }
        }
    }

    public void broadcastMessage(Player victim,
                                 ConfigManager.BroadcastMode mode,
                                 int radius,
                                 String message) {
        List<Player> recipients = getRecipients(victim, mode, radius);

        for (Player recipient : recipients) {
            if (recipient.isOnline()) {
                recipient.sendMessage(message);
            }
        }
    }

    public void broadcastGlobalMessage(String message) {
        Bukkit.getOnlinePlayers().forEach(p -> p.sendMessage(message));
    }

    public void broadcastGlobalSound(Sound sound, float volume, float pitch) {
        Bukkit.getOnlinePlayers().forEach(p ->
                p.playSound(p.getLocation(), sound, volume, pitch));
    }

    public String testBroadcastModes(Player tester) {
        StringBuilder result = new StringBuilder();

        result.append("§6§l=== Broadcast Mode Test ===\n");

        int globalCount = Bukkit.getOnlinePlayers().size();
        result.append("§eGLOBAL: §fWould reach ").append(globalCount).append(" players\n");

        int worldCount = tester.getWorld().getPlayers().size();
        result.append("§eWORLD: §fWould reach ").append(worldCount)
                .append(" players in world '").append(tester.getWorld().getName()).append("'\n");

        int[] testRadii = {10, 30, 50, 100};
        for (int radius : testRadii) {
            long radiusCount = tester.getWorld().getPlayers().stream()
                    .filter(p -> p.getLocation().distance(tester.getLocation()) <= radius)
                    .count();

            result.append("§eRADIUS(").append(radius).append("): §fWould reach ")
                    .append(radiusCount).append(" players\n");
        }

        result.append("§eVICTIM_ONLY: §fWould reach 1 player (you)\n");

        return result.toString();
    }
}