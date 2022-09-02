package io.github.mianalysis.mia.module.inputoutput;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.scijava.Priority;
import org.scijava.plugin.Plugin;

import com.drew.lang.annotations.Nullable;

import ij.ImagePlus;
import ij.gui.Roi;
import ij.io.RoiDecoder;
import io.github.mianalysis.mia.MIA;
import io.github.mianalysis.mia.module.Categories;
import io.github.mianalysis.mia.module.Category;
import io.github.mianalysis.mia.module.Module;
import io.github.mianalysis.mia.module.Modules;
import io.github.mianalysis.mia.module.objects.relate.TrackObjects;
import io.github.mianalysis.mia.object.Obj;
import io.github.mianalysis.mia.object.Objs;
import io.github.mianalysis.mia.object.Workspace;
import io.github.mianalysis.mia.object.image.Image;
import io.github.mianalysis.mia.object.parameters.BooleanP;
import io.github.mianalysis.mia.object.parameters.ChoiceP;
import io.github.mianalysis.mia.object.parameters.FilePathP;
import io.github.mianalysis.mia.object.parameters.InputImageP;
import io.github.mianalysis.mia.object.parameters.Parameters;
import io.github.mianalysis.mia.object.parameters.SeparatorP;
import io.github.mianalysis.mia.object.parameters.objects.OutputObjectsP;
import io.github.mianalysis.mia.object.parameters.text.MessageP;
import io.github.mianalysis.mia.object.parameters.text.StringP;
import io.github.mianalysis.mia.object.refs.ParentChildRef;
import io.github.mianalysis.mia.object.refs.collections.ImageMeasurementRefs;
import io.github.mianalysis.mia.object.refs.collections.MetadataRefs;
import io.github.mianalysis.mia.object.refs.collections.ObjMeasurementRefs;
import io.github.mianalysis.mia.object.refs.collections.ParentChildRefs;
import io.github.mianalysis.mia.object.refs.collections.PartnerRefs;
import io.github.mianalysis.mia.object.system.Colours;
import io.github.mianalysis.mia.object.system.Preferences;
import io.github.mianalysis.mia.object.system.Status;
import io.github.mianalysis.mia.object.units.TemporalUnit;
import io.github.sjcross.sjcommon.object.volume.SpatCal;
import io.github.sjcross.sjcommon.object.volume.VolumeType;
import loci.common.services.DependencyException;
import loci.common.services.ServiceException;
import loci.formats.FormatException;

/**
 * Created by sc13967 on 26/06/2017.
 */
@Plugin(type = Module.class, priority = Priority.LOW, visible = true)
public class LoadObjectsFromROIs extends Module {
    public static final String LOADER_SEPARATOR = "Object loading";
    public static final String OUTPUT_OBJECTS = "Output objects";
    public static final String ASSIGN_TRACKS = "Assign tracks";
    public static final String TRACK_OBJECTS = "Output track objects";
    public static final String REFERENCE_IMAGE = "Reference image";

    public static final String PATH_SEPARATOR = "File path controls";
    public static final String FILE_PATH_MODE = "File path mode";
    public static final String SPECIFIC_FILE_PATH = "Specific file path";
    public static final String GENERIC_FILE_PATH = "Generic file path";
    public static final String AVAILABLE_METADATA_FIELDS = "Available metadata fields";

    public interface FilePathModes {
        String GENERIC_PATH = "Generic path";
        String SPECIFIC_FILE = "Specific file";

        String[] ALL = new String[] { GENERIC_PATH, SPECIFIC_FILE };

    }

    public LoadObjectsFromROIs(Modules modules) {
        super("Load objects from ROIs", modules);
    }

    @Override
    public Category getCategory() {
        return Categories.INPUT_OUTPUT;
    }

    @Override
    public String getDescription() {
        return "";
    }

