package io.github.mianalysis.mia.macro.objectprocessing;

import ij.macro.MacroExtension;
import io.github.mianalysis.mia.MIA;
import io.github.mianalysis.mia.macro.MacroOperation;
import io.github.mianalysis.mia.module.Modules;
import io.github.mianalysis.mia.module.objectprocessing.identification.IdentifyObjects;
import io.github.mianalysis.mia.object.Workspace;

public class MIA_IdentifyObjects extends MacroOperation {
    public MIA_IdentifyObjects(MacroExtension theHandler) {
        super(theHandler);
    }

    @Override
    public int[] getArgumentTypes() {
        return new int[]{ARG_STRING,ARG_STRING,ARG_NUMBER,ARG_NUMBER,ARG_NUMBER,ARG_NUMBER};
    }

    @Override
    public String action(Object[] objects, Workspace workspace, Modules modules) {
        IdentifyObjects identifyObjects = new IdentifyObjects(modules);

        identifyObjects.updateParameterValue(IdentifyObjects.INPUT_IMAGE,objects[0]);
        identifyObjects.updateParameterValue(IdentifyObjects.OUTPUT_OBJECTS,objects[1]);
        identifyObjects.updateParameterValue(IdentifyObjects.BINARY_LOGIC,((double) objects[2] == 1) ? IdentifyObjects.BinaryLogic.BLACK_BACKGROUND : IdentifyObjects.BinaryLogic.WHITE_BACKGROUND);
        identifyObjects.updateParameterValue(IdentifyObjects.SINGLE_OBJECT,((double) objects[3] == 1));

        if ((double) objects[4] == 6) {
            identifyObjects.updateParameterValue(IdentifyObjects.CONNECTIVITY,IdentifyObjects.Connectivity.SIX);
        } else if ((double) objects[4] == 26) {
            identifyObjects.updateParameterValue(IdentifyObjects.CONNECTIVITY,IdentifyObjects.Connectivity.TWENTYSIX);
        } else {
            MIA.log.writeWarning("Connectivity must be set to either 6 or 26.");
            return null;
        }

        identifyObjects.setShowOutput((double) objects[5] == 1);

        identifyObjects.process(workspace);

        return null;

    }

    @Override
    public String getArgumentsDescription() {
        return "String inputImageName, String outputObjectsName, boolean whiteBackground, boolean singleObject, "+
                "int connectivity, boolean showObjects";
    }

    @Override
    public String getDescription() {
        return "Uses connected component labelling to convert a binary image to objects.  Connectivity must be set to "+
                "either 6 or 26.";
    }
}
