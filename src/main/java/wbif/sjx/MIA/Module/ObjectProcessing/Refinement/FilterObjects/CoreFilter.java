package wbif.sjx.MIA.Module.ObjectProcessing.Refinement.FilterObjects;

import ij.ImagePlus;
import wbif.sjx.MIA.Module.Module;
import wbif.sjx.MIA.Module.ObjectProcessing.Miscellaneous.ConvertObjectsToImage;
import wbif.sjx.MIA.Object.ModuleCollection;
import wbif.sjx.MIA.Object.Obj;
import wbif.sjx.MIA.Object.ObjCollection;
import wbif.sjx.MIA.Process.ColourFactory;
import wbif.sjx.common.Object.LUTs;

import java.util.HashMap;
import java.util.Iterator;

public abstract class CoreFilter extends Module {
    public CoreFilter(ModuleCollection modules) {
        super(modules);
    }

    public interface FilterModes {
        String DO_NOTHING = "Do nothing";
        String MOVE_FILTERED = "Move filtered objects to new class";
        String REMOVE_FILTERED = "Remove filtered objects";

        String[] ALL = new String[]{DO_NOTHING, MOVE_FILTERED, REMOVE_FILTERED};

    }

    public interface FilterMethods {
        String LESS_THAN = "Less than";
        String LESS_THAN_OR_EQUAL_TO = "Less than or equal to";
        String EQUAL_TO = "Equal to";
        String GREATER_THAN_OR_EQUAL_TO = "Greater than or equal to";
        String GREATER_THAN = "Greater than";
        String NOT_EQUAL_TO = "Not equal to";

        String[] ALL = new String[]{LESS_THAN,LESS_THAN_OR_EQUAL_TO,EQUAL_TO,GREATER_THAN_OR_EQUAL_TO,GREATER_THAN,NOT_EQUAL_TO};

    }

    static boolean testFilter(double testValue, double referenceValue, String filterMethod) {
        switch (filterMethod) {
            case FilterMethods.LESS_THAN:
                return testValue < referenceValue;
            case FilterMethods.LESS_THAN_OR_EQUAL_TO:
                return testValue <= referenceValue;
            case FilterMethods.EQUAL_TO:
                return testValue == referenceValue;
            case FilterMethods.GREATER_THAN_OR_EQUAL_TO:
                return testValue >= referenceValue;
            case FilterMethods.GREATER_THAN:
                return testValue > referenceValue;
            case FilterMethods.NOT_EQUAL_TO:
                return testValue != referenceValue;
        }

        return false;

    }

    static String getFilterMethodSymbol(String filterMethod) {
        switch (filterMethod) {
            case FilterMethods.LESS_THAN:
                return "<";
            case FilterMethods.LESS_THAN_OR_EQUAL_TO:
                return "<=";
            case FilterMethods.EQUAL_TO:
                return "==";
            case FilterMethods.GREATER_THAN_OR_EQUAL_TO:
                return ">=";
            case FilterMethods.GREATER_THAN:
                return ">";
            case FilterMethods.NOT_EQUAL_TO:
                return "!=";
        }

        return "";

    }

    static void processRemoval(Obj inputObject, ObjCollection outputObjects, Iterator<Obj> iterator) {
        inputObject.removeRelationships();
        if (outputObjects != null) {
            inputObject.setName(outputObjects.getName());
            outputObjects.add(inputObject);
        }
        iterator.remove();
    }

    static void showRemainingObjects(ObjCollection inputObjects) {
        HashMap<Integer,Float> hues = ColourFactory.getRandomHues(inputObjects);
        String mode = ConvertObjectsToImage.ColourModes.RANDOM_COLOUR;
        ImagePlus dispIpl = inputObjects.convertObjectsToImage("Objects", null, hues, 8,false).getImagePlus();
        dispIpl.setLut(LUTs.Random(true));
        dispIpl.setPosition(1,1,1);
        dispIpl.updateChannelAndDraw();
        dispIpl.show();

    }
}
