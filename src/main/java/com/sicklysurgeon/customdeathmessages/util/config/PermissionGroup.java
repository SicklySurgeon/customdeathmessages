package com.sicklysurgeon.customdeathmessages.util.config;

import org.bukkit.entity.Player;

public enum PermissionGroup {
    DEFAULT("default"),
    VIP("vip"),
    ADMIN("admin");

    private final String configKey;

    PermissionGroup(String configKey) {
        this.configKey = configKey;
    }

    public String getConfigKey() {
        return configKey;
    }

    public static PermissionGroup fromPlayer(Player player) {
        if (player.hasPermission("cdm.message.admin")) {
            return ADMIN;
        }

        if (player.hasPermission("cdm.message.vip")) {
            return VIP;
        }

        return DEFAULT;
    }
}