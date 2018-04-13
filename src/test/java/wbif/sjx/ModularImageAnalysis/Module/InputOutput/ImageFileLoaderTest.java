package wbif.sjx.ModularImageAnalysis.Module.InputOutput;

import org.junit.Test;
import wbif.sjx.ModularImageAnalysis.Module.Module;
import wbif.sjx.ModularImageAnalysis.Object.Image;
import wbif.sjx.ModularImageAnalysis.Object.Workspace;

import java.io.File;
import java.net.URLDecoder;

import static org.junit.Assert.*;

/**
 * Created by steph on 29/08/2017.
 */
public class ImageFileLoaderTest {
    @Test
    public void testGetTitle() throws Exception {
        assertNotNull(new ImageLoader().getTitle());

    }

    @Test
    public void testRunWithSpecificTiffFile() throws Exception {
        // Initialising a blank workspace
        Workspace workspace = new Workspace(0,null,1);

        // Initialising ImageFileLoader
        ImageLoader imageFileLoader = new ImageLoader();
        imageFileLoader.initialiseParameters();

        // Setting parameters
        imageFileLoader.updateParameterValue(ImageLoader.IMPORT_MODE, ImageLoader.ImportModes.SPECIFIC_FILE);
        String pathToImage = URLDecoder.decode(this.getClass().getResource("/images/BlankHyperstack5D_8bit.tif").getPath(),"UTF-8");
        imageFileLoader.updateParameterValue(ImageLoader.FILE_PATH,pathToImage);
        imageFileLoader.updateParameterValue(ImageLoader.OUTPUT_IMAGE,"Test_Output_Image");
        imageFileLoader.updateParameterValue(ImageLoader.USE_ALL_C,true);
        imageFileLoader.updateParameterValue(ImageLoader.USE_ALL_Z,true);
        imageFileLoader.updateParameterValue(ImageLoader.USE_ALL_T,true);
        imageFileLoader.updateParameterValue(ImageLoader.SET_CAL,false);
        imageFileLoader.updateParameterValue(ImageLoader.SHOW_IMAGE,false);

        // Running module
        imageFileLoader.run(workspace);

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
        assertEquals(0.02,image.getImagePlus().getCalibration().getX(1),1E-10);
        assertEquals(0.02,image.getImagePlus().getCalibration().getY(1),1E-10);
        assertEquals(0.1,image.getImagePlus().getCalibration().getZ(1),1E-10);

    }

    @Test
    public void testRunWithCurrentTiffFile() throws Exception {
        // Getting path to image file
        String pathToImage = URLDecoder.decode(this.getClass().getResource("/images/BlankHyperstack5D_8bit.tif").getPath(),"UTF-8");

        // Initialising a blank workspace
        Workspace workspace = new Workspace(0,new File(pathToImage),1);

        // Initialising the ImageFileLoader
        ImageLoader imageFileLoader = new ImageLoader();
        imageFileLoader.initialiseParameters();

        // Setting parameters
        imageFileLoader.updateParameterValue(ImageLoader.IMPORT_MODE, ImageLoader.ImportModes.CURRENT_FILE);
        imageFileLoader.updateParameterValue(ImageLoader.OUTPUT_IMAGE,"Test_Output_Image");
        imageFileLoader.updateParameterValue(ImageLoader.USE_ALL_C,true);
        imageFileLoader.updateParameterValue(ImageLoader.USE_ALL_Z,true);
        imageFileLoader.updateParameterValue(ImageLoader.USE_ALL_T,true);
        imageFileLoader.updateParameterValue(ImageLoader.SET_CAL,false);
        imageFileLoader.updateParameterValue(ImageLoader.SHOW_IMAGE,false);

        // Running module
        imageFileLoader.run(workspace);

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
        assertEquals(0.02,image.getImagePlus().getCalibration().getX(1),1E-10);
        assertEquals(0.02,image.getImagePlus().getCalibration().getY(1),1E-10);
        assertEquals(0.1,image.getImagePlus().getCalibration().getZ(1),1E-10);

    }

