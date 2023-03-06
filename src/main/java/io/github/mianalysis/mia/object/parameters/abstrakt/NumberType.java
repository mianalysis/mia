package io.github.mianalysis.mia.object.parameters.abstrakt;

import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

import io.github.mianalysis.mia.gui.parametercontrols.ParameterControl;
import io.github.mianalysis.mia.gui.parametercontrols.SliderParameter;
import io.github.mianalysis.mia.module.Module;

public abstract class NumberType extends TextType {
    private boolean isSlider = false;
    private double sliderMin = 0;
    private double sliderMax = 100;
    
    public NumberType(String name, Module module) {
        super(name, module);
    }

    public NumberType(String name, Module module, String description) {
        super(name, module, description);
    }

    @Override
    public void appendXMLAttributes(Element element) {
        // Adding the values from ExportableRef
        super.appendXMLAttributes(element);

        element.setAttribute("IS_SLIDER", String.valueOf(isSlider));
        element.setAttribute("SLIDER_MIN", String.valueOf(sliderMin));
        element.setAttribute("SLIDER_MAX", String.valueOf(sliderMax));

    }

    @Override
    public void setAttributesFromXML(Node node) {
        super.setAttributesFromXML(node);

        NamedNodeMap map = node.getAttributes();
        if (map.getNamedItem("IS_SLIDER") != null)            
            this.isSlider = Boolean.parseBoolean(map.getNamedItem("IS_SLIDER").getNodeValue());

        if (map.getNamedItem("SLIDER_MIN") != null)
            this.sliderMin = Double.parseDouble(map.getNamedItem("IS_SLIDER").getNodeValue());

        if (map.getNamedItem("SLIDER_MAX") != null)
            this.sliderMax = Double.parseDouble(map.getNamedItem("IS_SLIDER").getNodeValue());
        
    }

    @Override
    protected ParameterControl initialiseControl() {
        return new SliderParameter(this);
    }

    public boolean isSlider() {
        return isSlider;
    }

    public void setIsSlider(boolean isSlider) {
        this.isSlider = isSlider;
    }

    public double getSliderMin() {
        return sliderMin;
    }

    public void setSliderMin(double sliderMin) {
        this.sliderMin = sliderMin;
    }

    public double getSliderMax() {
        return sliderMax;
    }

    public void setSliderMax(double sliderMax) {
        this.sliderMax = sliderMax;
    }
}
