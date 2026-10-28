# 1.7.0 — Multi-version release

CustomDeathMessages now ships as **two JARs built from the same code base**, so the plugin runs on every Minecraft version from 1.8 to 26.3:

| Build | Minecraft | Java | File |
|---|---|---|---|
| **Modern** | 1.21 – 26.3 | 21+ | `customdeathmessages-1.7.0-modern.jar` |
| **Legacy** | 1.8 – 1.20.x | 8+ | `customdeathmessages-1.7.0-legacy.jar` |

Both builds share the same configuration files, commands, placeholders and features. Configuration files from 1.6.x keep working unchanged.

## Added

- **Legacy build for Minecraft 1.8 – 1.20.x.** Declares no `api-version` and compiles for Java 8, so it loads on old servers without any bridge plugin.
- **Version compatibility layer.** Sounds, particles, titles, action bars, biomes, potion effects, item-in-hand checks and hex colours are resolved at runtime instead of being hard-coded to one API version.
- **Cross-version sound aliases.** Renamed sounds are translated in both directions, including chains such as `ENTITY_LIGHTNING_BOLT_THUNDER` → `ENTITY_LIGHTNING_THUNDER` → `AMBIENCE_THUNDER`. Configs may use any known name.
- **Cross-version particle aliases.** The 1.17 particle renames are translated in both directions (`EXPLOSION` ↔ `EXPLOSION_LARGE`, `EXPLOSION_EMITTER` ↔ `EXPLOSION_HUGE`, `SMOKE` ↔ `SMOKE_NORMAL`, and so on). On 1.8 servers, particles without a `Particle` enum are mapped to the closest `Effect`.
- **Hex colour fallback.** On servers without `ChatColor#of` (below 1.16), hex colours are approximated with the closest legacy colour unless `legacy-color-support: true` removes them.
- **1.8 action bars and titles.** Titles use the 1.8 two-argument method when needed; action bars use the Bungee chat API or the 1.8 NMS packet.

## Fixed

- Epic thunder sound rejected on Minecraft 1.9 – 1.12 (`ENTITY_LIGHTNING_BOLT_THUNDER` does not exist there).
- Epic and death particles rejected on 1.16.5 because 1.17 names were validated literally.
- `/cdmreload` aborted even when validation passed, because error detection relied on a mis-encoded character. It now checks the report colour prefix.
- `/cdmconfig validate` counted issues by emoji instead of colour codes, which could report the wrong total.
- `/cdmbroadcast particle` printed a raw enum name instead of the configured particle name.
- Default particles on 1.8 no longer depend on `Effect.CLOUD`, which is missing from newer APIs.

## Tested

Real server runs with the final JARs:

| Minecraft | Build | Java | Result |
|---|---|---|---|
| 1.8.8 | Legacy | 8 | Plugin enabled, validation clean, real death, epic effects, respawn |
| 1.12.2 | Legacy | 8 | Plugin enabled, validation clean, real epic death, respawn |
| 1.16.5 | Legacy | 8 | Plugin enabled, validation clean, real epic death, respawn |
| 1.20.6 | Legacy | 25 | Plugin enabled, validation clean, real epic death, respawn |
| 1.21.11 | Modern | 25 | Plugin enabled, validation clean, real death, respawn |
| 26.3 | Modern | 25 | Plugin enabled, validation clean |

Servers: Paper. No errors, warnings or invalid sounds/particles in any run.

## Notes

- Use the **modern** JAR on 1.21 and newer, and the **legacy** JAR on 1.20.x and older. Installing both at once causes command conflicts.
- The legacy JAR targets the 1.8 API, so it also loads on intermediate versions (1.9 – 1.15). Effects that do not exist in a given version fall back to the closest equivalent.
- On 1.8 – 1.15 clients, hex colours are approximated with legacy colours; true hex requires a 1.16+ client and server.
- No configuration keys were added or renamed in this release.
