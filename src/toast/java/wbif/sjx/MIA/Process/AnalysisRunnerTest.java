package wbif.sjx.MIA.Process;

import org.junit.Ignore;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import wbif.sjx.MIA.Module.Hidden.InputControl;
import wbif.sjx.MIA.Module.Hidden.OutputControl;
import wbif.sjx.MIA.MIA;
import wbif.sjx.MIA.Process.AnalysisHandling.AnalysisRunner;

import java.io.File;

import static org.junit.Assert.*;

/**
 * Created by sc13967 on 22/06/2018.
 */
public class AnalysisRunnerTest {
    @Test @Ignore
    public void startAnalysis() throws Exception {
    }

    @Test @Ignore
    public void stopAnalysis() throws Exception {
    }


    // TESTS FOR GETTING THE INPUT FILE

    @Test
    public void testGetInputFileSingleFilePresent() throws Exception {
        // Creating a fake file
        TemporaryFolder temporaryFolder = new TemporaryFolder();
        temporaryFolder.create();
        File file = temporaryFolder.newFile("fake file.tif");
        String path = file.getAbsolutePath();

        InputControl inputControl = new InputControl(null);
        inputControl.updateParameterValue(InputControl.INPUT_PATH,path);

        File actual = AnalysisRunner.getInputFile(inputControl);

        assertNotNull(actual);
        assert(actual.isFile());
        assertEquals("fake file.tif",actual.getName());
        assertEquals(path,actual.getAbsolutePath());

    }

    @Test
    public void testGetInputFileSingleFileMissing() throws Exception {
        InputControl inputControl = new InputControl(null);
        inputControl.updateParameterValue(InputControl.INPUT_PATH,"");

        File actual = AnalysisRunner.getInputFile(inputControl);

        assertNull(actual);

    }

    @Test
    public void testGetInputFileBatchPresent() throws Exception {
        // Creating a fake folder
        TemporaryFolder temporaryFolder = new TemporaryFolder();
        temporaryFolder.create();
        File folder = temporaryFolder.newFolder("test folder");
        String path = folder.getAbsolutePath();

        InputControl inputControl = new InputControl(null);
        inputControl.updateParameterValue(InputControl.INPUT_PATH,path);

        File actual = AnalysisRunner.getInputFile(inputControl);

        assertNotNull(actual);
        assert(actual.isDirectory());
        assertEquals("test folder",actual.getName());
        assertEquals(path,actual.getAbsolutePath());

    }


    // TESTS FOR FILE VALIDITY

    @Test
    public void testCheckInputFileValidityNoFileFolderSet() throws Exception {
        boolean actual = AnalysisRunner.checkInputFileValidity(null);

        assertFalse(actual);

    }

    @Test
    public void testCheckInputFileValidityMissingFile() throws Exception {
        // Creating a fake folder, but checking a file which doesn't exist
        TemporaryFolder temporaryFolder = new TemporaryFolder();
        temporaryFolder.create();
        File folder = temporaryFolder.newFolder("test folder");
        String path = folder.getAbsolutePath() + MIA.getSlashes() + "fake file.tif";

        boolean actual = AnalysisRunner.checkInputFileValidity(path);

        assertFalse(actual);

    }

    @Test
    public void testCheckInputFileValidityMissingFolder() throws Exception {
        // Creating a fake folder, but checking a folder which doesn't exist
        TemporaryFolder temporaryFolder = new TemporaryFolder();
        temporaryFolder.create();
        File folder = temporaryFolder.newFolder("test folder");
        String path = folder.getParent() + MIA.getSlashes() + "test fake folder" + MIA.getSlashes();

        boolean actual = AnalysisRunner.checkInputFileValidity(path);

        assertFalse(actual);

    }

