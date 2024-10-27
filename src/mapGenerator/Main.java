package mapGenerator;

import game.*;
import javax.swing.*;
import java.awt.*;

public class Main {

    public static void main(String[] args) {
        int nodePixelSize;
        nodePixelSize = 12;
        JFrame window = new JFrame("Maze Generation");
        window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        window.setResizable(false);
        window.setPreferredSize(new Dimension(600, 600));

        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new GridBagLayout());
        GridBagConstraints mainGrid = new GridBagConstraints();
        mainGrid.insets = new Insets(10, 10, 10, 10);

        GridPanel gridPanel1 = new GridPanel(nodePixelSize);

        JButton mazeButton = new JButton("Generate Maze");
        mazeButton.addActionListener(_ -> {
            if (!gridPanel1.isRunning && !gridPanel1.isFinished) {
                gridPanel1.startMaze();
            }
        });

        JButton resetButton = new JButton("Reset");
        resetButton.addActionListener(_ -> gridPanel1.reset());

        JButton startGameButton = new JButton("Start Game");
        startGameButton.addActionListener(_ -> {
            //window.dispose();
            Game game = new Game(gridPanel1.arrayConverter(), gridPanel1.maxCol, gridPanel1.maxRow, 64, gridPanel1);
        });
        Timer updateTimer = new Timer(25, _ -> {
            mazeButton.setEnabled(!gridPanel1.isRunning && !gridPanel1.isFinished);
            resetButton.setEnabled(!gridPanel1.isRunning && gridPanel1.isFinished);
            startGameButton.setEnabled(!gridPanel1.isRunning && gridPanel1.isFinished);
        });
        updateTimer.start();

        mainGrid.gridx = 0;
        mainGrid.gridy = 0;
        mainPanel.add(gridPanel1, mainGrid);

        mainGrid.gridy = 1;
        mainGrid.gridwidth = 1;
        mainPanel.add(mazeButton, mainGrid);

        JPanel buttonPanel = new JPanel(new GridBagLayout());
        GridBagConstraints buttonGrid = new GridBagConstraints();
        buttonGrid.insets = new Insets(10, 10, 10, 10);

        buttonGrid.gridx = 0;
        buttonGrid.gridy = 0;
        buttonPanel.add(resetButton, buttonGrid);

        buttonGrid.gridx = 0;
        buttonGrid.gridy = 1;
        buttonPanel.add(startGameButton, buttonGrid);

        mainGrid.gridy = 2;
        mainPanel.add(buttonPanel, mainGrid);

        window.add(mainPanel);
        window.pack();
        window.setVisible(true);
    }
}