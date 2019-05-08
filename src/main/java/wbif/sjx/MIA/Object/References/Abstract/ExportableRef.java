package wbif.sjx.MIA.Object.References.Abstract;

import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;

public abstract class ExportableRef extends Ref {
    private boolean exportGlobal = true; // This is mainly for the GUI
    private boolean exportIndividual = true;
    private boolean exportMean = true;
    private boolean exportMin = true;
    private boolean exportMax = true;
    private boolean exportSum = true;
    private boolean exportStd = true;

    private String description = "";
    private String nickname = "";


    public ExportableRef(String name) {
        super(name);
        this.nickname = name;
    }


    public void appendXMLAttributes(Element element) {
        element.setAttribute("NAME",name);
        element.setAttribute("NICKNAME",nickname);
        element.setAttribute("EXPORT_GLOBAL",String.valueOf(exportGlobal));
        element.setAttribute("EXPORT_INDIVIDUAL",String.valueOf(exportIndividual));
        element.setAttribute("EXPORT_MEAN",String.valueOf(exportMean));
        element.setAttribute("EXPORT_MIN",String.valueOf(exportMin));
        element.setAttribute("EXPORT_MAX",String.valueOf(exportMax));
        element.setAttribute("EXPORT_SUM",String.valueOf(exportSum));
        element.setAttribute("EXPORT_STD",String.valueOf(exportStd));

    }

    public void setAttributesFromXML(NamedNodeMap attributes) {
        String measurementNickName = attributes.getNamedItem("NAME").getNodeValue();
        if (attributes.getNamedItem("NICKNAME") != null) measurementNickName = attributes.getNamedItem("NICKNAME").getNodeValue();
        setNickname(measurementNickName);

        boolean exportGlobal = true;
        if (attributes.getNamedItem("EXPORT_GLOBAL") != null) {
            exportGlobal= Boolean.parseBoolean(attributes.getNamedItem("EXPORT_GLOBAL").getNodeValue());
        }
        setExportGlobal(exportGlobal);

        boolean exportIndividual = true;
        if (attributes.getNamedItem("EXPORT_INDIVIDUAL") != null) {
            exportIndividual = Boolean.parseBoolean(attributes.getNamedItem("EXPORT_INDIVIDUAL").getNodeValue());
        }
        setExportIndividual(exportIndividual);

        boolean exportMean = true;
        if (attributes.getNamedItem("EXPORT_MEAN") != null) {
            exportMean = Boolean.parseBoolean(attributes.getNamedItem("EXPORT_MEAN").getNodeValue());
        }
        setExportMean(exportMean);

        boolean exportMin = true;
        if (attributes.getNamedItem("EXPORT_MIN") != null) {
            exportMin = Boolean.parseBoolean(attributes.getNamedItem("EXPORT_MIN").getNodeValue());
        }
        setExportMin(exportMin);

        boolean exportMax = true;
        if (attributes.getNamedItem("EXPORT_MAX") != null) {
            exportMax = Boolean.parseBoolean(attributes.getNamedItem("EXPORT_MAX").getNodeValue());
        }
        setExportMax(exportMax);

        boolean exportSum = true;
        if (attributes.getNamedItem("EXPORT_SUM") != null) {
            exportSum = Boolean.parseBoolean(attributes.getNamedItem("EXPORT_SUM").getNodeValue());
        }
        setExportSum(exportSum);

        boolean exportStd = true;
        if (attributes.getNamedItem("EXPORT_STD") != null) {
            exportStd = Boolean.parseBoolean(attributes.getNamedItem("EXPORT_STD").getNodeValue());
        }
        setExportStd(exportStd);

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

    public boolean isExportMean() {
        return exportMean;
    }

    public void setExportMean(boolean exportMean) {
        this.exportMean = exportMean;

    }

    public boolean isExportMin() {
        return exportMin;
    }

    public void setExportMin(boolean exportMin) {
        this.exportMin = exportMin;

    }

    public boolean isExportMax() {
        return exportMax;
    }

    public void setExportMax(boolean exportMax) {
        this.exportMax = exportMax;

    }

    public boolean isExportSum() {
        return exportSum;
    }

    public void setExportSum(boolean exportSum) {
        this.exportSum = exportSum;

    }

    public boolean isExportStd() {
        return exportStd;
    }

    public void setExportStd(boolean exportStd) {
        this.exportStd = exportStd;

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

}
