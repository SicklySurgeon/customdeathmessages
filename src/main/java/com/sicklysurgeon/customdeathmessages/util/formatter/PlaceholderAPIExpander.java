package com.sicklysurgeon.customdeathmessages.util.formatter;

import com.sicklysurgeon.customdeathmessages.CustomDeathMessages;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class PlaceholderAPIExpander {

    private final CustomDeathMessages plugin;

    private Boolean papiAvailable;
    private boolean papiBroken = false;

    private boolean warned = false;

    public PlaceholderAPIExpander(CustomDeathMessages plugin) {
        this.plugin = plugin;
    }

    public String expandPlaceholders(Player player, String input) {
        if (input == null || input.isEmpty()) {
            return input;
        }

        if (!isPapiAvailable()) {
            return input;
        }

        try {
            return me.clip.placeholderapi.PlaceholderAPI.setPlaceholders(player, input);
        } catch (NoClassDefFoundError e) {
            papiBroken = true;
            warnOnce("PlaceholderAPI detected but class not found. Is it properly installed?");
            return input;
        } catch (Exception e) {
            warnOnce("PlaceholderAPI expansion failed: " + e.getMessage());
            return input;
        }
    }

    private boolean isPapiAvailable() {
        if (papiBroken) {
            return false;
        }

        if (Boolean.TRUE.equals(papiAvailable)) {
            return true;
        }

        // Re-check while unavailable so the plugin can pick PlaceholderAPI up
        // if it is enabled later without a server restart.
        papiAvailable = Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI");
        return papiAvailable;
    }

    private void warnOnce(String message) {
        if (!warned) {
            warned = true;
            plugin.getLogger().warning(message);
        }
    }
}