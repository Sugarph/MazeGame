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
        int tileX = pixelX / tileSize;
        int tileY = pixelY / tileSize;
        return isWall(tileX, tileY);
    }

    public int getTileValue(int tileX, int tileY) {
        if (tileX < 0 || tileX >= width || tileY < 0 || tileY >= height) {
            return 1;
        }

        int index = tileY * width + tileX;
        return mapData[index];
    }

    public void shuffleMap() {
        int wallCount = 0;
        for (int value : mapData) {
            if (value == 1 || value == 6) wallCount++;
        }

        int bloodiedWallCount = (int) (wallCount * 0.15);
        int changedWalls = 0;
        Random random = new Random();

        while (changedWalls < bloodiedWallCount) {
            int x = random.nextInt(width);
            int y = random.nextInt(height);
            int index = y * width + x;

            //Toggle between normal and bloody
            if (mapData[index] == 1) {
                mapData[index] = 6;
                changedWalls++;
            } else if (mapData[index] == 6) {
                mapData[index] = 1;
                changedWalls++;
            }
        }
    }

}