    @Test
    public void testRunWithCurrentTiffFileBioformats() throws Exception {
        // Getting path to image file
        String pathToImage = URLDecoder.decode(this.getClass().getResource("/images/BlankHyperstack5D_8bit.tif").getPath(),"UTF-8");

        // Initialising a blank workspace
        Workspace workspace = new Workspace(0,new File(pathToImage),1);

        // Initialising the ImageFileLoader
        ImageLoader imageFileLoader = new ImageLoader();
        imageFileLoader.initialiseParameters();

        // Setting parameters
        imageFileLoader.updateParameterValue(ImageLoader.IMPORT_MODE, ImageLoader.ImportModes.CURRENT_FILE);
        imageFileLoader.updateParameterValue(ImageLoader.OUTPUT_IMAGE,"Test_Output_Image");
        imageFileLoader.updateParameterValue(ImageLoader.USE_ALL_C,true);
        imageFileLoader.updateParameterValue(ImageLoader.USE_ALL_Z,true);
        imageFileLoader.updateParameterValue(ImageLoader.USE_ALL_T,true);
        imageFileLoader.updateParameterValue(ImageLoader.SET_CAL,false);
        imageFileLoader.updateParameterValue(ImageLoader.SHOW_IMAGE,false);

        // Running module
        imageFileLoader.run(workspace);

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
        assertEquals(0.02,image.getImagePlus().getCalibration().getX(1),1E-10);
        assertEquals(0.02,image.getImagePlus().getCalibration().getY(1),1E-10);
        assertEquals(0.1,image.getImagePlus().getCalibration().getZ(1),1E-10);

    }

    @Test
    public void testRunWithCurrentTiffSpecifiedCalibration() throws Exception {
        // Getting path to image file
        String pathToImage = URLDecoder.decode(this.getClass().getResource("/images/BlankHyperstack5D_8bit.tif").getPath(),"UTF-8");

        // Initialising a blank workspace
        Workspace workspace = new Workspace(0,new File(pathToImage),1);

        // Initialising the ImageFileLoader
        ImageLoader imageFileLoader = new ImageLoader();
        imageFileLoader.initialiseParameters();

        // Setting parameters
        imageFileLoader.updateParameterValue(ImageLoader.IMPORT_MODE, ImageLoader.ImportModes.CURRENT_FILE);
        imageFileLoader.updateParameterValue(ImageLoader.OUTPUT_IMAGE,"Test_Output_Image");
        imageFileLoader.updateParameterValue(ImageLoader.USE_ALL_C,true);
        imageFileLoader.updateParameterValue(ImageLoader.USE_ALL_Z,true);
        imageFileLoader.updateParameterValue(ImageLoader.USE_ALL_T,true);
        imageFileLoader.updateParameterValue(ImageLoader.SET_CAL,true);
        imageFileLoader.updateParameterValue(ImageLoader.XY_CAL,0.5);
        imageFileLoader.updateParameterValue(ImageLoader.Z_CAL,0.2);
        imageFileLoader.updateParameterValue(ImageLoader.UNITS,"test units");
        imageFileLoader.updateParameterValue(ImageLoader.SHOW_IMAGE,false);

        // Running module
        imageFileLoader.run(workspace);

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
        assertEquals(0.5,image.getImagePlus().getCalibration().getX(1),1E-10);
        assertEquals(0.5,image.getImagePlus().getCalibration().getY(1),1E-10);
        assertEquals(0.2,image.getImagePlus().getCalibration().getZ(1),1E-10);
        assertEquals("test units",image.getImagePlus().getCalibration().getUnits());

    }

