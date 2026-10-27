package com.sicklysurgeon.customdeathmessages.util.formatter;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

public class KillerInfo {

    private final String displayName;
    private final String username;
    private final String sourceName;
    private final Entity responsibleEntity;
    private final Entity directSourceEntity;

    public KillerInfo(String displayName,
                      String username,
                      String sourceName,
                      Entity responsibleEntity,
                      Entity directSourceEntity) {
        this.displayName = displayName;
        this.username = username;
        this.sourceName = sourceName;
        this.responsibleEntity = responsibleEntity;
        this.directSourceEntity = directSourceEntity;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getUsername() {
        return username;
    }

    public String getSourceName() {
        return sourceName;
    }

    public Entity getResponsibleEntity() {
        return responsibleEntity;
    }

    public Entity getDirectSourceEntity() {
        return directSourceEntity;
    }

    public static KillerInfo unknown() {
        return new KillerInfo("Unknown", "Unknown", "Unknown", null, null);
    }

    public static KillerInfo fromPlayer(Player player, String displayName) {
        return new KillerInfo(
                displayName,
                player.getName(),
                "Player",
                player,
                player
        );
    }

    public static KillerInfo fromTest(String displayName, String username, String sourceName) {
        return new KillerInfo(displayName, username, sourceName, null, null);
    }
}