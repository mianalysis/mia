// package io.github.mianalysis.mia.module.objects.detect;

// import java.awt.Polygon;
// import java.awt.event.MouseEvent;
// import java.awt.event.MouseListener;
// import java.io.IOException;
// import java.util.ArrayList;
// import java.util.List;

// import org.scijava.Priority;
// import org.scijava.plugin.Plugin;

// import ai.nets.samj.communication.model.EfficientSAM;
// import ai.nets.samj.install.EfficientSamEnvManager;
// import ai.nets.samj.models.AbstractSamJ;
// import ai.nets.samj.models.EfficientSamJ;
// import ai.nets.samj.ui.SAMJLogger;
// import ij.ImagePlus;
// import ij.gui.PolygonRoi;
// import io.github.mianalysis.mia.MIA;
// import io.github.mianalysis.mia.module.Categories;
// import io.github.mianalysis.mia.module.Category;
// import io.github.mianalysis.mia.module.Module;
// import io.github.mianalysis.mia.module.Modules;
// import io.github.mianalysis.mia.module.visualise.overlays.AddObjectOutline;
// import io.github.mianalysis.mia.module.visualise.overlays.ClearOverlay;
// import io.github.mianalysis.mia.object.Obj;
// import io.github.mianalysis.mia.object.Objs;
// import io.github.mianalysis.mia.object.Workspace;
// import io.github.mianalysis.mia.object.coordinates.volume.PointOutOfRangeException;
// import io.github.mianalysis.mia.object.coordinates.volume.SpatCal;
// import io.github.mianalysis.mia.object.coordinates.volume.VolumeType;
// import io.github.mianalysis.mia.object.image.Image;
// import io.github.mianalysis.mia.object.parameters.InputImageP;
// import io.github.mianalysis.mia.object.parameters.Parameters;
// import io.github.mianalysis.mia.object.parameters.SeparatorP;
// import io.github.mianalysis.mia.object.parameters.objects.OutputObjectsP;
// import io.github.mianalysis.mia.object.refs.collections.ImageMeasurementRefs;
// import io.github.mianalysis.mia.object.refs.collections.MetadataRefs;
// import io.github.mianalysis.mia.object.refs.collections.ObjMeasurementRefs;
// import io.github.mianalysis.mia.object.refs.collections.ObjMetadataRefs;
// import io.github.mianalysis.mia.object.refs.collections.ParentChildRefs;
// import io.github.mianalysis.mia.object.refs.collections.PartnerRefs;
// import io.github.mianalysis.mia.object.system.Status;
// import io.github.mianalysis.mia.object.units.TemporalUnit;
// import io.github.mianalysis.mia.process.ColourFactory;

// /**
//  * Created by sc13967 on 06/06/2017.
//  */

// /**
//  * Creates objects from an input binary image. Each object is identified in 3D
//  * as a contiguous region of foreground labelled pixels. All coordinates
//  * corresponding to that object are stored for use later.<br>
//  * <br>
//  * Note: Input binary images must be 8-bit and only contain values 0 and
//  * 255.<br>
//  * <br>
//  * Note: Uses MorphoLibJ to perform connected components labelling in 3D.
//  */
// @Plugin(type = Module.class, priority = Priority.LOW, visible = true)
// public class SAMJDetection extends Module implements MouseListener {

//     /**
//     * 
//     */
//     public static final String INPUT_SEPARATOR = "Image input, object output";

//     /**
//      * Input binary image from which objects will be identified. This image must be
//      * 8-bit and only contain values 0 and 255.
//      */
//     public static final String INPUT_IMAGE = "Input image";

//     /**
//      * Name of output objects to be stored in workspace.
//      */
//     public static final String OUTPUT_OBJECTS = "Output objects";

//     private EfficientSamJ efficientSamJ;
//     private Objs outputObjects;
//     private ImagePlus inputIpl;

//     public SAMJDetection(Modules modules) {
//         super("SAMJ detection", modules);
//     }

//     @Override
//     public Category getCategory() {
//         return Categories.OBJECTS_DETECT;
//     }

//     @Override
//     public String getVersionNumber() {
//         return "1.0.0";
//     }

//     @Override
//     public String getDescription() {
//         return "";
//     }

//     @Override
//     public Status process(Workspace workspace) {
//         // Getting parameters
//         String inputImageName = parameters.getValue(INPUT_IMAGE, workspace);
//         String outputObjectsName = parameters.getValue(OUTPUT_OBJECTS, workspace);

