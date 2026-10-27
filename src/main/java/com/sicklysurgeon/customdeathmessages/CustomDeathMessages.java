// ===== src/main/java/com/sicklysurgeon/customdeathmessages/CustomDeathMessages.java =====

package com.sicklysurgeon.customdeathmessages;

import com.sicklysurgeon.customdeathmessages.dependency.DependencyContainer;
import com.sicklysurgeon.customdeathmessages.lifecycle.PluginLifecycleManager;
import com.sicklysurgeon.customdeathmessages.util.managers.MessagesManager;
import com.sicklysurgeon.customdeathmessages.util.config.ConfigManager;
import com.sicklysurgeon.customdeathmessages.util.formatter.MessageFormatter;
import com.sicklysurgeon.customdeathmessages.util.placeholder.PlaceholderRegistry;
import com.sicklysurgeon.customdeathmessages.util.tracking.UpdateChecker;
import com.sicklysurgeon.customdeathmessages.util.effects.VaultManager;
import com.sicklysurgeon.customdeathmessages.util.tracking.DamageTracker;

import org.bukkit.plugin.java.JavaPlugin;

public class CustomDeathMessages extends JavaPlugin {
    
    // Core Managers
    private DependencyContainer dependencyContainer;
    private PluginLifecycleManager lifecycleManager;
    
    @Override
    public void onEnable() {
        // ✅ PRIMERO: Guardar configuraciones predeterminadas
        saveDefaultConfig();
        
        // ✅ SEGUNDO: Inicializar DependencyContainer
        this.dependencyContainer = new DependencyContainer(this);
        
        // ✅ TERCERO: Inicializar PluginLifecycleManager con el DependencyContainer ya creado
        this.lifecycleManager = new PluginLifecycleManager(this, dependencyContainer);
        
        // ✅ CUARTO: Ejecutar la inicialización del plugin
        this.lifecycleManager.onEnable();
        
        getLogger().info("CustomDeathMessages v" + getDescription().getVersion() + " enabled!");
    }
    
    @Override
    public void onDisable() {
        if (lifecycleManager != null) {
            this.lifecycleManager.onDisable();
        }
        getLogger().info("CustomDeathMessages disabled!");
    }
    
    // ✅ Método auxiliar para obtener MessagesManager de forma segura
    public MessagesManager getMessagesManager() {
        if (dependencyContainer == null) {
            // Crear un MessagesManager temporal si dependencyContainer es null
            getLogger().warning("DependencyContainer is null! Creating temporary MessagesManager");
            return new MessagesManager(this);
        }
        return dependencyContainer.getMessagesManager();
    }
    
    // Getters para todos los componentes (con protección null)
    public DependencyContainer getDependencyContainer() {
        return dependencyContainer;
    }
    
    public ConfigManager getConfigManager() {
        return dependencyContainer != null ? dependencyContainer.getConfigManager() : null;
    }
    
    public MessageFormatter getMessageFormatter() {
        return dependencyContainer != null ? dependencyContainer.getMessageFormatter() : null;
    }
    
    public PlaceholderRegistry getPlaceholderRegistry() {
        return dependencyContainer != null ? dependencyContainer.getPlaceholderRegistry() : null;
    }
    
    public UpdateChecker getUpdateChecker() {
        return dependencyContainer != null ? dependencyContainer.getUpdateChecker() : null;
    }
    
    public VaultManager getVaultManager() {
        return dependencyContainer != null ? dependencyContainer.getVaultManager() : null;
    }
    
    public DamageTracker getDamageTracker() {
        return dependencyContainer != null ? dependencyContainer.getDamageTracker() : null;
    }
    
    public PluginLifecycleManager getLifecycleManager() {
        return lifecycleManager;
    }
}