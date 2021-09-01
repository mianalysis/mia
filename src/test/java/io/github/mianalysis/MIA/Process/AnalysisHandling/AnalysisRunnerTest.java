package io.github.mianalysis.MIA.Process.AnalysisHandling;
//package io.github.mianalysis.MIA.Process.AnalysisHandling;
//
//import org.junit.jupiter.api.Disabled;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.io.TempDir;
//import io.github.mianalysis.MIA.Module.Hidden.InputControl;
//import io.github.mianalysis.MIA.Module.Hidden.OutputControl;
//import io.github.mianalysis.MIA.MIA;
//import io.github.mianalysis.MIA.Module.ModuleCollection;
//import io.github.mianalysis.MIA.Process.AnalysisHandling.AnalysisRunner;
//
//import java.io.File;
//import java.nio.file.Path;
//
//import static org.junit.jupiter.api.Assertions.*;
//
///**
// * Created by sc13967 on 22/06/2018.
// */
//public class AnalysisRunnerTest {
//    @Test @Disabled
//    public void startAnalysis() {
//    }
//
//    @Test @Disabled
//    public void stopAnalysis() {
//    }
//
//
//    // TESTS FOR GETTING THE INPUT FILE
//
//    @Test
//    public void testGetInputFileSingleFilePresent(@TempDir Path tempPath) throws Exception {
//        File file = new File(tempPath+File.separator+"fake file.tif");
//        file.createNewFile();
//        String path = file.getAbsolutePath();
//
//        InputControl inputControl = new InputControl(null);
//        inputControl.updateParameterValue(InputControl.INPUT_PATH,path);
//
//        File actual = AnalysisRunner.getInputFile(inputControl);
//
//        assertNotNull(actual);
//        assert(actual.isFile());
//        assertEquals("fake file.tif",actual.getName());
//        assertEquals(path,actual.getAbsolutePath());
//
//    }
//
//    @Test
//    public void testGetInputFileSingleFileMissing() {
//        InputControl inputControl = new InputControl(null);
//        inputControl.updateParameterValue(InputControl.INPUT_PATH,"");
//
//        File actual = AnalysisRunner.getInputFile(inputControl);
//
//        assertNull(actual);
//
//    }
//
//    @Test
//    public void testGetInputFileBatchPresent(@TempDir Path tempPath) {
//        File file = new File(tempPath.toString());
//
//        InputControl inputControl = new InputControl(null);
//        inputControl.updateParameterValue(InputControl.INPUT_PATH,file.getPath());
//
//        File actual = AnalysisRunner.getInputFile(inputControl);
//
//        assertNotNull(actual);
//        assert(actual.isDirectory());
//
//    }
//
//
//    // TESTS FOR FILE VALIDITY
//
//    @Test
//    public void testCheckInputFileValidityNoFileFolderSet() {
//        boolean actual = AnalysisRunner.checkInputFileValidity(null);
//
//        assertFalse(actual);
//
//    }
//
//    @Test
//    public void testCheckInputFileValidityMissingFile(@TempDir Path tempPath) throws Exception {
//        File folder = new File(tempPath+File.separator+"test folder\\");
//        folder.createNewFile();
//        String path = folder.getAbsolutePath() + File.separator + "fake file.tif";
//
//        boolean actual = AnalysisRunner.checkInputFileValidity(path);
//
//        assertFalse(actual);
//
//    }
//
//    @Test
//    public void testCheckInputFileValidityMissingFolder(@TempDir Path tempPath) throws Exception {
//        File folder = new File(tempPath+File.separator+"test folder\\");
//        folder.createNewFile();
//        String path = folder.getParent() + File.separator + "test fake folder" + File.separator;
//
//        boolean actual = AnalysisRunner.checkInputFileValidity(path);
//
//        assertFalse(actual);
//
//    }
//
//    @Test
//    public void testCheckInputFileValidityCorrectFile(@TempDir Path tempPath) throws Exception {
//        // Creating a fake file
//        File file = new File(tempPath+File.separator+"fake file.tif");
//        file.createNewFile();
//        String path = file.getAbsolutePath();
//
//        boolean actual = AnalysisRunner.checkInputFileValidity(path);
//
//        assertTrue(actual);
//
//    }
//
//    @Test
//    public void testCheckInputFileValidityCorrectFolder(@TempDir Path tempPath) throws Exception {
//        // Creating a fake folder
//        File folder = new File(tempPath+File.separator+"test folder\\");
//        folder.createNewFile();
//        String path = folder.getAbsolutePath();
//
//        boolean actual = AnalysisRunner.checkInputFileValidity(path);
//
//        assertTrue(actual);
//
//    }
//
//
//    // TESTS FOR GENERATION OF EXCEL FILE FILENAMES
//
//    @Test
//    public void testGetExportNameSingleFileSingleSeries(@TempDir Path tempPath) throws Exception {
//        ModuleCollection modules = new ModuleCollection();
//        InputControl inputControl = modules.getInputControl();
//        OutputControl outputControl = modules.getOutputControl();
//
//        inputControl.updateParameterValue(InputControl.SERIES_MODE,InputControl.SeriesModes.SERIES_LIST);
//        inputControl.updateParameterValue(InputControl.SERIES_LIST,"3");
//
//        File file = new File(tempPath+File.separator+"fake file.tif");
//        file.createNewFile();
//
//        String actual = AnalysisRunner.getExportName(inputControl,outputControl,file);
//        String expected = file.getParent()+ File.separator + "fake file_S3";
//
//        assertEquals(expected,actual);
//
//    }
//
//    @Test
//    public void testGetExportNameSingleFileAllSeries(@TempDir Path tempPath) throws Exception {
//        ModuleCollection modules = new ModuleCollection();
//        InputControl inputControl = modules.getInputControl();
//        OutputControl outputControl = modules.getOutputControl();
//
//        inputControl.updateParameterValue(InputControl.SERIES_MODE,InputControl.SeriesModes.ALL_SERIES);
//
//        File file = new File(tempPath+File.separator+"fake file.tif");
//        file.createNewFile();
//
//        String actual = AnalysisRunner.getExportName(inputControl,outputControl,file);
//        String expected = file.getParent()+ File.separator + "fake file";
//
//        assertEquals(expected,actual);
//
//    }
//
//    @Test
//    public void testGetExportNameBatchSingleSeries(@TempDir Path tempPath) throws Exception {
//        ModuleCollection modules = new ModuleCollection();
//        InputControl inputControl = modules.getInputControl();
//        OutputControl outputControl = modules.getOutputControl();
//
//        inputControl.updateParameterValue(InputControl.SERIES_MODE,InputControl.SeriesModes.SERIES_LIST);
//        inputControl.updateParameterValue(InputControl.SERIES_LIST,"3");
//
//        // Creating a fake folder
//        File folder = new File(tempPath+File.separator+"test folder\\");
//        folder.mkdirs();
//        folder.createNewFile();
//
//        String actual = AnalysisRunner.getExportName(inputControl,outputControl,folder);
//        String expected = folder+File.separator+folder.getName()+"_S3";
//
//        assertEquals(expected,actual);
//
//    }
//
//    @Test
//    public void testGetExportNameBatchAllSeries(@TempDir Path tempPath) throws Exception {
//        ModuleCollection modules = new ModuleCollection();
//        InputControl inputControl = modules.getInputControl();
//        OutputControl outputControl = modules.getOutputControl();
//
//        inputControl.updateParameterValue(InputControl.SERIES_MODE,InputControl.SeriesModes.ALL_SERIES);
//
//        // Creating a fake folder
//        File folder = new File(tempPath+File.separator+"test folder\\");
//        folder.mkdirs();
//        folder.createNewFile();
//
//        String actual = AnalysisRunner.getExportName(inputControl,outputControl,folder);
//        String expected = folder+File.separator+folder.getName();
//
//        assertEquals(expected,actual);
//
//    }
//
//
//    // TESTS FOR SETTING FILENAME FILTERS
//
//    @Test @Disabled
//    public void addFilenameFilters() throws Exception {
//    }
//
//    @Test @Disabled
//    public void initialiseExporter() throws Exception {
//    }
//
//}