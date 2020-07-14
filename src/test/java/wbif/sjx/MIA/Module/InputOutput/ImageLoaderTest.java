package wbif.sjx.MIA.Module.InputOutput;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.io.File;
import java.net.URLDecoder;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import ij.IJ;
import wbif.sjx.MIA.MIA;
import wbif.sjx.MIA.Module.Module;
import wbif.sjx.MIA.Module.ModuleCollection;
import wbif.sjx.MIA.Module.ModuleTest;
import wbif.sjx.MIA.Module.ImageProcessing.Stack.CropImage;
import wbif.sjx.MIA.Object.Image;
import wbif.sjx.MIA.Object.Status;
import wbif.sjx.MIA.Object.Units;
import wbif.sjx.MIA.Object.Workspace;
import wbif.sjx.MIA.Object.WorkspaceCollection;

/**
 * Created by Stephen on 29/08/2017.
 */
public class ImageLoaderTest extends ModuleTest {
    @BeforeAll
    public static void setVerbose() {
        Module.setVerbose(false);
    }

    @BeforeEach
    public void setupTest() {
        Units.setUnits(Units.SpatialUnits.MICROMETRE);
    }

    @Override
    public void testGetHelp() {
        assertNotNull(new ImageLoader(null).getDescription());

    }

    @Test
    public void testCheckBitDepthLessThan8() {
        assertEquals(8,ImageLoader.checkBitDepth(1));
        assertEquals(8,ImageLoader.checkBitDepth(6));
        assertEquals(8,ImageLoader.checkBitDepth(7));

    }

    @Test
    public void testCheckBitDepthEqualTo8() {
        assertEquals(8,ImageLoader.checkBitDepth(8));

    }

    @Test
    public void testCheckBitDepthBetween8And16() {
        assertEquals(16,ImageLoader.checkBitDepth(9));
        assertEquals(16,ImageLoader.checkBitDepth(12));
        assertEquals(16,ImageLoader.checkBitDepth(14));
        assertEquals(16,ImageLoader.checkBitDepth(15));

    }

    @Test
    public void testCheckBitDepthEqualTo16() {
        assertEquals(16,ImageLoader.checkBitDepth(16));

    }

    @Test
    public void testCheckBitDepthBetween16And32() {
        assertEquals(32,ImageLoader.checkBitDepth(17));
        assertEquals(32,ImageLoader.checkBitDepth(21));
        assertEquals(32,ImageLoader.checkBitDepth(24));
        assertEquals(32,ImageLoader.checkBitDepth(31));

    }

    @Test
    public void testCheckBitDepthEqualTo32() {
        assertEquals(32,ImageLoader.checkBitDepth(32));

    }

    @Test
    public void testCheckBitDepthGreaterThan32() {
        assertEquals(-1,ImageLoader.checkBitDepth(33));

    }

    @Test
    public void testRunWithSpecificTiffFile() throws Exception {
        // Initialising a blank workspace
        WorkspaceCollection workspaces = new WorkspaceCollection();
        Workspace workspace = workspaces.getNewWorkspace(null,1);

        // Initialising ImageFileLoader
        ImageLoader imageLoader = new ImageLoader(new ModuleCollection());
        imageLoader.initialiseParameters();

        // Setting parameters
        imageLoader.updateParameterValue(ImageLoader.IMPORT_MODE, ImageLoader.ImportModes.SPECIFIC_FILE);
        String pathToImage = URLDecoder.decode(this.getClass().getResource("/images/BlankHyperstack5D_8bit.tif").getPath(),"UTF-8");
        imageLoader.updateParameterValue(ImageLoader.FILE_PATH,pathToImage);
        imageLoader.updateParameterValue(ImageLoader.OUTPUT_IMAGE,"Test_Output_Image");

        // Running module
        imageLoader.execute(workspace);

        // Checking there is one image in the workspace
        assertEquals(1,workspace.getImages().size());

        // Getting the loaded image
        Image image = workspace.getImage("Test_Output_Image");

        // Checking the image has the expected name
        assertEquals("Test_Output_Image",image.getName());

        // Checking there are no measurements associated with this image
        assertEquals(0,image.getMeasurements().size());

        // Checking the dimensions of the image
        assertEquals(64,image.getImagePlus().getWidth());
        assertEquals(76,image.getImagePlus().getHeight());
        assertEquals(2,image.getImagePlus().getNChannels());
        assertEquals(12,image.getImagePlus().getNSlices());
        assertEquals(6,image.getImagePlus().getNFrames());

        // Checking the image has the expected calibration
        assertEquals(0.02,image.getImagePlus().getCalibration().pixelWidth,1E-10);
        assertEquals(0.02,image.getImagePlus().getCalibration().pixelHeight,1E-10);
        assertEquals(0.1,image.getImagePlus().getCalibration().pixelDepth,1E-10);

    }

    @Test
    public void testRunWithCurrentTiffFile() throws Exception {
        // Getting path to image file
        String pathToImage = URLDecoder.decode(this.getClass().getResource("/images/BlankHyperstack5D_8bit.tif").getPath(),"UTF-8");

        // Initialising a blank workspace
        WorkspaceCollection workspaces = new WorkspaceCollection();
        Workspace workspace = workspaces.getNewWorkspace(new File(pathToImage),1);

        // Initialising the ImageFileLoader
        ImageLoader imageFileLoader = new ImageLoader(new ModuleCollection());
        imageFileLoader.initialiseParameters();

        // Setting parameters
        imageFileLoader.updateParameterValue(ImageLoader.IMPORT_MODE, ImageLoader.ImportModes.CURRENT_FILE);
        imageFileLoader.updateParameterValue(ImageLoader.OUTPUT_IMAGE,"Test_Output_Image");

        // Running module
        imageFileLoader.execute(workspace);

        // Checking there is one image in the workspace
        assertEquals(1,workspace.getImages().size());

        // Getting the loaded image
        Image image = workspace.getImage("Test_Output_Image");

        // Checking the image has the expected name
        assertEquals("Test_Output_Image",image.getName());

        // Checking there are no measurements associated with this image
        assertEquals(0,image.getMeasurements().size());

        // Checking the dimensions of the image
        assertEquals(64,image.getImagePlus().getWidth());
        assertEquals(76,image.getImagePlus().getHeight());
        assertEquals(2,image.getImagePlus().getNChannels());
        assertEquals(12,image.getImagePlus().getNSlices());
        assertEquals(6,image.getImagePlus().getNFrames());

        // Checking the image has the expected calibration
        assertEquals(0.02,image.getImagePlus().getCalibration().pixelWidth,1E-10);
        assertEquals(0.02,image.getImagePlus().getCalibration().pixelHeight,1E-10);
        assertEquals(0.1,image.getImagePlus().getCalibration().pixelDepth,1E-10);

    }

