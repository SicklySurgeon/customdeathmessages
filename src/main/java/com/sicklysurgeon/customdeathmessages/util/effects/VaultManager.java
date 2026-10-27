package com.sicklysurgeon.customdeathmessages.util.effects;

import com.sicklysurgeon.customdeathmessages.CustomDeathMessages;
import com.sicklysurgeon.customdeathmessages.util.formatter.ColorFormatter;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

public class VaultManager {

    private final CustomDeathMessages plugin;
    private Economy economy;
    private boolean warnedMissingEconomy = false;

    public VaultManager(CustomDeathMessages plugin) {
        this.plugin = plugin;
        setupEconomy();
    }

    private void setupEconomy() {
        if (plugin.getServer().getPluginManager().getPlugin("Vault") == null) {
            return;
        }

        org.bukkit.plugin.RegisteredServiceProvider<Economy> rsp =
                plugin.getServer().getServicesManager().getRegistration(Economy.class);

        if (rsp != null) {
            this.economy = rsp.getProvider();
        }
    }

    public boolean hasEconomy() {
        return economy != null;
    }

    public boolean chargePlayer(Player player, double cost, String group) {
        if (cost <= 0.0) {
            return true;
        }

        if (economy == null) {
            setupEconomy();
        }

        if (!hasEconomy()) {
            if (!warnedMissingEconomy) {
                warnedMissingEconomy = true;
                plugin.getLogger().warning(
                        "Economy cost is enabled but Vault economy provider is not available. Allowing death message without charging.");
            }
            return true;
        }

        if (plugin.getConfigManager().isExemptFromCost(group)) {
            sendSystemMessage(player, "economy-exempt-message", "&7(Exempt from cost)", cost);
            return true;
        }

        if (!economy.has(player, cost)) {
            sendSystemMessage(player, "economy-insufficient-funds",
                    "&cYou cannot afford the death message cost (${cost}).", cost);
            return false;
        }

        EconomyResponse response = economy.withdrawPlayer(player, cost);

        if (!response.transactionSuccess()) {
            sendSystemMessage(player, "economy-insufficient-funds",
                    "&cYou cannot afford the death message cost (${cost}).", cost);
            return false;
        }

        sendSystemMessage(player, "economy-cost-message", "&c${cost} deducted.", cost);
        return true;
    }

    private void sendSystemMessage(Player player, String key, String defaultMsg, double cost) {
        if (player == null) {
            return;
        }

        FileConfiguration msgs = plugin.getMessagesManager().getMessages();
        String raw = msgs.getString("system-messages." + key, defaultMsg);

        if (raw == null || raw.isEmpty()) {
            return;
        }

        raw = raw.replace("{cost}", String.valueOf(cost));
        player.sendMessage(ColorFormatter.formatColors(raw,
                plugin.getConfigManager().isLegacyColorSupport()));
    }
}