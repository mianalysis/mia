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

package signalprocesser.voronoi.representation.triangulation;
import java.util.ArrayList;

import signalprocesser.voronoi.VPoint;

public class VVertex extends VPoint {
    
    /* ***************************************************** */
    // Static Variables
    
    public static int uniqueid = 1;
    
    /* ***************************************************** */
    // Variables
    
    public int id = uniqueid++;
    
    private ArrayList<VHalfEdge> edges;
    
    /* ***************************************************** */
    // Constructors
    
    public VVertex() { super(); }
    public VVertex(int x, int y) { super(x, y); }
    public VVertex(VPoint point) { super(point); }
    
    /* ***************************************************** */
    // Edge Methods
    
    public boolean hasEdges() {
        return ( edges!=null && edges.size()>0 );
    }
    public void clearEdges() {
        if ( edges!=null ) {
            edges.clear();
        }
    }
    public void addEdge(VHalfEdge edge) {
        if ( edges==null ) {
            edges = new ArrayList<VHalfEdge>();
        }
        edges.add( edge );
    }
    public ArrayList<VHalfEdge> getEdges() {
        if ( edges==null || edges.size()<=0 ) {
            return null;
        } else {
            return edges;
        }
    }
    public boolean removeEdge(VHalfEdge edge) {
        if ( edges==null ) {
            return false;
        }
        return edges.remove( edge );
    }
    
    /* ***************************************************** */
    // Calculate Distance to Vertex method
    
    public double distanceTo(VVertex distance) {
        return Math.sqrt( (x-distance.x)*(x-distance.x) + (y-distance.y)*(y-distance.y) );
    }
    
    /* ***************************************************** */
    // Helper methods

    public VHalfEdge getEdge(VVertex connectedtovertex) {
        if ( edges==null || edges.size()<=0 ) {
            return null;
        } else {
            for ( VHalfEdge edge : edges ) {
                if ( edge.next!=null && edge.next.vertex==connectedtovertex ) {
                    return edge;
                }
            }
            return null;
        }
    }
    public VHalfEdge getEdge(int vertexnumber) {
        if ( edges==null || edges.size()<=0 ) {
            return null;
        } else {
            for ( VHalfEdge edge : edges ) {
                // Don't actually need .next in edge.next.vertexnumber, as
                //  edge.next.vertexnumber==edge.vertexnumber as long as the
                //  system is in a valid state.
                if ( edge.next!=null && edge.next.vertexnumber==vertexnumber ) {
                    return edge;
                }
            }
            return null;
        }
    }
    
    public boolean isConnectedTo(VVertex connectedtovertex) {
        VHalfEdge edge = getEdge(connectedtovertex);
        return ( edge!=null );
    }
    
    /* ***************************************************** */
    // toString() Methods
    
    public String toString() {
        return "VVertex (connected to " + getConnectedVertexString() + ")";
    }
    public String getConnectedEdgeString() {
        if ( edges==null || edges.size()<=0 ) {
            return null;
        } else {
            String str = null;
            for ( VHalfEdge edge : edges ) {
                if ( str==null ) {
                    str  = "" + edge.vertexnumber;
                } else {
                    str += ", " + edge.vertexnumber;
                }
            }
            return str;
        }
    }
    public String getConnectedVertexString() {
        if ( edges==null || edges.size()<=0 ) {
            return null;
        } else {
            String str = null;
            for ( VHalfEdge edge : edges ) {
                if ( str==null ) {
                    str  = "" + edge.getConnectedVertex().id;
                } else {
                    str += ", " + edge.getConnectedVertex().id;
                }
            }
            return str;
        }
    }
    
    /* ***************************************************** */
}
