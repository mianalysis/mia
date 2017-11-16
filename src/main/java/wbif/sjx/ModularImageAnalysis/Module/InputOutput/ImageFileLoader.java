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

import java.io.IOException;

/**
 * Created by sc13967 on 15/05/2017.
 */
public class ImageFileLoader extends HCModule {
    public static final String IMPORT_MODE = "Import mode";
    public static final String FILE_PATH = "File path";
    public static final String USE_BIOFORMATS = "Use Bio-formats importer";
    public static final String SERIES_NUMBER = "Series number (>= 1)";
    public static final String OUTPUT_IMAGE = "Output image";
    public static final String SHOW_IMAGE = "Show image";

    public interface ImportModes {
        String CURRENT_FILE = "Current file";
        String SPECIFIC_FILE = "Specific file";

        String[] ALL = new String[]{CURRENT_FILE, SPECIFIC_FILE};

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

    @Override
    public String getTitle() {
        return "Load image from file";

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
        String outputImageName = parameters.getValue(OUTPUT_IMAGE);
        int seriesNumber = parameters.getValue(SERIES_NUMBER);

        // If the file currently in the workspace is to be used, update the file path accordingly
        if (importMode.equals(ImportModes.CURRENT_FILE)) {
            if (workspace.getMetadata().getFile() == null) throw new GenericMIAException("Load file using Analysis > Set file to analyse");
            filePath = workspace.getMetadata().getFile().getAbsolutePath();
        }

        ImagePlus ipl;
        // Importing the file
        if (parameters.getValue(USE_BIOFORMATS)) {
            DebugTools.enableLogging("off");
            DebugTools.setRootLevel("off");
            ipl = getBFImage(filePath,seriesNumber);

        } else {
            ipl = IJ.openImage(filePath);

        }

        // If the image is an RGB, converting to composite
        ipl = new CompositeImage(ipl);

        // Adding image to workspace
        if (verbose) System.out.println("["+moduleName+"] Adding image ("+outputImageName+") to workspace");
        workspace.addImage(new Image(outputImageName,ipl));

        // Displaying the image (the image is duplicated, so it doesn't get deleted if the window is closed)
        if (parameters.getValue(SHOW_IMAGE)) {
            ipl = new Duplicator().run(ipl);
            ipl.show();
        }
    }

    @Override
    public void initialiseParameters() {
        parameters.addParameter(new Parameter(IMPORT_MODE, Parameter.CHOICE_ARRAY,ImportModes.CURRENT_FILE,ImportModes.ALL));
        parameters.addParameter(new Parameter(FILE_PATH, Parameter.FILE_PATH,null));
        parameters.addParameter(new Parameter(OUTPUT_IMAGE, Parameter.OUTPUT_IMAGE,null));
        parameters.addParameter(new Parameter(USE_BIOFORMATS, Parameter.BOOLEAN,true));
        parameters.addParameter(new Parameter(SERIES_NUMBER,Parameter.INTEGER,1));
        parameters.addParameter(new Parameter(SHOW_IMAGE, Parameter.BOOLEAN,false));

    }

    @Override
    public ParameterCollection getActiveParameters() {
        ParameterCollection returnedParameters = new ParameterCollection();

        returnedParameters.addParameter(parameters.getParameter(OUTPUT_IMAGE));
        returnedParameters.addParameter(parameters.getParameter(IMPORT_MODE));
        if (parameters.getValue(IMPORT_MODE).equals(ImportModes.SPECIFIC_FILE)) {
            returnedParameters.addParameter(parameters.getParameter(FILE_PATH));

        }

        returnedParameters.addParameter(parameters.getParameter(USE_BIOFORMATS));
        if (parameters.getValue(USE_BIOFORMATS)) {
                returnedParameters.addParameter(parameters.getParameter(SERIES_NUMBER));

        }

        returnedParameters.addParameter(parameters.getParameter(SHOW_IMAGE));

        return returnedParameters;

    }

    @Override
    public void addMeasurements(MeasurementCollection measurements) {

    }

    @Override
    public void addRelationships(RelationshipCollection relationships) {

    }
}