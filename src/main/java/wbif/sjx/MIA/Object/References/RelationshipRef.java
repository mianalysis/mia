package wbif.sjx.MIA.Object.References;

import org.apache.poi.ss.usermodel.Sheet;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import wbif.sjx.MIA.Object.References.Abstract.Ref;
import wbif.sjx.MIA.Object.Workspace;

import java.util.LinkedHashMap;

public class RelationshipRef extends Ref {
    private final String parentName;
    private final String childName;


    public RelationshipRef(NamedNodeMap attributes) {
        super(attributes);
        this.childName = attributes.getNamedItem("CHILD_NAME").getNodeValue();
        this.parentName = attributes.getNamedItem("PARENT_NAME").getNodeValue();
    }

    public RelationshipRef(String parentName, String childName) {
        super(createName(parentName,childName));
        this.parentName = parentName;
        this.childName = childName;

    }

    @Override
    public void appendXMLAttributes(Element element)  {
        super.appendXMLAttributes(element);

        element.setAttribute("CHILD_NAME",childName);
        element.setAttribute("PARENT_NAME",parentName);

    }

    @Override
    public void setAttributesFromXML(NamedNodeMap attributes) {
        super.setAttributesFromXML(attributes);

    }

    public String getParentName() {
        return parentName;
    }

    public String getChildName() {
        return childName;
    }

    public static String createName(String parent, String child) {
        return parent+" // "+child;
    }

}