    @Test
    public void testRunWithCurrentTiffFileBioformats() throws Exception {
        // Getting path to image file
        String pathToImage = URLDecoder.decode(this.getClass().getResource("/images/BlankHyperstack5D_8bit.tif").getPath(),"UTF-8");

        // Initialising a blank workspace
        WorkspaceCollection workspaces = new WorkspaceCollection();
        Workspace workspace = workspaces.getNewWorkspace(new File(pathToImage),1);

        // Initialising the ImageFileLoader
        ImageLoader imageFileLoader = new ImageLoader(new ModuleCollection());
        imageFileLoader.initialiseParameters();

        // Setting parameters
        imageFileLoader.updateParameterValue(ImageLoader.IMPORT_MODE, ImageLoader.ImportModes.CURRENT_FILE);
        imageFileLoader.updateParameterValue(ImageLoader.OUTPUT_IMAGE,"Test_Output_Image");

        // Running module
        imageFileLoader.execute(workspace);

        // Checking there is one image in the workspace
        assertEquals(1,workspace.getImages().size());

        // Getting the loaded image
        Image image = workspace.getImage("Test_Output_Image");

        // Checking the image has the expected name
        assertEquals("Test_Output_Image",image.getName());

        // Checking there are no measurements associated with this image
        assertEquals(0,image.getMeasurements().size());

        // Checking the dimensions of the image
        assertEquals(64,image.getImagePlus().getWidth());
        assertEquals(76,image.getImagePlus().getHeight());
        assertEquals(2,image.getImagePlus().getNChannels());
        assertEquals(12,image.getImagePlus().getNSlices());
        assertEquals(6,image.getImagePlus().getNFrames());

        // Checking the image has the expected calibration
        assertEquals(0.02,image.getImagePlus().getCalibration().pixelWidth,1E-10);
        assertEquals(0.02,image.getImagePlus().getCalibration().pixelHeight,1E-10);
        assertEquals(0.1,image.getImagePlus().getCalibration().pixelDepth,1E-10);

    }

    @Test
    public void testRunWithCurrentTiffSpecifiedCalibration() throws Exception {
        // Getting path to image file
        String pathToImage = URLDecoder.decode(this.getClass().getResource("/images/BlankHyperstack5D_8bit.tif").getPath(),"UTF-8");

        // Initialising a blank workspace
        WorkspaceCollection workspaces = new WorkspaceCollection();
        Workspace workspace = workspaces.getNewWorkspace(new File(pathToImage),1);

        // Initialising the ImageFileLoader
        ImageLoader imageFileLoader = new ImageLoader(new ModuleCollection());
        imageFileLoader.initialiseParameters();

        // Setting parameters
        imageFileLoader.updateParameterValue(ImageLoader.IMPORT_MODE, ImageLoader.ImportModes.CURRENT_FILE);
        imageFileLoader.updateParameterValue(ImageLoader.OUTPUT_IMAGE,"Test_Output_Image");
        imageFileLoader.updateParameterValue(ImageLoader.CHANNELS,"1-end");
        imageFileLoader.updateParameterValue(ImageLoader.SLICES,"1-end");
        imageFileLoader.updateParameterValue(ImageLoader.FRAMES,"1-end");
        imageFileLoader.updateParameterValue(ImageLoader.SET_CAL,true);
        imageFileLoader.updateParameterValue(ImageLoader.XY_CAL,0.5);
        imageFileLoader.updateParameterValue(ImageLoader.Z_CAL,0.2);

        // Running module
        imageFileLoader.execute(workspace);

        // Checking there is one image in the workspace
        assertEquals(1,workspace.getImages().size());

        // Getting the loaded image
        Image image = workspace.getImage("Test_Output_Image");

        // Checking the image has the expected name
        assertEquals("Test_Output_Image",image.getName());

        // Checking there are no measurements associated with this image
        assertEquals(0,image.getMeasurements().size());

        // Checking the dimensions of the image
        assertEquals(64,image.getImagePlus().getWidth());
        assertEquals(76,image.getImagePlus().getHeight());
        assertEquals(2,image.getImagePlus().getNChannels());
        assertEquals(12,image.getImagePlus().getNSlices());
        assertEquals(6,image.getImagePlus().getNFrames());

        // Checking the image has the expected calibration
        assertEquals(0.5,image.getImagePlus().getCalibration().pixelWidth,1E-10);
        assertEquals(0.5,image.getImagePlus().getCalibration().pixelHeight,1E-10);
        assertEquals(0.2,image.getImagePlus().getCalibration().pixelDepth,1E-10);

    }

    @Test
    public void testRunWithCurrentTiffSubsetC() throws Exception {
        // Getting path to image file
        String pathToImage = URLDecoder.decode(this.getClass().getResource("/images/BlankHyperstack5D_8bit.tif").getPath(),"UTF-8");

        // Initialising a blank workspace
        WorkspaceCollection workspaces = new WorkspaceCollection();
        Workspace workspace = workspaces.getNewWorkspace(new File(pathToImage),1);

        // Initialising the ImageFileLoader
        ImageLoader imageFileLoader = new ImageLoader(new ModuleCollection());
        imageFileLoader.initialiseParameters();

        // Setting parameters
        imageFileLoader.updateParameterValue(ImageLoader.IMPORT_MODE, ImageLoader.ImportModes.CURRENT_FILE);
        imageFileLoader.updateParameterValue(ImageLoader.OUTPUT_IMAGE,"Test_Output_Image");
        imageFileLoader.updateParameterValue(ImageLoader.CHANNELS,"2-2");
        imageFileLoader.updateParameterValue(ImageLoader.SLICES,"1-end");
        imageFileLoader.updateParameterValue(ImageLoader.FRAMES,"1-end");
        imageFileLoader.updateParameterValue(ImageLoader.SET_CAL,false);

        // Running module
        imageFileLoader.execute(workspace);

        // Checking there is one image in the workspace
        assertEquals(1,workspace.getImages().size());

        // Getting the loaded image
        Image image = workspace.getImage("Test_Output_Image");

        // Checking the image has the expected name
        assertEquals("Test_Output_Image",image.getName());

        // Checking there are no measurements associated with this image
        assertEquals(0,image.getMeasurements().size());

        // Checking the dimensions of the image
        assertEquals(64,image.getImagePlus().getWidth());
        assertEquals(76,image.getImagePlus().getHeight());
        assertEquals(1,image.getImagePlus().getNChannels());
        assertEquals(12,image.getImagePlus().getNSlices());
        assertEquals(6,image.getImagePlus().getNFrames());

        // Checking the image has the expected calibration
        assertEquals(0.02,image.getImagePlus().getCalibration().pixelWidth,1E-10);
        assertEquals(0.02,image.getImagePlus().getCalibration().pixelHeight,1E-10);
        assertEquals(0.1,image.getImagePlus().getCalibration().pixelDepth,1E-10);

    }

