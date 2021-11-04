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

package signalprocesser.voronoi.statusstructure.doublelinkedlistimpl;

import signalprocesser.voronoi.VoronoiShared;
import signalprocesser.voronoi.eventqueue.EventQueue;
import signalprocesser.voronoi.eventqueue.VSiteEvent;
import signalprocesser.voronoi.statusstructure.AbstractStatusStructure;
import signalprocesser.voronoi.statusstructure.VLinkedNode;

/**
 * Implementation *not* working
 */
public class DLinkedListStatusStructure extends AbstractStatusStructure {
    
    /* ***************************************************** */
    // Variables
    
    private VLinkedNode head = null;
    
    /* ***************************************************** */
    // Core Methods
    
    public boolean isStatusStructureEmpty() {
        return ( head==null );
    }
    
    public void setRootNode( VSiteEvent siteevent ) {
        setRootNode( new VLinkedNode(siteevent) );
    }
    protected void setRootNode( VLinkedNode node ) {
        head = node;
    }
    
    public VLinkedNode insertNode(VLinkedNode nodetosplit, VSiteEvent siteevent) {
        VLinkedNode newnode = new VLinkedNode(siteevent);
        
        // Prepare to link new node into linked list...
        VLinkedNode leaf1 = nodetosplit;
        VLinkedNode leaf3 = nodetosplit.cloneLinkedNode();
        VLinkedNode tmp = nodetosplit.getNext();
        
        // Set next variables appropriately (each call sets prev value as well)
        leaf1.setNext( newnode );
        newnode.setNext( leaf3 );
        leaf3.setNext( tmp );
        
        // Return the newly create node
        return newnode;
    }
    
    public void removeNode(EventQueue eventqueue, VLinkedNode toremove) {
        // Unlink Double-Linked List Structure
        if ( toremove.getPrev()==null ) {
            toremove.setNext(null);
        } else {
            toremove.getPrev().setNext( toremove.getNext() );
        }
    }
    
    public VLinkedNode getNodeAboveSiteEvent( int siteevent_x , int sweepline ) {
        if ( head==null ) { return null; }
        //if ( head.getNext()==null ) { return head; }
        
        VLinkedNode curr = head;
        //curr.siteevent.calcParabolaConstants(sweepline);
        while ( curr.getNext()!=null ) {
            VSiteEvent v1 = head.siteevent;
            VSiteEvent v2 = head.getNext().siteevent;
            
            //if ( sweepline>v1.x && sweepline>v2.x ) {
                // Calculate parabolic constants
                v1.calcParabolaConstants(sweepline);
                v2.calcParabolaConstants(sweepline);
                
                // Determine where two parabola meet
                double intersects[] = VoronoiShared.solveQuadratic(v1.a-v2.a, v1.b-v2.b, v1.c-v2.c);
                //double intersects[] = VoronoiShared.solveQuadratic(v2.a-v1.a, v2.b-v1.b, v2.c-v1.c);
                if (!( intersects[0] <= siteevent_x && intersects[0]!=intersects[1] )) {
                    return curr;
                }
            //}
            
            curr=curr.getNext();
        }
        
        return curr;
    }
    
    // Function only used by test functions
    public VLinkedNode getHeadNode() {
        return head;
    }
    
    /* ***************************************************** */
    // Debug toString() Method
    
    public String toString() {
        VLinkedNode node = getHeadNode();
        if ( node==null ) {
            return "| Doubly-linked list is empty";
        } else {
            StringBuffer buffer = new StringBuffer();
            buffer.append("| ");
            boolean isfirst = true;
            do {
                if ( isfirst ) {
                    isfirst = false;
                } else {
                    buffer.append(" -> ");
                }
                buffer.append("Node (" + node.siteevent.getX() + "," + node.siteevent.getY() + ") #" + node.siteevent.getID());
            } while ( (node=node.getNext())!=null );
            return buffer.toString();
        }
    }
    
    /* ***************************************************** */
}
