package wbif.sjx.ModularImageAnalysis.Module.InputOutput;

import org.junit.Ignore;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import wbif.sjx.ModularImageAnalysis.Object.Workspace;

import java.io.File;
import java.io.IOException;

import static org.junit.Assert.*;

public class MetadataExtractorTest {

    @Test
    public void testGetTitle() {
        assertNotNull(new MetadataExtractor().getTitle());
    }

    @Test
    public void testRunKeywordMatching() throws IOException {
        // Creating the fake file to process
        TemporaryFolder temporaryFolder = new TemporaryFolder();
        temporaryFolder.create();
        File testFile = temporaryFolder.newFile("Test filename with k2 keyword.tif");

        // Creating a new workspace
        Workspace workspace = new Workspace(0,testFile);

        // Creating list of keywords
        String keywords = "Keyword1, k2, with gaps";

        // Setting up the module
        MetadataExtractor extractor = new MetadataExtractor();
        extractor.initialiseParameters();
        extractor.updateParameterValue(MetadataExtractor.EXTRACTOR_MODE,MetadataExtractor.ExtractorModes.KEYWORD_MODE);
        extractor.updateParameterValue(MetadataExtractor.KEYWORD_LIST,keywords);

        // Running the module
        extractor.run(workspace,false);

        // Testing the returned value
        assertEquals("k2",workspace.getMetadata().getKeyword());

    }

    @Test
    public void testRunKeywordMatchingWithGaps() throws IOException {
        // Creating the fake file to process
        TemporaryFolder temporaryFolder = new TemporaryFolder();
        temporaryFolder.create();
        File testFile = temporaryFolder.newFile("Test filename with gaps keyword.tif");

        // Creating a new workspace
        Workspace workspace = new Workspace(0,testFile);

        // Creating list of keywords
        String keywords = "Keyword1, k2, with gaps";

        // Setting up the module
        MetadataExtractor extractor = new MetadataExtractor();
        extractor.initialiseParameters();
        extractor.updateParameterValue(MetadataExtractor.EXTRACTOR_MODE,MetadataExtractor.ExtractorModes.KEYWORD_MODE);
        extractor.updateParameterValue(MetadataExtractor.KEYWORD_LIST,keywords);

        // Running the module
        extractor.run(workspace,false);

        // Testing the returned value
        assertEquals("with gaps",workspace.getMetadata().getKeyword());

    }

    @Test
    public void testRunKeywordMatchingWithSymbols() throws IOException {
        // Creating the fake file to process
        TemporaryFolder temporaryFolder = new TemporaryFolder();
        temporaryFolder.create();
        File testFile = temporaryFolder.newFile("Test filename with %$ keyword.tif");

        // Creating a new workspace
        Workspace workspace = new Workspace(0,testFile);

        // Creating list of keywords
        String keywords = "Keyword1, k2, %$, with gaps";

        // Setting up the module
        MetadataExtractor extractor = new MetadataExtractor();
        extractor.initialiseParameters();
        extractor.updateParameterValue(MetadataExtractor.EXTRACTOR_MODE,MetadataExtractor.ExtractorModes.KEYWORD_MODE);
        extractor.updateParameterValue(MetadataExtractor.KEYWORD_LIST,keywords);

        // Running the module
        extractor.run(workspace,false);

        // Testing the returned value
        assertEquals("%$",workspace.getMetadata().getKeyword());

    }

    @Test @Ignore
    public void testRunKeywordMissing() throws IOException {
        // Creating the fake file to process
        TemporaryFolder temporaryFolder = new TemporaryFolder();
        temporaryFolder.create();
        File testFile = temporaryFolder.newFile("Test filename without keyword.tif");

        // Creating a new workspace
        Workspace workspace = new Workspace(0,testFile);

        // Creating list of keywords
        String keywords = "Keyword1, k2, with gaps";

        // Setting up the module
        MetadataExtractor extractor = new MetadataExtractor();
        extractor.initialiseParameters();
        extractor.updateParameterValue(MetadataExtractor.EXTRACTOR_MODE,MetadataExtractor.ExtractorModes.KEYWORD_MODE);
        extractor.updateParameterValue(MetadataExtractor.KEYWORD_LIST,keywords);

        // Running the module
        extractor.run(workspace,false);

        // Testing the returned value
        assertEquals("",workspace.getMetadata().getKeyword());

    }
}