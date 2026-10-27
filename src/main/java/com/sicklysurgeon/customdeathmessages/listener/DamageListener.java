package com.sicklysurgeon.customdeathmessages.listener;

import com.sicklysurgeon.customdeathmessages.CustomDeathMessages;
import com.sicklysurgeon.customdeathmessages.util.formatter.MessageFormatter;
import com.sicklysurgeon.customdeathmessages.util.managers.MessageSelector;
import com.sicklysurgeon.customdeathmessages.util.tracking.DamageTracker;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;

public class DamageListener implements Listener {

    private final CustomDeathMessages plugin;
    private final DamageTracker damageTracker;

    public DamageListener(CustomDeathMessages plugin) {
        this.plugin = plugin;
        this.damageTracker = plugin.getDamageTracker();
    }

    @EventHandler
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        if (event.getEntity() instanceof Player) {
            Player victim = (Player) event.getEntity();
            Entity damager = event.getDamager();

            if (damageTracker != null) {
                damageTracker.trackDamage(victim, damager);
            }
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        if (damageTracker != null) {
            damageTracker.clear(event.getPlayer());
        }
        clearMessageHistory(event.getPlayer());
    }

    @EventHandler
    public void onRespawn(PlayerRespawnEvent event) {
        if (damageTracker != null) {
            damageTracker.clear(event.getPlayer());
        }
        clearMessageHistory(event.getPlayer());
    }

    private void clearMessageHistory(Player player) {
        if (player == null) {
            return;
        }

        MessageFormatter formatter = plugin.getMessageFormatter();
        if (formatter == null) {
            return;
        }

        MessageSelector selector = formatter.getMessageSelector();
        if (selector != null) {
            selector.clearHistory(player.getUniqueId());
        }
    }
}