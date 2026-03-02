package io.github.mianalysis.mia.object.parameters.abstrakt;

import java.lang.reflect.InvocationTargetException;

import org.w3c.dom.Element;
import org.w3c.dom.Node;

import io.github.mianalysis.mia.module.ModuleI;
import io.github.mianalysis.mia.object.WorkspaceI;
import io.github.mianalysis.mia.object.refs.abstrakt.Ref;

public interface ParameterI extends Ref {

    public ParameterControl initialiseControl();

    public <T> T getValue(WorkspaceI workspace);

    public <T> void setValue(T value);

    public String getRawStringValue();

    public void setValueFromString(String string);

    public boolean verify();

    public <T extends ParameterI> T duplicate(ModuleI newModule);

    public String getNameAsString();

    // Can be used to display a different name if the raw name isn't useful for the
    // GUI
    public String getAlternativeString();

    public ParameterI createNewInstance(String name, ModuleI module) throws InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException;

    public ModuleI getModule();

    public void setModule(ModuleI module);

    public ParameterControl getControl();

    public void setControl(ParameterControl control);

    public boolean isVisible();

    public void setVisible(boolean visible);

    public boolean isValid();

    public void setValid(boolean valid);

    public boolean isExported();

    public void setExported(boolean exported);

}