    @Test
    public void testRunWithCurrentTiffSubsetCTooLow() throws Exception {
        // Getting path to image file
        String pathToImage = URLDecoder.decode(this.getClass().getResource("/images/BlankHyperstack5D_8bit.tif").getPath(),"UTF-8");

        // Initialising a blank workspace
        WorkspaceCollection workspaces = new WorkspaceCollection();
        Workspace workspace = workspaces.getNewWorkspace(new File(pathToImage),1);

        // Initialising the ImageFileLoader
        ImageLoader imageFileLoader = new ImageLoader(new ModuleCollection());
        imageFileLoader.initialiseParameters();

        // Setting parameters
        imageFileLoader.updateParameterValue(ImageLoader.IMPORT_MODE, ImageLoader.ImportModes.CURRENT_FILE);
        imageFileLoader.updateParameterValue(ImageLoader.OUTPUT_IMAGE,"Test_Output_Image");
        imageFileLoader.updateParameterValue(ImageLoader.CHANNELS,"0-3");
        imageFileLoader.updateParameterValue(ImageLoader.SLICES,"1-end");
        imageFileLoader.updateParameterValue(ImageLoader.FRAMES,"1-end");
        imageFileLoader.updateParameterValue(ImageLoader.SET_CAL,false);

        // Running module
        Status status = imageFileLoader.execute(workspace);

        assertEquals(Status.FAIL,status);

    }

    @Test
    public void testRunWithCurrentTiffSubsetCTooHigh() throws Exception {
        // Getting path to image file
        String pathToImage = URLDecoder.decode(this.getClass().getResource("/images/BlankHyperstack5D_8bit.tif").getPath(),"UTF-8");

        // Initialising a blank workspace
        WorkspaceCollection workspaces = new WorkspaceCollection();
        Workspace workspace = workspaces.getNewWorkspace(new File(pathToImage),1);

        // Initialising the ImageFileLoader
        ImageLoader imageFileLoader = new ImageLoader(new ModuleCollection());
        imageFileLoader.initialiseParameters();

        // Setting parameters
        imageFileLoader.updateParameterValue(ImageLoader.IMPORT_MODE, ImageLoader.ImportModes.CURRENT_FILE);
        imageFileLoader.updateParameterValue(ImageLoader.OUTPUT_IMAGE,"Test_Output_Image");
        imageFileLoader.updateParameterValue(ImageLoader.CHANNELS,"5-8");
        imageFileLoader.updateParameterValue(ImageLoader.SLICES,"1-end");
        imageFileLoader.updateParameterValue(ImageLoader.FRAMES,"1-end");
        imageFileLoader.updateParameterValue(ImageLoader.SET_CAL,false);

        // Running module
        Status status = imageFileLoader.execute(workspace);

        assertEquals(Status.FAIL,status);

    }

    @Test
    public void testRunWithCurrentTiffSubsetZ() throws Exception {
        // Getting path to image file
        String pathToImage = URLDecoder.decode(this.getClass().getResource("/images/BlankHyperstack5D_8bit.tif").getPath(),"UTF-8");

        // Initialising a blank workspace
        WorkspaceCollection workspaces = new WorkspaceCollection();
        Workspace workspace = workspaces.getNewWorkspace(new File(pathToImage),1);

        // Initialising the ImageFileLoader
        ImageLoader imageFileLoader = new ImageLoader(new ModuleCollection());
        imageFileLoader.initialiseParameters();

        // Setting parameters
        imageFileLoader.updateParameterValue(ImageLoader.IMPORT_MODE, ImageLoader.ImportModes.CURRENT_FILE);
        imageFileLoader.updateParameterValue(ImageLoader.OUTPUT_IMAGE,"Test_Output_Image");
        imageFileLoader.updateParameterValue(ImageLoader.CHANNELS,"1-end");
        imageFileLoader.updateParameterValue(ImageLoader.SLICES,"3-6");
        imageFileLoader.updateParameterValue(ImageLoader.FRAMES,"1-end");
        imageFileLoader.updateParameterValue(ImageLoader.SET_CAL,false);

        // Running module
        imageFileLoader.execute(workspace);

        // Checking there is one image in the workspace
        assertEquals(1,workspace.getImages().size());

        // Getting the loaded image
        Image image = workspace.getImage("Test_Output_Image");

        // Checking the image has the expected name
        assertEquals("Test_Output_Image",image.getName());

        // Checking there are no measurements associated with this image
        assertEquals(0,image.getMeasurements().size());

        // Checking the dimensions of the image
        assertEquals(64,image.getImagePlus().getWidth());
        assertEquals(76,image.getImagePlus().getHeight());
        assertEquals(2,image.getImagePlus().getNChannels());
        assertEquals(4,image.getImagePlus().getNSlices());
        assertEquals(6,image.getImagePlus().getNFrames());

        // Checking the image has the expected calibration
        assertEquals(0.02,image.getImagePlus().getCalibration().pixelWidth,1E-10);
        assertEquals(0.02,image.getImagePlus().getCalibration().pixelHeight,1E-10);
        assertEquals(0.1,image.getImagePlus().getCalibration().pixelDepth,1E-10);

    }

    @Test
    public void testRunWithCurrentTiffSubsetZTooLow() throws Exception {
        // Getting path to image file
        String pathToImage = URLDecoder.decode(this.getClass().getResource("/images/BlankHyperstack5D_8bit.tif").getPath(),"UTF-8");

        // Initialising a blank workspace
        WorkspaceCollection workspaces = new WorkspaceCollection();
        Workspace workspace = workspaces.getNewWorkspace(new File(pathToImage),1);

        // Initialising the ImageFileLoader
        ImageLoader imageFileLoader = new ImageLoader(new ModuleCollection());
        imageFileLoader.initialiseParameters();

        // Setting parameters
        imageFileLoader.updateParameterValue(ImageLoader.IMPORT_MODE, ImageLoader.ImportModes.CURRENT_FILE);
        imageFileLoader.updateParameterValue(ImageLoader.OUTPUT_IMAGE,"Test_Output_Image");
        imageFileLoader.updateParameterValue(ImageLoader.CHANNELS,"1-end");
        imageFileLoader.updateParameterValue(ImageLoader.SLICES,"0-1");
        imageFileLoader.updateParameterValue(ImageLoader.FRAMES,"1-end");
        imageFileLoader.updateParameterValue(ImageLoader.SET_CAL,false);

        // Running module
        Status status = imageFileLoader.execute(workspace);

        assertEquals(Status.FAIL,status);

    }

