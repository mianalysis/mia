package wbif.sjx.MIA.Module.InputOutput;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import wbif.sjx.MIA.Module.Module;
import wbif.sjx.MIA.Module.ModuleCollection;
import wbif.sjx.MIA.Module.ModuleTest;
import wbif.sjx.MIA.Object.Workspace;
import wbif.sjx.MIA.Object.WorkspaceCollection;

public class MetadataExtractorTest extends ModuleTest {
    @BeforeAll
    public static void setVerbose() {
        Module.setVerbose(false);
    }

    @Override
    public void testGetHelp() {
        assertNotNull(new MetadataExtractor(null).getDescription());
    }

    @Test
    public void testRunKeywordMatching(@TempDir Path tempPath) throws IOException {
        File testFile = new File(tempPath+File.separator+"Test filename with k2 keyword.tif");
        testFile.createNewFile();

        // Creating a new workspace
        WorkspaceCollection workspaces = new WorkspaceCollection();
        Workspace workspace = workspaces.getNewWorkspace(testFile,1);

        // Creating list of keywords
        String keywords = "Keyword1, k2, with gaps";

        // Setting up the module
        MetadataExtractor extractor = new MetadataExtractor(new ModuleCollection());
        extractor.initialiseParameters();
        extractor.updateParameterValue(MetadataExtractor.EXTRACTOR_MODE,MetadataExtractor.ExtractorModes.KEYWORD_MODE);
        extractor.updateParameterValue(MetadataExtractor.KEYWORD_LIST,keywords);

        // Running the module
        extractor.execute(workspace);

        // Testing the returned value
        assertEquals("k2",workspace.getMetadata().getKeyword());

    }

    @Test
    public void testRunKeywordMatchingWithGaps(@TempDir Path tempPath) throws IOException {
        File testFile = new File(tempPath+File.separator+"Test filename with gaps keyword.tif");
        testFile.createNewFile();

        // Creating a new workspace
        WorkspaceCollection workspaces = new WorkspaceCollection();
        Workspace workspace = workspaces.getNewWorkspace(testFile,1);

        // Creating list of keywords
        String keywords = "Keyword1, k2, with gaps";

        // Setting up the module
        MetadataExtractor extractor = new MetadataExtractor(new ModuleCollection());
        extractor.initialiseParameters();
        extractor.updateParameterValue(MetadataExtractor.EXTRACTOR_MODE,MetadataExtractor.ExtractorModes.KEYWORD_MODE);
        extractor.updateParameterValue(MetadataExtractor.KEYWORD_LIST,keywords);

        // Running the module
        extractor.execute(workspace);

        // Testing the returned value
        assertEquals("with gaps",workspace.getMetadata().getKeyword());

    }

    @Test
    public void testRunKeywordMatchingWithSymbols(@TempDir Path tempPath) throws IOException {
        File testFile = new File(tempPath+File.separator+"Test filename with %$ keyword.tif");
        testFile.createNewFile();

        // Creating a new workspace
        WorkspaceCollection workspaces = new WorkspaceCollection();
        Workspace workspace = workspaces.getNewWorkspace(testFile,1);

        // Creating list of keywords
        String keywords = "Keyword1, k2, %$, with gaps";

        // Setting up the module
        MetadataExtractor extractor = new MetadataExtractor(new ModuleCollection());
        extractor.initialiseParameters();
        extractor.updateParameterValue(MetadataExtractor.EXTRACTOR_MODE,MetadataExtractor.ExtractorModes.KEYWORD_MODE);
        extractor.updateParameterValue(MetadataExtractor.KEYWORD_LIST,keywords);

        // Running the module
        extractor.execute(workspace);

        // Testing the returned value
        assertEquals("%$",workspace.getMetadata().getKeyword());

    }

    @Test
    public void testRunKeywordMissing(@TempDir Path tempPath) throws IOException {
        File testFile = new File(tempPath+File.separator+"Test filename without keyword.tif");
        testFile.createNewFile();

        // Creating a new workspace
        WorkspaceCollection workspaces = new WorkspaceCollection();
        Workspace workspace = workspaces.getNewWorkspace(testFile,1);

        // Creating list of keywords
        String keywords = "Keyword1, k2, with gaps";

        // Setting up the module
        MetadataExtractor extractor = new MetadataExtractor(new ModuleCollection());
        extractor.initialiseParameters();
        extractor.updateParameterValue(MetadataExtractor.EXTRACTOR_MODE,MetadataExtractor.ExtractorModes.KEYWORD_MODE);
        extractor.updateParameterValue(MetadataExtractor.KEYWORD_LIST,keywords);

        // Running the module
        extractor.execute(workspace);

        // Testing the returned value
        assertEquals("",workspace.getMetadata().getKeyword());

    }
}