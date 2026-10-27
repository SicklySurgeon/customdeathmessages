package com.sicklysurgeon.customdeathmessages.command;

import com.sicklysurgeon.customdeathmessages.CustomDeathMessages;
import com.sicklysurgeon.customdeathmessages.util.ParticleUtil;
import com.sicklysurgeon.customdeathmessages.util.SoundUtil;
import com.sicklysurgeon.customdeathmessages.util.broadcast.BroadcastManager;
import com.sicklysurgeon.customdeathmessages.util.config.ConfigManager;
import com.sicklysurgeon.customdeathmessages.util.effects.ParticleManager;
import com.sicklysurgeon.customdeathmessages.util.effects.SoundManager;
import com.sicklysurgeon.customdeathmessages.util.formatter.ColorFormatter;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.Locale;

public class BroadcastCommand implements CommandExecutor {

    private final CustomDeathMessages plugin;
    private final BroadcastManager broadcastManager;
    private final SoundManager soundManager;
    private final ParticleManager particleManager;

    public BroadcastCommand(CustomDeathMessages plugin) {
        this.plugin = plugin;
        this.broadcastManager = new BroadcastManager(plugin);
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
        if (!sender.hasPermission("cdm.broadcast")) {
            sender.sendMessage(getMsg("broadcast-no-permission",
                    "&cNo permission! You need cdm.broadcast"));
            return true;
        }

        if (args.length < 1) {
            showHelp(sender);
            return true;
        }

        String subCommand = args[0].toLowerCase(Locale.ROOT);

        switch (subCommand) {
            case "sound":
                handleSoundBroadcast(sender, args);
                break;
            case "particle":
                handleParticleBroadcast(sender, args);
                break;
            case "message":
                handleMessageBroadcast(sender, args);
                break;
            case "test":
                handleTestBroadcast(sender, args);
                break;
            case "modes":
                showBroadcastModes(sender);
                break;
            case "global":
                handleGlobalBroadcast(sender, args);
                break;
            default:
                showHelp(sender);
        }

        return true;
    }

    private ConfigManager.BroadcastMode parseMode(String modeStr) {
        return plugin.getConfigManager().parseBroadcastMode(modeStr);
    }

    private int parseRadius(CommandSender sender, String raw, int fallback) {
        try {
            return Math.max(0, Integer.parseInt(raw));
        } catch (NumberFormatException e) {
            sender.sendMessage(getMsg("broadcast-invalid-radius",
                    "&cInvalid radius '{radius}'. Using {fallback} blocks.")
                    .replace("{radius}", raw)
                    .replace("{fallback}", String.valueOf(fallback)));
            return fallback;
        }
    }

    private void handleSoundBroadcast(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(getMsg("broadcast-player-only", "&cOnly players can use this!"));
            return;
        }

        Player player = (Player) sender;

        if (args.length < 2) {
            player.sendMessage(getMsg("broadcast-usage-sound",
                    "&cUsage: /cdm broadcast sound <GLOBAL|WORLD|RADIUS|VICTIM> [radius] [sound]"));
            player.sendMessage(getMsg("broadcast-usage-sound-example",
                    "&7Example: /cdm broadcast sound GLOBAL entity.player.death"));
            return;
        }

        ConfigManager.BroadcastMode mode = parseMode(args[1]);
        if (mode == null) {
            player.sendMessage(getMsg("broadcast-invalid-mode",
                    "&cInvalid mode! Use: GLOBAL, WORLD, RADIUS, or VICTIM"));
            return;
        }

        int radius = plugin.getConfigManager().getBroadcastRadius("sound");
        Sound sound = plugin.getConfigManager().getCachedSound();

        if (args.length > 2) {
            if (args[2].matches("-?\\d+")) {
                radius = parseRadius(sender, args[2], radius);

                if (args.length > 3) {
                    sound = parseSound(sender, args[3]);
                }
            } else {
                sound = parseSound(sender, args[2]);
            }
        }

