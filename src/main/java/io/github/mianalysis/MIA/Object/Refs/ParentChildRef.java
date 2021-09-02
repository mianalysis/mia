package io.github.mianalysis.MIA.Object.Refs;

import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

import io.github.mianalysis.MIA.Object.Obj;
import io.github.mianalysis.MIA.Object.Refs.Abstract.Ref;

public class ParentChildRef extends Ref {
    private final String parentName;
    private final String childName;

    public ParentChildRef(Node node) {
        super(node);

        NamedNodeMap map = node.getAttributes();
        this.childName = map.getNamedItem("CHILD_NAME").getNodeValue();
        this.parentName = map.getNamedItem("PARENT_NAME").getNodeValue();

        setAttributesFromXML(node);

    }

    public ParentChildRef(String parentName, String childName) {
        super(createName(Obj.getNameWithoutRelationship(parentName), Obj.getNameWithoutRelationship(childName)));
        
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
    public void setAttributesFromXML(Node node) {
        super.setAttributesFromXML(node);

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

    public ParentChildRef duplicate() {
        ParentChildRef ref = new ParentChildRef(parentName,childName);

        ref.setDescription(description);
        ref.setNickname(nickname);

        return ref;
        
    }
}
