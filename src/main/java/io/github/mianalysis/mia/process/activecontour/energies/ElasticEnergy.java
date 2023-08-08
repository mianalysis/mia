//TODO: Add math for getEnergy

package io.github.mianalysis.mia.process.activecontour.energies;

import io.github.mianalysis.mia.process.activecontour.physicalmodel.Vertex;

/**
 * Created by Stephen on 16/09/2016.
 */
public class ElasticEnergy extends Energy {
    public ElasticEnergy(double weight) {
        super(weight);
    }

    public double getEnergy(Vertex node) {
        double x = node.getX();
        double y = node.getY();

        double term_x_left = 0;
        double term_y_left = 0;
        double term_x_right = 0;
        double term_y_right = 0;
        double term_x_top = 0;
        double term_y_top = 0;
        double term_x_bottom = 0;
        double term_y_bottom = 0;

        double left_factor = 1;
        double right_factor = 1;
        double top_factor = 1;
        double bottom_factor = 1;

        if (node.getLeftNeighbour() != null) {
            term_x_left = Math.pow(x-node.getLeftNeighbour().getX(),2);
            term_y_left = Math.pow(y-node.getLeftNeighbour().getY(),2);

        } else {
            left_factor = 0;
            right_factor = 0;
        }

        if (node.getRightNeighbour() != null) {
            term_x_right = Math.pow(x-node.getRightNeighbour().getX(),2);
            term_y_right = Math.pow(y-node.getRightNeighbour().getY(),2);

        } else {
            left_factor = 0;
            right_factor = 0;
        }

        if (node.getTopNeighbour() != null) {
            term_x_top = Math.pow(x-node.getTopNeighbour().getX(),2);
            term_y_top = Math.pow(y-node.getTopNeighbour().getY(),2);
        } else {
            top_factor = 0;
            bottom_factor = 0;
        }

        if (node.getBottomNeighbour() != null) {
            term_x_bottom = Math.pow(x-node.getBottomNeighbour().getX(),2);
            term_y_bottom = Math.pow(y-node.getBottomNeighbour().getY(),2);
        }  else {
            top_factor = 0;
            bottom_factor = 0;
        }

        return weight*(left_factor*(term_x_left+term_y_left)+right_factor*(term_x_right+term_y_right)+top_factor*(term_x_top+term_y_top)+bottom_factor*(term_x_bottom+term_y_bottom));

    }
}
