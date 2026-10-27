package com.sicklysurgeon.customdeathmessages.util.hooks;

import com.earth2me.essentials.Essentials;
import com.earth2me.essentials.User;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

public class EssentialsHook {

    private static Essentials essentials = null;
    private static boolean enabled = false;

    public static void checkEssentials() {
        Plugin plugin = Bukkit.getPluginManager().getPlugin("Essentials");

        if (plugin != null && plugin.isEnabled() && plugin instanceof Essentials) {
            essentials = (Essentials) plugin;
            enabled = true;
        } else {
            essentials = null;
            enabled = false;
        }
    }

    private static boolean ensureReady() {
        if (!enabled || essentials == null || !essentials.isEnabled()) {
            checkEssentials();
        }

        return enabled && essentials != null;
    }

    public static String getNickname(Player player) {
        if (player == null) {
            return "Unknown";
        }

        if (ensureReady()) {
            try {
                User user = essentials.getUser(player);

                if (user != null) {
                    String nickname = user.getNickname();

                    if (nickname != null && !nickname.isEmpty()) {
                        return nickname;
                    }
                }
            } catch (Exception ignored) {
            }
        }

        return player.getName();
    }

    public static String getNickname(String playerName) {
        if (playerName == null || playerName.isEmpty()) {
            return "Unknown";
        }

        if (ensureReady()) {
            try {
                User user = essentials.getOfflineUser(playerName);

                if (user != null) {
                    String nickname = user.getNickname();

                    if (nickname != null && !nickname.isEmpty()) {
                        return nickname;
                    }
                }
            } catch (Exception ignored) {
            }
        }

        return playerName;
    }

    public static boolean isEnabled() {
        return ensureReady();
    }
}