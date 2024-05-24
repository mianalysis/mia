package io.github.mianalysis.mia.process.skeleton;

import java.util.ArrayList;

import ij.ImagePlus;
import io.github.mianalysis.mia.object.coordinates.Vertex;
import io.github.mianalysis.mia.object.coordinates.VertexCollection;

/**
 * Created by sc13967 on 24/01/2018.
 */
public class Skeleton extends VertexCollection {
    /**
     *
     */
    private static final long serialVersionUID = 3490921805740347855L;
    private double longestDistance;
    private ArrayList<Vertex> longestPath;

    public Skeleton(ImagePlus ipl) {
        for (int x=0;x<ipl.getWidth();x++) {
            for (int y=0;y<ipl.getHeight();y++) {
                for (int z=0;z<ipl.getNSlices();z++) {
                    ipl.setPosition(1,z+1,1);
                    if (ipl.getProcessor().getPixel(x,y) == 0) {
                        add(new Vertex(x,y,z));
                    }
                }
            }
        }

        assignNeighbours();

    }

    public Skeleton(int[] x, int[] y, int[] z) {
        // Creating an array of neighbours
        for (int i=0;i<x.length;i++) {
            add(new Vertex(x[i],y[i],z[i]));

        }

        assignNeighbours();

    }

    public ArrayList<Vertex> getLongestPath() {
        longestDistance = 0;
        longestPath = new ArrayList<>();

        // Getting the vertices with one neighbour (those at branch ends)
        ArrayList<Vertex> endPoints = getEndPoints();

        for (Vertex vertex : endPoints) {
            ArrayList<Vertex> currentPath = new ArrayList<>();
            addDistanceToNextVertex(vertex, 0, currentPath);
        }

        return longestPath;

    }

    public ArrayList<Vertex> getEndPoints() {
        ArrayList<Vertex> endPoints = new ArrayList<>();

        // End points only have one neighbour
        for (Vertex vertex:this) {
            if (vertex.getNumberOfNeighbours() == 1) endPoints.add(vertex);
        }

        return endPoints;

    }

    public ArrayList<Vertex> getBranchPoints() {
        ArrayList<Vertex> branchPoints = new ArrayList<>();

        // Branch points have 3 or more neighbours
        for (Vertex vertex:this) {
            if (vertex.getNumberOfNeighbours() >= 3) branchPoints.add(vertex);
        }

        return branchPoints;

    }

    public Vertex addBreak() {
        // Iterate over each vertex until we find one that creating a break at will cause an end
        for (Vertex vertex : this) {
            for (Vertex neighbour : vertex.getNeighbours()) {
                if (neighbour.getNumberOfNeighbours() == 2) {
                    remove(vertex);
                    return vertex;
                }
            }
        }
        
        return null;
        
    }

    public int[] getX() {
        return stream().mapToInt(Vertex::getX).toArray();
    }

    public int[] getY() {
        return stream().mapToInt(Vertex::getY).toArray();
    }

    public int[] getZ() {
        return stream().mapToInt(Vertex::getZ).toArray();
    }

    private void assignNeighbours() {
        // Assigning neighbours
        for (Vertex vertex1:this) {
            for (Vertex vertex2:this) {
                if (vertex1 == vertex2) continue;
                double distance = vertex1.calculateDistanceToPoint(vertex2);

                // The longest distance for 26-way connectivity is 1.73
                if (distance<1.75) vertex1.addNeighbour(vertex2);

            }
        }

        // There may be junctions with excessive linkages.  Identifying these and removing them.
        ArrayList<Vertex[]> linksToRemove = new ArrayList<>();
        for (Vertex vertex1:this) {
            if (vertex1.getNumberOfNeighbours() > 2) {
                // If the current vertex and a neighbour are connected to the same Vertex, keep the links with the
                // shortest length
                for (Vertex vertex2:vertex1.getNeighbours()) {
                    for (Vertex vertex3:vertex2.getNeighbours()) {
                        // Removing the longest link
                        if (vertex1.getNeighbours().contains(vertex3)) {
                            double v1v3 = vertex1.calculateDistanceToPoint(vertex3);
                            double v1v2 = vertex1.calculateDistanceToPoint(vertex2);
                            double v2v3 = vertex2.calculateDistanceToPoint(vertex3);

                            if (v1v2 > v1v3 && v1v2 > v2v3) {
                                linksToRemove.add(new Vertex[]{vertex1,vertex2});
                            } else if (v1v3 > v1v2 && v1v3 > v2v3) {
                                linksToRemove.add(new Vertex[]{vertex1, vertex3});
                            } else if (v2v3 > v1v2 && v2v3 > v1v3) {
                                linksToRemove.add(new Vertex[]{vertex2, vertex3});
                            }
                        }
                    }
                }
            }
        }

        for (Vertex[] link:linksToRemove) {
            link[0].removeNeighbour(link[1]);
            link[1].removeNeighbour(link[0]);
        }
    }

    private void addDistanceToNextVertex(Vertex currentVertex, double distance, ArrayList<Vertex> currentPath) {
        // Making a new path for this point onwards
        ArrayList<Vertex> newCurrentPath = new ArrayList<>();
        newCurrentPath.addAll(currentPath);
        newCurrentPath.add(currentVertex);

        for (Vertex neighbourVertex:currentVertex.getNeighbours()) {
            // If this Vertex has already been traversed, skip it
            if (newCurrentPath.contains(neighbourVertex)) continue;

            // Calculating the distance to the new Vertex
            distance = distance + currentVertex.calculateDistanceToPoint(neighbourVertex);

            addDistanceToNextVertex(neighbourVertex,distance,newCurrentPath);

        }

        // If we've ended up at an end-point Vertex and the distance travelled is longer than the previously-assigned
        // longest distance assign this as the longest path
        if (currentVertex.getNumberOfNeighbours() == 1 && distance > longestDistance) {
            longestDistance = distance;
            longestPath = newCurrentPath;
        }
    }

    @Override
    public boolean remove(Object o) {
        Vertex vertex = (Vertex) o;

        // Removing this as a neighbour of its neighbours
        for (Vertex neighbour:vertex.getNeighbours()) {
            neighbour.removeNeighbour(vertex);
        }

        // Removing from the collection
        return super.remove(vertex);

    }
}
