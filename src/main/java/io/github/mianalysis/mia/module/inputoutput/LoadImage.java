package io.github.mianalysis.mia.module.inputoutput;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.filefilter.WildcardFileFilter;
import org.scijava.Priority;
import org.scijava.plugin.Plugin;

import io.github.mianalysis.mia.MIA;
import io.github.mianalysis.mia.module.Categories;
import io.github.mianalysis.mia.module.Category;
import io.github.mianalysis.mia.module.IL2Support;
import io.github.mianalysis.mia.module.Module;
import io.github.mianalysis.mia.module.Modules;
import io.github.mianalysis.mia.module.core.InputControl;
import io.github.mianalysis.mia.object.Workspace;
import io.github.mianalysis.mia.object.image.Image;
import io.github.mianalysis.mia.object.image.ImageFactory;
import io.github.mianalysis.mia.object.image.ImageType;
import io.github.mianalysis.mia.object.metadata.Metadata;
import io.github.mianalysis.mia.object.parameters.ChoiceP;
import io.github.mianalysis.mia.object.parameters.FilePathP;
import io.github.mianalysis.mia.object.parameters.OutputImageP;
import io.github.mianalysis.mia.object.parameters.Parameters;
import io.github.mianalysis.mia.object.parameters.SeparatorP;
import io.github.mianalysis.mia.object.parameters.text.MessageP;
import io.github.mianalysis.mia.object.parameters.text.StringP;
import io.github.mianalysis.mia.object.refs.collections.ImageMeasurementRefs;
import io.github.mianalysis.mia.object.refs.collections.MetadataRefs;
import io.github.mianalysis.mia.object.refs.collections.ObjMeasurementRefs;
import io.github.mianalysis.mia.object.refs.collections.ParentChildRefs;
import io.github.mianalysis.mia.object.refs.collections.PartnerRefs;
import io.github.mianalysis.mia.object.system.Colours;
import io.github.mianalysis.mia.object.system.Preferences;
import io.github.mianalysis.mia.object.system.Status;
import io.scif.config.SCIFIOConfig;
import io.scif.config.SCIFIOConfig.ImgMode;
import io.scif.img.IO;
import loci.common.services.DependencyException;
import loci.common.services.ServiceException;
import loci.formats.FormatException;
import net.imagej.ImgPlus;
import net.imagej.axis.Axes;
import net.imagej.axis.CalibratedAxis;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;

/**
 * Created by Stephen Cross on 28/03/2022.
 */
@Plugin(type = Module.class, priority = Priority.LOW, visible = true)
public class LoadImage<T extends RealType<T> & NativeType<T>> extends Module {
    /**
    * 
    */
    public static final String LOADER_SEPARATOR = "Core image loading controls";

    /**
     * Name assigned to the image.
     */
    public static final String OUTPUT_IMAGE = "Output image";

    /**
     * Controls where the image will be loaded from:<br>
     * <ul>
     * <li>"Current file" (default option) will import the current root-file for the
     * workspace (this is the file specified in the "Input control" module).</li>
     * <li>"From ImageJ" will load the active image fromm ImageJ.</li>
     * <li>"Image sequence (alphabetical)" will load a series of images matching a
     * specified name format in alphabetical order. The format of the names to be
     * loaded is specified by the "Sequence root name" parameter.</li>
     * <li>"Image sequence (zero-based)" will load a series of images with numbered
     * elements. The format of the names to be loaded is specified by the "Sequence
     * root name" parameter.</li>
     * <li>"Matching format" will load the image matching a filename based on the
     * root-file for the workspace and a series of rules.</li>
     * <li>"Specific file" will load the image at the location specified by "File
     * path".</li>
     * </ul>
     */
    public static final String IMPORT_MODE = "Import mode";

    /**
     * Format for a generic filename. Plain text can be mixed with global variables
     * or metadata values currently stored in the workspace. Global variables are
     * specified using the "V{name}" notation, where "name" is the name of the
     * variable to insert. Similarly, metadata values are specified with the
     * "M{name}" notation.
     */
    public static final String GENERIC_FORMAT = "Generic format";

