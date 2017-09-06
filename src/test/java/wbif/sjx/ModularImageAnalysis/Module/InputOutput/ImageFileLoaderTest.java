package wbif.sjx.ModularImageAnalysis.Module.InputOutput;

import org.junit.Test;
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
    public void testRunWithSpecificTiffFile() throws Exception {
        // Initialising a blank workspace
        Workspace workspace = new Workspace(0,null);

        // Initialising ImageFileLoader
        ImageFileLoader imageFileLoader = new ImageFileLoader();
        imageFileLoader.initialiseParameters();

        // Setting parameters
        imageFileLoader.updateParameterValue(ImageFileLoader.IMPORT_MODE,ImageFileLoader.IMPORT_MODES[1]);
        String pathToImage = URLDecoder.decode(this.getClass().getResource("/images/BlankHyperstack5D_8bit.tif").getPath(),"UTF-8");
        imageFileLoader.updateParameterValue(ImageFileLoader.FILE_PATH,pathToImage);
        imageFileLoader.updateParameterValue(ImageFileLoader.USE_BIOFORMATS,false);
        imageFileLoader.updateParameterValue(ImageFileLoader.OUTPUT_IMAGE,"Test_Output_Image");
        imageFileLoader.updateParameterValue(ImageFileLoader.SHOW_IMAGE,false);
        imageFileLoader.updateParameterValue(ImageFileLoader.FLEX_BUGFIX,false);

        // Running module
        imageFileLoader.run(workspace,false);

        // Checking there is one image in the workspace
        assertEquals(1,workspace.getImages().size());

        // Getting the loaded image
        Image image = workspace.getImage("Test_Output_Image");

        // Checking the image has the expected name
        assertEquals("Test_Output_Image",image.getName());

        // Checking there are no measurements associated with this image
        assertEquals(0,image.getSingleMeasurements().size());

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
        Workspace workspace = new Workspace(0,new File(pathToImage));

        // Initialising the ImageFileLoader
        ImageFileLoader imageFileLoader = new ImageFileLoader();
        imageFileLoader.initialiseParameters();

        // Setting parameters
        imageFileLoader.updateParameterValue(ImageFileLoader.IMPORT_MODE,ImageFileLoader.IMPORT_MODES[0]);
        imageFileLoader.updateParameterValue(ImageFileLoader.USE_BIOFORMATS,false);
        imageFileLoader.updateParameterValue(ImageFileLoader.OUTPUT_IMAGE,"Test_Output_Image");
        imageFileLoader.updateParameterValue(ImageFileLoader.SHOW_IMAGE,false);
        imageFileLoader.updateParameterValue(ImageFileLoader.FLEX_BUGFIX,false);

        // Running module
        imageFileLoader.run(workspace,false);

        // Checking there is one image in the workspace
        assertEquals(1,workspace.getImages().size());

        // Getting the loaded image
        Image image = workspace.getImage("Test_Output_Image");

        // Checking the image has the expected name
        assertEquals("Test_Output_Image",image.getName());

        // Checking there are no measurements associated with this image
        assertEquals(0,image.getSingleMeasurements().size());

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
        Workspace workspace = new Workspace(0,new File(pathToImage));

        // Initialising the ImageFileLoader
        ImageFileLoader imageFileLoader = new ImageFileLoader();
        imageFileLoader.initialiseParameters();

        // Setting parameters
        imageFileLoader.updateParameterValue(ImageFileLoader.IMPORT_MODE,ImageFileLoader.IMPORT_MODES[0]);
        imageFileLoader.updateParameterValue(ImageFileLoader.USE_BIOFORMATS,true);
        imageFileLoader.updateParameterValue(ImageFileLoader.OUTPUT_IMAGE,"Test_Output_Image");
        imageFileLoader.updateParameterValue(ImageFileLoader.SHOW_IMAGE,false);
        imageFileLoader.updateParameterValue(ImageFileLoader.FLEX_BUGFIX,false);

        // Running module
        imageFileLoader.run(workspace,false);

        // Checking there is one image in the workspace
        assertEquals(1,workspace.getImages().size());

        // Getting the loaded image
        Image image = workspace.getImage("Test_Output_Image");

        // Checking the image has the expected name
        assertEquals("Test_Output_Image",image.getName());

        // Checking there are no measurements associated with this image
        assertEquals(0,image.getSingleMeasurements().size());

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
    public void testRunWithCurrentLifFile() throws Exception {
        // Getting path to image file
        String pathToImage = URLDecoder.decode(this.getClass().getResource("/images/BlankLif5D_8bit.lif").getPath(),"UTF-8");

        // Initialising a blank workspace
        Workspace workspace = new Workspace(0,new File(pathToImage));

        // Initialising the ImageFileLoader
        ImageFileLoader imageFileLoader = new ImageFileLoader();
        imageFileLoader.initialiseParameters();

        // Setting parameters
        imageFileLoader.updateParameterValue(ImageFileLoader.IMPORT_MODE,ImageFileLoader.IMPORT_MODES[0]);
        imageFileLoader.updateParameterValue(ImageFileLoader.USE_BIOFORMATS,true);
        imageFileLoader.updateParameterValue(ImageFileLoader.OUTPUT_IMAGE,"Test_Output_Image");
        imageFileLoader.updateParameterValue(ImageFileLoader.SHOW_IMAGE,false);
        imageFileLoader.updateParameterValue(ImageFileLoader.FLEX_BUGFIX,false);

        // Running module
        imageFileLoader.run(workspace,false);

        // Checking there is one image in the workspace
        assertEquals(1,workspace.getImages().size());

        // Getting the loaded image
        Image image = workspace.getImage("Test_Output_Image");

        // Checking the image has the expected name
        assertEquals("Test_Output_Image",image.getName());

        // Checking there are no measurements associated with this image
        assertEquals(0,image.getSingleMeasurements().size());

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