package io.github.mianalysis.mia.object.refs.abstrakt;

import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

public abstract class AbstractRef implements Ref {
    protected final String name;
    protected String nickname = "";
    protected String description = "";

    public AbstractRef(String name) {
        this.name = name;
        this.nickname = name;
    }

    public AbstractRef(Node node) {
        NamedNodeMap map = node.getAttributes();
        this.name = map.getNamedItem("NAME").getNodeValue();
        this.nickname = map.getNamedItem("NAME").getNodeValue();
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public String getNickname() {
        return nickname;
    }

    @Override
    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    @Override
    public void appendXMLAttributes(Element element) {
        element.setAttribute("NAME",getName());
        element.setAttribute("NICKNAME",getNickname());

    }

    @Override
    public void setAttributesFromXML(Node node) {
        NamedNodeMap map = node.getAttributes();

        if (map.getNamedItem("NICKNAME") == null)
            setNickname(map.getNamedItem("NAME").getNodeValue());
        else
            setNickname(map.getNamedItem("NICKNAME").getNodeValue());
        
    }
}
