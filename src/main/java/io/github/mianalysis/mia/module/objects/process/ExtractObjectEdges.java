package io.github.mianalysis.mia.module.objects.process;

import java.util.HashMap;

import org.scijava.Priority;
import org.scijava.plugin.Plugin;

import ij.IJ;
import ij.ImagePlus;
import ij.plugin.Resizer;
import ij.process.LUT;
import ij.process.StackStatistics;
import inra.ijpb.binary.ChamferWeights3D;
import inra.ijpb.binary.distmap.DistanceTransform3DShort;
import io.github.mianalysis.mia.module.Categories;
import io.github.mianalysis.mia.module.Category;
import io.github.mianalysis.mia.module.Module;
import io.github.mianalysis.mia.module.Modules;
import io.github.mianalysis.mia.module.images.transform.InterpolateZAxis;
import io.github.mianalysis.mia.object.Obj;
import io.github.mianalysis.mia.object.Objs;
import io.github.mianalysis.mia.object.Workspace;
import io.github.mianalysis.mia.object.image.Image;
import io.github.mianalysis.mia.object.image.ImageFactory;
import io.github.mianalysis.mia.object.parameters.BooleanP;
import io.github.mianalysis.mia.object.parameters.ChoiceP;
import io.github.mianalysis.mia.object.parameters.InputObjectsP;
import io.github.mianalysis.mia.object.parameters.Parameters;
import io.github.mianalysis.mia.object.parameters.SeparatorP;
import io.github.mianalysis.mia.object.parameters.objects.OutputObjectsP;
import io.github.mianalysis.mia.object.parameters.text.DoubleP;
import io.github.mianalysis.mia.object.refs.collections.ImageMeasurementRefs;
import io.github.mianalysis.mia.object.refs.collections.MetadataRefs;
import io.github.mianalysis.mia.object.refs.collections.ObjMeasurementRefs;
import io.github.mianalysis.mia.object.refs.collections.ParentChildRefs;
import io.github.mianalysis.mia.object.refs.collections.PartnerRefs;
import io.github.mianalysis.mia.object.system.Status;
import io.github.mianalysis.mia.process.ColourFactory;
import io.github.sjcross.sjcommon.imagej.LUTs;
import io.github.sjcross.sjcommon.object.Point;
import io.github.sjcross.sjcommon.object.volume.PointOutOfRangeException;

/**
 * Created by sc13967 on 01/08/2017.
 */
