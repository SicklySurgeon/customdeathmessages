package com.sicklysurgeon.customdeathmessages.util.placeholder;

import com.sicklysurgeon.customdeathmessages.CustomDeathMessages;
import com.sicklysurgeon.customdeathmessages.util.context.ContextEnricher;
import com.sicklysurgeon.customdeathmessages.util.context.DeathContext;
import com.sicklysurgeon.customdeathmessages.util.hooks.EssentialsHook;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public class PlaceholderRegistry {

    private final Map<String, Function<DeathContext, String>> placeholders;
    private CustomDeathMessages plugin;
    private ContextEnricher contextEnricher;

    public PlaceholderRegistry() {
        this.placeholders = new HashMap<>();
        registerDefaultPlaceholders();
    }

    public void setPlugin(CustomDeathMessages plugin) {
        this.plugin = plugin;
        this.contextEnricher = new ContextEnricher(plugin);
        EssentialsHook.checkEssentials();

        if (EssentialsHook.isEnabled()) {
            plugin.getLogger().info("Essentials detected! Nicknames will be used in death messages.");
        }
    }

    private void registerDefaultPlaceholders() {
        placeholders.put("victim", context -> EssentialsHook.getNickname(context.getVictim()));

        placeholders.put("victim_username", context -> context.getVictim().getName());

        placeholders.put("killer_name", context -> {
            String value = context.getKillerName();
            return value == null || value.isEmpty() ? "Unknown" : value;
        });

        placeholders.put("killer_username", context -> {
            String value = context.getKillerUsername();
            if (value == null || value.isEmpty()) {
                value = context.getKillerName();
            }
            return value == null || value.isEmpty() ? "Unknown" : value;
        });

        placeholders.put("killer_displayname", context -> {
            String value = context.getKillerName();
            return value == null || value.isEmpty() ? "Unknown" : value;
        });

        placeholders.put("killer_source", context -> {
            String value = context.getKillerSource();
            return value == null || value.isEmpty() ? "Unknown" : value;
        });

        placeholders.put("cause", context -> context.getCause().name());

        placeholders.put("world", context -> context.getVictim().getWorld().getName());

        placeholders.put("distance", context -> {
            double distance = contextEnricher.calculateKillerDistance(context);
            return String.format("%.1f", distance);
        });

        placeholders.put("biome", context -> contextEnricher.getDeathBiome(context));
        placeholders.put("victim_weapon", context -> contextEnricher.getVictimWeapon(context));
        placeholders.put("victim_effects", context -> contextEnricher.getVictimEffects(context));
        placeholders.put("time", context -> contextEnricher.getFormattedTime(context));

        placeholders.put("x", context -> String.valueOf(context.getVictim().getLocation().getBlockX()));
        placeholders.put("y", context -> String.valueOf(context.getVictim().getLocation().getBlockY()));
        placeholders.put("z", context -> String.valueOf(context.getVictim().getLocation().getBlockZ()));

        placeholders.put("cost", context -> {
            double cost = plugin.getConfigManager().getDeathMessageCost();
            return cost > 0 ? String.valueOf(cost) : "0";
        });
    }

    public String replacePlaceholders(String message, DeathContext context) {
        if (message == null) {
            return "";
        }

        String result = message;

        for (Map.Entry<String, Function<DeathContext, String>> entry : placeholders.entrySet()) {
            String placeholder = "{" + entry.getKey() + "}";

            // Only resolve placeholders that are actually present. This avoids
            // expensive lookups (Essentials, distance, potion effects...) for unused ones.
            if (!result.contains(placeholder)) {
                continue;
            }

            String replacement = entry.getValue().apply(context);
            result = result.replace(placeholder, replacement == null ? "" : replacement);
        }

        return result;
    }

    public void registerPlaceholder(String key, Function<DeathContext, String> resolver) {
        placeholders.put(key, resolver);
    }

    public List<String> getAllPlaceholderKeys() {
        return new ArrayList<>(placeholders.keySet());
    }

    public boolean containsPlaceholder(String key) {
        return placeholders.containsKey(key);
    }
}