//         Image inputImage = workspace.getImage(inputImageName);

//         efficientSamJ = null;

//         inputIpl = inputImage.getImagePlus().duplicate();
//         SpatCal spatCal = SpatCal.getFromImage(inputIpl);

//         outputObjects = new Objs(outputObjectsName, spatCal, inputIpl.getNFrames(),
//                 inputIpl.getCalibration().frameInterval, TemporalUnit.getOMEUnit());

//         AbstractSamJ.MAX_ENCODED_AREA_RS = 3000;
//         AbstractSamJ.MAX_ENCODED_SIDE = 3000;

//         inputIpl.show();
//         inputIpl.getCanvas().addMouseListener(this);
        
//         try {
//             EfficientSamEnvManager manager = EfficientSamEnvManager.create("C:\\Users\\sc13967\\Programs\\Fiji.app\\appose_x86_64\\"); 
//             efficientSamJ = EfficientSamJ.initializeSam(manager);
//             efficientSamJ.setImage(inputImage.getImgPlus());

//         } catch (IOException | RuntimeException | InterruptedException e) {
//             e.printStackTrace();
//         }

//         while (inputIpl.isVisible())
//             try {
//                 Thread.sleep(100);
//             } catch (InterruptedException e) {
//                 // Do nothing as the user has selected this
//             }

//         // Adding objects to workspace
//         workspace.addObjects(outputObjects);

//         // Showing objects
//         if (showOutput)
//             outputObjects.convertToImageIDColours().show();

//         return Status.PASS;

//     }

//     @Override
//     protected void initialiseParameters() {
//         parameters.add(new SeparatorP(INPUT_SEPARATOR, this));
//         parameters.add(new InputImageP(INPUT_IMAGE, this));
//         parameters.add(new OutputObjectsP(OUTPUT_OBJECTS, this));

//     }

//     @Override
//     public Parameters updateAndGetParameters() {
//         return parameters;

//     }

//     @Override
//     public ImageMeasurementRefs updateAndGetImageMeasurementRefs() {
//         return null;
//     }

//     @Override
//     public ObjMeasurementRefs updateAndGetObjectMeasurementRefs() {
//         return null;
//     }

//     @Override
//     public ObjMetadataRefs updateAndGetObjectMetadataRefs() {
//         return null;
//     }

//     @Override
//     public MetadataRefs updateAndGetMetadataReferences() {
//         return null;
//     }

//     @Override
//     public ParentChildRefs updateAndGetParentChildRefs() {
//         return null;
//     }

//     @Override
//     public PartnerRefs updateAndGetPartnerRefs() {
//         return null;
//     }

//     @Override
//     public boolean verify() {
//         return true;
//     }

//     @Override
//     public void mouseClicked(MouseEvent e) {
//         if (efficientSamJ == null) {
//             MIA.log.writeWarning("SAMJ not ready yet");
//             return;
//         }

//         int x = (int) Math.round(inputIpl.getRoi().getXBase());
//         int y = (int) Math.round(inputIpl.getRoi().getYBase());
//         ArrayList<int[]> pts = new ArrayList<>();
//         pts.add(new int[]{x,y});

//         try {
//             efficientSamJ.persistEncoding();
//         } catch (IOException | InterruptedException e1) {
//             e1.printStackTrace();
//         }

//         try {
//             List<Polygon> polygons = efficientSamJ.processPoints(pts);
//             for (Polygon polygon : polygons) {
//                 Obj outputObject = outputObjects.createAndAddNewObject(VolumeType.QUADTREE);
//                 outputObject.addPointsFromPolygon(polygon, 0);
//             }
//         } catch (IOException | InterruptedException | RuntimeException | PointOutOfRangeException e1) {
//             e1.printStackTrace();
//         }

//         if (inputIpl.getOverlay() != null)
//             inputIpl.getOverlay().clear();

//         AddObjectOutline.addOverlay(inputIpl,outputObjects,1d,1d,ColourFactory.getColours(ColourFactory.getIDHues(outputObjects, false)),false,true);
                
//     }

//     @Override
//     public void mousePressed(MouseEvent e) {
//     }

//     @Override
//     public void mouseReleased(MouseEvent e) {

//     }

//     @Override
//     public void mouseEntered(MouseEvent e) {
//     }

//     @Override
//     public void mouseExited(MouseEvent e) {
//     }
// }
