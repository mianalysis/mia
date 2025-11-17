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
import io.github.mianalysis.mia.MIA;
import io.github.mianalysis.mia.module.Categories;
import io.github.mianalysis.mia.module.Category;
import io.github.mianalysis.mia.module.Module;
import io.github.mianalysis.mia.module.Modules;
import io.github.mianalysis.mia.module.inputoutput.abstrakt.AbstractSaver;
import io.github.mianalysis.mia.object.Obj;
import io.github.mianalysis.mia.object.Objs;
import io.github.mianalysis.mia.object.Workspace;
import io.github.mianalysis.mia.object.parameters.Parameters;
import io.github.mianalysis.mia.object.parameters.objects.OutputObjectsP;
import io.github.mianalysis.mia.object.refs.ObjMeasurementRef;
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

    public static void saveObjects(String outPath, Modules modules, Workspace workspace, Module exportModule) {
        String moduleName = new SaveObjectsAsROIs(null).getName();
        try {
            ZipOutputStream zos = new ZipOutputStream(new BufferedOutputStream(new FileOutputStream(outPath + ".zip")));
            DataOutputStream out = new DataOutputStream(new BufferedOutputStream(zos));
            RoiEncoder re = new RoiEncoder(out);

            // Exporting object templates
            LinkedHashSet<OutputObjectsP> availableObjects = modules.getAvailableObjects(exportModule);
            for (OutputObjectsP availableObject : availableObjects) {
                MIA.log.writeDebug(availableObject.getName());
                JSONObject objTemplateJSON = getObjectTemplate(modules, availableObject.getObjectsName(), exportModule);
                zos.putNextEntry(new ZipEntry(availableObject.getObjectsName()+".objtemplate"));
                out.writeChars(objTemplateJSON.toString());
                out.flush();
            }

            // Exporting objects
            LinkedHashMap<String, Objs> allObjects = workspace.getObjects();
            int total = allObjects.size();
            int count = 0;
            for (Objs inputObjects : allObjects.values()) {
                for (Obj inputObject : inputObjects.values()) {
                    HashMap<Integer, Roi> rois = getObjectRois(inputObject);
                    for (Roi roi : rois.values()) {
                        zos.putNextEntry(new ZipEntry(roi.getName()));
                        re.write(roi);
                        out.flush();
                    }
                }

                writeProgressStatus(++count, total, "objects", moduleName);

            }

            out.close();

        } catch (Exception e) {
            e.printStackTrace();
            return;
        }
    }

    public static JSONObject getObjectTemplate(Modules modules, String objectName, Module exportModule) {
        JSONObject jsonObject = new JSONObject();

        ObjMeasurementRefs currObjectMeasurementRefs = modules.getObjectMeasurementRefs(objectName, exportModule);
        JSONArray measurementArray = new JSONArray();
        for (ObjMeasurementRef currObjectMeasurementRef:currObjectMeasurementRefs.values()) {
            JSONObject measurementJSON = new JSONObject();
            measurementJSON.put("Description", currObjectMeasurementRef.getDescription());
            measurementJSON.put("FinalName", currObjectMeasurementRef.getFinalName());
            measurementJSON.put("IsExportGlobal", currObjectMeasurementRef.isExportGlobal());
            measurementJSON.put("isExportIndividual", currObjectMeasurementRef.isExportIndividual());
            measurementJSON.put("isExportMax", currObjectMeasurementRef.isExportMax());
            measurementJSON.put("isExportMean", currObjectMeasurementRef.isExportMean());
            measurementJSON.put("isExportMin", currObjectMeasurementRef.isExportMin());
            measurementJSON.put("isExportStd", currObjectMeasurementRef.isExportStd());
            measurementJSON.put("isExportSum", currObjectMeasurementRef.isExportSum());
            measurementJSON.put("Name", currObjectMeasurementRef.getName());
            measurementJSON.put("Nickname", currObjectMeasurementRef.getNickname());

            measurementArray.put(measurementJSON);
        }

        jsonObject.put("Measurements", measurementArray);

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
