package wbif.sjx.ModularImageAnalysis.Module.ObjectProcessing.Identification;

import ij.ImagePlus;
import ij.gui.Roi;
import ij.measure.Calibration;
import ij.plugin.Duplicator;
import wbif.sjx.ModularImageAnalysis.Module.Module;
import wbif.sjx.ModularImageAnalysis.Module.PackageNames;
import wbif.sjx.ModularImageAnalysis.Module.Visualisation.AddObjectsOverlay;
import wbif.sjx.ModularImageAnalysis.Object.*;
import wbif.sjx.ModularImageAnalysis.Object.Image;
import wbif.sjx.common.Process.ActiveContour.ContourInitialiser;
import wbif.sjx.common.Process.ActiveContour.Energies.BendingEnergy;
import wbif.sjx.common.Process.ActiveContour.Energies.ElasticEnergy;
import wbif.sjx.common.Process.ActiveContour.Energies.EnergyCollection;
import wbif.sjx.common.Process.ActiveContour.Energies.PathEnergy;
import wbif.sjx.common.Process.ActiveContour.Minimisers.GreedyMinimiser;
import wbif.sjx.common.Process.ActiveContour.PhysicalModel.NodeCollection;
import wbif.sjx.common.Process.ActiveContour.Visualisation.GridOverlay;
import wbif.sjx.common.Process.IntensityMinMax;

import java.awt.*;
import java.util.HashMap;
import java.util.Iterator;

/**
 * Created by sc13967 on 16/01/2018.
 */
public class ActiveContourObjectDetection extends Module {
    public static final String INPUT_IMAGE = "Input image";
    public static final String INPUT_OBJECTS = "Input objects";
    public static final String UPDATE_INPUT_OBJECTS = "Update input objects";
    public static final String OUTPUT_OBJECTS = "Output objects";
    public static final String NODE_DENSITY = "Node density";
    public static final String ELASTIC_ENERGY = "Elastic energy contribution";
    public static final String BENDING_ENERGY = "Bending energy contribution";
    public static final String IMAGE_PATH_ENERGY = "Image path energy contribution";
    public static final String SEARCH_RADIUS = "Search radius (px)";
    public static final String NUMBER_OF_ITERATIONS = "Maximum nmber of iterations";
    public static final String SHOW_CONTOURS_REALTIME = "Show contours in realtime";


    @Override
    public String getTitle() {
        return "Active contour-based detection";
    }

    @Override
    public String getPackageName() {
        return PackageNames.OBJECT_PROCESSING_IDENTIFICATION;
    }

    @Override
    public String getHelp() {
        return null;
    }

