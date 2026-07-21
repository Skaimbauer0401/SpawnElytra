# Spawn Elytra

Allows players to start flying within a configurable start area.
Pressing spacebar whilst flying boosts the player in the direction they are looking.
The default center is the world spawn.

## Features
- Start flying in start area by double-tapping spacebar
- Boost in the direction you're looking
- Invulnerability in start area
- Landing protection for 20 ticks after landing
- Configurable via commands

## Commands
All commands require OP permission level 2.

- `/spawnelytra radius <radius>` - Set spawn radius (1-1000)
- `/spawnelytra booststrength <strength>` - Set boost strength (0.01-100.0)
- `/spawnelytra center <x> <y> <z>` - Set center to specific coordinates
- `/spawnelytra default` - Reset all settings to default
- `/spawnelytra info` - Show current settings

## Default Values
- Spawn Radius: 30 blocks
- Boost Strength: 2.5
- Center: World spawn

## Configuration
Settings are automatically saved to `config/spawnelytra.json` and persist across server restarts.

## Supported Minecraft versions

Each Minecraft version line has its own release. Download the matching jar
from the [releases page](https://github.com/Skaimbauer0401/SpawnElytra/releases).

| Minecraft | Mod version | Fabric Loader |
| --- | --- | --- |
| 26.2 | 1.2.0 | 0.19.3+ |
| 26.1, 26.1.1, 26.1.2 | 1.1.0 | 0.19.3+ |
| 1.21.11 | 1.0.3 | 0.18.4+ |
| 1.21.9, 1.21.10 | 1.0.2 | 0.17.0+ |

This is a **server-side** mod. It does not need to be installed on clients.

## Installation
1. Install Fabric Loader and Fabric API for your Minecraft version.
2. Drop the matching jar from the table above into the server's `mods` folder.
3. Restart the server.

## Building from source

Requires JDK 25 for Minecraft 26.1 and later (JDK 21 for the 1.21.x releases).

```
./gradlew build
```

The jar is written to `build/libs/`.

## How to Use
1. Stand within the spawn radius
2. Jump and double-tap spacebar to start gliding
3. Press spacebar again while flying to boost in the direction you're looking
4. Land safely with 20 ticks of invulnerability protection