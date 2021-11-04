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

import java.awt.Component;
import java.awt.Font;
import java.awt.Rectangle;
import java.awt.geom.Area;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InvalidClassException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JProgressBar;
import javax.swing.UIManager;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableColumn;

import signalprocesser.shared.StatusDialog;
import signalprocesser.voronoi.VPoint;
import signalprocesser.voronoi.VoronoiAlgorithm;
import signalprocesser.voronoi.VoronoiShared;
import signalprocesser.voronoi.representation.RepresentationFactory;
import signalprocesser.voronoi.representation.triangulation.TriangulationRepresentation;
import signalprocesser.voronoi.shapegeneration.ShapeGeneration;
import signalprocesser.voronoi.shapegeneration.ShapeGenerationException;

public class TestSuite extends javax.swing.JDialog {
    
    private static final int NUMBER_OF_COLUMNS = 21;
    
    private static final String TESTCASE_FILE = "testsuite.save";
    
    private static final int CUTOFF_NONE                       = -9001;
    private static final int CUTOFF_MAXEDGEOFMST               = -9002;
    private static final int CUTOFF_LARGESTSMALLESTTRIANGLEDGE = -9003;
    
    private static final int MODE_LENGTH           = 1;
    private static final int MODE_NORMALISEDLENGTH = 2;
    private static final int MODE_DENSITY          = 3;
    
    private boolean exitonclose;
    
    private int mode = MODE_LENGTH;
    
    private CountryListModel countrylistmodel;
    private TestCaseTableModel testcasemodel;
    
    private ArrayList<TestCase> testcases = new ArrayList<TestCase>();
    
    // MUTEX is notifies all threads at the conclusion of runTestSuite()
    final private Object MUTEX = new Object();
    
    public TestSuite(boolean _exitonclose, java.awt.Frame parent) {
        super(parent, true);
        initComponents();
        this.exitonclose = _exitonclose;
        
        // Setup state
        optLetterGenerationActionPerformed(null);
        
        // Load testcase from file if present
        try {
            loadTestcases();
        } catch ( IOException e ) {
            displayError(e);
            return;
        } catch ( ClassNotFoundException e ) {
            displayError(e);
            return;
        }
        
        // Create testcase table model for table
        testcasemodel = new TestCaseTableModel();
        tblTestcases.setModel( testcasemodel );
        
        // Adjust column widths
        TableColumn column;
        for ( int i=0 ; i<testcasemodel.getColumnCount() ; i++ ) {
            column = tblTestcases.getColumnModel().getColumn(i);
            if (i == 0) {
                column.setPreferredWidth(140);
            } else {
                column.setPreferredWidth(50);
            }
        }
        
        // Set countries to country list
        try {
            cboCountries.setModel( countrylistmodel = new CountryListModel(cboCountries,CountryData.getCountryList()) );
        } catch ( IOException e ) {
            displayError(e);
        }
    }
    
    private void saveTestcases() throws IOException {
        // Save testcases
        ObjectOutputStream stream = null;
        try {
            stream = new ObjectOutputStream(new FileOutputStream(TESTCASE_FILE));
            stream.writeObject( testcases );
        } finally {
            if ( stream!=null ) stream.close();
        }
    }
    
    private void loadTestcases() throws IOException, ClassNotFoundException {
        ArrayList<TestCase> newtestcases;
        
        // Load the testcases
        ObjectInputStream stream = null;
        try {
            stream = new ObjectInputStream(new FileInputStream(TESTCASE_FILE));
            newtestcases = (ArrayList<TestCase>) stream.readObject();
            
            // Check some testcases are present
            if ( newtestcases==null || newtestcases.size()<=0 ) {
                newtestcases = createDefaultSetOfTestCases();
            }
        } catch ( FileNotFoundException e ) {
            newtestcases = createDefaultSetOfTestCases();
        } catch ( InvalidClassException e ) {
            // Ignore error when version of class has changed and
            //  test suite is reloaded
            newtestcases = createDefaultSetOfTestCases();
        } finally {
            if ( stream!=null ) stream.close();
        }
        
        // Set the new set of testcases
        testcases = newtestcases;
        if ( testcasemodel!=null ) {
            testcasemodel.fireTableDataChanged();
        }
    }
    
    private ArrayList<TestCase> createDefaultSetOfTestCases() {
        // Default values
        int lengthcutoff   = CUTOFF_MAXEDGEOFMST;
        int mindensity     = 15;
        int numberofpoints = Integer.MAX_VALUE;
        
        // Create testcases
        ArrayList<TestCase> newtestcases = new ArrayList<TestCase>();
        newtestcases.add( new TestCase("C", "Garamond",  lengthcutoff, mindensity, numberofpoints ) );
        newtestcases.add( new TestCase("S", "Garamond",  lengthcutoff, mindensity, numberofpoints ) );
        newtestcases.add( new TestCase("F", "Garamond",  lengthcutoff, mindensity, numberofpoints ) );
        newtestcases.add( new TestCase("G", "Garamond",  lengthcutoff, mindensity, numberofpoints ) );
        newtestcases.add( new TestCase("Germany.txt",    lengthcutoff, mindensity, numberofpoints ) );
        newtestcases.add( new TestCase("Italy.txt",      lengthcutoff, mindensity, numberofpoints ) );
        newtestcases.add( new TestCase("Thailand.txt",   lengthcutoff, mindensity, numberofpoints ) );
        newtestcases.add( new TestCase("France.txt",     lengthcutoff, mindensity, numberofpoints ) );
        
        // Return testsuite
        return newtestcases;
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {
        groupGenerationType = new javax.swing.ButtonGroup();
        panelInCenter = new javax.swing.JPanel();
        scrollPane = new javax.swing.JScrollPane();
        tblTestcases = new javax.swing.JTable();
        panelStatusOutter = new javax.swing.JPanel();
        panelStatus = new javax.swing.JPanel();
        lblStatus = new javax.swing.JLabel();
        jPanel1 = new javax.swing.JPanel();
        lblOverall = new javax.swing.JLabel();
        lblTestcases = new javax.swing.JLabel();
        jPanel5 = new javax.swing.JPanel();
        progressOverall = new javax.swing.JProgressBar();
        progressTestcases = new javax.swing.JProgressBar();
        jScrollPane1 = new javax.swing.JScrollPane();
        panelOnRight = new javax.swing.JPanel();
        panelUpNorth = new javax.swing.JPanel();
        panelVariables = new javax.swing.JPanel();
        panelGenerate2 = new javax.swing.JPanel();
        panelGenerationSelection = new javax.swing.JPanel();
        txtLetter = new javax.swing.JTextField();
        cboCountries = new javax.swing.JComboBox();
        btnAddTestCase = new javax.swing.JButton();
        panelGap13 = new javax.swing.JPanel();
        panelPointOptions2 = new javax.swing.JPanel();
        panelLeft4 = new javax.swing.JPanel();
        lblGenerationType2 = new javax.swing.JLabel();
        lblFont2 = new javax.swing.JLabel();
        lblLengthCutoff = new javax.swing.JLabel();
        lblShapePoints = new javax.swing.JLabel();
        lblInternalPoints = new javax.swing.JLabel();
        lblShapePointMinDensity = new javax.swing.JLabel();
        lblInternalMinDensity = new javax.swing.JLabel();
        panelCenter4 = new javax.swing.JPanel();
        panelGenerationType2 = new javax.swing.JPanel();
        optLetterGeneration = new javax.swing.JRadioButton();
        optCountryGeneration = new javax.swing.JRadioButton();
        cboFont = new javax.swing.JComboBox();
        cboLengthCutoff = new javax.swing.JComboBox();
        cboShapePoints = new javax.swing.JComboBox();
        cboInternalPoints = new javax.swing.JComboBox();
        cboShapePointMinDensity = new javax.swing.JComboBox();
        cboInternalMinDensity = new javax.swing.JComboBox();
        panelGap8 = new javax.swing.JPanel();
        jPanel2 = new javax.swing.JPanel();
        chkAddShapePointsToSplitLongLines = new javax.swing.JCheckBox();
        panelExecutionOptions = new javax.swing.JPanel();
        panelCaptions2 = new javax.swing.JPanel();
        lblWidth = new javax.swing.JLabel();
        lblHeight = new javax.swing.JLabel();
        lblOutputDir = new javax.swing.JLabel();
        lblTimesToExecuteEachTestcase = new javax.swing.JLabel();
        panelTextfields = new javax.swing.JPanel();
        jPanel6 = new javax.swing.JPanel();
        txtWidth = new javax.swing.JTextField();
        lblPixels = new javax.swing.JLabel();
        jPanel7 = new javax.swing.JPanel();
        txtHeight = new javax.swing.JTextField();
        lblPixels1 = new javax.swing.JLabel();
        txtOutputDir = new javax.swing.JTextField();
        txtTimesToExecuteEachTestcase = new javax.swing.JTextField();
        panelOtherOptions = new javax.swing.JPanel();
        jPanel3 = new javax.swing.JPanel();
        btnDeleteSelected = new javax.swing.JButton();
        panelGap14 = new javax.swing.JPanel();
        jPanel4 = new javax.swing.JPanel();
        btnMoveUp = new javax.swing.JButton();
        btnMoveDown = new javax.swing.JButton();
        tabsTestCaseType = new javax.swing.JTabbedPane();
        panelLengthParameter = new javax.swing.JPanel();
        panelCenterPanel = new javax.swing.JPanel();
        panelNorth = new javax.swing.JPanel();
        panelLengthParameters = new javax.swing.JPanel();
        panelLeft = new javax.swing.JPanel();
        lblStartLength = new javax.swing.JLabel();
        lblEndLength = new javax.swing.JLabel();
        lblLengthIncrements = new javax.swing.JLabel();
        panelCenter = new javax.swing.JPanel();
        txtStartLength = new javax.swing.JTextField();
        txtEndLength = new javax.swing.JTextField();
        txtLengthIncrements = new javax.swing.JTextField();
        btnLengthParameter = new javax.swing.JButton();
        panelGap1 = new javax.swing.JPanel();
        panelGap2 = new javax.swing.JPanel();
        panelGap3 = new javax.swing.JPanel();
        panelGap4 = new javax.swing.JPanel();
        panelNormalisedLengthParameter = new javax.swing.JPanel();
        panelCenterPanel1 = new javax.swing.JPanel();
        panelNorth1 = new javax.swing.JPanel();
        panelLengthParameters1 = new javax.swing.JPanel();
        panelLeft1 = new javax.swing.JPanel();
        lblStartLength1 = new javax.swing.JLabel();
        lblEndLength1 = new javax.swing.JLabel();
        lblLengthIncrements1 = new javax.swing.JLabel();
        panelCenter1 = new javax.swing.JPanel();
        txtNormalisedStartLength = new javax.swing.JTextField();
        txtNormalisedEndLength = new javax.swing.JTextField();
        txtNormalisedLengthIncrements = new javax.swing.JTextField();
        btnNormalisedLengthParameter = new javax.swing.JButton();
        panelGap5 = new javax.swing.JPanel();
        panelGap6 = new javax.swing.JPanel();
        panelGap7 = new javax.swing.JPanel();
        panelGap15 = new javax.swing.JPanel();
        panelDensityParameter = new javax.swing.JPanel();
        panelCenterPanel2 = new javax.swing.JPanel();
        panelNorth2 = new javax.swing.JPanel();
        panelLengthParameters2 = new javax.swing.JPanel();
        panelLeft2 = new javax.swing.JPanel();
        lblStartDensity = new javax.swing.JLabel();
        lblEndDensity = new javax.swing.JLabel();
        lblDensityIncrements = new javax.swing.JLabel();
        panelCenter2 = new javax.swing.JPanel();
        txtStartDensity = new javax.swing.JTextField();
        txtEndDensity = new javax.swing.JTextField();
        txtDensityIncrements = new javax.swing.JTextField();
        btnDensityParameter = new javax.swing.JButton();
        panelGap9 = new javax.swing.JPanel();
        panelGap10 = new javax.swing.JPanel();
        panelGap11 = new javax.swing.JPanel();
        panelGap12 = new javax.swing.JPanel();
        panelRunAll = new javax.swing.JPanel();
        panelNorth3 = new javax.swing.JPanel();
        panelRunAllCheckBoxes = new javax.swing.JPanel();
        chkRunLength = new javax.swing.JCheckBox();
        chkRunNormalisedLength = new javax.swing.JCheckBox();
        chkRunDensity = new javax.swing.JCheckBox();
        btnRunAll = new javax.swing.JButton();
        panelGap16 = new javax.swing.JPanel();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Test Suite");
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosed(java.awt.event.WindowEvent evt) {
                formWindowClosed(evt);
            }
        });

