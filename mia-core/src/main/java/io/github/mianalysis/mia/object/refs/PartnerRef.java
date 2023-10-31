package io.github.mianalysis.mia.object.refs;

import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

import io.github.mianalysis.mia.object.Obj;
import io.github.mianalysis.mia.object.refs.abstrakt.Ref;

public class PartnerRef extends Ref implements Comparable {
    private final String object1Name;
    private final String object2Name;
    

    public PartnerRef(Node node) {
        super(node);

        NamedNodeMap map = node.getAttributes();
        this.object1Name = map.getNamedItem("OBJECT_1_NAME").getNodeValue();
        this.object2Name = map.getNamedItem("OBJECT_2_NAME").getNodeValue();
        
        setAttributesFromXML(node);

    }

    public PartnerRef(String object1Name, String object2Name) {
        super(createName(Obj.getNameWithoutRelationship(object1Name), Obj.getNameWithoutRelationship(object2Name)));
        
        this.object1Name = object1Name;
        this.object2Name = object2Name;

    }

    @Override
    public void appendXMLAttributes(Element element)  {
        super.appendXMLAttributes(element);

        element.setAttribute("OBJECT_2_NAME",object2Name);
        element.setAttribute("OBJECT_1_NAME",object1Name);

    }

    @Override
    public void setAttributesFromXML(Node node) {
        super.setAttributesFromXML(node);

    }

    public String getPartnerName(String objectName) {
        if (objectName.equals(object1Name)) {
            return object2Name;
        } else if (objectName.equals(object2Name)) {
            return object1Name;
        } else {
            return "";
        }
    }

    public String getObject1Name() {
        return object1Name;
    }

    public String getObject2Name() {
        return object2Name;
    }

    public static String createName(String object1Name, String object2Name) {
        return object1Name+" // "+object2Name;
    }

    public PartnerRef duplicate() {
        PartnerRef ref = new PartnerRef(object1Name, object2Name);

        ref.setDescription(description);
        ref.setNickname(nickname);

        return ref;

    }

    @Override
    public int compareTo(Object o) {
        String namePair1 = object1Name+object2Name;
        String namePair2 = ((PartnerRef) o).getObject1Name() + ((PartnerRef) o).getObject2Name();
        
        return namePair1.compareTo(namePair2);

    }

    // @Override
    // public int hashCode() {
    //     int hash = 1;

    //     // Using smallest hash first, so it doesn't matter which way round names are specified
    //     int hash1 = object1Name.hashCode();
    //     int hash2 = object2Name.hashCode();

    //     if (hash1 <= hash2) {
    //         hash = 31 * hash + hash1;
    //         return 31 * hash + hash2;
    //     } else {
    //         hash = 31 * hash + hash2;
    //         return 31 * hash + hash1;
    //     }       
    // }

    // @Override
    // public boolean equals(Object obj) {
    //     if (obj == this) return true;
    //     if (!(obj instanceof PartnerRef)) return false;

    //     PartnerRef ref2 = (PartnerRef) obj;
        
    //     // Same names in the same order
    //     if (object1Name.equals(ref2.getObject1Name()) && object2Name.equals(ref2.getObject2Name()))
    //         return true;

    //     // Same names in the opposite order
    //     if (object1Name.equals(ref2.getObject2Name()) && object2Name.equals(ref2.getObject1Name()))
    //             return true;
            
    //     return false;

    // }
}
