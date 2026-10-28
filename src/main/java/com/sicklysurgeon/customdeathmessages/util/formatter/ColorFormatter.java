// 📄 src/main/java/com/sicklysurgeon/customdeathmessages/util/formatter/ColorFormatter.java

package com.sicklysurgeon.customdeathmessages.util.formatter;

import org.bukkit.ChatColor;

import java.lang.reflect.Method;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ColorFormatter {
    // Both hex syntaxes are supported: <#RRGGBB> and &#RRGGBB
    private static final Pattern HEX_TAG_PATTERN = Pattern.compile("<#([A-Fa-f0-9]{6})>");
    private static final Pattern HEX_AMPERSAND_PATTERN = Pattern.compile("&#([A-Fa-f0-9]{6})");

    private static final char[] LEGACY_CODES = {
            '0', '1', '2', '3', '4', '5', '6', '7',
            '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'
    };

    private static final int[][] LEGACY_RGB = {
            {0, 0, 0}, {0, 0, 170}, {0, 170, 0}, {0, 170, 170},
            {170, 0, 0}, {170, 0, 170}, {255, 170, 0}, {170, 170, 170},
            {85, 85, 85}, {85, 85, 255}, {85, 255, 85}, {85, 255, 255},
            {255, 85, 85}, {255, 85, 255}, {255, 255, 85}, {255, 255, 255}
    };

    public static String formatColors(String input, boolean legacySupport) {
        if (input == null) return null;

        String result = input;

        if (legacySupport) {
            // Simply remove hex codes
            result = HEX_TAG_PATTERN.matcher(result).replaceAll("");
            result = HEX_AMPERSAND_PATTERN.matcher(result).replaceAll("");
        } else {
            // Translate hex codes
            result = translateHex(result, HEX_TAG_PATTERN);
            result = translateHex(result, HEX_AMPERSAND_PATTERN);
        }

        // Always translate & codes
        result = ChatColor.translateAlternateColorCodes('&', result);

        return result;
    }

    private static String translateHex(String input, Pattern pattern) {
        Matcher matcher = pattern.matcher(input);
        StringBuffer buffer = new StringBuffer();
        while (matcher.find()) {
            matcher.appendReplacement(buffer, Matcher.quoteReplacement(hexToChatColor(matcher.group(1))));
        }
        matcher.appendTail(buffer);
        return buffer.toString();
    }

    /**
     * ChatColor#of only exists on 1.16+. On older servers the colour is
     * approximated with the closest legacy colour so messages still look right.
     */
    private static String hexToChatColor(String hex) {
        try {
            Class<?> chatColorClass = Class.forName("org.bukkit.ChatColor");
            Method of = chatColorClass.getMethod("of", String.class);
            Object color = of.invoke(null, "#" + hex);
            if (color != null) {
                return color.toString();
            }
        } catch (Throwable ignored) {
        }

        return legacyColor(hex);
    }

    private static String legacyColor(String hex) {
        int red;
        int green;
        int blue;

        try {
            red = Integer.parseInt(hex.substring(0, 2), 16);
            green = Integer.parseInt(hex.substring(2, 4), 16);
            blue = Integer.parseInt(hex.substring(4, 6), 16);
        } catch (RuntimeException e) {
            return "";
        }

        int bestIndex = 0;
        int bestDistance = Integer.MAX_VALUE;

        for (int i = 0; i < LEGACY_RGB.length; i++) {
            int[] candidate = LEGACY_RGB[i];
            int dr = red - candidate[0];
            int dg = green - candidate[1];
            int db = blue - candidate[2];
            int distance = dr * dr + dg * dg + db * db;

            if (distance < bestDistance) {
                bestDistance = distance;
                bestIndex = i;
            }
        }

        return ChatColor.getByChar(LEGACY_CODES[bestIndex]).toString();
    }
}
