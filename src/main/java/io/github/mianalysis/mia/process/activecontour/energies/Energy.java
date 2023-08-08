package io.github.mianalysis.mia.process.activecontour.energies;

import io.github.mianalysis.mia.process.activecontour.physicalmodel.Vertex;

/**
 * Created by Stephen on 16/09/2016.
 */
public class Energy {
    public double weight = 1;

    public Energy(double weight) {
        this.weight = weight;

    }

    public double getWeight() {
        return weight;

    }

    public void setWeight(double weight) {
        this.weight = weight;

    }

    public double getEnergy(Vertex node) {
        return 0;

    }

}
