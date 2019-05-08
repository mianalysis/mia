package wbif.sjx.MIA.Object.References.Abstract;

import org.w3c.dom.Element;

public abstract class Ref {
    protected final String name;
    private boolean available = true;

    public Ref(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public boolean isAvailable() {
        return available;
    }

    public void setAvailable(boolean available) {
        this.available = available;
    }

    public void appendXMLAttributes(Element element) {
        element.setAttribute("NAME",name);
    }
}
