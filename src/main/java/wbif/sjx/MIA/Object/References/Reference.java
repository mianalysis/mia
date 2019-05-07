package wbif.sjx.MIA.Object.References;

public abstract class Reference {
    protected final String name;

    public Reference(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

}
