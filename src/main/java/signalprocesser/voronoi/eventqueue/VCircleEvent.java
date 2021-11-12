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

package signalprocesser.voronoi.eventqueue;
import signalprocesser.voronoi.statusstructure.VLinkedNode;

public class VCircleEvent extends VEvent {
    
    /* ***************************************************** */
    // Variables

    // X/Y Coordinate for bottom of circle
    private int x;
    private int y;
    
    // Note: y corresponds to the lowest most point of the circle
    //   whereas center_y is the center of the circle.
    private int center_y;
    
    public VLinkedNode leafnode;
    
    /* ***************************************************** */
    // Constructors
    
    public VCircleEvent() {
        super();
    }
    public VCircleEvent(int _x, int _y) {
        this.x = _x;
        this.y = _y;
    }
    
    /* ***************************************************** */
    // Abstract Methods
    
    public int getX() { return this.x; }
    public void setX(int _x) {
        this.x = _x;
    }
    
    public int getY() { return this.y; }
    public void setY(int _y) {
        this.y = _y;
    }
    
    public int getCenterY() { return this.center_y; }
    public void setCenterY(int _center_y) {
        this.center_y = _center_y;
    }
    
    public boolean isSiteEvent() { return false; }
    
    public boolean isCircleEvent() { return true; }
    

    /* ***************************************************** */
    // To String Method
    
    public String toString() {
        return "VCircleEvent (" + x + "," + y + ")";
    }
    
    /* ***************************************************** */
}
