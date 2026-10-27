package com.sicklysurgeon.customdeathmessages.util.formatter;

import com.sicklysurgeon.customdeathmessages.CustomDeathMessages;
import com.sicklysurgeon.customdeathmessages.util.hooks.EssentialsHook;
import com.sicklysurgeon.customdeathmessages.util.tracking.DamageTracker;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;

import java.util.Locale;

public class KillerInfoResolver {

    private final CustomDeathMessages plugin;
    private final EntityNameFormatter entityNameFormatter;

    public KillerInfoResolver(CustomDeathMessages plugin) {
        this.plugin = plugin;
        this.entityNameFormatter = new EntityNameFormatter();
    }

    public KillerInfo resolveKillerInfo(PlayerDeathEvent event) {
        Player victim = event.getEntity();

        DamageTracker.DamageSourceInfo info = plugin.getDamageTracker().getLastDamageInfo(victim);
        if (info != null) {
            KillerInfo resolved = fromDamageInfo(info);
            if (resolved != null) {
                return resolved;
            }
        }

        EntityDamageEvent lastCause = victim.getLastDamageCause();
        if (lastCause instanceof EntityDamageByEntityEvent) {
            Entity damager = ((EntityDamageByEntityEvent) lastCause).getDamager();
            Entity responsible = damager;

            if (damager instanceof Projectile) {
                Object shooter = ((Projectile) damager).getShooter();

                if (shooter instanceof Entity) {
                    responsible = (Entity) shooter;
                }
            }

            KillerInfo resolved = fromEntities(responsible, damager);
            if (resolved != null) {
                return resolved;
            }
        }

        Player killerPlayer = victim.getKiller();
        if (killerPlayer != null) {
            return KillerInfo.fromPlayer(killerPlayer, EssentialsHook.getNickname(killerPlayer));
        }

        return KillerInfo.unknown();
    }

    private KillerInfo fromDamageInfo(DamageTracker.DamageSourceInfo info) {
        if (info.getBlockSourceName() != null
                && !info.getBlockSourceName().isEmpty()
                && (info.getResponsible() == null || !info.getResponsible().isValid())) {

            String directName = info.getDirectSource() != null && info.getDirectSource().isValid()
                    ? entityNameFormatter.formatEntityName(info.getDirectSource())
                    : info.getBlockSourceName();

            return new KillerInfo(
                    info.getBlockSourceName(),
                    info.getBlockSourceName().toLowerCase(Locale.ROOT),
                    directName,
                    null,
                    info.getDirectSource()
            );
        }

        return fromEntities(info.getResponsible(), info.getDirectSource());
    }

    private KillerInfo fromEntities(Entity responsible, Entity directSource) {
        Entity realResponsible = responsible;
        Entity realDirect = directSource;

        if ((realResponsible == null || !realResponsible.isValid())
                && realDirect != null && realDirect.isValid()) {
            realResponsible = realDirect;
        }

        if (realResponsible == null || !realResponsible.isValid()) {
            return null;
        }

        String sourceName;
        if (realDirect != null && realDirect.isValid()) {
            sourceName = entityNameFormatter.formatEntityName(realDirect);
        } else {
            sourceName = entityNameFormatter.formatEntityName(realResponsible);
        }

        if (realResponsible instanceof Player) {
            Player player = (Player) realResponsible;

            return new KillerInfo(
                    EssentialsHook.getNickname(player),
                    player.getName(),
                    sourceName,
                    realResponsible,
                    realDirect
            );
        }

        String displayName = entityNameFormatter.formatEntityName(realResponsible);
        String username = realResponsible.getType().name().toLowerCase(Locale.ROOT).replace('_', ' ');

        return new KillerInfo(
                displayName,
                username,
                sourceName,
                realResponsible,
                realDirect
        );
    }

    public KillerInfo resolveForTest(String killerName, String killerSource) {
        if (killerName == null || killerName.equalsIgnoreCase("unknown")) {
            return KillerInfo.unknown();
        }

        Player online = plugin.getServer().getPlayer(killerName);
        boolean sourceIsUnknown = killerSource == null || killerSource.equalsIgnoreCase("unknown");

        if (online != null && sourceIsUnknown) {
            return KillerInfo.fromPlayer(online, EssentialsHook.getNickname(online));
        }

        String displayName = EssentialsHook.getNickname(killerName);
        String username = killerName;
        String sourceName = sourceIsUnknown ? displayName : killerSource;

        return KillerInfo.fromTest(displayName, username, sourceName);
    }
}