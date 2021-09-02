package io.github.mianalysis.MIA.Object.Refs;

import java.util.LinkedHashMap;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.w3c.dom.Node;

import io.github.mianalysis.MIA.Object.Workspace;
import io.github.mianalysis.MIA.Object.Refs.Abstract.ExportableRef;
import io.github.mianalysis.MIA.Object.Refs.Abstract.SpreadsheetWriter;

public class MetadataRef extends ExportableRef implements SpreadsheetWriter {
    public MetadataRef(Node node) {
        super(node);
        setAttributesFromXML(node);
    }

    public MetadataRef(String name) {
        super(name);
    }

    @Override
    public void addSummaryXLSX(Sheet sheet, LinkedHashMap<Integer,Workspace> workspaces) {
        if (!isExportIndividual() && !isExportGlobal()) return;

        // Getting the column number for this reference
        Row titleRow = sheet.getRow(0);
        int col = titleRow.getLastCellNum();
        if (col == -1) col++;

        // Adding the heading to the title row
        Cell cell = titleRow.createCell(col);
        cell.setCellValue(getNickname());

        // Adding to each row
        for (int rowN:workspaces.keySet()) {
            Row row = sheet.getRow(rowN);
            cell = row.createCell(col);
            Workspace workspace = workspaces.get(rowN);
            cell.setCellValue(workspace.getMetadata().getAsString(getNickname()));
        }
    }

    public MetadataRef duplicate() {
        MetadataRef ref = new MetadataRef(name);

        ref.setDescription(description);
        ref.setNickname(nickname);

        ref.setExportGlobal(isExportGlobal());
        ref.setExportIndividual(isExportIndividual());

        return ref;
    }
}
