package io.github.mianalysis.mia.object.image;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Set;

import com.drew.lang.annotations.Nullable;

import ij.ImagePlus;
import ij.gui.Overlay;
import ij.measure.ResultsTable;
import ij.process.LUT;
import io.github.mianalysis.mia.module.ModuleI;
import io.github.mianalysis.mia.object.ObjsI;
import io.github.mianalysis.mia.object.coordinates.ObjI;
import io.github.mianalysis.mia.object.coordinates.volume.CoordinateSetFactoryI;
import io.github.mianalysis.mia.object.image.renderer.ImagePlusRenderer;
import io.github.mianalysis.mia.object.image.renderer.ImageRenderer;
import io.github.mianalysis.mia.object.measurements.MeasurementI;
import io.github.mianalysis.mia.object.measurements.MeasurementProvider;
import io.github.mianalysis.mia.object.refs.ImageMeasurementRef;
import io.github.mianalysis.mia.object.refs.collections.ImageMeasurementRefs;
import net.imagej.ImgPlus;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;
import ome.units.quantity.Time;
import ome.units.unit.Unit;

public interface ImageI<T extends RealType<T> & NativeType<T>> extends MeasurementProvider {

    public interface DisplayModes {
        String COLOUR = "Colour";
        String COMPOSITE = "Composite";
        String COMPOSITE_INVERT = "Composite (invert)";
        String COMPOSITE_MAX = "Composite (max)";
        String COMPOSITE_MIN = "Composite (min)";

        String[] ALL = new String[]{COLOUR, COMPOSITE, COMPOSITE_INVERT, COMPOSITE_MAX, COMPOSITE_MIN};

    }

    public ImageRenderer getRenderer();

    public void clear();

    public void setRenderer(ImageRenderer imageRenderer);

    public void show(String title, @Nullable LUT lut, boolean normalise, String displayMode, @Nullable Overlay overlay);

    public int getWidth();

    public int getHeight();

    public int getNChannels();

    public int getNSlices();

    public int getNFrames();

    public double getDppXY();

    public double getDppZ();

    public String getSpatialUnits();

    public double getFrameInterval();

    public Unit<Time> getTemporalUnit();

    public ImagePlus getImagePlus();

    public void setImagePlus(ImagePlus imagePlus);

    public ImgPlus<T> getImgPlus();

    public void setImgPlus(ImgPlus<T> img);

    public Object getRawImage();

    public void setRawImage(Object image);

    public ObjsI initialiseEmptyObjs(String outputObjectsName);

    public void addObject(ObjI obj, float hue);

    public void addObjectCentroid(ObjI obj, float hue);

    public ImageI<T> duplicate(String outputImageName);

    public Overlay getOverlay();

    public void setOverlay(Overlay overlay);


    public static ImageRenderer getGlobalImageRenderer() {
        return GlobalImageRenderer.globalImageRenderer;
    }
    
    public static void setGlobalRenderer(ImageRenderer imageRenderer) {
        GlobalImageRenderer.globalImageRenderer = imageRenderer;
    }

    public static boolean getUseGlobalImageRenderer() {
        return UseGlobalImageRenderer.useGlobalImageRenderer;
    }
    
    public static void setUseGlobalImageRenderer(boolean state) {
        UseGlobalImageRenderer.useGlobalImageRenderer = state;
    }

    public ObjsI convertImageToObjects(CoordinateSetFactoryI factory, String outputObjectsName, boolean singleObject);
                                       
    public ObjsI convertImageToSingleObjects(CoordinateSetFactoryI factory, String outputObjectsName, boolean blackBackground);

    public void addMeasurement(MeasurementI measurement);

    public MeasurementI getMeasurement(String name);

    /**
     * Displays measurement values from a specific Module
     *
     * @param module Module for which to display measurements
     */
    public default void showMeasurements(ModuleI module) {
        String name = getName();
        
        // Getting MeasurementReferences
        ImageMeasurementRefs measRefs = module.updateAndGetImageMeasurementRefs();

        // Creating a new ResultsTable for these values
        ResultsTable rt = new ResultsTable();

        // Getting a list of all measurements relating to this object collection
        LinkedHashSet<String> measNames = new LinkedHashSet<>();
        for (ImageMeasurementRef measRef : measRefs.values()) {
            if (measRef.getImageName().equals(name))
                measNames.add(measRef.getName());
        }

        // Setting the measurements from the Module
        for (String measName : measNames) {
            MeasurementI measurement = getMeasurement(measName);
            double value = measurement == null ? Double.NaN : measurement.getValue();

            // Setting value
            rt.setValue(measName, 0, value);

        }

        // Displaying the results table
        rt.show("\"" + module.getName() + " \"measurements for \"" + name + "\"");

    }

    public default void showAllMeasurements() {
        // Creating a new ResultsTable for these values
        ResultsTable rt = new ResultsTable();

        // Getting a list of all measurements relating to this object collection
        Set<String> measNames = getMeasurements().keySet();

        // Setting the measurements from the Module
        int row = 0;
        for (String measName : measNames) {
            MeasurementI measurement = getMeasurement(measName);
            double value = measurement == null ? Double.NaN : measurement.getValue();

            // Setting value
            rt.setValue(measName, row, value);

        }

        // Displaying the results table
        rt.show("All measurements for \"" + getName() + "\"");

    }

    public default void showWithTitle(String title) {
        show(title, null, true, DisplayModes.COLOUR, null);
    }

    public default void showWithLUT(LUT lut) {
        show(getName(), lut, true, DisplayModes.COLOUR, null);
    }

    public default void showAsIs() {
        show(getName(), null, true, DisplayModes.COLOUR, null);
    }

    public default void showWithOverlay(Overlay overlay) {
        show(getName(), null, true, DisplayModes.COLOUR, overlay);
    }

    public default void showWithNormalisation(boolean normalise) {
        show(getName(), null, normalise, DisplayModes.COLOUR, null);
    }

    // GETTERS AND SETTERS

    public String getName();

    public LinkedHashMap<String, MeasurementI> getMeasurements();

    public void setMeasurements(LinkedHashMap<String, MeasurementI> measurements);

    class GlobalImageRenderer {
        private static ImageRenderer globalImageRenderer = new ImagePlusRenderer();
    }

    class UseGlobalImageRenderer {
        private static boolean useGlobalImageRenderer = false;
    }

}
