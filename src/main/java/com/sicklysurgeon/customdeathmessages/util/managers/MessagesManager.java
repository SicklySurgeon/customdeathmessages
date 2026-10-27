package com.sicklysurgeon.customdeathmessages.util.managers;

import com.sicklysurgeon.customdeathmessages.CustomDeathMessages;
import com.sicklysurgeon.customdeathmessages.util.ParticleUtil;
import com.sicklysurgeon.customdeathmessages.util.SoundUtil;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.entity.EntityDamageEvent;

import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class MessagesManager {

    private final CustomDeathMessages plugin;

    private FileConfiguration messagesConfig;
    private File messagesFile;

    private Map<String, Map<String, Object>> messageCache;
    private Map<String, List<?>> conditionalMessageCache;

    private Sound cachedEpicSound;
    private Particle cachedEpicParticle;

    private String epicSoundName;
    private String epicParticleName;
    private int epicParticleCount;
    private String epicTitle;
    private String epicSubtitle;

    public MessagesManager(CustomDeathMessages plugin) {
        this.plugin = plugin;
        this.messageCache = new HashMap<>();
        this.conditionalMessageCache = new HashMap<>();
        reloadMessages();
    }

    public void reloadMessages() {
        messagesFile = new File(plugin.getDataFolder(), "messages.yml");

        if (!messagesFile.exists()) {
            plugin.saveResource("messages.yml", false);
        }

        messagesConfig = YamlConfiguration.loadConfiguration(messagesFile);

        InputStream defaultStream = plugin.getResource("messages.yml");
        if (defaultStream != null) {
            YamlConfiguration defaultConfig = YamlConfiguration.loadConfiguration(
                    new InputStreamReader(defaultStream, StandardCharsets.UTF_8)
            );
            messagesConfig.setDefaults(defaultConfig);
        }

        initializeEpicEffectsCache();

        this.messageCache.clear();
        buildMessageCache();
    }

    private void initializeEpicEffectsCache() {
        Sound defaultEpicSound = Sound.ENTITY_LIGHTNING_BOLT_THUNDER;
        Particle defaultEpicParticle = Particle.EXPLOSION;

        String rawEpicSound = messagesConfig.getString(
                "epic-effects.sound",
                "ENTITY_LIGHTNING_BOLT_THUNDER"
        );

        String rawEpicParticle = messagesConfig.getString(
                "epic-effects.particle",
                "EXPLOSION_EMITTER"
        );

        this.cachedEpicSound = SoundUtil.parse(
                rawEpicSound,
                defaultEpicSound,
                msg -> plugin.getLogger().warning(msg)
        );

        this.cachedEpicParticle = ParticleUtil.parse(
                rawEpicParticle,
                defaultEpicParticle,
                msg -> plugin.getLogger().warning(msg)
        );

        this.epicSoundName = SoundUtil.keyName(cachedEpicSound);
        this.epicParticleName = ParticleUtil.name(cachedEpicParticle);

        this.epicParticleCount = Math.max(0,
                messagesConfig.getInt("epic-effects.particle-count", 100));
        this.epicTitle = messagesConfig.getString("epic-effects.title", "&6&lEPIC DEATH!");
        this.epicSubtitle = messagesConfig.getString("epic-effects.subtitle", "&e{victim} died in style!");
    }

    public Sound getCachedEpicSound() {
        return cachedEpicSound;
    }

    public Particle getCachedEpicParticle() {
        return cachedEpicParticle;
    }

    public String getEpicSoundName() {
        return epicSoundName;
    }

    public String getEpicParticleName() {
        return epicParticleName;
    }

    public int getEpicParticleCount() {
        return epicParticleCount;
    }

    public String getEpicTitle() {
        return epicTitle;
    }

    public String getEpicSubtitle() {
        return epicSubtitle;
    }

    private void buildMessageCache() {
        String[] groups = {"default", "vip", "admin"};

        for (String group : groups) {
            Map<String, Object> groupCache = new HashMap<>();

            String groupPrefix = "groups." + group;

            String defaultKey = groupPrefix + ".default-messages";
            if (messagesConfig.contains(defaultKey)) {
                if (messagesConfig.isList(defaultKey)) {
                    groupCache.put("default", messagesConfig.getStringList(defaultKey));
                } else {
                    groupCache.put("default", messagesConfig.getString(defaultKey));
                }
            }

            String causeSectionKey = groupPrefix + ".cause-messages";
            if (messagesConfig.contains(causeSectionKey)) {
                ConfigurationSection causeSection =
                        messagesConfig.getConfigurationSection(causeSectionKey);

                if (causeSection != null) {
                    for (String causeName : causeSection.getKeys(false)) {
                        String normalizedCause = causeName.toUpperCase(Locale.ROOT);
                        String cacheKey = "messages." + normalizedCause;
                        String fullKey = causeSectionKey + "." + causeName;

                        if (messagesConfig.isList(fullKey)) {
                            groupCache.put(cacheKey, messagesConfig.getStringList(fullKey));
                        } else {
                            groupCache.put(cacheKey, messagesConfig.getString(fullKey));
                        }
                    }
                }
            }

            this.messageCache.put(group, groupCache);
        }

        buildConditionalMessageCache();
    }

    /**
     * Caches the conditional message lists per group/cause so deaths do not have
     * to traverse the YAML configuration sections every time.
     */
    private void buildConditionalMessageCache() {
        this.conditionalMessageCache.clear();

        ConfigurationSection conditionalRoot =
                messagesConfig.getConfigurationSection("conditional-messages");
        if (conditionalRoot == null) {
            return;
        }

        for (String group : conditionalRoot.getKeys(false)) {
            ConfigurationSection causeSection =
                    conditionalRoot.getConfigurationSection(group + ".messages");
            if (causeSection == null) {
                continue;
            }

            for (String causeName : causeSection.getKeys(false)) {
                List<?> messages = causeSection.getList(causeName);
                if (messages != null && !messages.isEmpty()) {
                    conditionalMessageCache.put(
                            group + "." + causeName.toUpperCase(Locale.ROOT),
                            messages
                    );
                }
            }
        }
    }

    public List<?> getConditionalMessages(String group, EntityDamageEvent.DamageCause cause) {
        if (group == null || cause == null) {
            return null;
        }

        return conditionalMessageCache.get(group + "." + cause.name());
    }

    public Object getCachedMessage(String group, String key) {
        Map<String, Object> groupCache = messageCache.get(group);

        if (groupCache != null) {
            return groupCache.get(key);
        }

        return null;
    }

    public FileConfiguration getMessages() {
        return messagesConfig;
    }
}