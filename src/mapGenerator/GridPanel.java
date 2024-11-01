package mapGenerator;

import javax.swing.*;
import java.awt.*;

public class GridPanel extends JPanel {
    public Node[][] nodes;
    public final int maxCol;
    public final int maxRow;
    private MazeDFSAlgorithm dfsMaze;
    public Node startNode, finishNode;
    public boolean isRunning;
    public boolean isFinished;
    public boolean maze;

    GridPanel(int nodeSize) {
        int panelSize = 250;
        maxCol = panelSize / nodeSize + 1;
        maxRow = panelSize / nodeSize + 1;
        nodes = new Node[maxCol][maxRow];
        setPreferredSize(new Dimension(panelSize, panelSize));
        setLayout(new GridLayout(maxRow, maxCol));

        isRunning = false;
        isFinished = false;
        maze = false;

        for (int col = 0; col < maxCol; col++) {
            for (int row = 0; row < maxRow; row++) {
                nodes[col][row] = new Node(col, row, this);
                nodes[col][row].setPreferredSize(new Dimension(nodeSize, nodeSize));

                //Set border nodes
                if (col == 0 || row == 0 || col == maxCol - 1 || row == maxRow - 1) {
                    nodes[col][row].wall = true;
                    nodes[col][row].border = true;
                    nodes[col][row].setBackground(Color.black);
                }

                this.add(nodes[col][row]);
            }
        }
        nodes[1][1].setStartPoint();
    }

    public void startMaze() {
        if (isRunning) return;
        isRunning = true;
        dfsMaze = new MazeDFSAlgorithm(nodes, startNode, maxCol, maxRow);
        Timer timer = new Timer(5, e -> {
            dfsMaze.generateMazeStep();
            repaint();
            if (dfsMaze.isFinished()) {
                ((Timer) e.getSource()).stop();
                finishNode = dfsMaze.finishNode;
                isRunning = false;
                isFinished = true;

            }

        });
        timer.start();
    }

    public void reset() {
        for (int col = 0; col < maxCol; col++) {
            for (int row = 0; row < maxRow; row++) {
                nodes[col][row].reset();

                if (col == 0 || row == 0 || col == maxCol - 1 || row == maxRow - 1) {
                    nodes[col][row].wall = true;
                    nodes[col][row].setBackground(Color.black);
                }
            }
        }
        startNode.setStartPoint();
        finishNode = null;
        isFinished = false;
        isRunning = false;
    }
    public void resetParent() {
        for (int col = 0; col < maxCol; col++) {
            for (int row = 0; row < maxRow; row++) {
                nodes[col][row].parent = null;

            }
        }
    }

    public int[] arrayConverter() {
        int[] gridArray = new int[maxCol * maxRow];
        int index = 0;

        for (int col = 0; col < maxCol; col++) {
            for (int row = 0; row < maxRow; row++) {
                Node node = nodes[col][row];
                node.setCoordinates();

                if (node.startPoint) {
                    gridArray[index] = 3;
                } else if (node.finishPoint) {
                    gridArray[index] = 4;
                } else if (node.wall) {
                    gridArray[index] = 1;
                } else if (node.shadow) {
                    gridArray[index] = 5;
                } else {
                    gridArray[index] = 0;
                }
                index++;
            }
        }
        return gridArray;
    }
}