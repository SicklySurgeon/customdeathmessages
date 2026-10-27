package com.sicklysurgeon.customdeathmessages.command;

import com.sicklysurgeon.customdeathmessages.CustomDeathMessages;
import com.sicklysurgeon.customdeathmessages.util.formatter.ColorFormatter;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.util.Locale;

public class HelpCommand implements CommandExecutor {

    private final CustomDeathMessages plugin;

    public HelpCommand(CustomDeathMessages plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        FileConfiguration messages = plugin.getMessagesManager().getMessages();

        if (!(sender instanceof Player)) {
            String msg = messages.getString("system-messages.players-only",
                    "&cOnly players can use this command.");
            sender.sendMessage(formatColor(msg));
            return true;
        }

        Player player = (Player) sender;

        if (args.length == 0) {
            showGeneralHelp(player, messages);
        } else {
            showCommandHelp(player, args, messages);
        }

        return true;
    }

    private void showGeneralHelp(Player player, FileConfiguration messages) {
        String header = formatColor(messages.getString("help-system.general-header",
                "&6&l=== CustomDeathMessages Help ==="));

        String description = formatColor(messages.getString("help-system.general-description",
                "&eCustomize death messages, sounds, particles, and more."));

        String usage = formatColor(messages.getString("help-system.general-usage",
                "&7Use &f/cdm help <command> &7to see details about a specific command."));

        player.sendMessage(header);
        player.sendMessage(description);
        player.sendMessage("");

        sendCommandInfo(player, "/cdm test <cause>", "cdm.test",
                messages.getString("help-system.commands.test-desc", "Test a death message."));

        sendCommandInfo(player, "/cdm broadcast <mode>", "cdm.broadcast",
                messages.getString("help-system.commands.broadcast-desc",
                        "Test broadcast modes."));

        sendCommandInfo(player, "/cdm reload", "cdm.reload",
                messages.getString("help-system.commands.reload-desc", "Reload configuration."));

        sendCommandInfo(player, "/cdm config validate", "cdm.reload",
                messages.getString("help-system.commands.config-validate-desc",
                        "Validate configuration."));

        sendCommandInfo(player, "/cdm help", "cdm.use",
                messages.getString("help-system.commands.help-desc", "Show this help message."));

        player.sendMessage("");
        player.sendMessage(usage);
    }

    private void showCommandHelp(Player player, String[] args, FileConfiguration messages) {
        String fullCmd = String.join(" ", args).toLowerCase(Locale.ROOT);
        String key = "help-system.command-details." + fullCmd;

        if (!messages.contains(key) && args.length > 1) {
            key = "help-system.command-details." + args[0].toLowerCase(Locale.ROOT);
            fullCmd = args[0].toLowerCase(Locale.ROOT);
        }

        if (messages.contains(key)) {
            String title = formatColor(messages.getString(key + ".title",
                    "&6&lCommand: &f" + fullCmd));

            String desc = messages.getString(key + ".description", "No description.");
            String usage = messages.getString(key + ".usage", "Usage not specified.");
            String permission = messages.getString(key + ".permission", "None");

            if (desc == null) {
                desc = "No description.";
            }
            if (usage == null) {
                usage = "Usage not specified.";
            }
            if (permission == null) {
                permission = "None";
            }

            player.sendMessage(title);

            String descLabel = messages.getString("system-messages.help-detail-description",
                    "&7Description: &f{desc}");
            player.sendMessage(formatColor(descLabel.replace("{desc}", desc)));

            String usageLabel = messages.getString("system-messages.help-detail-usage",
                    "&7Usage: &f{usage}");
            player.sendMessage(formatColor(usageLabel.replace("{usage}", usage)));

            String permLabel = messages.getString("system-messages.help-detail-permission",
                    "&7Permission: &f{permission}");
            player.sendMessage(formatColor(permLabel.replace("{permission}", permission)));
        } else {
            String unknown = formatColor(messages.getString("help-system.unknown-command",
                    "&cCommand '&f{command}&c' not found. Use &f/cdm help&c to see the list."));

            unknown = unknown.replace("{command}", fullCmd);
            player.sendMessage(unknown);
        }
    }

    private void sendCommandInfo(Player player, String command, String permission, String description) {
        FileConfiguration messages = plugin.getMessagesManager().getMessages();

        String line = messages.getString("system-messages.help-command-line",
                "&a{command} &8- &7{description} &8[&fPerm: {permission}&8]");

        if (line == null) {
            line = "&a{command} &8- &7{description} &8[&fPerm: {permission}&8]";
        }

        line = line.replace("{command}", command)
                .replace("{description}", description)
                .replace("{permission}", permission);

        player.sendMessage(formatColor(line));
    }

    private String formatColor(String msg) {
        if (msg == null) {
            return "";
        }

        return ColorFormatter.formatColors(msg, plugin.getConfigManager().isLegacyColorSupport());
    }
}