    @Test
    public void testCheckInputFileValidityCorrectFile() throws Exception {
        // Creating a fake file
        TemporaryFolder temporaryFolder = new TemporaryFolder();
        temporaryFolder.create();
        File file = temporaryFolder.newFile("fake file.tif");
        String path = file.getAbsolutePath();

        boolean actual = AnalysisRunner.checkInputFileValidity(path);

        assertTrue(actual);

    }

    @Test
    public void testCheckInputFileValidityCorrectFolder() throws Exception {
        // Creating a fake folder
        TemporaryFolder temporaryFolder = new TemporaryFolder();
        temporaryFolder.create();
        File folder = temporaryFolder.newFolder("test folder");
        String path = folder.getAbsolutePath();

        boolean actual = AnalysisRunner.checkInputFileValidity(path);

        assertTrue(actual);

    }


    // TESTS FOR GENERATION OF EXCEL FILE FILENAMES

    @Test
    public void testGetExportNameSingleFileSingleSeries() throws Exception {
        InputControl inputControl = new InputControl(null);
        OutputControl outputControl = new OutputControl(null);

        inputControl.updateParameterValue(InputControl.SERIES_MODE,InputControl.SeriesModes.SERIES_LIST);
        inputControl.updateParameterValue(InputControl.SERIES_LIST,"3");

        TemporaryFolder temporaryFolder = new TemporaryFolder();
        temporaryFolder.create();
        File file = temporaryFolder.newFile("fake file.tif");

        String actual = AnalysisRunner.getExportName(inputControl,outputControl,file);
        String expected = file.getParent()+ MIA.getSlashes() + "fake file_S3";

        assertEquals(expected,actual);

    }

    @Test
    public void testGetExportNameSingleFileAllSeries() throws Exception {
        InputControl inputControl = new InputControl(null);
        OutputControl outputControl = new OutputControl(null);
        
        inputControl.updateParameterValue(InputControl.SERIES_MODE,InputControl.SeriesModes.ALL_SERIES);

        TemporaryFolder temporaryFolder = new TemporaryFolder();
        temporaryFolder.create();
        File file = temporaryFolder.newFile("fake file.tif");

        String actual = AnalysisRunner.getExportName(inputControl,outputControl,file);
        String expected = file.getParent()+ MIA.getSlashes() + "fake file";

        assertEquals(expected,actual);

    }

    @Test
    public void testGetExportNameBatchSingleSeries() throws Exception {
        InputControl inputControl = new InputControl(null);
        OutputControl outputControl = new OutputControl(null);

        inputControl.updateParameterValue(InputControl.SERIES_MODE,InputControl.SeriesModes.SERIES_LIST);
        inputControl.updateParameterValue(InputControl.SERIES_LIST,"3");

        // Creating a fake folder
        TemporaryFolder temporaryFolder = new TemporaryFolder();
        temporaryFolder.create();
        File folder = temporaryFolder.newFolder("test folder");

        String actual = AnalysisRunner.getExportName(inputControl,outputControl,folder);
        String expected = folder+MIA.getSlashes()+folder.getName()+"_S3";

        assertEquals(expected,actual);

    }

    @Test
    public void testGetExportNameBatchAllSeries() throws Exception {
        InputControl inputControl = new InputControl(null);
        OutputControl outputControl = new OutputControl(null);
        
        inputControl.updateParameterValue(InputControl.SERIES_MODE,InputControl.SeriesModes.ALL_SERIES);

        // Creating a fake folder
        TemporaryFolder temporaryFolder = new TemporaryFolder();
        temporaryFolder.create();
        File folder = temporaryFolder.newFolder("test folder");

        String actual = AnalysisRunner.getExportName(inputControl,outputControl,folder);
        String expected = folder+MIA.getSlashes()+folder.getName();

        assertEquals(expected,actual);

    }


    // TESTS FOR SETTING FILENAME FILTERS

    @Test @Ignore
    public void addFilenameFilters() throws Exception {
    }

    @Test @Ignore
    public void initialiseExporter() throws Exception {
    }

}