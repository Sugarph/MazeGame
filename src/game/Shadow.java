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
    private final Renderer renderer;
    private final double speed = 4.0;
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
            int shadowCol = (int) (y / 64);
            int shadowRow = (int) (x / 64);
            if (!pathCalculated || needRecalculate) {
                Node startNode = gridPanel.nodes[shadowCol][shadowRow];
                int targetCol = (int) (playerY / 64);
                int targetRow = (int) (playerX / 64);
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