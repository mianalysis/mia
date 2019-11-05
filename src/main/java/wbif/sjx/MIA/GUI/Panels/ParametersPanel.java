package wbif.sjx.MIA.GUI.Panels;

import wbif.sjx.MIA.GUI.ComponentFactory;
import wbif.sjx.MIA.GUI.ParameterControls.RemoveParametersButton;
import wbif.sjx.MIA.MIA;
import wbif.sjx.MIA.Module.Hidden.InputControl;
import wbif.sjx.MIA.Module.Hidden.OutputControl;
import wbif.sjx.MIA.GUI.GUI;
import wbif.sjx.MIA.Module.Module;
import wbif.sjx.MIA.Module.ModuleCollection;
import wbif.sjx.MIA.Object.References.Abstract.ExportableRef;
import wbif.sjx.MIA.Object.References.Abstract.RefCollection;
import wbif.sjx.MIA.Object.References.Abstract.SummaryRef;
import wbif.sjx.MIA.Object.References.ImageMeasurementRefCollection;
import wbif.sjx.MIA.Object.References.ObjMeasurementRefCollection;
import wbif.sjx.MIA.Object.Parameters.*;
import wbif.sjx.MIA.Object.Parameters.Abstract.Parameter;
import wbif.sjx.MIA.Object.References.MetadataRefCollection;
import wbif.sjx.MIA.Object.References.RelationshipRefCollection;
import wbif.sjx.MIA.Process.AnalysisHandling.Analysis;

import javax.swing.*;
import javax.swing.border.EtchedBorder;
import java.awt.*;
import java.util.LinkedHashSet;

public class ParametersPanel extends JScrollPane {
    private static final int minimumWidth = 300;
    private static final int preferredWidth = 500;

    private JPanel panel;

    public ParametersPanel() {
        panel = new JPanel();
        setViewportView(panel);

        // Initialising the scroll panel
        setBorder(BorderFactory.createEtchedBorder(EtchedBorder.RAISED));
        setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        getVerticalScrollBar().setUnitIncrement(10);
        setMinimumSize(new Dimension(minimumWidth,1));
        setPreferredSize(new Dimension(preferredWidth,1));

        panel.setLayout(new GridBagLayout());
        panel.validate();
        panel.repaint();

        validate();
        repaint();

    }

    public void updatePanel(Module module) {
        Analysis analysis = GUI.getAnalysis();
        ModuleCollection modules = GUI.getModules();

        ComponentFactory componentFactory = GUI.getComponentFactory();
        InputControl inputControl = analysis.getModules().getInputControl();
        OutputControl outputControl = analysis.getModules().getOutputControl();

        panel.removeAll();

        GridBagConstraints c = new GridBagConstraints();
        c.gridy = 0;
        c.weightx = 1;
        c.weighty = 0;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.insets = new Insets(5, 5, 20, 0);
        c.anchor = GridBagConstraints.WEST;

        // If the active module is set to null (i.e. we're looking at the analysis options panel) exit this method
        if (module == null) {
            showUsageMessage();
            return;
        }

        JPanel topPanel = componentFactory.createParametersTopRow(module);
        c.gridwidth = 2;
        panel.add(topPanel,c);

        // If it's an input/output control, get the current version
        if (module.getClass().isInstance(new InputControl(modules))) module = inputControl;
        if (module.getClass().isInstance(new OutputControl(modules))) module = outputControl;

        // If the active module hasn't got parameters enabled, skip it
        c.anchor = GridBagConstraints.NORTHWEST;
        c.gridwidth = 1;
        c.insets = new Insets(2, 5, 0, 0);
        if (module.updateAndGetParameters() != null) {
            for (Parameter parameter : module.updateAndGetParameters().values()) {
                if (parameter.getClass() == ParameterGroup.class) {
                    addAdvancedParameterGroup((ParameterGroup) parameter,module,c);
                } else {
                    addAdvancedParameterControl(parameter,c);
                }
            }
        }

        // If selected, adding the measurement selector for output control
        String exportMode = outputControl.getParameterValue(OutputControl.EXPORT_MODE);
        if (module.getClass().isInstance(new OutputControl(modules)) && outputControl.isEnabled() &! exportMode.equals(OutputControl.ExportModes.NONE)) {
            MetadataRefCollection metadataRefs = modules.getMetadataRefs();
            addRefExportControls(metadataRefs,"Metadata",componentFactory,c);

            LinkedHashSet<OutputImageP> imageNameParameters = modules.getAvailableImages(null);
            for (OutputImageP imageNameParameter:imageNameParameters) {
                String imageName = imageNameParameter.getImageName();
                ImageMeasurementRefCollection measurementReferences = modules.getImageMeasurementRefs(imageName);
                addRefExportControls(measurementReferences,imageName+" (Image)",componentFactory,c);
            }

            LinkedHashSet<OutputObjectsP> objectNameParameters = modules.getAvailableObjects(null);
            for (OutputObjectsP objectNameParameter:objectNameParameters) {
                String objectName = objectNameParameter.getObjectsName();
                ObjMeasurementRefCollection measurementReferences = modules.getObjectMeasurementRefs(objectName);
                addSummaryRefExportControls(measurementReferences,objectName+" (Object)",componentFactory,c);
            }

            RelationshipRefCollection relationshipRefs = modules.getRelationshipRefs();
            addSummaryRefExportControls(relationshipRefs,"Number of children",componentFactory,c);

        }

        JSeparator separator = new JSeparator();
        separator.setOpaque(true);
        separator.setSize(new Dimension(0,0));
        c.weighty = 1;
        c.gridy++;
        c.insets = new Insets(20,0,0,0);
        c.fill = GridBagConstraints.VERTICAL;
        panel.add(separator,c);

        panel.revalidate();
        panel.repaint();

        revalidate();
        repaint();

    }

