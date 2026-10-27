package com.sicklysurgeon.customdeathmessages.listener;

import com.sicklysurgeon.customdeathmessages.CustomDeathMessages;
import com.sicklysurgeon.customdeathmessages.util.broadcast.BroadcastManager;
import com.sicklysurgeon.customdeathmessages.util.config.ConfigManager;
import com.sicklysurgeon.customdeathmessages.util.config.PermissionGroup;
import com.sicklysurgeon.customdeathmessages.util.context.DeathContext;
import com.sicklysurgeon.customdeathmessages.util.effects.ParticleManager;
import com.sicklysurgeon.customdeathmessages.util.effects.SoundManager;
import com.sicklysurgeon.customdeathmessages.util.effects.VaultManager;
import com.sicklysurgeon.customdeathmessages.util.formatter.MessageFormatter;
import com.sicklysurgeon.customdeathmessages.util.tracking.DamageTracker;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class DeathMessageListener implements Listener {

    private final MessageFormatter messageFormatter;
    private final ConfigManager configManager;
    private final CustomDeathMessages plugin;
    private final DamageTracker damageTracker;
    private final BroadcastManager broadcastManager;
    private final SoundManager soundManager;
    private final ParticleManager particleManager;

    public DeathMessageListener(CustomDeathMessages plugin,
                                MessageFormatter messageFormatter,
                                ConfigManager configManager,
                                DamageTracker damageTracker) {
        this.plugin = plugin;
        this.messageFormatter = messageFormatter;
        this.configManager = configManager;
        this.damageTracker = damageTracker;
        this.broadcastManager = new BroadcastManager(plugin);
        this.soundManager = new SoundManager(plugin);
        this.particleManager = new ParticleManager(plugin);
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player victim = event.getEntity();

        if (victim.hasPermission("cdm.bypass")) {
            damageTracker.clear(victim);
            return;
        }

        String messageGroup = configManager.resolveMessageGroup(victim);
        String costGroup = PermissionGroup.fromPlayer(victim).getConfigKey();
        double cost = configManager.getDeathMessageCost();

        VaultManager vaultManager = plugin.getVaultManager();
        if (vaultManager != null && !vaultManager.chargePlayer(victim, cost, costGroup)) {
            damageTracker.clear(victim);
            return;
        }

        try {
            boolean isEpic = Math.random() < configManager.getEpicDeathChance();
            DeathContext context = messageFormatter.getContextFactory().createFromDeathEvent(event);
            String formattedMessage = messageFormatter.formatDeathMessage(context, isEpic);

            event.setDeathMessage(null);

            if (configManager.isLogDeathMessages()) {
                plugin.getLogger().info("Death Message: "
                        + org.bukkit.ChatColor.stripColor(formattedMessage));
            }

            if (configManager.isMessageToVictim()) {
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        if (victim.isOnline()) {
                            victim.sendMessage(formattedMessage);
                        }
                    }
                }.runTaskLater(plugin, 1L);
            }

            List<Player> recipients;

            if (configManager.getMessageToRadius() > 0) {
                double radius = configManager.getMessageToRadius();
                recipients = victim.getWorld().getNearbyEntities(
                                victim.getLocation(),
                                radius,
                                radius,
                                radius
                        ).stream()
                        .filter(e -> e instanceof Player)
                        .map(e -> (Player) e)
                        .filter(p -> !p.equals(victim))
                        .collect(Collectors.toList());
            } else if (configManager.isMessageToWorld()) {
                recipients = new ArrayList<>(victim.getWorld().getPlayers());
                recipients.remove(victim);
            } else {
                recipients = new ArrayList<>();
            }

            for (Player p : recipients) {
                p.sendMessage(formattedMessage);
            }

            if (configManager.isPlaySoundOnDeath()) {
                soundManager.playDeathSound(victim, configManager, plugin, messageGroup, isEpic);
            }

            if (configManager.isPlayParticlesOnDeath()) {
                particleManager.spawnDeathParticles(victim, configManager, isEpic);
            }

            if (configManager.isPlayTitleOnDeath()) {
                ConfigManager.BroadcastMode mode = configManager.getBroadcastMode("title");
                int radius = configManager.getBroadcastRadius("title");

                String title;
                String subtitle;

                if (isEpic) {
                    title = plugin.getMessagesManager().getEpicTitle();
                    subtitle = plugin.getMessagesManager().getEpicSubtitle();
                } else {
                    title = configManager.getDeathTitle();
                    subtitle = configManager.getDeathSubtitle();
                }

                title = messageFormatter.formatVisualMessage(title, context);
                subtitle = messageFormatter.formatVisualMessage(subtitle, context);

                broadcastManager.broadcastTitle(victim, mode, radius,
                        title, subtitle, 10, 70, 20);
            }

            if (configManager.isPlayActionBarOnDeath()) {
                ConfigManager.BroadcastMode mode = configManager.getBroadcastMode("actionbar");
                int radius = configManager.getBroadcastRadius("actionbar");

                String actionBarMsg = configManager.getDeathActionBar();
                actionBarMsg = messageFormatter.formatVisualMessage(actionBarMsg, context);

                broadcastManager.broadcastActionBar(victim, mode, radius, actionBarMsg);
            }

            particleManager.spawnDarkenScreenEffect(victim, configManager, plugin);

            if (isEpic) {
                Location loc = victim.getLocation();
                if (loc.getWorld() != null) {
                    loc.getWorld().strikeLightningEffect(loc);
                }
            }

        } catch (Exception e) {
            plugin.getLogger().severe("Error processing death message: " + e.getMessage());
            e.printStackTrace();
        } finally {
            damageTracker.clear(victim);
        }
    }
}