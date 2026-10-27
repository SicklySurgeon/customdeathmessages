// src/main/java/com/sicklysurgeon/customdeathmessages/initialization/PluginInitializer.java
package com.sicklysurgeon.customdeathmessages.initialization;

import com.sicklysurgeon.customdeathmessages.CustomDeathMessages;
import com.sicklysurgeon.customdeathmessages.dependency.DependencyContainer;

public class PluginInitializer {
    private final CustomDeathMessages plugin;
    private final DependencyContainer dependencies;
    
    public PluginInitializer(CustomDeathMessages plugin, DependencyContainer dependencies) {
        this.plugin = plugin;
        this.dependencies = dependencies;
    }
    
    public void initialize() {
        // Ensure data folder exists
        if (!plugin.getDataFolder().exists()) {
            plugin.getDataFolder().mkdirs();
        }
        
        // Save default configuration files
        plugin.saveDefaultConfig();
        
        // Initialize placeholder registry with plugin reference
        dependencies.getPlaceholderRegistry().setPlugin(plugin);
        
        plugin.getLogger().info("✓ Plugin initialized successfully");
    }
}