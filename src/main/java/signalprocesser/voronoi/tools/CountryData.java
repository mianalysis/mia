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

package signalprocesser.voronoi.tools;
import java.awt.Rectangle;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import signalprocesser.voronoi.VPoint;

public class CountryData {
    
    public static final String PATH_TO_COUNTRYDATA = "countrydata";
    public static final String INDEX_FILE          = PATH_TO_COUNTRYDATA + "/fileindex.dat";
    
    public static final Pattern PATTERN_COORDLINE = Pattern.compile("^[ ]*([0-9E.-]*)[ ]*,[ ]*([0-9E.-]*)[ ]*$");
    
    public static void main(String args[]) throws IOException, URISyntaxException {
        // Call country data generation to generate mapping data
        //CountryDataGeneration.main(args);
    }
    
    public static ArrayList<String> getCountryList() throws IOException {
        // Open the index file
        InputStream stream = CountryData.class.getResourceAsStream(INDEX_FILE);
        if ( stream==null ) {
            throw new FileNotFoundException("The resource \"" + INDEX_FILE + "\" was not found relative to CountryData.class" );
        }
        BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
        
        // Read the country files available
        String line;
        ArrayList<String> countrylist = new ArrayList<String>();
        while ( (line=reader.readLine())!=null ) {
            countrylist.add(line);
        }
        
        // Return the generated list
        return countrylist;
    }
    
    public static ArrayList<VPoint> getCountryData(String countryfile, Rectangle bounds) throws IOException {
        // Open country file requested
        String resourcename = PATH_TO_COUNTRYDATA + "/" + countryfile;
        InputStream stream = CountryData.class.getResourceAsStream(resourcename);
        if ( stream==null ) {
            stream = CountryData.class.getResourceAsStream(resourcename + ".txt");
            if ( stream==null ) {
                throw new FileNotFoundException("The resource \"" + resourcename + "\" (also tried with \".txt\" extension) was not found relative to CountryData.class");
            }
        }
        BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
        
        // Parse content in file
        String line;
        int linenumber=0;
        boolean isfirst = true;
        ArrayList<VPoint> points = new ArrayList<VPoint>();
        int min_x = -1, max_x = -1;
        int min_y = -1, max_y = -1;
        while ( (line=reader.readLine())!=null ) {
            // Increment line number
            linenumber++;
            
            // Attempt to match line
            Matcher matcher = PATTERN_COORDLINE.matcher(line);
            if ( matcher.matches()==false ) {
                throw new IOException("(line " + linenumber + ") Line doesn't match expected format - \"" + line + "\"");
            }
            
            // Get coordinates on line
            int x = (int) Double.parseDouble(matcher.group(1));
            int y = (int) Double.parseDouble(matcher.group(2));
            
            // Determine if min/max
            if ( isfirst ) {
                isfirst = false;
                min_x = x; max_x = x;
                min_y = y; max_y = y;
            } else {
                // Determine if min/max x value
                if ( x<min_x ) min_x = x;
                else if ( x>max_x ) max_x = x;
                
                // Determine if min/max y value
                if ( y<min_y ) min_y = y;
                else if ( y>max_y ) max_y = y;
            }
            
            // Add to array
            points.add( new VPoint(x, y) );
        }
        
        // Scale so as within bounds requested
        double scaleby;
        int margin_top, margin_left;
        if ( (double)(max_x-min_x)/(double)(bounds.width) >
                (double)(max_y-min_y)/(double)(bounds.height) ) {
            scaleby     = (double)(bounds.width)  / (double)(max_x-min_x);
            margin_top  = (int)( ((double)(bounds.height)-(double)(max_y-min_y)*scaleby) / 2.0 );
            margin_left = 0;
        } else {
            scaleby     = (double)(bounds.height) / (double)(max_y-min_y);
            margin_top  = 0;
            margin_left = (int)( ((double)(bounds.width)-(double)(max_x-min_x)*scaleby) / 2.0 );
        }
        
        // Scale all points appropriately
        Iterator<VPoint> iter = points.iterator();
        VPoint prevpoint = null;
        while ( iter.hasNext() ) {
            VPoint point = iter.next();
            point.x = bounds.x + margin_left + (int)( (double)(point.x - min_x) * scaleby );
            point.y = bounds.y + margin_top  + (int)( (double)(point.y - min_y) * scaleby );
            
            // Remove this point if the same as the previous one
            if ( prevpoint!=null && point.x==prevpoint.x && point.y==prevpoint.y ) {
                iter.remove();
            } else {
                prevpoint = point;
            }
        }
        
        // Close the shape if it is not closed
        if ( points.size()>=1 ) {
            VPoint first = points.get(0);
            if (!( first.x==prevpoint.x && first.y==prevpoint.y )) {
                points.add( first );
            }
        }
        
        // Return these points
        return points;
    }
    
}
