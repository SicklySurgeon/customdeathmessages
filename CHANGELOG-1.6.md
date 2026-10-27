# CustomDeathMessages - Version 1.6

> **Note:** Version 1.6 was superseded by 1.6.1 because its original `api-version: '26.3'` metadata prevented the plugin from loading on Minecraft 1.21.x. Use 1.6.1 for Minecraft 1.21.x–26.3. See `CHANGELOG-1.6.1.md`.

## Hex Color Rendering, Performance, and Minecraft 26.3 Compatibility Release

### Description

Version 1.6 is a focused quality, compatibility, and performance release. It resolves the remaining hex color rendering problems that could make `<#RRGGBB>` and `&#RRGGBB` appear as literal text, fixes action bar colors, and makes color handling consistent across every configurable message path.

This release also improves death attribution, removes several cross-world and null-configuration edge cases, reduces per-death configuration work through caching, and updates the plugin for Minecraft 26.3 with Java 21 while retaining compatibility with Minecraft 1.21.x.

Server owners can upgrade without rewriting their configuration. Existing `config.yml` and `messages.yml` files remain valid; the new broadcast message keys have built-in defaults and can be added optionally.

---

## Release Statistics

| Category | Count |
|---|---:|
| Critical fixes | 6 |
| Bug fixes | 22 |
| Performance improvements | 14 |
| Configuration and message additions | 6 |
| Compatibility and build | 5 |
| Quality-of-life improvements | 10 |
| Behavior changes | 7 |
| **Total documented changes** | **70** |

---

# Critical Fixes

## Hex Color Rendering

- Fixed `<#RRGGBB>` and `&#RRGGBB` colors not rendering in death messages, titles, subtitles, action bars, command feedback, help text, test summaries, broadcast feedback, reload messages, configuration validation, update notices, and respawn messages. Both hex syntaxes are now translated consistently instead of being shown as literal text. Raw color sequences also no longer appear in console enable/disable logs.

## Action Bar Colors

- Fixed action bar text not interpreting legacy `§` color codes correctly. Action bar messages now use legacy text parsing, so colors and formatting render as intended.

## Death Attribution and World Safety

- Fixed a cross-world distance error that could occur when the victim and the responsible entity were in different worlds. Distance is now calculated only when both entities share the same world.
- Fixed stale killer attribution. Damage information used to credit a killer now expires after 15 seconds, preventing an old hit from claiming an unrelated later death.
- Fixed projectile fallback attribution. When tracked damage data is unavailable, projectile deaths now resolve the shooter instead of crediting the projectile itself.

## Platform Compatibility

- Added Minecraft 26.3 support with Spigot API 26.3 and Java 21 while retaining Minecraft 1.21.x compatibility through an `api-version: '1.21'` declaration.

---

# Bug Fixes

## Commands and Message Formatting

- Fixed hex colors in `/cdm`, `/cdmhelp`, `/cdm test`, `/cdmbroadcast`, `/cdm reload`, and `/cdm config` feedback.
- Fixed hex colors in update notifications and respawn messages.
- Fixed `/cdmhelp` formatting command descriptions twice, which could alter literal `&` sequences or produce unexpected colors.
- Fixed blank or missing values in `messages.yml` causing `NullPointerException`s in commands.
- Fixed the invalid-cause response from the message formatter bypassing the color pipeline.
- Fixed the broadcast mode overview using hardcoded text instead of configurable `messages.yml` entries.

## Broadcast Handling

- Fixed negative broadcast radii being interpreted as a sound or particle name instead of a radius value.
- Fixed invalid broadcast radii not being reported clearly. The invalid value and the fallback radius are now shown.
- Fixed broadcast mode and radius resolution reading configuration through multiple inconsistent paths; both now use one effective resolution path and refresh on reload.

## Messages, Placeholders, and Integrations

- Fixed conditional death messages being rebuilt from YAML on every death. They are now cached by group and damage cause.
- Fixed a blank `message-not-found` entry in `messages.yml` causing a `NullPointerException` during fallback message selection.
- Fixed unnecessary placeholder work by resolving only the placeholders that actually appear in the selected message.
- Fixed PlaceholderAPI availability being checked repeatedly. Availability is now cached, while still allowing recovery if PlaceholderAPI is enabled later.
- Fixed a broken PlaceholderAPI installation repeatedly attempting placeholder expansion; a missing class is now handled once and the death flow continues normally.
- Fixed Essentials nickname lookups repeating unnecessary plugin-manager checks; the hook now revalidates itself when needed.
- Fixed the missing-economy warning being printed on every death. It is now shown once per server session.

## Validation and Safety

- Fixed negative epic particle counts being passed to the server; they are now clamped to a safe value.
- Fixed negative group sound volumes; they are now clamped to zero.
- Fixed the update checker retaining state between checks; it now resets before each check.
- Fixed the test command creating multiple death contexts for a single test instead of reusing one.
- Fixed tab completion suggestions not being filtered by the typed prefix and using locale-sensitive casing.
- Fixed console enable/disable messages containing raw color codes instead of clean, readable text.

---

# Performance Improvements

