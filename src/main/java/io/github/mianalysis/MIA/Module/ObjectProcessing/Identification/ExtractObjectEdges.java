package io.github.mianalysis.MIA.Module.ObjectProcessing.Identification;

import java.util.HashMap;

import ij.IJ;
import ij.ImagePlus;
import ij.plugin.Resizer;
import ij.process.LUT;
import ij.process.StackStatistics;
import inra.ijpb.binary.ChamferWeights3D;
import inra.ijpb.binary.distmap.DistanceTransform3DShort;
import io.github.mianalysis.MIA.Module.Categories;
import io.github.mianalysis.MIA.Module.Category;
import io.github.mianalysis.MIA.Module.Module;
import io.github.mianalysis.MIA.Module.Modules;
import io.github.mianalysis.MIA.Module.ImageProcessing.Stack.InterpolateZAxis;
import io.github.mianalysis.MIA.Object.Image;
import io.github.mianalysis.MIA.Object.Obj;
import io.github.mianalysis.MIA.Object.Objs;
import io.github.mianalysis.MIA.Object.Status;
import io.github.mianalysis.MIA.Object.Workspace;
import io.github.mianalysis.MIA.Object.Parameters.BooleanP;
import io.github.mianalysis.MIA.Object.Parameters.ChoiceP;
import io.github.mianalysis.MIA.Object.Parameters.InputObjectsP;
import io.github.mianalysis.MIA.Object.Parameters.Parameters;
import io.github.mianalysis.MIA.Object.Parameters.SeparatorP;
import io.github.mianalysis.MIA.Object.Parameters.Objects.OutputObjectsP;
import io.github.mianalysis.MIA.Object.Parameters.Text.DoubleP;
import io.github.mianalysis.MIA.Object.Refs.Collections.ImageMeasurementRefs;
import io.github.mianalysis.MIA.Object.Refs.Collections.MetadataRefs;
import io.github.mianalysis.MIA.Object.Refs.Collections.ObjMeasurementRefs;
import io.github.mianalysis.MIA.Object.Refs.Collections.ParentChildRefs;
import io.github.mianalysis.MIA.Object.Refs.Collections.PartnerRefs;
import io.github.mianalysis.MIA.Process.ColourFactory;
import io.github.sjcross.common.ImageJ.LUTs;
import io.github.sjcross.common.Object.Point;
import io.github.sjcross.common.Object.Volume.PointOutOfRangeException;

/**
 * Created by sc13967 on 01/08/2017.
 */
public class ExtractObjectEdges extends Module {
    public static final String INPUT_SEPARATOR = "Object input/output";
    public static final String INPUT_OBJECTS = "Input objects";
    public static final String CREATE_EDGE_OBJECTS = "Create edge objects";
    public static final String OUTPUT_EDGE_OBJECTS = "Output edge objects";
    public static final String CREATE_INTERIOR_OBJECTS = "Create interior objects";
    public static final String OUTPUT_INTERIOR_OBJECTS = "Output interior objects";
    public static final String DISTANCE_SEPARATOR = "Distance controls";
    public static final String EDGE_MODE = "Edge determination";
    public static final String EDGE_DISTANCE = "Distance";
    public static final String CALIBRATED_DISTANCES = "Calibrated distances";
    public static final String EDGE_PERCENTAGE = "Percentage";

    public enum Mode {INTERIOR, EDGE}

    public ExtractObjectEdges(Modules modules) {
        super("Extract object edges",modules);
    }

    public interface EdgeModes {
        String DISTANCE_FROM_EDGE = "Distance to edge";
        String PERCENTAGE_FROM_EDGE = "Percentage of maximum distance to edge";

        String[] ALL = new String[]{DISTANCE_FROM_EDGE, PERCENTAGE_FROM_EDGE};

    }

    public static Obj getRegion(Obj inputObject, Objs outputObjects, Image distImage, double edgeDistance, Mode mode) {
        ImagePlus distIpl = distImage.getImagePlus();

        // Creating new edge object
        Obj edgeObject = outputObjects.createAndAddNewObject(inputObject.getVolumeType());
        edgeObject.setT(inputObject.getT());

        double[][] range = inputObject.getExtents(true,false);

        // Adding pixel intensities to CumStat
        // Running through all pixels in this object and adding the intensity to the MultiCumStat object
        for (Point<Integer> point:inputObject.getCoordinateSet()) {
            distIpl.setPosition(1, (int) (point.getZ() - range[2][0] + 2), 1);
            double pixelVal = distIpl.getProcessor().getPixelValue((int) (point.getX() - range[0][0] + 1), (int) (point.getY() - range[1][0] + 1));

            try {
                switch (mode) {
                    case EDGE:
                        if (pixelVal <= edgeDistance) edgeObject.add(point.getX(),point.getY(),point.getZ());
                        break;
                    case INTERIOR:
                        if (pixelVal > edgeDistance) edgeObject.add(point.getX(),point.getY(),point.getZ());
                        break;
                }
            } catch (PointOutOfRangeException e) {}
        }

        // Applying relationships
        edgeObject.addParent(inputObject);
        inputObject.addChild(edgeObject);

        return edgeObject;

    }

