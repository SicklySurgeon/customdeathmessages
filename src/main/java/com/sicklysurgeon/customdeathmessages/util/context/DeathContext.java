package com.sicklysurgeon.customdeathmessages.util.context;

import com.sicklysurgeon.customdeathmessages.CustomDeathMessages;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent;

public class DeathContext {

    private final Player victim;
    private final String killerName;
    private final String killerUsername;
    private final String killerSource;
    private final Entity responsibleEntity;
    private final Entity directSourceEntity;
    private final EntityDamageEvent.DamageCause cause;
    private final CustomDeathMessages plugin;

    // Backward-compatible constructor
    public DeathContext(Player victim,
                        String killerName,
                        String killerSource,
                        EntityDamageEvent.DamageCause cause,
                        CustomDeathMessages plugin) {
        this(victim, killerName, killerName, killerSource, null, null, cause, plugin);
    }

    public DeathContext(Player victim,
                        String killerName,
                        String killerUsername,
                        String killerSource,
                        Entity responsibleEntity,
                        Entity directSourceEntity,
                        EntityDamageEvent.DamageCause cause,
                        CustomDeathMessages plugin) {
        this.victim = victim;
        this.killerName = killerName;
        this.killerUsername = killerUsername;
        this.killerSource = killerSource;
        this.responsibleEntity = responsibleEntity;
        this.directSourceEntity = directSourceEntity;
        this.cause = cause;
        this.plugin = plugin;
    }

    public Player getVictim() {
        return victim;
    }

    public String getKillerName() {
        return killerName;
    }

    public String getKillerUsername() {
        return killerUsername;
    }

    public String getKillerSource() {
        return killerSource;
    }

    public Entity getResponsibleEntity() {
        return responsibleEntity;
    }

    public Entity getDirectSourceEntity() {
        return directSourceEntity;
    }

    public EntityDamageEvent.DamageCause getCause() {
        return cause;
    }

    public CustomDeathMessages getPlugin() {
        return plugin;
    }

    public DeathContext withVictim(Player victim) {
        return new DeathContext(
                victim,
                killerName,
                killerUsername,
                killerSource,
                responsibleEntity,
                directSourceEntity,
                cause,
                plugin
        );
    }

    public DeathContext withKiller(String killerName,
                                   String killerUsername,
                                   String killerSource,
                                   Entity responsibleEntity,
                                   Entity directSourceEntity) {
        return new DeathContext(
                victim,
                killerName,
                killerUsername,
                killerSource,
                responsibleEntity,
                directSourceEntity,
                cause,
                plugin
        );
    }

    public DeathContext withCause(EntityDamageEvent.DamageCause cause) {
        return new DeathContext(
                victim,
                killerName,
                killerUsername,
                killerSource,
                responsibleEntity,
                directSourceEntity,
                cause,
                plugin
        );
    }
}