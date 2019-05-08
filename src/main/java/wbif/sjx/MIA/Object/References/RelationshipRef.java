package wbif.sjx.MIA.Object.References;

import wbif.sjx.MIA.Object.References.Abstract.ExportableRef;
import wbif.sjx.MIA.Object.References.Abstract.Ref;

public class RelationshipRef extends ExportableRef {
    private final String parentName;
    private final String childName;

    public RelationshipRef(String parentName, String childName) {
        super(createName(parentName,childName));
        this.parentName = parentName;
        this.childName = childName;

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
