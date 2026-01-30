package io.github.mianalysis.mia.module.system;

import org.scijava.Priority;
import org.scijava.plugin.Plugin;

import io.github.mianalysis.mia.module.Categories;
import io.github.mianalysis.mia.module.Category;
import io.github.mianalysis.mia.module.IL2Support;
import io.github.mianalysis.mia.module.Module;
import io.github.mianalysis.mia.module.Modules;
import io.github.mianalysis.mia.object.WorkspaceI;
import io.github.mianalysis.mia.object.image.ImageFactories;
import io.github.mianalysis.mia.object.image.ImageI;
import io.github.mianalysis.mia.object.image.ImgPlusImageFactory;
import io.github.mianalysis.mia.object.parameters.OutputImageP;
import io.github.mianalysis.mia.object.parameters.Parameters;
import io.github.mianalysis.mia.object.refs.collections.ImageMeasurementRefs;
import io.github.mianalysis.mia.object.refs.collections.MetadataRefs;
import io.github.mianalysis.mia.object.refs.collections.ObjMeasurementRefs;
import io.github.mianalysis.mia.object.refs.collections.ObjMetadataRefs;
import io.github.mianalysis.mia.object.refs.collections.ParentChildRefs;
import io.github.mianalysis.mia.object.refs.collections.PartnerRefs;
import io.github.mianalysis.mia.object.system.Status;
import net.imagej.ImgPlus;
import net.imagej.axis.Axes;
import net.imagej.axis.DefaultLinearAxis;
import net.imagej.ops.Ops.Convert.ImageType;
import net.imglib2.cache.img.DiskCachedCellImgFactory;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.real.FloatType;

/**
 * Created by Stephen Cross on 28/03/2022.
 */
@Plugin(type = Module.class, priority = Priority.LOW, visible = true)
public class TestBigImage<T extends RealType<T> & NativeType<T>> extends Module {
    public static final String OUTPUT_IMAGE = "Output image";

    public TestBigImage(Modules modules) {
        super("Test big image", modules);
        il2Support = IL2Support.FULL;

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
    public Status process(WorkspaceI workspace) {
        // Getting parameters
        String outputImageName = parameters.getValue(OUTPUT_IMAGE, workspace);

        int w = 2000;
        int h = 2000;
        int d = 3000;

        ImgPlus<FloatType> img = new ImgPlus<>(
                new DiskCachedCellImgFactory<>(new FloatType()).create(new int[] { 2000, 2000, 2, 20, 40 }));
        // ImgPlus<FloatType> img = (ImgPlus<FloatType>)
        // ImgPlusTools.createNewImgPlus(w, h, 0, d, 0, 0.2, 0.1, "um",
        // new FloatType());
        // img.dimensions(new long[]{2000,2000,2,20,10});
        img.setAxis(new DefaultLinearAxis(Axes.X, "um", 1), 0);
        img.setAxis(new DefaultLinearAxis(Axes.Y, "um", 1), 1);
        img.setAxis(new DefaultLinearAxis(Axes.CHANNEL, "", 1), 2);
        img.setAxis(new DefaultLinearAxis(Axes.Z, "um", 1), 3);
        img.setAxis(new DefaultLinearAxis(Axes.TIME, "s", 1), 4);

        // Creating a ramp intensity gradient along the x-axis, so operations can be
        // tested
        // RandomAccess ra = img.randomAccess();
        // Random random = new Random();
        // for (int i = 0; i < 1000000; i++) {
        // int x = (int) Math.floor(random.nextDouble() * w);
        // int y = (int) Math.floor(random.nextDouble() * h);
        // int z = (int) Math.floor(random.nextDouble() * d);
        // ra.setPosition(new int[] { x, y, z });
        // ((FloatType) ra.get()).set(1000);
        // }
        // Cursor<T> c = img.cursor();
        // while (c.hasNext()) {
        // c.fwd();
        // ((FloatType) c.get()).set(c.getFloatPosition(0));
        // }

        ImageI image = new ImgPlusImageFactory().create(outputImageName, img);
        workspace.addImage(image);

        if (showOutput)
            image.showAsIs();

        return Status.PASS;

    }

    @Override
    public void initialiseParameters() {
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
        // String storageMode = MIA.preferences.getDataStorageMode();
        // if (storageMode.equals(Preferences.DataStorageModes.STREAM_FROM_DRIVE) &
        // il2Support.equals(IL2Support.NONE))
        // return false;

        return true;
    }

    @Override
    public String getVersionNumber() {
        // TODO Auto-generated method stub
        return "1.0.0";
    }
}