    /**
     * List of the currently-available metadata values for this workspace. These can
     * be used when compiling a generic filename.
     */
    public static final String AVAILABLE_METADATA_FIELDS = "Available metadata fields";

    /**
     * Path to file to be loaded.
     */
    public static final String FILE_PATH = "File path";

    /**
     * Controls which series should be loaded for multiseries files (e.g. Leica LIF
     * files):<br>
     * <ul>
     * <li>"Current series" will load the same series as the current root file (i.e.
     * that selected via "Input control").</li>
     * <li>"Specific series" will load a specific series specified by the "Series
     * number"parameter.</li>
     * </ul>
     */
    public static final String SERIES_MODE = "Series mode";

    /**
     * If a specific series is being loaded ("Series mode" set to "Specific
     * series"), this is the series that will be used.
     */
    public static final String SERIES_NUMBER = "Series number";

    public interface ImportModes {
        String CURRENT_FILE = "Current file";
        String MATCHING_FORMAT = "Matching format";
        String SPECIFIC_FILE = "Specific file";

        String[] ALL = new String[] { CURRENT_FILE, MATCHING_FORMAT, SPECIFIC_FILE };

    }

    public interface SeriesModes {
        String CURRENT_SERIES = "Current series";
        String SPECIFIC_SERIES = "Specific series";

        String[] ALL = new String[] { CURRENT_SERIES, SPECIFIC_SERIES };

    }

    public LoadImage(Modules modules) {
        super("Load image 2", modules);
        il2Support = IL2Support.FULL;

        // This module isn't deprecated, but this will keep it mostly hidden
        this.deprecated = true;

    }

    public static String getGenericName(Metadata metadata, String genericFormat)
            throws ServiceException, DependencyException, FormatException, IOException {
        // Returns the first generic name matching the specified format
        return getGenericNames(metadata, genericFormat)[0];

    }

    public static String[] getGenericNames(Metadata metadata, String genericFormat)
            throws ServiceException, DependencyException, FormatException, IOException {
        String absolutePath = metadata.insertMetadataValues(genericFormat);
        String filepath = FilenameUtils.getFullPath(absolutePath);
        String filename = FilenameUtils.getName(absolutePath);

        // If name includes "*" get first instance of wildcard
        if (filename.contains("*")) {
            String[] filenames = new File(filepath).list(new WildcardFileFilter(filename));

            // Appending the filepath to the start of each name
            return Arrays.stream(filenames).map(v -> filepath + v).sorted().toArray(s -> new String[s]);
        }

        return new String[] { filepath + filename };

    }

    @Override
    public Category getCategory() {
        return Categories.INPUT_OUTPUT;
    }

    @Override
    public String getDescription() {
        return "In development image loader using ImgLib2.";
    }

    @Override
    public String getVersionNumber() {
        return "1.0.0";
    }

    public Image<T> loadImage(String filePath, int seriesNumber, String outputImageName) {
        SCIFIOConfig config = new SCIFIOConfig();
        config.imgOpenerSetImgModes(ImgMode.CELL);
        config.imgOpenerSetIndex(seriesNumber - 1);
        ImgPlus<T> img = (ImgPlus<T>) IO.open(filePath, config);

        int xIdx = img.dimensionIndex(Axes.X);
        if (xIdx != -1) {
            CalibratedAxis xAxis = img.axis(xIdx);
            if (xAxis != null && xAxis.unit() != null && xAxis.unit().equals("\\u00B5m"))
                xAxis.setUnit("μm");
        }

        int yIdx = img.dimensionIndex(Axes.Y);
        if (yIdx != -1) {
            CalibratedAxis yAxis = img.axis(yIdx);
            if (yAxis != null && yAxis.unit() != null && yAxis.unit().equals("\\u00B5m"))
                yAxis.setUnit("μm");
        }

        int zIdx = img.dimensionIndex(Axes.Z);
        if (zIdx != -1) {
            CalibratedAxis zAxis = img.axis(zIdx);
            if (zAxis != null && zAxis.unit() != null && zAxis.unit().equals("\\u00B5m"))
                zAxis.setUnit("μm");
        }

        return ImageFactory.createImage(outputImageName, img, ImageType.IMGLIB2);

    }

