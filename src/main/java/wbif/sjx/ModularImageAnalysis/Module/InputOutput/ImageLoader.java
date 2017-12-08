// TODO: Load colours from LIF file (if possible).  Similarly, colour Flex files blue, green, red (currently RGB)

package wbif.sjx.ModularImageAnalysis.Module.InputOutput;

import ij.CompositeImage;
import ij.IJ;
import ij.ImagePlus;
import ij.plugin.Duplicator;
import ij.process.ImageProcessor;
import loci.common.DebugTools;
import loci.common.services.DependencyException;
import loci.common.services.ServiceException;
import loci.common.services.ServiceFactory;
import loci.formats.*;
import loci.formats.meta.MetadataStore;
import loci.formats.services.OMEXMLService;
import loci.plugins.util.ImageProcessorReader;
import loci.plugins.util.LociPrefs;
import ome.xml.meta.IMetadata;
import wbif.sjx.ModularImageAnalysis.Exceptions.GenericMIAException;
import wbif.sjx.ModularImageAnalysis.Module.HCModule;
import wbif.sjx.ModularImageAnalysis.Object.*;
import wbif.sjx.common.MetadataExtractors.IncuCyteShortFilenameExtractor;
import wbif.sjx.common.MetadataExtractors.NameExtractor;
import wbif.sjx.common.Object.HCMetadata;

import java.io.File;
import java.io.IOException;

/**
 * Created by sc13967 on 15/05/2017.
 */
public class ImageLoader extends HCModule {
    public static final String IMPORT_MODE = "Import mode";
    public static final String NAME_FORMAT = "Name format";
    public static final String COMMENT = "Comment";
    public static final String FILE_PATH = "File path";
    public static final String USE_BIOFORMATS = "Use Bio-formats importer";
    public static final String SERIES_NUMBER = "Series number (>= 1)";
    public static final String OUTPUT_IMAGE = "Output image";
    public static final String SHOW_IMAGE = "Show image";

    private ImageObjReference outputImageRef;

    public interface ImportModes {
        String CURRENT_FILE = "Current file";
        String IMAGEJ = "From ImageJ";
        String MATCHING_FORMAT = "Matching format";
        String SPECIFIC_FILE = "Specific file";

        String[] ALL = new String[]{CURRENT_FILE, IMAGEJ, MATCHING_FORMAT, SPECIFIC_FILE};

    }

    public interface NameFormats {
        String INCUCYTE_SHORT = "Incucyte short filename";

        String[] ALL = new String[]{INCUCYTE_SHORT};

    }


    private static ImagePlus getFile(String filePath, boolean useBioformats, int seriesNumber) {
        ImagePlus ipl;
        // Importing the file
        if (useBioformats) {
            DebugTools.enableLogging("off");
            DebugTools.setRootLevel("off");
            ipl = getBFImage(filePath,seriesNumber);

        } else {
            ipl = IJ.openImage(filePath);

        }

        // If the image is an RGB, converting to composite
        ipl = new CompositeImage(ipl);

        return ipl;

    }

    private static ImagePlus getBFImage(String path, int seriesNumber) {
        ImagePlus ipl = null;

        ImageProcessorReader reader = new ImageProcessorReader(new ChannelSeparator(LociPrefs.makeImageReader()));

        // Setting spatial calibration
        IMetadata meta = null;
        try {
            ServiceFactory factory = new ServiceFactory();
            OMEXMLService service = factory.getInstance(OMEXMLService.class);
            meta = service.createOMEXMLMetadata();
            reader.setMetadataStore((MetadataStore) meta);

        } catch (DependencyException | ServiceException e) {
            e.printStackTrace();
        }

        try {
            reader.setGroupFiles(false);
            reader.setId(path);

            reader.setSeries(seriesNumber-1);

            int width = reader.getSizeX();
            int height = reader.getSizeY();
            int sizeC = reader.getSizeC();
            int sizeT = reader.getSizeT();
            int sizeZ = reader.getSizeZ();
            int bitDepth = reader.getBitsPerPixel();

            ipl = IJ.createHyperStack("Image", width, height, sizeC, sizeZ, sizeT, bitDepth);

            for (int z = 0; z < sizeZ; z++) {
                for (int c = 0; c < sizeC; c++) {
                    for (int t = 0; t < sizeT; t++) {
                        int idx = reader.getIndex(z, c, t);
                        ImageProcessor ip = reader.openProcessors(idx)[0];

                        ipl.setPosition(c + 1, z + 1, t + 1);
                        ipl.setProcessor(ip);

                    }
                }
            }

            ipl.setPosition(1, 1, 1);

            // Add spatial calibration
            if (meta != null) {
                if (meta.getPixelsPhysicalSizeX(0) != null) {
                    ipl.getCalibration().pixelWidth = (double) meta.getPixelsPhysicalSizeX(0).value();
                } else {
                    ipl.getCalibration().pixelWidth = 1.0;
                }

                if (meta.getPixelsPhysicalSizeX(0) != null) {
                    ipl.getCalibration().pixelHeight = (double) meta.getPixelsPhysicalSizeX(0).value();
                } else {
                    ipl.getCalibration().pixelHeight = 1.0;
                }

                if (ipl.getNSlices() > 1 && meta.getPixelsPhysicalSizeZ(0) != null) {
                    ipl.getCalibration().pixelDepth = (double) meta.getPixelsPhysicalSizeZ(0).value();
                } else {
                    ipl.getCalibration().pixelDepth = 1.0;
                }
            }

            reader.close();

        } catch (FormatException | IOException e) {
            e.printStackTrace();
        }

        return ipl;

    }

