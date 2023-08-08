package io.github.mianalysis.mia.process.activecontour.energies;

import io.github.mianalysis.mia.process.activecontour.physicalmodel.Vertex;

/**
 * Created by Stephen on 16/09/2016.
 */
public class BendingEnergy extends Energy {
    public BendingEnergy(double weight) {
        super(weight);
    }

    public double getEnergy(Vertex node) {
        double x = node.getX();
        double y = node.getY();

        double term_x_h = 0;
        double term_y_h = 0;
        double term_x_v = 0;
        double term_y_v = 0;

        if (node.getLeftNeighbour() != null & node.getRightNeighbour() != null) {
            term_x_h = Math.pow(node.getLeftNeighbour().getX()-2*x+node.getRightNeighbour().getX(),2);
            term_y_h = Math.pow(node.getLeftNeighbour().getY()-2*y+node.getRightNeighbour().getY(),2);

        }


        if (node.getTopNeighbour() != null & node.getBottomNeighbour() != null) {
            term_x_v = Math.pow(node.getTopNeighbour().getX()-2*x+node.getBottomNeighbour().getX(),2);
            term_y_v = Math.pow(node.getTopNeighbour().getY()-2*y+node.getBottomNeighbour().getY(),2);

        }

        return weight*(term_x_h+term_y_h+term_x_v+term_y_v);

    }
}