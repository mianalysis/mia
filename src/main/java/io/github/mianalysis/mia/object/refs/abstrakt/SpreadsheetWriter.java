package io.github.mianalysis.mia.object.refs.abstrakt;

import org.apache.poi.ss.usermodel.Sheet;
import io.github.mianalysis.mia.object.Workspace;

import java.util.LinkedHashMap;

public interface SpreadsheetWriter {
    public void addSummaryXLSX(Sheet sheet, LinkedHashMap<Integer, Workspace> workspaces);
}