        panelInCenter.setLayout(new java.awt.BorderLayout());

        scrollPane.setVerticalScrollBarPolicy(javax.swing.ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
        tblTestcases.setFont(new java.awt.Font("Arial", 0, 12));
        tblTestcases.setIntercellSpacing(new java.awt.Dimension(1, 3));
        scrollPane.setViewportView(tblTestcases);

        panelInCenter.add(scrollPane, java.awt.BorderLayout.CENTER);

        panelStatusOutter.setLayout(new java.awt.BorderLayout());

        panelStatusOutter.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        panelStatus.setLayout(new java.awt.BorderLayout());

        panelStatus.setBorder(javax.swing.BorderFactory.createEmptyBorder(5, 5, 5, 5));
        panelStatus.add(lblStatus, java.awt.BorderLayout.NORTH);

        jPanel1.setLayout(new java.awt.GridLayout(0, 1, 0, 2));

        lblOverall.setText("Overall:");
        jPanel1.add(lblOverall);

        lblTestcases.setText("Testcase:");
        jPanel1.add(lblTestcases);

        panelStatus.add(jPanel1, java.awt.BorderLayout.WEST);

        jPanel5.setLayout(new java.awt.GridLayout(0, 1, 0, 2));

        jPanel5.add(progressOverall);

        jPanel5.add(progressTestcases);

        panelStatus.add(jPanel5, java.awt.BorderLayout.CENTER);

        panelStatusOutter.add(panelStatus, java.awt.BorderLayout.CENTER);

        panelInCenter.add(panelStatusOutter, java.awt.BorderLayout.SOUTH);

        getContentPane().add(panelInCenter, java.awt.BorderLayout.CENTER);

        jScrollPane1.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        jScrollPane1.setVerticalScrollBarPolicy(javax.swing.ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
        panelOnRight.setLayout(new java.awt.BorderLayout());

        panelOnRight.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED));
        panelUpNorth.setLayout(new javax.swing.BoxLayout(panelUpNorth, javax.swing.BoxLayout.Y_AXIS));

        panelVariables.setLayout(new javax.swing.BoxLayout(panelVariables, javax.swing.BoxLayout.Y_AXIS));

        panelVariables.setBorder(javax.swing.BorderFactory.createTitledBorder("Testcase Details"));
        panelGenerate2.setLayout(new java.awt.BorderLayout(3, 0));

        panelGenerationSelection.setLayout(new java.awt.GridLayout(0, 1));

        txtLetter.setFont(new java.awt.Font("Tahoma", 1, 12));
        txtLetter.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        txtLetter.setText("S");
        panelGenerationSelection.add(txtLetter);

        panelGenerationSelection.add(cboCountries);

        panelGenerate2.add(panelGenerationSelection, java.awt.BorderLayout.CENTER);

