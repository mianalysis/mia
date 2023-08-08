package io.github.mianalysis.mia.module.images.measure;
// package io.github.mianalysis.MIA.Module.ImageMeasurements;

// import static org.junit.jupiter.api.Assertions.assertEquals;
// import static org.junit.jupiter.api.Assertions.assertNotNull;

// import java.net.URLDecoder;

// import org.junit.jupiter.api.Test;
// import org.junit.jupiter.params.ParameterizedTest;
// import org.junit.jupiter.params.provider.EnumSource;

// import ij.IJ;
// import ij.ImagePlus;
// import net.imglib2.img.Img;
// import net.imglib2.type.logic.BitType;
// import io.github.mianalysis.MIA.ExpectedObjects.ExpectedObjects;
// import io.github.mianalysis.MIA.ExpectedObjects.Objects2D;
// import io.github.mianalysis.MIA.Module.ModuleTest;
// import io.github.mianalysis.MIA.Object.Image;
// import io.github.mianalysis.MIA.Object.Objs;
// import io.github.mianalysis.MIA.Object.Workspace;
// import io.github.mianalysis.MIA.Object.Workspaces;
// import io.github.mianalysis.mia.object.coordinates.volume.VolumeType;

// 
// public class MeasureImageColocalisationTest extends ModuleTest {
//     private double tolerance = 1E-2;

//     @Test
//     public void testGetFullName() {
//         String inputImage2Name = "Im2";
//         String measurement = MeasureImageColocalisation.Measurements.MEAN_PCC;

//         String expected = "COLOCALISATION // Im2_MEAN_PCC";
//         String actual = MeasureImageColocalisation.getFullName(inputImage2Name,measurement);

//         assertEquals(expected,actual);
//     }

//     @ParameterizedTest
//     @EnumSource(VolumeType.class)
//     public void testGetMaskImageInsideObjects(VolumeType volumeType) throws Exception {
//         // Getting the expected objects
//         double dppXY = 0.02;
//         double dppZ = 0.1;
//         String calibratedUnits = "µm";
//         Objs expectedObjects = new Objects2D(volumeType).getObjects("Expected",ExpectedObjects.Mode.EIGHT_BIT,dppXY,dppZ,calibratedUnits,true);

//         // Loading images
//         String pathToImage = URLDecoder.decode(this.getClass().getResource("/images/BinaryObjects/BinaryObjects2D_8bit_whiteBG.zip").getPath(),"UTF-8");
//         ImagePlus expectedIpl = IJ.openImage(pathToImage);
//         Image expectedImage = ImageFactory.createImage("Expected",expectedIpl);

//         Image actual = MeasureImageColocalisation.getObjectMask(expectedObjects,MeasureImageColocalisation.ObjectMaskLogic.MEASURE_INSIDE_OBJECTS);

//         assertEquals(expectedImage,actual);

//     }

//     @ParameterizedTest
//     @EnumSource(VolumeType.class)
//     public void testGetMaskImageOutsideObjects(VolumeType volumeType) throws Exception {
//         // Getting the expected objects
//         double dppXY = 0.02;
//         double dppZ = 0.1;
//         String calibratedUnits = "µm";
//         Objs expectedObjects = new Objects2D(volumeType).getObjects("Expected",ExpectedObjects.Mode.EIGHT_BIT,dppXY,dppZ,calibratedUnits,true);

//         String pathToImage = URLDecoder.decode(this.getClass().getResource("/images/BinaryObjects/BinaryObjects2D_8bit_blackBG.zip").getPath(),"UTF-8");
//         ImagePlus expectedIpl = IJ.openImage(pathToImage);
//         Image expectedImage = ImageFactory.createImage("Expected",expectedIpl);

//         Image actual = MeasureImageColocalisation.getObjectMask(expectedObjects,MeasureImageColocalisation.ObjectMaskLogic.MEASURE_OUTSIDE_OBJECTS);

//         assertEquals(expectedImage,actual);

//     }

//     @Test
//     public void testMeasurePCCWholeImage() throws Exception {
//         // Creating a new workspace
//         Workspaces workspaces = new Workspaces();
//         Workspace workspace = workspaces.getNewWorkspace(null,1);

