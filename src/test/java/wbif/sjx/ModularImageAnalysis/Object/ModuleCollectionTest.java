package wbif.sjx.ModularImageAnalysis.Object;

import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;
import org.junit.Ignore;
import org.junit.Test;
import wbif.sjx.ModularImageAnalysis.GUI.GUIAnalysis;
import wbif.sjx.ModularImageAnalysis.Module.ImageMeasurements.MeasureImageIntensity;
import wbif.sjx.ModularImageAnalysis.Module.ImageMeasurements.MeasureImageTexture;
import wbif.sjx.ModularImageAnalysis.Module.ImageProcessing.Pixel.FilterImage;
import wbif.sjx.ModularImageAnalysis.Module.ImageProcessing.Pixel.ThresholdImage;
import wbif.sjx.ModularImageAnalysis.Module.InputOutput.ImageLoader;

import static org.junit.Assert.*;

public class ModuleCollectionTest < T extends RealType< T > & NativeType< T >> {

    @Test
    public void testGetImageMeasurementReferencesNoCutoff() {
        // Creating a ModuleCollection to hold the Modules
        ModuleCollection modules = new ModuleCollection();

        // Populating some modules (no need to populate all parameters as these should be initialised)
        ImageLoader<T> imageLoader = new ImageLoader<>();
        imageLoader.initialiseParameters();
        imageLoader.updateParameterValue(ImageLoader.OUTPUT_IMAGE,"Im 1");
        imageLoader.updateParameterValue(ImageLoader.IMPORT_MODE,ImageLoader.ImportModes.CURRENT_FILE);
        modules.add(imageLoader);

        FilterImage filterImage = new FilterImage();
        filterImage.initialiseParameters();
        filterImage.updateParameterValue(FilterImage.INPUT_IMAGE,"Im 1");
        filterImage.updateParameterValue(FilterImage.APPLY_TO_INPUT,false);
        filterImage.updateParameterValue(FilterImage.OUTPUT_IMAGE,"New_image");
        modules.add(filterImage);

        MeasureImageIntensity measureImageIntensity = new MeasureImageIntensity();
        measureImageIntensity.initialiseParameters();
        measureImageIntensity.updateParameterValue(MeasureImageIntensity.INPUT_IMAGE,"Im1");
        measureImageIntensity.updateParameterValue(MeasureImageIntensity.MEASURE_MEAN,true);
        measureImageIntensity.updateParameterValue(MeasureImageIntensity.MEASURE_STDEV,true);
        measureImageIntensity.updateParameterValue(MeasureImageIntensity.MEASURE_MIN,false);
        measureImageIntensity.updateParameterValue(MeasureImageIntensity.MEASURE_MAX,true);
        measureImageIntensity.updateParameterValue(MeasureImageIntensity.MEASURE_SUM,false);
        modules.add(measureImageIntensity);

        MeasureImageIntensity measureImageIntensity2 = new MeasureImageIntensity();
        measureImageIntensity2.initialiseParameters();
        measureImageIntensity2.updateParameterValue(MeasureImageIntensity.INPUT_IMAGE,"New_image");
        measureImageIntensity2.updateParameterValue(MeasureImageIntensity.MEASURE_MEAN,false);
        measureImageIntensity2.updateParameterValue(MeasureImageIntensity.MEASURE_STDEV,true);
        measureImageIntensity2.updateParameterValue(MeasureImageIntensity.MEASURE_MIN,true);
        measureImageIntensity2.updateParameterValue(MeasureImageIntensity.MEASURE_MAX,true);
        measureImageIntensity2.updateParameterValue(MeasureImageIntensity.MEASURE_SUM,false);
        modules.add(measureImageIntensity2);

        MeasureImageTexture measureImageTexture = new MeasureImageTexture();
        measureImageTexture.initialiseParameters();
        measureImageTexture.updateParameterValue(MeasureImageTexture.INPUT_IMAGE,"New_image");
        modules.add(measureImageTexture);

        // Checking the values for "Im1"
        MeasurementReferenceCollection references1 = modules.getImageMeasurementReferences("Im1");
        assertEquals(3,references1.size());

        String[] expectedNames1 = new String[]{MeasureImageIntensity.Measurements.MEAN,
                MeasureImageIntensity.Measurements.STDEV,
                MeasureImageIntensity.Measurements.MAX};

        for (String expectedName1:expectedNames1) {
            boolean found = false;
            for (MeasurementReference reference1:references1){
                if (reference1.getName().equals(expectedName1)) {
                    found = true;
                    break;
                }
            }
            assertTrue(found);
        }

        // Checking the values for "New_image"
        MeasurementReferenceCollection references2 = modules.getImageMeasurementReferences("New_image");
        assertEquals(7,references2.size());

        String[] expectedNames2 = new String[]{MeasureImageIntensity.Measurements.MIN,
                MeasureImageIntensity.Measurements.STDEV,
                MeasureImageIntensity.Measurements.MAX,
                MeasureImageTexture.Measurements.ASM,
                MeasureImageTexture.Measurements.CONTRAST,
                MeasureImageTexture.Measurements.CORRELATION,
                MeasureImageTexture.Measurements.ENTROPY};

        for (String expectedName2:expectedNames2) {
            boolean found = false;
            for (MeasurementReference reference2:references2){
                if (reference2.getName().equals(expectedName2)) {
                    found = true;
                    break;
                }
            }
            assertTrue(found);
        }
    }

    @Test @Ignore
    public void testGetImageMeasurementReferencesWithCutoff() {
    }

    @Test @Ignore
    public void testGetObjectMeasurementReferencesNoCutoff() {
    }

    @Test @Ignore
    public void testGetObjectMeasurementReferencesWithCutoff() {
    }

    @Test @Ignore
    public void testGetParametersMatchingTypeNoCutoff() {
    }

    @Test @Ignore
    public void testGetParametersMatchingTypeWithCutoff() {
    }

    @Test @Ignore
    public void testGetRelationshipsNoCutoff() {
    }

    @Test @Ignore
    public void testGetRelationshipsWithCutoff() {
    }
}