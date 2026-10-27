package com.sicklysurgeon.customdeathmessages.command.test;

import com.sicklysurgeon.customdeathmessages.CustomDeathMessages;
import com.sicklysurgeon.customdeathmessages.util.config.ConfigManager;
import com.sicklysurgeon.customdeathmessages.util.context.DeathContext;
import com.sicklysurgeon.customdeathmessages.util.effects.ParticleManager;
import com.sicklysurgeon.customdeathmessages.util.effects.SoundManager;
import com.sicklysurgeon.customdeathmessages.util.formatter.ColorFormatter;

import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class TestCommand implements CommandExecutor {

    private final CustomDeathMessages plugin;
    private final TestContextBuilder contextBuilder;
    private final TestSummaryRenderer summaryRenderer;
    private final SoundManager soundManager;
    private final ParticleManager particleManager;

    public TestCommand(CustomDeathMessages plugin) {
        this.plugin = plugin;
        this.contextBuilder = new TestContextBuilder(plugin);
        this.summaryRenderer = new TestSummaryRenderer(plugin);
        this.soundManager = new SoundManager(plugin);
        this.particleManager = new ParticleManager(plugin);
    }

    private String getMsg(String key, String defaultMsg) {
        FileConfiguration msgs = plugin.getMessagesManager().getMessages();
        String raw = msgs.getString("system-messages." + key, defaultMsg);

        return ColorFormatter.formatColors(raw == null ? defaultMsg : raw,
                plugin.getConfigManager().isLegacyColorSupport());
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("cdm.test")) {
            sender.sendMessage(getMsg("no-permission-test",
                    "&cYou do not have permission to use this command."));
            return true;
        }

        if (!(sender instanceof Player)) {
            sender.sendMessage(getMsg("players-only",
                    "&cOnly players can use this command."));
            return true;
        }

        Player executor = (Player) sender;

        int argStart = 0;
        if (args.length > 0 && args[0].equalsIgnoreCase("test")) {
            argStart = 1;
        }

        String[] effectiveArgs = Arrays.copyOfRange(args, argStart, args.length);

        boolean globalSound = false;
        boolean globalParticles = false;
        List<String> cleanArgs = new ArrayList<>();

        for (String arg : effectiveArgs) {
            if (arg.equalsIgnoreCase("global")) {
                globalSound = true;
                globalParticles = true;
            } else if (arg.equalsIgnoreCase("global-sound")) {
                globalSound = true;
            } else if (arg.equalsIgnoreCase("global-particles")) {
                globalParticles = true;
            } else {
                cleanArgs.add(arg);
            }
        }

        String[] filteredArgs = cleanArgs.toArray(new String[0]);

        if (filteredArgs.length < 1) {
            sender.sendMessage(getMsg("usage-test",
                    "&cUsage: /cdm test <cause> [killer] [source]"));
            return true;
        }

        try {
            TestContextBuilder.TestParams params = contextBuilder.parseArguments(filteredArgs);
            Player victim = contextBuilder.resolveVictim(executor, params);
            contextBuilder.validateVictimPermission(executor, victim);

            EntityDamageEvent.DamageCause cause = contextBuilder.validateCause(params.causeArg);

            boolean isEpic = Math.random() < plugin.getConfigManager().getEpicDeathChance();

            DeathContext deathContext = plugin.getMessageFormatter().getContextFactory()
                    .createForTest(victim, params.killerName, params.killerSource, cause);

            String formattedMessage = plugin.getMessageFormatter()
                    .formatDeathMessage(deathContext, isEpic);

            summaryRenderer.showTestSummary(executor, victim, formattedMessage, deathContext,
                    params.isBroadcast, isEpic);

            String permissionGroup = plugin.getConfigManager().resolveMessageGroup(victim);

            if (plugin.getConfigManager().isPlaySoundOnDeath()) {
                if (globalSound) {
                    Sound sound;
                    float volume;
                    float pitch;

                    if (isEpic) {
                        sound = plugin.getMessagesManager().getCachedEpicSound();
                        volume = 2.0f;
                        pitch = 0.8f;
                    } else {
                        sound = plugin.getConfigManager().getCachedSound();
                        volume = plugin.getConfigManager().getSoundVolume();
                        pitch = plugin.getConfigManager().getSoundPitch();
                    }

                    soundManager.playSoundGlobal(victim, sound, volume, pitch);
                } else {
                    soundManager.playDeathSound(victim, plugin.getConfigManager(), plugin,
                            permissionGroup, isEpic);
                }
            }

            if (plugin.getConfigManager().isPlayParticlesOnDeath()) {
                if (globalParticles) {
                    particleManager.spawnDeathParticlesWithMode(
                            victim,
                            plugin.getConfigManager(),
                            isEpic,
                            ConfigManager.BroadcastMode.GLOBAL,
                            0
                    );
                } else {
                    particleManager.spawnDeathParticles(victim, plugin.getConfigManager(), isEpic);
                }
            }

            if (params.isBroadcast) {
                broadcastTestMessage(victim, formattedMessage);
            }

        } catch (IllegalArgumentException e) {
            handleInvalidCause(sender);
        } catch (TestException e) {
            String errorMsg = getMsg("test-error", "&cError: {error}");
            errorMsg = errorMsg.replace("{error}", e.getMessage());
            sender.sendMessage(errorMsg);
        }

        return true;
    }

    private void broadcastTestMessage(Player victim, String message) {
        List<Player> recipients;

        if (plugin.getConfigManager().getMessageToRadius() > 0) {
            double radius = plugin.getConfigManager().getMessageToRadius();

            recipients = victim.getWorld().getNearbyEntities(
                            victim.getLocation(),
                            radius,
                            radius,
                            radius
                    ).stream()
                    .filter(e -> e instanceof Player)
                    .map(e -> (Player) e)
                    .filter(p -> !p.equals(victim))
                    .collect(Collectors.toList());
        } else if (plugin.getConfigManager().isMessageToWorld()) {
            recipients = new ArrayList<>(victim.getWorld().getPlayers());
            recipients.remove(victim);
        } else {
            recipients = new ArrayList<>();
        }

        for (Player p : recipients) {
            p.sendMessage(message);
        }

        if (plugin.getConfigManager().isMessageToVictim()) {
            victim.sendMessage(message);
        }
    }

    private void handleInvalidCause(CommandSender sender) {
        String invalidCauseMsg = getMsg("invalid-cause", "&cInvalid cause. Use: {causes}");

        invalidCauseMsg = invalidCauseMsg.replace("{causes}",
                Arrays.toString(EntityDamageEvent.DamageCause.values()));

        sender.sendMessage(invalidCauseMsg);
    }
}

class TestException extends Exception {
    public TestException(String message) {
        super(message);
    }
}