    @Test
    public void testRunWithCurrentTiffSubsetZTooHigh() throws Exception {
        // Getting path to image file
        String pathToImage = URLDecoder.decode(this.getClass().getResource("/images/BlankHyperstack5D_8bit.tif").getPath(),"UTF-8");

        // Initialising a blank workspace
        WorkspaceCollection workspaces = new WorkspaceCollection();
        Workspace workspace = workspaces.getNewWorkspace(new File(pathToImage),1);

        // Initialising the ImageFileLoader
        ImageLoader imageFileLoader = new ImageLoader(new ModuleCollection());
        imageFileLoader.initialiseParameters();

        // Setting parameters
        imageFileLoader.updateParameterValue(ImageLoader.IMPORT_MODE, ImageLoader.ImportModes.CURRENT_FILE);
        imageFileLoader.updateParameterValue(ImageLoader.OUTPUT_IMAGE,"Test_Output_Image");
        imageFileLoader.updateParameterValue(ImageLoader.CHANNELS,"1-end");
        imageFileLoader.updateParameterValue(ImageLoader.SLICES,"13");
        imageFileLoader.updateParameterValue(ImageLoader.FRAMES,"1-end");
        imageFileLoader.updateParameterValue(ImageLoader.SET_CAL,false);

        // Running module
        Status status = imageFileLoader.execute(workspace);

        assertEquals(Status.FAIL, status);

    }

    @Test
    public void testRunWithCurrentTiffSubsetT() throws Exception {
        // Getting path to image file
        String pathToImage = URLDecoder.decode(this.getClass().getResource("/images/BlankHyperstack5D_8bit.tif").getPath(),"UTF-8");

        // Initialising a blank workspace
        WorkspaceCollection workspaces = new WorkspaceCollection();
        Workspace workspace = workspaces.getNewWorkspace(new File(pathToImage),1);

        // Initialising the ImageFileLoader
        ImageLoader imageFileLoader = new ImageLoader(new ModuleCollection());
        imageFileLoader.initialiseParameters();

        // Setting parameters
        imageFileLoader.updateParameterValue(ImageLoader.IMPORT_MODE, ImageLoader.ImportModes.CURRENT_FILE);
        imageFileLoader.updateParameterValue(ImageLoader.OUTPUT_IMAGE,"Test_Output_Image");
        imageFileLoader.updateParameterValue(ImageLoader.CHANNELS,"1-end");
        imageFileLoader.updateParameterValue(ImageLoader.SLICES,"1-end");
        imageFileLoader.updateParameterValue(ImageLoader.FRAMES,"2-4");
        imageFileLoader.updateParameterValue(ImageLoader.SET_CAL,false);

        // Running module
        imageFileLoader.execute(workspace);

        // Checking there is one image in the workspace
        assertEquals(1,workspace.getImages().size());

        // Getting the loaded image
        Image image = workspace.getImage("Test_Output_Image");

        // Checking the image has the expected name
        assertEquals("Test_Output_Image",image.getName());

        // Checking there are no measurements associated with this image
        assertEquals(0,image.getMeasurements().size());

        // Checking the dimensions of the image
        assertEquals(64,image.getImagePlus().getWidth());
        assertEquals(76,image.getImagePlus().getHeight());
        assertEquals(2,image.getImagePlus().getNChannels());
        assertEquals(12,image.getImagePlus().getNSlices());
        assertEquals(3,image.getImagePlus().getNFrames());

        // Checking the image has the expected calibration
        assertEquals(0.02,image.getImagePlus().getCalibration().pixelWidth,1E-10);
        assertEquals(0.02,image.getImagePlus().getCalibration().pixelHeight,1E-10);
        assertEquals(0.1,image.getImagePlus().getCalibration().pixelDepth,1E-10);

    }

    @Test
    public void testRunWithCurrentTiffSubsetTTooLow() throws Exception {
        // Getting path to image file
        String pathToImage = URLDecoder.decode(this.getClass().getResource("/images/BlankHyperstack5D_8bit.tif").getPath(),"UTF-8");

        // Initialising a blank workspace
        WorkspaceCollection workspaces = new WorkspaceCollection();
        Workspace workspace = workspaces.getNewWorkspace(new File(pathToImage),1);

        // Initialising the ImageFileLoader
        ImageLoader imageFileLoader = new ImageLoader(new ModuleCollection());
        imageFileLoader.initialiseParameters();

        // Setting parameters
        imageFileLoader.updateParameterValue(ImageLoader.IMPORT_MODE, ImageLoader.ImportModes.CURRENT_FILE);
        imageFileLoader.updateParameterValue(ImageLoader.OUTPUT_IMAGE,"Test_Output_Image");
        imageFileLoader.updateParameterValue(ImageLoader.CHANNELS,"1-end");
        imageFileLoader.updateParameterValue(ImageLoader.SLICES,"1-end");
        imageFileLoader.updateParameterValue(ImageLoader.FRAMES,"-4");
        imageFileLoader.updateParameterValue(ImageLoader.SET_CAL,false);

        // Running module
        Status status = imageFileLoader.execute(workspace);

        assertEquals(Status.FAIL,status);

    }

    @Test
    public void testRunWithCurrentTiffSubsetTTooHigh() throws Exception {
        // Getting path to image file
        String pathToImage = URLDecoder.decode(this.getClass().getResource("/images/BlankHyperstack5D_8bit.tif").getPath(),"UTF-8");

        // Initialising a blank workspace
        WorkspaceCollection workspaces = new WorkspaceCollection();
        Workspace workspace = workspaces.getNewWorkspace(new File(pathToImage),1);

        // Initialising the ImageFileLoader
        ImageLoader imageFileLoader = new ImageLoader(new ModuleCollection());
        imageFileLoader.initialiseParameters();

        // Setting parameters
        imageFileLoader.updateParameterValue(ImageLoader.IMPORT_MODE, ImageLoader.ImportModes.CURRENT_FILE);
        imageFileLoader.updateParameterValue(ImageLoader.OUTPUT_IMAGE,"Test_Output_Image");
        imageFileLoader.updateParameterValue(ImageLoader.CHANNELS,"1-end");
        imageFileLoader.updateParameterValue(ImageLoader.SLICES,"1-end");
        imageFileLoader.updateParameterValue(ImageLoader.FRAMES,"7-8");
        imageFileLoader.updateParameterValue(ImageLoader.SET_CAL,false);

        // Running module
        Status status = imageFileLoader.execute(workspace);

        assertEquals(Status.FAIL,status);

    }

