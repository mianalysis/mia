package signalprocessor.voronoi;

public class VPoint {
    
    /* ***************************************************** */
    // Variables
    
    public int x;
    public int y;
    
    /* ***************************************************** */
    // Constructors
    
    public VPoint() {
        this(-1, -1);
    }
    public VPoint(int _x, int _y) {
        this.x = _x;
        this.y = _y;
    }
    public VPoint(VPoint point) {
        this.x = point.x;
        this.y = point.y;
    }
    
    public double distanceTo(VPoint point) {
        return Math.sqrt((this.x-point.x)*(this.x-point.x) + (this.y-point.y)*(this.y-point.y));
    }
    
    public String toString() {
        return "VPoint (" + x + "," + y + ")";
    }
    
    /* ***************************************************** */
}
