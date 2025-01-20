package io.github.mianalysis.mia.gui.regions.parameterlist;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTextPane;

import io.github.mianalysis.mia.gui.ComponentFactory;
import io.github.mianalysis.mia.gui.GUI;
import io.github.mianalysis.mia.module.Module;
import io.github.mianalysis.mia.module.Modules;
import io.github.mianalysis.mia.module.core.InputControl;
import io.github.mianalysis.mia.module.core.OutputControl;
import io.github.mianalysis.mia.object.parameters.AdjustParameters;
import io.github.mianalysis.mia.object.parameters.OutputImageP;
import io.github.mianalysis.mia.object.parameters.ParameterGroup;
import io.github.mianalysis.mia.object.parameters.Parameters;
import io.github.mianalysis.mia.object.parameters.abstrakt.Parameter;
import io.github.mianalysis.mia.object.parameters.objects.OutputObjectsP;
import io.github.mianalysis.mia.object.refs.abstrakt.ExportableRef;
import io.github.mianalysis.mia.object.refs.abstrakt.SummaryRef;
import io.github.mianalysis.mia.object.refs.collections.ImageMeasurementRefs;
import io.github.mianalysis.mia.object.refs.collections.MetadataRefs;
import io.github.mianalysis.mia.object.refs.collections.ObjMeasurementRefs;
import io.github.mianalysis.mia.object.refs.collections.ObjMetadataRefs;
import io.github.mianalysis.mia.object.refs.collections.Refs;

public class ParametersPanel extends JPanel {
    private static final long serialVersionUID = 1455273666893303846L;
    private static final int minimumWidth = 400;
    private static final int preferredWidth = 600;

    private JScrollPane scrollPane;
    private JPanel panel;

    public ParametersPanel() {
        setLayout(new BorderLayout(0,0));

        panel = new JPanel();
        panel.setOpaque(false);

        scrollPane = new JScrollPane(panel);
        scrollPane.setViewportView(panel);        

        // Initialising the scroll panel
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        scrollPane.setViewportBorder(BorderFactory.createEmptyBorder());
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.getVerticalScrollBar().setUnitIncrement(10);
        scrollPane.setMinimumSize(new Dimension(minimumWidth,1));
        scrollPane.setPreferredSize(new Dimension(preferredWidth,1));

        panel.setLayout(new GridBagLayout());
        panel.validate();
        panel.repaint();

        scrollPane.validate();
        scrollPane.repaint();

        add(scrollPane,BorderLayout.CENTER);

    }

    public void updatePanel(Module module) {
        Modules modules = GUI.getModules();

        ComponentFactory componentFactory = GUI.getComponentFactory();
        InputControl inputControl = modules.getInputControl();
        OutputControl outputControl = modules.getOutputControl();

        panel.removeAll();

        GridBagConstraints c = new GridBagConstraints();
        c.gridy = 0;
        c.weightx = 1;
        c.weighty = 0;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.insets = new Insets(10, 10, 20, 0);
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
        if (module instanceof InputControl) module = inputControl;
        if (module instanceof OutputControl) module = outputControl;

        // If the active module hasn't got parameters enabled, skip it
        c.anchor = GridBagConstraints.NORTHWEST;
        c.gridwidth = 1;
        c.insets = new Insets(2, 10, 0, 0);
        if (module.updateAndGetParameters() != null) {
            for (Parameter parameter : module.updateAndGetParameters().values()) {
                if (parameter.getClass() == ParameterGroup.class) {
                    addAdvancedParameterGroupControl((ParameterGroup) parameter,module,c);
                } else {
                    addAdvancedParameterControl(parameter,c);
                }
            }
        }

        // If selected, adding the measurement selector for output control
        String exportMode = outputControl.getParameterValue(OutputControl.EXPORT_MODE,null);
        if (module.getClass().isInstance(new OutputControl(modules)) && outputControl.isEnabled() &! exportMode.equals(OutputControl.ExportModes.NONE)) {
            MetadataRefs metadataRefs = modules.getMetadataRefs();
            addRefExportControls(metadataRefs,"Workspace metadata",componentFactory,c);

            LinkedHashSet<OutputImageP> imageNameParameters = modules.getAvailableImages(null,false);
            for (OutputImageP imageNameParameter:imageNameParameters) {
                String imageName = imageNameParameter.getImageName();
                ImageMeasurementRefs measurementReferences = modules.getImageMeasurementRefs(imageName);
                addRefExportControls(measurementReferences,"\""+imageName+"\" image measurements",componentFactory,c);
            }

            LinkedHashSet<OutputObjectsP> objectNameParameters = modules.getAvailableObjects(null,false);
            for (OutputObjectsP objectNameParameter:objectNameParameters) {
                String objectName = objectNameParameter.getObjectsName();
                ObjMetadataRefs objectMetadataReferences = modules.getObjectMetadataRefs(objectName);
                addRefExportControls(objectMetadataReferences,"\""+objectName+"\" object metadata",componentFactory,c);
                ObjMeasurementRefs measurementReferences = modules.getObjectMeasurementRefs(objectName);
                addSummaryRefExportControls(measurementReferences,"\""+objectName+"\" object measurements",componentFactory,c);
            }            
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

        scrollPane.revalidate();
        scrollPane.repaint();

    }

    void addRefExportControls(Refs<? extends ExportableRef> refs, String header, ComponentFactory componentFactory, GridBagConstraints c) {
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

    void addSummaryRefExportControls(Refs<? extends SummaryRef> refs, String header, ComponentFactory componentFactory, GridBagConstraints c) {
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

        c.insets = new Insets(2, 10, 0, 10);
        c.gridx = 0;
        c.gridy++;
        c.weightx = 1;
        c.anchor = GridBagConstraints.WEST;

        JPanel paramPanel = componentFactory.createParameterControl(parameter, GUI.getModules(), activeModule, true);
        panel.add(paramPanel, c);

    }

    public void addAdvancedParameterGroupControl(ParameterGroup group, Module module, GridBagConstraints c) {
        // Iterating over each collection of Parameters.  After adding each one, a remove button is included
        LinkedHashMap<Integer, Parameters> collections = group.getCollections(true);

        for (int collectionIdx : collections.keySet()) {
            Parameters collection = collections.get(collectionIdx);
            // Adding the individual parameters
            for (Parameter parameter:collection.values()) addAdvancedParameterControl(parameter,c);

            c.gridy++;
            AdjustParameters removeParameters = new AdjustParameters("",module,group,collectionIdx);
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
        usageMessage.setFont(GUI.getDefaultFont().deriveFont(14f));
        usageMessage.setText("<html><center>" +
                "To change parameters for an existing module," +
                "<br>click the module name on the list to the left."+
                "<br><br>" +
                "Modules can be added, removed and re-ordered using" +
                "<br>the +, -, ▲ and ▼ buttons." +
                "<br><br>" +
                "Modules can also be disabled using the power icons" +
                "<br>to the left of each module name.  " +
                "<br><br>Any modules highlighted in red are currently" +
                "<br>mis-configured (possibly missing outputs from " +
                "<br>previous modules) and won't run." +
                "<br><br>" +
                "To execute a full analysis, click \"Run\".  " +
                "<br>Alternatively, step through an analysis using the" +
                "<br>arrow icons to the right of each module name." +
                "</center></html>");
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