    void addRefExportControls(RefCollection<? extends ExportableRef> refs, String header, ComponentFactory componentFactory, GridBagConstraints c) {
        if (refs.values().size() == 0) return;

        JPanel  measurementHeader = componentFactory.createRefExportHeader(header,refs,false);
        c.gridx = 0;
        c.gridy++;
        c.anchor = GridBagConstraints.WEST;
        panel.add(measurementHeader,c);

        // Iterating over the measurements for the current object, adding a control for each
        for (ExportableRef reference:refs.values()) {
            // Adding measurement control
            JPanel currentMeasurementPanel = componentFactory.createSingleRefControl(reference);
            c.gridy++;
            c.anchor = GridBagConstraints.EAST;
            panel.add(currentMeasurementPanel,c);

        }
    }

    void addSummaryRefExportControls(RefCollection<? extends SummaryRef> refs, String header, ComponentFactory componentFactory, GridBagConstraints c) {
        if (refs.values().size() == 0) return;

        JPanel  measurementHeader = componentFactory.createRefExportHeader(header,refs,true);
        c.gridx = 0;
        c.gridy++;
        c.anchor = GridBagConstraints.WEST;
        panel.add(measurementHeader,c);

        // Iterating over the measurements for the current object, adding a control for each
        for (SummaryRef reference:refs.values()) {
            // Adding measurement control
            JPanel currentMeasurementPanel = componentFactory.createSingleSummaryRefControl(reference);
            c.gridy++;
            c.anchor = GridBagConstraints.EAST;
            panel.add(currentMeasurementPanel,c);

        }
    }

    public void addAdvancedParameterControl(Parameter parameter, GridBagConstraints c) {
        ComponentFactory componentFactory = GUI.getComponentFactory();
        Module activeModule = GUI.getFirstSelectedModule();
        int elementHeight = GUI.getElementHeight();

        c.insets = new Insets(2, 5, 0, 0);
        c.gridx = 0;
        c.gridy++;
        c.weightx = 1;
        c.anchor = GridBagConstraints.WEST;

        JPanel paramPanel = componentFactory.createParameterControl(parameter, GUI.getModules(), activeModule, true);
        panel.add(paramPanel, c);

    }

    public void addAdvancedParameterGroup(ParameterGroup group, Module module, GridBagConstraints c) {
        // Iterating over each collection of Parameters.  After adding each one, a remove button is included
        LinkedHashSet<ParameterCollection> collections = group.getCollections();

        for (ParameterCollection collection:collections) {
            // Adding the individual parameters
            for (Parameter parameter:collection.values()) addAdvancedParameterControl(parameter,c);

            c.gridy++;
            RemoveParameters removeParameters = new RemoveParameters("Remove",module,group,collection);
            addAdvancedParameterControl(removeParameters,c);

            c.gridy++;
            panel.add(getInvisibleSeparator(), c);

        }

        // Adding an addRef button
        addAdvancedParameterControl(group,c);

        c.gridy++;
        panel.add(getInvisibleSeparator(), c);

    }

    public void showUsageMessage() {
        panel.removeAll();

        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 0;
        c.weightx = 1;
        c.weighty = 1;
        c.anchor = GridBagConstraints.CENTER;
        c.fill = GridBagConstraints.BOTH;

        // Adding title to help window
        JTextPane usageMessage = new JTextPane();
        usageMessage.setContentType("text/html");
        usageMessage.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
        usageMessage.setText("<html><center><font face=\"sans-serif\" size=\"3\">" +
                "To change parameters for an existing module, click the module name on the list to the left."+
                "<br><br>" +
                "Modules can be added, removed and re-ordered using the +, -, ▲ and ▼ buttons." +
                "<br><br>" +
                "Modules can also be disabled using the power icons to the left of each module name.  " +
                "<br><br>Any modules highlighted in red are currently mis-configured " +
                "<br>(possibly missing outputs from previous modules) and won't run." +
                "<br><br>" +
                "To execute a full analysis, click \"Run\".  " +
                "<br>Alternatively, step through an analysis using the arrow icons to the right of each module name." +
                "</font></center></html>");
        usageMessage.setEditable(false);
        usageMessage.setBackground(null);
        usageMessage.setOpaque(false);
        panel.add(usageMessage);

        panel.revalidate();
        panel.repaint();

    }

    private JSeparator getInvisibleSeparator() {
        JSeparator separator = new JSeparator();
        separator.setPreferredSize(new Dimension(0,15));
        separator.setForeground(panel.getBackground());
        separator.setBackground(panel.getBackground());

        return separator;

    }

    public static int getMinimumWidth() {
        return minimumWidth;
    }

    public static int getPreferredWidth() {
        return preferredWidth;
    }
}