    @Test
    public void testRunWithCurrentTiffSubsetC() throws Exception {
        // Getting path to image file
        String pathToImage = URLDecoder.decode(this.getClass().getResource("/images/BlankHyperstack5D_8bit.tif").getPath(),"UTF-8");

        // Initialising a blank workspace
        Workspace workspace = new Workspace(0,new File(pathToImage),1);

        // Initialising the ImageFileLoader
        ImageLoader imageFileLoader = new ImageLoader();
        imageFileLoader.initialiseParameters();

        // Setting parameters
        imageFileLoader.updateParameterValue(ImageLoader.IMPORT_MODE, ImageLoader.ImportModes.CURRENT_FILE);
        imageFileLoader.updateParameterValue(ImageLoader.OUTPUT_IMAGE,"Test_Output_Image");
        imageFileLoader.updateParameterValue(ImageLoader.USE_ALL_C,false);
        imageFileLoader.updateParameterValue(ImageLoader.STARTING_C,2);
        imageFileLoader.updateParameterValue(ImageLoader.ENDING_C,2);
        imageFileLoader.updateParameterValue(ImageLoader.USE_ALL_Z,true);
        imageFileLoader.updateParameterValue(ImageLoader.USE_ALL_T,true);
        imageFileLoader.updateParameterValue(ImageLoader.SET_CAL,false);
        imageFileLoader.updateParameterValue(ImageLoader.SHOW_IMAGE,false);

        // Running module
        imageFileLoader.run(workspace);

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
        assertEquals(0.02,image.getImagePlus().getCalibration().getX(1),1E-10);
        assertEquals(0.02,image.getImagePlus().getCalibration().getY(1),1E-10);
        assertEquals(0.1,image.getImagePlus().getCalibration().getZ(1),1E-10);

    }

    @Test
    public void testRunWithCurrentTiffSubsetZ() throws Exception {
        // Getting path to image file
        String pathToImage = URLDecoder.decode(this.getClass().getResource("/images/BlankHyperstack5D_8bit.tif").getPath(),"UTF-8");

        // Initialising a blank workspace
        Workspace workspace = new Workspace(0,new File(pathToImage),1);

        // Initialising the ImageFileLoader
        ImageLoader imageFileLoader = new ImageLoader();
        imageFileLoader.initialiseParameters();

        // Setting parameters
        imageFileLoader.updateParameterValue(ImageLoader.IMPORT_MODE, ImageLoader.ImportModes.CURRENT_FILE);
        imageFileLoader.updateParameterValue(ImageLoader.OUTPUT_IMAGE,"Test_Output_Image");
        imageFileLoader.updateParameterValue(ImageLoader.USE_ALL_C,true);
        imageFileLoader.updateParameterValue(ImageLoader.USE_ALL_Z,false);
        imageFileLoader.updateParameterValue(ImageLoader.STARTING_Z,3);
        imageFileLoader.updateParameterValue(ImageLoader.ENDING_Z,6);
        imageFileLoader.updateParameterValue(ImageLoader.USE_ALL_T,true);
        imageFileLoader.updateParameterValue(ImageLoader.SET_CAL,false);
        imageFileLoader.updateParameterValue(ImageLoader.SHOW_IMAGE,false);

        // Running module
        imageFileLoader.run(workspace);

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
        assertEquals(0.02,image.getImagePlus().getCalibration().getX(1),1E-10);
        assertEquals(0.02,image.getImagePlus().getCalibration().getY(1),1E-10);
        assertEquals(0.1,image.getImagePlus().getCalibration().getZ(1),1E-10);

    }

    @Test
    public void testRunWithCurrentTiffSubsetT() throws Exception {
        // Getting path to image file
        String pathToImage = URLDecoder.decode(this.getClass().getResource("/images/BlankHyperstack5D_8bit.tif").getPath(),"UTF-8");

        // Initialising a blank workspace
        Workspace workspace = new Workspace(0,new File(pathToImage),1);

        // Initialising the ImageFileLoader
        ImageLoader imageFileLoader = new ImageLoader();
        imageFileLoader.initialiseParameters();

        // Setting parameters
        imageFileLoader.updateParameterValue(ImageLoader.IMPORT_MODE, ImageLoader.ImportModes.CURRENT_FILE);
        imageFileLoader.updateParameterValue(ImageLoader.OUTPUT_IMAGE,"Test_Output_Image");
        imageFileLoader.updateParameterValue(ImageLoader.USE_ALL_C,true);
        imageFileLoader.updateParameterValue(ImageLoader.USE_ALL_Z,true);
        imageFileLoader.updateParameterValue(ImageLoader.USE_ALL_T,false);
        imageFileLoader.updateParameterValue(ImageLoader.STARTING_T,2);
        imageFileLoader.updateParameterValue(ImageLoader.ENDING_T,4);
        imageFileLoader.updateParameterValue(ImageLoader.SET_CAL,false);
        imageFileLoader.updateParameterValue(ImageLoader.SHOW_IMAGE,false);

        // Running module
        imageFileLoader.run(workspace);

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
        assertEquals(0.02,image.getImagePlus().getCalibration().getX(1),1E-10);
        assertEquals(0.02,image.getImagePlus().getCalibration().getY(1),1E-10);
        assertEquals(0.1,image.getImagePlus().getCalibration().getZ(1),1E-10);

    }

