package io.github.mianalysis.mia.object.coordinates;

import java.util.HashSet;

/**
 * Created by sc13967 on 25/01/2018.
 */
public class Vertex extends Point<Integer> {
    /**
     *
     */
    private static final long serialVersionUID = -4612166594480719814L;
    private HashSet<Vertex> neighbours = new HashSet<>();

    public Vertex(Integer x, Integer y, Integer z) {
        super(x, y, z);
    }

    public int getNumberOfNeighbours() {
        return neighbours.size();
    }

    public void addNeighbour(Vertex neighbour) {
        neighbours.add(neighbour);
    }

    public void removeNeighbour(Vertex neighbour) {
        neighbours.remove(neighbour);
    }

    public HashSet<Vertex> getNeighbours() {
        return neighbours;
    }

    public void setNeighbours(HashSet<Vertex> neighbours) {
        this.neighbours = neighbours;
    }
}
