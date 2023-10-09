package io.github.mianalysis.mia.object.system;

import java.awt.Color;

import ij.IJ;
import ij.Prefs;
import io.github.mianalysis.mia.MIA;
import io.github.mianalysis.mia.gui.GUI;
import io.github.mianalysis.mia.module.Categories;
import io.github.mianalysis.mia.module.Category;
import io.github.mianalysis.mia.module.Module;
import io.github.mianalysis.mia.module.Modules;
import io.github.mianalysis.mia.object.Workspace;
import io.github.mianalysis.mia.object.parameters.BooleanP;
import io.github.mianalysis.mia.object.parameters.ChoiceP;
import io.github.mianalysis.mia.object.parameters.Parameters;
import io.github.mianalysis.mia.object.parameters.SeparatorP;
import io.github.mianalysis.mia.object.parameters.abstrakt.Parameter;
import io.github.mianalysis.mia.object.parameters.text.IntegerP;
import io.github.mianalysis.mia.object.parameters.text.MessageP;
import io.github.mianalysis.mia.object.refs.collections.ImageMeasurementRefs;
import io.github.mianalysis.mia.object.refs.collections.MetadataRefs;
import io.github.mianalysis.mia.object.refs.collections.ObjMeasurementRefs;
import io.github.mianalysis.mia.object.refs.collections.ParentChildRefs;
import io.github.mianalysis.mia.object.refs.collections.PartnerRefs;
import loci.formats.IFormatReader;
import loci.formats.Memoizer;

/**
 * Created by Stephen on 24/08/2021.
 */
public class Preferences extends Module {
    public static final String GUI_SEPARATOR = "GUI parameters";
    public static final String THEME = "Theme";
    public static final String SHOW_DEPRECATED = "Show deprecated modules (editing mode)";

    public static final String WORKFLOW_SEPARATOR = "Workflow parameters";
    public static final String IMAGE_DISPLAY_MODE = "Image display mode";

    public static final String DATA_SEPARATOR = "Data parameters";
    public static final String USE_MEMOIZER = "Use memoizer";
    public static final String MEMOIZER_THRESHOLD_S = "Memoizer threshold (s)";
    public static final String KRYO_MESSAGE = "Kryo message";
    // public static final String DATA_STORAGE_MODE = "Data storage mode";
    // public static final String SPECIFY_CACHE_DIRECTORY = "Specify cache
    // directory";
    // public static final String CACHE_DIRECTORY = "Cache directory";

    public interface ImageDisplayModes {
        String COMPOSITE = "Composite";
        String COLOUR = "Colour";

        String[] ALL = new String[] { COLOUR, COMPOSITE };

    }

    public interface Themes extends io.github.mianalysis.mia.gui.Themes {
    };

    // public interface DataStorageModes {
    // // String AUTOMATIC = "Automatic"; // This mode will look at each image and
    // send
    // // it to RAM if bigger than say 10% of the available memory
    // String KEEP_IN_RAM = "Keep in RAM";
    // String STREAM_FROM_DRIVE = "Stream from drive";

    // String[] ALL = new String[] { KEEP_IN_RAM, STREAM_FROM_DRIVE };

    // }

    public void setTheme() {
        String theme = parameters.getValue(THEME, null);

        Prefs.set("MIA.GUI.theme", theme);
        IJ.showMessage("Theme will be applied after restarting Fiji");

        //// Applying here and now appears to cause a StackOverflowError
        // parameters.getParameter(THEME).setValue(theme);
        // try {
        // LookAndFeel lookAndFeel = getThemeClass(theme);
        // UIManager.setLookAndFeel(lookAndFeel);
        // } catch (ClassNotFoundException | InstantiationException |
        //// IllegalAccessException
        // | UnsupportedLookAndFeelException | IllegalArgumentException |
        //// InvocationTargetException | NoSuchMethodException | SecurityException e) {
        // e.printStackTrace();
        // }

        // if (GUI.initialised)
        // GUI.refreshLookAndFeel();

    }

    public boolean darkThemeEnabled() {
        return io.github.mianalysis.mia.gui.Themes.isDarkTheme(parameters.getValue(THEME, null));
    }

    public boolean showDeprecated() {
        return parameters.getValue(SHOW_DEPRECATED, null);
    }

    public void setShowDeprecated(boolean showDeprecated) {
        Prefs.set("MIA.GUI.showDeprecated", showDeprecated);
        parameters.get(SHOW_DEPRECATED).setValue(showDeprecated);
        GUI.updateAvailableModules();
    }

    public void setShowDeprecated() {
        boolean showDeprecated = parameters.getValue(SHOW_DEPRECATED, null);
        Prefs.set("MIA.GUI.showDeprecated", showDeprecated);
        GUI.updateAvailableModules();
    }

    public String imageDisplayMode() {
        return parameters.getValue(IMAGE_DISPLAY_MODE, null);
    }

