package io.github.mianalysis.MIA.Object.References.Abstract;

import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

public abstract class ExportableRef extends Ref {
    private boolean exportIndividual = true;
    private boolean exportGlobal = true; // This is mainly for the GUI


    public ExportableRef(String name) {
        super(name);
    }

    public ExportableRef(Node node) {
        super(node);
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
        super.appendXMLAttributes(element);
        element.setAttribute("EXPORT_GLOBAL",String.valueOf(exportGlobal));
        element.setAttribute("EXPORT_INDIVIDUAL",String.valueOf(exportIndividual));

    }

    public void setAttributesFromXML(Node node) {
        super.setAttributesFromXML(node);

        NamedNodeMap map = node.getAttributes();
        if (map.getNamedItem("EXPORT_GLOBAL") != null) {
            exportGlobal = Boolean.parseBoolean(map.getNamedItem("EXPORT_GLOBAL").getNodeValue());
        }

        if (map.getNamedItem("EXPORT_INDIVIDUAL") != null) {
            exportIndividual = Boolean.parseBoolean(map.getNamedItem("EXPORT_INDIVIDUAL").getNodeValue());
        }
    }
}
