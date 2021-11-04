package signalprocessor.voronoi.representation;
import signalprocessor.voronoi.VPoint;

import java.awt.Graphics2D;

abstract public class AbstractRepresentation implements RepresentationInterface {
    
    public AbstractRepresentation() {
    }

    public abstract VPoint createPoint(int x, int y);
            
    public abstract void paint(Graphics2D g);
    
}
