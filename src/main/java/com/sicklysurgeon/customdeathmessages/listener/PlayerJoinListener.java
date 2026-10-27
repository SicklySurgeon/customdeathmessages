// 📄 src/main/java/com/sicklysurgeon/customdeathmessages/listener/PlayerJoinListener.java
package com.sicklysurgeon.customdeathmessages.listener;

import com.sicklysurgeon.customdeathmessages.CustomDeathMessages;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class PlayerJoinListener implements Listener {

    private final CustomDeathMessages plugin;

    public PlayerJoinListener(CustomDeathMessages plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        // Notificar al jugador si hay una actualización disponible
        plugin.getUpdateChecker().notifyPlayerIfUpdateAvailable(event.getPlayer());
    }
}