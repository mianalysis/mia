package io.github.mianalysis.mia.object.parameters.abstrakt;

import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

import io.github.mianalysis.mia.MIA;
import io.github.mianalysis.mia.module.Module;
import io.github.mianalysis.mia.module.system.GlobalVariables;
import io.github.mianalysis.mia.object.Workspace;
import io.github.mianalysis.mia.process.ParameterControlFactory;

public abstract class ChoiceType extends TextSwitchableParameter {
    protected String choice = "";

    public ChoiceType(String name, Module module) {
        super(name, module);
    }

    public ChoiceType(String name, Module module, String description) {
        super(name, module, description);
    }

    // public String getChoice() {
    //     return choice;

    // }

    public void setChoice(String choice) {
        if (choice == null)
            choice = "";
        this.choice = choice;
    }

    public abstract String[] getChoices();

    @Override
    public String getRawStringValue() {
        return choice;
    }

    @Override
    public void setValueFromString(String string) {
        if (string == null)
            string = "";

        this.choice = string;

    }

    @Override
    protected ParameterControl initialiseControl() {
        return ParameterControlFactory.getChoiceTypeControl(this);
    }

    @Override
    public <T> T getValue(Workspace workspace) {
        if (choice == null)
        return null;

        String converted = GlobalVariables.convertString(choice, module.getModules());
        converted = TextType.insertWorkspaceValues(converted, workspace);
        converted = TextType.applyCalculation(converted);

        return (T) converted;

    }

    @Override
    public <T> void setValue(T value) {
        if (value == null)
            choice = "";
        else 
            choice = (String) value;
    }

    @Override
    public void setAttributesFromXML(Node node) {
        super.setAttributesFromXML(node);

        NamedNodeMap map = node.getAttributes();

        // ChoiceType values can be from hard-coded sources, so we check if they're
        // labelled for reassignment.
        String xmlValue = map.getNamedItem("VALUE").getNodeValue();
        xmlValue = MIA.getLostAndFound().findParameterValue(module.getClass().getSimpleName(), getName(), xmlValue);
        setValueFromString(xmlValue);

        setVisible(Boolean.parseBoolean(map.getNamedItem("VISIBLE").getNodeValue()));

    }

    @Override
    public boolean verify() {
        if (isShowText()) {
            return super.verify();
        } else {
            // Verifying the choice is present in the choices. When we generateModuleList
            // getChoices, we should be getting the valid
            // options only.
            String[] choices = getChoices();

            String converted = GlobalVariables.convertString(choice, module.getModules());

            for (String currChoice : choices) {    
                if (converted.equals(currChoice))
                    return true;
            }

            return false;

        }
    }
}
