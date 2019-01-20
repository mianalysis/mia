package wbif.sjx.ModularImageAnalysis.Object.Parameters.Abstract;

import wbif.sjx.ModularImageAnalysis.GUI.ParameterControl;
import wbif.sjx.ModularImageAnalysis.Module.Module;

import javax.swing.*;

public abstract class Parameter {
    protected final String name;
    protected final Module module;
    private final ParameterControl control;
    private boolean visible = false;
    private boolean valid = true;


    // CONSTRUCTORS

    public Parameter(String name, Module module) {
        this.name = name;
        this.module = module;

        this.control = initialiseControl();

    }


    // ABSTRACT METHODS

    protected abstract ParameterControl initialiseControl();

    public abstract String getValueAsString();


    // PUBLIC METHODS

    public String getNameAsString() {
        return name.toString().replace("_"," ");
    }


    // GETTERS AND SETTERS

    public String getName() {
        return name;
    }

    public Module getModule() {
        return module;
    }

    public ParameterControl getControl() {
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
