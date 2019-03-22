package wbif.sjx.ModularImageAnalysis.GUI.Panels;

import wbif.sjx.ModularImageAnalysis.GUI.ComponentFactory;
import wbif.sjx.ModularImageAnalysis.GUI.ControlObjects.VisibleCheck;
import wbif.sjx.ModularImageAnalysis.GUI.InputOutput.InputControl;
import wbif.sjx.ModularImageAnalysis.GUI.InputOutput.OutputControl;
import wbif.sjx.ModularImageAnalysis.GUI.GUI;
import wbif.sjx.ModularImageAnalysis.Module.Module;
import wbif.sjx.ModularImageAnalysis.Object.MeasurementRef;
import wbif.sjx.ModularImageAnalysis.Object.MeasurementRefCollection;
import wbif.sjx.ModularImageAnalysis.Object.Parameters.*;
import wbif.sjx.ModularImageAnalysis.Object.Parameters.Abstract.Parameter;
import wbif.sjx.ModularImageAnalysis.Process.AnalysisHandling.Analysis;

import javax.swing.*;
import javax.swing.border.EtchedBorder;
import java.awt.*;
import java.util.LinkedHashSet;

public class ParametersPanel extends JScrollPane {
    private JPanel panel;

    public ParametersPanel() {
        panel = new JPanel();
        setViewportView(panel);

        int frameWidth = GUI.getMinimumFrameWidth();
        int bigButtonSize = GUI.getBigButtonSize();

        // Initialising the scroll panel
        setBorder(BorderFactory.createEtchedBorder(EtchedBorder.RAISED));
        setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        getVerticalScrollBar().setUnitIncrement(10);
        setPreferredSize(new Dimension(frameWidth-45-bigButtonSize, bigButtonSize+15));

        panel.setLayout(new GridBagLayout());
        panel.validate();
        panel.repaint();

        validate();
        repaint();

    }

    public void updatePanel(Module module) {
        Analysis analysis = GUI.getAnalysis();
        ComponentFactory componentFactory = GUI.getComponentFactory();
        MeasurementRef globalMeasurementRef = GUI.getGlobalMeasurementRef();

        panel.removeAll();

        GridBagConstraints c = new GridBagConstraints();
        c.gridy = 0;
        c.weightx = 1;
        c.weighty = 0;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.insets = new Insets(5, 5, 20, 0);
        c.anchor = GridBagConstraints.WEST;

        // If the active module is set to null (i.e. we're looking at the analysis options panel) exit this method
        if (module == null) return;

        boolean isInput = module.getClass().isInstance(new InputControl());
        boolean isOutput = module.getClass().isInstance(new OutputControl());

        JPanel topPanel = componentFactory.createParametersTopRow(module);
        c.gridwidth = 2;
        panel.add(topPanel,c);

        // If it's an input/output control, get the current version
        if (module.getClass().isInstance(new InputControl())) module = analysis.getInputControl();
        if (module.getClass().isInstance(new OutputControl())) module = analysis.getOutputControl();

        // If the active module hasn't got parameters enabled, skip it
        c.anchor = GridBagConstraints.NORTHWEST;
        c.gridwidth = 1;
        c.insets = new Insets(2, 5, 0, 0);
        if (module.updateAndGetParameters() != null) {
            for (Parameter parameter : module.updateAndGetParameters()) {
                if (parameter.getClass() == ParameterGroup.class) {
                    addAdvancedParameterGroup((ParameterGroup) parameter,c);
                } else {
                    addAdvancedParameterControl(parameter,c);
                }
            }
        }

        // If selected, adding the measurement selector for output control
        if (module.getClass().isInstance(new OutputControl())
                && analysis.getOutputControl().isEnabled()
                && ((BooleanP) analysis.getOutputControl().getParameter(OutputControl.SELECT_MEASUREMENTS)).isSelected()) {

            // Creating global controls for the different statistics
            JPanel measurementHeader = componentFactory.createMeasurementHeader("Global control",null);
            c.gridx = 0;
            c.gridy++;
            c.gridwidth = 2;
            c.fill = GridBagConstraints.HORIZONTAL;
            c.anchor = GridBagConstraints.WEST;
            panel.add(measurementHeader,c);

            JPanel currentMeasurementPanel = componentFactory.createGlobalMeasurementControl(globalMeasurementRef);
            c.gridy++;
            c.anchor = GridBagConstraints.EAST;
            panel.add(currentMeasurementPanel,c);

            LinkedHashSet<OutputImageP> imageNameParameters = analysis.getModules().getParametersMatchingType(OutputImageP.class);
            for (OutputImageP imageNameParameter:imageNameParameters) {
                String imageName = imageNameParameter.getImageName();
                MeasurementRefCollection measurementReferences = analysis.getModules().getImageMeasurementRefs(imageName);

                if (measurementReferences.size() == 0) continue;

                measurementHeader = componentFactory.createMeasurementHeader(imageName+" (Image)", measurementReferences);
                c.gridx = 0;
                c.gridy++;
                c.anchor = GridBagConstraints.WEST;
                panel.add(measurementHeader,c);

                // Iterating over the measurements for the current image, adding a control for each
                for (MeasurementRef measurementReference:measurementReferences.values()) {
                    if (!measurementReference.isCalculated()) continue;

                    // Adding measurement control
                    currentMeasurementPanel = componentFactory.createMeasurementControl(measurementReference);
                    c.gridy++;
                    c.anchor = GridBagConstraints.EAST;
                    panel.add(currentMeasurementPanel,c);

                }
            }

            LinkedHashSet<OutputObjectsP> objectNameParameters = analysis.getModules().getParametersMatchingType(OutputObjectsP.class);
            for (OutputObjectsP objectNameParameter:objectNameParameters) {
                String objectName = objectNameParameter.getObjectsName();
                MeasurementRefCollection measurementReferences = analysis.getModules().getObjectMeasurementRefs(objectName);

                if (measurementReferences.size() == 0) continue;

                measurementHeader = componentFactory.createMeasurementHeader(objectName+" (Object)",measurementReferences);
                c.gridx = 0;
                c.gridy++;
                c.anchor = GridBagConstraints.WEST;
                panel.add(measurementHeader,c);

                // Iterating over the measurements for the current object, adding a control for each
                for (MeasurementRef measurementReference:measurementReferences.values()) {
                    if (!measurementReference.isCalculated()) continue;

                    // Adding measurement control
                    currentMeasurementPanel = componentFactory.createMeasurementControl(measurementReference);
                    c.gridy++;
                    c.anchor = GridBagConstraints.EAST;
                    panel.add(currentMeasurementPanel,c);

                }
            }
        }

        // Creating the notes/help field at the bottom of the panel
        JSeparator separator = new JSeparator();
        separator.setOpaque(false);
        separator.setSize(new Dimension(0,0));
        c.weighty = 1;
        c.gridy++;
        c.fill = GridBagConstraints.VERTICAL;
        panel.add(separator,c);

        panel.revalidate();
        panel.repaint();

        revalidate();
        repaint();

    }

