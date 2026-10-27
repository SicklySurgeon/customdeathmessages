package com.sicklysurgeon.customdeathmessages.util.managers;

import com.sicklysurgeon.customdeathmessages.CustomDeathMessages;
import com.sicklysurgeon.customdeathmessages.util.config.PermissionGroup;
import com.sicklysurgeon.customdeathmessages.util.context.DeathContext;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.stream.Collectors;

public class MessageSelector {

    private final CustomDeathMessages plugin;
    private final Random random = new Random();
    private final Map<UUID, String> lastUsedMessage = new HashMap<>();

    public MessageSelector(CustomDeathMessages plugin) {
        this.plugin = plugin;
    }

    public void clearHistory(UUID uuid) {
        if (uuid != null) {
            lastUsedMessage.remove(uuid);
        }
    }

    public String selectRawMessage(DeathContext context) {
        boolean isEpic = random.nextDouble() < plugin.getConfigManager().getEpicDeathChance();
        return selectRawMessage(context, isEpic);
    }

    public String selectRawMessage(DeathContext context, boolean isEpic) {
        Player victim = context.getVictim();
        EntityDamageEvent.DamageCause cause = context.getCause();
        String worldName = victim.getWorld().getName();

        String rawMessage = plugin.getConfigManager().getPerWorldMessage(worldName);
        if (rawMessage != null && !rawMessage.isEmpty()) {
            return rawMessage;
        }

        if (isEpic) {
            return selectEpicMessage();
        }

        String permissionGroup = getPermissionGroup(victim);
        rawMessage = getGroupMessage(permissionGroup, cause, victim.getUniqueId(), context);

        if (rawMessage == null || rawMessage.isEmpty()) {
            String warningMsg = plugin.getMessagesManager().getMessages()
                    .getString("system-messages.message-not-found",
                            "No message found for world: {world_name} or group/cause. Using hardcoded default.");
            if (warningMsg == null) {
                warningMsg = "No message found for world: {world_name} or group/cause. Using hardcoded default.";
            }
            warningMsg = warningMsg.replace("{world_name}", worldName);
            plugin.getLogger().warning(warningMsg);

            rawMessage = "&c{victim} has died.";
        }

        return rawMessage;
    }

    private String selectEpicMessage() {
        FileConfiguration messages = plugin.getMessagesManager().getMessages();

        if (messages.contains("epic-messages") && messages.isList("epic-messages")) {
            List<String> epicList = messages.getStringList("epic-messages");

            if (!epicList.isEmpty()) {
                return epicList.get(random.nextInt(epicList.size()));
            }
        }

        return "&6&lEPIC DEATH! &e{victim} died in style!";
    }

    private String getPermissionGroup(Player victim) {
        if (plugin.getConfigManager().isUsePermissionBasedMessages()) {
            return PermissionGroup.fromPlayer(victim).getConfigKey();
        }

        return PermissionGroup.DEFAULT.getConfigKey();
    }

    private String getGroupMessage(String group,
                                   EntityDamageEvent.DamageCause cause,
                                   UUID playerId,
                                   DeathContext context) {
        List<?> conditionalList = plugin.getMessagesManager().getConditionalMessages(group, cause);

        if (conditionalList != null) {
            for (Object obj : conditionalList) {
                if (obj instanceof Map) {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> entry = (Map<String, Object>) obj;

                    String condition = (String) entry.get("condition");
                    String message = (String) entry.get("message");

                    if (condition != null && message != null
                            && !"default".equalsIgnoreCase(condition)) {
                        if (evaluateCondition(condition, context)) {
                            return message;
                        }
                    }
                }
            }

            for (Object obj : conditionalList) {
                if (obj instanceof Map) {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> entry = (Map<String, Object>) obj;

                    if ("default".equalsIgnoreCase((String) entry.get("condition"))) {
                        return (String) entry.get("message");
                    }

                    if (entry.containsKey("default")) {
                        Object defaultVal = entry.get("default");
                        if (defaultVal instanceof String) {
                            return (String) defaultVal;
                        }
                    }
                }
            }
        }

        Object messageObj = plugin.getMessagesManager()
                .getCachedMessage(group, "messages." + cause.name());

        String message = extractMessage(messageObj, playerId);

        if (message == null || message.isEmpty()) {
            messageObj = plugin.getMessagesManager().getCachedMessage(group, "default");
            message = extractMessage(messageObj, playerId);
        }

        return message;
    }

