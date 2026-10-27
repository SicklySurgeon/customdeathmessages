package com.sicklysurgeon.customdeathmessages.util.context;

import com.sicklysurgeon.customdeathmessages.CustomDeathMessages;
import com.sicklysurgeon.customdeathmessages.util.formatter.KillerInfo;
import com.sicklysurgeon.customdeathmessages.util.formatter.KillerInfoResolver;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;

public class DeathContextFactory {

    private final CustomDeathMessages plugin;
    private final KillerInfoResolver killerInfoResolver;

    public DeathContextFactory(CustomDeathMessages plugin) {
        this.plugin = plugin;
        this.killerInfoResolver = new KillerInfoResolver(plugin);
    }

    public DeathContext createFromDeathEvent(PlayerDeathEvent event) {
        Player victim = event.getEntity();
        EntityDamageEvent.DamageCause cause = determineDamageCause(event);
        KillerInfo killerInfo = killerInfoResolver.resolveKillerInfo(event);

        return new DeathContext(
                victim,
                killerInfo.getDisplayName(),
                killerInfo.getUsername(),
                killerInfo.getSourceName(),
                killerInfo.getResponsibleEntity(),
                killerInfo.getDirectSourceEntity(),
                cause,
                plugin
        );
    }

    public DeathContext createForTest(Player victim,
                                      String killerName,
                                      String killerSource,
                                      EntityDamageEvent.DamageCause cause) {
        KillerInfo killerInfo = killerInfoResolver.resolveForTest(killerName, killerSource);

        return new DeathContext(
                victim,
                killerInfo.getDisplayName(),
                killerInfo.getUsername(),
                killerInfo.getSourceName(),
                killerInfo.getResponsibleEntity(),
                killerInfo.getDirectSourceEntity(),
                cause,
                plugin
        );
    }

    public DeathContext createDefaultForEditor(Player player) {
        return new DeathContext(
                player,
                "Zombie",
                "zombie",
                "Zombie",
                null,
                null,
                EntityDamageEvent.DamageCause.FALL,
                plugin
        );
    }

    private EntityDamageEvent.DamageCause determineDamageCause(PlayerDeathEvent event) {
        if (event.getEntity().getLastDamageCause() != null) {
            return event.getEntity().getLastDamageCause().getCause();
        }

        return EntityDamageEvent.DamageCause.CUSTOM;
    }
}