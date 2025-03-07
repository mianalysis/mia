package io.github.mianalysis.mia.object.parameters.text;

import io.github.mianalysis.mia.MIA;
import io.github.mianalysis.mia.module.Module;
import io.github.mianalysis.mia.module.system.GlobalVariables;
import io.github.mianalysis.mia.object.Workspace;
import io.github.mianalysis.mia.object.parameters.abstrakt.Parameter;
import io.github.mianalysis.mia.object.parameters.abstrakt.TextType;

public class DoubleP extends TextType {
    protected String value;

    public DoubleP(String name, Module module, double value) {
        super(name, module);
        this.value = String.valueOf(value);
    }

    public DoubleP(String name, Module module, String value) {
        super(name, module);
        this.value = value;
    }

    public DoubleP(String name, Module module, double value, String description) {
        super(name, module, description);
        this.value = String.valueOf(value);
    }

    public DoubleP(String name, Module module, String value, String description) {
        super(name, module, description);
        this.value = value;
    }

    public void setValue(double value) {
        this.value = String.valueOf(value);
    }

    public void setValue(String value) throws NumberFormatException {
        // Checking this is valid
        if (GlobalVariables.containsValue(value) || containsReference(value)) {
            this.value = value;
        } else {
            // try {
            // Double.parseDouble(value);
            this.value = value;
            // } catch (NumberFormatException e) {
            // MIA.log.writeWarning("Module \"" + module.getName() + "\", parameter \"" +
            // getName()
            // + " \". Must either:"
            // + "\n - A double-precision value,"
            // + "\n - A global variable handle in the form V{[VARIABLE NAME]} (e.g.
            // V{name}),"
            // + "\n - Contain a calculation in the form CD{[EQUATION]} for double-precision
            // outputs or CI{[EQUATION]} for integer-precision outputs (e.g. CD{3/4}),"
            // + "\n - Contain an image measurement in the form Im{[IMAGE NAME|MEASUREMENT
            // NAME]} (e.g. Im{RedChannel|DIMENSIONS // WIDTH}),"
            // + "\n - Contain an object collection measurement statistic in the form
            // Os{[OBJECTS NAME|MEASUREMENT NAME|STATISTIC]} (e.g. Os{Nuclei|SHAPE //
            // N_VOXELS|MEAN}),"
            // + "\n - Contain an object count in the form Oc{[OBJECTS NAME]} (e.g.
            // Os{Nuclei}),"
            // + "\nNote: Global variables, dynamic values and calculations can be combined
            // (e.g. CD{V{name1} + Oc{name2} - 4})");
            // }
        }
    }

    @Override
    public String getRawStringValue() {
        return value;

    }

    @Override
    public <T extends Parameter> T duplicate(Module newModule) {
        DoubleP newParameter = new DoubleP(name, newModule, value, getDescription());
        newParameter.setNickname(getNickname());
        newParameter.setVisible(isVisible());
        newParameter.setExported(isExported());

        return (T) newParameter;

    }

    @Override
    public void setValueFromString(String value) {
        setValue(value);
    }

    @Override
    public <T> T getValue(Workspace workspace) throws NumberFormatException {
        String converted = GlobalVariables.convertString(value, module.getModules());
        converted = insertWorkspaceValues(converted, workspace);
        converted = applyCalculation(converted);

        return (T) (Double) Double.parseDouble(converted);

    }

    @Override
    public <T> void setValue(T value) {
        this.value = String.valueOf(value);

    }

    @Override
    public boolean verify() {
        if (!super.verify())
            return false;

        if (value.equals(""))
            return false;

        if (GlobalVariables.variablesPresent(getRawStringValue(), module.getModules()))
            return true;

        try {
            Double.parseDouble(value);
        } catch (NumberFormatException e) {
            return false;
        }

        return true;

    }
}
