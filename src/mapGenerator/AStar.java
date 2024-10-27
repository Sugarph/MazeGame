package mapGenerator;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;

public class AStar {
    private final Node finish;
    private final Node[][] nodes;
    private final int maxCol, maxRow;
    private boolean goalReached;
    private final PriorityQueue<Node> openList;
    private final ArrayList<Node> checkedList;

    public AStar(Node[][] nodes, Node start, Node finish, int maxCol, int maxRow, GridPanel gridPanel) {
        this.nodes = nodes;
        this.finish = finish;
        this.maxCol = maxCol;
        this.maxRow = maxRow;
        goalReached = false;
        gridPanel.resetParent();
        //priority based on fCost and tie-breaking with hCost
        Comparator<Node> comparator = Comparator.comparingInt((Node n) -> n.fCost)
                .thenComparingInt(n -> n.hCost);
        openList = new PriorityQueue<>(comparator);
        checkedList = new ArrayList<>();
        openList.add(start);
    }

    // A* algorithm
    public void runAStar() {
        while (!goalReached && !openList.isEmpty()) {
            Node current = openList.poll();
            checkedList.add(current);
            current.searched = true;

            if (current == finish) {
                goalReached = true;
                return;
            }

            // Check around the current node
            Node[] neighbors = {
                    (current.row - 1 >= 0) ? nodes[current.col][current.row - 1] : null,
                    (current.row + 1 < maxRow) ? nodes[current.col][current.row + 1] : null,
                    (current.col - 1 >= 0) ? nodes[current.col - 1][current.row] : null,
                    (current.col + 1 < maxCol) ? nodes[current.col + 1][current.row] : null
            };

            for (Node neighbor : neighbors) {
                if (neighbor == null || neighbor.wall || checkedList.contains(neighbor)) {
                    continue;
                }
                // Calculate the gCost
                int newGCost = current.gCost + getDistance(current, neighbor);
                // Check if the path is shorter
                if (newGCost < neighbor.gCost || !openList.contains(neighbor)) {
                    neighbor.gCost = newGCost;
                    neighbor.hCost = getDistance(neighbor, finish);
                    neighbor.fCost = neighbor.gCost + neighbor.hCost;
                    neighbor.parent = current;

                    if (!openList.contains(neighbor)) {
                        openList.add(neighbor);
                        neighbor.open = true;
                    }
                }
            }
        }
    }
    public List<Node> getPath() {
        List<Node> path = new ArrayList<>();
        if (!goalReached) {
            return path;
        }

        Node current = finish;
        while (current != null) {
            path.addFirst(current);
            current = current.parent;
        }

        return path;
    }

    // Calculate Distance (Manhattan Distance)
    private int getDistance(Node nodeA, Node nodeB) {
        int distanceX = Math.abs(nodeA.col - nodeB.col);
        int distanceY = Math.abs(nodeA.row - nodeB.row);
        return distanceX + distanceY;
    }

    public boolean isGoalReached() {
        return goalReached;
    }
}