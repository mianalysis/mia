// TODO: Add measurements

package io.github.mianalysis.mia.module.objects.measure.spatial;

import java.util.HashMap;

import org.scijava.Priority;
import org.scijava.plugin.Plugin;

import io.github.mianalysis.mia.module.Categories;
import io.github.mianalysis.mia.module.Category;
import io.github.mianalysis.mia.module.Module;
import io.github.mianalysis.mia.module.Modules;
import io.github.mianalysis.mia.module.images.process.ImageMath;
import io.github.mianalysis.mia.module.images.process.binary.DistanceMap;
import io.github.mianalysis.mia.module.objects.measure.intensity.MeasureObjectIntensity;
import io.github.mianalysis.mia.module.objects.process.CreateSkeleton;
import io.github.mianalysis.mia.module.objects.relate.mergeobjects.MergeRelatedObjects;
import io.github.mianalysis.mia.object.Objs;
import io.github.mianalysis.mia.object.Workspace;
import io.github.mianalysis.mia.object.WorkspaceI;
import io.github.mianalysis.mia.object.coordinates.Obj;
import io.github.mianalysis.mia.object.image.ImageI;
import io.github.mianalysis.mia.object.measurements.Measurement;
import io.github.mianalysis.mia.object.parameters.InputObjectsP;
import io.github.mianalysis.mia.object.parameters.Parameters;
import io.github.mianalysis.mia.object.parameters.SeparatorP;
import io.github.mianalysis.mia.object.refs.ObjMeasurementRef;
import io.github.mianalysis.mia.object.refs.collections.ImageMeasurementRefs;
import io.github.mianalysis.mia.object.refs.collections.MetadataRefs;
import io.github.mianalysis.mia.object.refs.collections.ObjMeasurementRefs;
import io.github.mianalysis.mia.object.refs.collections.ObjMetadataRefs;
import io.github.mianalysis.mia.object.refs.collections.ParentChildRefs;
import io.github.mianalysis.mia.object.refs.collections.PartnerRefs;
import io.github.mianalysis.mia.object.system.Status;
import io.github.mianalysis.mia.process.ColourFactory;
import io.github.mianalysis.mia.process.math.CumStat;
import sc.fiji.analyzeSkeleton.SkeletonResult;

/**
 * Created by sc13967 on 11/05/2017.
 */

/**
* 
*/
@Plugin(type = Module.class, priority = Priority.LOW, visible = true)
public class MeasureObjectWidth extends Module {

	/**
	* 
	*/
    public static final String INPUT_SEPARATOR = "Object input";

	/**
	* 
	*/
    public static final String INPUT_OBJECTS = "Input objects";

    public MeasureObjectWidth(Modules modules) {
        super("Measure object width", modules);
    }

    public interface Measurements {
        String MEAN_WIDTH_PX = "WIDTH // MEAN_(PX)";
        String MIN_WIDTH_PX = "WIDTH // MIN_(PX)";
        String MAX_WIDTH_PX = "WIDTH // MAX_(PX)";
        String STDEV_WIDTH_PX = "WIDTH // STDEV_(PX)";

        String MEAN_WIDTH_CAL = "WIDTH // MEAN_(${SCAL})";
        String MIN_WIDTH_CAL = "WIDTH // MIN_(${SCAL})";
        String MAX_WIDTH_CAL = "WIDTH // MAX_(${SCAL})";
        String STDEV_WIDTH_CAL = "WIDTH // STDEV_(${SCAL})";

    }

