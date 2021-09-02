package io.github.mianalysis.MIA.Object.Refs.Abstract;

import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;



public abstract class Ref {
    protected final String name;
    protected String nickname = "";
    protected String description = "";

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

    public void setAttributesFromXML(Node node) {
        NamedNodeMap map = node.getAttributes();

        if (map.getNamedItem("NICKNAME") == null) {
            nickname = map.getNamedItem("NAME").getNodeValue();
        } else {
            nickname = map.getNamedItem("NICKNAME").getNodeValue();
        }
    }
}