    private static ImagePlus getFormattedNameImage(String nameFormat, HCMetadata metadata, String comment,
                                                   boolean useBioformats, int seriesNumber) {

        String filename = null;
        switch (nameFormat) {
            case NameFormats.INCUCYTE_SHORT:
                // First, running metadata extraction on the input file
                NameExtractor filenameExtractor = new IncuCyteShortFilenameExtractor();
                filenameExtractor.extract(metadata, metadata.getFile().getName());

                // Constructing a new name using the same name format
                filename = metadata.getFile().getParent()+"\\"+IncuCyteShortFilenameExtractor
                        .generate(comment,metadata.getWell(),metadata.getAsString(HCMetadata.FIELD),metadata.getExt());

                break;
        }

        return getFile(filename,useBioformats,seriesNumber);

    }

    @Override
    public String getTitle() {
        return "Load image";

    }

    @Override
    public String getHelp() {
        return null;
    }

    @Override
    public void run(Workspace workspace, boolean verbose) throws GenericMIAException {
        // Getting parameters
        String importMode = parameters.getValue(IMPORT_MODE);
        String filePath = parameters.getValue(FILE_PATH);
        String nameFormat = parameters.getValue(NAME_FORMAT);
        String comment = parameters.getValue(COMMENT);
        boolean useBioformats = parameters.getValue(USE_BIOFORMATS);
        int seriesNumber = parameters.getValue(SERIES_NUMBER);
        String outputImageName = parameters.getValue(OUTPUT_IMAGE);
        boolean showImage = parameters.getValue(SHOW_IMAGE);

        ImagePlus ipl = null;
        switch (importMode) {
            case ImportModes.CURRENT_FILE:
                File file = workspace.getMetadata().getFile();
                if (file == null) throw new GenericMIAException("Load file using Analysis > Set file to analyse");
                ipl = getFile(workspace.getMetadata().getFile().getAbsolutePath(),useBioformats,seriesNumber);
                break;

            case ImportModes.IMAGEJ:
                ipl = IJ.getImage();
                break;

            case ImportModes.MATCHING_FORMAT:
                ipl = getFormattedNameImage(nameFormat,workspace.getMetadata(),comment,useBioformats,seriesNumber);
                break;

            case ImportModes.SPECIFIC_FILE:
                ipl = getFile(filePath,useBioformats,seriesNumber);
                break;
        }

        // Adding image to workspace
        if (verbose) System.out.println("["+moduleName+"] Adding image ("+outputImageName+") to workspace");
        workspace.addImage(new Image(outputImageName,ipl));

        // Displaying the image (the image is duplicated, so it doesn't get deleted if the window is closed)
        if (showImage && ipl != null) {
            ipl = new Duplicator().run(ipl);
            ipl.show();
        }
    }

    @Override
    public ParameterCollection initialiseParameters() {
        ParameterCollection returnedParameters = new ParameterCollection();

        returnedParameters.addParameter(
                new Parameter(IMPORT_MODE, Parameter.CHOICE_ARRAY,ImportModes.CURRENT_FILE,ImportModes.ALL));
        returnedParameters.addParameter(
                new Parameter(NAME_FORMAT,Parameter.CHOICE_ARRAY,NameFormats.INCUCYTE_SHORT,NameFormats.ALL));
        returnedParameters.addParameter(new Parameter(COMMENT,Parameter.STRING,""));
        returnedParameters.addParameter(new Parameter(FILE_PATH, Parameter.FILE_PATH,null));
        returnedParameters.addParameter(new Parameter(OUTPUT_IMAGE, Parameter.OUTPUT_IMAGE,null));
        returnedParameters.addParameter(new Parameter(USE_BIOFORMATS, Parameter.BOOLEAN,true));
        returnedParameters.addParameter(new Parameter(SERIES_NUMBER,Parameter.INTEGER,1));
        returnedParameters.addParameter(new Parameter(SHOW_IMAGE, Parameter.BOOLEAN,false));

        return returnedParameters;

    }

    @Override
    public ParameterCollection updateAndGetParameters() {
        ParameterCollection returnedParameters = new ParameterCollection();

        returnedParameters.addParameter(parameters.getParameter(IMPORT_MODE));
        switch((String) parameters.getValue(IMPORT_MODE)) {
            case ImportModes.CURRENT_FILE:
                break;

            case ImportModes.IMAGEJ:
                break;

            case ImportModes.MATCHING_FORMAT:
                returnedParameters.addParameter(parameters.getParameter(NAME_FORMAT));
                returnedParameters.addParameter(parameters.getParameter(COMMENT));
                break;

            case ImportModes.SPECIFIC_FILE:
                returnedParameters.addParameter(parameters.getParameter(FILE_PATH));
                break;
        }

        returnedParameters.addParameter(parameters.getParameter(USE_BIOFORMATS));
        if (parameters.getValue(USE_BIOFORMATS)) {
                returnedParameters.addParameter(parameters.getParameter(SERIES_NUMBER));

        }

        returnedParameters.addParameter(parameters.getParameter(OUTPUT_IMAGE));
        returnedParameters.addParameter(parameters.getParameter(SHOW_IMAGE));

        return returnedParameters;

    }

    @Override
    protected MeasurementReferenceCollection initialiseImageMeasurementReferences() {
        return null;
    }

    @Override
    public MeasurementReferenceCollection updateAndGetImageMeasurementReferences() {
        return null;
    }

    @Override
    protected MeasurementReferenceCollection initialiseObjectMeasurementReferences() {
        return null;
    }

    @Override
    public MeasurementReferenceCollection updateAndGetObjectMeasurementReferences() {
        return null;
    }

    @Override
    public void addRelationships(RelationshipCollection relationships) {

    }
}