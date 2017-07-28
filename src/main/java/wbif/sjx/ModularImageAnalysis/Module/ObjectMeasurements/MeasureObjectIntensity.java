package wbif.sjx.ModularImageAnalysis.Module.ObjectMeasurements;

import ij.ImagePlus;
import wbif.sjx.ModularImageAnalysis.Module.HCModule;
import wbif.sjx.ModularImageAnalysis.Object.*;
import wbif.sjx.common.MathFunc.CumStat;

import java.util.ArrayList;

/**
 * Created by sc13967 on 05/05/2017.
 */
public class MeasureObjectIntensity extends HCModule {
    public static final String INPUT_OBJECTS = "Input objects";
    public static final String INPUT_IMAGE = "Input image";


    @Override
    public String getTitle() {
        return "Measure object intensity";

    }

    @Override
    public String getHelp() {
        return null;
    }

    @Override
    public void execute(Workspace workspace, boolean verbose) {
        String moduleName = this.getClass().getSimpleName();
        if (verbose) System.out.println("["+moduleName+"] Initialising");

        // Getting input objects
        String objectName = parameters.getValue(INPUT_OBJECTS);
        ObjSet objects = workspace.getObjects().get(objectName);

        // Getting input image
        String imageName = parameters.getValue(INPUT_IMAGE);
        Image image = workspace.getImages().get(imageName);
        ImagePlus ipl = image.getImagePlus();

        // Measuring intensity for each object and adding the measurement to that object
        for (Obj object:objects.values()) {
            // Initialising the cumulative statistics object to store pixel intensities
            CumStat cs = new CumStat();

            // Getting pixel coordinates
            ArrayList<Integer> x = object.getCoordinates(Obj.X);
            ArrayList<Integer> y = object.getCoordinates(Obj.Y);
            ArrayList<Integer> z = object.getCoordinates(Obj.Z);
            int cPos = object.getCoordinates(Obj.C);
            int tPos = object.getCoordinates(Obj.T);

            // Running through all pixels in this object and adding the intensity to the MultiCumStat object
            for (int i=0;i<x.size();i++) {
                int zPos = z==null ? 0 : z.get(i);

                ipl.setPosition(cPos+1,zPos+1,tPos+1);
                cs.addMeasure(ipl.getProcessor().getPixelValue(x.get(i),y.get(i)));

            }

            // Calculating mean, std, min and max intensity
            MIAMeasurement meanIntensity = new MIAMeasurement(imageName+"_MEAN", cs.getMean());
            meanIntensity.setSource(this);
            object.addMeasurement(meanIntensity);

            MIAMeasurement stdIntensity = new MIAMeasurement(imageName+"_STD", cs.getStd(CumStat.SAMPLE));
            stdIntensity.setSource(this);
            object.addMeasurement(stdIntensity);

            MIAMeasurement minIntensity = new MIAMeasurement(imageName+"_MIN", cs.getMin());
            minIntensity.setSource(this);
            object.addMeasurement(minIntensity);

            MIAMeasurement maxIntensity = new MIAMeasurement(imageName+"_MAX", cs.getMax());
            maxIntensity.setSource(this);
            object.addMeasurement(maxIntensity);

        }


        if (verbose) System.out.println("["+moduleName+"] Complete");
    }

    @Override
    public void initialiseParameters() {
        parameters.addParameter(new Parameter(INPUT_OBJECTS, Parameter.INPUT_OBJECTS,null));
        parameters.addParameter(new Parameter(INPUT_IMAGE, Parameter.INPUT_IMAGE,null));

    }

    @Override
    public ParameterCollection getActiveParameters() {
        ParameterCollection returnedParameters = new ParameterCollection();
        returnedParameters.addParameter(parameters.getParameter(INPUT_IMAGE));
        returnedParameters.addParameter(parameters.getParameter(INPUT_OBJECTS));

        return returnedParameters;

    }

    @Override
    public void addMeasurements(MeasurementCollection measurements) {
        String inputImageName = parameters.getValue(INPUT_IMAGE);
        measurements.addMeasurement(parameters.getValue(INPUT_OBJECTS),inputImageName+"_MEAN");
        measurements.addMeasurement(parameters.getValue(INPUT_OBJECTS),inputImageName+"_STD");
        measurements.addMeasurement(parameters.getValue(INPUT_OBJECTS),inputImageName+"_MIN");
        measurements.addMeasurement(parameters.getValue(INPUT_OBJECTS),inputImageName+"_MAX");

    }

    @Override
    public void addRelationships(RelationshipCollection relationships) {

    }
}
