package io.github.mianalysis.mia.object.system;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.LookAndFeel;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import com.formdev.flatlaf.FlatDarculaLaf;
import com.formdev.flatlaf.FlatDarkLaf;
import com.formdev.flatlaf.FlatIntelliJLaf;
import com.formdev.flatlaf.FlatLightLaf;

import ij.Prefs;
import io.github.mianalysis.mia.MIA;
import io.github.mianalysis.mia.gui.GUI;
import io.github.mianalysis.mia.module.Categories;
import io.github.mianalysis.mia.module.Category;
import io.github.mianalysis.mia.module.IL2Support;
import io.github.mianalysis.mia.module.Module;
import io.github.mianalysis.mia.module.Modules;
import io.github.mianalysis.mia.object.Workspace;
import io.github.mianalysis.mia.object.parameters.BooleanP;
import io.github.mianalysis.mia.object.parameters.ChoiceP;
import io.github.mianalysis.mia.object.parameters.FolderPathP;
import io.github.mianalysis.mia.object.parameters.GenericButtonP;
import io.github.mianalysis.mia.object.parameters.Parameters;
import io.github.mianalysis.mia.object.parameters.SeparatorP;
import io.github.mianalysis.mia.object.refs.collections.ImageMeasurementRefs;
import io.github.mianalysis.mia.object.refs.collections.MetadataRefs;
import io.github.mianalysis.mia.object.refs.collections.ObjMeasurementRefs;
import io.github.mianalysis.mia.object.refs.collections.ParentChildRefs;
import io.github.mianalysis.mia.object.refs.collections.PartnerRefs;

/**
 * Created by Stephen on 24/08/2021.
 */
public class Preferences extends Module {
    public static final String GUI_SEPARATOR = "GUI parameters";
    public static final String THEME = "Theme";
    public static final String SHOW_DEPRECATED = "Show deprecated modules (editing mode)";

    public static final String DATA_SEPARATOR = "Data parameters";
    public static final String DATA_STORAGE_MODE = "Data storage mode";
    public static final String SPECIFY_CACHE_DIRECTORY = "Specify cache directory";
    public static final String CACHE_DIRECTORY = "Cache directory";

    public static final String UPDATE_SEPARATOR = "Update";
    public static final String UPDATE_PARAMETERS = "Update parameters";

    private static LookAndFeel ijLAF = UIManager.getLookAndFeel();

    public interface Themes {
        String FLAT_LAF_DARK = "Flat LAF (dark)";
        String FLAT_LAF_DARKULA = "Flat LAF (darkula)";
        String FLAT_LAF_INTELLIJ = "Flat LAF (IntelliJ)";
        String FLAT_LAF_LIGHT = "Flat LAF (light)";
        String MATCH_IMAGEJ = "Match ImageJ";
        String SYSTEM_DEFAULT = "System default";

        String[] ALL = new String[] { FLAT_LAF_DARK, FLAT_LAF_DARKULA, FLAT_LAF_INTELLIJ, FLAT_LAF_LIGHT, MATCH_IMAGEJ,
                SYSTEM_DEFAULT };

    }

    public interface DataStorageModes {
        // String AUTOMATIC = "Automatic"; // This mode will look at each image and send
        // it to RAM if bigger than say 10% of the available memory
        String KEEP_IN_RAM = "Keep in RAM";
        String STREAM_FROM_DRIVE = "Stream from drive";

        String[] ALL = new String[] { KEEP_IN_RAM, STREAM_FROM_DRIVE };

    }

    public static String getThemeClass(String theme) {
        switch (theme) {
            case Themes.FLAT_LAF_DARK:
                return FlatDarkLaf.class.getCanonicalName();
            case Themes.FLAT_LAF_DARKULA:
                return FlatDarculaLaf.class.getCanonicalName();
            case Themes.FLAT_LAF_INTELLIJ:
                return FlatIntelliJLaf.class.getCanonicalName();
            case Themes.FLAT_LAF_LIGHT:
                return FlatLightLaf.class.getCanonicalName();
            case Themes.MATCH_IMAGEJ:
                return ijLAF.getClass().getCanonicalName();
            case Themes.SYSTEM_DEFAULT:
            default:
                return UIManager.getSystemLookAndFeelClassName();

        }
    }

    public void setTheme(String theme) {
        Prefs.set("MIA.GUI.theme", theme);
        parameters.getParameter(THEME).setValue(theme);
        String themeClassName = getThemeClass(theme);
        try {
            UIManager.setLookAndFeel(themeClassName);            
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException
                | UnsupportedLookAndFeelException e) {
            e.printStackTrace();
        }
        
        GUI.refreshLookAndFeel();
        
    }