    public void addAdvancedParameterControl(Parameter parameter, GridBagConstraints c) {
        ComponentFactory componentFactory = GUI.getComponentFactory();
        Module activeModule = GUI.getActiveModule();
        int elementHeight = GUI.getElementHeight();

        c.insets = new Insets(2, 5, 0, 0);
        c.gridx = 0;
        c.gridy++;
        c.weightx = 1;
        c.anchor = GridBagConstraints.WEST;
        JPanel paramPanel = componentFactory.createParameterControl(parameter, GUI.getModules(), activeModule);
        panel.add(paramPanel, c);

        c.insets = new Insets(2, 5, 0, 5);
        c.gridx++;
        c.weightx = 0;
        c.anchor = GridBagConstraints.EAST;
        VisibleCheck visibleCheck = new VisibleCheck(parameter);
        visibleCheck.setPreferredSize(new Dimension(elementHeight, elementHeight));
        panel.add(visibleCheck, c);

    }

    public void addAdvancedParameterGroup(ParameterGroup group, GridBagConstraints c) {
        // Iterating over each collection of Parameters.  After adding each one, a remove button is included
        LinkedHashSet<ParameterCollection> collections = group.getCollections();

        c.gridy++;
        panel.add(getInvisibleSeparator(), c);

        for (ParameterCollection collection:collections) {
            // Adding the individual parameters
            for (Parameter parameter:collection) addAdvancedParameterControl(parameter,c);

            c.gridy++;
            panel.add(getInvisibleSeparator(), c);

        }

        // Adding an add button
        addAdvancedParameterControl(group,c);

        c.gridy++;
        panel.add(getInvisibleSeparator(), c);

    }

    private JSeparator getInvisibleSeparator() {
        JSeparator separator = new JSeparator();
        separator.setPreferredSize(new Dimension(0,15));
        separator.setForeground(panel.getBackground());
        separator.setBackground(panel.getBackground());

        return separator;

    }
}
