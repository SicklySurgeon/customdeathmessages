# CustomDeathMessages

![Modrinth Version](https://img.shields.io/modrinth/v/customdeathmessages)
![Modrinth Downloads](https://img.shields.io/modrinth/dt/customdeathmessages)
![Minecraft versions](https://img.shields.io/badge/Minecraft-1.21.x%E2%80%9326.3-brightgreen)

Custom, cinematic death messages for Minecraft servers running **Spigot, Paper or Purpur** on **Minecraft 1.21.x – 26.3** (Java 21 or newer).

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

1. Download the latest JAR from [Modrinth](https://modrinth.com/plugin/customdeathmessages).
2. Drop it into your `plugins/` folder (requires **Java 21+**).
3. Restart the server.
4. Edit `plugins/CustomDeathMessages/config.yml` and `messages.yml` to taste, then run `/cdmreload`.

Existing configuration files are never overwritten by updates.

## Building from source

Requirements: JDK 21+ and Maven 3.9+.

```bash
mvn clean package
```

The ready-to-install JAR is written to `target/customdeathmessages-<version>.jar`. The build compiles against Spigot API `26.3-R0.1-SNAPSHOT` and sets `api-version: 1.21`, so the same JAR loads on 1.21.x through 26.3.

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

Release history lives in [`CHANGELOG-1.6.md`](CHANGELOG-1.6.md) and [`CHANGELOG-1.6.1.md`](CHANGELOG-1.6.1.md). Version 1.6.1 is a compatibility hotfix: it restores `api-version: 1.21` so the same 1.6 code base loads on Minecraft 1.21.x and 26.3.

## License

Released under the [MIT License](LICENSE). © 2026 SicklySurgeon.
