package com.sicklysurgeon.customdeathmessages.command.test;

import com.sicklysurgeon.customdeathmessages.CustomDeathMessages;
import com.sicklysurgeon.customdeathmessages.util.ParticleUtil;
import com.sicklysurgeon.customdeathmessages.util.SoundUtil;
import com.sicklysurgeon.customdeathmessages.util.config.ConfigManager;
import com.sicklysurgeon.customdeathmessages.util.context.DeathContext;
import com.sicklysurgeon.customdeathmessages.util.formatter.ColorFormatter;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

public class TestSummaryRenderer {

    private final CustomDeathMessages plugin;

    public TestSummaryRenderer(CustomDeathMessages plugin) {
        this.plugin = plugin;
    }

    private String getMsg(String key, String defaultMsg) {
        FileConfiguration msgs = plugin.getMessagesManager().getMessages();
        String raw = msgs.getString("system-messages." + key, defaultMsg);
        return raw == null ? defaultMsg : raw;
    }

    private String colorize(String msg) {
        return ColorFormatter.formatColors(msg,
                plugin.getConfigManager().isLegacyColorSupport());
    }

    public void showTestSummary(Player executor,
                                Player victim,
                                String formattedMessage,
                                DeathContext context,
                                boolean isBroadcast,
                                boolean isEpic) {
        executor.sendMessage(colorize(getMsg("test-summary-title",
                "&6=== CustomDeathMessages Test Summary ===")));

        executor.sendMessage(colorize(getMsg("test-summary-victim", "&bVictim: &f{victim}")
                .replace("{victim}", victim.getName())));

        executor.sendMessage(colorize(getMsg("test-summary-killer", "&bKiller: &f{killer}")
                .replace("{killer}", context.getKillerName())));

        executor.sendMessage(colorize(getMsg("test-summary-source", "&bSource: &f{source}")
                .replace("{source}", context.getKillerSource())));

        executor.sendMessage(colorize(getMsg("test-summary-cause", "&bCause: &f{cause}")
                .replace("{cause}", context.getCause().name())));

        executor.sendMessage(colorize(getMsg("test-summary-broadcast", "&bBroadcast: &f{broadcast}")
                .replace("{broadcast}", isBroadcast ? "Yes" : "No")));

        executor.sendMessage(colorize(getMsg("test-summary-epic", "&bEpic: &f{epic}")
                .replace("{epic}", isEpic ? "Yes" : "No")));

        executor.sendMessage(colorize(getMsg("test-summary-message", "&bFinal Message:")));
        executor.sendMessage(formattedMessage);

        renderSoundInfo(executor, victim, isEpic);
        renderParticleInfo(executor, isEpic);
        renderCostInfo(executor);

        executor.sendMessage(colorize(getMsg("test-summary-separator",
                "&6=======================================")));
    }

    private void renderSoundInfo(Player executor, Player victim, boolean isEpic) {
        String soundName;

        if (isEpic) {
            soundName = plugin.getMessagesManager().getEpicSoundName();
        } else {
            String permissionGroup = plugin.getConfigManager().resolveMessageGroup(victim);
            ConfigManager.SoundConfig groupSound =
                    plugin.getConfigManager().getGroupSoundConfig(permissionGroup);

            if (groupSound != null) {
                soundName = SoundUtil.keyName(groupSound.sound);
            } else {
                soundName = SoundUtil.keyName(plugin.getConfigManager().getCachedSound());
            }
        }

        executor.sendMessage(colorize(getMsg("test-summary-sound", "&bSound: &f{sound}")
                .replace("{sound}", soundName)));
    }

    private void renderParticleInfo(Player executor, boolean isEpic) {
        String particleName;

        if (isEpic) {
            particleName = plugin.getMessagesManager().getEpicParticleName();
        } else {
            particleName = ParticleUtil.name(plugin.getConfigManager().getCachedParticle());
        }

        executor.sendMessage(colorize(getMsg("test-summary-particle", "&bParticle: &f{particle}")
                .replace("{particle}", particleName)));
    }

    private void renderCostInfo(Player executor) {
        double cost = plugin.getConfigManager().getDeathMessageCost();
        String costStr = cost > 0 ? "$" + cost : "Free";

        executor.sendMessage(colorize(getMsg("test-summary-cost", "&bCost: &f{cost}")
                .replace("{cost}", costStr)));
    }

    public void renderErrorMessage(Player executor, String error) {
        executor.sendMessage(colorize(getMsg("test-error", "&cError: {error}")
                .replace("{error}", error)));
    }

    public void renderSuccessMessage(Player executor, String message) {
        executor.sendMessage(org.bukkit.ChatColor.GREEN + "✓ " + message);
    }
}