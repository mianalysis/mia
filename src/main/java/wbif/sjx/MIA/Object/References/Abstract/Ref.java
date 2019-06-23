package wbif.sjx.MIA.Object.References.Abstract;

import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

public abstract class Ref {
    protected final String name;
    private String nickname = "";


    public Ref(String name) {
        this.name = name;
        this.nickname = name;
    }

    public Ref(Node node) {
        NamedNodeMap map = node.getAttributes();
        this.name = map.getNamedItem("NAME").getNodeValue();
        this.nickname = map.getNamedItem("NAME").getNodeValue();
    }

    public String getName() {
        return name;
    }

    public abstract String getDescription();

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

    public void setAttributesFromXML(Node node) {
        NamedNodeMap map = node.getAttributes();

        if (map.getNamedItem("NICKNAME") == null) {
            nickname = map.getNamedItem("NAME").getNodeValue();
        } else {
            nickname = map.getNamedItem("NICKNAME").getNodeValue();
        }
    }
}
