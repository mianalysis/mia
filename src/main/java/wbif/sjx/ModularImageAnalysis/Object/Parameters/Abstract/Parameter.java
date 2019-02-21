package wbif.sjx.ModularImageAnalysis.Object.Parameters.Abstract;

import wbif.sjx.ModularImageAnalysis.GUI.ParameterControl;
import wbif.sjx.ModularImageAnalysis.Module.Module;

public abstract class Parameter {
    protected final String name;
    protected final Module module;
    private final String description;
    private ParameterControl control;
    private boolean visible = false;
    private boolean valid = true;


    // CONSTRUCTORS

    public Parameter(String name, Module module) {
        this.name = name;
        this.module = module;
        this.description = "";
    }

    public Parameter(String name, Module module, String description) {
        this.name = name;
        this.module = module;
        this.description = description;
    }


    // ABSTRACT METHODS

    protected abstract ParameterControl initialiseControl();

    public abstract <T> T getValue();

    public abstract <T> void setValue(T value);

    public abstract String getValueAsString();

    public abstract boolean verify();

    public abstract <T extends Parameter> T duplicate();


    // PUBLIC METHODS

    public String getNameAsString() {
        return name.toString().replace("_"," ");
    }


    // GETTERS AND SETTERS


    public String getDescription() {
        return description;
    }

    public String getName() {
        return name;
    }

    public Module getModule() {
        return module;
    }

    public ParameterControl getControl() {
        if (control == null) control = initialiseControl();
        return control;
    }

    public boolean isVisible() {
        return visible;
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    public boolean isValid() {
        return valid;
    }

    public void setValid(boolean valid) {
        this.valid = valid;
    }
}
