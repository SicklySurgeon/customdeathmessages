package com.sicklysurgeon.customdeathmessages.listener;

import com.sicklysurgeon.customdeathmessages.CustomDeathMessages;
import com.sicklysurgeon.customdeathmessages.util.config.ConfigManager;
import com.sicklysurgeon.customdeathmessages.util.formatter.ColorFormatter;
import com.sicklysurgeon.customdeathmessages.util.hooks.EssentialsHook;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerRespawnEvent;

public class RespawnListener implements Listener {

    private final ConfigManager configManager;

    public RespawnListener(CustomDeathMessages plugin) {
        this.configManager = plugin.getConfigManager();
    }

    @EventHandler
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        Player player = event.getPlayer();

        if (configManager == null || !configManager.isRespawnMessageEnabled()) {
            return;
        }

        String rawMessage = configManager.getRespawnMessage();
        String formattedMessage = rawMessage.replace("{player}", EssentialsHook.getNickname(player));
        formattedMessage = ColorFormatter.formatColors(formattedMessage, configManager.isLegacyColorSupport());

        player.sendMessage(formattedMessage);
    }
}