    @Override
    protected void run(Workspace workspace) {
        // Getting input image
        String inputImageName = parameters.getValue(INPUT_IMAGE);
        Image inputImage = workspace.getImage(inputImageName);
        ImagePlus inputImagePlus = inputImage.getImagePlus();

        // Getting input objects
        String inputObjectsName = parameters.getValue(INPUT_OBJECTS);
        ObjCollection inputObjects = workspace.getObjectSet(inputObjectsName);

        // Getting output image name
        String outputObjectsName = parameters.getValue(OUTPUT_OBJECTS);
        ObjCollection outputObjects = new ObjCollection(outputObjectsName);

        // If there are no input objects, creating an empty collection
        if (inputObjects.getFirst() == null) {
            workspace.addObjects(outputObjects);
            return;
        }

        // Getting parameters
        boolean updateInputObjects = parameters.getValue(UPDATE_INPUT_OBJECTS);
        double nodeDensity = parameters.getValue(NODE_DENSITY);
        double elasticEnergy = parameters.getValue(ELASTIC_ENERGY);
        double bendingEnergy = parameters.getValue(BENDING_ENERGY);
        double pathEnergy = parameters.getValue(IMAGE_PATH_ENERGY);
        int searchRadius = parameters.getValue(SEARCH_RADIUS);
        int maxInteractions = parameters.getValue(NUMBER_OF_ITERATIONS);
        boolean showContoursRealtime = parameters.getValue(SHOW_CONTOURS_REALTIME);

        // Storing the image calibration
        Calibration calibration = inputImagePlus.getCalibration();
        double dppXY = calibration.getX(1);
        double dppZ = calibration.getZ(1);
        String calibrationUnits = calibration.getUnits();

        // Initialising the viewer
        GridOverlay gridOverlay = new GridOverlay();
        gridOverlay.setNodeRadius(2);
        ImagePlus dispIpl = new Duplicator().run(inputImagePlus);
        IntensityMinMax.run(dispIpl,true);
        if (showContoursRealtime) dispIpl.show();

        // Iterating over all objects
        int count = 1;
        int total = inputObjects.size();

        Iterator<Obj> iterator = inputObjects.values().iterator();
        while (iterator.hasNext()) {
            Obj inputObject = iterator.next();
            writeMessage("Processing object " + (count++) + " of " + total);

            // Getting the z-plane of the current object
            int z = inputObject.getPoints().iterator().next().getZ();

            // Getting the Roi for the current object
            Polygon roi = inputObject.getRoi(z).getPolygon();
            int[] xCoords = roi.xpoints;
            int[] yCoords = roi.ypoints;

            // Reducing the number of nodes
            int[] xCoordsSub = new int[(int) Math.floor(xCoords.length*nodeDensity)];
            int[] yCoordsSub = new int[(int) Math.floor(yCoords.length*nodeDensity)];
            for (int i=0;i<xCoordsSub.length;i++) {
                xCoordsSub[i] = xCoords[(int) Math.floor(i/nodeDensity)];
                yCoordsSub[i] = yCoords[(int) Math.floor(i/nodeDensity)];
            }

            // Initialising the contour
            NodeCollection nodes = ContourInitialiser.buildContour(xCoordsSub,yCoordsSub);

            //Assigning energies
            EnergyCollection energies = new EnergyCollection();
            energies.add(new ElasticEnergy(elasticEnergy));
            energies.add(new BendingEnergy(bendingEnergy));
            inputImagePlus.setPosition(1,(int) inputObject.getZ(false,false)[0]+1,inputObject.getT()+1);
            energies.add(new PathEnergy(pathEnergy,inputImagePlus));

            // Initialising the minimiser
            GreedyMinimiser greedy = new GreedyMinimiser(energies);
            greedy.setWidth(searchRadius);
            greedy.setSequence(GreedyMinimiser.RANDOM);

            // Up to the specified maximum number of iterations, updating the contour.  If the contour doesn't move
            // between frames, the loop is terminated.
            for (int i=0;i<maxInteractions;i++) {
                greedy.evaluateGreedy(nodes);
                if (showContoursRealtime) {
                    dispIpl.setPosition(1,(int) inputObject.getZ(false,false)[0]+1,inputObject.getT()+1);
                    gridOverlay.drawOverlay(nodes, dispIpl);
                }

                if (!nodes.anyNodesMoved()) break;
            }

            // Getting the new ROI
            Roi newRoi = nodes.getROI();

            // If the active contour shrank down to nothing the object is removed
            if (newRoi.getContainedPoints().length == 0) {
                iterator.remove();
                continue;
            }

            // If the input objects are to be transformed, taking the new pixel coordinates and applying them to
            // the input object.  Otherwise, the new object is added to the nascent ObjCollection.
            if (updateInputObjects) {
                inputObject.clearPoints();
                inputObject.addPointsFromRoi(newRoi,z);
            } else {
                Obj outputObject = new Obj(outputObjectsName,outputObjects.getNextID(),dppXY,dppZ,calibrationUnits,inputObject.is2D());
                outputObject.setT(inputObject.getT());
                outputObject.addPointsFromRoi(newRoi,z);
                outputObjects.add(outputObject);
            }
        }

        // Resetting the image position
        inputImagePlus.setPosition(1,1,1);

        if (showOutput) {
            // Removing old overlay
            dispIpl.setOverlay(null);
            String positionMode = AddObjectsOverlay.PositionModes.OUTLINE;
            String colourMode = ObjCollection.ColourModes.RANDOM_COLOUR;

            AddObjectsOverlay addObjectsOverlay = ((AddObjectsOverlay) new AddObjectsOverlay()
                    .updateParameterValue(AddObjectsOverlay.POSITION_MODE,positionMode)
                    .updateParameterValue(AddObjectsOverlay.LABEL_SIZE,8));

            if (updateInputObjects) {
                HashMap<Integer,Color> colours = inputObjects.getColours(colourMode,"",true);
                addObjectsOverlay.createOverlay(dispIpl,inputObjects,colours,null);
            } else {
                HashMap<Integer,Color> colours = outputObjects.getColours(colourMode,"",true);
                addObjectsOverlay.createOverlay(dispIpl,outputObjects,colours,null);
            }

            dispIpl.setPosition(1,1,1);
            dispIpl.updateChannelAndDraw();
            dispIpl.show();
        }

        // If selected, adding new ObjCollection to the Workspace
        if (!updateInputObjects) workspace.addObjects(outputObjects);

    }

