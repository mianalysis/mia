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

import java.util.Collection;

import signalprocesser.voronoi.VPoint;
import signalprocesser.voronoi.statusstructure.VLinkedNode;

public interface RepresentationInterface {
    
    // Executed before the algorithm begins to process (can be used to
    //   initialise any data structures required)
    public void beginAlgorithm(Collection<VPoint> points);
    
    // Called to record various events
    public void siteEvent( VLinkedNode n1 , VLinkedNode n2 , VLinkedNode n3 );
    public void circleEvent( VLinkedNode n1 , VLinkedNode n2 , VLinkedNode n3 , int circle_x , int circle_y );
    
    // Called when the algorithm has finished processing
    public void endAlgorithm(Collection<VPoint> points, int lastsweeplineposition, VLinkedNode headnode);
    
}
