package com.sicklysurgeon.customdeathmessages.util.context;

import com.sicklysurgeon.customdeathmessages.CustomDeathMessages;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Collection;
import java.util.Locale;
import java.util.stream.Collectors;

@SuppressWarnings("deprecation")
public class ContextEnricher {

    private final CustomDeathMessages plugin;

    private String cachedTimeFormat;
    private DateTimeFormatter cachedTimeFormatter;

    public ContextEnricher(CustomDeathMessages plugin) {
        this.plugin = plugin;
    }

    public double calculateKillerDistance(DeathContext context) {
        if (context == null || context.getVictim() == null) {
            return 0.0;
        }

        Entity responsible = context.getResponsibleEntity();
        if (responsible != null && responsible.isValid()
                && sameWorld(context.getVictim(), responsible)) {
            return context.getVictim().getLocation().distance(responsible.getLocation());
        }

        Player killer = context.getVictim().getKiller();
        if (killer != null && sameWorld(context.getVictim(), killer)) {
            return context.getVictim().getLocation().distance(killer.getLocation());
        }

        return 0.0;
    }

    private boolean sameWorld(Player victim, Entity other) {
        return victim.getWorld().equals(other.getWorld());
    }

    public String getDeathBiome(DeathContext context) {
        Location location = context.getVictim().getLocation();
        String biomeKey = location.getBlock().getBiome().getKey().getKey();

        return Arrays.stream(biomeKey.split("_"))
                .map(this::capitalizeFirst)
                .collect(Collectors.joining(" "));
    }

    public String getVictimWeapon(DeathContext context) {
        ItemStack item = context.getVictim().getInventory().getItemInMainHand();

        if (item == null || item.getType().isAir()) {
            return "Empty";
        }

        if (item.hasItemMeta()) {
            org.bukkit.inventory.meta.ItemMeta meta = item.getItemMeta();
            if (meta != null && meta.hasDisplayName()) {
                return meta.getDisplayName();
            }
        }

        String materialName = item.getType().name().toLowerCase(Locale.ROOT).replace("_", " ");
        return capitalizeWords(materialName);
    }

    public String getVictimEffects(DeathContext context) {
        Collection<PotionEffect> effects = context.getVictim().getActivePotionEffects();

        if (effects.isEmpty()) {
            return "None";
        }

        return effects.stream()
                .map(effect -> {
                    String effectName = effect.getType().getKey().getKey()
                            .toLowerCase(Locale.ROOT)
                            .replace("_", " ");
                    return capitalizeWords(effectName);
                })
                .collect(Collectors.joining(", "));
    }

    public String getFormattedTime(DeathContext context) {
        String timeFormat = plugin.getConfigManager().getTimeFormat();

        if (timeFormat == null || timeFormat.isEmpty()) {
            timeFormat = "HH:mm:ss";
        }

        if (cachedTimeFormatter == null || !timeFormat.equals(cachedTimeFormat)) {
            try {
                cachedTimeFormatter = DateTimeFormatter.ofPattern(timeFormat);
            } catch (Exception e) {
                plugin.getLogger().warning("Invalid time-format '" + timeFormat + "'. Using HH:mm:ss.");
                cachedTimeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss");
            }

            cachedTimeFormat = timeFormat;
        }

        return LocalTime.now().format(cachedTimeFormatter);
    }

    public String getFormattedCoordinates(DeathContext context) {
        Location loc = context.getVictim().getLocation();

        return String.format("X: %d, Y: %d, Z: %d",
                loc.getBlockX(),
                loc.getBlockY(),
                loc.getBlockZ());
    }

    public String capitalizeFirst(String word) {
        if (word == null || word.isEmpty()) {
            return word == null ? "" : word;
        }

        return Character.toUpperCase(word.charAt(0))
                + word.substring(1).toLowerCase(Locale.ROOT);
    }

    public String capitalizeWords(String input) {
        if (input == null || input.isEmpty()) {
            return input == null ? "" : input;
        }

        return Arrays.stream(input.split(" "))
                .map(this::capitalizeFirst)
                .collect(Collectors.joining(" "));
    }
}