    @Override
    protected void initialiseParameters() {
        parameters.add(new Parameter(INPUT_IMAGE,Parameter.INPUT_IMAGE,null));
        parameters.add(new Parameter(INPUT_OBJECTS,Parameter.INPUT_OBJECTS,null));
        parameters.add(new Parameter(UPDATE_INPUT_OBJECTS,Parameter.BOOLEAN,true));
        parameters.add(new Parameter(OUTPUT_OBJECTS,Parameter.OUTPUT_OBJECTS,null));
        parameters.add(new Parameter(NODE_DENSITY,Parameter.DOUBLE,1.0));
        parameters.add(new Parameter(ELASTIC_ENERGY,Parameter.DOUBLE,1.0));
        parameters.add(new Parameter(BENDING_ENERGY,Parameter.DOUBLE,1.0));
        parameters.add(new Parameter(IMAGE_PATH_ENERGY,Parameter.DOUBLE,1.0));
        parameters.add(new Parameter(SEARCH_RADIUS,Parameter.INTEGER,1));
        parameters.add(new Parameter(NUMBER_OF_ITERATIONS,Parameter.INTEGER,1000));
        parameters.add(new Parameter(SHOW_CONTOURS_REALTIME,Parameter.BOOLEAN,false));

    }

    @Override
    public ParameterCollection updateAndGetParameters() {
        ParameterCollection returnedParameters = new ParameterCollection();

        returnedParameters.add(parameters.getParameter(INPUT_IMAGE));
        returnedParameters.add(parameters.getParameter(INPUT_OBJECTS));
        returnedParameters.add(parameters.getParameter(UPDATE_INPUT_OBJECTS));

        if (! (boolean) parameters.getValue(UPDATE_INPUT_OBJECTS)) {
            returnedParameters.add(parameters.getParameter(OUTPUT_OBJECTS));
        }

        returnedParameters.add(parameters.getParameter(NODE_DENSITY));
        returnedParameters.add(parameters.getParameter(ELASTIC_ENERGY));
        returnedParameters.add(parameters.getParameter(BENDING_ENERGY));
        returnedParameters.add(parameters.getParameter(IMAGE_PATH_ENERGY));
        returnedParameters.add(parameters.getParameter(SEARCH_RADIUS));
        returnedParameters.add(parameters.getParameter(NUMBER_OF_ITERATIONS));
        returnedParameters.add(parameters.getParameter(SHOW_CONTOURS_REALTIME));

        return returnedParameters;

    }

    @Override
    public MeasurementReferenceCollection updateAndGetImageMeasurementReferences() {
        return null;
    }

    @Override
    public MeasurementReferenceCollection updateAndGetObjectMeasurementReferences() {
        return null;
    }

    @Override
    public MetadataReferenceCollection updateAndGetMetadataReferences() {
        return null;
    }

    @Override
    public void addRelationships(RelationshipCollection relationships) {

    }
}
