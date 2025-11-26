package io.github.mianalysis.mia.module.inputoutput;

import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.json.JSONArray;
import org.json.JSONObject;
import org.scijava.Priority;
import org.scijava.plugin.Plugin;

import ij.gui.Roi;
import ij.io.RoiEncoder;
import io.github.mianalysis.mia.module.Categories;
import io.github.mianalysis.mia.module.Category;
import io.github.mianalysis.mia.module.Module;
import io.github.mianalysis.mia.module.Modules;
import io.github.mianalysis.mia.module.inputoutput.abstrakt.AbstractSaver;
import io.github.mianalysis.mia.object.Obj;
import io.github.mianalysis.mia.object.ObjMetadata;
import io.github.mianalysis.mia.object.Objs;
import io.github.mianalysis.mia.object.Workspace;
import io.github.mianalysis.mia.object.measurements.Measurement;
import io.github.mianalysis.mia.object.parameters.Parameters;
import io.github.mianalysis.mia.object.parameters.objects.OutputObjectsP;
import io.github.mianalysis.mia.object.refs.ObjMeasurementRef;
import io.github.mianalysis.mia.object.refs.ObjMetadataRef;
import io.github.mianalysis.mia.object.refs.collections.ImageMeasurementRefs;
import io.github.mianalysis.mia.object.refs.collections.MetadataRefs;
import io.github.mianalysis.mia.object.refs.collections.ObjMeasurementRefs;
import io.github.mianalysis.mia.object.refs.collections.ObjMetadataRefs;
import io.github.mianalysis.mia.object.refs.collections.ParentChildRefs;
import io.github.mianalysis.mia.object.refs.collections.PartnerRefs;
import io.github.mianalysis.mia.object.system.Status;

/**
 * Created by Stephen Cross on 17/11/25
 */

@Plugin(type = Module.class, priority = Priority.LOW, visible = true)
public class SaveObjects extends AbstractSaver {

    public SaveObjects(Modules modules) {
        super("Save objects", modules);
    }

    @Override
    public Category getCategory() {
        return Categories.INPUT_OUTPUT;
    }

    @Override
    public String getVersionNumber() {
        return "1.0.0";
    }

    @Override
    public String getDescription() {
        return "";
    }

    public enum FieldKeys {
        CHILDREN, MEASUREMENTS, METADATA, NAME, PARENTS, PARTNERS
    }

    public static JSONObject getObjectTemplate(Modules modules, String objectName, Module exportModule) {
        JSONObject jsonObject = new JSONObject();

        jsonObject.put(FieldKeys.NAME.toString(), objectName);

        ObjMeasurementRefs currObjectMeasurementRefs = modules.getObjectMeasurementRefs(objectName, exportModule);
        JSONArray measurementArray = new JSONArray();
        for (ObjMeasurementRef currObjectMeasurementRef : currObjectMeasurementRefs.values())
            measurementArray.put(currObjectMeasurementRef.getName());
        jsonObject.put(FieldKeys.MEASUREMENTS.toString(), measurementArray);

        ObjMetadataRefs currObjectMetadataRefs = modules.getObjectMetadataRefs(objectName, exportModule);
        JSONArray metadataArray = new JSONArray();
        for (ObjMetadataRef currObjectMetadataRef : currObjectMetadataRefs.values())
            metadataArray.put(currObjectMetadataRef.getName());
        jsonObject.put(FieldKeys.METADATA.toString(), metadataArray);

        ParentChildRefs currParentChildRefs = modules.getParentChildRefs(exportModule);

        String[] currParentNames = currParentChildRefs.getParentNames(objectName, false);
        JSONArray parentNameArray = new JSONArray();
        for (String currParentName : currParentNames)
            parentNameArray.put(currParentName);
        jsonObject.put(FieldKeys.PARENTS.toString(), parentNameArray);

        String[] currChildNames = currParentChildRefs.getChildNames(objectName, false);
        JSONArray childNameArray = new JSONArray();
        for (String currChildName : currChildNames)
            childNameArray.put(currChildName);
        jsonObject.put(FieldKeys.CHILDREN.toString(), childNameArray);

        PartnerRefs currPartnerRefs = modules.getPartnerRefs(exportModule);
        String[] currPartnerNames = currPartnerRefs.getPartnerNamesArray(objectName);
        JSONArray partnerNameArray = new JSONArray();
        for (String currPartnerName : currPartnerNames)
            partnerNameArray.put(currPartnerName);
        jsonObject.put(FieldKeys.PARTNERS.toString(), partnerNameArray);

        return jsonObject;

    }

    public static void saveObjects(String outPath, Modules modules, Workspace workspace, Module exportModule) {
        String moduleName = new SaveObjectsAsROIs(null).getName();
        try {
            ZipOutputStream zos = new ZipOutputStream(new BufferedOutputStream(new FileOutputStream(outPath + ".zip")));
            DataOutputStream out = new DataOutputStream(new BufferedOutputStream(zos));
            RoiEncoder re = new RoiEncoder(out);

            // Exporting object templates
            LinkedHashSet<OutputObjectsP> availableObjects = modules.getAvailableObjects(exportModule);
            for (OutputObjectsP availableObject : availableObjects) {
                JSONObject objTemplateJSON = getObjectTemplate(modules, availableObject.getObjectsName(), exportModule);
                zos.putNextEntry(new ZipEntry(availableObject.getObjectsName() + ".objtemplate"));
                out.writeChars(objTemplateJSON.toString());
                out.flush();
            }

            // Exporting objects
            LinkedHashMap<String, Objs> allObjects = workspace.getObjects();
            int total = allObjects.size();
            int count = 0;
            for (Objs inputObjects : allObjects.values()) {
                for (Obj inputObject : inputObjects.values()) {
                    // Exporting ROIs
                    HashMap<Integer, Roi> rois = getObjectRois(inputObject);
                    for (Roi roi : rois.values()) {
                        zos.putNextEntry(new ZipEntry(roi.getName()));
                        re.write(roi);
                        out.flush();
                    }

                    // Exporting object metadata
                    JSONObject objMetadataJSON = getObjectMetadata(inputObject, rois, modules, exportModule);
                    zos.putNextEntry(
                            new ZipEntry(inputObject.getName() + "_ID" + inputObject.getID() + ".objmetadata"));
                    out.writeChars(objMetadataJSON.toString());
                    out.flush();
                }

                writeProgressStatus(++count, total, "objects", moduleName);

            }

            out.close();

        } catch (Exception e) {
            e.printStackTrace();
            return;
        }
    }

