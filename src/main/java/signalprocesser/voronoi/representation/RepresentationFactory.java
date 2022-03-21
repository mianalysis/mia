/*
 * "Concave" hulls by Glenn Hudson and Matt Duckham
 *
 * Source code downloaded from https://archive.md/l3Un5#selection-571.0-587.218 on 3rd November 2021.
 *
 * - This software is Copyright (C) 2008 Glenn Hudson released under Gnu Public License (GPL). Under 
 *   GPL you are free to use, modify, and redistribute the software. Please acknowledge Glenn Hudson 
 *   and Matt Duckham as the source of this software if you do use or adapt the code in further research 
 *   or other work. For full details of GPL see http://www.gnu.org/licenses/gpl-3.0.txt.
 * - This software comes with no warranty of any kind, expressed or implied.
 * 
 * A paper with full details of the characteristic hulls algorithm is published in Pattern Recognition.
 * Duckham, M., Kulik, L., Worboys, M.F., Galton, A. (2008) Efficient generation of simple polygons for
 * characterizing the shape of a set of points in the plane. Pattern Recognition v41, 3224-3236
 *
 * The software was developed by Glenn Hudson while working with me as an RA. The characteristic shapes 
 * algorithm is collaborative work between Matt Duckham, Lars Kulik, Antony Galton, and Mike Worboys.
 * 
 */

package signalprocesser.voronoi.representation;

import java.util.ArrayList;

import signalprocesser.voronoi.VPoint;
import signalprocesser.voronoi.representation.boundaryproblem.BoundaryProblemRepresentation;
import signalprocesser.voronoi.representation.simpletriangulation.SimpleTriangulationRepresentation;
import signalprocesser.voronoi.representation.triangulation.TriangulationRepresentation;
import signalprocesser.voronoi.representation.voronoicell.VVoronoiCell;
import signalprocesser.voronoi.representation.voronoicell.VoronoiCellRepresentation;

public class RepresentationFactory {
    
    // Don't allow to be instantiated
    private RepresentationFactory() { }
    
    /* ***************************************************** */
    // Create Representation Methods
    
    public static AbstractRepresentation createVoronoiCellRepresentation() {
        return new VoronoiCellRepresentation();
    }
    
    public static AbstractRepresentation createTriangulationRepresentation() {
        return new TriangulationRepresentation();
    }
    
    public static AbstractRepresentation createSimpleTriangulationRepresentation() {
        return new SimpleTriangulationRepresentation();
    }
    
    public static AbstractRepresentation createBoundaryProblemRepresentation() {
        return new BoundaryProblemRepresentation();
    }
    
    /* ***************************************************** */
    // Conversion Methods
    
    public static ArrayList<VPoint> convertPointsToVPoints(ArrayList<VPoint> points) {
        ArrayList<VPoint> newarraylist = new ArrayList<VPoint>();
        for ( VPoint point : points ) {
            newarraylist.add( new VPoint(point) );
        }
        return newarraylist;
    }
    
    public static ArrayList<VPoint> convertPointsToVoronoiCellPoints(ArrayList<VPoint> points) {
        ArrayList<VPoint> newarraylist = new ArrayList<VPoint>();
        for ( VPoint point : points ) {
            newarraylist.add( new VVoronoiCell(point) );
        }
        return newarraylist;
    }
    
    public static ArrayList<VPoint> convertPointsToTriangulationPoints(ArrayList<VPoint> points) {
        signalprocesser.voronoi.representation.triangulation.VVertex.uniqueid = 1;
        ArrayList<VPoint> newarraylist = new ArrayList<VPoint>();
        for ( VPoint point : points ) {
            newarraylist.add( new signalprocesser.voronoi.representation.triangulation.VVertex(point) );
        }
        return newarraylist;
    }
    
    public static ArrayList<VPoint> convertPointsToSimpleTriangulationPoints(ArrayList<VPoint> points) {
        return convertPointsToVPoints( points );
    }
    
    public static ArrayList<VPoint> convertPointsToBoundaryProblemPoints(ArrayList<VPoint> points) {
        ArrayList<VPoint> newarraylist = new ArrayList<VPoint>();
        for ( VPoint point : points ) {
            newarraylist.add( new signalprocesser.voronoi.representation.boundaryproblem.voronoicell.VVoronoiCell(point) );
        }
        return newarraylist;
    }
    
    /* ***************************************************** */
}
