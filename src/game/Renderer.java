package game;

import java.awt.*;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import javax.swing.*;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Renderer {
    private final Player player;
    private final Game mainGame;
    private final MapGrid map;
    private final int tileSize;
    private double fogFactor;
    private boolean flashlightOn = true;
    public List<Shadow> shadows = new ArrayList<>();
    private final SoundManager soundManager;
    private final double[] depthArray = new double[240];
    private boolean bloodVisible = false, showText = false, overlayChanged = false, applyFlashlight = true, endFadePlayed = false, fadeInProgress = false;
    private final Random random;
    private double lastJitterTime = 0;
    private int jitterX = 0, jitterY = 0, encounterCount = 0, fadeAlpha = 0;
    private BufferedImage wallTexture, floorTexture, bloodWallTexture, dontMoveText, currentOverlay, finishTexture, stayStillText, notThisTime, run;

    public Renderer(Player player, Game mainGame, MapGrid map) {
        this.mainGame = mainGame;
        this.soundManager = new SoundManager();
        this.player = player;
        this.map = map;
        this.tileSize = map.tileSize;
        for (int y = 0; y < map.height; y++) {
            for (int x = 0; x < map.width; x++) {
                if (map.getTileValue(x, y) == 5) {
                    shadows.add(new Shadow(x * tileSize + tileSize / 2, y * tileSize + tileSize / 2, this));
                }
            }
        }
        random = new Random();
        loadTextures();
        //startHallucination(2000 + random.nextInt(3000), false);

    }

    //I should make texture manager ;-;
    private void loadTextures() {
        try {
            wallTexture = ImageIO.read(new File("sprites/MainWall.png"));
            bloodWallTexture = ImageIO.read(new File("sprites/BloodyWall.png"));
            floorTexture = ImageIO.read(new File("sprites/MainWall.png"));
            finishTexture = ImageIO.read(new File("sprites/PentagramWall.png"));
            dontMoveText = ImageIO.read(new File("sprites/DontMove.png"));
            stayStillText = ImageIO.read(new File("sprites/StayStill.png"));
            notThisTime = ImageIO.read(new File("sprites/NotThisTimeOverlay.png"));
            run = ImageIO.read(new File("sprites/Run.png"));
            currentOverlay = dontMoveText;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void rayCasting(Graphics2D g) {
        for (int rayIndex = 0; rayIndex < 240; rayIndex++) {  // Cast 240 rays
            double rayAngle = (player.angle + 30 - (double) rayIndex / 4) % 360;
            if (rayAngle < 0) rayAngle += 360;
            double wallDistance = castRay(rayAngle, g, rayIndex);
            depthArray[rayIndex] = wallDistance;
        }
        soundManager.playSound("heartbeat", true, false);
        drawFadeEffect(g);
        drawShadows(g);
        flashlightEffect(g, applyFlashlight);
        drawOverlay(g);
        checkEndCondition();
    }

    //Ray casting method using DDA
    public double castRay(double rayAngle, Graphics2D g, int rayIndex) {
        double closestDistance, verticalDistance = 1000, horizontalDistance = 1000;
        double verticalHitX = 0, verticalHitY = 0, horizontalHitX = 0, horizontalHitY = 0;
        int depthOfField = 0;
        double tangent = Math.tan(Math.toRadians(rayAngle));
        boolean isVerticalHit;
        double hitX, hitY;
        int mapX, mapY;

        // Check Vertical
        double rayX = 0, rayY = 0, stepX = 0, stepY = 0;
        double rayDirX = Math.cos(Math.toRadians(rayAngle));
        double rayDirY = Math.sin(Math.toRadians(rayAngle));

        if (rayDirX > 0) {
            rayX = Math.floor(player.x / tileSize) * tileSize + tileSize;
            rayY = (player.x - rayX) * tangent + player.y;
            stepX = tileSize;
            stepY = -stepX * tangent;
        } else if (rayDirX < 0) {
            rayX = Math.floor(player.x / tileSize) * tileSize - 0.0001f;
            rayY = (player.x - rayX) * tangent + player.y;
            stepX = -tileSize;
            stepY = -stepX * tangent;
        }
        //limit at 8 tile
        while (depthOfField < 8) {
            mapX = (int) (rayX) / tileSize;
            mapY = (int) (rayY) / tileSize;
            if (map.isWall(mapX, mapY)) {
                verticalHitX = rayX;
                verticalHitY = rayY;
                verticalDistance = Math.sqrt(Math.pow(rayX - player.x, 2) + Math.pow(rayY - player.y, 2));
                break;
            }
            rayX += stepX;
            rayY += stepY;
            depthOfField++;
        }

        //Check Horizontal
        depthOfField = 0;
        tangent = 1.0f / tangent;
        if (rayDirY > 0) {
            rayY = Math.floor(player.y / tileSize) * tileSize - 0.0001f;
            rayX = (player.y - rayY) * tangent + player.x;
            stepY = -tileSize;
            stepX = -stepY * tangent;
        } else if (rayDirY < 0) {
            rayY = Math.floor(player.y / tileSize) * tileSize + tileSize;
            rayX = (player.y - rayY) * tangent + player.x;
            stepY = tileSize;
            stepX = -stepY * tangent;
        }

        while (depthOfField < 8) {
            mapX = (int) (rayX) / tileSize;
            mapY = (int) (rayY) / tileSize;
            if (map.isWall(mapX, mapY)) {
                horizontalHitX = rayX;
                horizontalHitY = rayY;
                horizontalDistance = Math.sqrt(Math.pow(rayX - player.x, 2) + Math.pow(rayY - player.y, 2));
                break;
            }
            rayX += stepX;
            rayY += stepY;
            depthOfField++;
        }
        //Check which ray hit first
        if (verticalDistance < horizontalDistance) {
            isVerticalHit = true;
            closestDistance = verticalDistance;
            hitX = verticalHitX;
            hitY = verticalHitY;
        } else {
            isVerticalHit = false;
            closestDistance = horizontalDistance;
            hitX = horizontalHitX;
            hitY = horizontalHitY;
        }
        int wallTileX = (int) hitX / tileSize;
        int wallTileY = (int) hitY / tileSize;
        BufferedImage texture = (map.getTileValue(wallTileX, wallTileY) == 6 && bloodVisible) ? bloodWallTexture : wallTexture;
        //Fix fisheye effect
        double angleDifference = fixAngle(player.angle - rayAngle);
        closestDistance *= Math.cos(Math.toRadians(angleDifference));

        int textureWidth = texture.getWidth();
        int textureHeight = texture.getHeight();
        double wallHeight = ((tileSize * 640) / closestDistance) * 1.6;
        double textureYStep = textureHeight / wallHeight;
        double textureYOff = 0;

        if (wallHeight > 640 * 1.6) {
            textureYOff = (wallHeight - 640 * 1.6) / 2;
            wallHeight = 640 * 1.6;
        }
        double wallOffset = (320 - wallHeight / 2) + player.pitch;

        g.fillRect(rayIndex * 4, (int) wallOffset, 4, (int) wallHeight);
        double textureX;
        double textureScaleFactor = (double) textureWidth / tileSize;
        if (isVerticalHit) {
            textureX = (int) ((hitY % tileSize) * textureScaleFactor) % textureWidth;
        } else {
            textureX = (int) ((hitX % tileSize) * textureScaleFactor) % textureWidth;
        }

        drawWall(g, textureX, wallHeight, wallOffset, textureYStep, (int) textureYOff, rayIndex, closestDistance, texture);
        drawFloor(g, rayAngle, rayIndex, (int) wallOffset, (int) wallHeight);
        drawCeiling(g, rayAngle, rayIndex, (int) wallOffset);
        return closestDistance;
    }

    //Using trigonometry for check where to draw the floor and ceiling
    private void drawFloor(Graphics2D g, double rayAngle, int rayIndex, int wallOffset, int wallHeight) {
        int textureWidth = floorTexture.getWidth();
        int textureHeight = floorTexture.getHeight();
        double textureScaleFactor = (double) textureWidth / tileSize;
        double cameraHeight = (640 / 2.0) + player.pitch;

        for (int y = wallOffset + wallHeight; y < 640; y++) {
            double dy = (y - cameraHeight);
            int distance = (int) ((tileSize * 640) / dy);
            fogFactor = Math.min(1.0, (double) (distance - 100) / 400);
            double deg = Math.toRadians(rayAngle);
            double raFix = Math.cos(Math.toRadians(fixAngle(player.angle - rayAngle)));

            // Find distance to floor using trigonometry
            double worldX = player.x / 2 + Math.cos(deg) * 320 * 1.6 * 32 / dy / raFix;
            double worldY = player.y / 2 - Math.sin(deg) * 320 * 1.6 * 32 / dy / raFix;

            int tileX = (int) (worldX / tileSize * 2);
            int tileY = (int) (worldY / tileSize * 2);

            BufferedImage textureToUse = floorTexture;
            if (map.getTileValue(tileX, tileY) == 4) {
                textureToUse = finishTexture;
            }
            //find texture coordinates
            double textureX = Math.abs((int) worldX * textureScaleFactor * 2);
            double textureY = Math.abs((int) worldY * textureScaleFactor * 2);

            int actualTextureX = (int) (textureX % textureWidth);
            int actualTextureY = (int) (textureY % textureHeight);

            int pixelColor = textureToUse.getRGB(actualTextureX, actualTextureY);
            Color floorColor = new Color(pixelColor);

            Color foggyFloorColor = getFogWallColor(fogFactor, floorColor);

            g.setColor(foggyFloorColor);
            g.fillRect(rayIndex * 4, y, 4, 1);

        }
    }

    private void drawCeiling(Graphics2D g, double rayAngle, int rayIndex, int wallOffset) {
        int textureWidth = wallTexture.getWidth();
        int textureHeight = wallTexture.getHeight();
        double textureScaleFactor = (double) textureWidth / tileSize;
        double cameraHeight = (640 / 2.0) + player.pitch;

        for (int y = 0; y < wallOffset; y++) {
            double dy = cameraHeight - y;
            int distance = (int) ((tileSize * 640) / dy);
            fogFactor = Math.min(1.0, (double) (distance - 100) / 400);
            double deg = Math.toRadians(rayAngle);
            double raFix = Math.cos(Math.toRadians(fixAngle(player.angle - rayAngle)));

            double textureX = Math.abs((int) (player.x / 2 + Math.cos(deg) * 320 * 1.6 * 32 / dy / raFix) * textureScaleFactor * 2);
            double textureY = Math.abs((int) (player.y / 2 - Math.sin(deg) * 320 * 1.6 * 32 / dy / raFix) * textureScaleFactor * 2);

            int actualTextureX = (int) (textureX % textureWidth);
            int actualTextureY = (int) (textureY % textureHeight);

            int pixelColor = floorTexture.getRGB(actualTextureX, actualTextureY);
            Color ceilingColor = new Color(pixelColor);

            Color foggyCeilingColor = getFogWallColor(fogFactor, ceilingColor);

            g.setColor(foggyCeilingColor);
            g.fillRect(rayIndex * 4, y, 4, 1);
        }
    }

    private void drawWall(Graphics2D g, double textureX, double wallHeight, double wallOffset, double textureYstep, int textureYOff, int rayIndex, double distance, BufferedImage texture) {
        int textureWidth = texture.getWidth();
        int textureHeight = texture.getHeight();
        double textureY = (textureYOff * textureYstep);
        double maxDistance = 300.0;
        double fogFactor = Math.min(1.0, distance / maxDistance);

        //Loop through each pixel in the wall slice from the texture
        for (int y = 0; y < wallHeight; y++) {
            int wrappedTextureX = (int) textureX % textureWidth;
            int wrappedTextureY = (int) textureY % textureHeight;

            wrappedTextureX = Math.max(0, wrappedTextureX);
            wrappedTextureY = Math.max(0, wrappedTextureY);

            int pixelColor = texture.getRGB(wrappedTextureX, wrappedTextureY);
            Color wallColor = new Color(pixelColor);
            wallColor = getFogWallColor(fogFactor, wallColor);

            g.setColor(wallColor);
            g.fillRect(rayIndex * 4, (int) wallOffset + y, 4, 1);
            textureY += textureYstep;
        }
    }

    private Color getFogWallColor(double fogFactor, Color wallColor) {
        Color fogColor = new Color(10, 10, 10);

        int red = (int) ((1 - fogFactor) * wallColor.getRed() + fogFactor * fogColor.getRed());
        int green = (int) ((1 - fogFactor) * wallColor.getGreen() + fogFactor * fogColor.getGreen());
        int blue = (int) ((1 - fogFactor) * wallColor.getBlue() + fogFactor * fogColor.getBlue());

        red = Math.max(0, Math.min(255, red));
        green = Math.max(0, Math.min(255, green));
        blue = Math.max(0, Math.min(255, blue));

        return new Color(red, green, blue);
    }

    public double fixAngle(double angle) {
        if (angle > 359) {
            angle -= 360;
        } else if (angle < 0) {
            angle += 360;
        }
        return angle;
    }

    public void toggleFlashlight() {
        map.shuffleMap();
        soundManager.playSound("lightSwitch", false, true);
        flashlightOn = !flashlightOn;
    }

    private void flashlightEffect(Graphics2D g, boolean applyFlashlight) {
        if (!applyFlashlight) {
            return;
        }
        int flashlightRadius = 300;
        Point center = new Point(480, 320); // Assuming the screen's center point

        if (flashlightOn) {
            RadialGradientPaint flashlight = new RadialGradientPaint(
                    center,
                    flashlightRadius,
                    new float[]{0.0f, 0.5f, 1.0f}, // Adding a midpoint for a sharper transition
                    new Color[]{
                            new Color(0, 0, 0, 0), // Center is fully transparent
                            new Color(0, 0, 0, 100),     // Midpoint is semi-dark
                            new Color(0, 0, 0, 252)      // Edge is nearly opaque
                    }
            );

            g.setPaint(flashlight);
            g.fillRect(0, 0, 960, 640);

        } else {
            g.setColor(new Color(0, 0, 0, 252));
            g.fillRect(0, 0, 960, 640);
        }
    }

    public void drawShadows(Graphics2D g) {
        for (Shadow shadow : shadows) {
            if (!shadow.visible) {
                continue;
            }
            double dx = shadow.x - player.x;
            double dy = shadow.y - player.y;
            double distance = Math.sqrt(dx * dx + dy * dy);

            if (distance > 4 * tileSize) {
                continue;
            }

            double angleToShadow = Math.atan2(dy, dx);
            double angleDiff = angleToShadow - Math.toRadians(-player.angle);

            if (angleDiff > Math.PI) {
                angleDiff -= 2 * Math.PI;
            }
            if (angleDiff < -Math.PI) {
                angleDiff += 2 * Math.PI;
            }

            int shadowRayIndex = (int) ((angleDiff + Math.toRadians(30)) * (240 / Math.toRadians(60)));

            if (shadowRayIndex >= 0 && shadowRayIndex < 240 && distance < depthArray[shadowRayIndex]) {
                double screenX = (960 / 2) + Math.tan(angleDiff) * 960;
                int shadowSize = (int) (64 * 100 / distance);
                shadowSize = Math.min(shadowSize, 640);

                if (!shadow.seen) {
                    shadow.seen = true;
                    int duration = 2000 + random.nextInt(3000);
                    shadow.reactToPlayer(soundManager, duration);
                }

                g.setColor(new Color(0, 0, 0, 150));
                g.fillRect((int) screenX - shadowSize / 2, 320 - shadowSize / 2 + (int) (player.pitch), shadowSize, shadowSize);
            }
        }
    }

    public void startHallucination(int duration, boolean shadowAct) {
        if (shadowAct) {
            encounterCount++;
            switch (encounterCount) {
                case 1:
                    currentOverlay = dontMoveText;
                    break;
                case 2:
                    currentOverlay = stayStillText;
                    break;
                case 3:
                    currentOverlay = notThisTime;
                    break;
                default:
                    currentOverlay = dontMoveText;
                    break;
            }
        }
        soundManager.stopSound("heartbeat");
        soundManager.playSound("fast heartbeat", true, false);
        Random random = new Random();
        double startTime = System.currentTimeMillis();
        double endTime = System.currentTimeMillis() + duration;
        showText = true;

        Timer flickerTimer = new Timer(50, null);
        flickerTimer.addActionListener(_ -> {
            toggleFlashlight();
            showText = !showText;
            if (encounterCount == 3 && !overlayChanged) {
                long currentTime = System.currentTimeMillis();
                if (currentTime - startTime >= duration / 2) {
                    currentOverlay = run;
                    overlayChanged = true;
                }
            }
            if (flashlightOn) {
                bloodVisible = random.nextDouble() < 0.5;
            }
            if (System.currentTimeMillis() >= endTime) {
                flickerTimer.stop();
                lightSequence();
            } else {
                int nextDelay = 300 + random.nextInt(400);
                flickerTimer.setDelay(nextDelay);
            }
        });

        if (!flashlightOn) toggleFlashlight();
        flickerTimer.setInitialDelay(300 + random.nextInt(400));
        flickerTimer.start();
    }

    //This is a disaster of nested timer
    private void lightSequence() {
        if (!flashlightOn) toggleFlashlight();
        bloodVisible = false;
        showText = false;
        toggleFlashlight();
        Timer offTimer = new Timer(1000, delay -> {
            ((Timer) delay.getSource()).stop();
            toggleFlashlight();
            bloodVisible = true;
            Timer bloodTimer = new Timer(5000, delay2 -> {
                ((Timer) delay2.getSource()).stop();
                toggleFlashlight();
                bloodVisible = false;
                Timer delayTimer = new Timer(300, delay3 -> {
                    ((Timer) delay3.getSource()).stop();
                    toggleFlashlight();
                    soundManager.stopSound("fast heartbeat");
                    soundManager.playSound("heartbeat", true, false);
                });
                delayTimer.start();
            });
            bloodTimer.start();
        });
        offTimer.start();
    }

    public void drawOverlay(Graphics2D g) {
        if (currentOverlay != null && showText) {
            double currentTime = System.currentTimeMillis();
            if (currentTime - lastJitterTime > 100) {
                jitterX = random.nextInt(30) - 15;
                jitterY = random.nextInt(30) - 15;
                lastJitterTime = currentTime;
            }
            int posX = (960 - currentOverlay.getWidth()) / 2 + jitterX;
            int posY = (640 - currentOverlay.getHeight()) / 2 + jitterY;
            g.drawImage(currentOverlay, posX, posY, null);
        }
    }

    public void startEndGameAnimation() {
        endFadePlayed = true;
        fadeAlpha = 0;
        fadeInProgress = true;
        applyFlashlight = false;

        Timer fadeTimer = new Timer(50, e -> {
            fadeAlpha += 5;
            if (fadeAlpha >= 255) {
                fadeAlpha = 255;
                mainGame.gameEnd();
                soundManager.stopSound("fast heartbeat");
                ((Timer) e.getSource()).stop();
            }
        });
        fadeTimer.setRepeats(true);
        fadeTimer.start();
    }

    public void drawFadeEffect(Graphics2D g) {
        if (fadeInProgress) {
            g.setColor(new Color(102, 0, 0, fadeAlpha));
            g.fillRect(0, 0, 960, 640);
        }
    }

    private final boolean allItemsCollected = true;

    public void checkEndCondition() {
        if (endFadePlayed) {
            return;
        }
        if (map.getTileValue((int) (player.x / tileSize), (int) (player.y / tileSize)) == 4 && allItemsCollected) {
            startEndGameAnimation();
        }
    }
}