    public static JSONObject getObjectMetadata(Obj inputObject, HashMap<Integer, Roi> rois, Modules modules,
            Module exportModule) {
        JSONObject jsonObject = new JSONObject();

        JSONArray measurementArray = new JSONArray();
        for (Measurement currObjectMeasurement : inputObject.getMeasurements().values()) {
            JSONObject measurementJSON = new JSONObject();
            measurementJSON.put("Name", currObjectMeasurement.getName());
            measurementJSON.put("Value", currObjectMeasurement.getValue());
            measurementArray.put(measurementJSON);
        }
        jsonObject.put("Measurements", measurementArray);

        JSONArray metadataArray = new JSONArray();
        for (ObjMetadata currObjectMetadata : inputObject.getMetadata().values()) {
            JSONObject metadataJSON = new JSONObject();
            metadataJSON.put("Name", currObjectMetadata.getName());
            metadataJSON.put("Value", currObjectMetadata.getValue());
            metadataArray.put(metadataJSON);
        }
        jsonObject.put("Metadata", metadataArray);

        JSONArray parentArray = new JSONArray();
        for (Obj currParent : inputObject.getParents(false).values()) {
            JSONObject parentJSON = new JSONObject();
            parentJSON.put("Name", currParent.getName());
            parentJSON.put("ID", currParent.getID());
            parentArray.put(parentJSON);
        }
        jsonObject.put("Parents", parentArray);

        JSONArray childrenArray = new JSONArray();
        for (String currChildrenName : inputObject.getChildren().keySet()) {
            JSONObject childrenJSON = new JSONObject();
            childrenJSON.put("Name", currChildrenName);
            JSONArray childrenIDArray = new JSONArray();
            for (Obj currChild : inputObject.getChildren(currChildrenName).values())
                childrenIDArray.put(currChild.getID());
            childrenJSON.put("IDs", childrenIDArray);
            childrenArray.put(childrenJSON);
        }
        jsonObject.put("Children", childrenArray);

        JSONArray partnersArray = new JSONArray();
        for (String currPartnersName : inputObject.getPartners().keySet()) {
            JSONObject partnersJSON = new JSONObject();
            partnersJSON.put("Name", currPartnersName);
            JSONArray partnersIDArray = new JSONArray();
            for (Obj currPartner : inputObject.getPartners(currPartnersName).values())
                partnersIDArray.put(currPartner.getID());
            partnersJSON.put("IDs", partnersIDArray);
            partnersArray.put(partnersJSON);
        }
        jsonObject.put("Partners", partnersArray);

        JSONArray roiJSON = new JSONArray();
        for (Roi roi : rois.values())
            roiJSON.put(roi.getName());
        jsonObject.put("ROIs", roiJSON);

        return jsonObject;

    }

    public static HashMap<Integer, Roi> getObjectRois(Obj inputObject) {
        Objs inputObjects = inputObject.getObjectCollection();
        int oid = inputObject.getID();

        HashMap<Integer, Roi> rois = inputObject.getRois();
        for (int z : rois.keySet()) {
            Roi roi = rois.get(z);

            String label = inputObject.getName() + "_ID" + String.valueOf(oid) + "_T"
                    + (inputObject.getT() + 1) + "_Z" + (z + 1) + ".roi";

            roi.setName(label);

            if (inputObjects.getNFrames() > 1 && inputObjects.getNSlices() > 1)
                roi.setPosition(1, z + 1, inputObject.getT() + 1);
            else if (inputObjects.getNFrames() > 1 && inputObjects.getNSlices() == 1)
                roi.setPosition(inputObject.getT() + 1);
            else
                roi.setPosition(z + 1);

        }

        return rois;

    }

    @Override
    public Status process(Workspace workspace) {
        // Getting input objects
        String appendSeriesMode = parameters.getValue(APPEND_SERIES_MODE, workspace);
        String appendDateTimeMode = parameters.getValue(APPEND_DATETIME_MODE, workspace);
        String suffix = parameters.getValue(SAVE_SUFFIX, workspace);

        String outputPath = getOutputPath(modules, workspace);
        String outputName = getOutputName(modules, workspace);

        // Ensuring folders have been created
        new File(outputPath).mkdirs();

        // Adding last bits to name
        outputPath = outputPath + outputName;
        outputPath = appendSeries(outputPath, workspace, appendSeriesMode);
        outputPath = appendDateTime(outputPath, appendDateTimeMode);
        outputPath = outputPath + suffix;

        saveObjects(outputPath, modules, workspace, this);

        return Status.PASS;

    }

    @Override
    protected void initialiseParameters() {
        super.initialiseParameters();

    }

    @Override
    public Parameters updateAndGetParameters() {
        Parameters returnedParameters = new Parameters();

        returnedParameters.addAll(super.updateAndGetParameters());

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

    protected void addParameterDescriptions() {
        super.addParameterDescriptions();
    }
}