- Conditional death messages are cached per group and damage cause, eliminating repeated YAML section traversal during the death flow.
- Broadcast mode and radius settings are cached and rebuilt only when the configuration is reloaded.
- Placeholder resolution skips lookups for placeholders that are not present in the selected message.
- PlaceholderAPI availability is cached after the first successful check, avoiding a plugin lookup for every message.
- Essentials integration avoids repeated plugin-manager lookups during nickname resolution.
- The darken-screen particle ring offsets are precomputed once instead of recalculating trigonometry every tick.
- Darken-screen recipients are resolved once per tick instead of once for each of the 12 particle points.
- `/cdm test` reuses a single `DeathContext` and cached effect managers.
- `/cdmbroadcast` caches its `BroadcastManager`, `SoundManager`, and `ParticleManager` instances.
- Message selection uses `Random.nextDouble()` instead of the shared `Math.random()` implementation.
- Tab completion filters candidates by prefix before constructing the suggestion list.
- Case conversions use `Locale.ROOT`, preventing locale-dependent behavior and unnecessary work.
- Victim weapon resolution reads `ItemMeta` once per lookup instead of twice.
- Particle counts, sound volumes, radii, durations, and economy costs are clamped early, preventing invalid or unexpectedly expensive effect calls.

---

# Configuration and Message Additions

The following new keys are available under `system-messages` in `messages.yml`:

- `broadcast-current-sound-radius` — current sound broadcast radius display.
- `broadcast-current-particle-radius` — current particle broadcast radius display.
- `broadcast-current-message-radius` — current message radius display.
- `broadcast-current-message-world` — current world message mode display.
- `broadcast-current-message-victim` — current victim-only message mode display.
- `broadcast-invalid-radius` — feedback shown when an invalid broadcast radius is entered.

These keys are optional. If they are missing from an existing `messages.yml`, the plugin uses built-in defaults.

---

# Compatibility and Build

- Updated the build to `spigot-api 26.3-R0.1-SNAPSHOT`.
- Updated `plugin.yml` to declare `api-version: '1.21'`, keeping the plugin loadable on both 1.21.x and 26.3.
- Updated the compilation target to Java 21.
- Updated the plugin version metadata to 1.6 in `pom.xml`, `plugin.yml`, `config.yml`, and `messages.yml`.
- Verified a clean Maven build and packaged `customdeathmessages-1.6.jar`. The resulting JAR was load-tested on Paper 1.21.11 and Paper 26.3; the plugin enabled successfully on both.

Compatible with Spigot, Paper, and Purpur servers running Minecraft 1.21.x through 26.3. Java 21 or newer is required. PlaceholderAPI, Vault, and Essentials remain optional integrations.

---

# Quality-of-Life Improvements

- `/cdmbroadcast modes` now uses configurable `messages.yml` entries for its current-settings output.
- Invalid radius feedback now shows both the rejected value and the fallback value.
- Broadcast command help and examples are now sourced from the message system, making them easier to translate and customize.
- Help descriptions containing color codes now render exactly as written.
- Missing or blank message entries fall back to readable built-in defaults instead of failing silently.
- Console logs are clean and readable without raw `&` or hex color sequences.
- Vault users receive one clear warning when an economy cost is configured but no economy provider is available.
- Tab completion responds faster and only suggests entries matching the current input.
- `/cdm test` output follows the same formatting and context path as real death messages.
- Update notifications and broadcast feedback now match the plugin's normal color formatting.

---

# Behavior Changes

- `<#RRGGBB>` is now fully supported in addition to `&#RRGGBB`.
- `legacy-color-support: false` translates hex colors; `legacy-color-support: true` removes hex colors for older clients. The setting keeps its original meaning and is now applied consistently.
- Negative broadcast radii are clamped to `0` instead of being interpreted as a sound or particle name.
- Broadcast mode and radius changes are picked up after `/cdm reload`.
- Killer attribution no longer uses damage older than 15 seconds.
- Blank `messages.yml` entries now fall back to defaults rather than causing errors.
- Version 1.6 supports Minecraft 1.21.x through 26.3 and requires Java 21. Minecraft versions below 1.21 are not supported by this build.

---

# Upgrade Notes

1. Back up your current `plugins/CustomDeathMessages/config.yml` and `messages.yml`.
2. Replace the old plugin JAR with `customdeathmessages-1.6.jar`.
3. Restart the server. A full restart is recommended after a major platform update.
4. Your existing configuration files will not be overwritten.
5. If you want to customize the new broadcast messages, add the six keys listed above to your existing `messages.yml`.
6. Keep `legacy-color-support: false` on Minecraft 1.21+ to enable hex colors. Set it to `true` only if you intentionally need hex codes removed for older clients.
7. Verify the update with `/cdm reload`, `/cdm test FALL`, and `/cdmbroadcast modes`.

---

# Summary of Major 1.6 Outcomes

- Hex colors now render correctly across all configurable message paths.
- Action bar colors now render correctly.
- Cross-world distance errors no longer interrupt death processing.
- Killer attribution is more accurate and no longer uses stale damage.
- Projectile fallback attribution now identifies the shooter.
- Conditional messages, broadcast settings, placeholders, and particle recipients are handled with less repeated work.
- Commands and configuration messages are more resilient to missing or blank values.
- Broadcast feedback is more configurable and easier to translate.
- The plugin is ready for Minecraft 1.21.x through 26.3 and Java 21.
