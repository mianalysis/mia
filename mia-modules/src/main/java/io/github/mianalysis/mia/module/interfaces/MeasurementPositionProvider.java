package io.github.mianalysis.mia.module.interfaces;

import io.github.mianalysis.mia.module.Module;
import io.github.mianalysis.mia.object.Workspace;
import io.github.mianalysis.mia.object.WorkspaceI;
import io.github.mianalysis.mia.object.coordinates.Obj;
import io.github.mianalysis.mia.object.parameters.ChoiceP;
import io.github.mianalysis.mia.object.parameters.ObjectMeasurementP;
import io.github.mianalysis.mia.object.parameters.Parameters;

public interface MeasurementPositionProvider {
    /**
    * 
    */
    public static final String POSITION_MODE = "Position mode";

    /**
     * Object measurement specifying the X-position. Measurement value must be
     * specified in pixel units.
     */
    public static final String X_POSITION_MEASUREMENT = "X-position measurement";

    /**
     * Object measurement specifying the Y-position. Measurement value must be
     * specified in pixel units.
     */
    public static final String Y_POSITION_MEASUREMENT = "Y-position measurement";

    /**
     * Object measurement specifying the Z-position (slice). Measurement value must
     * be specified in slice units.
     */
    public static final String Z_POSITION_MEASUREMENT = "Z-position measurement";


    public static interface PositionModes {
        String OBJECT_CENTROID = "Object centroid";
        String OBJECT_MEASUREMENTS = "Object measurements";

        String[] ALL = new String[] { OBJECT_CENTROID, OBJECT_MEASUREMENTS };
    }

    default Parameters initialisePositionParameters(Module module) {
        Parameters parameters = new Parameters();

        parameters.add(new ChoiceP(POSITION_MODE, module, PositionModes.OBJECT_CENTROID, PositionModes.ALL));
        
        parameters.add(new ObjectMeasurementP(X_POSITION_MEASUREMENT, module));
        parameters.add(new ObjectMeasurementP(Y_POSITION_MEASUREMENT, module));
        parameters.add(new ObjectMeasurementP(Z_POSITION_MEASUREMENT, module));

        return parameters;

    }

    default Parameters updateAndGetPositionParameters(String objectsName, Parameters parameters) {
        WorkspaceI workspace = null;
        Parameters returnedParameters = new Parameters();

        returnedParameters.add(parameters.getParameter(POSITION_MODE));
        switch ((String) parameters.getValue(POSITION_MODE, workspace)) {
            case PositionModes.OBJECT_MEASUREMENTS:
                returnedParameters.add(parameters.getParameter(X_POSITION_MEASUREMENT));
                ((ObjectMeasurementP) parameters.getParameter(X_POSITION_MEASUREMENT)).setObjectName(objectsName);
                
                returnedParameters.add(parameters.getParameter(Y_POSITION_MEASUREMENT));
                ((ObjectMeasurementP) parameters.getParameter(Y_POSITION_MEASUREMENT)).setObjectName(objectsName);

                returnedParameters.add(parameters.getParameter(Z_POSITION_MEASUREMENT));
                ((ObjectMeasurementP) parameters.getParameter(Z_POSITION_MEASUREMENT)).setObjectName(objectsName);
                break;
        }

        return returnedParameters;

    }

    default double[] getObjectPosition(Obj object, Parameters parameters, boolean pixelDistances, boolean matchXY) {
        String positionMode = parameters.getValue(POSITION_MODE, null);
        switch (positionMode) {
            case PositionModes.OBJECT_CENTROID:
            default:
                return new double[]{object.getXMean(pixelDistances), object.getYMean(pixelDistances), object.getZMean(pixelDistances,matchXY)};
                    
            case PositionModes.OBJECT_MEASUREMENTS:
                double[] position = new double[3];
                
                position[0] = object.getMeasurement(parameters.getValue(X_POSITION_MEASUREMENT, null)).getValue();
                position[1] = object.getMeasurement(parameters.getValue(Y_POSITION_MEASUREMENT, null)).getValue();
                position[2] = object.getMeasurement(parameters.getValue(Z_POSITION_MEASUREMENT, null)).getValue();

                return position;
        }
    }
}