    public static Image getPaddedDistanceMap(Obj inputObject) {
        // Creating a Hyperstack to hold the distance transform.  The image is padded by 1px to ensure the distance
        // map knows where the object edges are.  If the image is a single plane there is no z-padding.
        double[][] range = inputObject.getExtents(true,false);
        int zPad = inputObject.is2D() ? 0 : 1;

        ImagePlus distIpl = IJ.createHyperStack("Objects", (int) (range[0][1] - range[0][0] + 3),
                (int) (range[1][1] - range[1][0] + 3), 1, (int) (range[2][1] - range[2][0] + 1 + 2 * zPad), 1, 8);
        inputObject.getObjectCollection().applyCalibration(distIpl);

        // Setting pixels corresponding to the parent object to 1
        for (Point<Integer> point : inputObject.getCoordinateSet()) {
            distIpl.setPosition(1, (int) (point.getZ() - range[2][0] + 1 + zPad), 1);
            distIpl.getProcessor().set((int) (point.getX() - range[0][0] + 1), (int) (point.getY() - range[1][0] + 1),
                    255);
        }
        
        int nSlices = distIpl.getNSlices();
        if (!inputObject.is2D())
            distIpl = InterpolateZAxis.matchZToXY(distIpl,InterpolateZAxis.InterpolationModes.NONE);

        // Creating distance map using MorphoLibJ
        short[] weights = ChamferWeights3D.BORGEFORS.getShortWeights();
        DistanceTransform3DShort distTransform = new DistanceTransform3DShort(weights, true);
        
        distIpl.setStack(distTransform.distanceMap(distIpl.getStack()));

        if (!inputObject.is2D()) {
            Resizer resizer = new Resizer();
            resizer.setAverageWhenDownsizing(true);
            distIpl = resizer.zScale(distIpl, nSlices, Resizer.IN_PLACE);
        }

        return new Image("Distance",distIpl);

    }

    static double convertEdgePercentageToDistance(Image distImage, double edgePercentage) {
        // If percentage is being used, calculate the current value for edgeDistance
        StackStatistics stackStatistics = new StackStatistics(distImage.getImagePlus());
        double maxDist = stackStatistics.max;

        return  (edgePercentage/100)*maxDist;

    }

    static void showObjects(Objs objects, String parentObjectsName, LUT lut) {
        HashMap<Integer,Float> hues = ColourFactory.getParentIDHues(objects,parentObjectsName,true);
        Image dispImage = objects.convertToImage(objects.getName(),hues,8,false);
        ImagePlus dispIpl = dispImage.getImagePlus();
        dispIpl.setLut(lut);
        dispIpl.setPosition(1,1,1);
        dispIpl.updateChannelAndDraw();
        dispIpl.show();

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
        // Getting input objects
        String inputObjectsName = parameters.getValue(INPUT_OBJECTS);
        Objs inputObjects = workspace.getObjects().get(inputObjectsName);

        // Getting parameters
        boolean createEdgeObjects = parameters.getValue(CREATE_EDGE_OBJECTS);
        boolean createInteriorObjects = parameters.getValue(CREATE_INTERIOR_OBJECTS);
        String edgeMode = parameters.getValue(EDGE_MODE);
        double edgeDistance = parameters.getValue(EDGE_DISTANCE);
        boolean calibratedDistances = parameters.getValue(CALIBRATED_DISTANCES);
        double edgePercentage = parameters.getValue(EDGE_PERCENTAGE);

        // Initialising output edge objects
        String edgeObjectName = null;
        Objs edgeObjects = null;
        if (createEdgeObjects) {
            edgeObjectName = parameters.getValue(OUTPUT_EDGE_OBJECTS);
            edgeObjects = new Objs(edgeObjectName,inputObjects);
            workspace.addObjects(edgeObjects);
        }

        // Initialising output interior objects
        String interiorObjectName = null;
        Objs interiorObjects = null;
        if (createInteriorObjects) {
            interiorObjectName = parameters.getValue(OUTPUT_INTERIOR_OBJECTS);
            interiorObjects = new Objs(interiorObjectName,inputObjects);
            workspace.addObjects(interiorObjects);
        }

        // Checking there are objects to process
        if (inputObjects.size() == 0) return Status.PASS;

        // If necessary, converting calibrated edge distance to pixels
        if (calibratedDistances) edgeDistance = edgeDistance/inputObjects.getDppXY();

        for (Obj inputObject : inputObjects.values()) {
            // Creating distance map
            Image distImage = getPaddedDistanceMap(inputObject);
            if (edgeMode.equals(EdgeModes.PERCENTAGE_FROM_EDGE)) {
                edgeDistance = convertEdgePercentageToDistance(distImage,edgePercentage);
            }

            // Extracting interior and edge objects
            if (createEdgeObjects)
                getRegion(inputObject,edgeObjects,distImage,edgeDistance,Mode.EDGE);
                
            if (createInteriorObjects) 
                getRegion(inputObject,interiorObjects,distImage,edgeDistance,Mode.INTERIOR);
            
        }

        LUT randomLUT = LUTs.Random(true);
        if (showOutput && createEdgeObjects) showObjects(edgeObjects,inputObjectsName,randomLUT);
        if (showOutput && createInteriorObjects) showObjects(interiorObjects,inputObjectsName,randomLUT);

        return Status.PASS;

    }

