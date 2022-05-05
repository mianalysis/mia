package io.github.mianalysis.mia.module.objects.process;

import java.awt.geom.Area;
import java.util.ArrayList;

import org.scijava.Priority;
import org.scijava.plugin.Plugin;

import io.github.mianalysis.mia.MIA;
import io.github.mianalysis.mia.module.Categories;
import io.github.mianalysis.mia.module.Category;
import io.github.mianalysis.mia.module.IL2Support;
import io.github.mianalysis.mia.module.Module;
import io.github.mianalysis.mia.module.Modules;
import io.github.mianalysis.mia.object.Obj;
import io.github.mianalysis.mia.object.Objs;
import io.github.mianalysis.mia.object.Workspace;
import io.github.mianalysis.mia.object.parameters.InputObjectsP;
import io.github.mianalysis.mia.object.parameters.Parameters;
import io.github.mianalysis.mia.object.parameters.SeparatorP;
import io.github.mianalysis.mia.object.parameters.objects.OutputObjectsP;
import io.github.mianalysis.mia.object.parameters.text.IntegerP;
import io.github.mianalysis.mia.object.refs.collections.ImageMeasurementRefs;
import io.github.mianalysis.mia.object.refs.collections.MetadataRefs;
import io.github.mianalysis.mia.object.refs.collections.ObjMeasurementRefs;
import io.github.mianalysis.mia.object.refs.collections.ParentChildRefs;
import io.github.mianalysis.mia.object.refs.collections.PartnerRefs;
import io.github.mianalysis.mia.object.system.Preferences;
import io.github.mianalysis.mia.object.system.Status;
import io.github.sjcross.sjcommon.imagej.LUTs;
import io.github.sjcross.sjcommon.object.volume.PointOutOfRangeException;
import io.github.sjcross.sjcommon.object.volume.VolumeType;
import signalprocesser.voronoi.VPoint;
import signalprocesser.voronoi.VoronoiAlgorithm;
import signalprocesser.voronoi.representation.AbstractRepresentation;
import signalprocesser.voronoi.representation.RepresentationFactory;
import signalprocesser.voronoi.representation.triangulation.TriangulationRepresentation;
import signalprocesser.voronoi.representation.triangulation.TriangulationRepresentation.CalcCutOff;
import signalprocesser.voronoi.shapegeneration.ShapeGeneration;

@Plugin(type = Module.class, priority = Priority.LOW, visible = true)
public class FitConcaveHull2D extends Module {
    public static final String INPUT_SEPARATOR = "Object input/output";
    public static final String INPUT_OBJECTS = "Input objects";
    public static final String OUTPUT_OBJECTS = "Output objects";

    public static final String HULL_SEPARATOR = "Concave hull controls";
    public static final String RANGE_PX = "Range (px)";

    public Obj processObject(Obj inputObject, Objs outputObjects, int range) {
        ArrayList<VPoint> points = new ArrayList<VPoint>();
        points = RepresentationFactory.convertPointsToSimpleTriangulationPoints(points);
        AbstractRepresentation representation = RepresentationFactory.createTriangulationRepresentation();
        TriangulationRepresentation triangularrep = (TriangulationRepresentation) representation;
        triangularrep.beginAlgorithm(points);
        CalcCutOff calccutoff = new CalcCutOff() {
            public int calculateCutOff(TriangulationRepresentation rep) {
                return range;
            }
        };
        triangularrep.setCalcCutOff(calccutoff);
        for (java.awt.Point point : inputObject.getRoi(0).getContainedPoints())
            points.add(representation.createPoint((int) Math.round(point.x), (int) Math.round(point.y)));

        // TestRepresentationWrapper representationwrapper.innerrepresentation =
        // representation;
        VoronoiAlgorithm.generateVoronoi(triangularrep, points);

        ArrayList<VPoint> outterpoints = triangularrep.getPointsFormingOutterBoundary();
        Area shape = ShapeGeneration.createArea(outterpoints);

        // We have to explicitly define this, as the number of slices is 1 (potentially
        // unlike the input object)
        Obj outputObject = outputObjects.createAndAddNewObject(VolumeType.QUADTREE);
        try {
            outputObject.addPointsFromShape(shape, 0);
        } catch (PointOutOfRangeException e) {
        }

        outputObject.setT(inputObject.getT());

        outputObject.addParent(inputObject);
        inputObject.addChild(outputObject);

        return outputObject;

    }

    public FitConcaveHull2D(Modules modules) {
        super("Fit concave hull 2D", modules);
        il2Support = IL2Support.FULL;
    }

