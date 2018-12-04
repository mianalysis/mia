package wbif.sjx.ModularImageAnalysis.Process;

import wbif.sjx.ModularImageAnalysis.Object.Obj;
import wbif.sjx.ModularImageAnalysis.Object.ObjCollection;

import java.text.DecimalFormat;
import java.util.HashMap;

public class LabelFactory {
    public interface LabelModes {
        String ID = "ID";
        String MEASUREMENT_VALUE = "Measurement value";
        String PARENT_ID = "Parent ID";
        String PARENT_MEASUREMENT_VALUE = "Parent measurement value";

        String[] ALL = new String[]{ID,MEASUREMENT_VALUE,PARENT_ID,PARENT_MEASUREMENT_VALUE};

    }

    private static DecimalFormat getDecimalFormat(int nDecimalPlaces, boolean useScientific) {
        DecimalFormat df;

        if (nDecimalPlaces == 0) {
            df = new DecimalFormat("0");
        } else {
            if (useScientific) {
                StringBuilder zeros = new StringBuilder("0.");
                for (int i = 0; i < nDecimalPlaces; i++) {
                    zeros.append("0");
                }
                zeros.append("E0");
                df = new DecimalFormat(zeros.toString());
            } else {
                StringBuilder zeros = new StringBuilder("0");
                if (nDecimalPlaces != 0) zeros.append(".");
                for (int i = 0;i <nDecimalPlaces; i++) {
                    zeros.append("0");
                }
                df = new DecimalFormat(zeros.toString());
            }
        }

        return df;

    }

    public static HashMap<Integer,String> getIDLabels(ObjCollection objects, int nDecimalPlaces, boolean useScientific) {
        HashMap<Integer,String> IDs = new HashMap<>();
        if (objects == null) return IDs;

        DecimalFormat df = getDecimalFormat(nDecimalPlaces,useScientific);

        for (Obj object:objects.values()) IDs.put(object.getID(),df.format(object.getID()));

        return IDs;

    }

    public static HashMap<Integer,String> getParentIDLabels(ObjCollection objects, String parentObjectsName, int nDecimalPlaces, boolean useScientific) {
        HashMap<Integer,String> IDs = new HashMap<>();
        if (objects == null) return IDs;

        DecimalFormat df = getDecimalFormat(nDecimalPlaces,useScientific);

        for (Obj object:objects.values()) {
            if (object.getParent(parentObjectsName) == null) {
                IDs.put(object.getID(), "NA");
            } else {
                IDs.put(object.getID(), df.format(object.getParent(parentObjectsName).getID()));
            }
        }

        return IDs;

    }

    public static HashMap<Integer,String> getMeasurementLabels(ObjCollection objects, String measurementName, int nDecimalPlaces, boolean useScientific) {
        HashMap<Integer,String> IDs = new HashMap<>();
        if (objects == null) return IDs;

        DecimalFormat df = getDecimalFormat(nDecimalPlaces,useScientific);

        for (Obj object:objects.values()) {
            if (Double.isNaN(object.getMeasurement(measurementName).getValue())) {
                IDs.put(object.getID(), "NA");
            } else {
                IDs.put(object.getID(), df.format(object.getMeasurement(measurementName).getValue()));
            }
        }

        return IDs;

    }

    public static HashMap<Integer,String> getParentMeasurementLabels(ObjCollection objects, String parentObjectsName, String measurementName, int nDecimalPlaces, boolean useScientific) {
        HashMap<Integer,String> IDs = new HashMap<>();
        if (objects == null) return IDs;

        DecimalFormat df = getDecimalFormat(nDecimalPlaces,useScientific);

        for (Obj object:objects.values()) {
            Obj parentObj = object.getParent(parentObjectsName);
            if (parentObj == null) break;

            if (Double.isNaN(parentObj.getMeasurement(measurementName).getValue())) {
                IDs.put(parentObj.getID(), "NA");
            } else {
                IDs.put(parentObj.getID(), df.format(parentObj.getMeasurement(measurementName).getValue()));
            }
        }

        return IDs;

    }
}
