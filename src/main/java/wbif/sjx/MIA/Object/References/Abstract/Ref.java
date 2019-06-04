package wbif.sjx.MIA.Object.References.Abstract;

import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;

public abstract class Ref {
    protected final String name;
    private String nickname = "";
    private String description = "";

    public Ref(String name) {
        this.name = name;
        this.nickname = name;
    }

    public Ref(String name, String description) {
        this.name = name;
        this.nickname = name;
        this.description = description;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public void appendXMLAttributes(Element element) {
        element.setAttribute("NAME",name);
        element.setAttribute("NICKNAME",nickname);

    }

    public void setAttributesFromXML(NamedNodeMap attributes) {
        if (attributes.getNamedItem("NICKNAME") != null) {
            nickname = attributes.getNamedItem("NAME").getNodeValue();
        } else {
            nickname = attributes.getNamedItem("NICKNAME").getNodeValue();
        }
    }
}
