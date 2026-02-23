package io.github.mianalysis.mia.object.refs.abstrakt;

import org.w3c.dom.Element;
import org.w3c.dom.Node;

public interface Ref {
    public String getName();
    public String getDescription();
    public void setDescription(String description);
    public String getNickname();
    public void setNickname(String nickname);
    public void appendXMLAttributes(Element element);
    public void setAttributesFromXML(Node node);

}
