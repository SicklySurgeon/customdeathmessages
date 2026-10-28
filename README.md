# CustomDeathMessages

![Modrinth Version](https://img.shields.io/modrinth/v/customdeathmessages)
![Modrinth Downloads](https://img.shields.io/modrinth/dt/customdeathmessages)
![Minecraft versions](https://img.shields.io/badge/Minecraft-1.8%E2%80%9326.3-brightgreen)

Custom, cinematic death messages for Minecraft servers running **Spigot, Paper or Purpur** on **Minecraft 1.8 – 26.3**. Two builds share the same code and configuration:

| Build | Minecraft | Java | JAR |
|---|---|---|---|
| **Modern** | 1.21 – 26.3 | 21+ | `customdeathmessages-1.7.0-modern.jar` |
| **Legacy** | 1.8 – 1.20.x | 8+ | `customdeathmessages-1.7.0-legacy.jar` |

Links:

- Modrinth: <https://modrinth.com/plugin/customdeathmessages>
- Documentation: [GitHub Wiki](https://github.com/SicklySurgeon/customdeathmessages/wiki)
- Source code: this repository.

## Features

- **Per-cause and per-group messages.** Death messages are configurable per damage cause, per message group (`default`, `vip`, `admin`) and per world. Groups are selected by permission or directly through `messages.yml`.
- **True hex colors.** Supports both `&#RRGGBB` and `<#RRGGBB>` syntax (requires Minecraft 1.16+ clients). On servers with `legacy-color-support: true`, hex codes are removed instead of translated so pre-1.16 clients stay readable.
- **Epic deaths.** Configurable chance that triggers extra title, action bar, custom sound and particle burst effects when a player dies.
- **Unified effect broadcasting.** Every death effect (sound, particles, title, action bar and darken-screen) has its own broadcast mode: `GLOBAL`, `WORLD`, `RADIUS` or `VICTIM_ONLY`, with cached radius resolution.
- **Accurate attribution.** Killer detection uses recent damage only (15-second window), falls back to the shooter for projectiles and avoids cross-world errors.
- **Rich placeholders.** Built-in placeholders for victim, killer, weapon, biome, effects, distance, coordinates and more. PlaceholderAPI, Vault (optional death-message cost) and Essentials (nicknames) are automatic optional integrations.
- **Respawn and time messages.** Configurable respawn broadcast and `{time}` stamp via `time-format`.

## Installation

1. Download the build that matches your server from [Modrinth](https://modrinth.com/plugin/customdeathmessages):
   - **Modern** for Minecraft 1.21 – 26.3 (Java 21+).
   - **Legacy** for Minecraft 1.8 – 1.20.x (Java 8+).
2. Drop the JAR into your `plugins/` folder. Do not install both builds at once.
3. Restart the server.
4. Edit `plugins/CustomDeathMessages/config.yml` and `messages.yml` to taste, then run `/cdmreload`.

Existing configuration files are never overwritten by updates.

## Compatibility

| Item | Modern build | Legacy build |
|---|---|---|
| Minecraft | 1.21 – 26.3 | 1.8 – 1.20.x |
| Java | 21+ | 8+ |
| `api-version` | `1.21` | not declared |
| Hex colors | Full support | Full on 1.16+ servers; approximated below 1.16 |
| Particles | `Particle` registry | `Particle` when available, `Effect` on 1.8 |

Sounds and particles are resolved at runtime, so the same configuration works on both builds. Renamed sounds and particles (for example `EXPLOSION_LARGE` on 1.16 and `EXPLOSION` on 1.17+) are translated automatically.

## Building from source

Requirements: JDK 21+ (the legacy build is compiled with `--release 8`) and Maven 3.9+.

```bash
mvn clean package            # modern build
mvn -Plegacy clean package   # legacy build
```

Outputs:

```text
target/customdeathmessages-1.7.0-modern.jar
target/customdeathmessages-1.7.0-legacy.jar
```

## Commands

| Command | Description | Permission |
|---|---|---|
| `/cdmhelp [command]` | Shows plugin help | `cdm.use` |
| `/cdm` | Root command (`/cdm [test\|broadcast\|help\|config\|reload]`) | `cdm.use` |
| `/cdm test [type]` | Simulates a death message | `cdm.test` (`cdm.test.others` on other players) |
| `/cdmbroadcast <sound\|particle\|message\|test\|modes\|global>` | Tests broadcast modes for death effects | `cdm.broadcast` |
| `/cdmreload` | Reloads the configuration | `cdm.reload` |
| `/cdmconfig validate` | Validates the current configuration | `cdm.reload` |

Additional permissions:

- `cdm.use` (default: everyone) — basic access.
- `cdm.bypass` — player stops receiving custom death messages.
- `cdm.message.vip` / `cdm.message.admin` — forces use of the VIP/Admin message group.
- `cdm.admin` — receives plugin update notifications.

## Placeholders

All placeholders are resolved only when they actually appear in the selected message, so unused ones cost nothing:

| Placeholder | Meaning |
|---|---|
| `{victim}` | Victim, using Essentials nickname when available |
| `{victim_username}` | Raw victim username |
| `{killer_name}` / `{killer_username}` / `{killer_displayname}` | Killer identity variants |
| `{killer_source}` | Source of the killing entity |
| `{cause}` | Vanilla damage cause name |
| `{world}` | World of the death |
| `{distance}` | Distance between victim and killer (`%.1f`, blocks) |
| `{biome}` | Death biome as a readable name |
| `{victim_weapon}` | Weapon or item picked up from the victim's main hand |
| `{victim_effects}` | Active potion effects on the victim |
| `{time}` | Death time formatted via `time-format` |
| `{x}` / `{y}` / `{z}` | Death coordinates |
| `{cost}` | Current death message cost (economy) |

## Configuration overview

- `config.yml` tells the plugin **what to show**: message targets (`message-to-victim`, `message-to-world`, `message-to-radius`), sounds, particles, titles, action bars, darken-screen, unified `effects-broadcast` modes, epic-death chance, economy cost and per-world overrides.
- `messages.yml` tells it **what to say**: per-group message trees, per-cause messages, conditional messages, epic-effects entries and all broadcast feedback text.

Example hex usage inside any message:

```yaml
death-title: "<#FF5555>YOU DIED &8« &7{cause}"
```

Automatic updates, PlaceholderAPI placeholders, Essentials nicknames and Vault costs stay optional and are detected at runtime when the plugins are present.

## Documentation

The [GitHub Wiki](https://github.com/SicklySurgeon/customdeathmessages/wiki) contains the full reference: installation, commands and permissions, the complete `config.yml` and `messages.yml` guides, placeholders, building from source and troubleshooting.

## Changelog

Release history lives in [`CHANGELOG-1.7.0.md`](CHANGELOG-1.7.0.md), [`CHANGELOG-1.6.1.md`](CHANGELOG-1.6.1.md) and [`CHANGELOG-1.6.md`](CHANGELOG-1.6.md). Version 1.7.0 adds the legacy build for Minecraft 1.8 – 1.20.x and a runtime compatibility layer for sounds, particles, titles, action bars and colors.

## License

Released under the [MIT License](LICENSE). © 2026 SicklySurgeon.