    @Test
    public void testRunWithCurrentTiffSubsetAll() throws Exception {
        // Getting path to image file
        String pathToImage = URLDecoder.decode(this.getClass().getResource("/images/BlankHyperstack5D_8bit.tif").getPath(),"UTF-8");

        // Initialising a blank workspace
        WorkspaceCollection workspaces = new WorkspaceCollection();
        Workspace workspace = workspaces.getNewWorkspace(new File(pathToImage),1);

        // Initialising the ImageFileLoader
        ImageLoader imageFileLoader = new ImageLoader(new ModuleCollection());
        imageFileLoader.initialiseParameters();

        // Setting parameters
        imageFileLoader.updateParameterValue(ImageLoader.IMPORT_MODE, ImageLoader.ImportModes.CURRENT_FILE);
        imageFileLoader.updateParameterValue(ImageLoader.OUTPUT_IMAGE,"Test_Output_Image");
        imageFileLoader.updateParameterValue(ImageLoader.CHANNELS,"2");
        imageFileLoader.updateParameterValue(ImageLoader.SLICES,"3-8");
        imageFileLoader.updateParameterValue(ImageLoader.FRAMES,"3,4");
        imageFileLoader.updateParameterValue(ImageLoader.SET_CAL,false);

        // Running module
        imageFileLoader.execute(workspace);

        // Checking there is one image in the workspace
        assertEquals(1,workspace.getImages().size());

        // Getting the loaded image
        Image image = workspace.getImage("Test_Output_Image");

        // Checking the image has the expected name
        assertEquals("Test_Output_Image",image.getName());

        // Checking there are no measurements associated with this image
        assertEquals(0,image.getMeasurements().size());

        // Checking the dimensions of the image
        assertEquals(64,image.getImagePlus().getWidth());
        assertEquals(76,image.getImagePlus().getHeight());
        assertEquals(1,image.getImagePlus().getNChannels());
        assertEquals(6,image.getImagePlus().getNSlices());
        assertEquals(2,image.getImagePlus().getNFrames());

        // Checking the image has the expected calibration
        assertEquals(0.02,image.getImagePlus().getCalibration().pixelWidth,1E-10);
        assertEquals(0.02,image.getImagePlus().getCalibration().pixelHeight,1E-10);
        assertEquals(0.1,image.getImagePlus().getCalibration().pixelDepth,1E-10);

    }

    @Test
    public void testRunWithCurrentLifFile() throws Exception {
        // Getting path to image file
        String pathToImage = URLDecoder.decode(this.getClass().getResource("/images/BlankLif5D_8bit.lif").getPath(),"UTF-8");

        // Initialising a blank workspace
        WorkspaceCollection workspaces = new WorkspaceCollection();
        Workspace workspace = workspaces.getNewWorkspace(new File(pathToImage),1);

        // Initialising the ImageFileLoader
        ImageLoader imageFileLoader = new ImageLoader(new ModuleCollection());
        imageFileLoader.initialiseParameters();

        // Setting parameters
        imageFileLoader.updateParameterValue(ImageLoader.IMPORT_MODE, ImageLoader.ImportModes.CURRENT_FILE);
        imageFileLoader.updateParameterValue(ImageLoader.OUTPUT_IMAGE,"Test_Output_Image");

        // Running module
        imageFileLoader.execute(workspace);

        // Checking there is one image in the workspace
        assertEquals(1,workspace.getImages().size());

        // Getting the loaded image
        Image image = workspace.getImage("Test_Output_Image");

        // Checking the image has the expected name
        assertEquals("Test_Output_Image",image.getName());

        // Checking there are no measurements associated with this image
        assertEquals(0,image.getMeasurements().size());

        // Checking the dimensions of the image
        assertEquals(64,image.getImagePlus().getWidth());
        assertEquals(64,image.getImagePlus().getHeight());
        assertEquals(2,image.getImagePlus().getNChannels());
        assertEquals(12,image.getImagePlus().getNSlices());
        assertEquals(6,image.getImagePlus().getNFrames());

        // Checking the image has the expected calibration
        assertEquals(5.55,image.getImagePlus().getCalibration().pixelWidth,1E-2);
        assertEquals(5.55,image.getImagePlus().getCalibration().pixelHeight,1E-2);
        assertEquals(2.00,image.getImagePlus().getCalibration().pixelDepth,1E-2);

    }

    @Test
    public void testRunWithCropping() throws Exception {
        // Getting path to image file
        String pathToImage = URLDecoder.decode(this.getClass().getResource("/images/NoisyGradient/NoisyGradient5D_8bit.tif").getPath(),"UTF-8");

        // Initialising a blank workspace
        WorkspaceCollection workspaces = new WorkspaceCollection();
        Workspace workspace = workspaces.getNewWorkspace(new File(pathToImage),1);

        // Initialising the ImageFileLoader
        ImageLoader imageFileLoader = new ImageLoader(new ModuleCollection());
        imageFileLoader.initialiseParameters();

        // Setting parameters
        imageFileLoader.updateParameterValue(ImageLoader.IMPORT_MODE, ImageLoader.ImportModes.CURRENT_FILE);
        imageFileLoader.updateParameterValue(ImageLoader.OUTPUT_IMAGE,"Test_Output_Image");
        imageFileLoader.updateParameterValue(ImageLoader.CROP_MODE,ImageLoader.CropModes.FIXED);
        imageFileLoader.updateParameterValue(CropImage.LEFT,3);
        imageFileLoader.updateParameterValue(CropImage.TOP,12);
        imageFileLoader.updateParameterValue(CropImage.WIDTH,49);
        imageFileLoader.updateParameterValue(CropImage.HEIGHT,37);

        // Running module
        imageFileLoader.execute(workspace);

        // Getting expected image
        pathToImage = URLDecoder.decode(this.getClass().getResource("/images/CropImage/NoisyGradient5D_8bit_3-12-52-49.tif").getPath(),"UTF-8");
        Image expectedImage = new Image("Expected", IJ.openImage(pathToImage));

        // Checking there is one image in the workspace
        assertEquals(1,workspace.getImages().size());

        // Checking the output image has the expected calibration
        Image outputImage = workspace.getImage("Test_Output_Image");
        assertEquals(expectedImage,outputImage);

    }

