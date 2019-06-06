package wbif.sjx.MIA.Object.References.Abstract;

import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;

public abstract class ExportableRef extends Ref {
    private boolean exportIndividual = true;
    private boolean exportable = true; // This is mainly for the GUI


    public ExportableRef(String name) {
        super(name);
    }

    public ExportableRef(NamedNodeMap attributes) {
        super(attributes.getNamedItem("NAME").getNodeValue());
    }

    public void setExportable(boolean exportable) {
        this.exportable = exportable;
    }

    public boolean isExportable() {
        return exportable;
    }

    public boolean isExportIndividual() {
        return exportIndividual;
    }

    public void setExportIndividual(boolean exportIndividual) {
        this.exportIndividual = exportIndividual;

    }

    public void setAllExport(boolean export) {
        exportable = export;
        exportIndividual = export;

    }

    public void appendXMLAttributes(Element element) {
        super.appendXMLAttributes(element);
        element.setAttribute("EXPORT_GLOBAL",String.valueOf(exportable));
        element.setAttribute("EXPORT_INDIVIDUAL",String.valueOf(exportIndividual));

    }

    public void setAttributesFromXML(NamedNodeMap attributes) {
        super.setAttributesFromXML(attributes);

        if (attributes.getNamedItem("EXPORT_GLOBAL") != null) {
            exportable = Boolean.parseBoolean(attributes.getNamedItem("EXPORT_GLOBAL").getNodeValue());
        }

        if (attributes.getNamedItem("EXPORT_INDIVIDUAL") != null) {
            exportIndividual = Boolean.parseBoolean(attributes.getNamedItem("EXPORT_INDIVIDUAL").getNodeValue());
        }
    }
}
