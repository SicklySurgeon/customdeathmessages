package com.sicklysurgeon.customdeathmessages.util.formatter;

import com.sicklysurgeon.customdeathmessages.CustomDeathMessages;
import com.sicklysurgeon.customdeathmessages.util.context.DeathContext;
import com.sicklysurgeon.customdeathmessages.util.context.DeathContextFactory;
import com.sicklysurgeon.customdeathmessages.util.managers.MessageSelector;
import com.sicklysurgeon.customdeathmessages.util.placeholder.PlaceholderRegistry;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;

import java.util.Locale;

public class MessageFormatter {

    private final CustomDeathMessages plugin;
    private final PlaceholderRegistry placeholderRegistry;
    private final MessageSelector messageSelector;
    private final PlaceholderAPIExpander papiExpander;
    private final DeathContextFactory contextFactory;

    public MessageFormatter(CustomDeathMessages plugin, PlaceholderRegistry placeholderRegistry) {
        this.plugin = plugin;
        this.placeholderRegistry = placeholderRegistry;
        this.messageSelector = new MessageSelector(plugin);
        this.papiExpander = new PlaceholderAPIExpander(plugin);
        this.contextFactory = new DeathContextFactory(plugin);
        this.placeholderRegistry.setPlugin(plugin);
    }

    public String formatDeathMessage(PlayerDeathEvent event) {
        DeathContext context = contextFactory.createFromDeathEvent(event);
        return formatMessage(context);
    }

    public String formatDeathMessage(PlayerDeathEvent event, boolean isEpic) {
        DeathContext context = contextFactory.createFromDeathEvent(event);
        return formatDeathMessage(context, isEpic);
    }

    public String formatDeathMessage(DeathContext context, boolean isEpic) {
        String rawMessage = messageSelector.selectRawMessage(context, isEpic);
        return formatMessageWithRawText(context, rawMessage);
    }

    public String formatTestMessage(Player victim,
                                    String killerName,
                                    String killerSource,
                                    String causeName) {
        try {
            EntityDamageEvent.DamageCause cause =
                    EntityDamageEvent.DamageCause.valueOf(causeName.toUpperCase(Locale.ROOT));
            boolean isEpic = Math.random() < plugin.getConfigManager().getEpicDeathChance();
            return formatTestMessage(victim, killerName, killerSource, cause, isEpic);
        } catch (IllegalArgumentException e) {
            return ColorFormatter.formatColors("&cInvalid cause: " + causeName,
                    plugin.getConfigManager().isLegacyColorSupport());
        }
    }

    public String formatTestMessage(Player victim,
                                    String killerName,
                                    String killerSource,
                                    EntityDamageEvent.DamageCause cause,
                                    boolean isEpic) {
        DeathContext context = contextFactory.createForTest(victim, killerName, killerSource, cause);
        String rawMessage = messageSelector.selectRawMessage(context, isEpic);
        return formatMessageWithRawText(context, rawMessage);
    }

    public String formatPreviewMessage(Player player, String rawMessage) {
        DeathContext context = contextFactory.createDefaultForEditor(player);
        return formatMessageWithRawText(context, rawMessage);
    }

    public String formatVisualMessage(String rawMessage, DeathContext context) {
        if (rawMessage == null || rawMessage.isEmpty()) {
            return "";
        }

        String withPlaceholders = placeholderRegistry.replacePlaceholders(rawMessage, context);
        String withPapi = papiExpander.expandPlaceholders(context.getVictim(), withPlaceholders);

        return ColorFormatter.formatColors(withPapi,
                plugin.getConfigManager().isLegacyColorSupport());
    }

    private String formatMessage(DeathContext context) {
        String rawMessage = messageSelector.selectRawMessage(context);
        return formatMessageWithRawText(context, rawMessage);
    }

    private String formatMessageWithRawText(DeathContext context, String rawMessage) {
        if (rawMessage == null || rawMessage.isEmpty()) {
            rawMessage = "&c{victim} has died.";
        }

        String withPlaceholders = placeholderRegistry.replacePlaceholders(rawMessage, context);
        String withPapi = papiExpander.expandPlaceholders(context.getVictim(), withPlaceholders);

        return ColorFormatter.formatColors(withPapi,
                plugin.getConfigManager().isLegacyColorSupport());
    }

    public PlaceholderRegistry getPlaceholderRegistry() {
        return placeholderRegistry;
    }

    public DeathContextFactory getContextFactory() {
        return contextFactory;
    }

    public MessageSelector getMessageSelector() {
        return messageSelector;
    }
}