    @Test
    public void testRunWithSpecifiedCalibration() throws Exception {
        // Getting path to image file
        String pathToImage = URLDecoder.decode(this.getClass().getResource("/images/NoisyGradient/NoisyGradient5D_8bit.tif").getPath(),"UTF-8");

        // Initialising a blank workspace
        WorkspaceCollection workspaces = new WorkspaceCollection();
        Workspace workspace = workspaces.getNewWorkspace(new File(pathToImage),1);

        // Initialising the ImageFileLoader
        ImageLoader imageFileLoader = new ImageLoader(new ModuleCollection());
        imageFileLoader.initialiseParameters();

        // Setting parameters
        imageFileLoader.updateParameterValue(ImageLoader.IMPORT_MODE, ImageLoader.ImportModes.CURRENT_FILE);
        imageFileLoader.updateParameterValue(ImageLoader.OUTPUT_IMAGE,"Test_Output_Image");
        imageFileLoader.updateParameterValue(ImageLoader.CROP_MODE,ImageLoader.CropModes.NONE);
        imageFileLoader.updateParameterValue(ImageLoader.SET_CAL,true);
        imageFileLoader.updateParameterValue(ImageLoader.XY_CAL,0.5);
        imageFileLoader.updateParameterValue(ImageLoader.Z_CAL,1.2);

        // Running module
        imageFileLoader.execute(workspace);

        // Checking there is one image in the workspace
        assertEquals(1,workspace.getImages().size());

        // Getting the loaded image
        Image image = workspace.getImage("Test_Output_Image");

        // Checking the dimensions of the image
        assertEquals(64,image.getImagePlus().getWidth());
        assertEquals(76,image.getImagePlus().getHeight());
        assertEquals(2,image.getImagePlus().getNChannels());
        assertEquals(12,image.getImagePlus().getNSlices());
        assertEquals(4,image.getImagePlus().getNFrames());

        // Checking the image has the expected calibration
        assertEquals(0.5,image.getImagePlus().getCalibration().pixelWidth,1E-2);
        assertEquals(0.5,image.getImagePlus().getCalibration().pixelHeight,1E-2);
        assertEquals(1.2,image.getImagePlus().getCalibration().pixelDepth,1E-2);

    }

    @Test
    public void testRunWithNanometreCalibration() throws Exception {
        // Setting the spatial calibration
        Units.setUnits(Units.SpatialUnits.NANOMETRE);

        // Getting path to image file
        String pathToImage = URLDecoder.decode(this.getClass().getResource("/images/NoisyGradient/NoisyGradient5D_8bit.tif").getPath(),"UTF-8");

        // Initialising a blank workspace
        WorkspaceCollection workspaces = new WorkspaceCollection();
        Workspace workspace = workspaces.getNewWorkspace(new File(pathToImage),1);

        // Initialising the ImageFileLoader
        ImageLoader imageFileLoader = new ImageLoader(new ModuleCollection());
        imageFileLoader.initialiseParameters();

        // Setting parameters
        imageFileLoader.updateParameterValue(ImageLoader.IMPORT_MODE, ImageLoader.ImportModes.CURRENT_FILE);
        imageFileLoader.updateParameterValue(ImageLoader.OUTPUT_IMAGE,"Test_Output_Image");
        imageFileLoader.updateParameterValue(ImageLoader.CROP_MODE,ImageLoader.CropModes.NONE);
        imageFileLoader.updateParameterValue(ImageLoader.SET_CAL,false);

        // Running module
        imageFileLoader.execute(workspace);

        // Checking there is one image in the workspace
        assertEquals(1,workspace.getImages().size());

        // Getting the loaded image
        Image image = workspace.getImage("Test_Output_Image");

        // Checking the dimensions of the image
        assertEquals(64,image.getImagePlus().getWidth());
        assertEquals(76,image.getImagePlus().getHeight());
        assertEquals(2,image.getImagePlus().getNChannels());
        assertEquals(12,image.getImagePlus().getNSlices());
        assertEquals(4,image.getImagePlus().getNFrames());

        // Expected calibration
        double dppXY = 0.02*1000;
        double dppZ = 0.1*1000;
        String calibratedUnits = "nm";

        // Checking the image has the expected calibration
        assertEquals(dppXY,image.getImagePlus().getCalibration().pixelWidth,1E-2);
        assertEquals(dppXY,image.getImagePlus().getCalibration().pixelHeight,1E-2);
        assertEquals(dppZ,image.getImagePlus().getCalibration().pixelDepth,1E-2);
        assertEquals(calibratedUnits,image.getImagePlus().getCalibration().getUnits());

        // Need to return calibration to microns, else the other tests may fail
        Units.setUnits(Units.SpatialUnits.MICROMETRE);

    }

    @Test
    public void testRunWithMillimetreCalibration() throws Exception {
        // Setting the spatial calibration
        Units.setUnits(Units.SpatialUnits.MILLIMETRE);

        // Getting path to image file
        String pathToImage = URLDecoder.decode(this.getClass().getResource("/images/NoisyGradient/NoisyGradient5D_8bit.tif").getPath(),"UTF-8");

        // Initialising a blank workspace
        WorkspaceCollection workspaces = new WorkspaceCollection();
        Workspace workspace = workspaces.getNewWorkspace(new File(pathToImage),1);

        // Initialising the ImageFileLoader
        ImageLoader imageFileLoader = new ImageLoader(new ModuleCollection());
        imageFileLoader.initialiseParameters();

        // Setting parameters
        imageFileLoader.updateParameterValue(ImageLoader.IMPORT_MODE, ImageLoader.ImportModes.CURRENT_FILE);
        imageFileLoader.updateParameterValue(ImageLoader.OUTPUT_IMAGE,"Test_Output_Image");
        imageFileLoader.updateParameterValue(ImageLoader.CROP_MODE,ImageLoader.CropModes.NONE);
        imageFileLoader.updateParameterValue(ImageLoader.SET_CAL,false);

        // Running module
        imageFileLoader.execute(workspace);

        // Checking there is one image in the workspace
        assertEquals(1,workspace.getImages().size());

        // Getting the loaded image
        Image image = workspace.getImage("Test_Output_Image");

        // Checking the dimensions of the image
        assertEquals(64,image.getImagePlus().getWidth());
        assertEquals(76,image.getImagePlus().getHeight());
        assertEquals(2,image.getImagePlus().getNChannels());
        assertEquals(12,image.getImagePlus().getNSlices());
        assertEquals(4,image.getImagePlus().getNFrames());

        // Expected calibration
        double dppXY = 0.02*1E-3;
        double dppZ = 0.1*1E-3;
        String calibratedUnits = "mm";

        // Checking the image has the expected calibration
        assertEquals(dppXY,image.getImagePlus().getCalibration().pixelWidth,1E-2);
        assertEquals(dppXY,image.getImagePlus().getCalibration().pixelHeight,1E-2);
        assertEquals(dppZ,image.getImagePlus().getCalibration().pixelDepth,1E-2);
        assertEquals(calibratedUnits,image.getImagePlus().getCalibration().getUnits());

        // Need to return calibration to microns, else the other tests may fail
        Units.setUnits(Units.SpatialUnits.MICROMETRE);

    }