    @Override
    public Status process(Workspace workspace) {
        // Getting parameters
        String outputImageName = parameters.getValue(OUTPUT_IMAGE, workspace);
        String importMode = parameters.getValue(IMPORT_MODE, workspace);
        String filePath = parameters.getValue(FILE_PATH, workspace);
        String genericFormat = parameters.getValue(GENERIC_FORMAT, workspace);
        String seriesMode = parameters.getValue(SERIES_MODE, workspace);

        // Series number comes from the Workspace
        int seriesNumber = 1;
        switch (seriesMode) {
            case SeriesModes.CURRENT_SERIES:
                seriesNumber = workspace.getMetadata().getSeriesNumber();
                break;
            case SeriesModes.SPECIFIC_SERIES:
                String seriesNumberText = parameters.getValue(SERIES_NUMBER, workspace);
                Metadata metadata = workspace.getMetadata();
                seriesNumber = (int) Math
                        .round(Double.parseDouble(metadata.insertMetadataValues(seriesNumberText)));
                break;
        }

        File file = null;
        try {
            switch (importMode) {
                case ImportModes.CURRENT_FILE:
                    file = workspace.getMetadata().getFile();
                    if (file == null) {
                        MIA.log.writeWarning("No input file/folder selected.");
                        return Status.FAIL;
                    }

                    if (!file.exists()) {
                        MIA.log.writeWarning("File \"" + file.getAbsolutePath() + "\" not found.  Skipping file.");
                        return Status.FAIL;
                    }

                    break;

                case ImportModes.MATCHING_FORMAT:
                    Metadata metadata = (Metadata) workspace.getMetadata().clone();
                    String path;

                    path = getGenericName(metadata, genericFormat);

                    file = new File(path);

                    if (!file.exists()) {
                        MIA.log.writeWarning("File \"" + file.getAbsolutePath() + "\" not found.  Skipping file.");
                        return Status.FAIL;
                    }

                    break;

                case ImportModes.SPECIFIC_FILE:
                    if (!(new File(filePath)).exists()) {
                        MIA.log.writeWarning("File \"" + filePath + "\" not found.  Skipping file.");
                        return Status.FAIL;
                    }

                    break;
            }
        } catch (ServiceException | DependencyException | FormatException | IOException e) {
            MIA.log.writeWarning(e);
            return Status.FAIL;
        }

        if (file == null) {
            MIA.log.writeWarning("File not found.  Skipping module.");
            return Status.FAIL;
        }

        Image<T> image = loadImage(file.getAbsolutePath(), seriesNumber, outputImageName);
        workspace.addImage(image);

        if (showOutput)
            image.show();

        return Status.PASS;

    }

    @Override
    protected void initialiseParameters() {
        Preferences preferences = MIA.getPreferences();
        boolean darkMode = preferences == null ? false : preferences.darkThemeEnabled();

        parameters.add(new SeparatorP(LOADER_SEPARATOR, this));
        parameters.add(new OutputImageP(OUTPUT_IMAGE, this));
        parameters.add(new ChoiceP(IMPORT_MODE, this, ImportModes.CURRENT_FILE, ImportModes.ALL));
        parameters.add(new StringP(GENERIC_FORMAT, this));
        parameters.add(new MessageP(AVAILABLE_METADATA_FIELDS, this, Colours.getDarkBlue(darkMode), 130));
        parameters.add(new FilePathP(FILE_PATH, this));
        parameters.add(new ChoiceP(SERIES_MODE, this, SeriesModes.CURRENT_SERIES, SeriesModes.ALL));
        parameters.add(new StringP(SERIES_NUMBER, this, "1"));

        addParameterDescriptions();

    }

