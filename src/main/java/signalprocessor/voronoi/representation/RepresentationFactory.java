package signalprocessor.voronoi.representation;

import java.util.ArrayList;

import signalprocessor.voronoi.VPoint;
import signalprocessor.voronoi.representation.triangulation.TriangulationRepresentation;
import signalprocessor.voronoi.representation.voronoicell.VVoronoiCell;

public class RepresentationFactory {
    
    // Don't allow to be instantiated
    private RepresentationFactory() { }
    
    /* ***************************************************** */
    // Create Representation Methods
    
    
    public static AbstractRepresentation createTriangulationRepresentation() {
        return new TriangulationRepresentation();
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
        
    public static ArrayList<VPoint> convertPointsToSimpleTriangulationPoints(ArrayList<VPoint> points) {
        return convertPointsToVPoints( points );
    }
    
    /* ***************************************************** */
}
