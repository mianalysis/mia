package wbif.sjx.ModularImageAnalysis.Macro;

import ij.macro.ExtensionDescriptor;
import ij.macro.MacroExtension;
import wbif.sjx.ModularImageAnalysis.Module.Module;
import wbif.sjx.ModularImageAnalysis.Object.Workspace;
import wbif.sjx.ModularImageAnalysis.Process.ModuleReader;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Set;

public class MacroHandler implements MacroExtension {
    private static ArrayList<MacroOperation> macroOperations = new ArrayList<>();
    private static Workspace workspace;


    public MacroHandler() {
        try {
            workspace = new Workspace(0,File.createTempFile("Temp","File"),0);
        } catch (IOException e) {
            e.printStackTrace();
        }

        this.macroOperations= initialiseMacroOperations();

    }

    @Override
    public String handleExtension(String s, Object[] objects) {
        // Getting MacroOperation with matching name
        for (MacroOperation macroOperation:macroOperations) {
            if (macroOperation.name.equals(s)) {
                // Perform operation
                macroOperation.action(objects,workspace);
                return null;
            }
        }

        // Iterating over all
        return null;

    }

    @Override
    public ExtensionDescriptor[] getExtensionFunctions() {
        ExtensionDescriptor[] descriptors = new ExtensionDescriptor[macroOperations.size()];
        for (int i=0;i<macroOperations.size();i++) {
            descriptors[i] = (ExtensionDescriptor) macroOperations.get(i);
        }

        return descriptors;

    }

    private ArrayList<MacroOperation> initialiseMacroOperations() {
        // Using Reflections to get all Modules
        Set<Class<? extends Module>> modules = ModuleReader.getModules(false);

        // Iterating over each Module, adding MacroOperations to an ArrayList
        ArrayList<MacroOperation> macroOperations = new ArrayList<>();
        for (Class<? extends Module> module : modules) {
            try {
                ArrayList<MacroOperation> currentOperations = ((Module) ((Class) module).newInstance()).getMacroOperations(this);
                if (currentOperations != null) macroOperations.addAll(currentOperations);
            } catch (InstantiationException | IllegalAccessException e) {
                e.printStackTrace();
            }
        }

        return macroOperations;

    }

    public static Workspace getWorkspace() {
        return workspace;
    }

    public static void setWorkspace(Workspace workspace) {
        MacroHandler.workspace = workspace;
    }

    public static ArrayList<MacroOperation> getMacroOperations() {
        return macroOperations;
    }
}
