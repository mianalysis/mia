package wbif.sjx.MIA.Object.References;

public class RelationshipRef extends Reference {
    private final String parentName;
    private final String childName;

    public RelationshipRef(String name, String parentName, String childName) {
        super(name);
        this.parentName = parentName;
        this.childName = childName;
    }

    public String getParentName() {
        return parentName;
    }

    public String getChildName() {
        return childName;
    }

}
