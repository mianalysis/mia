package io.github.mianalysis.mia.module.inputoutput;

import java.io.File;
import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import org.scijava.Priority;
import org.scijava.plugin.Plugin;

import io.github.mianalysis.mia.MIA;
import io.github.mianalysis.mia.module.Categories;
import io.github.mianalysis.mia.module.Category;
import io.github.mianalysis.mia.module.Module;
import io.github.mianalysis.mia.module.Modules;
import io.github.mianalysis.mia.module.inputoutput.LoadObjectsFromROIs.ObjMetadataItems;
import io.github.mianalysis.mia.module.inputoutput.abstrakt.AbstractSaver;
import io.github.mianalysis.mia.object.ObjMetadata;
import io.github.mianalysis.mia.object.ObjsI;
import io.github.mianalysis.mia.object.Workspace;
import io.github.mianalysis.mia.object.WorkspaceI;
import io.github.mianalysis.mia.object.coordinates.ObjI;
import io.github.mianalysis.mia.object.image.ImageI;
import io.github.mianalysis.mia.object.parameters.InputImageP;
import io.github.mianalysis.mia.object.parameters.InputObjectsP;
import io.github.mianalysis.mia.object.parameters.ObjectMetadataP;
import io.github.mianalysis.mia.object.parameters.Parameters;
import io.github.mianalysis.mia.object.parameters.SeparatorP;
import io.github.mianalysis.mia.object.refs.collections.ImageMeasurementRefs;
import io.github.mianalysis.mia.object.refs.collections.MetadataRefs;
import io.github.mianalysis.mia.object.refs.collections.ObjMeasurementRefs;
import io.github.mianalysis.mia.object.refs.collections.ObjMetadataRefs;
import io.github.mianalysis.mia.object.refs.collections.ParentChildRefs;
import io.github.mianalysis.mia.object.refs.collections.PartnerRefs;
import io.github.mianalysis.mia.object.system.Status;
import io.github.mianalysis.mia.process.io.VOCWriter;

@Plugin(type = Module.class, priority = Priority.LOW, visible = true)
public class ExportVOCAnnotations extends AbstractSaver {
    /**
    * 
    */
    public static final String INPUT_SEPARATOR = "Objects/image input";

    /**
    * 
    */
    public static final String INPUT_OBJECTS = "Input objects";

    /**
    * 
    */
    public static final String INPUT_IMAGE = "Input image";

    /**
    * 
    */
    public static final String OBJECT_SEPARATOR = "Object controls";

    /**
    * 
    */
    public static final String METADATA_FOR_CLASS = "Metadata item for class";

    public ExportVOCAnnotations(Modules modules) {
        super("Export VOC annotations", modules);
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
    public Status process(WorkspaceI workspace) {
        // Getting parameters
        String inputObjectsName = parameters.getValue(INPUT_OBJECTS, workspace);
        String inputImageName = parameters.getValue(INPUT_IMAGE, workspace);
        String metadataForClass = parameters.getValue(METADATA_FOR_CLASS, workspace);
        String appendSeriesMode = parameters.getValue(APPEND_SERIES_MODE, workspace);
        String appendDateTimeMode = parameters.getValue(APPEND_DATETIME_MODE, workspace);
        String suffix = parameters.getValue(SAVE_SUFFIX, workspace);

        // Getting input objects and image
        ObjsI inputObjects = workspace.getObjects(inputObjectsName);
        ImageI inputImage = workspace.getImage(inputImageName);

        String outputPath = getOutputPath(modules, workspace);
        
        // Ensuring folders have been created
        new File(outputPath).mkdirs();
        

        // Adding filename
        String outputName = getOutputName(modules, workspace);
        outputPath = outputPath + outputName;
        outputPath = appendSeries(outputPath, workspace, appendSeriesMode);
        outputPath = appendDateTime(outputPath, appendDateTimeMode);
        outputPath = outputPath + suffix + ".xml";

        try {
            VOCWriter writer = new VOCWriter();

            writer.addImagePath(workspace.getMetadata().getFile().getAbsolutePath());
            writer.addImageSize(inputImage.getImagePlus());
            writer.addOther("segmented", "0");

            for (ObjI inputObject:inputObjects.values()) {
                ObjMetadata metadataItem = inputObject.getMetadataItem(metadataForClass);
                String metadataValue = metadataItem == null ? "Null" : metadataItem.getValue();

                double[][] extentsD = inputObject.getExtents(true, false);
                int[][] extentsI = new int[][]{{(int) Math.round(extentsD[0][0]),(int) Math.round(extentsD[0][1])},{(int) Math.round(extentsD[1][0]),(int) Math.round(extentsD[1][1])}};
                writer.addObject(metadataValue, "Unspecified", false, false, extentsI);

            }


            writer.write(outputPath);

        } catch (IOException | TransformerException | ParserConfigurationException e) {
            MIA.log.writeError(e);
            return Status.FAIL;
        }

        return Status.PASS;

    }

    @Override
    public void initialiseParameters() {
        super.initialiseParameters();

        parameters.add(new SeparatorP(INPUT_SEPARATOR, this));
        parameters.add(new InputObjectsP(INPUT_OBJECTS, this));
        parameters.add(new InputImageP(INPUT_IMAGE, this));

        parameters.add(new SeparatorP(OBJECT_SEPARATOR, this));
        parameters.add(new ObjectMetadataP(METADATA_FOR_CLASS, this));

    }

    @Override
    public Parameters updateAndGetParameters() {
        Parameters returnedParameters = new Parameters();

        returnedParameters.add(parameters.get(INPUT_SEPARATOR));
        returnedParameters.add(parameters.get(INPUT_OBJECTS));
        returnedParameters.add(parameters.get(INPUT_IMAGE));

        returnedParameters.add(parameters.get(INPUT_SEPARATOR));
        returnedParameters.add(parameters.get(METADATA_FOR_CLASS));
        String inputObjectsName = parameters.getValue(INPUT_OBJECTS, null);
        ((ObjectMetadataP) parameters.get(METADATA_FOR_CLASS)).setObjectName(inputObjectsName);

        Parameters saverParameters = super.updateAndGetParameters();
        returnedParameters.addAll(saverParameters);

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
}
