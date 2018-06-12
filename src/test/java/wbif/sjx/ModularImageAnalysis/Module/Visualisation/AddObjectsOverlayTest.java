package wbif.sjx.ModularImageAnalysis.Module.Visualisation;

import ij.IJ;
import ij.ImageJ;
import ij.ImagePlus;
import org.junit.Ignore;
import org.junit.Test;
import wbif.sjx.ModularImageAnalysis.ExpectedObjects.ExpectedDenseTracks2D;
import wbif.sjx.ModularImageAnalysis.ExpectedObjects.ExpectedObjects;
import wbif.sjx.ModularImageAnalysis.Module.ImageProcessing.Stack.ConvertStackToTimeseries;
import wbif.sjx.ModularImageAnalysis.Module.Module;
import wbif.sjx.ModularImageAnalysis.Module.ObjectProcessing.Identification.TrackObjects;
import wbif.sjx.ModularImageAnalysis.Object.Image;
import wbif.sjx.ModularImageAnalysis.Object.ObjCollection;
import wbif.sjx.ModularImageAnalysis.Object.Workspace;

import java.awt.*;
import java.net.URLDecoder;
import java.util.HashMap;

import static org.junit.Assert.*;

public class AddObjectsOverlayTest {

    @Test @Ignore
    public void getColours() {
    }

    @Test @Ignore
    public void getLabels() {
    }

    @Test @Ignore
    public void getPositionMeasurements() {
    }

    @Test @Ignore
    public void addAllPointsOverlay() {
    }

    @Test @Ignore
    public void addCentroidOverlay() {
    }

    @Test @Ignore
    public void addOutlineOverlay() {
    }

    @Test @Ignore
    public void addPositionMeasurementsOverlay() {
    }

    @Test @Ignore
    public void testCreateTrackOverlay() throws Exception {

        // THE OBJECTS FOR THIS TEST WILL NEED TO BE RE-CREATED - WE DON'T WANT OBJECTS SPANNING A 512 X 512 IMAGE

        // Creating a new workspace
        Workspace workspace = new Workspace(0,null,1);
        Module.setVerbose(true);

        // Setting object parameters
        String inputObjectsName = "Test_objects";
        String trackObjectsName = "Tracks";
        double dppXY = 0.02;
        double dppZ = 0.1;
        String calibratedUnits = "um";

        // Creating objects and adding to workspace
        ObjCollection testObjects = new ExpectedDenseTracks2D().getObjects(inputObjectsName, ExpectedObjects.Mode.EIGHT_BIT,dppXY,dppZ,calibratedUnits,true);
        workspace.addObjects(testObjects);

        // Tracking objects
        // FOR FINAL TEST DON'T RELY ON ANOTHER MODULE
        TrackObjects trackObjectsModule = (TrackObjects) new TrackObjects()
                .updateParameterValue(TrackObjects.INPUT_OBJECTS,inputObjectsName)
                .updateParameterValue(TrackObjects.TRACK_OBJECTS,trackObjectsName)
                .updateParameterValue(TrackObjects.LINKING_METHOD,TrackObjects.LinkingMethods.CENTROID)
                .updateParameterValue(TrackObjects.MAXIMUM_LINKING_DISTANCE,2d)
                .updateParameterValue(TrackObjects.MAXIMUM_MISSING_FRAMES,0);
        trackObjectsModule.run(workspace);

        // Loading the test image and adding to workspace
        String imageName = "Test_image";
        String pathToImage = URLDecoder.decode(this.getClass().getResource("/images/Tracks.tif").getPath(),"UTF-8");
        ImagePlus ipl = IJ.openImage(pathToImage);
        ConvertStackToTimeseries.process(ipl);
        Image intensityImage = new Image(imageName,ipl);
        workspace.addImage(intensityImage);



        AddObjectsOverlay addObjectsOverlay = (AddObjectsOverlay) new AddObjectsOverlay()
                .updateParameterValue(AddObjectsOverlay.INPUT_OBJECTS,inputObjectsName)
                .updateParameterValue(AddObjectsOverlay.TRACK_OBJECTS,trackObjectsName)
                .updateParameterValue(AddObjectsOverlay.INPUT_IMAGE,imageName)
                .updateParameterValue(AddObjectsOverlay.POSITION_MODE,AddObjectsOverlay.PositionModes.TRACKS)
                .updateParameterValue(AddObjectsOverlay.LINE_WIDTH,1d)
                .updateParameterValue(AddObjectsOverlay.LIMIT_TRACK_HISTORY,false)
                .updateParameterValue(AddObjectsOverlay.SHOW_IMAGE,true);

//        HashMap<Integer, Color> colours = testObjects.getColours(ObjCollection.ColourModes.PARENT_ID,trackObjectsName, false);
//        ObjCollection trackObjects = workspace.getObjectSet(trackObjectsName);
//        addObjectsOverlay.createTrackOverlay(ipl,trackObjects,colours);

        new ImageJ();
        addObjectsOverlay.run(workspace);

        IJ.runMacro("waitForUser");

    }

    @Test @Ignore
    public void addLabelsOverlay() {
    }

    @Test @Ignore
    public void getTitle() {
    }
}