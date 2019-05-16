package wbif.sjx.MIA.Object.References.Abstract;

import org.apache.poi.ss.usermodel.Sheet;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import wbif.sjx.MIA.Object.Workspace;

import java.util.LinkedHashMap;

/**
 * Created by sc13967 on 01/12/2017.
 */
public abstract class MeasurementRef extends SummaryRef {
    protected String imageObjName = "";

    public MeasurementRef(NamedNodeMap attributes, String imageObjName) {
        super(attributes);

    }

    public MeasurementRef(String name) {
        super(name);
    }

    public MeasurementRef(String name, String imageObjName) {
        super(name);
        this.imageObjName = imageObjName;
    }

    @Override
    public void appendXMLAttributes(Element element)  {
        // Adding the values from ExportableRef
        super.appendXMLAttributes(element);

        element.setAttribute("IMAGE_OBJECT_NAME",imageObjName);

    }

    @Override
    public void setAttributesFromXML(NamedNodeMap attributes) {
        super.setAttributesFromXML(attributes);

        setImageObjName(attributes.getNamedItem("IMAGE_OBJECT_NAME").getNodeValue());

    }

//    @Override
//    public void addSummaryXLSX(Sheet sheet, LinkedHashMap<Integer,Workspace> workspaces) {
//        if (!isAvailable()) return;
//        if (!isExportGlobal()) return;
//
//
////        // Getting the column number for this reference
////        Row titleRow = sheet.getRow(0);
////        int col = titleRow.getLastCellNum();
////        if (col == -1) col++;
////
////        // Adding the heading to the title row
////        Cell cell = titleRow.createCell(col);
////        switch (type) {
////            case IMAGE:
////                cell.setCellValue(imageObjName+"_(IM) // "+getNickname());
////                break;
////            case OBJECT:
////                cell.setCellValue(imageObjName+"_(OBJ) // "+getNickname());
////                break;
////        }
////
////        // Adding to each row
////        for (int rowN:workspaces.keySet()) {
////            Row row = sheet.getRow(rowN);
////            cell = row.createCell(col);
////            Workspace workspace = workspaces.get(rowN);
////            cell.setCellValue(workspace.getMetadata().getAsString(getNickname()));
////        }
//    }

    @Override
    public String getName() {
        return name;
    }

    public String getFinalName() {
        int idx = name.lastIndexOf("//");

        return name.substring(idx+2);

    }

    public String getImageObjName() {
        return imageObjName;
    }

    public MeasurementRef setImageObjName(String imageObjName) {
        this.imageObjName = imageObjName;
        return this;

    }

    @Override
    public String toString() {
        return "Measurement reference ("+name+")";
    }

}