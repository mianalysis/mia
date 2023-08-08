package io.github.mianalysis.mia.process.activecontour;

import io.github.mianalysis.mia.process.activecontour.physicalmodel.NodeCollection;
import io.github.mianalysis.mia.process.activecontour.physicalmodel.Vertex;
import io.github.mianalysis.mia.process.math.Indexer;

/**
 * Created by Stephen on 07/09/2016.
 */
public class GridInitialiser {
    private int n_cols; //Number of columns
    private int n_rows; //Number of rows
    private double x_min = 0; //Minimum x position
    private double y_min = 0; //Minimum y position
    private double x_space = 1; //Column spacing
    private double y_space = 1; //Row spacing
    private boolean fix_edges = false; //Controls if edge nodes are fixed in the corresponding axis

    public GridInitialiser(int n_cols, int n_rows) {
        this.n_cols = n_cols;
        this.n_rows = n_rows;

    }

    public GridInitialiser() {
        this.n_cols = 1;
        this.n_rows = 1;

    }

    public NodeCollection buildGrid(){
        Vertex[] node_arr = new Vertex[n_cols*n_rows];
        Indexer indexer = new Indexer(n_cols, n_rows);

        //Adding Nodes to Vertex array
        for (int c = 0;c < n_cols; c++) {
            for (int r = 0;r < n_rows; r++) {
                double x = x_min+c*x_space;
                double y = y_min+r*y_space;

                int ind = indexer.getIndex(new int[]{c,r});
                node_arr[ind] = new Vertex(x,y);

                if (c == 0 & r == 0) {
                    node_arr[ind].setSpecial(Vertex.TOPLEFT);
                } else if (c == n_cols-1 & r == 0) {
                    node_arr[ind].setSpecial(Vertex.TOPRIGHT);
                } else if (c == 0 & r == n_rows-1) {
                    node_arr[ind].setSpecial(Vertex.BOTTOMLEFT);
                } else if (c == n_cols-1 & r == n_rows-1) {
                    node_arr[ind].setSpecial(Vertex.BOTTOMRIGHT);
                }

                if (fix_edges) {
                    if (c == 0 | c == n_cols - 1) {
                        node_arr[ind].setFixedX(true);
                    }

                    if (r == 0 | r == n_rows - 1) {
                        node_arr[ind].setFixedY(true);
                    }
                }
            }
        }

        //Adding node links
        for (int c = 0;c < n_cols; c++) {
            for (int r = 0;r < n_rows; r++) {
                Vertex top_neighbour = null;
                Vertex bottom_neighbour = null;
                Vertex left_neighbour = null;
                Vertex right_neighbour = null;

                if (r > 0) {
                    int ind_top = indexer.getIndex(new int[]{c, r - 1});
                    top_neighbour = node_arr[ind_top];

                }

                if (r < n_rows-1) {
                    int ind_bottom = indexer.getIndex(new int[]{c, r + 1});
                    bottom_neighbour = node_arr[ind_bottom];

                }

                if (c > 0) {
                    int ind_left = indexer.getIndex(new int[]{c - 1, r});
                    left_neighbour = node_arr[ind_left];

                }

                if (c < n_cols-1) {
                    int ind_right = indexer.getIndex(new int[]{c + 1, r});
                    right_neighbour = node_arr[ind_right];

                }

                int ind = indexer.getIndex(new int[]{c,r});
                node_arr[ind].setTopNeighbour(top_neighbour);
                node_arr[ind].setBottomNeighbour(bottom_neighbour);
                node_arr[ind].setLeftNeighbour(left_neighbour);
                node_arr[ind].setRightNeighbour(right_neighbour);
            }
        }

        //Adding Nodes to NodeCollection
        NodeCollection nodes = new NodeCollection();
        for (int i=0;i<node_arr.length;i++) {
            nodes.add(node_arr[i]);
        }

        return nodes;

    }

    public int getNCols() {
        return n_cols;

    }

    public int getNRows() {
        return n_rows;

    }

    public double getXMin() {
        return x_min;

    }

    public double getYMin() {
        return y_min;

    }

    public double getXSpace() {
        return x_space;

    }

    public double getYSpace() {
        return y_space;

    }

    public boolean getFixEdges() {
        return fix_edges;

    }

    public void setNCols(int n_cols) {
        this.n_cols = n_cols;


    }

    public void setNRows(int n_rows) {
        this.n_rows = n_rows;


    }

    public void setXMin(double x_min) {
        this.x_min = x_min;


    }

    public void setYMin(double y_min) {
        this.y_min = y_min;


    }

    public void setXSpace(double x_space) {
        this.x_space = x_space;


    }

    public void setYSpace(double y_space) {
        this.y_space = y_space;


    }

    public void setFixEdges(boolean fix_edges) {
        this.fix_edges = fix_edges;

    }

}
