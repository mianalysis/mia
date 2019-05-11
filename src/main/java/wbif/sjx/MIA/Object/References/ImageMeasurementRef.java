package wbif.sjx.MIA.Object.References;

import org.w3c.dom.Element;
import wbif.sjx.MIA.Object.References.Abstract.MeasurementRef;

public class ImageMeasurementRef extends MeasurementRef {
    public ImageMeasurementRef(String name) {
        super(name);
    }

    public ImageMeasurementRef(String name, String imageObjName) {
        super(name, imageObjName);
    }

    @Override
    public void appendXMLAttributes(Element element)  {
        // Adding the values from ExportableRef
        super.appendXMLAttributes(element);

        element.setAttribute("IMAGE_OBJECT_NAME",imageObjName);
        element.setAttribute("TYPE","IMAGE");

    }

    public MeasurementRef duplicate() {
        ImageMeasurementRef newRef = new ImageMeasurementRef(name);

        newRef.setAvailable(isAvailable());
        newRef.setImageObjName(imageObjName);
        newRef.setDescription(getDescription());
        newRef.setNickname(getNickname());
        newRef.setExportGlobal(isExportGlobal());
        newRef.setExportIndividual(isExportIndividual());
        newRef.setExportMean(isExportMean());
        newRef.setExportMin(isExportMin());
        newRef.setExportMax(isExportMax());
        newRef.setExportSum(isExportSum());
        newRef.setExportStd(isExportStd());

        return newRef;

    }
}
