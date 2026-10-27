package com.sicklysurgeon.customdeathmessages.command.test;

import com.sicklysurgeon.customdeathmessages.CustomDeathMessages;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent;

import java.util.Locale;

public class TestContextBuilder {

    public TestContextBuilder(CustomDeathMessages plugin) {
    }

    public static class TestParams {
        public boolean isBroadcast = false;
        public String victimName;
        public String causeArg;
        public String killerName = "Unknown";
        public String killerSource = "Unknown";
    }

    public TestParams parseArguments(String[] args) throws TestException {
        TestParams params = new TestParams();

        int argOffset = 0;

        if (args.length >= 1 && args[0].equalsIgnoreCase("broadcast")) {
            params.isBroadcast = true;
            argOffset = 1;
        }

        if (args.length <= argOffset) {
            params.causeArg = "FALL";
            return params;
        }

        String first = args[argOffset];
        Player potentialVictim = Bukkit.getPlayer(first);

        if (potentialVictim != null) {
            params.victimName = first;

            if (args.length < argOffset + 2) {
                throw new TestException("Specify a cause: FALL, FIRE, ENTITY_ATTACK, etc.");
            }

            params.causeArg = args[argOffset + 1];

            if (args.length > argOffset + 2) {
                params.killerName = args[argOffset + 2];
            }

            if (args.length > argOffset + 3) {
                params.killerSource = args[argOffset + 3];
            }

            return params;
        }

        if (isValidCause(first)) {
            params.causeArg = first;

            if (args.length > argOffset + 1) {
                params.killerName = args[argOffset + 1];
            }

            if (args.length > argOffset + 2) {
                params.killerSource = args[argOffset + 2];
            }

            return params;
        }

        if (args.length > argOffset + 1) {
            params.victimName = first;
            params.causeArg = args[argOffset + 1];

            if (args.length > argOffset + 2) {
                params.killerName = args[argOffset + 2];
            }

            if (args.length > argOffset + 3) {
                params.killerSource = args[argOffset + 3];
            }

            return params;
        }

        params.causeArg = first;
        return params;
    }

    public Player resolveVictim(Player executor, TestParams params) throws TestException {
        if (params.victimName != null) {
            Player victim = Bukkit.getPlayer(params.victimName);

            if (victim == null) {
                throw new TestException("Player '" + params.victimName + "' is not online.");
            }

            return victim;
        }

        return executor;
    }

    public EntityDamageEvent.DamageCause validateCause(String causeName) throws IllegalArgumentException {
        return EntityDamageEvent.DamageCause.valueOf(causeName.toUpperCase(Locale.ROOT));
    }

    public void validateVictimPermission(Player executor, Player victim) throws TestException {
        if (!victim.equals(executor) && !executor.hasPermission("cdm.test.others")) {
            throw new TestException("You don't have permission to test on other players.");
        }
    }

    private boolean isValidCause(String name) {
        try {
            EntityDamageEvent.DamageCause.valueOf(name.toUpperCase(Locale.ROOT));
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
}