        soundManager.playCustomSound(player, sound,
                plugin.getConfigManager().getSoundVolume(),
                plugin.getConfigManager().getSoundPitch(),
                mode, radius);

        String radiusInfo = mode == ConfigManager.BroadcastMode.RADIUS
                ? getMsg("broadcast-radius-suffix", " (radius: {radius} blocks)")
                .replace("{radius}", String.valueOf(radius))
                : "";

        String successMsg = getMsg("broadcast-success-sound",
                "&a✓ Broadcasted sound '{sound}' with mode: {mode}{radius_info}");

        successMsg = successMsg
                .replace("{sound}", SoundUtil.keyName(sound))
                .replace("{mode}", mode.name())
                .replace("{radius_info}", radiusInfo);

        player.sendMessage(successMsg);
    }

    private void handleParticleBroadcast(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(getMsg("broadcast-player-only", "&cOnly players can use this!"));
            return;
        }

        Player player = (Player) sender;

        if (args.length < 2) {
            player.sendMessage(getMsg("broadcast-usage-particle",
                    "&cUsage: /cdm broadcast particle <GLOBAL|WORLD|RADIUS|VICTIM> [radius] [particle]"));
            player.sendMessage(getMsg("broadcast-usage-particle-example",
                    "&7Example: /cdm broadcast particle GLOBAL EXPLOSION"));
            return;
        }

        ConfigManager.BroadcastMode mode = parseMode(args[1]);
        if (mode == null) {
            player.sendMessage(getMsg("broadcast-invalid-mode",
                    "&cInvalid mode! Use: GLOBAL, WORLD, RADIUS, or VICTIM"));
            return;
        }

        int radius = plugin.getConfigManager().getBroadcastRadius("particle");
        Particle particle = plugin.getConfigManager().getCachedParticle();
        int count = plugin.getConfigManager().getParticleCount();

        double offsetX = plugin.getConfigManager().getParticleOffsetX();
        double offsetY = plugin.getConfigManager().getParticleOffsetY();
        double offsetZ = plugin.getConfigManager().getParticleOffsetZ();
        double extra = plugin.getConfigManager().getParticleExtra();

        if (args.length > 2) {
            if (args[2].matches("-?\\d+")) {
                radius = parseRadius(sender, args[2], radius);

                if (args.length > 3) {
                    particle = parseParticle(sender, args[3]);
                }
            } else {
                particle = parseParticle(sender, args[2]);
            }
        }

        particleManager.spawnCustomParticles(player, particle, count,
                offsetX, offsetY, offsetZ, extra, mode, radius);

        String radiusInfo = mode == ConfigManager.BroadcastMode.RADIUS
                ? getMsg("broadcast-radius-suffix", " (radius: {radius} blocks)")
                .replace("{radius}", String.valueOf(radius))
                : "";

        String successMsg = getMsg("broadcast-success-particle",
                "&a✓ Broadcasted particles '{particle}' with mode: {mode}{radius_info}");

        successMsg = successMsg
                .replace("{particle}", particle.name())
                .replace("{mode}", mode.name())
                .replace("{radius_info}", radiusInfo);

        player.sendMessage(successMsg);
    }

    private void handleMessageBroadcast(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(getMsg("broadcast-player-only", "&cOnly players can use this!"));
            return;
        }

        Player player = (Player) sender;

        if (args.length < 3) {
            player.sendMessage(getMsg("broadcast-usage-message",
                    "&cUsage: /cdm broadcast message <GLOBAL|WORLD|RADIUS|VICTIM> <message>"));
            return;
        }

        ConfigManager.BroadcastMode mode = parseMode(args[1]);
        if (mode == null) {
            player.sendMessage(getMsg("broadcast-invalid-mode",
                    "&cInvalid mode! Use: GLOBAL, WORLD, RADIUS, or VICTIM"));
            return;
        }

        String message = String.join(" ", Arrays.copyOfRange(args, 2, args.length));
        int radius = mode == ConfigManager.BroadcastMode.RADIUS
                ? plugin.getConfigManager().getMessageToRadius() > 0
                ? plugin.getConfigManager().getMessageToRadius()
                : 50
                : 0;

        message = ColorFormatter.formatColors(message,
                plugin.getConfigManager().isLegacyColorSupport());

        broadcastManager.broadcastMessage(player, mode, radius, message);

        player.sendMessage(getMsg("broadcast-success-message",
                "&a✓ Broadcasted message with mode: {mode}")
                .replace("{mode}", mode.name()));
    }

    private void handleTestBroadcast(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(getMsg("broadcast-player-only", "&cOnly players can test!"));
            return;
        }

        Player player = (Player) sender;

        String result = broadcastManager.testBroadcastModes(player);

        for (String line : result.split("\n")) {
            if (!line.trim().isEmpty()) {
                sender.sendMessage(line);
            }
        }
    }

    private void handleGlobalBroadcast(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage(getMsg("broadcast-usage-global",
                    "&cUsage: /cdm broadcast global <sound|particle|message>"));
            return;
        }

        String type = args[1].toLowerCase(Locale.ROOT);

        switch (type) {
            case "sound":
                broadcastManager.broadcastGlobalSound(
                        plugin.getConfigManager().getCachedSound(),
                        plugin.getConfigManager().getSoundVolume(),
                        plugin.getConfigManager().getSoundPitch()
                );

                sender.sendMessage(getMsg("broadcast-success-global-sound",
                        "&a✓ Played global sound to all players!"));
                break;

            case "message":
                if (args.length < 3) {
                    sender.sendMessage(getMsg("broadcast-usage-global-message",
                            "&cUsage: /cdm broadcast global message <message>"));
                    return;
                }

                String message = String.join(" ", Arrays.copyOfRange(args, 2, args.length));
                message = ColorFormatter.formatColors(message,
                        plugin.getConfigManager().isLegacyColorSupport());

                broadcastManager.broadcastGlobalMessage(message);

                sender.sendMessage(getMsg("broadcast-success-global-message",
                        "&a✓ Sent global message to all players!"));
                break;

            case "particle":
                if (!(sender instanceof Player)) {
                    sender.sendMessage(getMsg("broadcast-player-only",
                            "&cOnly players can spawn particles!"));
                    return;
                }

                particleManager.spawnParticlesGlobal(
                        (Player) sender,
                        plugin.getConfigManager().getCachedParticle(),
                        plugin.getConfigManager().getParticleCount(),
                        plugin.getConfigManager().getParticleOffsetX(),
                        plugin.getConfigManager().getParticleOffsetY(),
                        plugin.getConfigManager().getParticleOffsetZ(),
                        plugin.getConfigManager().getParticleExtra()
                );

                sender.sendMessage(getMsg("broadcast-success-global-particle",
                        "&a✓ Spawned global particles!"));
                break;

            default:
                sender.sendMessage(getMsg("broadcast-invalid-type",
                        "&cInvalid type! Use: sound, particle, or message"));
        }
    }

    private void showBroadcastModes(CommandSender sender) {
        sender.sendMessage(getMsg("broadcast-modes-title", "&6=== Broadcast Modes ==="));
        sender.sendMessage(getMsg("broadcast-mode-global",
                "&bGLOBAL &7- All online players on the server"));
        sender.sendMessage(getMsg("broadcast-mode-world",
                "&bWORLD &7- All players in the same world"));
        sender.sendMessage(getMsg("broadcast-mode-radius",
                "&bRADIUS &7- Players within specified radius"));
        sender.sendMessage(getMsg("broadcast-mode-victim",
                "&bVICTIM_ONLY &7- Only the player who died"));
        sender.sendMessage("");

        sender.sendMessage(getMsg("broadcast-current-settings", "&6Current Effective Config Settings:"));

        ConfigManager.BroadcastMode soundMode = plugin.getConfigManager().getSoundBroadcastMode();
        ConfigManager.BroadcastMode particleMode = plugin.getConfigManager().getParticleBroadcastMode();

        sender.sendMessage(getMsg("broadcast-current-sound", "&bSound Mode: &f{mode}")
                .replace("{mode}", soundMode.name()));

        if (soundMode == ConfigManager.BroadcastMode.RADIUS) {
            sender.sendMessage(getMsg("broadcast-current-sound-radius",
                    "&7Sound Radius: &f{radius} blocks")
                    .replace("{radius}",
                            String.valueOf(plugin.getConfigManager().getSoundBroadcastRadius())));
        }

        sender.sendMessage(getMsg("broadcast-current-particle", "&bParticle Mode: &f{mode}")
                .replace("{mode}", particleMode.name()));

        if (particleMode == ConfigManager.BroadcastMode.RADIUS) {
            sender.sendMessage(getMsg("broadcast-current-particle-radius",
                    "&7Particle Radius: &f{radius} blocks")
                    .replace("{radius}",
                            String.valueOf(plugin.getConfigManager().getParticleBroadcastRadius())));
        }

        if (plugin.getConfigManager().getMessageToRadius() > 0) {
            sender.sendMessage(getMsg("broadcast-current-message-radius",
                    "&bMessage Mode: &fRADIUS &7({radius} blocks)")
                    .replace("{radius}",
                            String.valueOf(plugin.getConfigManager().getMessageToRadius())));
        } else if (plugin.getConfigManager().isMessageToWorld()) {
            sender.sendMessage(getMsg("broadcast-current-message-world",
                    "&bMessage Mode: &fWORLD"));
        } else {
            sender.sendMessage(getMsg("broadcast-current-message-victim",
                    "&bMessage Mode: &fVICTIM_ONLY"));
        }
    }

    private void showHelp(CommandSender sender) {
        sender.sendMessage(getMsg("broadcast-help-title", "&6=== Broadcast Command Help ==="));
        sender.sendMessage(getMsg("broadcast-help-sound",
                "&b/cdm broadcast sound <mode> [radius] [sound]"));
        sender.sendMessage(getMsg("broadcast-help-particle",
                "&b/cdm broadcast particle <mode> [radius] [particle]"));
        sender.sendMessage(getMsg("broadcast-help-message",
                "&b/cdm broadcast message <mode> <message>"));
        sender.sendMessage(getMsg("broadcast-help-test",
                "&b/cdm broadcast test &7- Test all modes"));
        sender.sendMessage(getMsg("broadcast-help-modes",
                "&b/cdm broadcast modes &7- Show available modes"));
        sender.sendMessage(getMsg("broadcast-help-global",
                "&b/cdm broadcast global <sound|particle|message> &7- Global broadcast"));
        sender.sendMessage(getMsg("broadcast-modes-list",
                "&7Modes: GLOBAL, WORLD, RADIUS, VICTIM"));
        sender.sendMessage(getMsg("broadcast-permission-info",
                "&7Permission: cdm.broadcast"));
    }

    private Sound parseSound(CommandSender sender, String soundName) {
        Sound sound = SoundUtil.tryParse(soundName);

        if (sound != null) {
            return sound;
        }

        sender.sendMessage(getMsg("config-invalid-sound",
                "&cInvalid sound '{sound_name}'. Using default: entity.player.death.")
                .replace("{sound_name}", soundName));

        return Sound.ENTITY_PLAYER_DEATH;
    }

    private Particle parseParticle(CommandSender sender, String particleName) {
        Particle particle = ParticleUtil.tryParse(particleName);

        if (particle != null) {
            return particle;
        }

        sender.sendMessage(getMsg("config-invalid-particle",
                "&cInvalid particle '{particle_name}'. Using default: EXPLOSION.")
                .replace("{particle_name}", particleName));

        return Particle.EXPLOSION;
    }
}