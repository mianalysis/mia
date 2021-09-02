package io.github.mianalysis.MIA.Process.AnalysisHandling;
//package io.github.mianalysis.MIA.Process.AnalysisHandling;
//
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.io.TempDir;
//import io.github.mianalysis.MIA.Module.Hidden.InputControl;
//import io.github.mianalysis.MIA.Module.Hidden.OutputControl;
//import io.github.mianalysis.MIA.Module.ImageProcessing.Pixel.FilterImage;
//import io.github.mianalysis.MIA.Module.InputOutput.ImageLoader;
//import io.github.mianalysis.MIA.Module.ObjectProcessing.Identification.IdentifyObjects;
//import io.github.mianalysis.MIA.Module.Modules;
//
//import java.io.File;
//import java.io.IOException;
//import java.nio.file.Path;
//
//import static org.junit.jupiter.api.Assertions.*;
//
//public class AnalysisTesterTest {
//    @Test
//    public void testModulesAllCorrect(@TempDir Path tempPath) throws IOException {
//        // Initialising Modules
//        Modules modules = new Modules();
//
//        File testFile = new File(tempPath+File.separator+"TestFile.tif");
//        testFile.createNewFile();
//
//        modules.getInputControl().updateParameterValue(InputControl.INPUT_PATH,testFile.getAbsolutePath());
//
//        // Adding a couple of test modules
//        ImageLoader imageLoader = new ImageLoader(modules);
//        imageLoader.updateParameterValue(ImageLoader.OUTPUT_IMAGE,"Test image");
//        modules.add(imageLoader);
//
//        FilterImage filterImage = new FilterImage(modules);
//        filterImage.updateParameterValue(FilterImage.INPUT_IMAGE,"Test image");
//        filterImage.updateParameterValue(FilterImage.APPLY_TO_INPUT,false);
//        filterImage.updateParameterValue(FilterImage.OUTPUT_IMAGE,"New output");
//        modules.add(filterImage);
//
//        IdentifyObjects identifyObjects = new IdentifyObjects(modules);
//        identifyObjects.updateParameterValue(IdentifyObjects.INPUT_IMAGE,"New output");
//        identifyObjects.updateParameterValue(IdentifyObjects.OUTPUT_OBJECTS,"Obs");
//        modules.add(identifyObjects);
//
//        assertEquals(3,AnalysisTester.testModules(modules));
//
//    }
//
//    @Test
//    public void testModulesOneIncorrect(@TempDir Path tempPath) throws IOException {
//        // Initialising Modules
//        Modules modules = new Modules();
//
//        File testFile = new File(tempPath+File.separator+"TestFile.tif");
//        testFile.createNewFile();
//
//        modules.getInputControl().updateParameterValue(InputControl.INPUT_PATH,testFile.getAbsolutePath());
//
//        // Adding a couple of test modules
//        ImageLoader imageLoader = new ImageLoader(modules);
//        imageLoader.updateParameterValue(ImageLoader.OUTPUT_IMAGE,"Test image");
//        modules.add(imageLoader);
//
//        FilterImage filterImage = new FilterImage(modules);
//        filterImage.updateParameterValue(FilterImage.INPUT_IMAGE,"Toast image");
//        filterImage.updateParameterValue(FilterImage.APPLY_TO_INPUT,false);
//        filterImage.updateParameterValue(FilterImage.OUTPUT_IMAGE,"New output");
//        modules.add(filterImage);
//
//        IdentifyObjects identifyObjects = new IdentifyObjects(modules);
//        identifyObjects.updateParameterValue(IdentifyObjects.INPUT_IMAGE,"New output");
//        identifyObjects.updateParameterValue(IdentifyObjects.OUTPUT_OBJECTS,"Obs");
//        modules.add(identifyObjects);
//
//        assertEquals(1,AnalysisTester.testModules(modules));
//        assertTrue(imageLoader.isRunnable());
//        assertFalse(filterImage.isRunnable());
//        assertFalse(identifyObjects.isRunnable());
//
//    }
//
//    @Test
//    public void testModulesOneIncorrectAnotherOneDisabled(@TempDir Path tempPath) throws IOException {
//        // Initialising Modules
//        Modules modules = new Modules();
//
//        File testFile = new File(tempPath+File.separator+"TestFile.tif");
//        testFile.createNewFile();
//
//        modules.getInputControl().updateParameterValue(InputControl.INPUT_PATH,testFile.getAbsolutePath());
//
//        // Adding a couple of test modules
//        ImageLoader imageLoader = new ImageLoader(modules);
//        imageLoader.updateParameterValue(ImageLoader.OUTPUT_IMAGE,"Test image");
//        modules.add(imageLoader);
//
//        FilterImage filterImage = new FilterImage(modules);
//        filterImage.updateParameterValue(FilterImage.INPUT_IMAGE,"Toast image");
//        filterImage.updateParameterValue(FilterImage.APPLY_TO_INPUT,false);
//        filterImage.updateParameterValue(FilterImage.OUTPUT_IMAGE,"New output");
//        modules.add(filterImage);
//
//        IdentifyObjects identifyObjects = new IdentifyObjects(modules);
//        identifyObjects.setEnabled(false);
//        identifyObjects.updateParameterValue(IdentifyObjects.INPUT_IMAGE,"New output");
//        identifyObjects.updateParameterValue(IdentifyObjects.OUTPUT_OBJECTS,"Obs");
//        modules.add(identifyObjects);
//
//        assertEquals(1,AnalysisTester.testModules(modules));
//        assertTrue(imageLoader.isRunnable());
//        assertFalse(filterImage.isRunnable());
//        assertFalse(identifyObjects.isRunnable());
//
//    }
//
//    @Test
//    public void testModulesOneIncorrectAndDisabled(@TempDir Path tempPath) throws IOException {
//        // Initialising Modules
//        Modules modules = new Modules();
//
//        File testFile = new File(tempPath+File.separator+"TestFile.tif");
//        testFile.createNewFile();
//
//        modules.getInputControl().updateParameterValue(InputControl.INPUT_PATH,testFile.getAbsolutePath());
//
//        // Adding a couple of test modules
//        ImageLoader imageLoader = new ImageLoader(modules);
//        imageLoader.updateParameterValue(ImageLoader.OUTPUT_IMAGE,"Test image");
//        modules.add(imageLoader);
//
//        FilterImage filterImage = new FilterImage(modules);
//        filterImage.updateParameterValue(FilterImage.INPUT_IMAGE,"Test image");
//        filterImage.updateParameterValue(FilterImage.APPLY_TO_INPUT,false);
//        filterImage.updateParameterValue(FilterImage.OUTPUT_IMAGE,"New output");
//        modules.add(filterImage);
//
//        IdentifyObjects identifyObjects = new IdentifyObjects(modules);
//        identifyObjects.setEnabled(false);
//        identifyObjects.updateParameterValue(IdentifyObjects.INPUT_IMAGE,"Noo output");
//        identifyObjects.updateParameterValue(IdentifyObjects.OUTPUT_OBJECTS,"Obs");
//        modules.add(identifyObjects);
//
//        assertEquals(2,AnalysisTester.testModules(modules));
//        assertTrue(imageLoader.isRunnable());
//        assertTrue(filterImage.isRunnable());
//        assertFalse(identifyObjects.isRunnable());
//
//    }
//
//    @Test
//    public void testModulesAllDisabled(@TempDir Path tempPath) throws IOException {
//        // Initialising Modules
//        Modules modules = new Modules();
//
//        File testFile = new File(tempPath+File.separator+"TestFile.tif");
//        testFile.createNewFile();
//
//        modules.getInputControl().updateParameterValue(InputControl.INPUT_PATH,testFile.getAbsolutePath());
//
//        // Adding a couple of test modules
//        ImageLoader imageLoader = new ImageLoader(modules);
//        imageLoader.setEnabled(false);
//        imageLoader.updateParameterValue(ImageLoader.OUTPUT_IMAGE,"Test image");
//        modules.add(imageLoader);
//
//        FilterImage filterImage = new FilterImage(modules);
//        filterImage.setEnabled(false);
//        filterImage.updateParameterValue(FilterImage.INPUT_IMAGE,"Test image");
//        filterImage.updateParameterValue(FilterImage.APPLY_TO_INPUT,false);
//        filterImage.updateParameterValue(FilterImage.OUTPUT_IMAGE,"New output");
//        modules.add(filterImage);
//
//        IdentifyObjects identifyObjects = new IdentifyObjects(modules);
//        identifyObjects.setEnabled(false);
//        identifyObjects.updateParameterValue(IdentifyObjects.INPUT_IMAGE,"New output");
//        identifyObjects.updateParameterValue(IdentifyObjects.OUTPUT_OBJECTS,"Obs");
//        modules.add(identifyObjects);
//
//        assertEquals(0,AnalysisTester.testModules(modules));
//        assertTrue(imageLoader.isRunnable());
//        assertFalse(filterImage.isRunnable());
//        assertFalse(identifyObjects.isRunnable());
//
//    }
//
//    @Test
//    public void testModulesErrorInInput() {
//        // Initialising Modules
//        Modules modules = new Modules();
//
//        modules.getInputControl().updateParameterValue(InputControl.INPUT_PATH,null);
//
//        // Adding a couple of test modules
//        ImageLoader imageLoader = new ImageLoader(modules);
//        imageLoader.updateParameterValue(ImageLoader.OUTPUT_IMAGE,"Test image");
//        modules.add(imageLoader);
//
//        FilterImage filterImage = new FilterImage(modules);
//        filterImage.updateParameterValue(FilterImage.INPUT_IMAGE,"Test image");
//        filterImage.updateParameterValue(FilterImage.APPLY_TO_INPUT,false);
//        filterImage.updateParameterValue(FilterImage.OUTPUT_IMAGE,"New output");
//        modules.add(filterImage);
//
//        IdentifyObjects identifyObjects = new IdentifyObjects(modules);
//        identifyObjects.updateParameterValue(IdentifyObjects.INPUT_IMAGE,"New output");
//        identifyObjects.updateParameterValue(IdentifyObjects.OUTPUT_OBJECTS,"Obs");
//        modules.add(identifyObjects);
//
//        // Errors in the input module shouldn't lead to the module being non-runnable.  This is so the user interface
//        // doesn't lose all its elements from other modules.
//        assertEquals(3,AnalysisTester.testModules(modules));
//        assertTrue(modules.getInputControl().isRunnable());
//        assertTrue(imageLoader.isRunnable());
//        assertTrue(filterImage.isRunnable());
//        assertTrue(identifyObjects.isRunnable());
//        assertTrue(modules.getOutputControl().isRunnable());
//
//    }
//
//    @Test
//    public void testModulesErrorInOutput(@TempDir Path tempPath) throws IOException {
//        File testFile = new File(tempPath+File.separator+"TestFile.tif");
//        testFile.createNewFile();
//
//        // Initialising Modules
//        Modules modules = new Modules();
//
//        modules.getInputControl().updateParameterValue(InputControl.INPUT_PATH,testFile.getAbsolutePath());
//
//        // Adding a couple of test modules
//        ImageLoader imageLoader = new ImageLoader(modules);
//        imageLoader.updateParameterValue(ImageLoader.OUTPUT_IMAGE,"Test image");
//        modules.add(imageLoader);
//
//        FilterImage filterImage = new FilterImage(modules);
//        filterImage.updateParameterValue(FilterImage.INPUT_IMAGE,"Test image");
//        filterImage.updateParameterValue(FilterImage.APPLY_TO_INPUT,false);
//        filterImage.updateParameterValue(FilterImage.OUTPUT_IMAGE,"New output");
//        modules.add(filterImage);
//
//        IdentifyObjects identifyObjects = new IdentifyObjects(modules);
//        identifyObjects.updateParameterValue(IdentifyObjects.INPUT_IMAGE,"New output");
//        identifyObjects.updateParameterValue(IdentifyObjects.OUTPUT_OBJECTS,"Obs");
//        modules.add(identifyObjects);
//
//        OutputControl outputControl = modules.getOutputControl();
//        outputControl.updateParameterValue(OutputControl.SAVE_LOCATION,OutputControl.SaveLocations.SPECIFIC_LOCATION);
//        outputControl.updateParameterValue(OutputControl.SAVE_FILE_PATH,null);
//
//        assertEquals(3,AnalysisTester.testModules(modules));
//        assertTrue(modules.getInputControl().isRunnable());
//        assertTrue(imageLoader.isRunnable());
//        assertTrue(filterImage.isRunnable());
//        assertTrue(identifyObjects.isRunnable());
//        assertTrue(modules.getOutputControl().isRunnable());
//
//    }
//
//    @Test
//    public void testModulesEmptyCollection(@TempDir Path tempPath) throws IOException {
//        File testFile = new File(tempPath+File.separator+"TestFile.tif");
//        testFile.createNewFile();
//
//        // Initialising Modules
//        Modules modules = new Modules();
//
//        InputControl inputControl = modules.getInputControl();
//        inputControl.updateParameterValue(InputControl.INPUT_PATH,testFile.getAbsolutePath());
//
//        assertEquals(0,AnalysisTester.testModules(modules));
//        assertTrue(modules.getInputControl().isRunnable());
//        assertTrue(modules.getOutputControl().isRunnable());
//
//    }
//
//    @Test
//    public void testModuleAllCorrect(@TempDir Path tempPath) throws IOException {
//        // Initialising Modules
//        Modules modules = new Modules();
//
//        File testFile = new File(tempPath+File.separator+"TestFile.tif");
//        testFile.createNewFile();
//
//        modules.getInputControl().updateParameterValue(InputControl.INPUT_PATH,testFile.getAbsolutePath());
//
//        // Adding a couple of test modules
//        ImageLoader imageLoader = new ImageLoader(modules);
//        imageLoader.updateParameterValue(ImageLoader.OUTPUT_IMAGE,"Test image");
//        modules.add(imageLoader);
//
//        FilterImage filterImage = new FilterImage(modules);
//        filterImage.updateParameterValue(FilterImage.INPUT_IMAGE,"Test image");
//        filterImage.updateParameterValue(FilterImage.APPLY_TO_INPUT,false);
//        filterImage.updateParameterValue(FilterImage.OUTPUT_IMAGE,"New output");
//        modules.add(filterImage);
//
//        IdentifyObjects identifyObjects = new IdentifyObjects(modules);
//        identifyObjects.updateParameterValue(IdentifyObjects.INPUT_IMAGE,"New output");
//        identifyObjects.updateParameterValue(IdentifyObjects.OUTPUT_OBJECTS,"Obs");
//        modules.add(identifyObjects);
//
//        // Checking the general module result
//        assertTrue(AnalysisTester.testModule(identifyObjects,modules));
//
//        // Checking the correct parameters were flagged
//        assertTrue(identifyObjects.getParameter(IdentifyObjects.INPUT_IMAGE).isValid());
//        assertTrue(identifyObjects.getParameter(IdentifyObjects.OUTPUT_OBJECTS).isValid());
//        assertTrue(identifyObjects.getParameter(IdentifyObjects.WHITE_BACKGROUND).isValid());
//
//    }
//
//    @Test
//    public void testModuleOneIncorrect(@TempDir Path tempPath) throws IOException {
//        // Initialising Modules
//        Modules modules = new Modules();
//
//        File testFile = new File(tempPath+File.separator+"TestFile.tif");
//        testFile.createNewFile();
//
//        modules.getInputControl().updateParameterValue(InputControl.INPUT_PATH,testFile.getAbsolutePath());
//
//        // Adding a couple of test modules
//        ImageLoader imageLoader = new ImageLoader(modules);
//        imageLoader.updateParameterValue(ImageLoader.OUTPUT_IMAGE,"Test image");
//        modules.add(imageLoader);
//
//        FilterImage filterImage = new FilterImage(modules);
//        filterImage.updateParameterValue(FilterImage.INPUT_IMAGE,"Test image");
//        filterImage.updateParameterValue(FilterImage.APPLY_TO_INPUT,false);
//        filterImage.updateParameterValue(FilterImage.OUTPUT_IMAGE,"Noo output");
//        modules.add(filterImage);
//
//        IdentifyObjects identifyObjects = new IdentifyObjects(modules);
//        identifyObjects.updateParameterValue(IdentifyObjects.INPUT_IMAGE,"New output");
//        identifyObjects.updateParameterValue(IdentifyObjects.OUTPUT_OBJECTS,"Obs");
//        modules.add(identifyObjects);
//
//        // Checking the general module result
//        assertFalse(AnalysisTester.testModule(identifyObjects,modules));
//
//        // Checking the correct parameters were flagged
//        assertFalse(identifyObjects.getParameter(IdentifyObjects.INPUT_IMAGE).isValid());
//        assertTrue(identifyObjects.getParameter(IdentifyObjects.OUTPUT_OBJECTS).isValid());
//        assertTrue(identifyObjects.getParameter(IdentifyObjects.WHITE_BACKGROUND).isValid());
//
//    }
//
//    @Test
//    public void testModuleNull(@TempDir Path tempPath) throws IOException {
//        // Initialising Modules
//        Modules modules = new Modules();
//
//        File testFile = new File(tempPath+File.separator+"TestFile.tif");
//        testFile.createNewFile();
//
//        modules.getInputControl().updateParameterValue(InputControl.INPUT_PATH,testFile.getAbsolutePath());
//
//        // Adding a couple of test modules
//        ImageLoader imageLoader = new ImageLoader(modules);
//        imageLoader.updateParameterValue(ImageLoader.OUTPUT_IMAGE,"Test image");
//        modules.add(imageLoader);
//
//        FilterImage filterImage = new FilterImage(modules);
//        filterImage.updateParameterValue(FilterImage.INPUT_IMAGE,"Test image");
//        filterImage.updateParameterValue(FilterImage.APPLY_TO_INPUT,false);
//        filterImage.updateParameterValue(FilterImage.OUTPUT_IMAGE,"Noo output");
//        modules.add(filterImage);
//
//        IdentifyObjects identifyObjects = new IdentifyObjects(modules);
//        identifyObjects.updateParameterValue(IdentifyObjects.INPUT_IMAGE,"New output");
//        identifyObjects.updateParameterValue(IdentifyObjects.OUTPUT_OBJECTS,"Obs");
//        modules.add(identifyObjects);
//
//        // Checking the general module result
//        assertFalse(AnalysisTester.testModule(null,modules));
//
//    }
//}