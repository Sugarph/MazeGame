package mapGenerator;

import javax.swing.*;
import java.awt.*;

public class Node extends JButton {
    public boolean startPoint;
    public boolean finishPoint;
    boolean wall;
    public int col;
    public int row;
    private final GridPanel gridPanel;
    public boolean visited;
    public boolean border;
    public boolean shadow;

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