    @Override
    public Category getCategory() {
        return Categories.OBJECTS_MEASURE_SPATIAL;
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
        // Getting current objects
        String inputObjectName = parameters.getValue(INPUT_OBJECTS,workspace);
        Objs inputObjects = workspace.getObjects(inputObjectName);

        HashMap<Integer, Float> hues = ColourFactory.getSingleColourValues(inputObjects, ColourFactory.SingleColours.WHITE);
        ImageI binaryImage = inputObjects.convertToImage("Binary", hues, 8, false);
        ImageI distanceMap = DistanceMap.process(binaryImage, "DistanceMapTemp", true, DistanceMap.WeightModes.WEIGHTS_3_4_5_7, false, false);
        ImageMath.process(distanceMap, ImageMath.CalculationModes.MULTIPLY, 2);
        
        // Getting the centroids of each and saving them to the objects
        for (Obj inputObject : inputObjects.values()) {
            double dppXY = inputObject.getDppXY();

            final Objs skeletonObjects = new Objs("SkeletonTemp", inputObjects);
            final Objs edgeObjects = new Objs("EdgesTemp", inputObjects);
            final Objs junctionObjects = new Objs("JunctionsTemp", inputObjects);

            Object[] result = CreateSkeleton.initialiseAnalyzer(inputObject, 0, false);
            Obj skeleton = CreateSkeleton.createEdgeJunctionObjects(inputObject, (SkeletonResult) result[1],
                    skeletonObjects, edgeObjects, junctionObjects, false);
            MergeRelatedObjects.mergeRelatedObjectsUpdateParent(skeletonObjects, "EdgesTemp",
                    MergeRelatedObjects.MergeModes.MERGE_PARENTS_AND_CHILDREN);
            MergeRelatedObjects.mergeRelatedObjectsUpdateParent(skeletonObjects, "JunctionsTemp",
                    MergeRelatedObjects.MergeModes.MERGE_PARENTS_AND_CHILDREN);

            CumStat cs = MeasureObjectIntensity.measureIntensity(skeleton, distanceMap, false, false);

            inputObject.addMeasurement(new Measurement(Measurements.MEAN_WIDTH_PX,cs.getMean()));
            inputObject.addMeasurement(new Measurement(Measurements.MEAN_WIDTH_CAL,cs.getMean()*dppXY));

            inputObject.addMeasurement(new Measurement(Measurements.MIN_WIDTH_PX,cs.getMin()));
            inputObject.addMeasurement(new Measurement(Measurements.MIN_WIDTH_CAL,cs.getMin()*dppXY));

            inputObject.addMeasurement(new Measurement(Measurements.MAX_WIDTH_PX,cs.getMax()));
            inputObject.addMeasurement(new Measurement(Measurements.MAX_WIDTH_CAL,cs.getMax()*dppXY));

            inputObject.addMeasurement(new Measurement(Measurements.STDEV_WIDTH_PX,cs.getStd()));
            inputObject.addMeasurement(new Measurement(Measurements.STDEV_WIDTH_CAL,cs.getStd()*dppXY));

        }

        if (showOutput)
            inputObjects.showMeasurements(this, modules);

        return Status.PASS;

    }

    @Override
    public void initialiseParameters() {
        parameters.add(new SeparatorP(INPUT_SEPARATOR, this));
        parameters.add(new InputObjectsP(INPUT_OBJECTS, this));

        addParameterDescriptions();

    }

    @Override
    public Parameters updateAndGetParameters() {
WorkspaceI workspace = null;
        return parameters;
    }

    @Override
    public ImageMeasurementRefs updateAndGetImageMeasurementRefs() {
return null;
    }

    @Override
public ObjMeasurementRefs updateAndGetObjectMeasurementRefs() {
WorkspaceI workspace = null;
        ObjMeasurementRefs returnedRefs = new ObjMeasurementRefs();
        String inputObjectsName = parameters.getValue(INPUT_OBJECTS,workspace);

        ObjMeasurementRef reference = objectMeasurementRefs.getOrPut(Measurements.MEAN_WIDTH_PX);
        reference.setObjectsName(inputObjectsName);
        returnedRefs.add(reference);

        reference = objectMeasurementRefs.getOrPut(Measurements.MEAN_WIDTH_CAL);
        reference.setObjectsName(inputObjectsName);
        returnedRefs.add(reference);

        reference = objectMeasurementRefs.getOrPut(Measurements.MIN_WIDTH_PX);
        reference.setObjectsName(inputObjectsName);
        returnedRefs.add(reference);

        reference = objectMeasurementRefs.getOrPut(Measurements.MIN_WIDTH_CAL);
        reference.setObjectsName(inputObjectsName);
        returnedRefs.add(reference);

        reference = objectMeasurementRefs.getOrPut(Measurements.MAX_WIDTH_PX);
        reference.setObjectsName(inputObjectsName);
        returnedRefs.add(reference);

        reference = objectMeasurementRefs.getOrPut(Measurements.MAX_WIDTH_CAL);
        reference.setObjectsName(inputObjectsName);
        returnedRefs.add(reference);

        reference = objectMeasurementRefs.getOrPut(Measurements.STDEV_WIDTH_PX);
        reference.setObjectsName(inputObjectsName);
        returnedRefs.add(reference);

        reference = objectMeasurementRefs.getOrPut(Measurements.STDEV_WIDTH_CAL);
        reference.setObjectsName(inputObjectsName);
        returnedRefs.add(reference);

        return returnedRefs;

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

    void addParameterDescriptions() {

    }
}
