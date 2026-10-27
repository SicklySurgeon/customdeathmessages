// src/main/java/com/sicklysurgeon/customdeathmessages/command/ReloadCommand.java
package com.sicklysurgeon.customdeathmessages.command;

import com.sicklysurgeon.customdeathmessages.CustomDeathMessages;
import com.sicklysurgeon.customdeathmessages.lifecycle.ReloadManager;
import com.sicklysurgeon.customdeathmessages.util.formatter.ColorFormatter;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class ReloadCommand implements CommandExecutor {
    private final CustomDeathMessages plugin;
    
    public ReloadCommand(CustomDeathMessages plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("cdm.reload")) {
            String msg = plugin.getMessagesManager().getMessages()
                .getString("system-messages.no-permission-reload", 
                    "§cYou do not have permission to reload the configuration.");
            if (msg == null) {
                msg = "§cYou do not have permission to reload the configuration.";
            }
            sender.sendMessage(ColorFormatter.formatColors(msg,
                    plugin.getConfigManager().isLegacyColorSupport()));
            return true;
        }
        
        // Usar el ReloadManager del lifecycle manager
        ReloadManager reloadManager = plugin.getLifecycleManager().getReloadManager();
        return reloadManager.reloadConfiguration(sender);
    }
}