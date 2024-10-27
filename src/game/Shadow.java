package game;

import mapGenerator.AStar;
import mapGenerator.GridPanel;
import mapGenerator.Node;

import javax.swing.*;
import java.util.List;
import java.util.ArrayList;

public class Shadow {
    public double x, y;
    public boolean seen;
    public boolean visible;
    private Renderer renderer;
    private double speed = 200.0;
    private List<Node> currentPath = new ArrayList<>();
    private int currentPathIndex = 0;

    public Shadow(double x, double y, Renderer renderer) {
        this.x = x;
        this.y = y;
        visible = true;
        seen = false;
        this.renderer = renderer;
    }

    public void reactToPlayer(SoundManager soundManager, int duration) {
        System.out.println("Shadow spotted!");
        soundManager.playSound("shadow", false, true);
        renderer.startHallucination(duration, true);
        Timer timer = new Timer(duration + 1000, e -> {
            //visible = false;
            ((Timer) e.getSource()).stop();
        });
        timer.start();
    }

    private boolean pathCalculated = false;
    private boolean needRecalculate = false;

    public void updatePosition(double playerX, double playerY, GridPanel gridPanel) {
        if (visible && seen) {
            // Calculate the shadow's grid position
            int shadowCol = (int) x / 64;
            int shadowRow = (int) y / 64;

            if (shadowCol >= 0 && shadowCol < gridPanel.maxCol && shadowRow >= 0 && shadowRow < gridPanel.maxRow) {

                // Only calculate a new path if needed
                if (!pathCalculated || needRecalculate) {
                    Node startNode = gridPanel.nodes[shadowCol][shadowRow];

                    int targetCol = (int) playerX / 64;
                    int targetRow = (int) playerY / 64;

                    if (targetCol >= 0 && targetCol < gridPanel.maxCol && targetRow >= 0 && targetRow < gridPanel.maxRow) {
                        Node targetNode = gridPanel.nodes[targetCol][targetRow];

                        AStar aStar = new AStar(gridPanel.nodes, startNode, targetNode, gridPanel.maxCol, gridPanel.maxRow);
                        aStar.runAStar();

                        if (aStar.isGoalReached()) {
                            currentPath = aStar.getPath();
                            currentPathIndex = 0;
                            pathCalculated = true;
                            needRecalculate = false;
                        } else {
                            pathCalculated = false;
                        }
                    }
                }
            }

            if (!currentPath.isEmpty() && currentPathIndex < currentPath.size()) {
                Node nextNode = currentPath.get(currentPathIndex);
                moveTo(nextNode);

                if (Math.abs(x - nextNode.x) < 5 && Math.abs(y - nextNode.y) < 5) {
                    currentPathIndex++;
                    System.out.println("Path Index: " + currentPathIndex);

                    if (currentPathIndex >= currentPath.size()) {
                        needRecalculate = true;
                    }
                }
            } else {
                moveToAbsolute(playerX, playerY);
            }
        }
    }

    // Method to move the shadow towards a node
    private void moveTo(Node nextNode) {
        double dx = nextNode.x - x;
        double dy = nextNode.y - y;
        double distance = Math.sqrt(dx * dx + dy * dy);

        if (distance < 2) {
            return;
        }

        if (distance > 0) {
            dx /= distance;
            dy /= distance;

            x += dx * speed;
            y += dy * speed;
        }
    }

    private void moveToAbsolute(double playerX, double playerY) {
        double dx = playerX - x;
        double dy = playerY - y;
        double distance = Math.sqrt(dx * dx + dy * dy);

        if (distance < 5) {
            return;
        }

        if (distance > 0) {
            dx /= distance;
            dy /= distance;

            x += dx * speed;
            y += dy * speed;
        }
    }


}