        btnAddTestCase.setText("Add Testcase");
        btnAddTestCase.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnAddTestCaseActionPerformed(evt);
            }
        });

        panelGenerate2.add(btnAddTestCase, java.awt.BorderLayout.EAST);

        panelVariables.add(panelGenerate2);

        panelGap13.setLayout(null);

        panelGap13.setPreferredSize(new java.awt.Dimension(4, 4));
        panelVariables.add(panelGap13);

        panelPointOptions2.setLayout(new java.awt.BorderLayout(2, 0));

        panelLeft4.setLayout(new java.awt.GridLayout(0, 1, 0, 2));

        lblGenerationType2.setText("Generation Type:");
        panelLeft4.add(lblGenerationType2);

        lblFont2.setText("Font:");
        panelLeft4.add(lblFont2);

        lblLengthCutoff.setText("Length Cut-off:");
        panelLeft4.add(lblLengthCutoff);

        lblShapePoints.setText("Shape Points:");
        panelLeft4.add(lblShapePoints);

        lblInternalPoints.setText("Internal Points:");
        panelLeft4.add(lblInternalPoints);

        lblShapePointMinDensity.setText("Shape Point Min Density:");
        panelLeft4.add(lblShapePointMinDensity);

        lblInternalMinDensity.setText("Internal Min Density:");
        panelLeft4.add(lblInternalMinDensity);

        panelPointOptions2.add(panelLeft4, java.awt.BorderLayout.WEST);

        panelCenter4.setLayout(new java.awt.GridLayout(0, 1, 0, 2));

        panelGenerationType2.setLayout(new java.awt.BorderLayout());

        groupGenerationType.add(optLetterGeneration);
        optLetterGeneration.setSelected(true);
        optLetterGeneration.setText("Letter");
        optLetterGeneration.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                optLetterGenerationActionPerformed(evt);
            }
        });

        panelGenerationType2.add(optLetterGeneration, java.awt.BorderLayout.WEST);

        groupGenerationType.add(optCountryGeneration);
        optCountryGeneration.setText("Country");
        optCountryGeneration.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                optCountryGenerationActionPerformed(evt);
            }
        });

        panelGenerationType2.add(optCountryGeneration, java.awt.BorderLayout.CENTER);

        panelCenter4.add(panelGenerationType2);

        cboFont.setEditable(true);
        cboFont.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Arial", "Courier New", "Garamond", "Times New Roman", "Lucida Console" }));
        cboFont.setSelectedIndex(2);
        panelCenter4.add(cboFont);

        cboLengthCutoff.setEditable(true);
        cboLengthCutoff.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "No Length Cut-off", "Max Edge of MST", "Largest Smallest Triangle Edge", "5", "10", "15", "etc" }));
        cboLengthCutoff.setSelectedIndex(1);
        panelCenter4.add(cboLengthCutoff);

        cboShapePoints.setEditable(true);
        cboShapePoints.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "No Points", "10", "25", "50", "100", "250", "Maximum Possible" }));
        cboShapePoints.setSelectedIndex(6);
        panelCenter4.add(cboShapePoints);

        cboInternalPoints.setEditable(true);
        cboInternalPoints.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "No Points", "10", "25", "50", "100", "250", "Maximum Possible" }));
        cboInternalPoints.setSelectedIndex(6);
        panelCenter4.add(cboInternalPoints);

        cboShapePointMinDensity.setEditable(true);
        cboShapePointMinDensity.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "1", "2", "3", "4", "5", "6", "8", "10", "12", "15", "20", "25", "30", "40", "50", "100", "250" }));
        cboShapePointMinDensity.setSelectedIndex(9);
        panelCenter4.add(cboShapePointMinDensity);

        cboInternalMinDensity.setEditable(true);
        cboInternalMinDensity.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "1", "2", "3", "4", "5", "6", "8", "10", "12", "15", "20", "25", "30", "40", "50", "100", "250" }));
        cboInternalMinDensity.setSelectedIndex(9);
        panelCenter4.add(cboInternalMinDensity);

        panelPointOptions2.add(panelCenter4, java.awt.BorderLayout.CENTER);

        panelVariables.add(panelPointOptions2);

        panelGap8.setLayout(null);

        panelGap8.setPreferredSize(new java.awt.Dimension(4, 4));
        panelVariables.add(panelGap8);

        jPanel2.setLayout(new java.awt.BorderLayout());

        chkAddShapePointsToSplitLongLines.setSelected(true);
        chkAddShapePointsToSplitLongLines.setText("Add Shape Points to Split Long Lines");
        jPanel2.add(chkAddShapePointsToSplitLongLines, java.awt.BorderLayout.CENTER);

        panelVariables.add(jPanel2);

        panelUpNorth.add(panelVariables);

        panelExecutionOptions.setLayout(new java.awt.BorderLayout(3, 0));

        panelExecutionOptions.setBorder(javax.swing.BorderFactory.createTitledBorder("Execution Options"));
        panelCaptions2.setLayout(new java.awt.GridLayout(0, 1));

        lblWidth.setText("Width:");
        panelCaptions2.add(lblWidth);

        lblHeight.setText("Height:");
        panelCaptions2.add(lblHeight);

        lblOutputDir.setText("Output Dir:");
        panelCaptions2.add(lblOutputDir);

        lblTimesToExecuteEachTestcase.setText("Times to Execute Each:");
        panelCaptions2.add(lblTimesToExecuteEachTestcase);

        panelExecutionOptions.add(panelCaptions2, java.awt.BorderLayout.WEST);

        panelTextfields.setLayout(new java.awt.GridLayout(0, 1, 0, 2));

        jPanel6.setLayout(new java.awt.BorderLayout(3, 0));

        txtWidth.setText("1000");
        jPanel6.add(txtWidth, java.awt.BorderLayout.CENTER);

        lblPixels.setText("pixels");
        jPanel6.add(lblPixels, java.awt.BorderLayout.EAST);

        panelTextfields.add(jPanel6);

        jPanel7.setLayout(new java.awt.BorderLayout(3, 0));

        txtHeight.setText("1000");
        jPanel7.add(txtHeight, java.awt.BorderLayout.CENTER);

        lblPixels1.setText("pixels");
        jPanel7.add(lblPixels1, java.awt.BorderLayout.EAST);

        panelTextfields.add(jPanel7);

        txtOutputDir.setText("testcases");
        panelTextfields.add(txtOutputDir);

        txtTimesToExecuteEachTestcase.setText("50");
        panelTextfields.add(txtTimesToExecuteEachTestcase);

        panelExecutionOptions.add(panelTextfields, java.awt.BorderLayout.CENTER);

        panelUpNorth.add(panelExecutionOptions);

        panelOtherOptions.setLayout(new javax.swing.BoxLayout(panelOtherOptions, javax.swing.BoxLayout.Y_AXIS));

        panelOtherOptions.setBorder(javax.swing.BorderFactory.createTitledBorder("Other Options"));
        jPanel3.setLayout(new java.awt.BorderLayout());

        btnDeleteSelected.setText("Remove Selected");
        btnDeleteSelected.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnDeleteSelectedActionPerformed(evt);
            }
        });

        jPanel3.add(btnDeleteSelected, java.awt.BorderLayout.CENTER);

        panelOtherOptions.add(jPanel3);

        panelGap14.setLayout(null);

        panelGap14.setPreferredSize(new java.awt.Dimension(5, 5));
        panelOtherOptions.add(panelGap14);

        jPanel4.setLayout(new java.awt.BorderLayout(0, 3));

        btnMoveUp.setText("Move Up");
        btnMoveUp.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnMoveUpActionPerformed(evt);
            }
        });

        jPanel4.add(btnMoveUp, java.awt.BorderLayout.NORTH);

        btnMoveDown.setText("Move Down");
        btnMoveDown.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnMoveDownActionPerformed(evt);
            }
        });

        jPanel4.add(btnMoveDown, java.awt.BorderLayout.CENTER);

        panelOtherOptions.add(jPanel4);

        panelUpNorth.add(panelOtherOptions);

        panelOnRight.add(panelUpNorth, java.awt.BorderLayout.NORTH);

        tabsTestCaseType.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                tabsTestCaseTypeStateChanged(evt);
            }
        });

        panelLengthParameter.setLayout(new java.awt.BorderLayout());

        panelCenterPanel.setLayout(new java.awt.BorderLayout());

        panelNorth.setLayout(new java.awt.BorderLayout(0, 5));

        panelLengthParameters.setLayout(new java.awt.BorderLayout(4, 0));

        panelLengthParameters.setBorder(javax.swing.BorderFactory.createTitledBorder("Length Details"));
        panelLeft.setLayout(new java.awt.GridLayout(0, 1, 0, 3));

        lblStartLength.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        lblStartLength.setText("Start Length:");
        panelLeft.add(lblStartLength);

        lblEndLength.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        lblEndLength.setText("End Length:");
        panelLeft.add(lblEndLength);

        lblLengthIncrements.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        lblLengthIncrements.setText("Length Increments:");
        panelLeft.add(lblLengthIncrements);

        panelLengthParameters.add(panelLeft, java.awt.BorderLayout.WEST);

        panelCenter.setLayout(new java.awt.GridLayout(0, 1, 0, 3));

        txtStartLength.setColumns(4);
        txtStartLength.setText("10");
        panelCenter.add(txtStartLength);

        txtEndLength.setColumns(4);
        txtEndLength.setText("90");
        panelCenter.add(txtEndLength);

        txtLengthIncrements.setColumns(4);
        txtLengthIncrements.setText("5");
        panelCenter.add(txtLengthIncrements);

        panelLengthParameters.add(panelCenter, java.awt.BorderLayout.CENTER);

        panelNorth.add(panelLengthParameters, java.awt.BorderLayout.NORTH);

        btnLengthParameter.setText("Execute Testsuite");
        btnLengthParameter.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnLengthParameterActionPerformed(evt);
            }
        });

        panelNorth.add(btnLengthParameter, java.awt.BorderLayout.CENTER);

        panelCenterPanel.add(panelNorth, java.awt.BorderLayout.NORTH);

        panelLengthParameter.add(panelCenterPanel, java.awt.BorderLayout.CENTER);

        panelGap1.setLayout(null);

        panelGap1.setPreferredSize(new java.awt.Dimension(6, 6));
        panelLengthParameter.add(panelGap1, java.awt.BorderLayout.NORTH);

        panelGap2.setLayout(null);

        panelGap2.setPreferredSize(new java.awt.Dimension(5, 5));
        panelLengthParameter.add(panelGap2, java.awt.BorderLayout.WEST);

        panelGap3.setLayout(null);

        panelGap3.setPreferredSize(new java.awt.Dimension(5, 5));
        panelLengthParameter.add(panelGap3, java.awt.BorderLayout.EAST);

        panelGap4.setLayout(null);

        panelGap4.setPreferredSize(new java.awt.Dimension(8, 8));
        panelLengthParameter.add(panelGap4, java.awt.BorderLayout.SOUTH);

        tabsTestCaseType.addTab("Length", panelLengthParameter);

        panelNormalisedLengthParameter.setLayout(new java.awt.BorderLayout());

        panelCenterPanel1.setLayout(new java.awt.BorderLayout());

        panelNorth1.setLayout(new java.awt.BorderLayout(0, 5));

        panelLengthParameters1.setLayout(new java.awt.BorderLayout(4, 0));

        panelLengthParameters1.setBorder(javax.swing.BorderFactory.createTitledBorder("Normalised Length Details"));
        panelLeft1.setLayout(new java.awt.GridLayout(0, 1, 0, 3));

        lblStartLength1.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        lblStartLength1.setText("Start Length:");
        panelLeft1.add(lblStartLength1);

        lblEndLength1.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        lblEndLength1.setText("End Length:");
        panelLeft1.add(lblEndLength1);

        lblLengthIncrements1.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        lblLengthIncrements1.setText("Length Increments:");
        panelLeft1.add(lblLengthIncrements1);

        panelLengthParameters1.add(panelLeft1, java.awt.BorderLayout.WEST);

        panelCenter1.setLayout(new java.awt.GridLayout(0, 1, 0, 3));

        txtNormalisedStartLength.setColumns(4);
        txtNormalisedStartLength.setText("0.00");
        panelCenter1.add(txtNormalisedStartLength);

        txtNormalisedEndLength.setColumns(4);
        txtNormalisedEndLength.setText("1.00");
        panelCenter1.add(txtNormalisedEndLength);

        txtNormalisedLengthIncrements.setColumns(4);
        txtNormalisedLengthIncrements.setText("0.05");
        panelCenter1.add(txtNormalisedLengthIncrements);

        panelLengthParameters1.add(panelCenter1, java.awt.BorderLayout.CENTER);

        panelNorth1.add(panelLengthParameters1, java.awt.BorderLayout.NORTH);

        btnNormalisedLengthParameter.setText("Execute Testsuite");
        btnNormalisedLengthParameter.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnNormalisedLengthParameterActionPerformed(evt);
            }
        });

        panelNorth1.add(btnNormalisedLengthParameter, java.awt.BorderLayout.CENTER);

        panelCenterPanel1.add(panelNorth1, java.awt.BorderLayout.NORTH);

        panelNormalisedLengthParameter.add(panelCenterPanel1, java.awt.BorderLayout.CENTER);

        panelGap5.setLayout(null);

        panelGap5.setPreferredSize(new java.awt.Dimension(6, 6));
        panelNormalisedLengthParameter.add(panelGap5, java.awt.BorderLayout.NORTH);

        panelGap6.setLayout(null);

        panelGap6.setPreferredSize(new java.awt.Dimension(5, 5));
        panelNormalisedLengthParameter.add(panelGap6, java.awt.BorderLayout.WEST);

        panelGap7.setLayout(null);

        panelGap7.setPreferredSize(new java.awt.Dimension(5, 5));
        panelNormalisedLengthParameter.add(panelGap7, java.awt.BorderLayout.EAST);

        panelGap15.setLayout(null);

        panelGap15.setPreferredSize(new java.awt.Dimension(8, 8));
        panelNormalisedLengthParameter.add(panelGap15, java.awt.BorderLayout.SOUTH);

        tabsTestCaseType.addTab("Normalised Length", panelNormalisedLengthParameter);

        panelDensityParameter.setLayout(new java.awt.BorderLayout());

        panelCenterPanel2.setLayout(new java.awt.BorderLayout());

        panelNorth2.setLayout(new java.awt.BorderLayout(0, 5));

        panelLengthParameters2.setLayout(new java.awt.BorderLayout(4, 0));

        panelLengthParameters2.setBorder(javax.swing.BorderFactory.createTitledBorder("Density Details"));
        panelLeft2.setLayout(new java.awt.GridLayout(0, 1, 0, 3));

        lblStartDensity.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        lblStartDensity.setText("Start Density:");
        panelLeft2.add(lblStartDensity);

        lblEndDensity.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        lblEndDensity.setText("End Density:");
        panelLeft2.add(lblEndDensity);

        lblDensityIncrements.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        lblDensityIncrements.setText("Density Increments:");
        panelLeft2.add(lblDensityIncrements);

        panelLengthParameters2.add(panelLeft2, java.awt.BorderLayout.WEST);

        panelCenter2.setLayout(new java.awt.GridLayout(0, 1, 0, 3));

        txtStartDensity.setColumns(4);
        txtStartDensity.setText("10");
        panelCenter2.add(txtStartDensity);

        txtEndDensity.setColumns(4);
        txtEndDensity.setText("45");
        panelCenter2.add(txtEndDensity);

        txtDensityIncrements.setColumns(4);
        txtDensityIncrements.setText("5");
        panelCenter2.add(txtDensityIncrements);

        panelLengthParameters2.add(panelCenter2, java.awt.BorderLayout.CENTER);

        panelNorth2.add(panelLengthParameters2, java.awt.BorderLayout.NORTH);

        btnDensityParameter.setText("Execute Testsuite");
        btnDensityParameter.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnDensityParameterActionPerformed(evt);
            }
        });

        panelNorth2.add(btnDensityParameter, java.awt.BorderLayout.CENTER);

        panelCenterPanel2.add(panelNorth2, java.awt.BorderLayout.NORTH);

        panelDensityParameter.add(panelCenterPanel2, java.awt.BorderLayout.CENTER);

        panelGap9.setLayout(null);

        panelGap9.setPreferredSize(new java.awt.Dimension(6, 6));
        panelDensityParameter.add(panelGap9, java.awt.BorderLayout.NORTH);

        panelGap10.setLayout(null);

        panelGap10.setPreferredSize(new java.awt.Dimension(5, 5));
        panelDensityParameter.add(panelGap10, java.awt.BorderLayout.WEST);

        panelGap11.setLayout(null);

        panelGap11.setPreferredSize(new java.awt.Dimension(5, 5));
        panelDensityParameter.add(panelGap11, java.awt.BorderLayout.EAST);

        panelGap12.setLayout(null);

        panelGap12.setPreferredSize(new java.awt.Dimension(8, 8));
        panelDensityParameter.add(panelGap12, java.awt.BorderLayout.SOUTH);

        tabsTestCaseType.addTab("Density", panelDensityParameter);

        panelRunAll.setLayout(new java.awt.GridBagLayout());

        panelNorth3.setLayout(new java.awt.BorderLayout(0, 6));

        panelRunAllCheckBoxes.setLayout(new java.awt.GridLayout(0, 1, 0, 4));

        chkRunLength.setText("Run Length Test Suite");
        panelRunAllCheckBoxes.add(chkRunLength);

        chkRunNormalisedLength.setSelected(true);
        chkRunNormalisedLength.setText("Run Normalised Length Test Suite");
        panelRunAllCheckBoxes.add(chkRunNormalisedLength);

        chkRunDensity.setSelected(true);
        chkRunDensity.setText("Run Density Test Suite");
        panelRunAllCheckBoxes.add(chkRunDensity);

        panelNorth3.add(panelRunAllCheckBoxes, java.awt.BorderLayout.NORTH);

        btnRunAll.setText("Execute Testsuite");
        btnRunAll.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnRunAllActionPerformed(evt);
            }
        });

        panelNorth3.add(btnRunAll, java.awt.BorderLayout.CENTER);

        panelGap16.setLayout(null);

        panelNorth3.add(panelGap16, java.awt.BorderLayout.SOUTH);

        panelRunAll.add(panelNorth3, new java.awt.GridBagConstraints());

        tabsTestCaseType.addTab("Run All/Selection", panelRunAll);

        panelOnRight.add(tabsTestCaseType, java.awt.BorderLayout.SOUTH);

        jScrollPane1.setViewportView(panelOnRight);

        getContentPane().add(jScrollPane1, java.awt.BorderLayout.EAST);

        java.awt.Dimension screenSize = java.awt.Toolkit.getDefaultToolkit().getScreenSize();
        setBounds((screenSize.width-845)/2, (screenSize.height-730)/2, 845, 730);
    }// </editor-fold>//GEN-END:initComponents
    
    private void btnRunAllActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnRunAllActionPerformed
        new Thread() {
            public void run() {
                try {
                    if ( chkRunLength.isSelected() ) {
                        // Select the appropriate tab
                        tabsTestCaseType.setSelectedComponent(panelLengthParameter);
                        
                        // Run the test suite
                        btnLengthParameter.doClick();
                        
                        // Wait for MUTEX
                        synchronized ( MUTEX ) {
                            MUTEX.wait();
                        }
                    }
                    
                    if ( chkRunNormalisedLength.isSelected() ) {
                        // Select the appropriate tab
                        tabsTestCaseType.setSelectedComponent(panelNormalisedLengthParameter);
                        
                        // Run the test suite
                        btnNormalisedLengthParameter.doClick();
                        
                        // Wait for MUTEX
                        synchronized ( MUTEX ) {
                            MUTEX.wait();
                        }
                    }
                    
                    if ( chkRunDensity.isSelected() ) {
                        // Select the appropriate tab
                        tabsTestCaseType.setSelectedComponent(panelDensityParameter);
                        
                        // Run the test suite
                        btnDensityParameter.doClick();
                        
                        // Wait for MUTEX
                        synchronized ( MUTEX ) {
                            MUTEX.wait();
                        }
                    }
                } catch ( InterruptedException e ) {
                    displayError(e);
                }
            }
        }.start();
    }//GEN-LAST:event_btnRunAllActionPerformed
    
    private void btnNormalisedLengthParameterActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnNormalisedLengthParameterActionPerformed
        try {
            final double start = Double.parseDouble( txtNormalisedStartLength.getText() );
            final double end   = Double.parseDouble( txtNormalisedEndLength.getText() );
            final double incr  = Double.parseDouble( txtNormalisedLengthIncrements.getText() );
            if ( start>=end ) {
                displayError("Start value must be less than end value");
                return;
            } else if ( start<0.0 || start>1.0 ) {
                displayError("Start value must be between 0 and 1");
                return;
            } else if ( end<0.0 || end>1.0 ) {
                displayError("End value must be between 0 and 1");
                return;
            } else if ( incr<=0.0 || incr>=1.0) {
                displayError("Increment must be between 0 and 1");
                return;
            }
            runTestSuite( new RunTestSeries() {
                public boolean runTestSeries(JProgressBar progress, TestCase testcase, int width, int height, File outputdir, int timestoexecute) throws IOException {
                    // Setup progress bar
                    progress.setMinimum(1);
                    progress.setMaximum( (int)( ((end-start)/incr + 1) * timestoexecute ) );
                    progress.setValue(1);
                    
                    // Determine string to prepend to filenames
                    String prepend = outputdir.getAbsolutePath() + File.separator;
                    String testcasename;
                    if ( testcase.font!=null && testcase.letter!=null && testcase.letter.trim().length()==1 ) {
                        testcasename = testcase.letter + " (" + testcase.font.getName() + ")";
                        prepend += "normalisedlength-" + testcase.letter + "-" + testcase.font.getName();
                    } else if ( testcase.countryfile!=null && testcase.countryfile.trim().length()>=1 ) {
                        testcasename = CountryListModel.formatHumanReadable(testcase.countryfile);
                        prepend += "normalisedlength-" + testcasename;
                    } else {
                        displayError("Unknown shape to generate for testcase");
                        return false;
                    }
                    
                    // Open summary file writer
                    BufferedWriter summarywriter = new BufferedWriter(new FileWriter(prepend + ".csv"));
                    
                    // Collects values
                    ValueCollector values = new ValueCollector(NUMBER_OF_COLUMNS);
                    
                    // Run testseries
                    boolean success;
                    boolean isfirst = true;
                    for ( double normalisedlength=start ; normalisedlength<=end ; normalisedlength+=incr ) {
                        // Reset the value collector (must do BEFORE writeHeadingRow())
                        values.reset();
                        
                        // Open file writer
                        BufferedWriter writer = new BufferedWriter(new FileWriter(prepend + "-" + normalisedlength + ".csv"));
                        
                        // Output heading row
                        writeHeadingRow(writer, values);
                        
                        // Create a new cutoff calculator for the new normalised length
                        final double finalnormalisedlength = normalisedlength;
                        TriangulationRepresentation.CalcCutOff cutoffcalc = new TriangulationRepresentation.CalcCutOff() {
                            public int calculateCutOff(TriangulationRepresentation rep) {
                                int min = rep.getMinLength();
                                int max = rep.getMaxLength();
                                return (int)( finalnormalisedlength * (double)(max - min) + min );
                            }
                        };
                        
                        // Execute testcase
                        for ( int x=1 ; x<=timestoexecute ; x++ ) {
                            // Set progress
                            progress.setValue( (int)( (normalisedlength-start)/incr * timestoexecute + x ) );
                            lblStatus.setText("Executing test case \"" + testcasename + "\" at normalised length " + normalisedlength + " - " + x + " of " + timestoexecute + " times" );
                            
                            // Execute testcase
                            success = executeTestCase(writer, values, width, height,
                                    testcase.font, testcase.letter, testcase.countryfile,
                                    testcase.shapepoints,    testcase.shapepoint_mindensity,
                                    testcase.internalpoints, testcase.internal_mindensity,
                                    -1, cutoffcalc,
                                    testcase.addshapepointstosplitlonglines);
                            if ( success==false ) {
                                return false;
                            }
                        }
                        
                        // Close writer
                        writer.flush();
                        writer.close();
                        
                        // Write summary
                        values.writeSummary( summarywriter , isfirst );
                        if ( isfirst ) isfirst = false;
                    }
                    
                    // Close summary writer
                    summarywriter.flush();
                    summarywriter.close();
                    
                    // Return true for success
                    return true;
                }
            } );
        } catch ( NumberFormatException e ) {
            displayError("Failed to parse length paramter - only integers are valid");
            return;
        }
    }//GEN-LAST:event_btnNormalisedLengthParameterActionPerformed
    
    private void formWindowClosed(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowClosed
        if ( exitonclose ) {
            System.exit(0);
        }
    }//GEN-LAST:event_formWindowClosed
    
    private void tabsTestCaseTypeStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_tabsTestCaseTypeStateChanged
        Component component = tabsTestCaseType.getSelectedComponent();
        if ( component==panelLengthParameter ) {
            mode = MODE_LENGTH;
            if ( testcasemodel!=null ) {
                testcasemodel.fireTableDataChanged();
            }
        } else if ( component==panelNormalisedLengthParameter ) {
            mode = MODE_NORMALISEDLENGTH;
            if ( testcasemodel!=null ) {
                testcasemodel.fireTableDataChanged();
            }
        } else if ( component==panelDensityParameter ) {
            mode = MODE_DENSITY;
            if ( testcasemodel!=null ) {
                testcasemodel.fireTableDataChanged();
            }
        } else if ( component==panelRunAll ) {
            mode = -1;
            if ( testcasemodel!=null ) {
                testcasemodel.fireTableDataChanged();
            }
        } else {
            displayError("Unknown tab selected");
            return;
        }
    }//GEN-LAST:event_tabsTestCaseTypeStateChanged
    
    private void btnMoveUpActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnMoveUpActionPerformed
        int selected[] = tblTestcases.getSelectedRows();
        if ( selected==null || selected.length==0 ) {
            displayError("Please selected value to move");
        } else if ( selected.length!=1 ) {
            displayError("You can only move only value at a time");
        } else {
            // Move testcase
            int index = selected[0];
            if ( index<=0 ) {
                return;
            } else {
                TestCase testcase = testcases.remove(index);
                testcases.add( index-1 , testcase );
            }
            
            // Update table
            testcasemodel.fireTableDataChanged();
            
            // Reselect new index
            tblTestcases.setRowSelectionInterval(index-1, index-1);
            
            // Save testcases
            try {
                saveTestcases();
            } catch ( IOException e ) {
                displayError(e);
                return;
            }
        }
    }//GEN-LAST:event_btnMoveUpActionPerformed
    
    private void btnMoveDownActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnMoveDownActionPerformed
        int selected[] = tblTestcases.getSelectedRows();
        if ( selected==null || selected.length==0 ) {
            displayError("Please selected value to move");
        } else if ( selected.length!=1 ) {
            displayError("You can only move only value at a time");
        } else {
            // Move testcase
            int index = selected[0];
            if ( index>=testcases.size()-1 ) {
                return;
            } else {
                TestCase testcase = testcases.remove(index);
                testcases.add( index+1 , testcase );
            }
            
            // Update table
            testcasemodel.fireTableDataChanged();
            
            // Reselect new index
            tblTestcases.setRowSelectionInterval(index+1, index+1);
            
            // Save testcases
            try {
                saveTestcases();
            } catch ( IOException e ) {
                displayError(e);
                return;
            }
        }
    }//GEN-LAST:event_btnMoveDownActionPerformed
    
    private void btnDeleteSelectedActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnDeleteSelectedActionPerformed
        int selected[] = tblTestcases.getSelectedRows();
        if ( selected==null || selected.length==0 ) {
            displayError("Please selected value/values to remove");
        } else {
            // Ensure selected list is sorted
            Arrays.sort(selected);
            
            // Remove from end of list
            for ( int x=selected.length-1 ; x>=0 ; x-- ) {
                testcases.remove( selected[x] );
            }
            
            // Update table
            testcasemodel.fireTableDataChanged();
            
            // Save testcases
            try {
                saveTestcases();
            } catch ( IOException e ) {
                displayError(e);
                return;
            }
        }
    }//GEN-LAST:event_btnDeleteSelectedActionPerformed
    
    private void btnDensityParameterActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnDensityParameterActionPerformed
        try {
            final int start = Integer.parseInt( txtStartDensity.getText() );
            final int end   = Integer.parseInt( txtEndDensity.getText() );
            final int incr  = Integer.parseInt( txtDensityIncrements.getText() );
            if ( start>=end ) {
                displayError("Start value must be less than end value");
                return;
            }
            runTestSuite( new RunTestSeries() {
                public boolean runTestSeries(JProgressBar progress, TestCase testcase, int width, int height, File outputdir, int timestoexecute) throws IOException {
                    // Setup progress bar
                    progress.setMinimum(1);
                    progress.setMaximum( ((end-start)/incr + 1) * timestoexecute );
                    progress.setValue(1);
                    
                    // Determine string to prepend to filenames
                    String prepend = outputdir.getAbsolutePath() + File.separator;
                    String testcasename;
                    if ( testcase.font!=null && testcase.letter!=null && testcase.letter.trim().length()==1 ) {
                        testcasename = testcase.letter + " (" + testcase.font.getName() + ")";
                        prepend += "density-" + testcase.letter + "-" + testcase.font.getName();
                    } else if ( testcase.countryfile!=null && testcase.countryfile.trim().length()>=1 ) {
                        testcasename = CountryListModel.formatHumanReadable(testcase.countryfile);
                        prepend += "density-" + testcasename;
                    } else {
                        displayError("Unknown shape to generate for testcase");
                        return false;
                    }
                    
                    // Open summary file writer
                    BufferedWriter summarywriter = new BufferedWriter(new FileWriter(prepend + ".csv"));
                    
                    // Collects values
                    ValueCollector values = new ValueCollector(NUMBER_OF_COLUMNS);
                    
                    // Run testseries
                    boolean success;
                    boolean isfirst = true;
                    for ( int density=start ; density<=end ; density+=incr ) {
                        // Reset the value collector (must do BEFORE writeHeadingRow())
                        values.reset();
                        
                        // Open file writer
                        BufferedWriter writer = new BufferedWriter(new FileWriter(prepend + "-" + density + ".csv"));
                        
                        // Output heading row
                        writeHeadingRow(writer, values);
                        
                        // Execute testcase
                        for ( int x=1 ; x<=timestoexecute ; x++ ) {
                            // Set progress
                            progress.setValue( (density-start)/incr * timestoexecute + x );
                            lblStatus.setText("Executing test case \"" + testcasename + "\" at a density " + density + " - " + x + " of " + timestoexecute + " times" );
                            
                            // Execute testcase
                            success = executeTestCase(writer, values, width, height,
                                    testcase.font, testcase.letter, testcase.countryfile,
                                    testcase.shapepoints,    density,
                                    testcase.internalpoints, density,
                                    testcase.lengthcutoff, null,
                                    testcase.addshapepointstosplitlonglines);
                            if ( success==false ) {
                                return false;
                            }
                        }
                        
                        // Close writer
                        writer.flush();
                        writer.close();
                        
                        // Write summary
                        values.writeSummary( summarywriter , isfirst );
                        if ( isfirst ) isfirst = false;
                    }
                    
                    // Close summary writer
                    summarywriter.flush();
                    summarywriter.close();
                    
                    // Return true for success
                    return true;
                }
            } );
        } catch ( NumberFormatException e ) {
            displayError("Failed to parse length paramter - only integers are valid");
            return;
        }
    }//GEN-LAST:event_btnDensityParameterActionPerformed
    
    private void btnLengthParameterActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnLengthParameterActionPerformed
        try {
            final int start = Integer.parseInt( txtStartLength.getText() );
            final int end   = Integer.parseInt( txtEndLength.getText() );
            final int incr  = Integer.parseInt( txtLengthIncrements.getText() );
            if ( start>=end ) {
                displayError("Start value must be less than end value");
                return;
            }
            runTestSuite( new RunTestSeries() {
                public boolean runTestSeries(JProgressBar progress, TestCase testcase, int width, int height, File outputdir, int timestoexecute) throws IOException {
                    // Setup progress bar
                    progress.setMinimum(1);
                    progress.setMaximum( ((end-start)/incr + 1) * timestoexecute );
                    progress.setValue(1);
                    
                    // Determine string to prepend to filenames
                    String prepend = outputdir.getAbsolutePath() + File.separator;
                    String testcasename;
                    if ( testcase.font!=null && testcase.letter!=null && testcase.letter.trim().length()==1 ) {
                        testcasename = testcase.letter + " (" + testcase.font.getName() + ")";
                        prepend += "length-" + testcase.letter + "-" + testcase.font.getName();
                    } else if ( testcase.countryfile!=null && testcase.countryfile.trim().length()>=1 ) {
                        testcasename = CountryListModel.formatHumanReadable(testcase.countryfile);
                        prepend += "length-" + testcasename;
                    } else {
                        displayError("Unknown shape to generate for testcase");
                        return false;
                    }
                    
                    // Open summary file writer
                    BufferedWriter summarywriter = new BufferedWriter(new FileWriter(prepend + ".csv"));
                    
                    // Collects values
                    ValueCollector values = new ValueCollector(NUMBER_OF_COLUMNS);
                    
                    // Run testseries
                    boolean success;
                    boolean isfirst = true;
                    for ( int length=start ; length<=end ; length+=incr ) {
                        // Reset the value collector (must do BEFORE writeHeadingRow())
                        values.reset();
                        
                        // Open file writer
                        BufferedWriter writer = new BufferedWriter(new FileWriter(prepend + "-" + length + ".csv"));
                        
                        // Output heading row
                        writeHeadingRow(writer, values);
                        
                        // Execute testcase
                        for ( int x=1 ; x<=timestoexecute ; x++ ) {
                            // Set progress
                            progress.setValue( (length-start)/incr * timestoexecute + x );
                            lblStatus.setText("Executing test case \"" + testcasename + "\" at a length " + length + " - " + x + " of " + timestoexecute + " times" );
                            
                            // Execute testcase
                            success = executeTestCase(writer, values, width, height,
                                    testcase.font, testcase.letter, testcase.countryfile,
                                    testcase.shapepoints,    testcase.shapepoint_mindensity,
                                    testcase.internalpoints, testcase.internal_mindensity,
                                    length, null,
                                    testcase.addshapepointstosplitlonglines);
                            if ( success==false ) {
                                return false;
                            }
                        }
                        
                        // Close writer
                        writer.flush();
                        writer.close();
                        
                        // Write summary
                        values.writeSummary( summarywriter , isfirst );
                        if ( isfirst ) isfirst = false;
                    }
                    
                    // Close summary writer
                    summarywriter.flush();
                    summarywriter.close();
                    
                    // Return true for success
                    return true;
                }
            } );
        } catch ( NumberFormatException e ) {
            displayError("Failed to parse length paramter - only integers are valid");
            return;
        }
    }//GEN-LAST:event_btnLengthParameterActionPerformed
    
    private void btnAddTestCaseActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAddTestCaseActionPerformed
        // Get the test case
        TestCase testcase = this.createTestCase();
        if ( testcase==null ) return;
        
        // Add it to the list
        testcases.add( testcase );
        
        // Update table
        int index = testcases.size() - 1;
        testcasemodel.fireTableRowsInserted(index, index);
        
        // Save testcases
        try {
            saveTestcases();
        } catch ( IOException e ) {
            displayError(e);
            return;
        }
    }//GEN-LAST:event_btnAddTestCaseActionPerformed
    
    public class TestCaseTableModel extends AbstractTableModel {
        public int getColumnCount() { return 7; }
        public String getColumnName(int col) {
            switch ( col ) {
                case 0: return "Testcase";
                case 1: return "Length Cutoff";
                case 2: return "# of Shape Points";
                case 3: return "# of Internal Points";
                case 4: return "Shape Point Min Density";
                case 5: return "Internal Min Density";
                case 6: return "Add Shape Points";
                default: return null;
            }
        }
        
        private Font font;
        private String letter;
        
        private String countryfile;
        
        private int maxdensity;
        private int numberofpoints;
        private int lengthcutoff;
        
        private boolean addshapepointstosplitlonglines;
        
        public int getRowCount() {
            return testcases.size();
        }
        public Class getColumnClass(int col) {
            return String.class;
        }
        public Object getValueAt(int row, int col) {
            // Get testcase
            TestCase testcase = testcases.get(row);
            
            // Return value
            switch ( col ) {
                case 0:
                    if ( testcase.font!=null && testcase.letter!=null && testcase.letter.trim().length()==1 ) {
                        return "Letter " + testcase.letter + " (" + testcase.font.getName() + ")";
                    } else if ( testcase.countryfile!=null && testcase.countryfile.trim().length()>=1 ) {
                        return CountryListModel.formatHumanReadable(testcase.countryfile);
                    } else {
                        return "n/a";
                    }
                case 1:
                    if ( mode==MODE_LENGTH || mode==MODE_NORMALISEDLENGTH ) {
                        return "n/a";
                    } else if ( testcase.lengthcutoff==CUTOFF_NONE ) {
                        return "None";
                    } else if ( testcase.lengthcutoff==CUTOFF_MAXEDGEOFMST ) {
                        return "Max Edge of MST";
                    } else if ( testcase.lengthcutoff==CUTOFF_LARGESTSMALLESTTRIANGLEDGE ) {
                        return "Max Smallest Triangle Edge";
                    } else if ( testcase.lengthcutoff>=0 ) {
                        return Integer.toString(testcase.lengthcutoff);
                    } else {
                        return "n/a";
                    }
                case 2:
                    if ( testcase.shapepoints<=0 ) {
                        return "No Points";
                    } else if ( testcase.shapepoints>=Integer.MAX_VALUE ) {
                        return "Max Possible";
                    } else {
                        return Integer.toString(testcase.shapepoints);
                    }
                case 3:
                    if ( testcase.internalpoints<=0 ) {
                        return "No Points";
                    } else if ( testcase.internalpoints>=Integer.MAX_VALUE ) {
                        return "Max Possible";
                    } else {
                        return Integer.toString(testcase.internalpoints);
                    }
                case 4:
                    if ( mode==MODE_DENSITY ) {
                        return "n/a";
                    } else {
                        return Integer.toString(testcase.shapepoint_mindensity);
                    }
                case 5:
                    if ( mode==MODE_DENSITY ) {
                        return "n/a";
                    } else {
                        return Integer.toString(testcase.internal_mindensity);
                    }
                case 6:
                    return ( testcase.addshapepointstosplitlonglines ? "Yes" : "No" );
            }
            
            // Otherwise, return null
            return null;
        }
    }
    
    private void optCountryGenerationActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_optCountryGenerationActionPerformed
        // By default, don't split long lines for countries
        chkAddShapePointsToSplitLongLines.setSelected(false);
        
        // Setup panel combo box
        panelGenerationSelection.removeAll();
        panelGenerationSelection.add( cboCountries );
        panelGenerationSelection.validate();
        panelGenerationSelection.repaint();
    }//GEN-LAST:event_optCountryGenerationActionPerformed
    
    private void optLetterGenerationActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_optLetterGenerationActionPerformed
        // By default, split long lines for letters
        chkAddShapePointsToSplitLongLines.setSelected(true);
        
        // Setup letter text field
        panelGenerationSelection.removeAll();
        panelGenerationSelection.add( txtLetter );
        panelGenerationSelection.validate();
        panelGenerationSelection.repaint();
    }//GEN-LAST:event_optLetterGenerationActionPerformed
    
    public static void main(String args[]) {
        // Set look and feel
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            displayError(null, e);
        }
        
        // Load test form
        TestSuite dialog = new TestSuite(true, new javax.swing.JFrame());
        dialog.setVisible(true);
    }
    
    private void runTestSuite(final RunTestSeries testseries) {
        // Update status
        lblStatus.setText("Starting test suite run...");
        
        // Get variables
        int _width, _height, _timestoexecute;
        try {
            _width  = Integer.parseInt( txtWidth.getText()  );
        } catch ( NumberFormatException e ) {
            displayError("Invalid width entered");
            return;
        }
        try {
            _height = Integer.parseInt( txtHeight.getText() );
        } catch ( NumberFormatException e ) {
            displayError("Invalid width entered");
            return;
        }
        try {
            _timestoexecute = Integer.parseInt( txtTimesToExecuteEachTestcase.getText() );
        } catch ( NumberFormatException e ) {
            displayError("Invalid width entered");
            return;
        }
        final int width=_width;
        final int height=_height;
        final int timestoexecute=_timestoexecute;
        
        String stroutputdir = txtOutputDir.getText();
        if ( stroutputdir==null || stroutputdir.trim().length()<=0 ) {
            displayError("Must enter output directory");
            return;
        }
        final File outputdir = new File(stroutputdir);
        if ( outputdir.isDirectory()==false && outputdir.mkdirs()==false ) {
            displayError("Failed to required create directory; " + outputdir.getAbsolutePath());
            return;
        }
        
        // Set up progress bar
        progressOverall.setMinimum( 1 );
        progressOverall.setMaximum( testcases.size() );
        
        // Run all testcases
        StatusDialog.start(this,"Running Testsuite", "Please wait for testsuite to complete...", new Thread() {
            public void run() {
                // Run test suite
                try {
                    boolean success;
                    for ( int x=1 ; x<=testcases.size() ; x++ ) {
                        progressOverall.setValue( x );
                        tblTestcases.setRowSelectionInterval(x-1, x-1);
                        
                        TestCase testcase = testcases.get(x-1);
                        success = testseries.runTestSeries(progressTestcases, testcase, width, height, outputdir, timestoexecute);
                        if ( success==false ) {
                            // Update status and return
                            lblStatus.setText("An error occurred while executing the test suite." );
                            return;
                        }
                    }
                } catch ( IOException e ) {
                    // Update status
                    lblStatus.setText(e.getClass().getName() + ": " + e.getMessage());
                    
                    // Display error
                    displayError(e);
                    return;
                }
                
                // Update status
                lblStatus.setText(null);
                
                // Notify MUTEX
                synchronized ( MUTEX ) {
                    if ( MUTEX!=null ) {
                        MUTEX.notifyAll();
                    }
                }
            }
        } );
    }
    
    private boolean executeTestCase(BufferedWriter writer,
            ValueCollector values, int width, int height,
            Font font, String letter, String countryfile,
            int shapepoints, int shapepoint_mindensity,
            int internalpoints, int internal_mindensity,
            int lengthcutoff, TriangulationRepresentation.CalcCutOff lengthcutoffcalc,
            boolean addshapepointstosplitlonglines) throws IOException {
        
        // Print text layout
        Rectangle shapebounds = new Rectangle(0, 0, width, height);
        
        // Create the shape
        String testcasename;
        ArrayList<VPoint> borderpoints;
        if ( font!=null && letter!=null && letter.trim().length()==1 ) {
            try {
                testcasename = "Letter " + letter + " (" + font.getName() + ")";
                borderpoints = ShapeGeneration.createShapeOutline(letter.trim(), shapebounds, font);
            } catch ( ShapeGenerationException e ) {
                displayError(e);
                return false;
            }
        } else if ( countryfile!=null && countryfile.trim().length()>=1 ) {
            try {
                testcasename = CountryListModel.formatHumanReadable(countryfile);
                borderpoints = CountryData.getCountryData(countryfile, shapebounds);
            } catch ( IOException e ) {
                displayError(e);
                return false;
            }
        } else {
            displayError("Unknown shape to generate for testcase");
            return false;
        }
        
        // Generate random points
        ArrayList<VPoint> points = null;
        try {
            boolean splitlonglines = addshapepointstosplitlonglines;
            points = ShapeGeneration.addRandomPoints(borderpoints, splitlonglines,
                    shapepoints, shapepoint_mindensity,
                    internalpoints, internal_mindensity);
        } catch ( ShapeGenerationException e ) {
            displayError(e);
            return false;
        }
        
        // Create cutoff calculator
        TriangulationRepresentation representation;
        if ( lengthcutoffcalc!=null ) {
            representation = new TriangulationRepresentation( lengthcutoffcalc );
        } else if ( lengthcutoff==CUTOFF_NONE ) {
            representation = new TriangulationRepresentation( 0 );
        } else if ( lengthcutoff==CUTOFF_MAXEDGEOFMST ) {
            representation = new TriangulationRepresentation( new TriangulationRepresentation.CalcCutOff() {
                public int calculateCutOff(TriangulationRepresentation rep) {
                    return rep.getMaxLengthOfMinimumSpanningTree();
                }
            } );
        } else if ( lengthcutoff==CUTOFF_LARGESTSMALLESTTRIANGLEDGE ) {
            representation = new TriangulationRepresentation( new TriangulationRepresentation.CalcCutOff() {
                public int calculateCutOff(TriangulationRepresentation rep) {
                    return rep.getMaxLengthOfSmallestTriangleEdge();
                }
            } );
        } else if ( lengthcutoff>=0 ) {
            representation = new TriangulationRepresentation( lengthcutoff );
        } else {
            displayError("Unknown length cut-off enter - \"" + lengthcutoff + "\"");
            return false;
        }
        
        try {
            // Convert points to the right form
            points = RepresentationFactory.convertPointsToTriangulationPoints(points);
            
            // Run the algorithm
            VoronoiAlgorithm.generateVoronoi(representation, points);
        } catch ( Error e ) {
            displayError(e);
            return false;
        } catch ( RuntimeException e ) {
            displayError(e);
            return false;
        }
        
        // Get the actual length cut-off
        lengthcutoff = representation.calculateLengthCutoff();
        
        // Get variables returns from running the algorithm
        double minlength = representation.getMinLength();
        double maxlength = representation.getMaxLength();
        double maxlengthofminimumspanningtree = representation.getMaxLengthOfMinimumSpanningTree();
        double maxlengthofsmallesttriangleedge = representation.getMaxLengthOfSmallestTriangleEdge();
        double normalizedlengthparameter = (lengthcutoff-minlength)/(maxlength-minlength);
        
        // Determine l2norm area
        Area area = ShapeGeneration.createArea( borderpoints );
        ArrayList outterpoints = representation.getPointsFormingOutterBoundary();
        area.exclusiveOr( ShapeGeneration.createArea( outterpoints ) );
        
        // Calculate expected/actual area and perimeter
        double expectedarea      = VoronoiShared.calculateAreaOfShape(borderpoints);
        double actualarea        = VoronoiShared.calculateAreaOfShape(outterpoints);
        double expectedperimeter = VoronoiShared.calculatePerimeterOfShape(borderpoints);
        double actualperimeter   = VoronoiShared.calculatePerimeterOfShape(outterpoints);
        
        // Calculate l2norm and it's error
        double l2norm = VoronoiShared.calculateAreaOfShape(area.getPathIterator(null));
        double error = l2norm / expectedarea * 100;
        
        // Output CSV Row
        writeColumn( writer ,  0 , values , testcasename );
        
        // Arguments to test function
        writeColumn( writer ,  1 , values , width );
        writeColumn( writer ,  2 , values , height );
        writeColumn( writer ,  3 , values , shapepoints );
        writeColumn( writer ,  4 , values , shapepoint_mindensity );
        writeColumn( writer ,  5 , values , internalpoints );
        writeColumn( writer ,  6 , values , internal_mindensity );
        writeColumn( writer ,  7 , values , points.size()  ); // Actual Number of Points
        writeColumn( writer ,  8 , values , lengthcutoff );
        writeColumn( writer ,  9 , values , normalizedlengthparameter );
        writeColumn( writer , 10 , values , ( addshapepointstosplitlonglines ? "Yes" : "No" ) );
        
        // Variables calculated
        writeColumn( writer , 11 , values , l2norm );
        writeColumn( writer , 12 , values , error );
        
        writeColumn( writer , 13 , values , expectedarea );
        writeColumn( writer , 14 , values , actualarea );
        writeColumn( writer , 15 , values , expectedperimeter );
        writeColumn( writer , 16 , values , actualperimeter );
        
        writeColumn( writer , 17 , values , minlength );
        writeColumn( writer , 18 , values , maxlengthofminimumspanningtree );
        writeColumn( writer , 19 , values , maxlengthofsmallesttriangleedge );
        writeColumn( writer , 20 , values , maxlength );
        
        // Write new line and return success
        writer.newLine();
        return true;
    }
    
    private void writeHeadingRow(BufferedWriter writer, ValueCollector values) throws IOException {
        // Output CSV Heading Row
        writeHeadingColumn( writer ,  0 , values , false , "Testcase Name" );
        
        // Arguments to test function
        writeHeadingColumn( writer ,  1 , values , false , "Width" );
        writeHeadingColumn( writer ,  2 , values , false , "Height" );
        writeHeadingColumn( writer ,  3 , values , false , "Shape Points" );
        writeHeadingColumn( writer ,  4 , values , false , "SP Min Density" );
        writeHeadingColumn( writer ,  5 , values , false , "Internal Points" );
        writeHeadingColumn( writer ,  6 , values , false , "IP Min Density" );
        writeHeadingColumn( writer ,  7 , values ,  true , "Actual Number of Points in Testcase" );
        writeHeadingColumn( writer ,  8 , values ,  true , "Length Cut-off Used" );
        writeHeadingColumn( writer ,  9 , values ,  true , "Normalized Length Parameter" );
        writeHeadingColumn( writer , 10 , values , false , "Shape Points added to Split Long Lines" );
        
        // Variables calculated
        writeHeadingColumn( writer , 11 , values ,  true , "L2-Norm Value" );
        writeHeadingColumn( writer , 12 , values ,  true , "Error from Expected Area" );
        
        writeHeadingColumn( writer , 13 , values , false , "Expected Area" );
        writeHeadingColumn( writer , 14 , values ,  true , "Actual Area" );
        writeHeadingColumn( writer , 15 , values , false , "Expected Perimeter" );
        writeHeadingColumn( writer , 16 , values ,  true , "Actual Perimeter" );
        
        writeHeadingColumn( writer , 17 , values ,  true , "Min Length" );
        writeHeadingColumn( writer , 18 , values ,  true , "Max Length of Minimum Spanning Tree" );
        writeHeadingColumn( writer , 19 , values ,  true , "Max Length of Smallest Triangle Edge" );
        writeHeadingColumn( writer , 20 , values ,  true , "Max Length" );
        
        // Finally write new line
        writer.newLine();
    }
    
    private static void writeColumn(BufferedWriter writer, int value) throws IOException {
        writer.write(Integer.toString(value));
        writer.write(",");
    }
    private static void writeColumn(BufferedWriter writer, double value) throws IOException {
        writer.write(Double.toString(value));
        writer.write(",");
    }
    private static void writeColumn(BufferedWriter writer, String string) throws IOException {
        if ( string==null ) {
            writer.write("Null Value");
        } else {
            writer.write(string.replaceAll(",", "\\,"));
        }
        writer.write(",");
    }
    
    private static void writeColumn(BufferedWriter writer, int column, ValueCollector values, int value) throws IOException {
        values.newValue(column, value);
        writer.write(Integer.toString(value));
        writer.write(",");
    }
    private static void writeColumn(BufferedWriter writer, int column, ValueCollector values, double value) throws IOException {
        values.newValue(column, value);
        writer.write(Double.toString(value));
        writer.write(",");
    }
    private static void writeColumn(BufferedWriter writer, int column, ValueCollector values, String string) throws IOException {
        values.newValue(column, string);
        if ( string==null ) {
            writer.write("Null Value");
        } else {
            writer.write(string.replaceAll(",", "\\,"));
        }
        writer.write(",");
    }
    
    private static void writeHeadingColumn(BufferedWriter writer, int column, ValueCollector values, boolean variance, String string) throws IOException {
        values.setHeading(column, string, variance);
        if ( string==null ) {
            writer.write("Null Value");
        } else {
            writer.write(string.replaceAll(",", "\\,"));
        }
        writer.write(",");
    }
    
    public TestCase createTestCase() {
        TestCase testcase = new TestCase();
        
        // Setup testcase
        if ( optLetterGeneration.isSelected() ) {
            // Get letter
            String letter = txtLetter.getText().trim();
            if ( letter.length()!=1 ) {
                displayError("Must enter in a single letter");
                return null;
            }
            testcase.letter = letter;
            
            // Get font
            String strfont = ((String) cboFont.getSelectedItem()).trim();
            if ( strfont.length()<=0 ) {
                displayError("Must enter in a font name for letter");
                return null;
            }
            Font font;
            try {
                font = new Font(strfont,Font.BOLD,200);
            } catch ( RuntimeException e ) {
                displayError(e);
                return null;
            }
            testcase.font = font;
        } else if ( optCountryGeneration.isSelected() ) {
            testcase.countryfile = (String) cboCountries.getSelectedItem();
        } else {
            displayError("Unknown shape generation option selected");
            return null;
        }
        
        // Set shape point properties
        try {
            testcase.shapepoint_mindensity = (int) Double.parseDouble( (String)cboShapePointMinDensity.getSelectedItem() );
        } catch ( NumberFormatException e ) {
            displayError("Invalid shape point min density entered");
            return null;
        }
        String strshapepoints = ((String)cboShapePoints.getSelectedItem()).toLowerCase();
        if ( strshapepoints.startsWith("n") ) {
            testcase.shapepoints = 0;
        } else if ( strshapepoints.startsWith("m") ) {
            testcase.shapepoints = Integer.MAX_VALUE;
        } else {
            try {
                testcase.shapepoints = Integer.parseInt(strshapepoints);
            } catch ( NumberFormatException e ) {
                displayError("Invalid number of shape points entered");
                return null;
            }
        }
        
        // Set internal properties
        try {
            testcase.internal_mindensity = (int) Double.parseDouble( (String)cboInternalMinDensity.getSelectedItem() );
        } catch ( NumberFormatException e ) {
            displayError("Invalid internal min density entered");
            return null;
        }
        String strinternalpoints = ((String)cboInternalPoints.getSelectedItem()).toLowerCase();
        if ( strinternalpoints.startsWith("n") ) {
            testcase.internalpoints = 0;
        } else if ( strinternalpoints.startsWith("m") ) {
            testcase.internalpoints = Integer.MAX_VALUE;
        } else {
            try {
                testcase.internalpoints = Integer.parseInt(strinternalpoints);
            } catch ( NumberFormatException e ) {
                displayError("Invalid number of internal points entered");
                return null;
            }
        }
        
        // Get length cutoff
        String strlengthcutoff = ((String)cboLengthCutoff.getSelectedItem()).toLowerCase();
        if ( strlengthcutoff.startsWith("n") ) {
            testcase.lengthcutoff = CUTOFF_NONE;
        } else if ( strlengthcutoff.startsWith("m") ) {
            testcase.lengthcutoff = CUTOFF_MAXEDGEOFMST;
        } else if ( strlengthcutoff.startsWith("l") ) {
            testcase.lengthcutoff = CUTOFF_LARGESTSMALLESTTRIANGLEDGE;
        } else {
            try {
                testcase.lengthcutoff = Integer.parseInt(strlengthcutoff);
            } catch ( NumberFormatException e ) {
                displayError("Invalid number of internal points entered");
                return null;
            }
        }
        testcase.addshapepointstosplitlonglines = chkAddShapePointsToSplitLongLines.isSelected();
        
        // Return the testcase
        return testcase;
    }
    
    public class ValueCollector {
        private String[] heading;
        private boolean[] calculatevariance;
        
        private int[]    count;
        private String[] caption;
        private double[] sumvalues;
        private double[] valuessquared;
        
        public ValueCollector(int size) {
            // Create arrays
            heading           = new String[size];
            calculatevariance = new boolean[size];
            count             = new int[size];
            caption           = new String[size];
            sumvalues         = new double[size];
            valuessquared     = new double[size];
            
            // Reset values
            reset();
        }
        
        public void reset() {
            for ( int x=0 ; x<heading.length ; x++ ) {
                //heading[x]           = null;
                //calculatevariance[x] = false;
                count[x]             = 0;
                caption[x]           = null;
                sumvalues[x]         = 0.0;
                valuessquared[x]     = 0.0;
            }
        }
        
        public String getHeading(int column) {
            return heading[column];
        }
        public boolean hasStandardDeviation(int column) {
            return calculatevariance[column];
        }
        public void setHeading(int column, String strheading, boolean hasvariance) {
            if ( strheading==null ) {
                throw new IllegalArgumentException("Null value given for heading");
            }
            heading[column] = strheading;
            calculatevariance[column] = hasvariance;
        }
        
        public void newValue(int column, int value) {
            count[column]++;
            sumvalues[column] += value;
            valuessquared[column] += value*value;
        }
        public void newValue(int column, double value) {
            count[column]++;
            sumvalues[column] += value;
            valuessquared[column] += value*value;
        }
        public void newValue(int column, String text) {
            caption[column] = text;
        }
        
        public boolean isCaption(int column) {
            return ( caption[column]!=null );
        }
        public boolean isValue(int column) {
            return ( caption[column]==null && count[column]>0 );
        }
        
        public String getCaption(int column) {
            return caption[column];
        }
        public double getAverage(int column) {
            return sumvalues[column] / count[column];
        }
        public double getStandardDeviation(int column) {
            double num = (double) count[column];
            return Math.sqrt( ( (num*valuessquared[column]) - (sumvalues[column]*sumvalues[column]) ) / ( num*(num-1) ) );
        }
        
        public void writeSummary( BufferedWriter writer , boolean isfirst ) throws IOException {
            int column;
            
            // Write heading row
            if ( isfirst ) {
                for ( column=0 ; column<heading.length ; column++ ) {
                    if ( isCaption(column) ) {
                        writeColumn( writer , getHeading(column) );
                    } else {
                        writeColumn( writer , getHeading(column) );
                        if ( calculatevariance[column] ) {
                            writeColumn( writer , "Std Deviation" );
                        }
                    }
                }
                writer.newLine();
            }
            
            // Write value row
            for ( column=0 ; column<heading.length ; column++ ) {
                if ( isCaption(column) ) {
                    writeColumn( writer , getCaption(column) );
                } else {
                    writeColumn( writer , getAverage(column) );
                    if ( calculatevariance[column] ) {
                        writeColumn( writer , getStandardDeviation(column) );
                    }
                }
            }
            writer.newLine();
        }
        
    }
    
    public abstract class RunTestSeries {
        
        abstract public boolean runTestSeries(JProgressBar progress, TestCase testcase, int width, int height, File outputdir, int timestoexecute) throws IOException;
        
    }
    
    public static class TestCase implements Serializable {
        private Font font;
        private String letter;
        
        private String countryfile;
        
        private int lengthcutoff;
        
        private int shapepoints, shapepoint_mindensity;
        private int internalpoints, internal_mindensity;
        
        // Default to true
        private boolean addshapepointstosplitlonglines;
        
        public TestCase() { }
        public TestCase(String letter, String font, int lengthcutoff, int mindensity, int numberofpoints) {
            this(letter, new Font(font,Font.BOLD,200), lengthcutoff, mindensity, numberofpoints);
            this.addshapepointstosplitlonglines = true;
        }
        public TestCase(String _letter, Font _font, int lengthcutoff, int mindensity, int numberofpoints) {
            this(lengthcutoff, mindensity, numberofpoints);
            this.letter = _letter;
            this.font = _font;
            this.addshapepointstosplitlonglines = true;
        }
        public TestCase(String _countryfile, int lengthcutoff, int mindensity, int numberofpoints) {
            this(lengthcutoff, mindensity, numberofpoints);
            this.countryfile = _countryfile;
            this.addshapepointstosplitlonglines = false;
        }
        public TestCase(int _lengthcutoff, int _mindensity, int _numberofpoints) {
            this.lengthcutoff = _lengthcutoff;
            
            this.shapepoints = _numberofpoints;
            this.internalpoints = _numberofpoints;
            
            this.shapepoint_mindensity = _mindensity;
            this.internal_mindensity    = _mindensity;
        }
    }
    
    private void displayError(String message) {
        JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
    }
    static private void displayError(JFrame parent, String message) {
        JOptionPane.showMessageDialog(parent, message, "Error", JOptionPane.ERROR_MESSAGE);
    }
    
    private void displayError(Throwable e) {
        e.printStackTrace();
        JOptionPane.showMessageDialog(this, e.getMessage(), e.getClass().getName(), JOptionPane.ERROR_MESSAGE);
    }
    static private void displayError(JFrame parent, Throwable e) {
        e.printStackTrace();
        JOptionPane.showMessageDialog(parent, e.getMessage(), e.getClass().getName(), JOptionPane.ERROR_MESSAGE);
    }
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnAddTestCase;
    private javax.swing.JButton btnDeleteSelected;
    private javax.swing.JButton btnDensityParameter;
    private javax.swing.JButton btnLengthParameter;
    private javax.swing.JButton btnMoveDown;
    private javax.swing.JButton btnMoveUp;
    private javax.swing.JButton btnNormalisedLengthParameter;
    private javax.swing.JButton btnRunAll;
    private javax.swing.JComboBox cboCountries;
    private javax.swing.JComboBox cboFont;
    private javax.swing.JComboBox cboInternalMinDensity;
    private javax.swing.JComboBox cboInternalPoints;
    private javax.swing.JComboBox cboLengthCutoff;
    private javax.swing.JComboBox cboShapePointMinDensity;
    private javax.swing.JComboBox cboShapePoints;
    private javax.swing.JCheckBox chkAddShapePointsToSplitLongLines;
    private javax.swing.JCheckBox chkRunDensity;
    private javax.swing.JCheckBox chkRunLength;
    private javax.swing.JCheckBox chkRunNormalisedLength;
    private javax.swing.ButtonGroup groupGenerationType;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JPanel jPanel6;
    private javax.swing.JPanel jPanel7;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JLabel lblDensityIncrements;
    private javax.swing.JLabel lblEndDensity;
    private javax.swing.JLabel lblEndLength;
    private javax.swing.JLabel lblEndLength1;
    private javax.swing.JLabel lblFont2;
    private javax.swing.JLabel lblGenerationType2;
    private javax.swing.JLabel lblHeight;
    private javax.swing.JLabel lblInternalMinDensity;
    private javax.swing.JLabel lblInternalPoints;
    private javax.swing.JLabel lblLengthCutoff;
    private javax.swing.JLabel lblLengthIncrements;
    private javax.swing.JLabel lblLengthIncrements1;
    private javax.swing.JLabel lblOutputDir;
    private javax.swing.JLabel lblOverall;
    private javax.swing.JLabel lblPixels;
    private javax.swing.JLabel lblPixels1;
    private javax.swing.JLabel lblShapePointMinDensity;
    private javax.swing.JLabel lblShapePoints;
    private javax.swing.JLabel lblStartDensity;
    private javax.swing.JLabel lblStartLength;
    private javax.swing.JLabel lblStartLength1;
    private javax.swing.JLabel lblStatus;
    private javax.swing.JLabel lblTestcases;
    private javax.swing.JLabel lblTimesToExecuteEachTestcase;
    private javax.swing.JLabel lblWidth;
    private javax.swing.JRadioButton optCountryGeneration;
    private javax.swing.JRadioButton optLetterGeneration;
    private javax.swing.JPanel panelCaptions2;
    private javax.swing.JPanel panelCenter;
    private javax.swing.JPanel panelCenter1;
    private javax.swing.JPanel panelCenter2;
    private javax.swing.JPanel panelCenter4;
    private javax.swing.JPanel panelCenterPanel;
    private javax.swing.JPanel panelCenterPanel1;
    private javax.swing.JPanel panelCenterPanel2;
    private javax.swing.JPanel panelDensityParameter;
    private javax.swing.JPanel panelExecutionOptions;
    private javax.swing.JPanel panelGap1;
    private javax.swing.JPanel panelGap10;
    private javax.swing.JPanel panelGap11;
    private javax.swing.JPanel panelGap12;
    private javax.swing.JPanel panelGap13;
    private javax.swing.JPanel panelGap14;
    private javax.swing.JPanel panelGap15;
    private javax.swing.JPanel panelGap16;
    private javax.swing.JPanel panelGap2;
    private javax.swing.JPanel panelGap3;
    private javax.swing.JPanel panelGap4;
    private javax.swing.JPanel panelGap5;
    private javax.swing.JPanel panelGap6;
    private javax.swing.JPanel panelGap7;
    private javax.swing.JPanel panelGap8;
    private javax.swing.JPanel panelGap9;
    private javax.swing.JPanel panelGenerate2;
    private javax.swing.JPanel panelGenerationSelection;
    private javax.swing.JPanel panelGenerationType2;
    private javax.swing.JPanel panelInCenter;
    private javax.swing.JPanel panelLeft;
    private javax.swing.JPanel panelLeft1;
    private javax.swing.JPanel panelLeft2;
    private javax.swing.JPanel panelLeft4;
    private javax.swing.JPanel panelLengthParameter;
    private javax.swing.JPanel panelLengthParameters;
    private javax.swing.JPanel panelLengthParameters1;
    private javax.swing.JPanel panelLengthParameters2;
    private javax.swing.JPanel panelNormalisedLengthParameter;
    private javax.swing.JPanel panelNorth;
    private javax.swing.JPanel panelNorth1;
    private javax.swing.JPanel panelNorth2;
    private javax.swing.JPanel panelNorth3;
    private javax.swing.JPanel panelOnRight;
    private javax.swing.JPanel panelOtherOptions;
    private javax.swing.JPanel panelPointOptions2;
    private javax.swing.JPanel panelRunAll;
    private javax.swing.JPanel panelRunAllCheckBoxes;
    private javax.swing.JPanel panelStatus;
    private javax.swing.JPanel panelStatusOutter;
    private javax.swing.JPanel panelTextfields;
    private javax.swing.JPanel panelUpNorth;
    private javax.swing.JPanel panelVariables;
    private javax.swing.JProgressBar progressOverall;
    private javax.swing.JProgressBar progressTestcases;
    private javax.swing.JScrollPane scrollPane;
    private javax.swing.JTabbedPane tabsTestCaseType;
    private javax.swing.JTable tblTestcases;
    private javax.swing.JTextField txtDensityIncrements;
    private javax.swing.JTextField txtEndDensity;
    private javax.swing.JTextField txtEndLength;
    private javax.swing.JTextField txtHeight;
    private javax.swing.JTextField txtLengthIncrements;
    private javax.swing.JTextField txtLetter;
    private javax.swing.JTextField txtNormalisedEndLength;
    private javax.swing.JTextField txtNormalisedLengthIncrements;
    private javax.swing.JTextField txtNormalisedStartLength;
    private javax.swing.JTextField txtOutputDir;
    private javax.swing.JTextField txtStartDensity;
    private javax.swing.JTextField txtStartLength;
    private javax.swing.JTextField txtTimesToExecuteEachTestcase;
    private javax.swing.JTextField txtWidth;
    // End of variables declaration//GEN-END:variables
    
}
