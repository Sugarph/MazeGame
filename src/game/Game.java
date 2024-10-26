package game;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;

public class Game extends JPanel implements KeyListener, MouseMotionListener {

    private final Player player;
    private final MapGrid map;
    private final Renderer rayCasting;
    private boolean keyW = false, keyS = false, keyA = false, keyD = false;
    private double lastFrameTime = System.nanoTime();
    private final Point centerPoint;
    private Robot robot;
    private final JFrame frame;
    public final SoundManager soundManager;
    private boolean isWalkingSoundPlaying = false;

    public Game(int[] mapData, int mapWidth, int mapHeight, int tileSize) {
        map = new MapGrid(mapWidth, mapHeight, tileSize, mapData);
        int startX = 0, startY = 0;
        for (int i = 0; i < mapData.length; i++) {
            if (mapData[i] == 3) {
                startX = (i % mapWidth) * tileSize + tileSize / 2;
                startY = (i / mapWidth) * tileSize + tileSize / 2;
                break;
            }
        }
        soundManager = new SoundManager();
        player = new Player(startX, startY, 90);
        rayCasting = new Renderer(player, map);

        // Initialize SoundManager and load sounds


        frame = new JFrame("Ray Casting Demo");
        frame.setSize(960, 640);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setResizable(false);
        frame.add(this);
        frame.addKeyListener(this);
        frame.addMouseMotionListener(this);

        // Create blank cursor
        BufferedImage cursorImg = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
        Cursor blankCursor = Toolkit.getDefaultToolkit().createCustomCursor(cursorImg, new Point(0, 0), "blank cursor");
        frame.setCursor(blankCursor);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
        centerPoint = new Point(frame.getWidth() / 2, frame.getHeight() / 2);

        try {
            robot = new Robot();
        } catch (Exception e) {
            e.printStackTrace();
        }

        Timer timer = new Timer(20, _ -> {
            double currentTime = System.currentTimeMillis();
            double deltaTime = (currentTime - lastFrameTime) / 1_000.0;
            lastFrameTime = currentTime;
            handleMovement(deltaTime);
            repaint();
        });
        timer.start();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        rayCasting.rayCasting(g2);
    }

    private void handleMovement(double deltaTime) {
        double movementSpeed = 200.0 * deltaTime;
        double sideWaySpeed = movementSpeed * 0.7; // Sideway movement adjustment
        double diagonalSpeedFactor = 1.4;

        boolean movingForwardBackward = keyW || keyS;
        boolean moveSideWay = keyA || keyD;

        if (movingForwardBackward && moveSideWay) {
            movementSpeed /= diagonalSpeedFactor;
            sideWaySpeed /= diagonalSpeedFactor;
        }

        if ((keyW || keyS || keyA || keyD) && !isWalkingSoundPlaying) {
            soundManager.playSound("walking", true, false);
            isWalkingSoundPlaying = true;
        } else if (!(keyW || keyS || keyA || keyD) && isWalkingSoundPlaying) {
            soundManager.stopSound("walking");  // Stop walking sound
            isWalkingSoundPlaying = false;
        }

        if (keyW && !map.WallAtTile((int) (player.x + player.deltaX * movementSpeed), (int) (player.y + player.deltaY * movementSpeed))) {
            player.moveForward(movementSpeed);
        }
        if (keyS && !map.WallAtTile((int) (player.x - player.deltaX * movementSpeed), (int) (player.y - player.deltaY * movementSpeed))) {
            player.moveBackward(movementSpeed);
        }

        if (keyA && !map.WallAtTile((int) (player.x + player.deltaY * sideWaySpeed), (int) (player.y - player.deltaX * sideWaySpeed))) {
            player.moveSideway(sideWaySpeed);
        }
        if (keyD && !map.WallAtTile((int) (player.x - player.deltaY * sideWaySpeed), (int) (player.y + player.deltaX * sideWaySpeed))) {
            player.moveSideway(-sideWaySpeed);
        }
    }

    @Override
    public void keyPressed(KeyEvent e) {
        switch (e.getKeyCode()) {
            case KeyEvent.VK_W -> keyW = true;
            case KeyEvent.VK_S -> keyS = true;
            case KeyEvent.VK_A -> keyA = true;
            case KeyEvent.VK_D -> keyD = true;
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
        switch (e.getKeyCode()) {
            case KeyEvent.VK_W -> keyW = false;
            case KeyEvent.VK_S -> keyS = false;
            case KeyEvent.VK_A -> keyA = false;
            case KeyEvent.VK_D -> keyD = false;
        }
    }

    @Override
    public void keyTyped(KeyEvent e) {}

    @Override
    public void mouseMoved(MouseEvent e) {
        Point mousePosition = e.getPoint();
        int deltaX = mousePosition.x - centerPoint.x;
        int deltaY = mousePosition.y - centerPoint.y;
        double sensitivity = 0.1;
        player.updateAngle(-deltaX * sensitivity);
        player.updatePitch(-deltaY * sensitivity * 10);
        Point windowLocation = frame.getLocationOnScreen();
        robot.mouseMove(windowLocation.x + centerPoint.x, windowLocation.y + centerPoint.y);
    }

    @Override
    public void mouseDragged(MouseEvent e) {}
}
