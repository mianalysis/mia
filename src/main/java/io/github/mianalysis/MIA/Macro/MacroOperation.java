package io.github.mianalysis.MIA.Macro;

import ij.macro.ExtensionDescriptor;
import ij.macro.MacroExtension;
import io.github.mianalysis.MIA.Module.Modules;
import io.github.mianalysis.MIA.Object.Workspace;

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

    public String getName() {
        return this.getClass().getSimpleName();
    }
    
    public abstract int[] getArgumentTypes();
    public abstract String action(Object[] objects, Workspace workspace, Modules modules);
    public abstract String getArgumentsDescription();
    public abstract String getDescription();

}