    @Override
    public String getDescription() {
        return "Fits a 2D concave hull to all objects in a collection.  Each input object will be fit with a single concave hull.  Even for non-contiguous input objects, a single concave hull will be created.  The smoothness of the output hull is controlled by the \"range\" parameter, with smaller range values more closely following the surface of the object.  Larger range values should be used to overcome gaps in object edges.<br><br>"

                + "Generated concave hulls are set as children of their respective input object.  If objects are in 3D, a Z-projection of the object is used.<br><br>"

                + "The implementation used in this module (\"chi-shapes\") is entirely from the \"Concave hulls\" library by Glenn Hudson and Matt Duckham (<a href=\"https://archive.md/l3Un5#selection-571.0-587.218\">link</a>).  A paper with full details of the characteristic hulls algorithm is published in Pattern Recognition:<br><br>"

                + "Duckham, M., Kulik, L., Worboys, M.F., Galton, A. (2008) \"Efficient generation of simple polygons for characterizing the shape of a set of points in the plane\", <i>Pattern Recognition</i>, <b>41</b>, 3224-3236 (<a href=\"https://archive.md/o/l3Un5/www.geosensor.net/papers/duckham08.PR.pdf\">PDF</a>, <a href=\"https://archive.md/o/l3Un5/dx.doi.org/10.1016/j.patcog.2008.03.023\">DOI</a>).";
    }

    @Override
    public Category getCategory() {
        return Categories.OBJECTS_PROCESS;
    }

    @Override
    protected Status process(Workspace workspace) {
        // Getting input objects
        String inputObjectsName = parameters.getValue(INPUT_OBJECTS);
        Objs inputObjects = workspace.getObjectSet(inputObjectsName);

        // Getting parameters
        String outputObjectsName = parameters.getValue(OUTPUT_OBJECTS);
        int range = parameters.getValue(RANGE_PX);

        // If necessary, creating a new Objs and adding it to the Workspace
        Objs outputObjects = new Objs(outputObjectsName, inputObjects);
        workspace.addObjects(outputObjects);

        for (Obj inputObject : inputObjects.values())
            processObject(inputObject, outputObjects, range);

        if (showOutput)
            outputObjects.convertToImageRandomColours().showImage(LUTs.Random(true));

        return Status.PASS;

    }

    @Override
    protected void initialiseParameters() {
        parameters.add(new SeparatorP(INPUT_SEPARATOR, this));
        parameters.add(new InputObjectsP(INPUT_OBJECTS, this));
        parameters.add(new OutputObjectsP(OUTPUT_OBJECTS, this));

        parameters.add(new SeparatorP(HULL_SEPARATOR, this));
        parameters.add(new IntegerP(RANGE_PX, this, 50));

        addParameterDescriptions();

    }

    @Override
    public Parameters updateAndGetParameters() {
        Parameters returnedParameters = new Parameters();

        returnedParameters.add(parameters.getParameter(INPUT_SEPARATOR));
        returnedParameters.add(parameters.getParameter(INPUT_OBJECTS));
        returnedParameters.add(parameters.getParameter(OUTPUT_OBJECTS));

        returnedParameters.add(parameters.getParameter(HULL_SEPARATOR));
        returnedParameters.add(parameters.getParameter(RANGE_PX));

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

        String inputObjectsName = parameters.getValue(INPUT_OBJECTS);
        String outputObjectsName = parameters.getValue(OUTPUT_OBJECTS);
        returnedRelationships.add(parentChildRefs.getOrPut(inputObjectsName, outputObjectsName));

        return returnedRelationships;

    }

    @Override
    public PartnerRefs updateAndGetPartnerRefs() {
        return null;
    }

    @Override
    public boolean verify() {
        String storageMode = MIA.preferences.getDataStorageMode();
        if (storageMode.equals(Preferences.DataStorageModes.STREAM_FROM_DRIVE) & il2Support.equals(IL2Support.NONE))
            return false;

        return true;
    }

    void addParameterDescriptions() {
        parameters.get(INPUT_OBJECTS).setDescription(
                "Input objects to create 2D concave hulls for.  Each convex hull will be a child of its respective input object.");

        parameters.get(OUTPUT_OBJECTS).setDescription(
                "Output concave hull objects will be stored in the workspace with this name.  Each concave hull object will be a child of the input object it was created from.");

        parameters.get(RANGE_PX).setDescription("The maximum gap in the surface (edge) of an object that the hull can smooth over.  For gaps larger than this the hull will follow the discontinuity.");

    }
}
