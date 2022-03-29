package io.github.mianalysis.mia;

import org.scijava.Priority;
import org.scijava.plugin.Plugin;

import io.github.mianalysis.mia.module.Categories;
import io.github.mianalysis.mia.module.Category;
import io.github.mianalysis.mia.module.Module;
import io.github.mianalysis.mia.module.Modules;
import io.github.mianalysis.mia.object.Image;
import io.github.mianalysis.mia.object.Status;
import io.github.mianalysis.mia.object.Workspace;
import io.github.mianalysis.mia.object.parameters.OutputImageP;
import io.github.mianalysis.mia.object.parameters.Parameters;
import io.github.mianalysis.mia.object.refs.collections.ImageMeasurementRefs;
import io.github.mianalysis.mia.object.refs.collections.MetadataRefs;
import io.github.mianalysis.mia.object.refs.collections.ObjMeasurementRefs;
import io.github.mianalysis.mia.object.refs.collections.ParentChildRefs;
import io.github.mianalysis.mia.object.refs.collections.PartnerRefs;
import net.imagej.ImgPlus;
import net.imglib2.img.Img;
import net.imglib2.img.ImgFactory;
import net.imglib2.img.cell.CellImgFactory;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.real.FloatType;

/**
 * Created by Stephen Cross on 28/03/2022.
 */
@Plugin(type = Module.class, priority = Priority.LOW, visible = true)
public class TestBigImage<T extends RealType<T> & NativeType<T>> extends Module {
    public static final String OUTPUT_IMAGE = "Output image";

    public static void main(String[] args) {
        final ImgFactory<FloatType> imgFactory = new CellImgFactory<FloatType>(new FloatType(), 5);
        final Img< FloatType > img1 = imgFactory.create( 600, 900, 100 );
        final ImgPlus<FloatType> img = new ImgPlus<FloatType>(img1);

    }
    public TestBigImage(Modules modules) {
        super("Test big image", modules);

        // This module isn't deprecated, but this will keep it mostly hidden
        this.deprecated = true;
    }

    @Override
    public Category getCategory() {
        return Categories.SYSTEM;
    }

    @Override
    public String getDescription() {
        return "In development image loader using ImgLib2.";
    }

    @Override
    public Status process(Workspace workspace) {
        // Getting parameters
        String outputImageName = parameters.getValue(OUTPUT_IMAGE);

        final ImgFactory<FloatType> imgFactory = new CellImgFactory<FloatType>(new FloatType(), 20);
        final Img< FloatType > img1 = imgFactory.create( 6000, 900, 1000 );
        final ImgPlus<FloatType> img = new ImgPlus<FloatType>(img1);
        
        Image image = new Image(outputImageName, img);
        workspace.addImage(image);
        
        if (showOutput)
            image.showImage();
        
        return Status.PASS;

    }

    @Override
    protected void initialiseParameters() {
        parameters.add(new OutputImageP(OUTPUT_IMAGE, this));
        
    }

    @Override
    public Parameters updateAndGetParameters() {
        Parameters returnedParameters = new Parameters();

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
