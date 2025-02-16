package io.github.mianalysis.mia.object.refs;

import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

import io.github.mianalysis.mia.object.ObjI;
import io.github.mianalysis.mia.object.refs.abstrakt.SummaryRef;

public class ObjMetadataRef extends SummaryRef {
    private String objectsName = "";

    public ObjMetadataRef(Node node) {
        super(node);
        setAttributesFromXML(node);
    }

    public ObjMetadataRef(String name) {
        super(name);
    }

    public ObjMetadataRef(String name, String objectsName) {
        super(name);
        this.objectsName = ObjI.getNameWithoutRelationship(objectsName);
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

    public ObjMetadataRef duplicate() {
        ObjMetadataRef ref = new ObjMetadataRef(name);

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

    public ObjMetadataRef setObjectsName(String objectsName) {
        this.objectsName = ObjI.getNameWithoutRelationship(objectsName);
        return this;
    }
}