    private boolean evaluateCondition(String condition, DeathContext context) {
        String[] orClauses = condition.split("(?i)\\s+OR\\s+", -1);

        for (String orClause : orClauses) {
            String[] andClauses = orClause.split("(?i)\\s+AND\\s+", -1);
            boolean allAndTrue = true;

            for (String andClause : andClauses) {
                if (!evaluateSingleCondition(andClause.trim(), context)) {
                    allAndTrue = false;
                    break;
                }
            }

            if (allAndTrue) {
                return true;
            }
        }

        return false;
    }

    private boolean evaluateSingleCondition(String singleCondition, DeathContext context) {
        String[] operators = {
                " >= ",
                " <= ",
                " contains ",
                " equals ",
                " startswith ",
                " endswith ",
                " > ",
                " < "
        };

        String lowerCondition = singleCondition.toLowerCase(Locale.ROOT);

        for (String op : operators) {
            int opIndex = lowerCondition.indexOf(op);

            if (opIndex >= 0) {
                String placeholderPart = singleCondition.substring(0, opIndex).trim();
                String valuePart = singleCondition.substring(opIndex + op.length()).trim();

                if (!placeholderPart.startsWith("{") || !placeholderPart.endsWith("}")) {
                    plugin.getLogger().warning("Invalid condition format: " + singleCondition);
                    return false;
                }

                String placeholderKey = placeholderPart.substring(1, placeholderPart.length() - 1);

                String expectedValue;
                if (valuePart.startsWith("'") && valuePart.endsWith("'")) {
                    expectedValue = valuePart.substring(1, valuePart.length() - 1);
                } else {
                    expectedValue = valuePart;
                }

                String actualValue = context.getPlugin().getMessageFormatter()
                        .getPlaceholderRegistry()
                        .replacePlaceholders("{" + placeholderKey + "}", context);

                if (actualValue == null) {
                    return false;
                }

                switch (op.trim()) {
                    case "contains":
                        return actualValue.contains(expectedValue);
                    case "equals":
                        return actualValue.equals(expectedValue);
                    case "startswith":
                        return actualValue.startsWith(expectedValue);
                    case "endswith":
                        return actualValue.endsWith(expectedValue);
                    case ">":
                        try {
                            return Double.parseDouble(actualValue) > Double.parseDouble(expectedValue);
                        } catch (NumberFormatException e) {
                            return false;
                        }
                    case "<":
                        try {
                            return Double.parseDouble(actualValue) < Double.parseDouble(expectedValue);
                        } catch (NumberFormatException e) {
                            return false;
                        }
                    case ">=":
                        try {
                            return Double.parseDouble(actualValue) >= Double.parseDouble(expectedValue);
                        } catch (NumberFormatException e) {
                            return false;
                        }
                    case "<=":
                        try {
                            return Double.parseDouble(actualValue) <= Double.parseDouble(expectedValue);
                        } catch (NumberFormatException e) {
                            return false;
                        }
                    default:
                        return false;
                }
            }
        }

        plugin.getLogger().warning("Unknown operator in condition: " + singleCondition);
        return false;
    }

    private String extractMessage(Object messageObj, UUID playerId) {
        if (messageObj instanceof List) {
            @SuppressWarnings("unchecked")
            List<String> list = (List<String>) messageObj;

            if (!list.isEmpty()) {
                String lastMsg = lastUsedMessage.get(playerId);

                List<String> available = list.stream()
                        .filter(msg -> !msg.equals(lastMsg))
                        .collect(Collectors.toList());

                if (available.isEmpty()) {
                    available = list;
                }

                String selected = available.get(random.nextInt(available.size()));
                lastUsedMessage.put(playerId, selected);

                return selected;
            }
        } else if (messageObj instanceof String) {
            return (String) messageObj;
        }

        return null;
    }
}