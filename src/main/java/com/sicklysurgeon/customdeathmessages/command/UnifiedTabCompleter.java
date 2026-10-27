package com.sicklysurgeon.customdeathmessages.command;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.event.entity.EntityDamageEvent;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

public class UnifiedTabCompleter implements TabCompleter {

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            String input = args[0].toLowerCase(Locale.ROOT);
            return Arrays.asList("test", "help", "config", "reload", "broadcast")
                    .stream()
                    .filter(cmd -> cmd.startsWith(input))
                    .filter(cmd -> {
                        switch (cmd) {
                            case "test":
                                return sender.hasPermission("cdm.test");
                            case "config":
                            case "reload":
                                return sender.hasPermission("cdm.reload");
                            case "broadcast":
                                return sender.hasPermission("cdm.broadcast");
                            default:
                                return true;
                        }
                    })
                    .collect(Collectors.toList());
        }

        if ("test".equalsIgnoreCase(args[0])) {
            if (!sender.hasPermission("cdm.test")) {
                return Collections.emptyList();
            }

            if (args.length == 2) {
                String input = args[1].toLowerCase(Locale.ROOT);
                List<String> completions = new ArrayList<>();

                if ("broadcast".startsWith(input)) {
                    completions.add("broadcast");
                }

                Arrays.stream(EntityDamageEvent.DamageCause.values())
                        .map(cause -> cause.name())
                        .filter(c -> c.toLowerCase(Locale.ROOT).startsWith(input))
                        .forEach(completions::add);

                Bukkit.getOnlinePlayers().stream()
                        .map(player -> player.getName())
                        .filter(n -> n.toLowerCase(Locale.ROOT).startsWith(input))
                        .forEach(completions::add);

                return completions;
            }

            if (args.length == 3) {
                String input = args[2].toLowerCase(Locale.ROOT);
                List<String> completions = new ArrayList<>();

                if ("broadcast".equalsIgnoreCase(args[1])) {
                    Bukkit.getOnlinePlayers().stream()
                            .map(player -> player.getName())
                            .filter(n -> n.toLowerCase(Locale.ROOT).startsWith(input))
                            .forEach(completions::add);

                    return completions;
                }

                Arrays.stream(EntityDamageEvent.DamageCause.values())
                        .map(cause -> cause.name())
                        .filter(c -> c.toLowerCase(Locale.ROOT).startsWith(input))
                        .forEach(completions::add);

                Bukkit.getOnlinePlayers().stream()
                        .map(player -> player.getName())
                        .filter(n -> n.toLowerCase(Locale.ROOT).startsWith(input))
                        .forEach(completions::add);

                if ("global".startsWith(input)) {
                    completions.add("global");
                }

                if ("global-sound".startsWith(input)) {
                    completions.add("global-sound");
                }

                if ("global-particles".startsWith(input)) {
                    completions.add("global-particles");
                }

                return completions;
            }

            if (args.length == 4) {
                String input = args[3].toLowerCase(Locale.ROOT);
                List<String> completions = new ArrayList<>();

                if ("broadcast".equalsIgnoreCase(args[1])) {
                    Arrays.stream(EntityDamageEvent.DamageCause.values())
                            .map(cause -> cause.name())
                            .filter(c -> c.toLowerCase(Locale.ROOT).startsWith(input))
                            .forEach(completions::add);
                } else {
                    Bukkit.getOnlinePlayers().stream()
                            .map(player -> player.getName())
                            .filter(n -> n.toLowerCase(Locale.ROOT).startsWith(input))
                            .forEach(completions::add);
                }

                return completions;
            }
        }

        if (args.length == 2) {
            if ("config".equalsIgnoreCase(args[0])) {
                if (!sender.hasPermission("cdm.reload")) {
                    return Collections.emptyList();
                }

                return filterPrefix(Collections.singletonList("validate"), args[1]);
            }

            if ("help".equalsIgnoreCase(args[0])) {
                return filterPrefix(Arrays.asList("test", "reload", "config validate", "broadcast", "help"), args[1]);
            }

            if ("broadcast".equalsIgnoreCase(args[0])) {
                return sender.hasPermission("cdm.broadcast")
                        ? filterPrefix(Arrays.asList("sound", "particle", "message", "test", "modes", "global"), args[1])
                        : Collections.emptyList();
            }
        }

        if (args.length == 3 && "broadcast".equalsIgnoreCase(args[0])) {
            if (!sender.hasPermission("cdm.broadcast")) {
                return Collections.emptyList();
            }

            String subCommand = args[1].toLowerCase(Locale.ROOT);

            if ("global".equals(subCommand)) {
                return filterPrefix(Arrays.asList("sound", "particle", "message"), args[2]);
            }

            if ("sound".equals(subCommand)
                    || "particle".equals(subCommand)
                    || "message".equals(subCommand)) {
                return filterPrefix(Arrays.asList("GLOBAL", "WORLD", "RADIUS", "VICTIM"), args[2]);
            }
        }

        return Collections.emptyList();
    }

    private List<String> filterPrefix(List<String> values, String input) {
        String prefix = input == null ? "" : input.toLowerCase(Locale.ROOT);
        return values.stream()
                .filter(value -> value.toLowerCase(Locale.ROOT).startsWith(prefix))
                .collect(Collectors.toList());
    }
}