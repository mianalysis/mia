package wbif.sjx.MIA.Object.References;

import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

import wbif.sjx.MIA.Object.References.Abstract.SummaryRef;

public class ObjMeasurementRef extends SummaryRef {
    private String objectsName = "";

    public ObjMeasurementRef(Node node) {
        super(node);
        setAttributesFromXML(node);
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
    public void setAttributesFromXML(Node node) {
        super.setAttributesFromXML(node);

        NamedNodeMap map = node.getAttributes();
        if (map.getNamedItem("OBJECT_NAME") == null) {
            this.objectsName = map.getNamedItem("IMAGE_OBJECT_NAME").getNodeValue();
        } else {
            this.objectsName = map.getNamedItem("OBJECT_NAME").getNodeValue();
        }
    }

    public ObjMeasurementRef duplicate() {
        ObjMeasurementRef ref = new ObjMeasurementRef(name);

        ref.setDescription(description);
        ref.setObjectsName(objectsName);
        ref.setNickname(nickname);

        ref.setExportGlobal(isExportGlobal());
        ref.setExportIndividual(isExportIndividual());
        ref.setExportMean(isExportMean());
        ref.setExportMax(isExportMax());
        ref.setExportMin(isExportMin());
        ref.setExportStd(isExportStd());
        ref.setExportSum(isExportSum());

        return ref;

    }

    public String getFinalName() {
        if (name.length() == 0)
            return name;
        if (!name.contains("//"))
            return name;
            
        int idx = name.lastIndexOf("//");
        return name.substring(idx+3);

    }

    public String getObjectsName() {
        return objectsName;
    }

    public ObjMeasurementRef setObjectsName(String objectsName) {
        this.objectsName = objectsName;
        return this;
    }
}