    @Test
    public void testRunWithCurrentTiffSubsetAll() throws Exception {
        // Getting path to image file
        String pathToImage = URLDecoder.decode(this.getClass().getResource("/images/BlankHyperstack5D_8bit.tif").getPath(),"UTF-8");

        // Initialising a blank workspace
        Workspace workspace = new Workspace(0,new File(pathToImage),1);

        // Initialising the ImageFileLoader
        ImageLoader imageFileLoader = new ImageLoader();
        imageFileLoader.initialiseParameters();

        // Setting parameters
        imageFileLoader.updateParameterValue(ImageLoader.IMPORT_MODE, ImageLoader.ImportModes.CURRENT_FILE);
        imageFileLoader.updateParameterValue(ImageLoader.OUTPUT_IMAGE,"Test_Output_Image");
        imageFileLoader.updateParameterValue(ImageLoader.USE_ALL_C,false);
        imageFileLoader.updateParameterValue(ImageLoader.STARTING_C,2);
        imageFileLoader.updateParameterValue(ImageLoader.ENDING_C,2);
        imageFileLoader.updateParameterValue(ImageLoader.USE_ALL_Z,false);
        imageFileLoader.updateParameterValue(ImageLoader.STARTING_Z,3);
        imageFileLoader.updateParameterValue(ImageLoader.ENDING_Z,8);
        imageFileLoader.updateParameterValue(ImageLoader.USE_ALL_T,false);
        imageFileLoader.updateParameterValue(ImageLoader.STARTING_T,3);
        imageFileLoader.updateParameterValue(ImageLoader.ENDING_T,4);
        imageFileLoader.updateParameterValue(ImageLoader.SET_CAL,false);
        imageFileLoader.updateParameterValue(ImageLoader.SHOW_IMAGE,false);

        // Running module
        imageFileLoader.run(workspace);

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
        assertEquals(0.02,image.getImagePlus().getCalibration().getX(1),1E-10);
        assertEquals(0.02,image.getImagePlus().getCalibration().getY(1),1E-10);
        assertEquals(0.1,image.getImagePlus().getCalibration().getZ(1),1E-10);

    }

    @Test
    public void testRunWithCurrentLifFile() throws Exception {
        // Getting path to image file
        String pathToImage = URLDecoder.decode(this.getClass().getResource("/images/BlankLif5D_8bit.lif").getPath(),"UTF-8");

        // Initialising a blank workspace
        Workspace workspace = new Workspace(0,new File(pathToImage),1);

        // Initialising the ImageFileLoader
        ImageLoader imageFileLoader = new ImageLoader();
        imageFileLoader.initialiseParameters();

        // Setting parameters
        imageFileLoader.updateParameterValue(ImageLoader.IMPORT_MODE, ImageLoader.ImportModes.CURRENT_FILE);
        imageFileLoader.updateParameterValue(ImageLoader.OUTPUT_IMAGE,"Test_Output_Image");
        imageFileLoader.updateParameterValue(ImageLoader.USE_ALL_C,true);
        imageFileLoader.updateParameterValue(ImageLoader.USE_ALL_Z,true);
        imageFileLoader.updateParameterValue(ImageLoader.USE_ALL_T,true);
        imageFileLoader.updateParameterValue(ImageLoader.SET_CAL,false);
        imageFileLoader.updateParameterValue(ImageLoader.SHOW_IMAGE,false);

        // Running module
        imageFileLoader.run(workspace);

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
        assertEquals(5.55,image.getImagePlus().getCalibration().getX(1),1E-2);
        assertEquals(5.55,image.getImagePlus().getCalibration().getY(1),1E-2);
        assertEquals(2.00,image.getImagePlus().getCalibration().getZ(1),1E-2);

    }

}