package signalprocessor.voronoi.statusstructure;


import java.awt.Rectangle;
import java.awt.Graphics2D;

import java.util.*;
import javax.swing.*;

import signalprocessor.voronoi.eventqueue.EventQueue;
import signalprocessor.voronoi.eventqueue.VSiteEvent;
import signalprocessor.voronoi.statusstructure.binarysearchtreeimpl.BSTStatusStructure;

abstract public class AbstractStatusStructure {
    
    /* ***************************************************** */
    // Static Entry Point
    
    public static AbstractStatusStructure createDefaultStatusStructure() {
        return new BSTStatusStructure();
        //return new DLinkedListStatusStructure();
    }
    
    /* ***************************************************** */
    // General Methods
    
    abstract public boolean isStatusStructureEmpty();
    
    abstract public void setRootNode( VSiteEvent siteevent );
    
    abstract public VLinkedNode insertNode( VLinkedNode nodetosplit, VSiteEvent siteevent );
    
    abstract public void removeNode(EventQueue eventqueue, VLinkedNode toremove);
    
    public VLinkedNode getNodeAboveSiteEvent( VSiteEvent siteevent , int sweepline ) {
        return getNodeAboveSiteEvent(siteevent.getX(), sweepline);
    }
    abstract public VLinkedNode getNodeAboveSiteEvent( int siteevent_x , int sweepline);
    
    abstract public VLinkedNode getHeadNode();
    
    /* ***************************************************** */
    // Debug print() Method
    
    public void print(Graphics2D g, VSiteEvent siteevent, int sweepline) {
        
    }
    
    /* ***************************************************** */
}
