package mapGenerator;

import java.awt.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.Stack;

public class MazeDFSAlgorithm {
    private final Node[][] nodes;
    private final Stack<Node> stack;
    private Node current;
    private final int maxCol, maxRow;
    private boolean generationFinished = false;
    private boolean finishNodeSet = false;
    public Node finishNode;
    private int backtrackCount = 0;
    private final int finishBacktrackThreshold;
    private boolean backtrackingStarted = false;
    private final List<Node> shadowNodes = new ArrayList<>();
    private final Random random = new Random();
    private final int minDistanceFromStart = 20;
    private final int minDistanceBetweenShadows = 12;
    private final int guaranteedShadows = 4;
    private final int maxShadowChance = 5; // 5% chance after guaranteed

    public MazeDFSAlgorithm(Node[][] nodes, Node startNode, int maxCol, int maxRow) {
        this.nodes = nodes;
        this.maxCol = maxCol;
        this.maxRow = maxRow;
        this.stack = new Stack<>();
        this.finishBacktrackThreshold = random.nextInt(4) + 2;

        for (int col = 0; col < maxCol; col++) {
            for (int row = 0; row < maxRow; row++) {
                if (!nodes[col][row].startPoint && !nodes[col][row].wall) {
                    nodes[col][row].toggleWall();
                }
                nodes[col][row].visited = false;
            }
        }
        current = startNode;
        current.visited = true;
        stack.push(current);
    }

    public void generateMazeStep() {
        if (!stack.isEmpty()) {
            Node next = getRandomUnvisitedNeighbor(current);
            if (next != null) {
                removeWallBetween(current, next);
                next.visited = true;
                next.reset();
                stack.push(next);
                current = next;
                backtrackingStarted = false;

                // Shadow spawning logic during generation
                if (shadowNodes.size() < guaranteedShadows || random.nextInt(100) < maxShadowChance) {
                    if (distanceCheck(current)) {
                        current.shadow = true;
                        shadowNodes.add(current);
                        current.setBackground(Color.yellow); // Visual indicator
                    }
                }

            } else {
                if (!backtrackingStarted) {
                    backtrackingStarted = true;
                    backtrackCount++;
                }

                if (!finishNodeSet && backtrackCount == finishBacktrackThreshold) {
                    finishNodeSet = true;
                    current.setFinishPoint();
                    finishNode = current;
                }
                current = stack.pop();
            }
        } else {
            generationFinished = true;
        }
    }

    private Node getRandomUnvisitedNeighbor(Node node) {
        List<Node> neighbors = new ArrayList<>();

        int col = node.col;
        int row = node.row;

        if (col > 1 && !nodes[col - 2][row].visited) neighbors.add(nodes[col - 2][row]);
        if (col < maxCol - 2 && !nodes[col + 2][row].visited) neighbors.add(nodes[col + 2][row]);
        if (row > 1 && !nodes[col][row - 2].visited) neighbors.add(nodes[col][row - 2]);
        if (row < maxRow - 2 && !nodes[col][row + 2].visited) neighbors.add(nodes[col][row + 2]);

        if (neighbors.isEmpty()) return null;

        Collections.shuffle(neighbors);
        return neighbors.get(0);
    }

    private void removeWallBetween(Node current, Node next) {
        int col = (current.col + next.col) / 2;
        int row = (current.row + next.row) / 2;

        nodes[col][row].toggleWall();
    }

    private boolean distanceCheck(Node node) {
        if (distance(node, nodes[0][0]) < minDistanceFromStart) return false;
        for (Node shadow : shadowNodes) {
            if (distance(node, shadow) < minDistanceBetweenShadows) return false;
        }
        return true;
    }

    private int distance(Node a, Node b) {
        return Math.abs(a.col - b.col) + Math.abs(a.row - b.row);
    }

    public boolean isFinished() {
        return generationFinished;
    }
}