@Plugin(type = Module.class, priority=Priority.LOW, visible=true)
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
        Obj outputObject = outputObjects.createAndAddNewObject(inputObject.getVolumeType());
        outputObject.setT(inputObject.getT());

        double[][] range = inputObject.getExtents(true,false);

        // Adding pixel intensities to CumStat
        // Running through all pixels in this object and adding the intensity to the MultiCumStat object
        for (Point<Integer> point:inputObject.getCoordinateSet()) {
            distIpl.setPosition(1, (int) (point.getZ() - range[2][0] + 2), 1);
            double pixelVal = distIpl.getProcessor().getPixelValue((int) (point.getX() - range[0][0] + 1), (int) (point.getY() - range[1][0] + 1));

            try {
                switch (mode) {
                    case EDGE:
                        if (pixelVal <= edgeDistance) outputObject.add(point.getX(),point.getY(),point.getZ());
                        break;
                    case INTERIOR:
                        if (pixelVal > edgeDistance) outputObject.add(point.getX(),point.getY(),point.getZ());
                        break;
                }
            } catch (PointOutOfRangeException e) {}
        }

        // Applying relationships
        outputObject.addParent(inputObject);
        inputObject.addChild(outputObject);

        return outputObject;

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

        return ImageFactory.createImage("Distance",distIpl);

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
        return Categories.OBJECTS_PROCESS;
    }

    @Override
    public String getDescription() {
        return "Extracts edge and interior objects for each object in a specified set.  The boundary defining the transition between edges and interiors can be specified either at a fixed distance from the object edge or as a percentage of the maximum edge-centroid distance for each object.  Output edge and interior objects are stored as children of the associated input object.    ";
    }

    @Override
    public Status process(Workspace workspace) {
        // Getting input objects
        String inputObjectsName = parameters.getValue(INPUT_OBJECTS,workspace);
        Objs inputObjects = workspace.getObjects().get(inputObjectsName);

        // Getting parameters
        boolean createEdgeObjects = parameters.getValue(CREATE_EDGE_OBJECTS,workspace);
        boolean createInteriorObjects = parameters.getValue(CREATE_INTERIOR_OBJECTS,workspace);
        String edgeMode = parameters.getValue(EDGE_MODE,workspace);
        double edgeDistance = parameters.getValue(EDGE_DISTANCE,workspace);
        boolean calibratedDistances = parameters.getValue(CALIBRATED_DISTANCES,workspace);
        double edgePercentage = parameters.getValue(EDGE_PERCENTAGE,workspace);

        // Initialising output edge objects
        String edgeObjectName = null;
        Objs edgeObjects = null;
        if (createEdgeObjects) {
            edgeObjectName = parameters.getValue(OUTPUT_EDGE_OBJECTS,workspace);
            edgeObjects = new Objs(edgeObjectName,inputObjects);
            workspace.addObjects(edgeObjects);
        }

        // Initialising output interior objects
        String interiorObjectName = null;
        Objs interiorObjects = null;
        if (createInteriorObjects) {
            interiorObjectName = parameters.getValue(OUTPUT_INTERIOR_OBJECTS,workspace);
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

        addParameterDescriptions();

    }

    @Override
    public Parameters updateAndGetParameters() {
Workspace workspace = null;
        Parameters returnedParameters = new Parameters();
        returnedParameters.add(parameters.getParameter(INPUT_SEPARATOR));
        returnedParameters.add(parameters.getParameter(INPUT_OBJECTS));

        returnedParameters.add(parameters.getParameter(CREATE_EDGE_OBJECTS));
        if ((boolean) parameters.getValue(CREATE_EDGE_OBJECTS,workspace)) {
            returnedParameters.add(parameters.getParameter(OUTPUT_EDGE_OBJECTS));
        }

        returnedParameters.add(parameters.getParameter(CREATE_INTERIOR_OBJECTS));
        if ((boolean) parameters.getValue(CREATE_INTERIOR_OBJECTS,workspace)) {
            returnedParameters.add(parameters.getParameter(OUTPUT_INTERIOR_OBJECTS));
        }

        returnedParameters.add(parameters.getParameter(DISTANCE_SEPARATOR));
        returnedParameters.add(parameters.getParameter(EDGE_MODE));

        if (parameters.getValue(EDGE_MODE,workspace).equals(EdgeModes.DISTANCE_FROM_EDGE)) {
            returnedParameters.add(parameters.getParameter(EDGE_DISTANCE));
            returnedParameters.add(parameters.getParameter(CALIBRATED_DISTANCES));

        } else if (parameters.getValue(EDGE_MODE,workspace).equals(EdgeModes.PERCENTAGE_FROM_EDGE)) {
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
Workspace workspace = null;
        ParentChildRefs returnedRelationships = new ParentChildRefs();

        String inputObjects = parameters.getValue(INPUT_OBJECTS,workspace);

        if ((boolean) parameters.getValue(CREATE_EDGE_OBJECTS,workspace)) {
            String outputEdgeObjects = parameters.getValue(OUTPUT_EDGE_OBJECTS,workspace);
            returnedRelationships.add(parentChildRefs.getOrPut(inputObjects, outputEdgeObjects));
        }

        if ((boolean) parameters.getValue(CREATE_INTERIOR_OBJECTS,workspace)) {
            String outputInteriorObjects = parameters.getValue(OUTPUT_INTERIOR_OBJECTS,workspace);
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

    void addParameterDescriptions() {
        parameters.get(INPUT_OBJECTS).setDescription("Objects from workspace for which edge and/or interior regions will be extracted.  Any extracted regions will be children of the associated input objects.");

        parameters.get(CREATE_EDGE_OBJECTS).setDescription("When selected, an edge object will be created for each input object.  Edge objects contain only coordinates from the input object within a given distance of the object edge.  Output edge objects will be children associated with the relevant input object.  The edge objects will be stored in the workspace with the name specified by \""+OUTPUT_EDGE_OBJECTS+"\".");

        parameters.get(OUTPUT_EDGE_OBJECTS).setDescription("If \""+CREATE_EDGE_OBJECTS+"\" is selected, this is the name assigned to output edge objects.");

        parameters.get(CREATE_INTERIOR_OBJECTS).setDescription("When selected, an interior object will be created for each input object.  Interior objects contain only coordinates from the input object a given distance from the object edge or greater (i.e. they contain any points from the input object which aren't assigned as \"edge\" objects).  Output interior objects will be children associated with the relevant input object.  The interior objects will be stored in the workspace with the name specified by \""+OUTPUT_INTERIOR_OBJECTS+"\".");

        parameters.get(OUTPUT_INTERIOR_OBJECTS).setDescription("If \""+CREATE_INTERIOR_OBJECTS+"\" is selected, this is the name assigned to output interior objects.");

        parameters.get(EDGE_MODE).setDescription("Controls how the boundary between \"edge\" and \"interior\" objects is defined:<br><ul>"
        
                + "<li>\"" + EdgeModes.DISTANCE_FROM_EDGE + "\" The boundary is defined by a fixed distance value specified by \""+EDGE_DISTANCE+"\".  Any input object coordinates within (less than or equal to) this distance of the object edge can be output as \"edge\" coordinates, otherwise they can be output as \"interior\" coordinates.</li>"
        
                +"<li>\""+EdgeModes.PERCENTAGE_FROM_EDGE+"\" The boundary is defined as a percentage of the maximum distance from the edge of the object to its centroid.  As such, this boundary value will vary from object to object in terms of the absolute width of edge objects.</li></ul>");

        parameters.get(EDGE_DISTANCE).setDescription("If \"" + EDGE_MODE + "\" is set to \""
                + EdgeModes.DISTANCE_FROM_EDGE
                + "\", this is the fixed distance value that defines the boundary between edge and interior objects.  It is assumed to be specified in pixel units unless \""+CALIBRATED_DISTANCES+"\" is selected, in which case they are assumed in calibrated units.");

        parameters.get(CALIBRATED_DISTANCES).setDescription("When selected, the fixed boundary distance specified by \""+EDGE_DISTANCE+"\" is assumed to be in calibrated units.  Otherwise, the fixed distance is in pixel units.");

        parameters.get(EDGE_PERCENTAGE).setDescription("If \"" + EDGE_MODE + "\" is set to \""
        + EdgeModes.PERCENTAGE_FROM_EDGE
        + "\", this is the percentage of the maximum centroid-edge distance for an object that will be used to calculate the edge/interior boundary location.  Percentages approaching 0% will put the boundary increasingly close to the object edge (more detected as \"interiors\"), while percentages approaching 100% will have the boundary increasingly close to the object centroid (more detected as \"edges\").");

    }
}
