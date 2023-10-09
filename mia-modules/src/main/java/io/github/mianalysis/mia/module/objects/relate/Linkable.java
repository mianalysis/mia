package io.github.mianalysis.mia.module.objects.relate;

public class Linkable {
    final double cost;
    final int ID1;
    final int ID2;

    public Linkable(double cost, int ID1, int ID2) {
        this.cost = cost;
        this.ID1 = ID1;
        this.ID2 = ID2;
    }

    public double getCost() {
        return cost;
    }

    public int getID1() {
        return ID1;
    }

    public int getID2() {
        return ID2;
    }
}
