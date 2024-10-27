package mapGenerator;

import javax.swing.*;
import java.awt.*;

public class Node extends JButton {
    public boolean startPoint;
    public boolean finishPoint;
    boolean wall;
    public int col, row, x, y;
    private final GridPanel gridPanel;
    public boolean visited;
    public boolean border;
    public boolean shadow;
    public boolean searched, open;
    public int gCost;
    public int hCost;
    public int fCost;
    public Node parent;

    public Node(int col, int row, GridPanel gridPanel) {
        this.col = col;
        this.row = row;
        setBackground(Color.white);
        setBorder(BorderFactory.createLineBorder(Color.black));
        this.gridPanel = gridPanel;
        border = false;
        visited = false;
        shadow = false;
    }

    public void setStartPoint() {
        setBackground(Color.green);
        startPoint = true;
        gridPanel.startNode = this;
    }

    public void setCoordinates() {
        this.y = col * 64 + 32;
        this.x = row * 64 + 32;
    }

    public void setFinishPoint() {
        setBackground(Color.red);
        finishPoint = true;
        gridPanel.finishNode = this;
    }

    public void toggleWall() {
        wall = !wall;
        setBackground(wall ? Color.black : Color.white);
    }

    public void reset() {
        wall = false;
        setBackground(Color.white);
        setForeground(Color.white);
        shadow = false;
    }

}