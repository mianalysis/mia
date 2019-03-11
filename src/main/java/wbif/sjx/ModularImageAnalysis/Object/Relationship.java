package wbif.sjx.ModularImageAnalysis.Object;

public class Relationship {
    private final String parentName;
    private final String childName;

    public Relationship(String parentName, String childName) {
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
