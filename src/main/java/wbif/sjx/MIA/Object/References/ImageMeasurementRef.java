package wbif.sjx.MIA.Object.References;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

import wbif.sjx.MIA.Object.Workspace;
import wbif.sjx.MIA.Object.References.Abstract.SpreadsheetWriter;
import wbif.sjx.MIA.Object.References.Abstract.SummaryRef;

import java.util.LinkedHashMap;

public class ImageMeasurementRef extends SummaryRef implements SpreadsheetWriter {
    private String imageName = "";
    
    public ImageMeasurementRef(Node node) {
        super(node);
        setAttributesFromXML(node);
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
    public void setAttributesFromXML(Node node) {
        super.setAttributesFromXML(node);

        NamedNodeMap map = node.getAttributes();
        if (map.getNamedItem("IMAGE_NAME") == null) {
            this.imageName = map.getNamedItem("IMAGE_OBJECT_NAME").getNodeValue();
        } else {
            this.imageName = map.getNamedItem("IMAGE_NAME").getNodeValue();
        }
    }

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

    @Override
    public void addSummaryXLSX(Sheet sheet, LinkedHashMap<Integer, Workspace> workspaces) {
        if (!isExportGlobal()) return;


        // Getting the column number for this reference
        Row titleRow = sheet.getRow(0);
        int col = titleRow.getLastCellNum();
        if (col == -1) col++;

        // Adding the heading to the title row
        Cell cell = titleRow.createCell(col);
        cell.setCellValue(imageName+"_(IM) // "+getNickname());

        // Adding to each row
        for (int rowN:workspaces.keySet()) {
            Row row = sheet.getRow(rowN);
            cell = row.createCell(col);
            Workspace workspace = workspaces.get(rowN);
            cell.setCellValue(workspace.getMetadata().getAsString(getNickname()));
        }
    }

    public ImageMeasurementRef duplicate() {
        ImageMeasurementRef ref = new ImageMeasurementRef(name);

        ref.setDescription(description);
        ref.setImageName(imageName);
        ref.setNickname(nickname);

        ref.setExportGlobal(isExportGlobal());
        ref.setExportIndividual(isExportIndividual());
        ref.setExportMean(isExportMean());
        ref.setExportMax(isExportMax());
        ref.setExportMin(isExportMin());
        ref.setExportStd(isExportStd());
        ref.setExportSum(isExportSum());

        return ref;

    }
}
