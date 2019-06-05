package wbif.sjx.MIA.Object.References;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import wbif.sjx.MIA.Object.References.Abstract.SpreadsheetWriter;
import wbif.sjx.MIA.Object.References.Abstract.SummaryRef;
import wbif.sjx.MIA.Object.Workspace;

import java.util.LinkedHashMap;

public class ImageMeasurementRef extends SummaryRef implements SpreadsheetWriter {
    private String imageName = "";
    private String description = "";

    public ImageMeasurementRef(NamedNodeMap attributes) {
        super(attributes);
        setAttributesFromXML(attributes);
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

    @Override
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
