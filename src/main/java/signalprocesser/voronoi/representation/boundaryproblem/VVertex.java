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

package signalprocesser.voronoi.representation.boundaryproblem;
import java.util.ArrayList;

import signalprocesser.voronoi.VPoint;

public class VVertex extends VPoint {
    
    public static int uniqueid = 1;
    
    public int id = uniqueid++;
    
    private ArrayList<VHalfEdge> connectedvertexs;
    
    public VVertex() { super(); }
    public VVertex(int x, int y) { super(x, y); }
    public VVertex(VPoint point) { super(point); }
    
    public void clearConnectedVertexs() {
        if ( connectedvertexs!=null ) {
            connectedvertexs.clear();
        }
    }
    
    public void addConnectedVertex(VHalfEdge edge) {
        if ( connectedvertexs==null ) {
            connectedvertexs = new ArrayList<VHalfEdge>();
        }
        connectedvertexs.add( edge );
    }
    
    public ArrayList<VHalfEdge> getConnectedVertexs() {
        if ( connectedvertexs==null || connectedvertexs.size()<=0 ) {
            return null;
        } else {
            return connectedvertexs;
        }
    }
    
    public double distanceTo(VVertex distance) {
        return Math.sqrt( (x-distance.x)*(x-distance.x) + (y-distance.y)*(y-distance.y) );
    }
    
    public VHalfEdge getNextConnectedEdge(int vertexnumber) {
        if ( connectedvertexs==null || connectedvertexs.size()<=0 ) {
            return null;
        } else {
            for ( VHalfEdge edge : connectedvertexs ) {
                if ( edge.vertexnumber==vertexnumber ) {
                    return edge;
                }
            }
            return null;
        }
    }
    public VHalfEdge getNextConnectedEdge(VVertex nextvertex) {
        if ( connectedvertexs==null || connectedvertexs.size()<=0 ) {
            return null;
        } else {
            for ( VHalfEdge edge : connectedvertexs ) {
                if ( edge.vertex==nextvertex ) {
                    return edge;
                }
            }
            return null;
        }
    }
    
    public VVertex getNextConnectedVertex(int vertexnumber) {
        VHalfEdge edge = getNextConnectedEdge(vertexnumber);
        return ( edge==null ? null : edge.vertex );
    }
    
    public String getConnectedVertexString() {
        String str = null;
        for ( VHalfEdge edge : connectedvertexs ) {
            if ( str==null ) {
                str  = "" + edge.vertexnumber;
            } else {
                str += ", " + edge.vertexnumber;
            }
        }
        return str;
    }
}
