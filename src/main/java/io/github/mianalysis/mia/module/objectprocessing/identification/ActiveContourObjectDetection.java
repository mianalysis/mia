package io.github.mianalysis.mia.module.objectprocessing.identification;

import java.awt.Polygon;
import java.util.Iterator;

import ij.ImagePlus;
import ij.gui.Roi;
import ij.plugin.Duplicator;
import io.github.mianalysis.mia.module.Module;
import io.github.mianalysis.mia.module.Modules;
import io.github.mianalysis.mia.module.Category;
import io.github.mianalysis.mia.module.Categories;
import io.github.mianalysis.mia.object.Status;
import io.github.mianalysis.mia.object.Image;
import io.github.mianalysis.mia.object.Obj;
import io.github.mianalysis.mia.object.Objs;
import io.github.mianalysis.mia.object.Workspace;
import io.github.mianalysis.mia.object.parameters.BooleanP;
import io.github.mianalysis.mia.object.parameters.InputImageP;
import io.github.mianalysis.mia.object.parameters.InputObjectsP;
import io.github.mianalysis.mia.object.parameters.Parameters;
import io.github.mianalysis.mia.object.parameters.objects.OutputObjectsP;
import io.github.mianalysis.mia.object.parameters.text.DoubleP;
import io.github.mianalysis.mia.object.parameters.text.IntegerP;
import io.github.mianalysis.mia.object.refs.collections.ImageMeasurementRefs;
import io.github.mianalysis.mia.object.refs.collections.MetadataRefs;
import io.github.mianalysis.mia.object.refs.collections.ObjMeasurementRefs;
import io.github.mianalysis.mia.object.refs.collections.ParentChildRefs;
import io.github.mianalysis.mia.object.refs.collections.PartnerRefs;
import io.github.sjcross.common.exceptions.IntegerOverflowException;
import io.github.sjcross.common.object.volume.PointOutOfRangeException;
import io.github.sjcross.common.process.IntensityMinMax;
import io.github.sjcross.common.process.activecontour.ContourInitialiser;
import io.github.sjcross.common.process.activecontour.energies.BendingEnergy;
import io.github.sjcross.common.process.activecontour.energies.ElasticEnergy;
import io.github.sjcross.common.process.activecontour.energies.Energy;
import io.github.sjcross.common.process.activecontour.energies.EnergyCollection;
import io.github.sjcross.common.process.activecontour.energies.PathEnergy;
import io.github.sjcross.common.process.activecontour.minimisers.GreedyMinimiser;
import io.github.sjcross.common.process.activecontour.physicalmodel.NodeCollection;
import io.github.sjcross.common.process.activecontour.physicalmodel.Vertex;
import io.github.sjcross.common.process.activecontour.visualisation.GridOverlay;

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
    public static final String BALLOON_ENERGY = "Balloon energy contribution";
    public static final String SEARCH_RADIUS = "Search radius (px)";
    public static final String NUMBER_OF_ITERATIONS = "Maximum number of iterations";
    public static final String USE_MOTION_THRESHOLD = "Use motion threshold";
    public static final String MOTION_THRESHOLD_PX = "Motion threshold (px)";
    public static final String SHOW_CONTOURS_REALTIME = "Show contours in realtime";

    public ActiveContourObjectDetection(Modules modules) {
        super("Active contour-based detection", modules);
    }


    @Override
    public Category getCategory() {
        return Categories.OBJECT_PROCESSING_IDENTIFICATION;
    }

    @Override
    public String getDescription() {
        return "";
    }

    @Override
    public Status process(Workspace workspace) {
        // Getting input image
        String inputImageName = parameters.getValue(INPUT_IMAGE);
        Image inputImage = workspace.getImage(inputImageName);
        ImagePlus inputImagePlus = inputImage.getImagePlus();

        // Getting input objects
        String inputObjectsName = parameters.getValue(INPUT_OBJECTS);
        Objs inputObjects = workspace.getObjectSet(inputObjectsName);

        // Getting output image name
        String outputObjectsName = parameters.getValue(OUTPUT_OBJECTS);
        Objs outputObjects = new Objs(outputObjectsName, inputObjects);

        // If there are no input objects, creating an empty collection
        if (inputObjects.getFirst() == null) {
            workspace.addObjects(outputObjects);
            return Status.PASS;
        }

        // Getting parameters
        boolean updateInputObjects = parameters.getValue(UPDATE_INPUT_OBJECTS);
        double nodeDensity = parameters.getValue(NODE_DENSITY);
        double elasticEnergy = parameters.getValue(ELASTIC_ENERGY);
        double bendingEnergy = parameters.getValue(BENDING_ENERGY);
        double pathEnergy = parameters.getValue(IMAGE_PATH_ENERGY);
        double balloonEnergy = parameters.getValue(BALLOON_ENERGY);
        int searchRadius = parameters.getValue(SEARCH_RADIUS);
        int maxInteractions = parameters.getValue(NUMBER_OF_ITERATIONS);
        boolean useMotionThreshold = parameters.getValue(USE_MOTION_THRESHOLD);
        double motionThreshold = parameters.getValue(MOTION_THRESHOLD_PX);
        boolean showContoursRealtime = parameters.getValue(SHOW_CONTOURS_REALTIME);

        if (!useMotionThreshold)
            motionThreshold = 0;

        // Initialising the viewer
        GridOverlay gridOverlay = new GridOverlay();
        gridOverlay.setNodeRadius(2);
        ImagePlus dispIpl = new Duplicator().run(inputImagePlus);
        IntensityMinMax.run(dispIpl, true);
        if (showContoursRealtime)
            dispIpl.show();

        // Iterating over all objects
        int count = 1;
        int total = inputObjects.size();

        Iterator<Obj> iterator = inputObjects.values().iterator();
        while (iterator.hasNext()) {
            Obj inputObject = iterator.next();
            
            // Getting the z-plane of the current object
            int z = inputObject.getCoordinateSet().iterator().next().getZ();

            // Getting the Roi for the current object
            Polygon roi = inputObject.getRoi(z).getPolygon();
            int[] xCoords = roi.xpoints;
            int[] yCoords = roi.ypoints;

            // Reducing the number of nodes
            int[] xCoordsSub = new int[(int) Math.floor(xCoords.length * nodeDensity)];
            int[] yCoordsSub = new int[(int) Math.floor(yCoords.length * nodeDensity)];
            for (int i = 0; i < xCoordsSub.length; i++) {
                xCoordsSub[i] = xCoords[(int) Math.floor(i / nodeDensity)];
                yCoordsSub[i] = yCoords[(int) Math.floor(i / nodeDensity)];
            }

            // Initialising the contour
            NodeCollection nodes = ContourInitialiser.buildContour(xCoordsSub, yCoordsSub);

            //Assigning energies
            EnergyCollection energies = new EnergyCollection();
            energies.add(new ElasticEnergy(elasticEnergy));
            energies.add(new BendingEnergy(bendingEnergy));
            inputImagePlus.setPosition(1, (int) inputObject.getZ(false, false)[0] + 1, inputObject.getT() + 1);
            energies.add(new PathEnergy(pathEnergy, inputImagePlus));
            energies.add(new BalloonEnergy(balloonEnergy));

            // Initialising the minimiser
            GreedyMinimiser greedy = new GreedyMinimiser(energies);
            greedy.setWidth(searchRadius);
            greedy.setSequence(GreedyMinimiser.RANDOM);

            // Up to the specified maximum number of iterations, updating the contour.  If the contour doesn't move
            // between frames, the loop is terminated.
            for (int i = 0; i < maxInteractions; i++) {
                greedy.evaluateGreedy(nodes);
                if (showContoursRealtime) {
                    dispIpl.setPosition(1, (int) inputObject.getZ(false, false)[0] + 1, inputObject.getT() + 1);
                    gridOverlay.drawOverlay(nodes, dispIpl);
                }

                if (nodes.getAverageDistanceMoved() <= motionThreshold)
                    break;

            }

            // Getting the new ROI
            Roi newRoi = nodes.getROI();

            // If the active contour shrank down to nothing the object is removed
            if (newRoi.getContainedPoints().length == 0) {
                iterator.remove();
                continue;
            }

            // If the input objects are to be transformed, taking the new pixel coordinates and applying them to
            // the input object.  Otherwise, the new object is added to the nascent Objs.
            try {
                if (updateInputObjects) {
                    inputObject.clearAllCoordinates();
                    inputObject.addPointsFromRoi(newRoi, z);
                } else {
                    Obj outputObject = outputObjects.createAndAddNewObject(inputObject.getVolumeType());
                    outputObject.setT(inputObject.getT());
                    outputObject.addPointsFromRoi(newRoi, z);
                }
            } catch (IntegerOverflowException e) {
                return Status.FAIL;
            }

            writeProgressStatus(count++, total, "objects");
            
        }

        // Resetting the image position
        inputImagePlus.setPosition(1, 1, 1);

        if (showOutput) {
            if (updateInputObjects)
                inputObjects.convertToImageRandomColours().showImage();
            else
                outputObjects.convertToImageRandomColours().showImage();
        }

        // If selected, adding new Objs to the Workspace
        if (!updateInputObjects)
            workspace.addObjects(outputObjects);

        return Status.PASS;

    }

    @Override
    protected void initialiseParameters() {
        parameters.add(new InputImageP(INPUT_IMAGE, this));
        parameters.add(new InputObjectsP(INPUT_OBJECTS, this));
        parameters.add(new BooleanP(UPDATE_INPUT_OBJECTS, this, true));
        parameters.add(new OutputObjectsP(OUTPUT_OBJECTS, this));
        parameters.add(new DoubleP(NODE_DENSITY, this, 1.0));
        parameters.add(new DoubleP(ELASTIC_ENERGY, this, 1.0));
        parameters.add(new DoubleP(BENDING_ENERGY, this, 1.0));
        parameters.add(new DoubleP(IMAGE_PATH_ENERGY, this, 1.0));
        parameters.add(new DoubleP(BALLOON_ENERGY, this, 1.0));
        parameters.add(new IntegerP(SEARCH_RADIUS, this, 1));
        parameters.add(new IntegerP(NUMBER_OF_ITERATIONS, this, 1000));
        parameters.add(new BooleanP(USE_MOTION_THRESHOLD, this, true));
        parameters.add(new DoubleP(MOTION_THRESHOLD_PX, this, 0.1d));
        parameters.add(new BooleanP(SHOW_CONTOURS_REALTIME, this, false));

    }

    @Override
    public Parameters updateAndGetParameters() {
        Parameters returnedParameters = new Parameters();

        returnedParameters.add(parameters.getParameter(INPUT_IMAGE));
        returnedParameters.add(parameters.getParameter(INPUT_OBJECTS));
        returnedParameters.add(parameters.getParameter(UPDATE_INPUT_OBJECTS));

        if (!(boolean) parameters.getValue(UPDATE_INPUT_OBJECTS)) {
            returnedParameters.add(parameters.getParameter(OUTPUT_OBJECTS));
        }

        returnedParameters.add(parameters.getParameter(NODE_DENSITY));
        returnedParameters.add(parameters.getParameter(ELASTIC_ENERGY));
        returnedParameters.add(parameters.getParameter(BENDING_ENERGY));
        returnedParameters.add(parameters.getParameter(IMAGE_PATH_ENERGY));
        returnedParameters.add(parameters.getParameter(BALLOON_ENERGY));
        returnedParameters.add(parameters.getParameter(SEARCH_RADIUS));
        returnedParameters.add(parameters.getParameter(NUMBER_OF_ITERATIONS));

        returnedParameters.add(parameters.getParameter(USE_MOTION_THRESHOLD));
        if ((boolean) parameters.getValue(USE_MOTION_THRESHOLD)) {
            returnedParameters.add(parameters.getParameter(MOTION_THRESHOLD_PX));
        }
        returnedParameters.add(parameters.getParameter(SHOW_CONTOURS_REALTIME));
        
        return returnedParameters;

    }

    @Override
    public ImageMeasurementRefs updateAndGetImageMeasurementRefs() {
        return null;
    }

    @Override
    public ObjMeasurementRefs updateAndGetObjectMeasurementRefs() {
        return null;
    }

    @Override
    public MetadataRefs updateAndGetMetadataReferences() {
        return null;
    }

    @Override
    public ParentChildRefs updateAndGetParentChildRefs() {
        return null;
    }

    @Override
    public PartnerRefs updateAndGetPartnerRefs() {
        return null;
    }

    @Override
    public boolean verify() {
        return true;
    }

    public static void main(String[] args) {
        BalloonEnergy balloonEnergy = new BalloonEnergy(1);

        Vertex node1 = new Vertex(50, 20);
        Vertex node2 = new Vertex(53, 18);
        Vertex node3 = new Vertex(54, 14);

        node2.setLeftNeighbour(node1);
        node2.setRightNeighbour(node3);

        balloonEnergy.getEnergy(node2);

    }
}

class BalloonEnergy extends Energy {

    public BalloonEnergy(double weight) {
        super(weight);

    }

    public double getEnergy(Vertex node) {
        double xT = node.getX();
        double yT = node.getY();
        double xL = node.getLeftNeighbour().getX();
        double yL = node.getLeftNeighbour().getY();        
        double xR = node.getRightNeighbour().getX();
        double yR = node.getRightNeighbour().getY();
        double xC = (xR - xL) / 2 + xL;
        double yC = (yR - yL) / 2 + yL;

        double dx = xR-xL;
        double dy = yR-yL;
        double mag = Math.sqrt(dx * dx + dy * dy);
        double nx = -dy / mag;
        double ny = dx / mag;
        
        double shiftX = xT - xC;
        double shiftY = yT - yC;

        double energy = weight * (shiftX * nx + shiftY * ny);

        return energy;

    }
}