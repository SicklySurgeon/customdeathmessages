package com.sicklysurgeon.customdeathmessages.command;

import com.sicklysurgeon.customdeathmessages.CustomDeathMessages;
import com.sicklysurgeon.customdeathmessages.util.config.ConfigValidator;
import com.sicklysurgeon.customdeathmessages.util.formatter.ColorFormatter;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import java.util.List;

public class ConfigCommand implements CommandExecutor {

    private final CustomDeathMessages plugin;

    public ConfigCommand(CustomDeathMessages plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("cdm.reload")) {
            // ✅ FIX: Usar messages.yml en vez de hardcodear
            String msg = plugin.getMessagesManager().getMessages()
                    .getString("system-messages.no-permission-config",
                            "&cYou do not have permission to use this command.");
            if (msg == null) {
                msg = "&cYou do not have permission to use this command.";
            }
            sender.sendMessage(ColorFormatter.formatColors(msg,
                    plugin.getConfigManager().isLegacyColorSupport()));
            return true;
        }

        if (args.length != 1 || !args[0].equalsIgnoreCase("validate")) {
            // ✅ FIX: Usar messages.yml en vez de hardcodear
            String msg = plugin.getMessagesManager().getMessages()
                    .getString("system-messages.usage-config",
                            "&cUsage: /cdm config validate");
            if (msg == null) {
                msg = "&cUsage: /cdm config validate";
            }
            sender.sendMessage(ColorFormatter.formatColors(msg,
                    plugin.getConfigManager().isLegacyColorSupport()));
            return true;
        }

        ConfigValidator validator = new ConfigValidator(plugin);
        List<String> report = validator.generateValidationReport();
        for (String line : report) {
            sender.sendMessage(line);
        }
        sender.sendMessage(validator.getSummary());
        return true;
    }
}