//         // Loading images
//         String pathToImage = URLDecoder.decode(this.getClass().getResource("/images/MeasureColocalisation/ColocalisationChannel1_2D_8bit.zip").getPath(),"UTF-8");
//         ImagePlus ipl1 = IJ.openImage(pathToImage);
//         Image image1 = ImageFactory.createImage("Im1",ipl1);
//         workspace.addImage(image1);

//         pathToImage = URLDecoder.decode(this.getClass().getResource("/images/MeasureColocalisation/ColocalisationChannel2_2D_8bit.zip").getPath(),"UTF-8");
//         ImagePlus ipl2 = IJ.openImage(pathToImage);
//         Image image2 = ImageFactory.createImage("Im2",ipl2);
//         workspace.addImage(image2);

//         // Running the analysis
//         MeasureImageColocalisation.measurePCC(image1,image2,null,MeasureImageColocalisation.PCCImplementations.CLASSIC);

//         // Calculating PCC (expected value from Coloc2)
//         double expected = 0.44;
//         double actual = image1.getMeasurement(MeasureImageColocalisation.getFullName("Im2",MeasureImageColocalisation.Measurements.MEAN_PCC)).getValue();

//         // Testing returned value
//         assertEquals(expected,actual,tolerance);

//     }

//     @Test
//     public void testMeasurePCCMaskImage() throws Exception {
//         // Creating a new workspace
//         Workspaces workspaces = new Workspaces();
//         Workspace workspace = workspaces.getNewWorkspace(null,1);

//         // Loading images
//         String pathToImage = URLDecoder.decode(this.getClass().getResource("/images/MeasureColocalisation/ColocalisationChannel1_2D_8bit.zip").getPath(),"UTF-8");
//         ImagePlus ipl1 = IJ.openImage(pathToImage);
//         Image image1 = ImageFactory.createImage("Im1",ipl1);
//         workspace.addImage(image1);

//         pathToImage = URLDecoder.decode(this.getClass().getResource("/images/MeasureColocalisation/ColocalisationChannel2_2D_8bit.zip").getPath(),"UTF-8");
//         ImagePlus ipl2 = IJ.openImage(pathToImage);
//         Image image2 = ImageFactory.createImage("Im2",ipl2);
//         workspace.addImage(image2);

//         pathToImage = URLDecoder.decode(this.getClass().getResource("/images/BinaryObjects/BinaryObjects2D_8bit_whiteBG.zip").getPath(),"UTF-8");
//         ImagePlus maskIpl = IJ.openImage(pathToImage);
//         Image maskImage = ImageFactory.createImage("Mask", maskIpl);        
//         workspace.addImage(maskImage);

//         // Running the analysis
//         Img<BitType> maskImg = MeasureImageColocalisation.getBitTypeMask(maskImage);
//         MeasureImageColocalisation.measurePCC(image1,image2,maskImg,MeasureImageColocalisation.PCCImplementations.CLASSIC);

//         // Calculating PCC (expected value from Coloc2)
//         double expected = 0.28;
//         double actual = image1.getMeasurement(MeasureImageColocalisation.getFullName("Im2",MeasureImageColocalisation.Measurements.MEAN_PCC)).getValue();

//         // Testing returned value
//         assertEquals(expected,actual,tolerance);

//     }

//     @Override
//     public void testGetHelp() {
//         assertNotNull(new MeasureImageColocalisation(null).getDescription());
//     }
// }

