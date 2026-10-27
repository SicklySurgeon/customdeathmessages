package com.sicklysurgeon.customdeathmessages.util.config;

import com.sicklysurgeon.customdeathmessages.CustomDeathMessages;
import com.sicklysurgeon.customdeathmessages.util.ParticleUtil;
import com.sicklysurgeon.customdeathmessages.util.SoundUtil;
import com.sicklysurgeon.customdeathmessages.util.formatter.ColorFormatter;
import org.bukkit.ChatColor;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class ConfigValidator {

    private final ConfigManager configManager;
    private final FileConfiguration messages;
    private final List<String> report;

    public ConfigValidator(ConfigManager configManager, FileConfiguration messages) {
        this.configManager = configManager;
        this.messages = messages;
        this.report = new ArrayList<>();
    }

    public ConfigValidator(CustomDeathMessages plugin) {
        this(plugin.getConfigManager(), plugin.getMessagesManager().getMessages());
    }

    public List<String> generateValidationReport() {
        report.clear();

        report.add(formatMessage("config-validator-title"));

        validateConfigYml();
        validateMessagesYml();

        long issueCount = report.stream()
                .filter(line -> line.contains("❌") || line.contains("⚠️"))
                .count();

        if (issueCount == 0) {
            report.add(formatMessage("config-validator-success-all"));
        } else {
            String errorMsg = formatMessage("config-validator-error-count");
            errorMsg = errorMsg.replace("{error_count}", String.valueOf(issueCount));
            report.add(errorMsg);
        }

        return new ArrayList<>(report);
    }

    private void validateConfigYml() {
        FileConfiguration config = configManager.getConfig();

        validateMode(config, "sound-broadcast-mode", "sound-broadcast-radius", false);
        validateMode(config, "particle-broadcast-mode", "particle-broadcast-radius", false);

        boolean hasEffectsSection = config.contains("effects-broadcast");
        validateMode(config, "effects-broadcast.default-mode", "effects-broadcast.default-radius", !hasEffectsSection);

        String[] effectTypes = {"sound", "particle", "title", "actionbar", "darken-effect"};
        for (String effect : effectTypes) {
            String modeKey = "effects-broadcast." + effect + "-mode";
            String radiusKey = "effects-broadcast." + effect + "-radius";
            validateMode(config, modeKey, radiusKey, true);
        }

        double epicChance = config.getDouble("epic-death-chance", 0.05);
        if (epicChance < 0.0 || epicChance > 1.0) {
            report.add(ChatColor.RED + "❌ epic-death-chance must be between 0.0 and 1.0. Found: " + epicChance);
        } else {
            report.add(ChatColor.GREEN + "✅ epic-death-chance is valid: " + epicChance);
        }

        double cost = config.getDouble("cost-per-death-message", 0.0);
        if (cost < 0.0) {
            report.add(ChatColor.RED + "❌ cost-per-death-message must be >= 0. Found: " + cost);
        } else {
            report.add(ChatColor.GREEN + "✅ cost-per-death-message is valid: " + cost);
        }

        int radius = config.getInt("message-to-radius", 0);
        if (radius < 0) {
            report.add(ChatColor.RED + "❌ message-to-radius must be >= 0. Found: " + radius);
        } else {
            report.add(ChatColor.GREEN + "✅ message-to-radius is valid: " + radius);
        }

        String timeFormat = config.getString("time-format", "HH:mm:ss");
        try {
            DateTimeFormatter.ofPattern(timeFormat);
            report.add(ChatColor.GREEN + "✅ time-format is valid: " + timeFormat);
        } catch (Exception e) {
            report.add(ChatColor.RED + "❌ Invalid time-format: '" + timeFormat + "'");
        }

        List<String> exemptGroups = config.getStringList("exempt-groups-from-cost");
        String[] validGroups = {"default", "vip", "admin"};

        for (String group : exemptGroups) {
            boolean known = false;
            for (String valid : validGroups) {
                if (valid.equalsIgnoreCase(group)) {
                    known = true;
                    break;
                }
            }

            if (!known) {
                report.add(ChatColor.YELLOW + "⚠️ Group '" + group + "' in exempt-groups-from-cost is not a known group.");
            }
        }

        for (String group : validGroups) {
            String groupKey = "groups." + group;

            if (!messages.contains(groupKey)) {
                report.add(ChatColor.RED + "❌ Group '" + group + "' is missing in messages.yml.");
            } else {
                report.add(ChatColor.GREEN + "✅ Group '" + group + "' found in messages.yml.");

                boolean hasDefaultMessages = messages.contains(groupKey + ".default-messages");
                boolean hasCauseMessages = messages.contains(groupKey + ".cause-messages");

                if (!hasDefaultMessages && !hasCauseMessages) {
                    report.add(ChatColor.YELLOW + "⚠️ Group '" + group
                            + "' has no 'default-messages' or 'cause-messages'. "
                            + "It will always fall back to hardcoded default.");
                } else {
                    if (hasDefaultMessages) {
                        report.add(ChatColor.GREEN + "✅ Group '" + group + "' has default-messages.");
                    }

                    if (hasCauseMessages) {
                        ConfigurationSection causeSection =
                                messages.getConfigurationSection(groupKey + ".cause-messages");

                        if (causeSection != null) {
                            for (String causeName : causeSection.getKeys(false)) {
                                report.add(ChatColor.GREEN + "  ✅ cause-messages." + causeName + " configured.");
                            }
                        }
                    }
                }
            }
        }

        validateSoundAndParticles(config);
    }

    private void validateMode(FileConfiguration config, String key, String radiusKey, boolean onlyIfPresent) {
        if (onlyIfPresent && !config.contains(key)) {
            return;
        }

        String raw = config.getString(key, "WORLD");
        ConfigManager.BroadcastMode mode = configManager.parseBroadcastMode(raw);

        if (mode == null) {
            report.add(ChatColor.RED + "❌ " + key + " must be one of: GLOBAL, WORLD, RADIUS, VICTIM_ONLY, VICTIM. Found: " + raw);
            return;
        }

        report.add(ChatColor.GREEN + "✅ " + key + ": " + mode.name());

        if (mode == ConfigManager.BroadcastMode.RADIUS) {
            int radius = config.getInt(radiusKey, 50);

            if (radius <= 0) {
                report.add(ChatColor.RED + "❌ " + radiusKey + " must be > 0 when using RADIUS mode");
            } else {
                report.add(ChatColor.GREEN + "✅ " + radiusKey + ": " + radius + " blocks");
            }
        }
    }

    private void validateSoundAndParticles(FileConfiguration config) {
        String soundName = config.getString("death-sound", "entity.player.death");
        Sound sound = SoundUtil.tryParse(soundName);

        if (sound == null) {
            report.add(ChatColor.RED + "❌ Sound '" + soundName + "' not found. Default will be used.");
        } else {
            report.add(ChatColor.GREEN + "✅ Sound '" + soundName + "' is valid.");
        }

        validateParticle(config.getString("death-particle", "EXPLOSION"), "death-particle");
        validateParticle(config.getString("darken-effect-particle", "CLOUD"), "darken-effect-particle");

        validateGroupSounds();
        validateEpicEffects();
    }

    private void validateGroupSounds() {
        String[] groups = {"default", "vip", "admin"};

        for (String group : groups) {
            String groupKey = "groups." + group;

            if (messages.contains(groupKey + ".death-sound")) {
                String s = messages.getString(groupKey + ".death-sound");

                if (SoundUtil.tryParse(s) == null) {
                    report.add(ChatColor.RED + "❌ Invalid sound for group '" + group + "': " + s);
                } else {
                    report.add(ChatColor.GREEN + "✅ Sound for group '" + group + "' is valid.");
                }
            }
        }
    }

    private void validateEpicEffects() {
        String epicSound = messages.getString("epic-effects.sound", "ENTITY_LIGHTNING_BOLT_THUNDER");

        if (SoundUtil.tryParse(epicSound) == null) {
            report.add(ChatColor.RED + "❌ Invalid epic sound: " + epicSound);
        } else {
            report.add(ChatColor.GREEN + "✅ Epic sound is valid.");
        }

        validateParticle(messages.getString("epic-effects.particle", "EXPLOSION_EMITTER"), "epic-particle");

        int epicParticleCount = messages.getInt("epic-effects.particle-count", 100);
        if (epicParticleCount < 0) {
            report.add(ChatColor.RED + "❌ epic-effects.particle-count must be >= 0. Found: " + epicParticleCount);
        } else {
            report.add(ChatColor.GREEN + "✅ epic-effects.particle-count is valid: " + epicParticleCount);
        }
    }

    private void validateParticle(String name, String context) {
        Particle particle = ParticleUtil.tryParse(name);

        if (particle == null) {
            report.add(ChatColor.RED + "❌ Invalid particle '" + name + "' (" + context + ").");
        } else {
            report.add(ChatColor.GREEN + "✅ Particle '" + name + "' (" + context + ") is valid.");
        }
    }

    private void validateMessagesYml() {
        validateConditions();
    }

    private void validateConditions() {
        ConfigurationSection condRoot = messages.getConfigurationSection("conditional-messages");
        if (condRoot == null) {
            return;
        }

        for (String group : condRoot.getKeys(false)) {
            ConfigurationSection groupSection = condRoot.getConfigurationSection(group + ".messages");

            if (groupSection == null) {
                report.add(ChatColor.YELLOW + "⚠️ Group '" + group
                        + "' in conditional-messages has no 'messages' section.");
                continue;
            }

            report.add(ChatColor.GREEN + "✅ Conditional messages found for group '" + group + "'.");

            for (String cause : groupSection.getKeys(false)) {
                List<?> list = groupSection.getList(cause);
                if (list == null) {
                    continue;
                }

                for (int i = 0; i < list.size(); i++) {
                    Object obj = list.get(i);

                    if (obj instanceof Map) {
                        @SuppressWarnings("unchecked")
                        Map<String, Object> entry = (Map<String, Object>) obj;

                        Object condObj = entry.get("condition");
                        if (condObj instanceof String) {
                            String cond = (String) condObj;
                            if (!cond.equalsIgnoreCase("default")) {
                                validateConditionSyntax(cond, "conditional-messages." + group + ".messages." + cause + "[" + i + "]");
                            }
                        }
                    }
                }
            }
        }
    }

    private void validateConditionSyntax(String condition, String location) {
        String[] orParts = condition.split("(?i)\\s+OR\\s+");

        for (String orPart : orParts) {
            String[] andParts = orPart.split("(?i)\\s+AND\\s+");

            for (String andPart : andParts) {
                validateClause(andPart.trim(), location);
            }
        }
    }

    private void validateClause(String clause, String location) {
        if (clause.isEmpty()) {
            report.add(ChatColor.YELLOW + "⚠️ Empty condition clause at " + location);
            return;
        }

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

        String lower = clause.toLowerCase(Locale.ROOT);
        String foundOperator = null;
        int operatorIndex = -1;

        for (String op : operators) {
            int idx = lower.indexOf(op);
            if (idx >= 0) {
                foundOperator = op;
                operatorIndex = idx;
                break;
            }
        }

        if (foundOperator == null) {
            report.add(ChatColor.YELLOW + "⚠️ Invalid operator in condition at " + location + ": " + clause);
            return;
        }

        String left = clause.substring(0, operatorIndex).trim();
        String right = clause.substring(operatorIndex + foundOperator.length()).trim();

        if (!left.startsWith("{") || !left.endsWith("}")) {
            report.add(ChatColor.YELLOW + "⚠️ Malformed placeholder in condition at " + location + ": " + clause);
        }

        if (right.isEmpty()) {
            report.add(ChatColor.YELLOW + "⚠️ Missing value in condition at " + location + ": " + clause);
        }
    }

    public String getSummary() {
        StringBuilder sb = new StringBuilder();

        sb.append(ChatColor.GRAY).append("ℹ️ Effective configuration: ");

        ConfigManager.BroadcastMode soundMode = configManager.getSoundBroadcastMode();
        ConfigManager.BroadcastMode particleMode = configManager.getParticleBroadcastMode();

        sb.append("Sound: ").append(soundMode.name());
        if (soundMode == ConfigManager.BroadcastMode.RADIUS) {
            sb.append(" (").append(configManager.getSoundBroadcastRadius()).append(" blocks)");
        }

        sb.append(". Particles: ").append(particleMode.name());
        if (particleMode == ConfigManager.BroadcastMode.RADIUS) {
            sb.append(" (").append(configManager.getParticleBroadcastRadius()).append(" blocks)");
        }

        sb.append(". ");

        if (configManager.getMessageToRadius() > 0) {
            sb.append("Messages: RADIUS (").append(configManager.getMessageToRadius()).append(" blocks). ");
        } else if (configManager.isMessageToWorld()) {
            sb.append("Messages: WORLD. ");
        } else {
            sb.append("Messages: VICTIM ONLY. ");
        }

        sb.append(configManager.isPlaySoundOnDeath() ? "Sound: ON. " : "Sound: OFF. ");
        sb.append(configManager.isPlayParticlesOnDeath() ? "Particles: ON." : "Particles: OFF.");

        return sb.toString();
    }

    private String formatMessage(String key) {
        String raw = messages.getString("system-messages." + key, "MISSING: " + key);
        if (raw == null) {
            raw = "MISSING: " + key;
        }
        return ColorFormatter.formatColors(raw, configManager.isLegacyColorSupport());
    }
}