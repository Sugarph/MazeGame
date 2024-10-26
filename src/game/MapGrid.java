package game;

import java.util.Random;

public class MapGrid {
    final int width;
    final int height;
    public final int tileSize;
    int[] mapData;

    public MapGrid(int width, int height, int tileSize, int[] mapData) {
        this.width = width;
        this.height = height;
        this.tileSize = tileSize;
        this.mapData = mapData;
        shuffleMap();
    }


    public boolean isWall(int x, int y) {
        if (x >= 0 && x < width && y >= 0 && y < height) {
            return (mapData[y * width + x] == 1) || (mapData[y * width + x] == 6);
        }
        return false;
    }

    public boolean WallAtTile(int pixelX, int pixelY) {
        int tileX = pixelX / tileSize; // Convert pixel to tile coordinate
        int tileY = pixelY / tileSize; // Convert pixel to tile coordinate
        return isWall(tileX, tileY); // Use the original isWall method with tile coordinates
    }

    public int getTileValue(int tileX, int tileY) {
        // Check if tileX and tileY are within the valid grid range
        if (tileX < 0 || tileX >= width || tileY < 0 || tileY >= height) {
            return 1;
        }

        int index = tileY * width + tileX;
        return mapData[index];
    }
    public void shuffleMap() {
        int wallCount = 0;
        for (int value : mapData) {
            if (value == 1 || value == 6) wallCount++; // Count walls (both normal and bloodied)
        }

        int bloodiedWallCount = (int) (wallCount * 0.15); // Target 15% of walls
        int changedWalls = 0;
        Random random = new Random();

        while (changedWalls < bloodiedWallCount) {
            int x = random.nextInt(width);
            int y = random.nextInt(height);
            int index = y * width + x;

            // Toggle between normal (1) and bloodied (6) walls
            if (mapData[index] == 1) {        // Convert to bloodied wall
                mapData[index] = 6;
                changedWalls++;
            } else if (mapData[index] == 6) { // Convert back to normal wall
                mapData[index] = 1;
                changedWalls++;
            }
        }
    }

}
