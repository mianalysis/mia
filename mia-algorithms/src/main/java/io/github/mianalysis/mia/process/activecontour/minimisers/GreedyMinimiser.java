//TODO: Add minimum and maximum node spacing.  Beyond this, the node stays in its original place

package io.github.mianalysis.mia.process.activecontour.minimisers;

import java.util.Iterator;

import io.github.mianalysis.mia.process.activecontour.energies.EnergyCollection;
import io.github.mianalysis.mia.process.activecontour.physicalmodel.NodeCollection;
import io.github.mianalysis.mia.process.activecontour.physicalmodel.Vertex;
import io.github.mianalysis.mia.process.math.CumStat;

/**
 * Created by Stephen on 16/09/2016.
 */
public class GreedyMinimiser {
    public static final int RANDOM = 0; //Evaluate nodes as they come off the NodeCollection iterator
    public static final int RIGHTDOWN = 1; //Start at top-left and work right, then down
    public static final int CLOCKWISE = 2; //Start at the first node in NodeCollection and proceed to the right
    int sequence = 0; //Defaults to random read

    EnergyCollection energies;
    double width = 100; //full width of window over which greedy minimiser checks
    double sample_n = 25; //Number of samples in one dimension


    public GreedyMinimiser(EnergyCollection energies) {
        this.energies = energies;

    }

    public void evaluateGreedy(Vertex node) {
        double x = node.getX();
        double y = node.getY();
        double x_min = x-width/2;
        double y_min = y-width/2;
        double spacing = width/(sample_n-1);

        double min_energy = Double.POSITIVE_INFINITY;
        CumStat cs_x = new CumStat();
        CumStat cs_y = new CumStat();

        //Iterating over all possible sites
        for (int c=0;c<sample_n;c++) {
            for (int r=0;r<sample_n;r++) {
                //Updating node position to the current test site
                node.setX(x_min+c*spacing);
                node.setY(y_min+r*spacing);

                double energy = energies.getEnergy(node);

                if (energy < min_energy) {
                    min_energy = energy;
                    cs_x = new CumStat();
                    cs_y = new CumStat();
                    cs_x.addMeasure(x_min+c*spacing);
                    cs_y.addMeasure(y_min+r*spacing);

                } else if (energy == min_energy) {
                    cs_x.addMeasure(x_min+c*spacing);
                    cs_y.addMeasure(y_min+r*spacing);
                }
            }
        }

        //Placing the node at the optimal location
        node.setXY(cs_x.getMean(),cs_y.getMean());
        node.setEnergy(min_energy);
        node.setDistanceMoved(x,y,node.getX(),node.getY());

    }

    public void evaluateGreedy(NodeCollection nodes) {
        switch(sequence){
            case RANDOM:
                Iterator<Vertex> iterator = nodes.iterator();
                while (iterator.hasNext()) {
                    evaluateGreedy(iterator.next());

                }

                break;

            case RIGHTDOWN:
                Vertex node_row = nodes.getSpecialNode(Vertex.TOPLEFT); //The starting node
                Vertex node_col = node_row;

                while (node_row.getBottomNeighbour() != null) {
                    while (node_col.getRightNeighbour() != null) {
                        evaluateGreedy(node_col);
                        node_col = node_col.getRightNeighbour(); //Moving onto the next node in the row

                    }

                    evaluateGreedy(node_col);

                    node_row = node_row.getBottomNeighbour(); //Moving onto the next node in the row
                    node_col = node_row;

                }

                while (node_col.getRightNeighbour() != null) {
                    evaluateGreedy(node_col);
                    node_col = node_col.getRightNeighbour(); //Moving onto the next node in the row

                }

                break;

            case CLOCKWISE:
                iterator = nodes.iterator();
                Vertex node = iterator.next();
                evaluateGreedy(node);
                int startNode = node.getID();

                node = node.getRightNeighbour();
                while (node.getID() != startNode) {
                    evaluateGreedy(node);
                    node = node.getRightNeighbour();
                }

                break;

        }
    }

    public double getWidth() {
        return width;

    }

    public double getSampleN() {
        return sample_n;

    }

    public void setWidth(double width) {
        this.width = width;

    }

    public void setSampleN(double sample_n) {
        this.sample_n = sample_n;

    }

    public void setSequence(int sequence) {
        this.sequence = sequence;

    }
}