    @Override
    public Parameters updateAndGetParameters() {
        Workspace workspace = null;
        Parameters returnedParameters = new Parameters();

        returnedParameters.add(parameters.getParameter(LOADER_SEPARATOR));
        returnedParameters.add(parameters.getParameter(OUTPUT_IMAGE));
        returnedParameters.add(parameters.getParameter(IMPORT_MODE));
        switch ((String) parameters.getValue(IMPORT_MODE, workspace)) {
            case ImportModes.CURRENT_FILE:
                break;

            case ImportModes.MATCHING_FORMAT:
                returnedParameters.add(parameters.getParameter(GENERIC_FORMAT));
                returnedParameters.add(parameters.getParameter(AVAILABLE_METADATA_FIELDS));
                metadataRefs = modules.getMetadataRefs(this);
                parameters.getParameter(AVAILABLE_METADATA_FIELDS).setValue(metadataRefs.getMetadataValues());

                break;

            case ImportModes.SPECIFIC_FILE:
                returnedParameters.add(parameters.getParameter(FILE_PATH));
                break;
        }

        returnedParameters.add(parameters.getParameter(SERIES_MODE));
        if (parameters.getValue(SERIES_MODE, workspace).equals(SeriesModes.SPECIFIC_SERIES))
            returnedParameters.add(parameters.getParameter(SERIES_NUMBER));

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
        return null;
    }

    @Override
    public PartnerRefs updateAndGetPartnerRefs() {
        return null;
    }

    @Override
    public boolean verify() {
        boolean valid = true;

        // If using the generic metadata extractor, check the values are available
        if (parameters.getValue(IMPORT_MODE, null).equals(ImportModes.MATCHING_FORMAT)) {
            MetadataRefs metadataRefs = modules.getMetadataRefs(this);
            String genericFormat = parameters.getValue(GENERIC_FORMAT, null);
            valid = metadataRefs.hasRef(genericFormat);
            parameters.getParameter(GENERIC_FORMAT).setValid(valid);

        }

        return valid;
    }

    void addParameterDescriptions() {
        parameters.get(OUTPUT_IMAGE).setDescription("Name assigned to the image.");

        parameters.get(IMPORT_MODE).setDescription("Controls where the image will be loaded from:<br><ul>"

                + "<li>\"" + ImportModes.CURRENT_FILE
                + "\" (default option) will import the current root-file for the workspace (this is the file specified in the \""
                + new InputControl(null).getName() + "\" module).</li>"

                + "<li>\"" + ImportModes.MATCHING_FORMAT
                + "\" will load the image matching a filename based on the root-file for the workspace and a series of rules.</li>"

                + "<li>\"" + ImportModes.SPECIFIC_FILE + "\" will load the image at the location specified by \""
                + FILE_PATH + "\".</li></ul>");

        parameters.get(SERIES_MODE).setDescription(
                "Controls which series should be loaded for multiseries files (e.g. Leica LIF files):<br><ul>"

                        + "<li>\"" + SeriesModes.CURRENT_SERIES
                        + "\" will load the same series as the current root file (i.e. that selected via \""
                        + new InputControl(null).getName() + "\").</li>"

                        + "<li>\"" + SeriesModes.SPECIFIC_SERIES + "\" will load a specific series specified by the \""
                        + SERIES_NUMBER + "\"parameter.</li></ul>");

        parameters.get(SERIES_NUMBER).setDescription("If a specific series is being loaded (\"" + SERIES_MODE
                + "\" set to \"" + SeriesModes.SPECIFIC_SERIES + "\"), this is the series that will be used.");

        parameters.get(GENERIC_FORMAT).setDescription(
                "Format for a generic filename.  Plain text can be mixed with global variables or metadata values currently stored in the workspace.  Global variables are specified using the \"V{name}\" notation, where \"name\" is the name of the variable to insert.  Similarly, metadata values are specified with the \"M{name}\" notation.");

        parameters.get(AVAILABLE_METADATA_FIELDS).setDescription(
                "List of the currently-available metadata values for this workspace.  These can be used when compiling a generic filename.");

        parameters.get(FILE_PATH).setDescription("Path to file to be loaded.");
    }
}

// when dataisgood, Gemma = given food
// i = 42^1000000000000000000000000000000000000000000 [dontend]