package com.sicklysurgeon.customdeathmessages.initialization;

import com.sicklysurgeon.customdeathmessages.CustomDeathMessages;
import com.sicklysurgeon.customdeathmessages.dependency.DependencyContainer;
import com.sicklysurgeon.customdeathmessages.listener.*;
import org.bukkit.plugin.PluginManager;

public class EventRegistrar {

    private final CustomDeathMessages plugin;
    private final DependencyContainer dependencies;

    public EventRegistrar(CustomDeathMessages plugin, DependencyContainer dependencies) {
        this.plugin = plugin;
        this.dependencies = dependencies;
    }

    public void registerEvents() {
        PluginManager pluginManager = plugin.getServer().getPluginManager();

        pluginManager.registerEvents(
                new DeathMessageListener(
                        plugin,
                        dependencies.getMessageFormatter(),
                        dependencies.getConfigManager(),
                        dependencies.getDamageTracker()
                ),
                plugin
        );

        pluginManager.registerEvents(new RespawnListener(plugin), plugin);
        pluginManager.registerEvents(new DamageListener(plugin), plugin);
        pluginManager.registerEvents(new PlayerJoinListener(plugin), plugin);

        // ✅ FIX: 4 listeners, no 5
        plugin.getLogger().info("✓ Registered 4 event listeners");
    }
}