    public boolean isDarkTheme(String theme) {
        switch (theme) {
            case Themes.FLAT_LAF_DARK:
            case Themes.FLAT_LAF_DARKULA:
                return true;
            case Themes.FLAT_LAF_INTELLIJ:
            case Themes.FLAT_LAF_LIGHT:
            case Themes.MATCH_IMAGEJ:
            case Themes.SYSTEM_DEFAULT:
            default:
                return false;

        }
    }

    public boolean darkThemeEnabled() {
        return isDarkTheme(parameters.getValue(THEME));
    }

    public boolean showDeprecated() {
        return parameters.getValue(SHOW_DEPRECATED);
    }

    public void setShowDeprecated(boolean showDeprecated) {
        Prefs.set("MIA.GUI.showDeprecated", showDeprecated);
        parameters.getParameter(SHOW_DEPRECATED).setValue(showDeprecated);
        GUI.updateAvailableModules();
    }

    public String getDataStorageMode() {
        return parameters.getValue(DATA_STORAGE_MODE);
    }

    public void setDataStorageMode(String dataStorageMode) {
        Prefs.set("MIA.core.dataStorageMode", dataStorageMode);
        parameters.getParameter(DATA_STORAGE_MODE).setValue(dataStorageMode);
    }

    public boolean isSpecifyCacheDirectory() {
        return parameters.getValue(SPECIFY_CACHE_DIRECTORY);
    }

    public void setSpecifyCacheDirectory(boolean specifyCacheDirectory) {
        Prefs.set("MIA.core.specifyCacheDirectory", specifyCacheDirectory);
        parameters.getParameter(SPECIFY_CACHE_DIRECTORY).setValue(specifyCacheDirectory);
    }

    public String getCacheDirectory() {
        return parameters.getValue(CACHE_DIRECTORY);
    }

    public void setCacheDirectory(String cacheDirectory) {
        Prefs.set("MIA.core.cacheDirectory", cacheDirectory);
        parameters.getParameter(CACHE_DIRECTORY).setValue(cacheDirectory);
    }

    public Preferences(Modules modules) {
        super("Preferences", modules);
        il2Support = IL2Support.FULL;
    }

    @Override
    public Category getCategory() {
        return Categories.CORE;
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
        parameters.add(new SeparatorP(GUI_SEPARATOR, this));
        parameters.add(new ChoiceP(THEME, this,
                Prefs.get("MIA.GUI.theme", Themes.FLAT_LAF_LIGHT), Themes.ALL));
        parameters.add(new BooleanP(SHOW_DEPRECATED, this, Prefs.get("MIA.GUI.showDeprecated", false)));

        parameters.add(new SeparatorP(DATA_SEPARATOR, this));
        parameters.add(new ChoiceP(DATA_STORAGE_MODE, this,
                Prefs.get("MIA.core.dataStorageMode", DataStorageModes.KEEP_IN_RAM), DataStorageModes.ALL));
        parameters.add(new BooleanP(SPECIFY_CACHE_DIRECTORY, this, Prefs.get("MIA.core.specifyCacheDirectory", false)));
        parameters.add(new FolderPathP(CACHE_DIRECTORY, this, Prefs.get("MIA.core.cacheDirectory", "")));

        parameters.add(new SeparatorP(UPDATE_SEPARATOR, this));
        parameters.add(new GenericButtonP(UPDATE_PARAMETERS, this, UPDATE_PARAMETERS, new Update()));

        addParameterDescriptions();

    }

    @Override
    public Parameters updateAndGetParameters() {
        Parameters returnedParameters = new Parameters();

        returnedParameters.add(parameters.getParameter(GUI_SEPARATOR));
        returnedParameters.add(parameters.getParameter(THEME));
        returnedParameters.add(parameters.getParameter(SHOW_DEPRECATED));

        returnedParameters.add(parameters.getParameter(DATA_SEPARATOR));
        returnedParameters.add(parameters.getParameter(DATA_STORAGE_MODE));
        switch ((String) parameters.getValue(DATA_STORAGE_MODE)) {
            case DataStorageModes.STREAM_FROM_DRIVE:
                returnedParameters.add(parameters.getParameter(SPECIFY_CACHE_DIRECTORY));
                if ((boolean) parameters.getValue(SPECIFY_CACHE_DIRECTORY))
                    returnedParameters.add(parameters.getParameter(CACHE_DIRECTORY));
                break;
        }

        returnedParameters.add(parameters.getParameter(UPDATE_SEPARATOR));
        returnedParameters.add(parameters.getParameter(UPDATE_PARAMETERS));

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

        parameters.get(UPDATE_PARAMETERS).setDescription("When clicked, the preferences within MIA will be updated.");

    }

    class Update implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            setTheme(parameters.getValue(THEME));
            setShowDeprecated(parameters.getValue(SHOW_DEPRECATED));
            setDataStorageMode(parameters.getValue(DATA_STORAGE_MODE));
            setCacheDirectory(parameters.getValue(CACHE_DIRECTORY));
        }
    }
}
