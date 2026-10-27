// src/main/java/com/sicklysurgeon/customdeathmessages/lifecycle/PluginLifecycleManager.java
package com.sicklysurgeon.customdeathmessages.lifecycle;

import com.sicklysurgeon.customdeathmessages.CustomDeathMessages;
import com.sicklysurgeon.customdeathmessages.dependency.DependencyContainer;
import com.sicklysurgeon.customdeathmessages.initialization.*;
import com.sicklysurgeon.customdeathmessages.util.config.ConfigValidator;
import com.sicklysurgeon.customdeathmessages.util.formatter.ColorFormatter;

import net.md_5.bungee.api.ChatColor;
import org.bukkit.event.HandlerList;
import java.util.List;

public class PluginLifecycleManager {
    private final CustomDeathMessages plugin;
    private final DependencyContainer dependencies;
    private final PluginInitializer pluginInitializer;
    private final EventRegistrar eventRegistrar;
    private final CommandRegistrar commandRegistrar;
    private final ReloadManager reloadManager;
    
    public PluginLifecycleManager(CustomDeathMessages plugin, DependencyContainer dependencies) {
        this.plugin = plugin;
        this.dependencies = dependencies;
        this.pluginInitializer = new PluginInitializer(plugin, dependencies);
        this.eventRegistrar = new EventRegistrar(plugin, dependencies);
        this.commandRegistrar = new CommandRegistrar(plugin, dependencies);
        this.reloadManager = new ReloadManager(plugin, dependencies);
    }
    
    public void onEnable() {
        // Initialize core components
        pluginInitializer.initialize();
        
        // Register events and commands
        eventRegistrar.registerEvents();
        commandRegistrar.registerCommands();
        
        // Start update checker
        dependencies.getUpdateChecker().checkForUpdates();
        
        // Log startup message
        plugin.getLogger().info(consoleMessage("plugin-enabled",
                "Plugin loaded successfully."));

        // Run initial configuration validation
        runInitialValidation();
    }
    
    public void onDisable() {
        // Unregister all event handlers
        HandlerList.unregisterAll(plugin);
        
        // Log shutdown message
        plugin.getLogger().info(consoleMessage("plugin-disabled", "Plugin disabled."));
    }

    /**
     * Console messages should not contain raw '&'/hex color codes. Format them
     * first (so hex is translated) and then strip the section signs.
     */
    private String consoleMessage(String key, String defaultMsg) {
        String raw = dependencies.getMessagesManager().getMessages()
                .getString("system-messages." + key, defaultMsg);
        String message = raw == null ? defaultMsg : raw;

        return ChatColor.stripColor(ColorFormatter.formatColors(message, false));
    }
    
    private void runInitialValidation() {
        if (dependencies.getConfigManager().getConfig().getBoolean("run-initial-validation", true)) {
            ConfigValidator startupValidator = new ConfigValidator(plugin);
            List<String> report = startupValidator.generateValidationReport();
            
            plugin.getLogger().info("=== CustomDeathMessages - Initial Configuration Validation ===");
            for (String line : report) {
                plugin.getLogger().info(ChatColor.stripColor(line));
            }
            plugin.getLogger().info("=============================================================");
        }
        
        if (!dependencies.getConfigManager().isUsePermissionBasedMessages()) {
            plugin.getLogger().warning("⚠️ 'use-permission-based-messages' is DISABLED. Group messages (vip/admin) will be ignored!");
            plugin.getLogger().warning("   Set it to 'true' in config.yml to enable permission-based messages.");
        }
    }
    
    public void reinitializeDependentComponents() {
        reloadManager.reinitializeDependentComponents();
    }
    
    public ReloadManager getReloadManager() {
        return reloadManager;
    }
}