package signalprocessor.voronoi.representation.triangulation;

public class VHalfEdge {

    /* ***************************************************** */
    // Variables
    
    public int vertexnumber;
    public VHalfEdge next;
    
    public VVertex vertex;
    
    private int length = -1;
    
    public boolean shownonminimumspanningtree = false;
    
    
    /* ***************************************************** */
    // Constructors
    
    public VHalfEdge(int _vertexnumber, VVertex _vertex) {
        this.vertexnumber = _vertexnumber;
        this.vertex = _vertex;
    }
    
    public VHalfEdge(int _vertexnumber, VVertex _vertex, VHalfEdge _next) {
        this.vertexnumber = _vertexnumber;
        this.vertex = _vertex;
        this.next = _next;
    }

    /* ***************************************************** */
    // Methods

    public boolean isOuterEdge() {
        return ( vertexnumber==TriangulationRepresentation.OUTER_VERTEXNUMBER );
    }
    
    public VVertex getConnectedVertex() {
        return next.vertex;
    }
    
    public int getLength() {
        if ( length==-1 ) {
            length = (int) vertex.distanceTo(next.vertex);
        }
        return length;
    }
    
    public int getX() {
        return vertex.x;
    }
    
    public int getY() {
        return vertex.y;
    }
    
    /* ***************************************************** */
}
