package io.github.mianalysis.mia.module.inputoutput;

import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Iterator;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.scijava.Priority;
import org.scijava.plugin.Plugin;

import ij.ImagePlus;
import ij.gui.Roi;
import ij.io.RoiEncoder;
import io.github.mianalysis.mia.MIA;
import io.github.mianalysis.mia.module.Categories;
import io.github.mianalysis.mia.module.Category;
import io.github.mianalysis.mia.module.Module;
import io.github.mianalysis.mia.module.Modules;
import io.github.mianalysis.mia.module.inputoutput.SaveObjectsAsROIs.FileModes;
import io.github.mianalysis.mia.module.inputoutput.abstrakt.AbstractSaver;
import io.github.mianalysis.mia.object.Workspace;
import io.github.mianalysis.mia.object.image.Image;
import io.github.mianalysis.mia.object.parameters.InputImageP;
import io.github.mianalysis.mia.object.parameters.Parameters;
import io.github.mianalysis.mia.object.parameters.SeparatorP;
import io.github.mianalysis.mia.object.refs.collections.ImageMeasurementRefs;
import io.github.mianalysis.mia.object.refs.collections.MetadataRefs;
import io.github.mianalysis.mia.object.refs.collections.ObjMeasurementRefs;
import io.github.mianalysis.mia.object.refs.collections.ObjMetadataRefs;
import io.github.mianalysis.mia.object.refs.collections.ParentChildRefs;
import io.github.mianalysis.mia.object.refs.collections.PartnerRefs;
import io.github.mianalysis.mia.object.system.Status;

/**
 * Created by sc13967 on 26/06/2017.
 */

/**
* 
*/
@Plugin(type = Module.class, priority = Priority.LOW, visible = true)
public class SaveOverlayAsROIs extends AbstractSaver {

    /**
    * 
    */
    public static final String LOADER_SEPARATOR = "Object output";

    public static final String INPUT_IMAGE = "Input image";

    public SaveOverlayAsROIs(Modules modules) {
        super("Save overlay as ROIs", modules);
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
        return "Saves image overlay as a zipped set of ROIs.  This method preserves colours and labels and allows overlays to be stored in a memory efficient manner.";
    }

    public static void saveOverlay(String outPath, Image inputImage) {
        try {
            ZipOutputStream zos = null;
            DataOutputStream out = null;
            RoiEncoder re = null;
            zos = new ZipOutputStream(new BufferedOutputStream(new FileOutputStream(outPath + ".zip")));
            out = new DataOutputStream(new BufferedOutputStream(zos));
            re = new RoiEncoder(out);

            ImagePlus ipl = inputImage.getImagePlus();
            Iterator<Roi> iterator = ipl.getOverlay().iterator();
            int count = 0;
            while (iterator.hasNext()) {
                Roi roi = iterator.next();
                
                zos.putNextEntry(new ZipEntry(String.valueOf(++count)+".roi"));
                re.write(roi);
                out.flush();

            }

            out.close();

        } catch (IOException e) {
            e.printStackTrace();
            return;
        }
    }

    @Override
    public Status process(Workspace workspace) {
        // Getting input objects
        String inputImageName = parameters.getValue(INPUT_IMAGE, workspace);
        String appendSeriesMode = parameters.getValue(APPEND_SERIES_MODE, workspace);
        String appendDateTimeMode = parameters.getValue(APPEND_DATETIME_MODE, workspace);
        String suffix = parameters.getValue(SAVE_SUFFIX, workspace);

        Image inputImage = workspace.getImage(inputImageName);

        String outputPath = getOutputPath(modules, workspace);
        String outputName = getOutputName(modules, workspace);

        // Ensuring folders have been created
        new File(outputPath).mkdirs();

        // Adding last bits to name
        outputPath = outputPath + outputName;
        outputPath = appendSeries(outputPath, workspace, appendSeriesMode);
        outputPath = appendDateTime(outputPath, appendDateTimeMode);
        outputPath = outputPath + suffix;

        saveOverlay(outputPath, inputImage);

        return Status.PASS;

    }

    @Override
    public void initialiseParameters() {
        super.initialiseParameters();

        parameters.add(new SeparatorP(LOADER_SEPARATOR, this));
        parameters.add(new InputImageP(INPUT_IMAGE, this));

        addParameterDescriptions();

    }

    @Override
    public Parameters updateAndGetParameters() {
        Parameters returnedParameters = new Parameters();

        returnedParameters.add(parameters.getParameter(LOADER_SEPARATOR));
        returnedParameters.add(parameters.getParameter(INPUT_IMAGE));

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