    public void setImageDisplayMode(String imageDisplayMode) {
        Prefs.set("MIA.Workflow.imageDisplayMode", imageDisplayMode);
        parameters.getParameter(IMAGE_DISPLAY_MODE).setValue(imageDisplayMode);
    }

    public void setImageDisplayMode() {
        String imageDisplayMode = parameters.getValue(IMAGE_DISPLAY_MODE, null);
        Prefs.set("MIA.Workflow.imageDisplayMode", imageDisplayMode);
    }

    public boolean useMemoizer() {
        return parameters.getValue(USE_MEMOIZER, null);
    }

    public void setUseMemoizer(boolean useMemoizer) {
        Prefs.set("MIA.data.useMemoizer", useMemoizer);
        parameters.getParameter(USE_MEMOIZER).setValue(useMemoizer);
    }

    public void setUseMemoizer() {
        boolean useMemoizer = parameters.getValue(USE_MEMOIZER, null);
        Prefs.set("MIA.data.useMemoizer", useMemoizer);
    }

    public int getMemoizerThreshold() {
        return parameters.getValue(MEMOIZER_THRESHOLD_S, null);
    }

    public void setMemoizerThreshold(int threshold) {
        Prefs.set("MIA.data.memoizerThreshold", threshold);
        parameters.getParameter(MEMOIZER_THRESHOLD_S).setValue(threshold);
    }

    public IFormatReader getReader(IFormatReader reader) {
        if ((boolean) parameters.getValue(USE_MEMOIZER, null)) {
            int memoizerThreshold = parameters.getValue(MEMOIZER_THRESHOLD_S, null);
            return new Memoizer(reader, memoizerThreshold * 1000);
        } else {
            return reader;
        }
    }

    // public String getDataStorageMode() {
    // return parameters.getValue(DATA_STORAGE_MODE,null);
    // }

    // public void setDataStorageMode() {
    // String dataStorageMode = parameters.getValue(DATA_STORAGE_MODE,null);
    // Prefs.set("MIA.core.dataStorageMode", dataStorageMode);
    // }

    // public void setDataStorageMode(String dataStorageMode) {
    // Prefs.set("MIA.core.dataStorageMode", dataStorageMode);
    // parameters.get(DATA_STORAGE_MODE).setValue(dataStorageMode);
    // }

    // public boolean isSpecifyCacheDirectory() {
    // return parameters.getValue(SPECIFY_CACHE_DIRECTORY,null);
    // }

    // public void setSpecifyCacheDirectory() {
    // boolean specifyCacheDirectory =
    // parameters.getValue(SPECIFY_CACHE_DIRECTORY,null);
    // Prefs.set("MIA.core.specifyCacheDirectory", specifyCacheDirectory);
    // }

    // public void setSpecifyCacheDirectory(boolean specifyCacheDirectory) {
    // Prefs.set("MIA.core.specifyCacheDirectory", specifyCacheDirectory);
    // parameters.get(SPECIFY_CACHE_DIRECTORY).setValue(specifyCacheDirectory);
    // }

    // public String getCacheDirectory() {
    // return parameters.getValue(CACHE_DIRECTORY,null);
    // }

    // public void setCacheDirectory() {
    // String cacheDirectory = parameters.getValue(CACHE_DIRECTORY,null);
    // Prefs.set("MIA.core.cacheDirectory", cacheDirectory);
    // }

    // public void setCacheDirectory(String cacheDirectory) {
    // Prefs.set("MIA.core.cacheDirectory", cacheDirectory);
    // parameters.get(CACHE_DIRECTORY).setValue(cacheDirectory);
    // }

    public Preferences(Modules modules) {
        super("Preferences", modules);
        // il2Support = IL2Support.FULL;
    }

    @Override
    public Category getCategory() {
        return Categories.CORE;
    }

    @Override
    public String getVersionNumber() {
        return "1.0.0";
    }

    @Override
    public String getDescription() {
        return "General MIA parameters.";
    }

    @Override
    public Status process(Workspace workspace) {
        return Status.PASS;
    }

