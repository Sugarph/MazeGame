package game;

import mapGenerator.AStar;
import mapGenerator.GridPanel;
import mapGenerator.Node;

import javax.swing.*;
import java.util.List;
import java.util.ArrayList;

public class Shadow {
    public double x, y;
    public boolean seen, visible, chase;
    private boolean pathCalculated = false;
    private boolean needRecalculate = false;
    private final Renderer renderer;
    private final double speed = 4.25;
    private List<Node> currentPath = new ArrayList<>();
    private int currentPathIndex = 0;
    private double lastDamageTime = 0;

    public Shadow(double x, double y, Renderer renderer) {
        this.x = x;
        this.y = y;
        visible = true;
        seen = false;
        chase = false;
        this.renderer = renderer;
    }

    public void reactToPlayer(SoundManager soundManager, int duration, Player player) {
        int visibleDuration = (renderer.encounterCount == 3) ? duration + 10000 : duration + 1000;
        System.out.println("Shadow spotted!");
        soundManager.playSound("shadow", false, true);
        renderer.hallucination(duration, true);
        double startTime = System.currentTimeMillis();

        Timer visibleTimer = new Timer(visibleDuration, e -> {
            visible = false;
            ((Timer) e.getSource()).stop();
        });
        visibleTimer.start();

        if (renderer.encounterCount == 3) {
            new Timer(2000, e -> {
                chase = true;
                visibleTimer.stop();
                visibleTimer.setInitialDelay(visibleDuration + 5000);
                visibleTimer.restart();
                ((Timer) e.getSource()).stop();
            }).start();
        }

        Timer loopCheck = new Timer(50, e -> {
            if (player.moved && !chase) {
                double elapsedTime = System.currentTimeMillis() - startTime;
                System.out.println(elapsedTime);
                chase = true;
                visibleTimer.stop();
                visibleTimer.setInitialDelay((int) (visibleDuration - elapsedTime));
                visibleTimer.restart();
                ((Timer) e.getSource()).stop();
            } else if (!visible) {
                ((Timer) e.getSource()).stop();
            }
        });
        loopCheck.setInitialDelay(1000); //Delay by 1 sec before check
        loopCheck.start();
    }

    public void updatePosition(Player player, GridPanel gridPanel) {
        if (visible && seen && chase) {
            int shadowCol = (int) (y / 64);
            int shadowRow = (int) (x / 64);
            double dx = player.x - x;
            double dy = player.y - y;

            double distance = Math.sqrt(dx * dx + dy * dy);

            if (distance < 35) {
                long currentTime = System.currentTimeMillis();
                if (currentTime - lastDamageTime >= 1500) {
                    player.hurt(20);
                    System.out.println(player.health);
                    lastDamageTime = currentTime;
                }
            }
            if (!pathCalculated || needRecalculate) {
                Node startNode = gridPanel.nodes[shadowCol][shadowRow];
                int targetCol = (int) (player.y / 64);
                int targetRow = (int) (player.x / 64);
                Node targetNode = gridPanel.nodes[targetCol][targetRow];
                AStar aStar = new AStar(gridPanel.nodes, startNode, targetNode, gridPanel.maxCol, gridPanel.maxRow, gridPanel);
                aStar.runAStar();

                if (aStar.isGoalReached()) {
                    List<Node> newPath = aStar.getPath();
                    if (newPath != null) {
                        currentPath.clear();
                        currentPath = newPath;
                        currentPathIndex = 0;
                        pathCalculated = true;
                        needRecalculate = false;
                    } else {
                        pathCalculated = false;
                    }
                }
            }
            if (!currentPath.isEmpty() && currentPathIndex < currentPath.size()) {
                Node nextNode = currentPath.get(currentPathIndex);
                moveTo(nextNode);

                if (Math.abs(x - nextNode.x) < 8 && Math.abs(y - nextNode.y) < 8) {
                    currentPathIndex++;
                    if (currentPathIndex >= currentPath.size()) {
                        needRecalculate = true;

                    }
                }
            } else {
                needRecalculate = true;
            }
        }
    }

    private void moveTo(Node nextNode) {
        double dx = nextNode.x - x;
        double dy = nextNode.y - y;
        double distance = Math.sqrt(dx * dx + dy * dy);

        if (distance > 0) {
            dx /= distance;
            dy /= distance;

            x += dx * speed;
            y += dy * speed;
        }
    }
}