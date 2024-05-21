// TODO: Add measurements

package io.github.mianalysis.mia.module.objects.measure.spatial;

import io.github.mianalysis.mia.module.Module;
import io.github.mianalysis.mia.module.Modules;
import io.github.mianalysis.mia.module.Module;
import org.scijava.Priority;
import org.scijava.plugin.Plugin;
import io.github.mianalysis.mia.module.Category;
import io.github.mianalysis.mia.module.Categories;
import io.github.mianalysis.mia.object.*;
import io.github.mianalysis.mia.object.parameters.InputObjectsP;
import io.github.mianalysis.mia.object.parameters.SeparatorP;
import io.github.mianalysis.mia.object.refs.*;
import io.github.mianalysis.mia.object.refs.collections.ImageMeasurementRefs;
import io.github.mianalysis.mia.object.refs.collections.MetadataRefs;
import io.github.mianalysis.mia.object.refs.collections.ObjMeasurementRefs;
import io.github.mianalysis.mia.object.refs.collections.ObjMetadataRefs;
import io.github.mianalysis.mia.object.refs.collections.ParentChildRefs;
import io.github.mianalysis.mia.object.refs.collections.PartnerRefs;
import io.github.mianalysis.mia.object.system.Status;
import io.github.mianalysis.mia.object.parameters.Parameters;
import io.github.mianalysis.mia.object.units.SpatialUnit;

import java.util.ArrayList;

/**
 * Created by sc13967 on 11/05/2017.
 */

/**
* Measure mean XYZ centroid for all objects in an object collection.  <br><br>Note: Z-coordinates are specified in terms of slices (not pixels).
*/
@Plugin(type = Module.class, priority=Priority.LOW, visible=true)
public class MeasureObjectCentroid extends Module {

	/**
	* 
	*/
    public static final String INPUT_SEPARATOR = "Object input";

	/**
	* Objects to measure mean XYZ centroid for.  Measurements will be associated with each object.
	*/
    public static final String INPUT_OBJECTS = "Input objects";

    public MeasureObjectCentroid(Modules modules) {
        super("Measure object centroid",modules);
    }

    public interface Measurements {
        String MEAN_X_PX = "CENTROID // MEAN_X_(PX)";
        String MEAN_Y_PX = "CENTROID // MEAN_Y_(PX)";
        String MEAN_Z_SLICE = "CENTROID // MEAN_Z_(SLICE)";
        String MEAN_X_CAL = "CENTROID // MEAN_X_(${SCAL})";
        String MEAN_Y_CAL = "CENTROID // MEAN_Y_(${SCAL})";
        String MEAN_Z_CAL = "CENTROID // MEAN_Z_(${SCAL})";

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
        return "Measure mean XYZ centroid for all objects in an object colleciton.  <br>br>Note: Z-coordinates are specified in terms of slices (not pixels).";
    }

    @Override
    public Status process(Workspace workspace) {
        // Getting current objects
        String inputObjectName = parameters.getValue(INPUT_OBJECTS,workspace);
        Objs inputObjects = workspace.getObjects().get(inputObjectName);

        // Getting the centroids of each and saving them to the objects
        for (Obj object:inputObjects.values()) {
            ArrayList<Integer> x = object.getXCoords();
            ArrayList<Integer> y = object.getYCoords();
            ArrayList<Integer> z = object.getZCoords();

            if (x != null) {
                object.addMeasurement(new Measurement(Measurements.MEAN_X_PX,object.getXMean(true)));
                object.addMeasurement(new Measurement(Measurements.MEAN_X_CAL,object.getXMean(false)));
            }
            if (y!= null) {
                object.addMeasurement(new Measurement(Measurements.MEAN_Y_PX,object.getYMean(true)));
                object.addMeasurement(new Measurement(Measurements.MEAN_Y_CAL,object.getYMean(false)));
            }
            if (z!= null) {
                object.addMeasurement(new Measurement(Measurements.MEAN_Z_SLICE,object.getZMean(true,false)));
                object.addMeasurement(new Measurement(Measurements.MEAN_Z_CAL,object.getZMean(false,false)));
            }
        }

        if (showOutput) inputObjects.showMeasurements(this,modules);

        return Status.PASS;

    }

    @Override
    protected void initialiseParameters() {
        parameters.add(new SeparatorP(INPUT_SEPARATOR,this));
        parameters.add(new InputObjectsP(INPUT_OBJECTS, this));

        addParameterDescriptions();

    }

    @Override
    public Parameters updateAndGetParameters() {
Workspace workspace = null;
        return parameters;
    }

    @Override
    public ImageMeasurementRefs updateAndGetImageMeasurementRefs() {
return null;
    }

    @Override
public ObjMeasurementRefs updateAndGetObjectMeasurementRefs() {
Workspace workspace = null;
        ObjMeasurementRefs returnedRefs = new ObjMeasurementRefs();
        String inputObjectsName = parameters.getValue(INPUT_OBJECTS,workspace);

        ObjMeasurementRef reference = objectMeasurementRefs.getOrPut(Measurements.MEAN_X_PX);
        reference.setObjectsName(inputObjectsName);
        reference.setDescription("Mean x-position of all pixels in the object, \""+inputObjectsName+"\"." +
                "  Measured in pixel units.");
        returnedRefs.add(reference);

        reference = objectMeasurementRefs.getOrPut(Measurements.MEAN_Y_PX);
        reference.setObjectsName(inputObjectsName);
        reference.setDescription("Mean y-position of all pixels in the object, \""+inputObjectsName+"\"." +
                "  Measured in pixel units.");
        returnedRefs.add(reference);

        reference = objectMeasurementRefs.getOrPut(Measurements.MEAN_Z_SLICE);
        reference.setObjectsName(inputObjectsName);
        reference.setDescription("Mean z-position of all pixels in the object, \""+inputObjectsName+"\"." +
                "  Measured in slice units.");
        returnedRefs.add(reference);

        reference = objectMeasurementRefs.getOrPut(Measurements.MEAN_X_CAL);
        reference.setObjectsName(inputObjectsName);
        reference.setDescription("Mean x-position of all pixels in the object, \""+inputObjectsName+"\"." +
                "  Measured in calibrated ("+SpatialUnit.getOMEUnit().getSymbol()+") units.");
        returnedRefs.add(reference);

        reference = objectMeasurementRefs.getOrPut(Measurements.MEAN_Y_CAL);
        reference.setObjectsName(inputObjectsName);
        reference.setDescription("Mean y-position of all pixels in the object, \""+inputObjectsName+"\"." +
                "  Measured in calibrated ("+SpatialUnit.getOMEUnit().getSymbol()+") units.");
        returnedRefs.add(reference);

        reference = objectMeasurementRefs.getOrPut(Measurements.MEAN_Z_CAL);
        reference.setObjectsName(inputObjectsName);
        reference.setDescription("Mean z-position of all pixels in the object, \""+inputObjectsName+"\"." +
                "  Measured in calibrated ("+SpatialUnit.getOMEUnit().getSymbol()+") units.");
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
        parameters.get(INPUT_OBJECTS).setDescription("Objects to measure mean XYZ centroid for.  Measurements will be associated with each object.");
    }
}
