package io.github.mianalysis.mia.module.objects.detect;

import java.util.ArrayList;

import org.scijava.Priority;
import org.scijava.plugin.Plugin;
import org.scijava.vecmath.Point3f;

import ij.measure.Calibration;
import ij.process.ImageStatistics;
import io.github.mianalysis.mia.module.Categories;
import io.github.mianalysis.mia.module.Category;
import io.github.mianalysis.mia.module.Module;
import io.github.mianalysis.mia.module.Modules;
import io.github.mianalysis.mia.module.images.transform.ExtractSubstack;
import io.github.mianalysis.mia.module.objects.process.CreateSkeleton;
import io.github.mianalysis.mia.object.ObjsFactories;
import io.github.mianalysis.mia.object.ObjsI;
import io.github.mianalysis.mia.object.Workspace;
import io.github.mianalysis.mia.object.WorkspaceI;
import io.github.mianalysis.mia.object.coordinates.ObjI;
import io.github.mianalysis.mia.object.coordinates.Point;
import io.github.mianalysis.mia.object.coordinates.volume.PointListFactory;
import io.github.mianalysis.mia.object.coordinates.volume.PointOutOfRangeException;
import io.github.mianalysis.mia.object.image.ImageI;
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
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;
import sc.fiji.snt.SNT.SearchImageType;
import sc.fiji.snt.tracing.BiSearch;
import sc.fiji.snt.tracing.cost.Cost;
import sc.fiji.snt.tracing.cost.Reciprocal;
import sc.fiji.snt.tracing.heuristic.Dijkstra;
import sc.fiji.snt.tracing.heuristic.Heuristic;

/**
 * Created by Stephen Cross on 31/10/2024.
 */
@Plugin(type = Module.class, priority = Priority.LOW, visible = true)
public class TracePaths<T extends RealType<T> & NativeType<T>> extends Module {

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
    public Status process(WorkspaceI workspace) {
        // Getting parameters
        String inputImageName = parameters.getValue(INPUT_IMAGE, workspace);
        String inputObjectsName = parameters.getValue(INPUT_OBJECTS, workspace);
        String outputObjectsName = parameters.getValue(OUTPUT_OBJECTS, workspace);
        int nodeInterval = parameters.getValue(NODE_INTERVAL, workspace);

        ImageI<T> inputImage = workspace.getImages().get(inputImageName);
        ObjsI inputObjects = workspace.getObjects(inputObjectsName);
        ObjsI outputObjects = ObjsFactories.getDefaultFactory().createFromExampleObjs(outputObjectsName, inputObjects);

        // Calibration calibration = inputImage.getImagePlus().getCalibration();
        Calibration calibration = new Calibration();
        ImageStatistics imageStatistics = inputImage.getImagePlus().getStatistics();
        Cost costFunction = new Reciprocal(imageStatistics.min, imageStatistics.max);
        Heuristic heuristic = new Dijkstra();

        for (ObjI inputObject : inputObjects.values()) {
            RandomAccessibleInterval<T> rai = ExtractSubstack
                    .extractSubstack(inputImage, "Timepoint", "1-end", "1-end", String.valueOf(inputObject.getT() + 1))
                    .getImgPlus();

            ObjI outputObject = outputObjects.createAndAddNewObject(new PointListFactory());

            ArrayList<Point<Integer>> longestPath = CreateSkeleton.getLargestShortestPath(inputObject);

            if (longestPath.size() <= 1) {
                ObjsI tempObjs = ObjsFactories.getDefaultFactory().createFromExampleObjs("Temp", inputObjects);
                ObjI tempObj = inputObject.duplicate(tempObjs, false, false, false);
                while (longestPath.size() <= 1 && tempObj.size() > 0) {
                    tempObj.getCoordinateSet().remove(tempObj.getCoordinateIterator().next());
                    longestPath = CreateSkeleton.getLargestShortestPath(tempObj);
                }
            }

            int nNodes = (int) Math.ceil(longestPath.size() / nodeInterval);
            for (int i = 0; i < nNodes; i++) {
                int startIdx = i * nodeInterval;
                int endIdx = i;
                if (i < nNodes - 1)
                    endIdx = Math.min((i + 1) * nodeInterval, longestPath.size() - 1);

                Point<Integer> startPt = longestPath.get(startIdx);
                Point<Integer> endPt = longestPath.get(endIdx);

                BiSearch biSearch = new BiSearch(rai, calibration, startPt.x, startPt.y, startPt.z, endPt.x, endPt.y,
                        endPt.z, Integer.MAX_VALUE, 1000, SearchImageType.ARRAY, costFunction, heuristic);

                biSearch.run();
                for (Point3f point : biSearch.getResult().getPoint3fList()) {
                    try {
                        outputObject.addCoord((int) point.x, (int) point.y, (int) point.z);
                    } catch (PointOutOfRangeException e) {
                    }
                }
            }

            outputObject.setT(inputObject.getT());
            inputObject.addChild(outputObject);
            outputObject.addParent(inputObject);

        }

        workspace.addObjects(outputObjects);

        if (showOutput)
            outputObjects.convertToImageIDColours().showWithNormalisation(false);

        return Status.PASS;

    }

    @Override
    public void initialiseParameters() {
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
        Workspace workspace = null;
        ParentChildRefs returnedRelationships = new ParentChildRefs();

        String inputObjectsName = parameters.getValue(INPUT_OBJECTS, workspace);
        String outputObjectsName = parameters.getValue(OUTPUT_OBJECTS, workspace);
        returnedRelationships.add(parentChildRefs.getOrPut(inputObjectsName, outputObjectsName));

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
