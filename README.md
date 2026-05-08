# DDA Voxel Engine

A voxel-based 3D engine written in pure Java using DDA (Digital Differential Analyzer) raycasting for rendering.

## Features

- **DDA Raycasting Renderer** - Efficient voxel traversal using DDA algorithm with parallel row rendering
- **Procedural Terrain** - World generation using OpenSimplex2 noise with multiple octaves
- **AABB Collision** - Axis-aligned bounding box collision detection for player physics
- **Block Interaction** - Place and destroy blocks in the world
- **First-Person Controls** - Mouse look and WASD movement with gravity and jumping

## Controls

| Key | Action |
|-----|--------|
| W | Move forward |
| S | Move backward |
| A | Strafe left |
| D | Strafe right |
| Space | Jump |
| Left Click | Destroy block |
| Right Click | Place block |
| Mouse | Look around |

## Version History

- **V1.3** - AABB collision detection
  - V1.3.1 - Function argument type fix
  - V1.3.2 - Increased world size (32 -> 128), random spawn position
  - V1.3.3 - Refactor file structure and separate WorldGenerator, Player, Renderer
  - V1.3.4 - Clamp player epsilon between -90 to 90
  - V1.3.5 - Block types
  - V1.3.6 - Prevent block placement if within player
- **V1.2** - Basic procedural world generation
  - V1.2.1 - Optimizations for improved FPS at lower FOV
- **V1.1** - Block placing and breaking
- **V1.0** - Working parallel raycaster

## Requirements

- Java 21+ (`Math.clamp`)

## Running

```bash
javac -d out src/game/*.java
java -cp out game.Projection
```
