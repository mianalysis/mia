// TODO: Implement generic type (Parameter<T>).  This would require INPUT_IMAGE_1, etc. to be created as classes
// TODO: Throw an error (in the IDE) if a CHOICE_ARRAY or MEASUREMENT isn't constructed with a specified valueSource

package wbif.sjx.ModularImageAnalysis.Object;

import java.io.Serializable;
import java.util.HashMap;

/**
 * Created by sc13967 on 03/05/2017.
 */
public class Parameter implements Serializable {
    /**
     * Name of Image class objects input to the module.  Used to connect images to be analysed between classes.  Input
     * images have been created by previous modules.
     */
    public final static int INPUT_IMAGE = 0;

    /**
     * Name of Image class objects output by the module.  Used to connect images to be analysed between classes.  Output
     * images are created by this module.
     */
    public final static int OUTPUT_IMAGE = 1;

    /**
     * Name of HCObject class objects input to the module.  Used to connect HCObjects to be analysed between classes.
     * Input HCObjects have been created by previous modules.
     */
    public final static int INPUT_OBJECTS = 2;

    /**
     * Name of HCObject class objects output by the module.  Used to connect HCObjects to be analysed between classes.
     * Output HCObjects are created by this module.
     */
    public final static int OUTPUT_OBJECTS = 3;

    /**
     * Image removed by a module.  This is used to tell the ComponentFactory if an image is no longer available
     */
    public final static int REMOVED_IMAGE = 4;

    /**
     * ObjCollection removed by a module.  This is used to tell the ComponentFactory if an object set is no longer
     * available.
     */
    public final static int REMOVED_OBJECTS = 5;

    /**
     * Single integer variable.  These can be set in ParameterWindow by numeric fields
     */
    public final static int INTEGER = 6;

    /**
     * Single double variable.  These can be set in ParameterWindow by numeric fields
     */
    public final static int DOUBLE = 7;

    /**
     * Single string variable.  These can be set in ParameterWindow by string fields
     */
    public final static int STRING = 8;

    /**
     * String array containing choices (e.g. names of thresholding methods).  These are displayed as drop-down choice
     * menus in ParameterWindow
     */
    public final static int CHOICE_ARRAY = 9;

    /**
     * Boolean class parameter.  These are displayed by ParameterWindow as checkboxes.
     */
    public final static int BOOLEAN = 10;

    /**
     * System file parameter.  These are displayed as buttons for loading file open dialog.  This is stored as an
     * absolute path String.
     */
    public final static int FILE_PATH = 11;

    /**
     * System file parameter.  These are displayed as buttons for loading file open dialog.  This is stored as an
     * absolute path String.
     */
    public final static int FOLDER_PATH = 12;

    /**
     * System file parameter.  These are displayed as buttons for loading file open dialog.  This is stored as an
     * absolute path String.
     */
    public final static int FILE_FOLDER_PATH = 13;

    /**
     * HCMeasurement input to the module.  This could be used as a parameter for plotting, or as a value for another
     * parameter
     */
    public final static int IMAGE_MEASUREMENT = 14;

    /**
     * HCMeasurement input to the module.  This could be used as a parameter for plotting, or as a value for another
     * parameter
     */
    public final static int OBJECT_MEASUREMENT = 15;

    /**
     * Child object of the given parent object.
     */
    public final static int CHILD_OBJECTS = 16;

    /**
     * Parent object of the given child object.
     */
    public final static int PARENT_OBJECTS = 17;

    /**
     * Metadata item stored for current Workspace.
     */
    public final static int METADATA_ITEM = 18;

    /**
     * Pseudo parameter which can display text on the parameters page
     */
    public final static int TEXT_DISPLAY = 19;


    private final String name;
    private int type;
    private Object valueSource; // Where the possible values come from (used for CHOICE_ARRAY and MEASUREMENT_FOR_COLOUR)
    private Object value;
    private boolean visible = false;
    private boolean valid = true;


    // CONSTRUCTORS

    public Parameter(String name, int type, Object value, Object valueSource) {
        this.type = type;
        this.name = name;
        this.value = value;
        this.valueSource = valueSource;

    }

    public Parameter(String name, int type, Object value) {
        this.type = type;
        this.name = name;
        this.value = value;
        this.valueSource = null;

    }


    // GETTERS AND SETTERS

    public String getName() {
        return name;
    }

    public void setType(int type) {
        this.type = type;
    }

    public int getType() {
        return type;
    }

    public <T> T getValueSource() {
        return (T) valueSource;
    }

    public void setValueSource(Object valueSource) {
        this.valueSource = valueSource;
    }

    public <T> T getValue() {
        return (T) value;

    }

    public void setValue(Object value) {
        this.value = value;
    }

    public boolean isVisible() {
        return visible;
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    public boolean isValid() {
        return valid;
    }

    public void setValid(boolean valid) {
        this.valid = valid;
    }

    @Override
    public String toString() {
        switch (type) {
            case INPUT_IMAGE:
            case OUTPUT_IMAGE:
            case INPUT_OBJECTS:
            case OUTPUT_OBJECTS:
            case METADATA_ITEM:
                return value == null ? "" : value.toString();

            case INTEGER:
            case DOUBLE:
            case BOOLEAN:
                return value == null ? "" : String.valueOf(value);

            case STRING:
            case CHOICE_ARRAY:
            case TEXT_DISPLAY:
            case FILE_PATH:
            case FOLDER_PATH:
            case FILE_FOLDER_PATH:
                return value == null ? "" : (String) value;

            default:
                return "";

        }
    }
}