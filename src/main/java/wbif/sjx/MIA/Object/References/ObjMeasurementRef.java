package wbif.sjx.MIA.Object.References;

import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import wbif.sjx.MIA.Object.References.Abstract.SummaryRef;

public class ObjMeasurementRef extends SummaryRef {
    private String objectsName = "";
    private String description = "";

    public ObjMeasurementRef(NamedNodeMap attributes) {
        super(attributes);
        setAttributesFromXML(attributes);
    }

    public ObjMeasurementRef(String name) {
        super(name);
    }

    public ObjMeasurementRef(String name, String objectsName) {
        super(name);
        this.objectsName = objectsName;
    }

    @Override
    public void appendXMLAttributes(Element element)  {
        // Adding the values from ExportableRef
        super.appendXMLAttributes(element);

        element.setAttribute("OBJECT_NAME", objectsName);

    }

    @Override
    public void setAttributesFromXML(NamedNodeMap attributes) {
        super.setAttributesFromXML(attributes);

        if (attributes.getNamedItem("OBJECT_NAME") == null) {
            this.objectsName = attributes.getNamedItem("IMAGE_OBJECT_NAME").getNodeValue();
        } else {
            this.objectsName = attributes.getNamedItem("OBJECT_NAME").getNodeValue();
        }
    }

    public String getFinalName() {
        int idx = name.lastIndexOf("//");

        return name.substring(idx+2);

    }

    public String getObjectsName() {
        return objectsName;
    }

    public ObjMeasurementRef setObjectsName(String objectsName) {
        this.objectsName = objectsName;
        return this;
    }

    @Override
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