    public static void loadObjects(String filePath, Objs outputObjects, @Nullable Objs trackObjects) {
        Pattern pattern = Pattern.compile("ID([0-9]+)_TR([\\-0-9]+)_T([0-9]+)_Z([0-9]+)");

        // This map contains each track (track IDs as keys). Each value is itself a map
        // of timepoints and the associated object IDs.
        // HashMap<Integer, HashMap<Integer, Integer>> trackLinks = trackObjects == null
        // ? null : new HashMap<>();

        try {
            ZipInputStream in = new ZipInputStream(new FileInputStream(filePath));
            byte[] buf = new byte[1024];
            int len;
            ZipEntry entry = in.getNextEntry();

            while (entry != null) {
                String name = entry.getName();

                if (!name.endsWith(".roi")) {
                    entry = in.getNextEntry();
                    continue;
                }

                ByteArrayOutputStream out = new ByteArrayOutputStream();
                while ((len = in.read(buf)) > 0)
                    out.write(buf, 0, len);
                out.close();

                byte[] bytes = out.toByteArray();
                RoiDecoder rd = new RoiDecoder(bytes, name);
                Roi roi = rd.getRoi();

                if (roi == null) {
                    entry = in.getNextEntry();
                    continue;
                }

                name = name.substring(0, name.length() - 4);
                Matcher matcher = pattern.matcher(name);
                if (matcher.matches()) {
                    int oid = Integer.parseInt(matcher.group(1));
                    int tid = Integer.parseInt(matcher.group(2));
                    int t = Integer.parseInt(matcher.group(3))-1;
                    int z = Integer.parseInt(matcher.group(4))-1;

                    if (!outputObjects.keySet().contains(oid))
                        outputObjects.createAndAddNewObject(VolumeType.QUADTREE, oid);
                    Obj outputObject = outputObjects.get(oid);

                    if (trackObjects != null && tid != -1) {
                        if (!trackObjects.keySet().contains(tid))
                            trackObjects.createAndAddNewObject(VolumeType.QUADTREE, tid);
                        Obj trackObject = trackObjects.get(tid);
                        trackObject.addChild(outputObject);
                        outputObject.addParent(trackObject);
                    }

                    outputObject.addPointsFromRoi(roi, z);
                    outputObject.setT(t);

                }

                entry = in.getNextEntry();

            }

            in.close();

        } catch (

        IOException e) {
            e.printStackTrace();
            return;
        }
    }

    @Override
    public Status process(Workspace workspace) {
        // Getting parameters
        String outputObjectsName = parameters.getValue(OUTPUT_OBJECTS, workspace);
        boolean assignTracks = parameters.getValue(ASSIGN_TRACKS, workspace);
        String trackObjectsName = parameters.getValue(TRACK_OBJECTS, workspace);
        String referenceImageName = parameters.getValue(REFERENCE_IMAGE, workspace);

        String filePathMode = parameters.getValue(FILE_PATH_MODE, workspace);
        String filePath = parameters.getValue(SPECIFIC_FILE_PATH, workspace);
        String genericFilePath = parameters.getValue(GENERIC_FILE_PATH, workspace);

        // Getting reference image
        Image refImage = workspace.getImage(referenceImageName);
        if (refImage == null) {
            MIA.log.writeWarning("Reference image not found.  Skipping file.");
            return Status.FAIL;
        }

        // Getting the final file path
        switch (filePathMode) {
            case FilePathModes.GENERIC_PATH:
                try {
                    filePath = ImageLoader.getGenericName(workspace.getMetadata(), genericFilePath);
                } catch (ServiceException | DependencyException | FormatException | IOException e) {
                    e.printStackTrace();
                }
                break;
        }

        // Creating output objects
        ImagePlus refIpl = refImage.getImagePlus();
        SpatCal cal = SpatCal.getFromImage(refIpl);
        int nFrames = refIpl.getNFrames();
        double frameInterval = refIpl.getCalibration().frameInterval;
        Objs outputObjects = new Objs(outputObjectsName, cal, nFrames, frameInterval, TemporalUnit.getOMEUnit());
        workspace.addObjects(outputObjects);

        Objs trackObjects = null;
        if (assignTracks) {
            trackObjects = new Objs(trackObjectsName, cal, nFrames, frameInterval, TemporalUnit.getOMEUnit());
            workspace.addObjects(trackObjects);
        }

        loadObjects(filePath, outputObjects, trackObjects);

        if (showOutput)
            if (assignTracks)
                TrackObjects.showObjects(outputObjects, trackObjectsName);
            else
                outputObjects.convertToImageRandomColours().showImage();

        return Status.PASS;

    }

