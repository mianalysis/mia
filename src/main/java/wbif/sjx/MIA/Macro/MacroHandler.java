package wbif.sjx.MIA.Macro;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

import ij.macro.ExtensionDescriptor;
import ij.macro.MacroExtension;
import wbif.sjx.MIA.MIA;
import wbif.sjx.MIA.Module.ModuleCollection;
import wbif.sjx.MIA.Object.Workspace;
import wbif.sjx.MIA.Object.WorkspaceCollection;
import wbif.sjx.MIA.Process.ClassHunter;

public class MacroHandler implements MacroExtension {
    private static MacroHandler macroHandler = null;
    private static ArrayList<MacroOperation> macroOperations = null;
    private static Workspace workspace;
    private static ModuleCollection modules;

    // Constructor is private to prevent instantiation
    public MacroHandler(){};

    public static MacroHandler getMacroHandler() {
        if (macroHandler == null) {
            macroHandler = new MacroHandler();

            try {
                if (workspace == null) {
                    workspace = new WorkspaceCollection().getNewWorkspace(File.createTempFile("Temp", "File"), 0);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            
            macroOperations = initialiseMacroOperations(macroHandler);
            
        }

        return macroHandler;

    }

    @Override
    public String handleExtension(String s, Object[] objects) {
        // Getting MacroOperation with matching name
        for (MacroOperation macroOperation:macroOperations) {
            if (macroOperation.name.equals(s)) {
                // Perform operation
                return macroOperation.action(objects,workspace,modules);

            }
        }

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

    private static ArrayList<MacroOperation> initialiseMacroOperations(MacroHandler macroHandler) {
        // Using Reflections to get all MacroOperations
        List<String> clazzes = new ClassHunter<MacroOperation>().getClasses(MacroOperation.class);

        // Iterating over each Module, adding MacroOperations to an ArrayList
        ArrayList<MacroOperation> macroOperations = new ArrayList<>();
        for (String clazz : clazzes) {
            try {
                macroOperations.add((MacroOperation) Class.forName(clazz).getDeclaredConstructor(MacroExtension.class)
                        .newInstance(macroHandler));
            } catch (InstantiationException | IllegalAccessException | NoSuchMethodException | InvocationTargetException
                    | ClassNotFoundException e) {
                MIA.log.writeError(e);
            }
        }

        return macroOperations;

    }

    public static ModuleCollection getModules() {
        return modules;
    }

    public static void setModules(ModuleCollection modules) {
        MacroHandler.modules = modules;
    }

    public static Workspace getWorkspace() {
        return workspace;
    }

    public static void setWorkspace(Workspace workspace) {
        MacroHandler.workspace = workspace;
    }

    public static ArrayList<MacroOperation> getMacroOperations() {
        if (macroOperations == null) macroOperations = initialiseMacroOperations(getMacroHandler());
        return macroOperations;
    }
}
