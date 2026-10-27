package com.sicklysurgeon.customdeathmessages.lifecycle;

import com.sicklysurgeon.customdeathmessages.CustomDeathMessages;
import com.sicklysurgeon.customdeathmessages.dependency.DependencyContainer;
import com.sicklysurgeon.customdeathmessages.listener.DamageListener;
import com.sicklysurgeon.customdeathmessages.listener.DeathMessageListener;
import com.sicklysurgeon.customdeathmessages.listener.PlayerJoinListener;
import com.sicklysurgeon.customdeathmessages.listener.RespawnListener;
import com.sicklysurgeon.customdeathmessages.util.config.ConfigManager;
import com.sicklysurgeon.customdeathmessages.util.config.ConfigValidator;
import com.sicklysurgeon.customdeathmessages.util.formatter.ColorFormatter;
import com.sicklysurgeon.customdeathmessages.util.managers.MessagesManager;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.HandlerList;

import java.io.File;
import java.util.List;

public class ReloadManager {

    private final CustomDeathMessages plugin;
    private final DependencyContainer dependencies;

    public ReloadManager(CustomDeathMessages plugin, DependencyContainer dependencies) {
        this.plugin = plugin;
        this.dependencies = dependencies;
    }

    public void reinitializeDependentComponents() {
        dependencies.reinitializeMessageFormatter();

        HandlerList.unregisterAll(plugin);

        plugin.getServer().getPluginManager().registerEvents(
                new DeathMessageListener(
                        plugin,
                        dependencies.getMessageFormatter(),
                        dependencies.getConfigManager(),
                        dependencies.getDamageTracker()
                ),
                plugin
        );

        plugin.getServer().getPluginManager().registerEvents(new RespawnListener(plugin), plugin);
        plugin.getServer().getPluginManager().registerEvents(new DamageListener(plugin), plugin);
        plugin.getServer().getPluginManager().registerEvents(new PlayerJoinListener(plugin), plugin);

        plugin.getLogger().info("Reinitialized listeners and formatters after config reload");
    }

    private String getMsg(String key, String defaultMsg) {
        try {
            FileConfiguration msgs = dependencies.getMessagesManager().getMessages();
            String raw = msgs.getString("system-messages." + key, defaultMsg);

            return ColorFormatter.formatColors(raw == null ? defaultMsg : raw,
                    dependencies.getConfigManager().isLegacyColorSupport());
        } catch (Exception e) {
            return ChatColor.translateAlternateColorCodes('&', defaultMsg);
        }
    }

    public boolean reloadConfiguration(CommandSender sender) {
        sender.sendMessage(getMsg("reload-starting",
                "&e[CustomDeathMessages] Starting configuration reload..."));

        try {
            plugin.saveDefaultConfig();

            File configFile = new File(plugin.getDataFolder(), "config.yml");
            FileConfiguration newConfig = YamlConfiguration.loadConfiguration(configFile);

            MessagesManager tempMessagesManager = new MessagesManager(plugin);
            ConfigManager tempConfigManager = new ConfigManager(plugin, tempMessagesManager);
            tempConfigManager.loadConfig(newConfig);

            ConfigValidator tempValidator = new ConfigValidator(tempConfigManager, tempMessagesManager.getMessages());
            List<String> report = tempValidator.generateValidationReport();

            boolean hasErrors = report.stream().anyMatch(s -> s.contains("❌"));
            boolean ignoreErrors = tempConfigManager.getConfig().getBoolean("ignore-validation-errors", false);

            if (hasErrors && !ignoreErrors) {
                sender.sendMessage(getMsg("reload-validation-failed",
                        "&c[CustomDeathMessages] Validation failed. Reload aborted."));
                report.forEach(sender::sendMessage);
                return false;
            }

            dependencies.reloadConfigManagers();
            reinitializeDependentComponents();

            String successMsg = dependencies.getMessagesManager().getMessages()
                    .getString("system-messages.reload-success",
                            "&a[CustomDeathMessages] Configuration reloaded successfully.");
            if (successMsg == null) {
                successMsg = "&a[CustomDeathMessages] Configuration reloaded successfully.";
            }
            sender.sendMessage(ColorFormatter.formatColors(successMsg,
                    dependencies.getConfigManager().isLegacyColorSupport()));

            if (hasErrors) {
                sender.sendMessage(getMsg("reload-with-warnings",
                        "&e[CustomDeathMessages] Reloaded with validation errors (ignored)."));
                report.forEach(sender::sendMessage);
            }

            plugin.getLogger().info("[CustomDeathMessages] Configuration reloaded by " + sender.getName());
            return true;

        } catch (Exception e) {
            sender.sendMessage(getMsg("reload-load-failed",
                    "&c[CustomDeathMessages] Failed to load config or messages. Check console."));
            plugin.getLogger().severe("Reload failed: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public void reloadConfigManagersOnly() {
        dependencies.reloadConfigManagers();
        plugin.getLogger().info("Configuration managers reloaded");
    }
}