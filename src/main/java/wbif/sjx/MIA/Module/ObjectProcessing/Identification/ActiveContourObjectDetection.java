package wbif.sjx.MIA.Module.ObjectProcessing.Identification;

import ij.ImagePlus;
import ij.gui.Roi;
import ij.measure.Calibration;
import ij.plugin.Duplicator;
import wbif.sjx.MIA.Module.Module;
import wbif.sjx.MIA.Module.PackageNames;
import wbif.sjx.MIA.Module.Deprecated.AddObjectsOverlay;
import wbif.sjx.MIA.Object.Image;
import wbif.sjx.MIA.Object.*;
import wbif.sjx.MIA.Object.Parameters.*;
import wbif.sjx.MIA.Process.ColourFactory;
import wbif.sjx.common.Exceptions.IntegerOverflowException;
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
        return "";
    }

    @Override
    public boolean process(Workspace workspace) {
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
            return true;
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
            try {
                if (updateInputObjects) {
                    inputObject.clearPoints();
                    inputObject.addPointsFromRoi(newRoi,z);
                } else {
                    Obj outputObject = new Obj(outputObjectsName,outputObjects.getNextID(),dppXY,dppZ,calibrationUnits,inputObject.is2D());
                    outputObject.setT(inputObject.getT());
                    outputObject.addPointsFromRoi(newRoi,z);
                    outputObjects.add(outputObject);
                }
            } catch (IntegerOverflowException e) {
                return false;
            }
        }

        // Resetting the image position
        inputImagePlus.setPosition(1,1,1);

        if (showOutput) {
            // Removing old overlay
            dispIpl.setOverlay(null);
            AddObjectsOverlay addObjectsOverlay = new AddObjectsOverlay();
            try {
                if (updateInputObjects) {
                    HashMap<Integer, Float> hues = ColourFactory.getRandomHues(inputObjects);
                    addObjectsOverlay.createOutlineOverlay(dispIpl, inputObjects, hues, false, 0.5, false);
                } else {
                    HashMap<Integer, Float> hues = ColourFactory.getRandomHues(outputObjects);
                    addObjectsOverlay.createOutlineOverlay(dispIpl, outputObjects, hues, false, 0.5, false);
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            dispIpl.setPosition(1,1,1);
            dispIpl.updateChannelAndDraw();
            dispIpl.show();
        }

        // If selected, adding new ObjCollection to the Workspace
        if (!updateInputObjects) workspace.addObjects(outputObjects);

        return true;

    }

    @Override
    protected void initialiseParameters() {
        parameters.add(new InputImageP(INPUT_IMAGE,this));
        parameters.add(new InputObjectsP(INPUT_OBJECTS,this));
        parameters.add(new BooleanP(UPDATE_INPUT_OBJECTS,this,true));
        parameters.add(new OutputObjectsP(OUTPUT_OBJECTS,this));
        parameters.add(new DoubleP(NODE_DENSITY,this,1.0));
        parameters.add(new DoubleP(ELASTIC_ENERGY,this,1.0));
        parameters.add(new DoubleP(BENDING_ENERGY,this,1.0));
        parameters.add(new DoubleP(IMAGE_PATH_ENERGY,this,1.0));
        parameters.add(new IntegerP(SEARCH_RADIUS,this,1));
        parameters.add(new IntegerP(NUMBER_OF_ITERATIONS,this,1000));
        parameters.add(new BooleanP(SHOW_CONTOURS_REALTIME,this,false));

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
    public MeasurementRefCollection updateAndGetImageMeasurementRefs() {
        return null;
    }

    @Override
    public MeasurementRefCollection updateAndGetObjectMeasurementRefs() {
        return null;
    }

    @Override
    public MetadataRefCollection updateAndGetMetadataReferences() {
        return null;
    }

    @Override
    public RelationshipCollection updateAndGetRelationships() {
        return null;
    }

}
