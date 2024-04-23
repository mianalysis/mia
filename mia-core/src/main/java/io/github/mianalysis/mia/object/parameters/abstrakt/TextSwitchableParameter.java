package io.github.mianalysis.mia.object.parameters.abstrakt;

import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

import io.github.mianalysis.mia.module.Module;
import io.github.mianalysis.mia.module.system.GlobalVariables;

public abstract class TextSwitchableParameter extends Parameter {
    protected boolean showText = false; // When true GUI shows a text entry for the raw string

    public TextSwitchableParameter(String name, Module module) {
        super(name,module);
    }

    public TextSwitchableParameter(String name, Module module, String description) {
        super(name,module,description);
    }

    public void setShowText(boolean showText) {
        this.showText = showText;
    }

    public boolean isShowText() {
        return this.showText;
    }

    @Override
    public void appendXMLAttributes(Element element) {
        super.appendXMLAttributes(element);

        element.setAttribute("SHOW_TEXT", Boolean.toString(isShowText()));

    }

    @Override
    public void setAttributesFromXML(Node node) {
        super.setAttributesFromXML(node);

        NamedNodeMap map = node.getAttributes();

        if (map.getNamedItem("SHOW_CHOICE") != null)
            setShowText(Boolean.parseBoolean(map.getNamedItem("SHOW_TEXT").getNodeValue()));

    }

    @Override
    public boolean verify() {
        if (isShowText())
            return GlobalVariables.variablesPresent(getRawStringValue(), module.getModules());
        else
            return true;
    }
}
