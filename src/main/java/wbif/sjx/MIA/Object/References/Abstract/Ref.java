package wbif.sjx.MIA.Object.References.Abstract;

import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;

public abstract class Ref {
    protected final String name;
    private String nickname = "";
    private String description = "";
    private boolean available = true;
    private boolean exportIndividual = true;
    private boolean exportGlobal = true; // This is mainly for the GUI

    public Ref(NamedNodeMap attributes) {
        this.name = attributes.getNamedItem("NAME").getNodeValue();
        setAttributesFromXML(attributes);
    }

    public Ref(String name) {
        this.name = name;
        this.nickname = name;
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

    public void setExportGlobal(boolean exportGlobal) {
        this.exportGlobal = exportGlobal;
    }

    public boolean isExportGlobal() {
        return exportGlobal;
    }

    public boolean isExportIndividual() {
        return exportIndividual;
    }

    public void setExportIndividual(boolean exportIndividual) {
        this.exportIndividual = exportIndividual;

    }

    public void setAllExport(boolean export) {
        exportGlobal = export;
        exportIndividual = export;

    }

    public void appendXMLAttributes(Element element) {
        element.setAttribute("NAME",name);
        element.setAttribute("NICKNAME",nickname);
        element.setAttribute("EXPORT_GLOBAL",String.valueOf(exportGlobal));
        element.setAttribute("EXPORT_INDIVIDUAL",String.valueOf(exportIndividual));

    }

    public void setAttributesFromXML(NamedNodeMap attributes) {
        nickname = attributes.getNamedItem("NAME").getNodeValue();
        if (attributes.getNamedItem("NICKNAME") != null) nickname = attributes.getNamedItem("NICKNAME").getNodeValue();
        if (attributes.getNamedItem("EXPORT_GLOBAL") != null) {
            exportGlobal = Boolean.parseBoolean(attributes.getNamedItem("EXPORT_GLOBAL").getNodeValue());
        }

        if (attributes.getNamedItem("EXPORT_INDIVIDUAL") != null) {
            exportIndividual = Boolean.parseBoolean(attributes.getNamedItem("EXPORT_INDIVIDUAL").getNodeValue());
        }
        setExportIndividual(exportIndividual);

    }
}
