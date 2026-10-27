// src/main/java/com/sicklysurgeon/customdeathmessages/util/formatter/EntityNameFormatter.java
package com.sicklysurgeon.customdeathmessages.util.formatter;

import org.bukkit.entity.Entity;
import java.util.Arrays;
import java.util.Locale;
import java.util.stream.Collectors;

/**
 * Formatea nombres de entidades de manera consistente.
 */
public class EntityNameFormatter {
    
    /**
     * Formatea el nombre de una entidad para mostrarlo en mensajes.
     */
    public String formatEntityName(Entity entity) {
        if (entity == null) return "Unknown";
        if (entity.getCustomName() != null && !entity.getCustomName().isEmpty()) {
            return entity.getCustomName();
        }

        String entityTypeName = entity.getType().name();

        // Special cases
        switch (entityTypeName) {
            case "PRIMED_TNT":
            case "TNT":
                return "TNT";
            case "AREA_EFFECT_CLOUD":
                return "Cloud";
            case "FISHING_BOBBER":
                return "Fishing Bobber";
            default:
                break;
        }

        String typeName = entityTypeName.toLowerCase(Locale.ROOT).replace("_", " ");
        return capitalizeWords(typeName);
    }
    
    /**
     * Capitaliza cada palabra en una cadena.
     */
    private String capitalizeWords(String input) {
        if (input == null || input.isEmpty()) {
            return input;
        }
        
        return Arrays.stream(input.split(" "))
                .map(word -> {
                    if (word.isEmpty()) return word;
                    return Character.toUpperCase(word.charAt(0)) + word.substring(1);
                })
                .collect(Collectors.joining(" "));
    }
    
    /**
     * Formatea un nombre de causa de daño.
     */
    public String formatDamageCause(String damageCause) {
        if (damageCause == null || damageCause.isEmpty()) {
            return "Unknown";
        }
        
        return damageCause.toLowerCase(Locale.ROOT).replace("_", " ");
    }
}