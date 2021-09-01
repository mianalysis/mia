package io.github.mianalysis.MIA.Process.Exporting;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.LinkedHashMap;

import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;

import io.github.mianalysis.MIA.Module.ModuleCollection;
import io.github.mianalysis.MIA.Object.Workspace;
import io.github.mianalysis.MIA.Object.WorkspaceCollection;
import io.github.mianalysis.MIA.Object.References.Abstract.SpreadsheetWriter;
import io.github.mianalysis.MIA.Object.References.Collections.MetadataRefCollection;
import io.github.mianalysis.MIA.Process.AnalysisHandling.Analysis;

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

        // Iterating over each metadata value, adding values
        for (SpreadsheetWriter ref:metadataRefs.values()) ref.addSummaryXLSX(sheet,workspaces);

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
