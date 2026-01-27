package io.github.mianalysis.mia.process;

import java.text.DecimalFormat;
import java.util.HashMap;

import io.github.mianalysis.mia.object.ObjsI;
import io.github.mianalysis.mia.object.coordinates.ObjI;
import io.github.mianalysis.mia.object.measurements.MeasurementI;
import io.github.mianalysis.mia.object.metadata.DefaultObjMetadata;
import io.github.mianalysis.mia.object.measurements.MeasurementFactories;

public class LabelFactory {
    public interface LabelModes {
        String CHILD_COUNT = "Child count";
        String ID = "ID";
        String MEASUREMENT_VALUE = "Measurement value";
        String OBJECT_METADATA_ITEM = "Object metadata item";
        String PARENT_ID = "Parent ID";
        String PARENT_MEASUREMENT_VALUE = "Parent measurement value";
        String PARTNER_COUNT = "Partner count";

        String[] ALL = new String[] { CHILD_COUNT, ID, MEASUREMENT_VALUE, OBJECT_METADATA_ITEM, PARENT_ID, PARENT_MEASUREMENT_VALUE,
                PARTNER_COUNT };

    }

    public static DecimalFormat getDecimalFormat(int nDecimalPlaces, boolean useScientific) {
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
                if (nDecimalPlaces != 0)
                    zeros.append(".");
                for (int i = 0; i < nDecimalPlaces; i++) {
                    zeros.append("0");
                }
                df = new DecimalFormat(zeros.toString());
            }
        }

        return df;

    }

    public static HashMap<Integer, String> getChildCountLabels(ObjsI objects, String childObjectsName,
            DecimalFormat df) {
        HashMap<Integer, String> IDs = new HashMap<>();
        if (objects == null)
            return IDs;

        for (ObjI object : objects.values()) {
            if (object.getChildren(childObjectsName) == null) {
                IDs.put(object.getID(), "NA");
            } else {
                IDs.put(object.getID(), df.format(object.getChildren(childObjectsName).size()));
            }
        }

        return IDs;

    }

    public static HashMap<Integer, String> getIDLabels(ObjsI objects, DecimalFormat df) {
        HashMap<Integer, String> IDs = new HashMap<>();
        if (objects == null)
            return IDs;

        for (ObjI object : objects.values())
            IDs.put(object.getID(), df.format(object.getID()));

        return IDs;

    }

    public static HashMap<Integer, String> getParentIDLabels(ObjsI objects, String parentObjectsName,
            DecimalFormat df) {
        HashMap<Integer, String> IDs = new HashMap<>();
        if (objects == null)
            return IDs;

        for (ObjI object : objects.values()) {
            if (object.getParent(parentObjectsName) == null) {
                IDs.put(object.getID(), "NA");
            } else {
                IDs.put(object.getID(), df.format(object.getParent(parentObjectsName).getID()));
            }
        }

        return IDs;

    }

    public static HashMap<Integer, String> getMeasurementLabels(ObjsI objects, String measurementName,
            DecimalFormat df) {
        HashMap<Integer, String> IDs = new HashMap<>();
        if (objects == null)
            return IDs;

        for (ObjI object : objects.values()) {
            MeasurementI measurement = object.getMeasurement(measurementName);
            if (measurement == null || Double.isNaN(measurement.getValue()))
                IDs.put(object.getID(), "NA");
            else
                IDs.put(object.getID(), df.format(object.getMeasurement(measurementName).getValue()));
        }

        return IDs;

    }

    public static HashMap<Integer, String> getObjectMetadataLabels(ObjsI objects, String metadataName,
            DecimalFormat df) {
        HashMap<Integer, String> IDs = new HashMap<>();
        if (objects == null)
            return IDs;

        for (ObjI object : objects.values()) {
            DefaultObjMetadata metadataItem = object.getMetadataItem(metadataName);
            if (metadataItem != null)
                IDs.put(object.getID(), metadataItem.getValue());
        }

        return IDs;

    }

    public static HashMap<Integer, String> getParentMeasurementLabels(ObjsI objects, String parentObjectsName,
            String measurementName, DecimalFormat df) {
        HashMap<Integer, String> IDs = new HashMap<>();
        if (objects == null)
            return IDs;

        for (ObjI object : objects.values()) {
            ObjI parentObj = object.getParent(parentObjectsName);
            if (parentObj == null)
                break;

            if (Double.isNaN(parentObj.getMeasurement(measurementName).getValue())) {
                IDs.put(parentObj.getID(), "NA");
            } else {
                IDs.put(parentObj.getID(), df.format(parentObj.getMeasurement(measurementName).getValue()));
            }
        }

        return IDs;

    }

    public static HashMap<Integer, String> getPartnerCountLabels(ObjsI objects, String partnerObjectsName,
            DecimalFormat df) {
        HashMap<Integer, String> IDs = new HashMap<>();
        if (objects == null)
            return IDs;

        for (ObjI object : objects.values()) {
            if (object.getPartners(partnerObjectsName) == null) {
                IDs.put(object.getID(), "NA");
            } else {
                IDs.put(object.getID(), df.format(object.getPartners(partnerObjectsName).size()));
            }
        }

        return IDs;

    }
}
