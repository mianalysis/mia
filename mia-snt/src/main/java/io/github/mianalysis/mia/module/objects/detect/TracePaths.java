package io.github.mianalysis.mia.module.objects.detect;

import java.util.ArrayList;

import org.scijava.Priority;
import org.scijava.plugin.Plugin;
import org.scijava.vecmath.Point3f;

import ij.measure.Calibration;
import ij.process.ImageStatistics;
import io.github.mianalysis.mia.MIA;
import io.github.mianalysis.mia.module.Categories;
import io.github.mianalysis.mia.module.Category;
import io.github.mianalysis.mia.module.Module;
import io.github.mianalysis.mia.module.Modules;
import io.github.mianalysis.mia.module.objects.process.CreateSkeleton;
import io.github.mianalysis.mia.object.Obj;
import io.github.mianalysis.mia.object.Objs;
import io.github.mianalysis.mia.object.Workspace;
import io.github.mianalysis.mia.object.coordinates.Point;
import io.github.mianalysis.mia.object.coordinates.volume.PointOutOfRangeException;
import io.github.mianalysis.mia.object.coordinates.volume.VolumeType;
import io.github.mianalysis.mia.object.image.Image;
import io.github.mianalysis.mia.object.parameters.InputImageP;
import io.github.mianalysis.mia.object.parameters.InputObjectsP;
import io.github.mianalysis.mia.object.parameters.Parameters;
import io.github.mianalysis.mia.object.parameters.SeparatorP;
import io.github.mianalysis.mia.object.parameters.objects.OutputObjectsP;
import io.github.mianalysis.mia.object.parameters.text.IntegerP;
import io.github.mianalysis.mia.object.refs.collections.ImageMeasurementRefs;
import io.github.mianalysis.mia.object.refs.collections.MetadataRefs;
import io.github.mianalysis.mia.object.refs.collections.ObjMeasurementRefs;
import io.github.mianalysis.mia.object.refs.collections.ObjMetadataRefs;
import io.github.mianalysis.mia.object.refs.collections.ParentChildRefs;
import io.github.mianalysis.mia.object.refs.collections.PartnerRefs;
import io.github.mianalysis.mia.object.system.Status;
import net.imglib2.RandomAccessibleInterval;
import sc.fiji.snt.SNT.SearchImageType;
import sc.fiji.snt.tracing.BiSearch;
import sc.fiji.snt.tracing.cost.Cost;
import sc.fiji.snt.tracing.cost.Reciprocal;
import sc.fiji.snt.tracing.heuristic.Euclidean;
import sc.fiji.snt.tracing.heuristic.Heuristic;

/**
 * Created by Stephen Cross on 31/10/2024.
 */
@Plugin(type = Module.class, priority = Priority.LOW, visible = true)
public class TracePaths extends Module {

    /**
    * 
    */
    public static final String INPUT_SEPARATOR = "Input and output";

    /**
     * 
     */
    public static final String INPUT_IMAGE = "Input image";

    /**
     * 
     */
    public static final String INPUT_OBJECTS = "Input objects";

    /**
     * 
     */
    public static final String OUTPUT_OBJECTS = "Output objects";

    public static final String NODE_INTERVAL = "Node interval";

    public TracePaths(Modules modules) {
        super("Trace paths", modules);
    }

    @Override
    public Category getCategory() {
        return Categories.OBJECTS_PROCESS;
    }

    @Override
    public String getVersionNumber() {
        return "1.0.0";
    }

    @Override
    public String getDescription() {
        return "";

    }

    @Override
    public Status process(Workspace workspace) {
        // Getting parameters
        String inputImageName = parameters.getValue(INPUT_IMAGE, workspace);
        String inputObjectsName = parameters.getValue(INPUT_OBJECTS, workspace);
        String outputObjectsName = parameters.getValue(OUTPUT_OBJECTS, workspace);
        int nodeInterval = parameters.getValue(NODE_INTERVAL, workspace);

        Image inputImage = workspace.getImages().get(inputImageName);
        Objs inputObjects = workspace.getObjects(inputObjectsName);
        Objs outputObjects = new Objs(outputObjectsName, inputObjects);

        
        RandomAccessibleInterval rai = inputImage.getImgPlus();
        Calibration calibration = inputImage.getImagePlus().getCalibration();
        ImageStatistics imageStatistics = inputImage.getImagePlus().getStatistics();
        Cost costFunction = new Reciprocal(imageStatistics.min, imageStatistics.max);
        Heuristic heuristic = new Euclidean(calibration);
        
        for (Obj inputObject : inputObjects.values()) {
            ArrayList<Point<Integer>> longestPath = CreateSkeleton.getLargestShortestPath(inputObject);

            Point<Integer> startPt = longestPath.get(0);
            Point<Integer> endPt = longestPath.get(nodeInterval);

            BiSearch biSearch = new BiSearch(rai, calibration, startPt.x, startPt.y, startPt.z, endPt.x, endPt.y,
                    endPt.z, Integer.MAX_VALUE, 1000, SearchImageType.ARRAY, costFunction, heuristic);
            
            biSearch.run();
            Obj outputObject = outputObjects.createAndAddNewObject(VolumeType.POINTLIST);
            for (Point3f point:biSearch.getResult().getPoint3fList()) {
                try {
                    outputObject.add((int) point.x,(int) point.y,(int) point.z);
                } catch (PointOutOfRangeException e) {
                }
            }
        }

        workspace.addObjects(outputObjects);
        
        if (showOutput)
            outputObjects.convertToImageIDColours().show(false);

        return Status.PASS;

    }

    @Override
    protected void initialiseParameters() {
        parameters.add(new SeparatorP(INPUT_SEPARATOR, this));
        parameters.add(new InputImageP(INPUT_IMAGE, this));
        parameters.add(new InputObjectsP(INPUT_OBJECTS, this));
        parameters.add(new OutputObjectsP(OUTPUT_OBJECTS, this));

        parameters.add(new IntegerP(NODE_INTERVAL, this, 10));

    }

    @Override
    public Parameters updateAndGetParameters() {
        return parameters;

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
    public ObjMetadataRefs updateAndGetObjectMetadataRefs() {
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
}
