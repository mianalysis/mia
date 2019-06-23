package wbif.sjx.MIA.Object.References.Abstract;

import org.apache.poi.ss.usermodel.Sheet;
import wbif.sjx.MIA.Object.Workspace;

import java.util.LinkedHashMap;

public interface SpreadsheetWriter {
    public void addSummaryXLSX(Sheet sheet, LinkedHashMap<Integer, Workspace> workspaces);
}
