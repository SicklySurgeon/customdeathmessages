# CustomDeathMessages — Version 1.6.1

## Minecraft 1.21.x–26.3 Compatibility Hotfix

### Description

Version 1.6.1 is a small but important compatibility hotfix for the 1.6 release. It fixes a plugin metadata issue that caused servers running Minecraft 1.21.x, including 1.21.11, to reject the plugin at startup with an `Unsupported API version 26.3` error.

The plugin code itself was already compatible with 1.21.x. The problem was the `api-version` declaration in `plugin.yml`: 1.6 declared 26.3, which is the minimum API version the plugin requires. A 1.21.11 server correctly refused to load it because its API is older. Version 1.6.1 declares `api-version: '1.21'`, so the same code now loads on 1.21.x and still loads on 26.3.

This release does not change gameplay, configuration, messages, commands, or the features introduced in 1.6. It is a drop-in replacement for 1.6.

### Includes All 1.6 Changes

Version 1.6.1 contains the complete 1.6 release, including:

- Hex color rendering fixes for `<#RRGGBB>` and `&#RRGGBB` across death messages, titles, action bars, commands, and feedback.
- Death attribution, cross-world distance, projectile, null-configuration, broadcast, PlaceholderAPI, Vault, and Essentials fixes.
- Performance improvements for conditional messages, broadcast settings, placeholders, particle recipients, and tab completion.
- The Minecraft 26.3 build with Spigot API 26.3 and Java 21.

For the full list of 1.6 changes, see the 1.6 release notes.

### What Changed

- Changed `plugin.yml` from `api-version: '26.3'` to `api-version: '1.21'`.
- Kept the build on Spigot API 26.3 and Java 21.
- Updated the plugin version metadata to 1.6.1 in `pom.xml`, `plugin.yml`, `config.yml`, and `messages.yml`.
- Rebuilt and packaged `customdeathmessages-1.6.1.jar`.
- Verified the JAR on Paper 1.21.11 (build 132) and Paper 26.3 (build 206). The plugin loaded and enabled successfully on both.

### Compatibility

| Item | Support |
|---|---|
| Minecraft | 1.21.x through 26.3 |
| Java | 21 or newer |
| Servers | Spigot, Paper, Purpur |
| Optional integrations | PlaceholderAPI, Vault, Essentials |
| Configuration | Existing 1.6 `config.yml` and `messages.yml` remain valid |

Minecraft 1.20.x and older are **not supported by this build**. They use an older API and cannot load a plugin that declares API 1.21. A separate legacy build for 1.16.5–1.20.x is planned for a later release. Do not mark those versions as compatible with 1.6.1.

### Upgrade Notes

1. Back up `plugins/CustomDeathMessages/config.yml` and `messages.yml`.
2. Replace the old JAR with `customdeathmessages-1.6.1.jar`.
3. Restart the server. A full restart is recommended.
4. Existing configuration files are not overwritten.
5. No new configuration keys are introduced in 1.6.1.
6. No changes to `legacy-color-support`; keep it `false` on 1.21+ to render hex colors.

### Why `api-version` Is 1.21, Not 26.3

`api-version` is the **minimum** API version a plugin requires, not the maximum version it supports. Declaring 26.3 tells every older server to reject the plugin, even when the code is compatible. Declaring 1.21 allows 1.21.x and newer servers, including 26.3, to load it. The plugin is compiled against Spigot 26.3 and load-tested on 26.3, so 26.3 support is unchanged.

### Summary

- Fixes 1.6 failing to load on Minecraft 1.21.x, including 1.21.11.
- Keeps full Minecraft 26.3 support.
- No gameplay, configuration, or message changes.
- Recommended for all 1.6 users on Minecraft 1.21.x–26.3.
- A separate 1.16.5–1.20.x legacy build is planned for later.