// //package io.github.mianalysis.MIA.Module.ImageMeasurements;
// //
// //import ij.IJ;
// //import ij.ImagePlus;
// //import net.imglib2.img.Img;
// //import net.imglib2.type.logic.BitType;
// //import org.junit.jupiter.api.Test;
// //import org.junit.jupiter.params.ParameterizedTest;
// //import org.junit.jupiter.params.provider.EnumSource;
// //import io.github.mianalysis.MIA.ExpectedObjects.ExpectedObjects;
// //import io.github.mianalysis.MIA.ExpectedObjects.Objects2D;
// //import io.github.mianalysis.MIA.Module.ModuleTest;
// //import io.github.mianalysis.MIA.Object.Image;
// //import io.github.mianalysis.MIA.Object.Objs;
// //import io.github.mianalysis.MIA.Object.Workspace;
// //import io.github.mianalysis.mia.object.coordinates.volume.VolumeType;
// //
// //import java.net.URLDecoder;
// //
// //import static org.junit.jupiter.api.Assertions.*;
// //
// //
// public class MeasureImageColocalisationTest extends ModuleTest {
// //    private double tolerance = 1E-2;
// //
// //    @Test
// //    public void testGetFullName() {
// //        String inputImage2Name = "Im2";
// //        String measurement = MeasureImageColocalisation.Measurements.MEAN_PCC;
// //
// //        String expected = "COLOCALISATION // Im2_MEAN_PCC";
// //        String actual = MeasureImageColocalisation.getFullName(inputImage2Name,measurement);
// //
// //        assertEquals(expected,actual);
// //    }
// //
// //    @ParameterizedTest
// //    @EnumSource(VolumeType.class)
// //    public void testGetMaskImageNone(VolumeType volumeType) throws Exception {
// //        // Getting the expected objects
// //        double dppXY = 0.02;
// //        double dppZ = 0.1;
// //        String calibratedUnits = "µm";
// //        Objs expectedObjects = new Objects2D(volumeType).getObjects("Expected",ExpectedObjects.Mode.EIGHT_BIT,dppXY,dppZ,calibratedUnits,true);
// //
// //        // Loading images
// //        String pathToImage = URLDecoder.decode(this.getClass().getResource("/images/MeasureColocalisation/ColocalisationChannel1_2D_8bit.zip").getPath(),"UTF-8");
// //        ImagePlus ipl1 = IJ.openImage(pathToImage);
// //        Image image1 = ImageFactory.createImage("Im1",ipl1);
// //
// //        Image actual = MeasureImageColocalisation.getObjectMaskImage(expectedObjects,image1,MeasureImageColocalisation.MaskingModes.NONE);
// //
// //        assertNull(actual);
// //
// //    }
// //
// //    @ParameterizedTest
// //    @EnumSource(VolumeType.class)
// //    public void testGetMaskImageInsideObjects(VolumeType volumeType) throws Exception {
// //        // Getting the expected objects
// //        double dppXY = 0.02;
// //        double dppZ = 0.1;
// //        String calibratedUnits = "µm";
// //        Objs expectedObjects = new Objects2D(volumeType).getObjects("Expected",ExpectedObjects.Mode.EIGHT_BIT,dppXY,dppZ,calibratedUnits,true);
// //
// //        // Loading images
// //        String pathToImage = URLDecoder.decode(this.getClass().getResource("/images/MeasureColocalisation/ColocalisationChannel1_2D_8bit.zip").getPath(),"UTF-8");
// //        ImagePlus ipl1 = IJ.openImage(pathToImage);
// //        Image image1 = ImageFactory.createImage("Im1",ipl1);
// //
// //        pathToImage = URLDecoder.decode(this.getClass().getResource("/images/BinaryObjects/BinaryObjects2D_8bit_whiteBG.zip").getPath(),"UTF-8");
// //        ImagePlus expectedIpl = IJ.openImage(pathToImage);
// //        Image expectedImage = ImageFactory.createImage("Expected",expectedIpl);
// //
// //        Image actual = MeasureImageColocalisation.getObjectMaskImage(expectedObjects,image1,MeasureImageColocalisation.MaskingModes.MEASURE_INSIDE_OBJECTS);
// //
// //        assertEquals(expectedImage,actual);
// //
// //    }
// //
// //    @ParameterizedTest
// //    @EnumSource(VolumeType.class)
// //    public void testGetMaskImageOutsideObjects(VolumeType volumeType) throws Exception {
// //        // Getting the expected objects
// //        double dppXY = 0.02;
// //        double dppZ = 0.1;
// //        String calibratedUnits = "µm";
// //        Objs expectedObjects = new Objects2D(volumeType).getObjects("Expected",ExpectedObjects.Mode.EIGHT_BIT,dppXY,dppZ,calibratedUnits,true);
// //
// //        // Loading images
// //        String pathToImage = URLDecoder.decode(this.getClass().getResource("/images/MeasureColocalisation/ColocalisationChannel1_2D_8bit.zip").getPath(),"UTF-8");
// //        ImagePlus ipl1 = IJ.openImage(pathToImage);
// //        Image image1 = ImageFactory.createImage("Im1",ipl1);
// //
// //        pathToImage = URLDecoder.decode(this.getClass().getResource("/images/BinaryObjects/BinaryObjects2D_8bit_blackBG.zip").getPath(),"UTF-8");
// //        ImagePlus expectedIpl = IJ.openImage(pathToImage);
// //        Image expectedImage = ImageFactory.createImage("Expected",expectedIpl);
// //
// //        Image actual = MeasureImageColocalisation.getObjectMaskImage(expectedObjects,image1,MeasureImageColocalisation.MaskingModes.MEASURE_OUTSIDE_OBJECTS);
// //
// //        assertEquals(expectedImage,actual);
// //
// //    }
// //
// //    @Test
// //    public void testMeasurePCCWholeImage() throws Exception {
// //        // Creating a new workspace
// //        Workspaces workspaces = new Workspaces();
// //        Workspace workspace = workspaces.getNewWorkspace(null,1);
// //
// //        // Loading images
// //        String pathToImage = URLDecoder.decode(this.getClass().getResource("/images/MeasureColocalisation/ColocalisationChannel1_2D_8bit.zip").getPath(),"UTF-8");
// //        ImagePlus ipl1 = IJ.openImage(pathToImage);
// //        Image image1 = ImageFactory.createImage("Im1",ipl1);
// //        workspace.addImage(image1);
// //
// //        pathToImage = URLDecoder.decode(this.getClass().getResource("/images/MeasureColocalisation/ColocalisationChannel2_2D_8bit.zip").getPath(),"UTF-8");
// //        ImagePlus ipl2 = IJ.openImage(pathToImage);
// //        Image image2 = ImageFactory.createImage("Im2",ipl2);
// //        workspace.addImage(image2);
// //
// //        // Running the analysis
// //        MeasureImageColocalisation.measurePCC(image1,image2,null,MeasureImageColocalisation.PCCImplementations.CLASSIC);
// //
// //        // Calculating PCC (expected value from Coloc2)
// //        double expected = 0.44;
// //        double actual = image1.getMeasurement(MeasureImageColocalisation.getFullName("Im2",MeasureImageColocalisation.Measurements.MEAN_PCC)).getValue();
// //
// //        // Testing returned value
// //        assertEquals(expected,actual,tolerance);
// //
// //    }
// //
// //    @Test
// //    public void testMeasurePCCMaskImage() throws Exception {
// //        // Creating a new workspace
// //        Workspaces workspaces = new Workspaces();
// //        Workspace workspace = workspaces.getNewWorkspace(null,1);
// //
// //        // Loading images
// //        String pathToImage = URLDecoder.decode(this.getClass().getResource("/images/MeasureColocalisation/ColocalisationChannel1_2D_8bit.zip").getPath(),"UTF-8");
// //        ImagePlus ipl1 = IJ.openImage(pathToImage);
// //        Image image1 = ImageFactory.createImage("Im1",ipl1);
// //        workspace.addImage(image1);
// //
// //        pathToImage = URLDecoder.decode(this.getClass().getResource("/images/MeasureColocalisation/ColocalisationChannel2_2D_8bit.zip").getPath(),"UTF-8");
// //        ImagePlus ipl2 = IJ.openImage(pathToImage);
// //        Image image2 = ImageFactory.createImage("Im2",ipl2);
// //        workspace.addImage(image2);
// //
// //        pathToImage = URLDecoder.decode(this.getClass().getResource("/images/BinaryObjects/BinaryObjects2D_8bit_whiteBG.zip").getPath(),"UTF-8");
// //        ImagePlus maskIpl = IJ.openImage(pathToImage);
// //        Image maskImage = ImageFactory.createImage("Mask",maskIpl);
// //        workspace.addImage(maskImage);
// //
// //        // Running the analysis
// //        Img<BitType> bitTypeMask = MeasureImageColocalisation.getBitTypeMask(maskImage);
// //        MeasureImageColocalisation.measurePCC(image1,image2,bitTypeMask,MeasureImageColocalisation.PCCImplementations.CLASSIC);
// //
// //        // Calculating PCC (expected value from Coloc2)
// //        double expected = 0.28;
// //        double actual = image1.getMeasurement(MeasureImageColocalisation.getFullName("Im2",MeasureImageColocalisation.Measurements.MEAN_PCC)).getValue();
// //
// //        // Testing returned value
// //        assertEquals(expected,actual,tolerance);
// //
// //    }
// //
// //    @Override
// //    public void testGetHelp() {
// //        assertNotNull(new MeasureImageColocalisation(null).getDescription());
// //    }
// //}