    @Override
    protected void initialiseParameters() {
        Preferences preferences = MIA.getPreferences();
        boolean darkMode = preferences == null ? false : preferences.darkThemeEnabled();

        parameters.add(new SeparatorP(LOADER_SEPARATOR, this));
        parameters.add(new OutputObjectsP(OUTPUT_OBJECTS, this));
        parameters.add(new BooleanP(ASSIGN_TRACKS, this, false));
        parameters.add(new OutputObjectsP(TRACK_OBJECTS, this));
        parameters.add(new InputImageP(REFERENCE_IMAGE, this));

        parameters.add(new SeparatorP(PATH_SEPARATOR, this));
        parameters.add(new ChoiceP(FILE_PATH_MODE, this, FilePathModes.GENERIC_PATH, FilePathModes.ALL));
        parameters.add(new StringP(GENERIC_FILE_PATH, this));
        parameters.add(new MessageP(AVAILABLE_METADATA_FIELDS, this, Colours.getDarkBlue(darkMode), 170));
        parameters.add(new FilePathP(SPECIFIC_FILE_PATH, this));

        addParameterDescriptions();

    }

    @Override
    public Parameters updateAndGetParameters() {
        Workspace workspace = null;
        Parameters returnedParameters = new Parameters();

        returnedParameters.add(parameters.getParameter(LOADER_SEPARATOR));
        returnedParameters.add(parameters.getParameter(OUTPUT_OBJECTS));
        returnedParameters.add(parameters.getParameter(ASSIGN_TRACKS));
        if ((boolean) parameters.getValue(ASSIGN_TRACKS, workspace))
            returnedParameters.add(parameters.getParameter(TRACK_OBJECTS));
        returnedParameters.add(parameters.getParameter(REFERENCE_IMAGE));

        returnedParameters.add(parameters.getParameter(PATH_SEPARATOR));
        returnedParameters.add(parameters.getParameter(FILE_PATH_MODE));
        switch ((String) parameters.getValue(FILE_PATH_MODE, workspace)) {
            case FilePathModes.GENERIC_PATH:
                returnedParameters.add(parameters.getParameter(GENERIC_FILE_PATH));
                returnedParameters.add(parameters.getParameter(AVAILABLE_METADATA_FIELDS));
                metadataRefs = modules.getMetadataRefs(this);
                parameters.getParameter(AVAILABLE_METADATA_FIELDS).setValue(metadataRefs.getMetadataValues());
                break;
            case FilePathModes.SPECIFIC_FILE:
                returnedParameters.add(parameters.getParameter(SPECIFIC_FILE_PATH));
                break;
        }

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
    public MetadataRefs updateAndGetMetadataReferences() {
        return null;
    }

    @Override
    public ParentChildRefs updateAndGetParentChildRefs() {
        Workspace workspace = null;
        ParentChildRefs returnedRefs = new ParentChildRefs();

        if ((boolean) parameters.getValue(ASSIGN_TRACKS, workspace)) {
            String outputObjectsName = parameters.getValue(OUTPUT_OBJECTS, workspace);
            String trackObjectsName = parameters.getValue(TRACK_OBJECTS, workspace);
            returnedRefs.add(new ParentChildRef(trackObjectsName, outputObjectsName));
        }

        return returnedRefs;

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

    }
}