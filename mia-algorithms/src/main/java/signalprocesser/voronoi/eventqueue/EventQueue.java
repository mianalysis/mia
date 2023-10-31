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
import java.util.Collection;
import java.util.Comparator;
import java.util.TreeMap;

public class EventQueue {
    
    /* ********************************************************* */
    // Constants
    
    private static final Comparator PRIORITY_COMPARATOR = new Comparator<VEvent>() {
        public int compare(VEvent p1, VEvent p2) {
            if (p1.getY() < p2.getY())      return -1;
            else if (p1.getY() > p2.getY()) return 1;
            else if (p1.getX() < p2.getX()) return -1;
            else if (p1.getX() > p2.getX()) return 1;
            else if (p1 == p2             ) return 0;
            else {
                // In situation where we have two different events
                //  both at the same coordinate - for site events the same,
                //  treat as equal (i.e. one will be deleted), otherwise treat
                //  as different (with Site Events going first)
                if ( p1.isSiteEvent() && p2.isSiteEvent() ) {
                    return 0;
                } else if ( p1.isCircleEvent() && p2.isCircleEvent() ) {
                    if ( p1.id < p2.id ) return -1;
                    else if ( p1.id > p2.id ) return 1;
                    else return 0;
                } else if ( p1.isSiteEvent() ) {
                    return -1;
                } else {
                    return 1;
                }
            }
        }
    };
    
    /* ********************************************************* */
    // Variables
    
    private TreeMap<VEvent,VEvent> queue;
    
    /* ********************************************************* */
    // Constructor
    
    public EventQueue() {
        queue = new TreeMap<VEvent,VEvent>(PRIORITY_COMPARATOR);
    }
    
    public EventQueue(Collection<VEvent> events) {
        this();
        
        for ( VEvent event : events ) {
            queue.put( event , event );
        }
    }
    
    /* ********************************************************* */
    // Methods
    
    public void addEvent(VEvent event) {
        queue.put(event, event);
    }
    
    public boolean removeEvent(VEvent event) {
        return ( queue.remove(event)!=null );
    }
    
    public VEvent getFirstEvent() {
        if ( queue.size()>0 ) {
            return queue.firstKey();
        } else {
            return null;
        }
    }
    
    public VEvent getAndRemoveFirstEvent() {
        VEvent event = queue.firstKey();
        queue.remove(event);
        return event;
    }
    
    public boolean isEventQueueEmpty() {
        return queue.isEmpty();
    }
    
    /* ********************************************************* */
}