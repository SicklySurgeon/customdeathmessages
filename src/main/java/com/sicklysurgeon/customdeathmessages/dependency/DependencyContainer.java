package com.sicklysurgeon.customdeathmessages.dependency;

import com.sicklysurgeon.customdeathmessages.CustomDeathMessages;
import com.sicklysurgeon.customdeathmessages.command.BroadcastCommand;
import com.sicklysurgeon.customdeathmessages.command.ConfigCommand;
import com.sicklysurgeon.customdeathmessages.command.HelpCommand;
import com.sicklysurgeon.customdeathmessages.command.ReloadCommand;
import com.sicklysurgeon.customdeathmessages.command.test.TestCommand;
import com.sicklysurgeon.customdeathmessages.util.config.ConfigManager;
import com.sicklysurgeon.customdeathmessages.util.effects.VaultManager;
import com.sicklysurgeon.customdeathmessages.util.formatter.MessageFormatter;
import com.sicklysurgeon.customdeathmessages.util.managers.MessagesManager;
import com.sicklysurgeon.customdeathmessages.util.placeholder.PlaceholderRegistry;
import com.sicklysurgeon.customdeathmessages.util.tracking.DamageTracker;
import com.sicklysurgeon.customdeathmessages.util.tracking.UpdateChecker;

public class DependencyContainer {

    private final CustomDeathMessages plugin;

    private ConfigManager configManager;
    private MessagesManager messagesManager;
    private MessageFormatter messageFormatter;
    private PlaceholderRegistry placeholderRegistry;
    private UpdateChecker updateChecker;
    private VaultManager vaultManager;
    private DamageTracker damageTracker;

    private TestCommand testCommand;
    private HelpCommand helpCommand;
    private ConfigCommand configCommand;
    private ReloadCommand reloadCommand;
    private BroadcastCommand broadcastCommand;

    public DependencyContainer(CustomDeathMessages plugin) {
        this.plugin = plugin;
        initializeDependencies();
    }

    private void initializeDependencies() {
        plugin.getLogger().info("Initializing MessagesManager...");
        this.messagesManager = new MessagesManager(plugin);

        plugin.getLogger().info("Initializing ConfigManager...");
        this.configManager = new ConfigManager(plugin, messagesManager);

        plugin.getLogger().info("Initializing ConfigManager dependencies...");
        this.configManager.initialize();

        plugin.getLogger().info("Initializing PlaceholderRegistry...");
        this.placeholderRegistry = new PlaceholderRegistry();

        plugin.getLogger().info("Initializing MessageFormatter...");
        this.messageFormatter = new MessageFormatter(plugin, placeholderRegistry);

        plugin.getLogger().info("Initializing UpdateChecker...");
        this.updateChecker = new UpdateChecker(plugin, "customdeathmessages");

        plugin.getLogger().info("Initializing VaultManager...");
        this.vaultManager = new VaultManager(plugin);

        plugin.getLogger().info("Initializing DamageTracker...");
        this.damageTracker = new DamageTracker();

        plugin.getLogger().info("Initializing commands...");
        this.testCommand = new TestCommand(plugin);
        this.helpCommand = new HelpCommand(plugin);
        this.configCommand = new ConfigCommand(plugin);
        this.reloadCommand = new ReloadCommand(plugin);
        this.broadcastCommand = new BroadcastCommand(plugin);

        plugin.getLogger().info("All dependencies initialized successfully");
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }

    public MessagesManager getMessagesManager() {
        return messagesManager;
    }

    public MessageFormatter getMessageFormatter() {
        return messageFormatter;
    }

    public PlaceholderRegistry getPlaceholderRegistry() {
        return placeholderRegistry;
    }

    public UpdateChecker getUpdateChecker() {
        return updateChecker;
    }

    public VaultManager getVaultManager() {
        return vaultManager;
    }

    public DamageTracker getDamageTracker() {
        return damageTracker;
    }

    public TestCommand getTestCommand() {
        return testCommand;
    }

    public HelpCommand getHelpCommand() {
        return helpCommand;
    }

    public ConfigCommand getConfigCommand() {
        return configCommand;
    }

    public ReloadCommand getReloadCommand() {
        return reloadCommand;
    }

    public BroadcastCommand getBroadcastCommand() {
        return broadcastCommand;
    }

    public void reloadConfigManagers() {
        plugin.getLogger().info("Reloading config managers...");

        if (messagesManager != null) {
            messagesManager.reloadMessages();
        }

        if (configManager != null) {
            configManager.reloadConfig();
        }

        plugin.getLogger().info("Config managers reloaded");
    }

    public void reinitializeMessageFormatter() {
        plugin.getLogger().info("Reinitializing MessageFormatter...");
        this.messageFormatter = new MessageFormatter(plugin, placeholderRegistry);
        plugin.getLogger().info("MessageFormatter reinitialized");
    }
}