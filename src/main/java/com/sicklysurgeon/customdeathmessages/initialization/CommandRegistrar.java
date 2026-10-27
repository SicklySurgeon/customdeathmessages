package com.sicklysurgeon.customdeathmessages.initialization;

import com.sicklysurgeon.customdeathmessages.CustomDeathMessages;
import com.sicklysurgeon.customdeathmessages.dependency.DependencyContainer;
import com.sicklysurgeon.customdeathmessages.command.UnifiedTabCompleter;
import com.sicklysurgeon.customdeathmessages.util.formatter.ColorFormatter;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.PluginCommand;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.Arrays;
import java.util.Locale;

public class CommandRegistrar {

    private final CustomDeathMessages plugin;
    private final DependencyContainer dependencies;

    public CommandRegistrar(CustomDeathMessages plugin, DependencyContainer dependencies) {
        this.plugin = plugin;
        this.dependencies = dependencies;
    }

    public void registerCommands() {
        registerSimpleCommand("cdmreload", dependencies.getReloadCommand());
        registerSimpleCommand("cdmconfig", dependencies.getConfigCommand());
        registerSimpleCommand("cdmhelp", dependencies.getHelpCommand());
        registerSimpleCommand("cdmbroadcast", dependencies.getBroadcastCommand());
        registerMainCommand();
        plugin.getLogger().info("✓ Registered plugin commands");
    }

    private void registerSimpleCommand(String commandName, CommandExecutor executor) {
        PluginCommand command = plugin.getCommand(commandName);
        if (command != null) {
            command.setExecutor(executor);
        } else {
            plugin.getLogger().warning("Failed to register command: " + commandName);
        }
    }

    private void registerMainCommand() {
        PluginCommand cdmCommand = plugin.getCommand("cdm");
        if (cdmCommand == null) {
            plugin.getLogger().warning("Failed to register main command: cdm");
            return;
        }
        CommandExecutor unifiedExecutor = createUnifiedExecutor();
        cdmCommand.setExecutor(unifiedExecutor);
        cdmCommand.setTabCompleter(new UnifiedTabCompleter());
    }

    private CommandExecutor createUnifiedExecutor() {
        return new CommandExecutor() {
            @Override
            public boolean onCommand(org.bukkit.command.CommandSender sender,
                                     org.bukkit.command.Command command,
                                     String label, String[] args) {
                if (args.length == 0) {
                    showGeneralHelp(sender);
                    return true;
                }

                String subCommand = args[0].toLowerCase(Locale.ROOT);
                String[] subArgs = Arrays.copyOfRange(args, 1, args.length);

                switch (subCommand) {
                    case "test":
                        return dependencies.getTestCommand().onCommand(sender, command, label, subArgs);
                    case "help":
                        return dependencies.getHelpCommand().onCommand(sender, command, label, subArgs);
                    case "config":
                        return dependencies.getConfigCommand().onCommand(sender, command, label, subArgs);
                    case "reload":
                        return dependencies.getReloadCommand().onCommand(sender, command, label, subArgs);
                    case "broadcast":
                        return dependencies.getBroadcastCommand().onCommand(sender, command, label, subArgs);
                    default:
                        // ✅ FIX: Usar messages.yml
                        FileConfiguration msgs = plugin.getMessagesManager().getMessages();
                        String unknownMsg = msgs.getString("system-messages.unknown-subcommand",
                                "&cUnknown command. Use /cdm help to see available commands.");
                        if (unknownMsg == null) {
                            unknownMsg = "&cUnknown command. Use /cdm help to see available commands.";
                        }
                        sender.sendMessage(ColorFormatter.formatColors(unknownMsg,
                                plugin.getConfigManager().isLegacyColorSupport()));
                        return true;
                }
            }
        };
    }

    // ✅ FIX: Todos los mensajes desde messages.yml
    private void showGeneralHelp(org.bukkit.command.CommandSender sender) {
        FileConfiguration msgs = plugin.getMessagesManager().getMessages();

        String title = msgs.getString("system-messages.general-help-title",
                "&6CustomDeathMessages v{version}");
        if (title == null) {
            title = "&6CustomDeathMessages v{version}";
        }
        title = title.replace("{version}", plugin.getDescription().getVersion());
        sender.sendMessage(ColorFormatter.formatColors(title,
                plugin.getConfigManager().isLegacyColorSupport()));

        String[] keys = {
                "system-messages.general-help-test",
                "system-messages.general-help-broadcast",
                "system-messages.general-help-help",
                "system-messages.general-help-reload",
                "system-messages.general-help-config"
        };
        String[] defaults = {
                "&b/cdm test &7- Test death messages",
                "&b/cdm broadcast &7- Test broadcast modes",
                "&b/cdm help &7- Show detailed help",
                "&b/cdm reload &7- Reload configuration",
                "&b/cdm config validate &7- Validate configuration"
        };

        for (int i = 0; i < keys.length; i++) {
            String line = msgs.getString(keys[i], defaults[i]);
            if (line == null) {
                line = defaults[i];
            }
            sender.sendMessage(ColorFormatter.formatColors(line,
                    plugin.getConfigManager().isLegacyColorSupport()));
        }
    }
}