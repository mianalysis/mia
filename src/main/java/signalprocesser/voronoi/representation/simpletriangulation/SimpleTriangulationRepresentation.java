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

package signalprocesser.voronoi.representation.simpletriangulation;

import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.Collection;

import signalprocesser.voronoi.VPoint;
import signalprocesser.voronoi.representation.AbstractRepresentation;
import signalprocesser.voronoi.statusstructure.VLinkedNode;

public class SimpleTriangulationRepresentation extends AbstractRepresentation {

    /* ***************************************************** */
    // Variables
    
    private final ArrayList<VTriangle> triangles = new ArrayList<VTriangle>();
    
    /* ***************************************************** */
    // Constructor
    
    public SimpleTriangulationRepresentation() {
        // do nothing
    }

    /* ***************************************************** */
    // Create Point

    public VPoint createPoint(int x, int y) {
        return new VPoint(x, y);
    }
    
    /* ***************************************************** */
    // Data/Representation Interface Method
    
    // Executed before the algorithm begins to process (can be used to
    //   initialise any data structures required)
    public void beginAlgorithm(Collection<VPoint> points) {
        // Reset the triangle array list
        triangles.clear();
    }
    
    // Called to record that a vertex has been found
    public void siteEvent( VLinkedNode n1 , VLinkedNode n2 , VLinkedNode n3 ) { }
    public void circleEvent( VLinkedNode n1 , VLinkedNode n2 , VLinkedNode n3 , int circle_x , int circle_y ) {
        VTriangle triangle = new VTriangle(circle_x, circle_y);
        triangle.p1 = n1.siteevent.getPoint();
        triangle.p2 = n2.siteevent.getPoint();
        triangle.p3 = n3.siteevent.getPoint();
        triangles.add( triangle );
    }
    
    // Called when the algorithm has finished processing
    public void endAlgorithm(Collection<VPoint> points, int lastsweeplineposition, VLinkedNode headnode) {
        // do nothing
    }
    
    /* ***************************************************** */    
    // Paint Method
    
    public void paint(Graphics2D g) {
        for ( VTriangle triangle : triangles ) {
            g.drawLine( triangle.p1.x , triangle.p1.y , triangle.p2.x , triangle.p2.y );
            g.drawLine( triangle.p2.x , triangle.p2.y , triangle.p3.x , triangle.p3.y );
            g.drawLine( triangle.p3.x , triangle.p3.y , triangle.p1.x , triangle.p1.y );
        }
    }
    
    /* ***************************************************** */    
}
