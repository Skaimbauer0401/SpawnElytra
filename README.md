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

- `/spawnelytra spawnradius <radius>` - Set spawn radius (1-1000)
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

## Installation
Requires Fabric Loader and Fabric API

## How to Use
1. Stand within the spawn radius
2. Jump and double-tap spacebar to start gliding
3. Press spacebar again while flying to boost in the direction you're looking
4. Land safely with 20 ticks of invulnerability protection
```