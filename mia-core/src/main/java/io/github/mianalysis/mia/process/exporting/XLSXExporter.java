package io.github.mianalysis.mia.process.exporting;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.LinkedHashMap;

import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;

import io.github.mianalysis.mia.module.Modules;
import io.github.mianalysis.mia.object.Workspace;
import io.github.mianalysis.mia.object.Workspaces;
import io.github.mianalysis.mia.object.refs.abstrakt.SpreadsheetWriter;
import io.github.mianalysis.mia.object.refs.collections.MetadataRefs;
import io.github.mianalysis.mia.process.analysishandling.Analysis;

public class XLSXExporter {

    public void exportSummary(String path, Workspaces workspaces, Analysis analysis) {
        // Getting modules
        Modules modules = analysis.getModules();

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

    public void addMetadataSummary(Sheet sheet, LinkedHashMap<Integer,Workspace> workspaces, Modules modules) {
        MetadataRefs metadataRefs = modules.getMetadataRefs(null);

        // Iterating over each metadata value, adding values
        for (SpreadsheetWriter ref:metadataRefs.values()) ref.addSummaryXLSX(sheet,workspaces);

    }

    public LinkedHashMap<Integer,Workspace> createRowList(Sheet sheet, Workspaces workspaces) {
        LinkedHashMap<Integer,Workspace> workspacesMap = new LinkedHashMap<>();

        int rowN = 1;
        for (Workspace workspace:workspaces) {
            sheet.createRow(rowN);
            workspacesMap.put(rowN++,workspace);
        }

        return workspacesMap;

    }
}