    @Override
    protected void initialiseParameters() {
        parameters.add(new SeparatorP(INPUT_SEPARATOR, this));
        parameters.add(new InputObjectsP(INPUT_OBJECTS, this));
        parameters.add(new BooleanP(CREATE_EDGE_OBJECTS, this, true));
        parameters.add(new OutputObjectsP(OUTPUT_EDGE_OBJECTS, this));
        parameters.add(new BooleanP(CREATE_INTERIOR_OBJECTS, this, true));
        parameters.add(new OutputObjectsP(OUTPUT_INTERIOR_OBJECTS, this));

        parameters.add(new SeparatorP(DISTANCE_SEPARATOR, this));
        parameters.add(new ChoiceP(EDGE_MODE, this, EdgeModes.DISTANCE_FROM_EDGE, EdgeModes.ALL));
        parameters.add(new DoubleP(EDGE_DISTANCE, this, 1.0));
        parameters.add(new BooleanP(CALIBRATED_DISTANCES,this,false));
        parameters.add(new DoubleP(EDGE_PERCENTAGE, this, 1.0));

    }

    @Override
    public Parameters updateAndGetParameters() {
        Parameters returnedParameters = new Parameters();
        returnedParameters.add(parameters.getParameter(INPUT_SEPARATOR));
        returnedParameters.add(parameters.getParameter(INPUT_OBJECTS));

        returnedParameters.add(parameters.getParameter(CREATE_EDGE_OBJECTS));
        if ((boolean) parameters.getValue(CREATE_EDGE_OBJECTS)) {
            returnedParameters.add(parameters.getParameter(OUTPUT_EDGE_OBJECTS));
        }

        returnedParameters.add(parameters.getParameter(CREATE_INTERIOR_OBJECTS));
        if ((boolean) parameters.getValue(CREATE_INTERIOR_OBJECTS)) {
            returnedParameters.add(parameters.getParameter(OUTPUT_INTERIOR_OBJECTS));
        }

        returnedParameters.add(parameters.getParameter(DISTANCE_SEPARATOR));
        returnedParameters.add(parameters.getParameter(EDGE_MODE));

        if (parameters.getValue(EDGE_MODE).equals(EdgeModes.DISTANCE_FROM_EDGE)) {
            returnedParameters.add(parameters.getParameter(EDGE_DISTANCE));
            returnedParameters.add(parameters.getParameter(CALIBRATED_DISTANCES));

        } else if (parameters.getValue(EDGE_MODE).equals(EdgeModes.PERCENTAGE_FROM_EDGE)) {
            returnedParameters.add(parameters.getParameter(EDGE_PERCENTAGE));

        }

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
        ParentChildRefs returnedRelationships = new ParentChildRefs();

        String inputObjects = parameters.getValue(INPUT_OBJECTS);

        if ((boolean) parameters.getValue(CREATE_EDGE_OBJECTS)) {
            String outputEdgeObjects = parameters.getValue(OUTPUT_EDGE_OBJECTS);
            returnedRelationships.add(parentChildRefs.getOrPut(inputObjects, outputEdgeObjects));
        }

        if ((boolean) parameters.getValue(CREATE_INTERIOR_OBJECTS)) {
            String outputInteriorObjects = parameters.getValue(OUTPUT_INTERIOR_OBJECTS);
            returnedRelationships.add(parentChildRefs.getOrPut(inputObjects,outputInteriorObjects));
        }

        return returnedRelationships;

    }

    @Override
    public PartnerRefs updateAndGetPartnerRefs() {
        return null;
    }

    @Override
    public boolean verify() {
        return true;
    }
}
