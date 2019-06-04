package wbif.sjx.MIA.Object.Parameters.Abstract;

import wbif.sjx.MIA.GUI.ParameterControls.ParameterControl;
import wbif.sjx.MIA.Module.Module;
import wbif.sjx.MIA.Object.References.Abstract.Ref;

public abstract class Parameter extends Ref {
    protected final Module module;
    private ParameterControl control;
    private boolean visible = false;
    private boolean valid = true;
    private boolean exported = true;


    // CONSTRUCTORS

    public Parameter(String name, Module module) {
        super(name);
        this.module = module;
    }

    public Parameter(String name, Module module, String description) {
        super(name,description);
        this.module = module;
    }


    // ABSTRACT METHODS

    protected abstract ParameterControl initialiseControl();

    public abstract <T> T getFinalValue();

    public abstract <T> void setValue(T value);

    public abstract String getRawStringValue();

    public abstract boolean verify();

    public abstract <T extends Parameter> T duplicate();


    // PUBLIC METHODS

    public String getNameAsString() {
        return name.toString().replace("_"," ");
    }


    // GETTERS AND SETTERS


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

    public boolean isExported() {
        return exported;
    }

    public void setExported(boolean exported) {
        this.exported = exported;
    }
}
