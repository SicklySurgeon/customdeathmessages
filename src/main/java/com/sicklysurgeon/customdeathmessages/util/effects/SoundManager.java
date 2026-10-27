// 📄 src/main/java/com/sicklysurgeon/customdeathmessages/util/effects/SoundManager.java
package com.sicklysurgeon.customdeathmessages.util.effects;

import com.sicklysurgeon.customdeathmessages.CustomDeathMessages;
import com.sicklysurgeon.customdeathmessages.util.broadcast.BroadcastManager;
import com.sicklysurgeon.customdeathmessages.util.config.ConfigManager;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

public class SoundManager {
    private final BroadcastManager broadcastManager;
    
    public SoundManager(CustomDeathMessages plugin) {
        this.broadcastManager = new BroadcastManager(plugin);
    }
    
    public void playDeathSound(Player player, ConfigManager configManager, 
                              CustomDeathMessages plugin, String permissionGroup, boolean isEpic) {
        
        Sound sound;
        float volume;
        float pitch;
        
        if (isEpic) {
            // Use cached epic sound from MessagesManager
            sound = plugin.getMessagesManager().getCachedEpicSound();
            volume = 2.0f; // Louder for epic
            pitch = 0.8f;  // Deeper for epic
        } else {
            ConfigManager.SoundConfig groupSoundConfig = configManager.getGroupSoundConfig(permissionGroup);
            if (groupSoundConfig != null) {
                sound = groupSoundConfig.sound;
                volume = groupSoundConfig.volume;
                pitch = groupSoundConfig.pitch;
            } else {
                sound = configManager.getCachedSound();
                volume = configManager.getSoundVolume();
                pitch = configManager.getSoundPitch();
            }
        }
        
        // Get broadcast settings
        ConfigManager.BroadcastMode mode = configManager.getBroadcastMode("sound");
        int radius = configManager.getBroadcastRadius("sound");
        
        // Broadcast the sound
        broadcastManager.broadcastSound(player, mode, radius, sound, volume, pitch);
    }
    
    /**
     * Play a custom sound with specific broadcast settings
     */
    public void playCustomSound(Player victim, Sound sound, float volume, float pitch,
                               ConfigManager.BroadcastMode mode, int radius) {
        broadcastManager.broadcastSound(victim, mode, radius, sound, volume, pitch);
    }
    
    /**
     * Play sound to global audience (all players on server)
     */
    public void playSoundGlobal(Player victim, Sound sound, float volume, float pitch) {
        broadcastManager.broadcastSound(victim, ConfigManager.BroadcastMode.GLOBAL, 0, 
                                      sound, volume, pitch);
    }
    
    /**
     * Play sound to all players in the same world
     */
    public void playSoundWorld(Player victim, Sound sound, float volume, float pitch) {
        broadcastManager.broadcastSound(victim, ConfigManager.BroadcastMode.WORLD, 0, 
                                      sound, volume, pitch);
    }
    
    /**
     * Play sound to players within radius
     */
    public void playSoundRadius(Player victim, Sound sound, float volume, float pitch, int radius) {
        broadcastManager.broadcastSound(victim, ConfigManager.BroadcastMode.RADIUS, radius, 
                                      sound, volume, pitch);
    }
    
    /**
     * Play sound to victim only
     */
    public void playSoundVictimOnly(Player victim, Sound sound, float volume, float pitch) {
        broadcastManager.broadcastSound(victim, ConfigManager.BroadcastMode.VICTIM_ONLY, 0, 
                                      sound, volume, pitch);
    }
    
    /**
     * Play death sound with custom broadcast mode (for testing)
     */
    public void playDeathSoundWithMode(Player player, ConfigManager configManager,
                                      CustomDeathMessages plugin, String permissionGroup, 
                                      boolean isEpic, ConfigManager.BroadcastMode mode, int radius) {
        Sound sound;
        float volume;
        float pitch;
        
        if (isEpic) {
            sound = plugin.getMessagesManager().getCachedEpicSound();
            volume = 2.0f;
            pitch = 0.8f;
        } else {
            ConfigManager.SoundConfig groupSoundConfig = configManager.getGroupSoundConfig(permissionGroup);
            if (groupSoundConfig != null) {
                sound = groupSoundConfig.sound;
                volume = groupSoundConfig.volume;
                pitch = groupSoundConfig.pitch;
            } else {
                sound = configManager.getCachedSound();
                volume = configManager.getSoundVolume();
                pitch = configManager.getSoundPitch();
            }
        }
        
        broadcastManager.broadcastSound(player, mode, radius, sound, volume, pitch);
    }
}