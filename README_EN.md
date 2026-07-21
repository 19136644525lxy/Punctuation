# Punctuation - Marker Mod

A Minecraft Fabric mod that allows players to mark positions using the middle mouse button and teleport to them, with API support for addon mods.

## Features

### Core Features
- **Marker Function**: Mark any block position with the middle mouse button
- **Teleport**: Press P to teleport to the marked position
- **Mode Switch**: Press Left Alt to switch between Marker Mode and Pick Block Mode
- **Visual Effects**: Highlight border and custom icon at the marker position
- **Config UI**: Customizable border color schemes (14 colors available)
- **Gradient Support**: Gradient color effects for borders

### API Features
- **MarkerEvent**: Marker event callback for addon mods to listen to marker actions
- **Marker Info Query**: Get marker position, entity ID, entity position, marker type, item stack, etc.
- **Network Sync**: Client-server marker data synchronization

## Controls

| Key | Function |
|-----|----------|
| Middle Mouse Button | Mark position in Marker Mode |
| P | Teleport to marker |
| Left Alt | Toggle Marker/Pick Block Mode |

## Configuration

Open the config screen via ModMenu to adjust:
- Show highlight border
- Border color scheme
- Use gradient color
- Marker broadcast duration (1-10 seconds)

## Addon Mod

### Alone Host (ah)

An addon mod that utilizes the main mod's marker functionality to friendly and control hostile mobs.

#### Features
- **Mob Befriending**: Right-click hostile mobs with Mob Box to make them friendly
- **Mob Storage**: Right-click befriended mobs to store them in the Mob Box (capacity 10)
- **Mob Release**: Shift+right-click blocks to release mobs from the Mob Box
- **AI Behavior Modes**:
  - Idle Mode: Wait for commands (default)
  - Follow Mode: Follow player, attack hostile mobs within 64 blocks, auto-teleport when out of range
  - Patrol Mode: Go to marker position and patrol, attack hostile mobs within 64 blocks
- **Behavior Switch**: Right-click befriended mobs with empty hand to cycle through behavior modes
- **Buff Effects**: Befriended mobs get 1000 HP, Strength IV, Resistance; Undead mobs additionally get Strength VI
- **Undead Protection**: Befriended undead mobs won't burn in sunlight

#### Controls
| Action | Function |
|--------|----------|
| Mob Box + Right-click hostile mob | Befriend the mob |
| Mob Box + Right-click befriended mob | Store in Mob Box |
| Shift + Mob Box + Right-click block | Release mobs from Mob Box |
| Empty hand + Right-click befriended mob | Switch behavior mode |

## Dependencies

- Fabric API
- Cloth Config API
- ModMenu

## Supported Version

- Minecraft 1.20.1

## License

MIT License