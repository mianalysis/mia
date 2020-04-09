package wbif.sjx.MIA.Object.Parameters.Abstract;

import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import wbif.sjx.MIA.GUI.ParameterControls.ParameterControl;
import wbif.sjx.MIA.Module.Module;
import wbif.sjx.MIA.Object.References.Abstract.Ref;

public abstract class Parameter extends Ref {
    protected Module module;
    private ParameterControl control;
    private boolean visible = false;
    private boolean valid = true;
    private boolean exported = true;
    private String description = "";


    // CONSTRUCTORS

    public Parameter(String name, Module module) {
        super(name);
        this.module = module;
    }

    public Parameter(String name, Module module, String description) {
        super(name);
        this.module = module;
        this.description = description;
    }


    // ABSTRACT METHODS

    protected abstract ParameterControl initialiseControl();

    public abstract <T> T getValue();

    public abstract <T> void setValue(T value);

    public abstract String getRawStringValue();

    public abstract void setValueFromString(String string);

    public abstract boolean verify();

    public abstract <T extends Parameter> T duplicate(Module newModule);


    // PUBLIC METHODS

    public String getNameAsString() {
        return name.toString().replace("_", " ");
    }

    // Can be used to display a different name if the raw name isn't useful for the GUI
    public String getAlternativeString() {
        return getRawStringValue();
    }


    // GETTERS AND SETTERS


    public Module getModule() {
        return module;
    }

    public void setModule(Module module) {
        this.module = module;
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

    @Override
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public void appendXMLAttributes(Element element) {
        super.appendXMLAttributes(element);

        String stringValue = getRawStringValue();
        if (stringValue == null) stringValue = "";
        element.setAttribute("VALUE",getRawStringValue());
        element.setAttribute("VISIBLE",Boolean.toString(isVisible()));

    }

    @Override
    public void setAttributesFromXML(Node node) {
        super.setAttributesFromXML(node);

        NamedNodeMap map = node.getAttributes();
        setValueFromString(map.getNamedItem("VALUE").getNodeValue());
        setVisible(Boolean.parseBoolean(map.getNamedItem("VISIBLE").getNodeValue()));

    }

    @Override
    public String toString() {
        return "Name: "+name+", value: "+getRawStringValue()+", module: "+module.getName();
    }
}
