package wbif.sjx.ModularImageAnalysis.Macro;

import ij.macro.ExtensionDescriptor;
import ij.macro.MacroExtension;
import wbif.sjx.ModularImageAnalysis.Object.Workspace;

public abstract class MacroOperation extends ExtensionDescriptor {
    // Using the same numbering as MacroExtension
    protected static int ARG_STRING = 1;
    protected static int ARG_NUMBER = 2;
    protected static int ARG_ARRAY = 4;
    protected static int ARG_OUTPUT = 16;
    protected static int ARG_OPTIONAL = 32;

    public MacroOperation(MacroExtension theHandler) {
        super("", null, theHandler);
        name = getName();
        argTypes = getArgumentTypes();
    }

    public abstract String getName();
    public abstract int[] getArgumentTypes();
    public abstract void action(Object[] objects, Workspace workspace);
    public abstract String getArgumentsDescription();
    public abstract String getDescription();

}
