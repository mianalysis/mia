package wbif.sjx.MIA.Process.Exporting;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import wbif.sjx.MIA.Object.ModuleCollection;
import wbif.sjx.MIA.Object.References.Abstract.ExportableRef;
import wbif.sjx.MIA.Object.References.MetadataRef;
import wbif.sjx.MIA.Object.References.MetadataRefCollection;
import wbif.sjx.MIA.Object.Workspace;
import wbif.sjx.MIA.Object.WorkspaceCollection;
import wbif.sjx.MIA.Process.AnalysisHandling.Analysis;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.LinkedHashMap;

public class XLSXExporter {

    public void exportSummary(String path, WorkspaceCollection workspaces, Analysis analysis) {
        // Getting modules
        ModuleCollection modules = analysis.getModules();

        // Initialising the workbook
        SXSSFWorkbook workbook = new SXSSFWorkbook();
        Sheet sheet = workbook.createSheet("Summary");

        LinkedHashMap<Integer,Workspace> workspacesMap = createRowList(sheet,workspaces);

        addMetadataSummary(sheet, workspacesMap, modules);

        FileOutputStream outputStream = null;
        try {
            outputStream = new FileOutputStream(path);
            workbook.write(outputStream);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void addMetadataSummary(Sheet sheet, LinkedHashMap<Integer,Workspace> workspaces, ModuleCollection modules) {
        MetadataRefCollection metadataRefs = modules.getMetadataRefs(null);
        Row titleRow = sheet.getRow(0);

        // Iterating over each metadata value, adding values
        for (ExportableRef ref:metadataRefs.values()) ref.addSummaryXLSX(sheet,workspaces);

    }

    public LinkedHashMap<Integer,Workspace> createRowList(Sheet sheet, WorkspaceCollection workspaces) {
        LinkedHashMap<Integer,Workspace> workspacesMap = new LinkedHashMap<>();

        int rowN = 1;
        for (Workspace workspace:workspaces) {
            sheet.createRow(rowN);
            workspacesMap.put(rowN++,workspace);
        }

        return workspacesMap;

    }
}
