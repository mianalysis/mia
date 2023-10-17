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

package signalprocesser.voronoi.statusstructure.binarysearchtreeimpl;

import signalprocesser.voronoi.eventqueue.VSiteEvent;
import signalprocesser.voronoi.statusstructure.VLinkedNode;

public class VLeafNode extends VLinkedNode implements VNode {
    
    /* ***************************************************** */
    // Variables
    
    public int id = BSTStatusStructure.uniqueid++;
    protected VInternalNode parent;
    
    /* ***************************************************** */
    // Constructor
    
    protected VLeafNode() { }
    public VLeafNode(VSiteEvent _siteevent) {
        super( _siteevent );
    }
    
    /* ***************************************************** */
    // Methods
    
    public VInternalNode getParent() { return parent; }
    public void setParent(VInternalNode _parent) {
        this.parent = _parent;
    }
    
    public boolean isLeafNode() { return true; }
    public boolean isInternalNode() { return false; }
    
    public VLeafNode cloneLeafNode() {
        VLeafNode clone = new VLeafNode(this.siteevent);
        // DO NOT DUPLICATE prev/next values
        // DO NOT DUPLICATE circle events???? (?!)
        return clone;
    }
    
    public VInternalNode getFirstCommonParent(VLeafNode othernode) {
        VInternalNode parent1 = parent;
        VInternalNode parent2 = othernode.parent;
        int depth1 = parent.getDepth();
        int depth2 = othernode.parent.getDepth();
        
        // Go up until at equal depths
        if ( depth1 > depth2 ) {
            do {
                depth1--;
                parent1 = parent1.getParent();
            } while ( depth1 > depth2 );
        } else if ( depth2 > depth1 ) {
            do {
                depth2--;
                parent2 = parent2.getParent();
            } while ( depth2 > depth1 );
        }
        
        // Find common parent from common depth
        while ( parent1!=parent2 ) {
            parent1 = parent1.getParent();
            parent2 = parent2.getParent();
        }

        // Return common parent
        return parent1;
    }
    
    
    /* ***************************************************** */
    // To String Method
    
    public String toString() {
        return "VLeafNode" + id + " (" + siteevent + ")";
    }
    
    /* ***************************************************** */
}
