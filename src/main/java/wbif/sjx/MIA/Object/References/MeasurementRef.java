package wbif.sjx.MIA.Object.References;

import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import wbif.sjx.MIA.Object.References.Abstract.ExportableRef;

/**
 * Created by sc13967 on 01/12/2017.
 */
public class MeasurementRef extends ExportableRef {
    private String imageObjName = "";
    private Type type;

    public enum Type {IMAGE,OBJECT};


    public MeasurementRef(String name, Type type) {
        super(name);
        this.type = type;
    }

    public MeasurementRef(String name, Type type, String imageObjName) {
        super(name);
        this.type = type;
        this.imageObjName = imageObjName;
    }

    @Override
    public void appendXMLAttributes(Element element)  {
        // Adding the values from ExportableRef
        super.appendXMLAttributes(element);

        element.setAttribute("IMAGE_OBJECT_NAME",imageObjName);

        switch (type) {
            case IMAGE:
                element.setAttribute("TYPE","IMAGE");
                break;
            case OBJECT:
                element.setAttribute("TYPE","OBJECTS");
                break;
        }
    }

    @Override
    public void setAttributesFromXML(NamedNodeMap attributes) {
        super.setAttributesFromXML(attributes);

        setImageObjName(attributes.getNamedItem("IMAGE_OBJECT_NAME").getNodeValue());

    }

    @Override
    public String getName() {
        return name;
    }

    public String getFinalName() {
        int idx = name.lastIndexOf("//");

        return name.substring(idx+2);

    }

    public String getImageObjName() {
        return imageObjName;
    }

    public MeasurementRef setImageObjName(String imageObjName) {
        this.imageObjName = imageObjName;
        return this;

    }

    public MeasurementRef duplicate() {
        MeasurementRef newRef = new MeasurementRef(name,type);

        newRef.setAvailable(isAvailable());
        newRef.setImageObjName(imageObjName);
        newRef.setDescription(getDescription());
        newRef.setNickname(getNickname());
        newRef.setExportGlobal(isExportGlobal());
        newRef.setExportIndividual(isExportIndividual());
        newRef.setExportMean(isExportMean());
        newRef.setExportMin(isExportMin());
        newRef.setExportMax(isExportMax());
        newRef.setExportSum(isExportSum());
        newRef.setExportStd(isExportStd());

        return newRef;

    }

    @Override
    public String toString() {
        return "Measurement reference ("+name+")";
    }

}