    @Test
    public void testRunThreeDTimeseries()throws Exception {
        // Getting path to image file
        String pathToImage = URLDecoder.decode(this.getClass().getResource("/images/NoisyGradient/NoisyGradient3D_8bit.tif").getPath(),"UTF-8");

        // Initialising a blank workspace
        WorkspaceCollection workspaces = new WorkspaceCollection();
        Workspace workspace = workspaces.getNewWorkspace(new File(pathToImage),1);

        // Initialising the ImageFileLoader
        ImageLoader imageFileLoader = new ImageLoader(new ModuleCollection());
        imageFileLoader.initialiseParameters();

        // Setting parameters
        imageFileLoader.updateParameterValue(ImageLoader.IMPORT_MODE, ImageLoader.ImportModes.CURRENT_FILE);
        imageFileLoader.updateParameterValue(ImageLoader.OUTPUT_IMAGE,"Test_Output_Image");
        imageFileLoader.updateParameterValue(ImageLoader.THREE_D_MODE,ImageLoader.ThreeDModes.TIMESERIES);

        // Running module
        imageFileLoader.execute(workspace);

        // Checking there is one image in the workspace
        assertEquals(1,workspace.getImages().size());

        // Getting the loaded image
        Image image = workspace.getImage("Test_Output_Image");

        // Checking the image has the expected name
        assertEquals("Test_Output_Image",image.getName());

        // Checking there are no measurements associated with this image
        assertEquals(0,image.getMeasurements().size());

        // Checking the dimensions of the image
        assertEquals(64,image.getImagePlus().getWidth());
        assertEquals(76,image.getImagePlus().getHeight());
        assertEquals(1,image.getImagePlus().getNChannels());
        assertEquals(1,image.getImagePlus().getNSlices());
        assertEquals(12,image.getImagePlus().getNFrames());

        // Checking the image has the expected calibration
        assertEquals(0.02,image.getImagePlus().getCalibration().pixelWidth,1E-10);
        assertEquals(0.02,image.getImagePlus().getCalibration().pixelHeight,1E-10);
        assertEquals(1,image.getImagePlus().getCalibration().pixelDepth,1E-10);
    }

    @Test
    public void testRunThreeDStack()throws Exception {
        // Getting path to image file
        String pathToImage = URLDecoder.decode(this.getClass().getResource("/images/NoisyGradient/NoisyGradient3D_8bit.tif").getPath(),"UTF-8");

        // Initialising a blank workspace
        WorkspaceCollection workspaces = new WorkspaceCollection();
        Workspace workspace = workspaces.getNewWorkspace(new File(pathToImage),1);

        // Initialising the ImageFileLoader
        ImageLoader imageFileLoader = new ImageLoader(new ModuleCollection());
        imageFileLoader.initialiseParameters();

        // Setting parameters
        imageFileLoader.updateParameterValue(ImageLoader.IMPORT_MODE, ImageLoader.ImportModes.CURRENT_FILE);
        imageFileLoader.updateParameterValue(ImageLoader.OUTPUT_IMAGE,"Test_Output_Image");
        imageFileLoader.updateParameterValue(ImageLoader.THREE_D_MODE,ImageLoader.ThreeDModes.ZSTACK);

        // Running module
        imageFileLoader.execute(workspace);

        // Checking there is one image in the workspace
        assertEquals(1,workspace.getImages().size());

        // Getting the loaded image
        Image image = workspace.getImage("Test_Output_Image");

        // Checking the image has the expected name
        assertEquals("Test_Output_Image",image.getName());

        // Checking there are no measurements associated with this image
        assertEquals(0,image.getMeasurements().size());

        // Checking the dimensions of the image
        assertEquals(64,image.getImagePlus().getWidth());
        assertEquals(76,image.getImagePlus().getHeight());
        assertEquals(1,image.getImagePlus().getNChannels());
        assertEquals(12,image.getImagePlus().getNSlices());
        assertEquals(1,image.getImagePlus().getNFrames());

        // Checking the image has the expected calibration
        assertEquals(0.02,image.getImagePlus().getCalibration().pixelWidth,1E-10);
        assertEquals(0.02,image.getImagePlus().getCalibration().pixelHeight,1E-10);
        assertEquals(0.1,image.getImagePlus().getCalibration().pixelDepth,1E-10);
    }

    @Test
    public void testRunFourDTimeseries()throws Exception {
        // Getting path to image file
        String pathToImage = URLDecoder.decode(this.getClass().getResource("/images/NoisyGradient/NoisyGradient4D_CT_8bit.tif").getPath(),"UTF-8");

        // Initialising a blank workspace
        WorkspaceCollection workspaces = new WorkspaceCollection();
        Workspace workspace = workspaces.getNewWorkspace(new File(pathToImage),1);

        // Initialising the ImageFileLoader
        ImageLoader imageFileLoader = new ImageLoader(new ModuleCollection());
        imageFileLoader.initialiseParameters();

        // Setting parameters
        imageFileLoader.updateParameterValue(ImageLoader.IMPORT_MODE, ImageLoader.ImportModes.CURRENT_FILE);
        imageFileLoader.updateParameterValue(ImageLoader.OUTPUT_IMAGE,"Test_Output_Image");
        imageFileLoader.updateParameterValue(ImageLoader.THREE_D_MODE,ImageLoader.ThreeDModes.TIMESERIES);

        // Running module
        imageFileLoader.execute(workspace);

        // Checking there is one image in the workspace
        assertEquals(1,workspace.getImages().size());

        // Getting the loaded image
        Image image = workspace.getImage("Test_Output_Image");

        // Checking the image has the expected name
        assertEquals("Test_Output_Image",image.getName());

        // Checking there are no measurements associated with this image
        assertEquals(0,image.getMeasurements().size());

        // Checking the dimensions of the image
        assertEquals(64,image.getImagePlus().getWidth());
        assertEquals(76,image.getImagePlus().getHeight());
        assertEquals(2,image.getImagePlus().getNChannels());
        assertEquals(1,image.getImagePlus().getNSlices());
        assertEquals(4,image.getImagePlus().getNFrames());

        // Checking the image has the expected calibration
        assertEquals(0.02,image.getImagePlus().getCalibration().pixelWidth,1E-10);
        assertEquals(0.02,image.getImagePlus().getCalibration().pixelHeight,1E-10);
        assertEquals(1,image.getImagePlus().getCalibration().pixelDepth,1E-10);
    }

