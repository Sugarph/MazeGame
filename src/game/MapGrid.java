package game;

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
    }

    public boolean isWall(int x, int y) {
        if (x >= 0 && x < width && y >= 0 && y < height) {
            return mapData[y * width + x] == 1;
        }
        return false;
    }
    public boolean WallAtTile(int pixelX, int pixelY) {
        int tileX = pixelX / tileSize; // Convert pixel to tile coordinate
        int tileY = pixelY / tileSize; // Convert pixel to tile coordinate
        return isWall(tileX, tileY); // Use the original isWall method with tile coordinates
    }
}
