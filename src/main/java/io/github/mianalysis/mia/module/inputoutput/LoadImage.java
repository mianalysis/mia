package io.github.mianalysis.mia.module.inputoutput;

import org.scijava.Priority;
import org.scijava.plugin.Plugin;

import io.github.mianalysis.mia.MIA;
import io.github.mianalysis.mia.module.Categories;
import io.github.mianalysis.mia.module.Category;
import io.github.mianalysis.mia.module.Module;
import io.github.mianalysis.mia.module.Modules;
import io.github.mianalysis.mia.object.Workspace;
import io.github.mianalysis.mia.object.image.Image;
import io.github.mianalysis.mia.object.image.ImageFactory;
import io.github.mianalysis.mia.object.parameters.FilePathP;
import io.github.mianalysis.mia.object.parameters.OutputImageP;
import io.github.mianalysis.mia.object.parameters.Parameters;
import io.github.mianalysis.mia.object.parameters.SeparatorP;
import io.github.mianalysis.mia.object.refs.collections.ImageMeasurementRefs;
import io.github.mianalysis.mia.object.refs.collections.MetadataRefs;
import io.github.mianalysis.mia.object.refs.collections.ObjMeasurementRefs;
import io.github.mianalysis.mia.object.refs.collections.ParentChildRefs;
import io.github.mianalysis.mia.object.refs.collections.PartnerRefs;
import io.github.mianalysis.mia.object.system.Status;
import io.scif.Reader;
import io.scif.SCIFIO;
import io.scif.config.SCIFIOConfig;
import io.scif.config.SCIFIOConfig.ImgMode;
import io.scif.img.IO;
import io.scif.img.SCIFIOImgPlus;
import net.imagej.ImgPlus;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;

/**
 * Created by Stephen Cross on 28/03/2022.
 */
@Plugin(type = Module.class, priority = Priority.LOW, visible = true)
public class LoadImage<T extends RealType<T> & NativeType<T>> extends Module {
    public static final String LOADER_SEPARATOR = "Core image loading controls";
    public static final String FILE_PATH = "File path";
    public static final String OUTPUT_IMAGE = "Output image";    

    public LoadImage(Modules modules) {
        super("Load image 2", modules);

        // This module isn't deprecated, but this will keep it mostly hidden
        this.deprecated = true;
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
    public Status process(Workspace workspace) {
        // Getting parameters
        String filePath = parameters.getValue(FILE_PATH);
        String outputImageName = parameters.getValue(OUTPUT_IMAGE);
        
        MIA.log.writeDebug(filePath);

        SCIFIOConfig config = new SCIFIOConfig();
        config.imgOpenerSetImgModes(ImgMode.CELL);
        ImgPlus<T> img = (ImgPlus<T>) IO.open(filePath, config);

        // For first, basic version, loading the entirety of the "current file" image as a CellImg
        // ImgOpener imgOpener = new ImgOpener();        
        // SCIFIOConfig config = new SCIFIOConfig();
        // config.imgOpenerSetImgModes(ImgMode.CELL);
        // ImgPlus< T > img = new ImgPlus(( Img< T > ) imgOpener.openImgs( filePath, config ).get( 0 ));

        Image image = ImageFactory.createImage(outputImageName, img);
        workspace.addImage(image);
        
        if (showOutput)
            image.showImage();
        
        return Status.PASS;

    }

    @Override
    protected void initialiseParameters() {
        parameters.add(new SeparatorP(LOADER_SEPARATOR, this));
        parameters.add(new FilePathP(FILE_PATH, this));
        parameters.add(new OutputImageP(OUTPUT_IMAGE, this));
        
    }

    @Override
    public Parameters updateAndGetParameters() {
        Parameters returnedParameters = new Parameters();

        returnedParameters.add(parameters.getParameter(LOADER_SEPARATOR));
        returnedParameters.add(parameters.getParameter(FILE_PATH));
        returnedParameters.add(parameters.getParameter(OUTPUT_IMAGE));
        
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
        return true;
    }
}

// when dataisgood, Gemma = given food
// i = 42^1000000000000000000000000000000000000000000 [dontend]
