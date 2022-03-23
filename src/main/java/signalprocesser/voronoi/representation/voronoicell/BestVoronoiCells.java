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

package signalprocesser.voronoi.representation.voronoicell;

import java.util.Collection;

import signalprocesser.voronoi.VPoint;

public class BestVoronoiCells {
    
    /* ***************************************************** */
    // Variables
    
    private int numberstored;
    
    private double[]       bestareas;
    private VVoronoiCell[] bestcells;
    
    /* ***************************************************** */
    // Constructor
    
    public BestVoronoiCells(int number) {
        bestareas = new double[number];
        bestcells = new VVoronoiCell[number];
    }
    public BestVoronoiCells(int number, Collection<VPoint> voronoicells) {
        this(number);
        findBest(voronoicells);
    }
    
    /* ***************************************************** */
    // Getters
    
    public int getBestStored() {
        return numberstored;
    }
    
    public double getBestArea(int index) {
        return bestareas[index];
    }
    
    public VVoronoiCell getBestCell(int index) {
        return bestcells[index];
    }
    
    public double getTotalAreaOfBest() {
        double sum = 0;
        for ( int x=0 ; x<numberstored ; x++ ) sum += bestareas[x];
        return sum;
    }

    public double getAverageArea() {
        double sum = 0;
        if ( numberstored==0 ) return -1.0;
        for ( int x=0 ; x<numberstored ; x++ ) sum += bestareas[x];
        return sum/((double)numberstored);
    }
    
    public int getAverageX() {
        int tmp = 0;
        if ( numberstored==0 ) return -1;
        for ( int x=0 ; x<numberstored ; x++ ) tmp += bestcells[x].x;
        return tmp/numberstored;
    }
    
    public int getAverageY() {
        int tmp = 0;
        if ( numberstored==0 ) return -1;
        for ( int x=0 ; x<numberstored ; x++ ) tmp += bestcells[x].y;
        return tmp/numberstored;
    }
    
    /* ***************************************************** */
    // Find Best method
    
    public void findBest(Collection<VPoint> voronoicells) {
        // Collect results
        numberstored = 0;
        double area;
        int index, tmp;
        VVoronoiCell cell;
        for ( VPoint point : voronoicells ) {
            // Get cell and cell area
            cell = (VVoronoiCell) point;
            area = cell.getAreaOfCell();
            
            // If area not given, then continue
            if ( area<0 ) continue;
            
            // Consider if best
            if ( numberstored==0 ) {
                numberstored = 1;
                bestareas[0] = area;
                bestcells[0] = cell;
            } else {
                // Find index where to insert
                index = numberstored;
                while ( bestareas[index-1]>area ) {
                    if ( index>1 ) {
                        index--;
                        continue;
                    }
                    
                    // Otherwise, insert in first position and break
                    index = 0;
                    break;
                }
                
                // Only insert if would fit in our structure
                if ( index<bestareas.length ) {
                    // Setup for next
                    if ( numberstored<bestareas.length ) {
                        tmp = numberstored;
                        
                        // Increment for next (when value inserted)
                        numberstored++;
                    } else {
                        tmp = bestareas.length - 1;
                    }
                    
                    // Shift everything right of index
                    for (   ; tmp>index ; tmp-- ) {
                        bestareas[tmp] = bestareas[tmp-1];
                        bestcells[tmp] = bestcells[tmp-1];
                    }
                    
                    // Add new values to index
                    bestareas[index] = area;
                    bestcells[index] = cell;
                }
            }
        }
        
        // Clear remaining values
        for ( tmp=numberstored ; tmp<bestareas.length ; tmp++ ) {
            bestareas[tmp] = 0.0;
            bestcells[tmp] = null;
        }
    }
    
    /* ***************************************************** */
}
