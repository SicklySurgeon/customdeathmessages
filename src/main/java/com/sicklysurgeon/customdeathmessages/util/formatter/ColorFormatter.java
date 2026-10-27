// 📄 src/main/java/com/sicklysurgeon/customdeathmessages/util/formatter/ColorFormatter.java

package com.sicklysurgeon.customdeathmessages.util.formatter;

import net.md_5.bungee.api.ChatColor;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ColorFormatter {
    // Both hex syntaxes are supported: <#RRGGBB> and &#RRGGBB
    private static final Pattern HEX_TAG_PATTERN = Pattern.compile("<#([A-Fa-f0-9]{6})>");
    private static final Pattern HEX_AMPERSAND_PATTERN = Pattern.compile("&#([A-Fa-f0-9]{6})");

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
            ChatColor color = ChatColor.of("#" + matcher.group(1));
            matcher.appendReplacement(buffer, color.toString());
        }
        matcher.appendTail(buffer);
        return buffer.toString();
    }
}