    @Test
    public void testRunFourDStack()throws Exception {
        // Getting path to image file
        String pathToImage = URLDecoder.decode(this.getClass().getResource("/images/NoisyGradient/NoisyGradient4D_CZ_8bit.tif").getPath(),"UTF-8");

        // Initialising a blank workspace
        WorkspaceCollection workspaces = new WorkspaceCollection();
        Workspace workspace = workspaces.getNewWorkspace(new File(pathToImage),1);

        // Initialising the ImageFileLoader
        ImageLoader imageFileLoader = new ImageLoader(new ModuleCollection());
        imageFileLoader.initialiseParameters();

        // Setting parameters
        imageFileLoader.updateParameterValue(ImageLoader.IMPORT_MODE, ImageLoader.ImportModes.CURRENT_FILE);
        imageFileLoader.updateParameterValue(ImageLoader.OUTPUT_IMAGE,"Test_Output_Image");
        imageFileLoader.updateParameterValue(ImageLoader.THREE_D_MODE,ImageLoader.ThreeDModes.ZSTACK);

        // Running module
        imageFileLoader.execute(workspace);

        // Checking there is one image in the workspace
        assertEquals(1,workspace.getImages().size());

        // Getting the loaded image
        Image image = workspace.getImage("Test_Output_Image");

        // Checking the image has the expected name
        assertEquals("Test_Output_Image",image.getName());

        // Checking there are no measurements associated with this image
        assertEquals(0,image.getMeasurements().size());

        // Checking the dimensions of the image
        assertEquals(64,image.getImagePlus().getWidth());
        assertEquals(76,image.getImagePlus().getHeight());
        assertEquals(2,image.getImagePlus().getNChannels());
        assertEquals(4,image.getImagePlus().getNSlices());
        assertEquals(1,image.getImagePlus().getNFrames());

        // Checking the image has the expected calibration
        assertEquals(0.02,image.getImagePlus().getCalibration().pixelWidth,1E-10);
        assertEquals(0.02,image.getImagePlus().getCalibration().pixelHeight,1E-10);
        assertEquals(0.1,image.getImagePlus().getCalibration().pixelDepth,1E-10);
    }

    @Test
    public void testRunImageSequenceFull() throws Exception {
        // Getting path to image file
        String pathToImage = URLDecoder.decode(this.getClass().getResource("/images/ImageSequence/Seq0000.tif").getPath(),"UTF-8");
        
        // Initialising a blank workspace
        WorkspaceCollection workspaces = new WorkspaceCollection();
        Workspace workspace = workspaces.getNewWorkspace(new File(pathToImage),1);

        // Initialising the ImageFileLoader
        ImageLoader imageFileLoader = new ImageLoader(new ModuleCollection());
        imageFileLoader.initialiseParameters();
        // Setting parameters
        imageFileLoader.updateParameterValue(ImageLoader.IMPORT_MODE, ImageLoader.ImportModes.IMAGE_SEQUENCE);
        imageFileLoader.updateParameterValue(ImageLoader.SEQUENCE_ROOT_NAME, new File(pathToImage).getParent()+MIA.getSlashes()+"SeqZ{0000}.tif");
        imageFileLoader.updateParameterValue(ImageLoader.OUTPUT_IMAGE, "Test_Output_Image");
        imageFileLoader.updateParameterValue(ImageLoader.FRAMES,"0-end");
        imageFileLoader.updateParameterValue(ImageLoader.CROP_MODE,ImageLoader.CropModes.NONE);

        // Running module
        imageFileLoader.execute(workspace);

        // Getting expected image
        pathToImage = URLDecoder.decode(this.getClass().getResource("/images/ImageSequence/Seq0-11.tif").getPath(),"UTF-8");
        Image expectedImage = new Image("Expected", IJ.openImage(pathToImage));

        // Checking there is one image in the workspace
        assertEquals(1,workspace.getImages().size());

        // Checking the output image has the expected calibration
        Image outputImage = workspace.getImage("Test_Output_Image");
        assertEquals(expectedImage,outputImage);

    }

    @Test
    public void testRunImageSequenceInterval2() throws Exception {
        // Getting path to image file
        String pathToImage = URLDecoder.decode(this.getClass().getResource("/images/ImageSequence/Seq0000.tif").getPath(),"UTF-8");

        // Initialising a blank workspace
        WorkspaceCollection workspaces = new WorkspaceCollection();
        Workspace workspace = workspaces.getNewWorkspace(new File(pathToImage),1);

        // Initialising the ImageFileLoader
        ImageLoader imageFileLoader = new ImageLoader(new ModuleCollection());
        imageFileLoader.initialiseParameters();

        // Setting parameters
        imageFileLoader.updateParameterValue(ImageLoader.IMPORT_MODE, ImageLoader.ImportModes.IMAGE_SEQUENCE);
        imageFileLoader.updateParameterValue(ImageLoader.SEQUENCE_ROOT_NAME, new File(pathToImage).getParent()+MIA.getSlashes()+"\SeqZ{0000}.tif");
        imageFileLoader.updateParameterValue(ImageLoader.OUTPUT_IMAGE, "Test_Output_Image");
        imageFileLoader.updateParameterValue(ImageLoader.FRAMES,"3-end-2");
        imageFileLoader.updateParameterValue(ImageLoader.CROP_MODE,ImageLoader.CropModes.NONE);

        // Running module
        imageFileLoader.execute(workspace);

        // Getting expected image
        pathToImage = URLDecoder.decode(this.getClass().getResource("/images/ImageSequence/Seq3-11-2.tif").getPath(),"UTF-8");
        Image expectedImage = new Image("Expected", IJ.openImage(pathToImage));

        // Checking there is one image in the workspace
        assertEquals(1,workspace.getImages().size());

        // Checking the output image has the expected calibration
        Image outputImage = workspace.getImage("Test_Output_Image");
        assertEquals(expectedImage,outputImage);

    }

    @Test
    public void testRunImageSequenceInterval2LimitFrames() throws Exception {
        // Getting path to image file
        String pathToImage = URLDecoder.decode(this.getClass().getResource("/images/ImageSequence/Seq0000.tif").getPath(),"UTF-8");

        // Initialising a blank workspace
        WorkspaceCollection workspaces = new WorkspaceCollection();
        Workspace workspace = workspaces.getNewWorkspace(new File(pathToImage),1);

        // Initialising the ImageFileLoader
        ImageLoader imageFileLoader = new ImageLoader(new ModuleCollection());
        imageFileLoader.initialiseParameters();

        // Setting parameters
        imageFileLoader.updateParameterValue(ImageLoader.IMPORT_MODE, ImageLoader.ImportModes.IMAGE_SEQUENCE);
        imageFileLoader.updateParameterValue(ImageLoader.SEQUENCE_ROOT_NAME, new File(pathToImage).getParent()+MIA.getSlashes()+"SeqZ{0000}.tif");
        imageFileLoader.updateParameterValue(ImageLoader.OUTPUT_IMAGE, "Test_Output_Image");
        imageFileLoader.updateParameterValue(ImageLoader.FRAMES,"4-8-2");
        imageFileLoader.updateParameterValue(ImageLoader.CROP_MODE,ImageLoader.CropModes.NONE);

        // Running module
        imageFileLoader.execute(workspace);

        // Getting expected image
        pathToImage = URLDecoder.decode(this.getClass().getResource("/images/ImageSequence/Seq4-8-2.tif").getPath(),"UTF-8");
        Image expectedImage = new Image("Expected", IJ.openImage(pathToImage));

        // Checking there is one image in the workspace
        assertEquals(1,workspace.getImages().size());

        // Checking the output image has the expected calibration
        Image outputImage = workspace.getImage("Test_Output_Image");
        assertEquals(expectedImage,outputImage);

    }
}