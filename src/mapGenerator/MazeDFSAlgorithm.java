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

    public MazeDFSAlgorithm(Node[][] nodes, Node startNode, int maxCol, int maxRow) {
        this.nodes = nodes;
        this.maxCol = maxCol;
        this.maxRow = maxRow;
        this.stack = new Stack<>();
        Random random = new Random();
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
        Collections.shuffle(neighbors);
        return neighbors.getFirst();
    }

    private void removeWallBetween(Node current, Node next) {
        int col = (current.col + next.col) / 2;
        int row = (current.row + next.row) / 2;

        nodes[col][row].toggleWall();
    }

    public boolean isFinished() {
        return generationFinished;
    }
}

