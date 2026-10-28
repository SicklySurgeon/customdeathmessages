package com.sicklysurgeon.customdeathmessages.util.config;

import com.sicklysurgeon.customdeathmessages.CustomDeathMessages;
import com.sicklysurgeon.customdeathmessages.util.ParticleUtil;
import com.sicklysurgeon.customdeathmessages.util.SoundUtil;
import com.sicklysurgeon.customdeathmessages.util.managers.MessagesManager;
import org.bukkit.Sound;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class ConfigManager {

    private final CustomDeathMessages plugin;
    private final MessagesManager messagesManager;

    private FileConfiguration config;

    private Sound cachedSound;
    private String cachedParticle;
    private String cachedDarkenParticle;

    private Map<String, String> perWorldMessagesCache;
    private Map<String, SoundConfig> groupSoundCache;
    private final Map<String, BroadcastMode> broadcastModeCache = new HashMap<>();
    private final Map<String, Integer> broadcastRadiusCache = new HashMap<>();

    public enum BroadcastMode {
        GLOBAL,
        WORLD,
        RADIUS,
        VICTIM_ONLY
    }

    public static class SoundConfig {
        public final Sound sound;
        public final float volume;
        public final float pitch;

        public SoundConfig(Sound sound, float volume, float pitch) {
            this.sound = sound;
            this.volume = volume;
            this.pitch = pitch;
        }
    }

    public ConfigManager(CustomDeathMessages plugin, MessagesManager messagesManager) {
        this.plugin = plugin;
        this.messagesManager = messagesManager;
        this.groupSoundCache = new HashMap<>();
        this.perWorldMessagesCache = new HashMap<>();
    }

    public void initialize() {
        reloadConfig();
    }

    public void reloadConfig() {
        plugin.saveDefaultConfig();
        plugin.reloadConfig();
        loadConfig(plugin.getConfig());
    }

    public void loadConfig(FileConfiguration newConfig) {
        this.config = newConfig;
        rebuildCaches();
    }

    private void rebuildCaches() {
        if (config == null) {
            plugin.saveDefaultConfig();
            plugin.reloadConfig();
            config = plugin.getConfig();
        }

        Sound defaultSound = SoundUtil.defaultSound();
        String defaultParticle = ParticleUtil.DEFAULT_PARTICLE;
        String defaultDarkenParticle = ParticleUtil.DEFAULT_DARKEN_PARTICLE;

        this.broadcastModeCache.clear();
        this.broadcastRadiusCache.clear();

        this.cachedSound = SoundUtil.parse(
                config.getString("death-sound", "entity.player.death"),
                defaultSound,
                msg -> plugin.getLogger().warning(msg)
        );

        this.cachedParticle = ParticleUtil.parse(
                config.getString("death-particle", "EXPLOSION"),
                defaultParticle,
                msg -> plugin.getLogger().warning(msg)
        );

        this.cachedDarkenParticle = ParticleUtil.parse(
                config.getString("darken-effect-particle", "CLOUD"),
                defaultDarkenParticle,
                msg -> plugin.getLogger().warning(msg)
        );

        this.perWorldMessagesCache.clear();

        ConfigurationSection perWorldSection = config.getConfigurationSection("per-world-messages");
        if (perWorldSection != null) {
            for (String worldName : perWorldSection.getKeys(false)) {
                String msg = perWorldSection.getString(worldName + ".default");
                if (msg != null && !msg.isEmpty()) {
                    perWorldMessagesCache.put(worldName, msg);
                }
            }
        }

        this.groupSoundCache.clear();
        buildGroupSoundCache();
    }

    private void buildGroupSoundCache() {
        try {
            if (messagesManager == null) {
                plugin.getLogger().warning("MessagesManager is null. Skipping group sound cache.");
                return;
            }

            FileConfiguration messages = messagesManager.getMessages();
            if (messages == null) {
                plugin.getLogger().warning("Messages configuration is null. Skipping group sound cache.");
                return;
            }

            String[] groups = {"default", "vip", "admin"};

            for (String group : groups) {
                String groupPrefix = "groups." + group;

                if (messages.contains(groupPrefix + ".death-sound")) {
                    try {
                        String soundKeyName = messages.getString(groupPrefix + ".death-sound");

                        Sound sound = SoundUtil.parse(
                                soundKeyName,
                                SoundUtil.defaultSound(),
                                msg -> plugin.getLogger().warning(
                                        "Invalid sound config for group " + group + " in messages.yml: " + msg)
                        );

                        float volume = (float) Math.max(0.0,
                                messages.getDouble(groupPrefix + ".sound-volume", 1.0));
                        float pitch = (float) messages.getDouble(groupPrefix + ".sound-pitch", 1.0);

                        groupSoundCache.put(group, new SoundConfig(sound, volume, pitch));
                    } catch (Exception e) {
                        plugin.getLogger().warning("Invalid sound config for group " + group
                                + " in messages.yml: " + e.getMessage());
                    }
                }
            }
        } catch (Exception e) {
            plugin.getLogger().severe("Failed to build group sound cache: " + e.getMessage());
        }
    }

    public SoundConfig getGroupSoundConfig(String group) {
        return groupSoundCache.get(group);
    }

    public BroadcastMode parseBroadcastMode(String raw) {
        if (raw == null || raw.trim().isEmpty()) {
            return null;
        }

        String value = raw.trim().toUpperCase(Locale.ROOT);

        if (value.equals("VICTIM")) {
            return BroadcastMode.VICTIM_ONLY;
        }

        try {
            return BroadcastMode.valueOf(value);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    private BroadcastMode parseOrDefault(String raw, BroadcastMode fallback) {
        BroadcastMode parsed = parseBroadcastMode(raw);
        return parsed != null ? parsed : fallback;
    }

    public BroadcastMode getBroadcastMode(String effectType) {
        if (config == null) {
            return BroadcastMode.WORLD;
        }

        BroadcastMode cached = broadcastModeCache.get(effectType);
        if (cached != null) {
            return cached;
        }

        BroadcastMode resolved = resolveBroadcastMode(effectType);
        broadcastModeCache.put(effectType, resolved);
        return resolved;
    }

    private BroadcastMode resolveBroadcastMode(String effectType) {
        String effectKey = "effects-broadcast." + effectType + "-mode";
        if (config.contains(effectKey)) {
            BroadcastMode parsed = parseBroadcastMode(config.getString(effectKey));
            if (parsed != null) {
                return parsed;
            }
        }

        if ("sound".equals(effectType) && config.contains("sound-broadcast-mode")) {
            BroadcastMode parsed = parseBroadcastMode(config.getString("sound-broadcast-mode"));
            if (parsed != null) {
                return parsed;
            }
        }

        if ("particle".equals(effectType) && config.contains("particle-broadcast-mode")) {
            BroadcastMode parsed = parseBroadcastMode(config.getString("particle-broadcast-mode"));
            if (parsed != null) {
                return parsed;
            }
        }

        return parseOrDefault(
                config.getString("effects-broadcast.default-mode"),
                BroadcastMode.WORLD
        );
    }

    public int getBroadcastRadius(String effectType) {
        if (config == null) {
            return 50;
        }

        Integer cached = broadcastRadiusCache.get(effectType);
        if (cached != null) {
            return cached;
        }

        int resolved = resolveBroadcastRadius(effectType);
        broadcastRadiusCache.put(effectType, resolved);
        return resolved;
    }

    private int resolveBroadcastRadius(String effectType) {
        String effectRadiusKey = "effects-broadcast." + effectType + "-radius";
        if (config.contains(effectRadiusKey)) {
            return Math.max(0, config.getInt(effectRadiusKey, 50));
        }

        if ("sound".equals(effectType) && config.contains("sound-broadcast-radius")) {
            return Math.max(0, config.getInt("sound-broadcast-radius", 50));
        }

        if ("particle".equals(effectType) && config.contains("particle-broadcast-radius")) {
            return Math.max(0, config.getInt("particle-broadcast-radius", 50));
        }

        return Math.max(0, config.getInt("effects-broadcast.default-radius", 50));
    }

    public BroadcastMode getSoundBroadcastMode() {
        return getBroadcastMode("sound");
    }

    public int getSoundBroadcastRadius() {
        return getBroadcastRadius("sound");
    }

    public BroadcastMode getParticleBroadcastMode() {
        return getBroadcastMode("particle");
    }

    public int getParticleBroadcastRadius() {
        return getBroadcastRadius("particle");
    }

    public boolean isMessageToVictim() {
        return config != null && config.getBoolean("message-to-victim", true);
    }

    public boolean isMessageToWorld() {
        return config != null && config.getBoolean("message-to-world", true);
    }

    public int getMessageToRadius() {
        return config != null ? Math.max(0, config.getInt("message-to-radius", 0)) : 0;
    }

    public boolean isPlaySoundOnDeath() {
        return config != null && config.getBoolean("play-sound-on-death", true);
    }

    public Sound getCachedSound() {
        return cachedSound;
    }

    public float getSoundVolume() {
        return config != null ? (float) Math.max(0.0, config.getDouble("sound-volume", 1.0)) : 1.0f;
    }

    public float getSoundPitch() {
        return config != null ? (float) config.getDouble("sound-pitch", 1.0) : 1.0f;
    }

    public boolean isPlayParticlesOnDeath() {
        return config != null && config.getBoolean("play-particles-on-death", true);
    }

    public String getCachedParticle() {
        return cachedParticle;
    }

    public int getParticleCount() {
        return config != null ? Math.max(0, config.getInt("particle-count", 50)) : 50;
    }

    public FileConfiguration getConfig() {
        return this.config;
    }

    public float getParticleOffsetX() {
        return config != null ? (float) config.getDouble("particle-offset-x", 0.5) : 0.5f;
    }

    public float getParticleOffsetY() {
        return config != null ? (float) config.getDouble("particle-offset-y", 0.5) : 0.5f;
    }

    public float getParticleOffsetZ() {
        return config != null ? (float) config.getDouble("particle-offset-z", 0.5) : 0.5f;
    }

    public float getParticleExtra() {
        return config != null ? (float) config.getDouble("particle-extra", 0.1) : 0.1f;
    }

    public boolean isUsePermissionBasedMessages() {
        return config != null && config.getBoolean("use-permission-based-messages", false);
    }

    public String resolveMessageGroup(Player player) {
        if (player == null) {
            return PermissionGroup.DEFAULT.getConfigKey();
        }

        if (isUsePermissionBasedMessages()) {
            return PermissionGroup.fromPlayer(player).getConfigKey();
        }

        return PermissionGroup.DEFAULT.getConfigKey();
    }

    public boolean isPlayTitleOnDeath() {
        return config != null && config.getBoolean("play-title-on-death", false);
    }

    public String getDeathTitle() {
        return config != null ? config.getString("death-title", "&cYOU DIED") : "&cYOU DIED";
    }

    public String getDeathSubtitle() {
        return config != null ? config.getString("death-subtitle", "&7{cause}") : "&7{cause}";
    }

    public boolean isPlayActionBarOnDeath() {
        return config != null && config.getBoolean("play-actionbar-on-death", false);
    }

    public String getDeathActionBar() {
        return config != null ? config.getString("death-actionbar", "&c{victim} died!") : "&c{victim} died!";
    }

    public boolean isPlayDarkenScreenEffect() {
        return config != null && config.getBoolean("play-darken-screen-effect", false);
    }

    public int getDarkenEffectDurationTicks() {
        return config != null ? Math.max(0, config.getInt("darken-effect-duration-ticks", 20)) : 20;
    }

    public String getCachedDarkenParticle() {
        return cachedDarkenParticle;
    }

    public String getTimeFormat() {
        return config != null ? config.getString("time-format", "HH:mm:ss") : "HH:mm:ss";
    }

    public String getPerWorldMessage(String worldName) {
        return perWorldMessagesCache.get(worldName);
    }

    public boolean isRespawnMessageEnabled() {
        return config != null && config.getBoolean("respawn-message-enabled", true);
    }

    public String getRespawnMessage() {
        return config != null ? config.getString("respawn-message", "&a{player} has respawned!")
                : "&a{player} has respawned!";
    }

    public double getEpicDeathChance() {
        if (config == null) {
            return 0.05;
        }

        double chance = config.getDouble("epic-death-chance", 0.05);

        if (chance < 0.0) {
            return 0.0;
        }

        if (chance > 1.0) {
            return 1.0;
        }

        return chance;
    }

    public double getDeathMessageCost() {
        return config != null ? Math.max(0.0, config.getDouble("cost-per-death-message", 0.0)) : 0.0;
    }

    public boolean isExemptFromCost(String group) {
        if (config == null || group == null) {
            return false;
        }

        for (String exempt : config.getStringList("exempt-groups-from-cost")) {
            if (exempt != null && exempt.equalsIgnoreCase(group)) {
                return true;
            }
        }

        return false;
    }

    public boolean isLegacyColorSupport() {
        return config != null && config.getBoolean("legacy-color-support", false);
    }

    public boolean isLogDeathMessages() {
        return config != null && config.getBoolean("log-death-messages", true);
    }
}