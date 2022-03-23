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

import java.awt.Dimension;
import java.util.ArrayList;

import javax.swing.AbstractListModel;
import javax.swing.ComboBoxModel;
import javax.swing.JComboBox;

public class CountryListModel extends AbstractListModel implements ComboBoxModel {
    
    /* ***************************************************** */
    // Variables
    
    private JComboBox combobox;
    
    private String selectedcountry;
    private ArrayList<String> countries;
    
    /* ***************************************************** */
    // Constructor
    
    public CountryListModel(JComboBox _combobox, ArrayList<String> _countries) {
        this.combobox = _combobox;
        this.countries = _countries;
        if ( countries.size()>=1 ) {
            this.selectedcountry = formatHumanReadable( countries.get(0) );
        }
        
        // Set an appriopriate render
        combobox.setRenderer( new CountryListRender() );
    }
    
    /* ***************************************************** */
    // Methods
    
    public int getSize() {
        return countries.size();
    }
    public Object getElementAt(int index) {
        return formatHumanReadable( countries.get(index) );
    }
    public Object getSelectedItem() {
        return ( selectedcountry==null ? null : selectedcountry );
    }
    public void setSelectedItem(Object _selectedcountry) {
        this.selectedcountry = (String) _selectedcountry;
    }
    
    public String getSelectedCountry() {
        int index = combobox.getSelectedIndex();
        if ( index<0 || index>=countries.size() ) {
            return null;
        } else {
            return countries.get( index );
        }
    }
    
    /* ***************************************************** */
    // Private method to nicely format filename
    //  (i.e. to turn "Some_Country.txt" --> "Some Country")
    
    public static String formatHumanReadable(String filename) {
        int index = filename.lastIndexOf('.');
        if ( index>0 ) {
            filename = filename.substring(0, index);
        }
        return filename.replace('_', ' ');
    }
    
    /* ***************************************************** */
    // Combobox Render that doesn't care about an oversided string
    
    public static class CountryListRender extends javax.swing.plaf.basic.BasicComboBoxRenderer {
        public Dimension getSize() {
            Dimension dimension = super.getSize();
            dimension.width = -1;
            return dimension;
        }
        public Dimension getPreferredSize() {
            Dimension dimension = super.getPreferredSize();
            dimension.width = -1;
            return dimension;
        }
    }
    
    /* ***************************************************** */
}