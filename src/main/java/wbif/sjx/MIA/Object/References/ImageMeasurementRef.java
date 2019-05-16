package wbif.sjx.MIA.Object.References;

import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import wbif.sjx.MIA.Object.References.Abstract.SummaryRef;

public class ImageMeasurementRef extends SummaryRef {
    private String imageName = "";

    public ImageMeasurementRef(NamedNodeMap attributes) {
        super(attributes);
    }

    public ImageMeasurementRef(String name) {
        super(name);
    }

    public ImageMeasurementRef(String name, String imageName) {
        super(name);
        this.imageName = imageName;
    }

    @Override
    public void appendXMLAttributes(Element element)  {
        // Adding the values from ExportableRef
        super.appendXMLAttributes(element);

        element.setAttribute("IMAGE_NAME",imageName);

    }

    @Override
    public void setAttributesFromXML(NamedNodeMap attributes) {
        super.setAttributesFromXML(attributes);

        if (attributes.getNamedItem("IMAGE_NAME") == null) {
            this.imageName = attributes.getNamedItem("IMAGE_OBJECT_NAME").getNodeValue();
        } else {
            this.imageName = attributes.getNamedItem("IMAGE_NAME").getNodeValue();
        }
    }



//    public MeasurementRef duplicate() {
//        ImageMeasurementRef newRef = new ImageMeasurementRef(name);
//
//        newRef.setAvailable(isAvailable());
//        newRef.setImageObjName(imageObjName);
//        newRef.setDescription(getDescription());
//        newRef.setNickname(getNickname());
//        newRef.setExportGlobal(isExportGlobal());
//        newRef.setExportIndividual(isExportIndividual());
//        newRef.setExportMean(isExportMean());
//        newRef.setExportMin(isExportMin());
//        newRef.setExportMax(isExportMax());
//        newRef.setExportSum(isExportSum());
//        newRef.setExportStd(isExportStd());
//
//        return newRef;
//
//    }

    public String getFinalName() {
        int idx = name.lastIndexOf("//");

        return name.substring(idx+2);

    }

    public String getImageName() {
        return imageName;
    }

    public ImageMeasurementRef setImageName(String imageName) {
        this.imageName = imageName;
        return this;
    }
}
