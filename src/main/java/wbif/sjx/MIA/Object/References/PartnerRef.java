package wbif.sjx.MIA.Object.References;

import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import wbif.sjx.MIA.Object.References.Abstract.SummaryRef;

public class PartnerRef extends SummaryRef {
    private final String object1Name;
    private final String object2Name;
    private String description = "";


    public PartnerRef(Node node) {
        super(node);

        NamedNodeMap map = node.getAttributes();
        this.object1Name = map.getNamedItem("OBJECT_1_NAME").getNodeValue();
        this.object2Name = map.getNamedItem("OBJECT_2_NAME").getNodeValue();
        
        setAttributesFromXML(node);

    }

    public PartnerRef(String object1Name, String object2Name) {
        super(createName(object1Name,object2Name));
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

    public String getObject1Name() {
        return object1Name;
    }

    public String getObject2Name() {
        return object2Name;
    }

    public static String createName(String object1Name, String object2Name) {
        return object1Name+" // "+object2Name;
    }

    @Override
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public PartnerRef duplicate() {
        PartnerRef ref = new PartnerRef(object1Name, object2Name);

        ref.setDescription(description);
        ref.setNickname(getNickname());

        ref.setExportGlobal(isExportGlobal());
        ref.setExportIndividual(isExportIndividual());
        ref.setExportMean(isExportMean());
        ref.setExportMax(isExportMax());
        ref.setExportMin(isExportMin());
        ref.setExportStd(isExportStd());
        ref.setExportSum(isExportSum());

        return ref;

    }

    @Override
    public int hashCode() {
        int hash = 1;

        // Using smallest hash first, so it doesn't matter which way round names are specified
        int hash1 = object1Name.hashCode();
        int hash2 = object2Name.hashCode();

        if (hash1 <= hash2) {
            hash = 31 * hash + hash1;
            return 31 * hash + hash2;
        } else {
            hash = 31 * hash + hash2;
            return 31 * hash + hash1;
        }       
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (!(obj instanceof PartnerRef)) return false;

        PartnerRef ref2 = (PartnerRef) obj;
        
        // Same names in the same order
        if (object1Name.equals(ref2.getObject1Name()) && object2Name.equals(ref2.getObject2Name()))
            return true;

        // Same names in the opposite order
        if (object1Name.equals(ref2.getObject2Name()) && object2Name.equals(ref2.getObject1Name()))
                return true;
            
        return false;

    }
}
