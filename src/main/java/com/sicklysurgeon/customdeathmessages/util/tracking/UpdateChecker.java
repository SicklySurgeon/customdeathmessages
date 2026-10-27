package com.sicklysurgeon.customdeathmessages.util.tracking;

import com.sicklysurgeon.customdeathmessages.CustomDeathMessages;
import com.sicklysurgeon.customdeathmessages.util.formatter.ColorFormatter;
import org.bukkit.entity.Player;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class UpdateChecker {

    private static final Pattern VERSION_PATTERN =
            Pattern.compile("\"version_number\"\\s*:\\s*\"([^\"]+)\"");

    private final CustomDeathMessages plugin;
    private final String projectSlug;

    private volatile String latestVersion;
    private volatile boolean checked = false;

    public UpdateChecker(CustomDeathMessages plugin, String projectSlug) {
        this.plugin = plugin;
        this.projectSlug = projectSlug;
    }

    public void checkForUpdates() {
        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, this::performUpdateCheck);
    }

    private void performUpdateCheck() {
        this.latestVersion = null;
        this.checked = false;

        String apiUrl = "https://api.modrinth.com/v2/project/" + projectSlug + "/version";
        HttpURLConnection connection = null;

        try {
            URL url = URI.create(apiUrl).toURL();
            connection = (HttpURLConnection) url.openConnection();

            connection.setRequestMethod("GET");
            connection.setRequestProperty("User-Agent",
                    plugin.getDescription().getName() + "/" + plugin.getDescription().getVersion());
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(5000);

            int responseCode = connection.getResponseCode();

            if (responseCode == 200) {
                StringBuilder response = new StringBuilder();

                try (BufferedReader in = new BufferedReader(
                        new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8))) {

                    String inputLine;
                    while ((inputLine = in.readLine()) != null) {
                        response.append(inputLine);
                    }
                }

                parseLatestVersion(response.toString());
            } else {
                if (plugin.isEnabled()) {
                    plugin.getLogger().warning("Update check failed with HTTP status: " + responseCode);
                }
            }

        } catch (Exception e) {
            if (plugin.isEnabled()) {
                plugin.getLogger().log(Level.WARNING, "Exception during update check", e);
            }
        } finally {
            if (connection != null) {
                connection.disconnect();
            }

            checked = true;
        }
    }

    private void parseLatestVersion(String responseBody) {
        try {
            List<String> versions = extractVersionNumbers(responseBody);
            String best = null;

            for (String version : versions) {
                if (best == null || compareVersions(version, best) > 0) {
                    best = version;
                }
            }

            if (best != null && plugin.isEnabled()) {
                String current = plugin.getDescription().getVersion();

                if (compareVersions(best, current) > 0) {
                    this.latestVersion = best;
                    plugin.getLogger().info(
                            "[CustomDeathMessages] A new version is available! Current: "
                                    + current + ", New: " + best + ". View at: " + getProjectPageUrl());
                }
            }
        } catch (Exception e) {
            if (plugin.isEnabled()) {
                plugin.getLogger().log(Level.WARNING, "Failed to parse update response", e);
            }
        }
    }

    private List<String> extractVersionNumbers(String json) {
        List<String> versions = new ArrayList<>();
        Matcher matcher = VERSION_PATTERN.matcher(json);

        while (matcher.find()) {
            versions.add(matcher.group(1));
        }

        return versions;
    }

    private int compareVersions(String v1, String v2) {
        String clean1 = sanitizeVersion(v1);
        String clean2 = sanitizeVersion(v2);

        if (clean1.isEmpty() || clean2.isEmpty()) {
            return 0;
        }

        String[] p1 = clean1.split("\\.");
        String[] p2 = clean2.split("\\.");

        int len = Math.max(p1.length, p2.length);

        for (int i = 0; i < len; i++) {
            int n1 = 0;
            int n2 = 0;

            try {
                n1 = i < p1.length && !p1[i].isEmpty() ? Integer.parseInt(p1[i]) : 0;
                n2 = i < p2.length && !p2[i].isEmpty() ? Integer.parseInt(p2[i]) : 0;
            } catch (NumberFormatException e) {
                return 0;
            }

            if (n1 != n2) {
                return Integer.compare(n1, n2);
            }
        }

        return 0;
    }

    private String sanitizeVersion(String version) {
        if (version == null) {
            return "";
        }

        String s = version.replaceAll("[^0-9.]", "");
        s = s.replaceAll("\\.{2,}", ".");

        if (s.startsWith(".")) {
            s = s.substring(1);
        }

        if (s.endsWith(".")) {
            s = s.substring(0, s.length() - 1);
        }

        return s;
    }

    private String getProjectPageUrl() {
        return "https://modrinth.com/plugin/" + projectSlug;
    }

    public void notifyPlayerIfUpdateAvailable(Player player) {
        if (!checked || latestVersion == null || !plugin.isEnabled()) {
            return;
        }

        if (player.hasPermission("cdm.admin") || player.isOp()) {
            String msg = plugin.getMessagesManager().getMessages()
                    .getString("update-notifications.player",
                            "&e[CustomDeathMessages] &aA new version is available! &fCurrent: {current_version}, New: {new_version}. View at: {project_url}");
            if (msg == null) {
                msg = "&e[CustomDeathMessages] &aA new version is available! &fCurrent: {current_version}, New: {new_version}. View at: {project_url}";
            }

            msg = msg
                    .replace("{current_version}", plugin.getDescription().getVersion())
                    .replace("{new_version}", latestVersion)
                    .replace("{project_url}", getProjectPageUrl());

            player.sendMessage(ColorFormatter.formatColors(msg,
                    plugin.getConfigManager().isLegacyColorSupport()));
        }
    }

    public boolean isUpdateAvailable() {
        return latestVersion != null;
    }
}