    @Override
    protected void initialiseParameters() {
        // GUI parameters
        parameters.add(new SeparatorP(GUI_SEPARATOR, this));

        Parameter parameter = new ChoiceP(THEME, this, Prefs.get("MIA.GUI.theme", Themes.FLAT_LAF_LIGHT), Themes.ALL);
        parameter.getControl().getComponent().addPropertyChangeListener("ToolTipText", evt -> {
            if (evt.getOldValue() != null)
                setTheme();
        });
        parameters.add(parameter);

        parameter = new BooleanP(SHOW_DEPRECATED, this, Prefs.get("MIA.GUI.showDeprecated", false));
        parameter.getControl().getComponent().addPropertyChangeListener("ToolTipText", evt -> {
            if (evt.getOldValue() != null)
                setShowDeprecated();
        });
        parameters.add(parameter);

        parameters.add(new SeparatorP(WORKFLOW_SEPARATOR, this));
        parameter = new ChoiceP(IMAGE_DISPLAY_MODE, this,
                Prefs.get("MIA.Workflow.imageDisplayMode", ImageDisplayModes.COMPOSITE), ImageDisplayModes.ALL);
        parameter.getControl().getComponent().addPropertyChangeListener("ToolTipText", evt -> {
            if (evt.getOldValue() != null)
                setImageDisplayMode();
        });
        parameters.add(parameter);

        // Data parameters
        parameters.add(new SeparatorP(DATA_SEPARATOR, this));

        parameter = new BooleanP(USE_MEMOIZER, this, Prefs.get("MIA.data.useMemoizer", false));
        parameter.getControl().getComponent().addPropertyChangeListener("ToolTipText", evt -> {
            if (evt.getOldValue() != null)
                setUseMemoizer();
        });
        parameters.add(parameter);
        parameters.add(new IntegerP(MEMOIZER_THRESHOLD_S, this,
                (int) Prefs.get("MIA.data.memoizerThreshold", 3)));

        parameters.add(new MessageP(KRYO_MESSAGE, this, "Memoizer requires Kryo version 5.4.0 or greater", Color.ORANGE));

        // parameter = new ChoiceP(DATA_STORAGE_MODE, this,
        // Prefs.get("MIA.core.dataStorageMode", DataStorageModes.KEEP_IN_RAM),
        // DataStorageModes.ALL);
        // parameter.getControl().getComponent().addPropertyChangeListener("ToolTipText",
        // evt -> {
        // if (evt.getOldValue() != null)
        // setDataStorageMode();});
        // parameters.add(parameter);

        // parameter = new BooleanP(SPECIFY_CACHE_DIRECTORY, this,
        // Prefs.get("MIA.core.specifyCacheDirectory", false));
        // parameter.getControl().getComponent().addPropertyChangeListener("ToolTipText",
        // evt -> {
        // if (evt.getOldValue() != null)
        // setSpecifyCacheDirectory();});
        // parameters.add(parameter);

        // parameter = new FolderPathP(CACHE_DIRECTORY, this,
        // Prefs.get("MIA.core.cacheDirectory", ""));
        // parameter.getControl().getComponent().addPropertyChangeListener("ToolTipText",
        // evt -> {
        // if (evt.getOldValue() != null)
        // setCacheDirectory();});
        // parameters.add(parameter);

        addParameterDescriptions();

    }

    @Override
    public Parameters updateAndGetParameters() {
        Parameters returnedParameters = new Parameters();

        returnedParameters.add(parameters.getParameter(GUI_SEPARATOR));
        returnedParameters.add(parameters.getParameter(THEME));
        returnedParameters.add(parameters.getParameter(SHOW_DEPRECATED));

        returnedParameters.add(parameters.getParameter(WORKFLOW_SEPARATOR));
        returnedParameters.add(parameters.getParameter(IMAGE_DISPLAY_MODE));

        returnedParameters.add(parameters.getParameter(DATA_SEPARATOR));
        if (MIA.kryoCheck()) {
            returnedParameters.add(parameters.getParameter(USE_MEMOIZER));
            if ((boolean) parameters.getValue(USE_MEMOIZER, null))
                returnedParameters.add(parameters.getParameter(MEMOIZER_THRESHOLD_S));
        } else {
            returnedParameters.add(parameters.get(KRYO_MESSAGE));
        }
        // returnedParameters.add(parameters.getParameter(DATA_STORAGE_MODE));
        // switch ((String) parameters.getValue(DATA_STORAGE_MODE,null)) {
        // case DataStorageModes.STREAM_FROM_DRIVE:
        // returnedParameters.add(parameters.getParameter(SPECIFY_CACHE_DIRECTORY));
        // if ((boolean) parameters.getValue(SPECIFY_CACHE_DIRECTORY,null))
        // returnedParameters.add(parameters.getParameter(CACHE_DIRECTORY));
        // break;
        // }

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

    void addParameterDescriptions() {
        parameters.get(SHOW_DEPRECATED).setDescription(
                "When selected, deprecated modules will appear in the editing view available modules list.  These modules will be marked with a strikethrough their name, but otherwise act as normal.  Note: Modules marked as deprecated will be removed from future versions of MIA.");

        parameters.get(MEMOIZER_THRESHOLD_S).setDescription(
                "When loading an image, if BioFormats takes longer than this threshold to read the metadata, a \"bfmemo\" file will be generated.  This file contains the contents of the imported metadata and can be read on subsequent file loads instead of reimporting the metadata.  For very large files, this can cut subsequent file loads from many minutes to seconds.");

    }

    // class Update implements ActionListener {
    // @Override
    // public void actionPerformed(ActionEvent e) {
    // //setTheme(parameters.getValue(THEME));
    // setShowDeprecated(parameters.getValue(SHOW_DEPRECATED));
    // setDataStorageMode(parameters.getValue(DATA_STORAGE_MODE));
    // setCacheDirectory(parameters.getValue(CACHE_DIRECTORY));
    // }
    // }
}
