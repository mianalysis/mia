package io.github.mianalysis.mia.macro;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

import ij.macro.ExtensionDescriptor;
import ij.macro.MacroExtension;
import io.github.mianalysis.mia.MIA;
import io.github.mianalysis.mia.module.Modules;
import io.github.mianalysis.mia.object.Workspace;
import io.github.mianalysis.mia.object.Workspaces;
import io.github.mianalysis.mia.process.ClassHunter;

public class MacroHandler implements MacroExtension {
    private static MacroHandler macroHandler = null;
    private static ArrayList<MacroOperation> macroOperations = null;
    private static Workspace workspace;
    private static Modules modules;

    // Constructor is private to prevent instantiation
    public MacroHandler(){};

    public static MacroHandler getMacroHandler() {
        if (macroHandler == null) {
            macroHandler = new MacroHandler();

            try {
                if (workspace == null) {
                    workspace = new Workspaces().getNewWorkspace(File.createTempFile("Temp", "File"), 0);
                }
            } catch (IOException e) {
                MIA.log.writeError(e);
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
        List<String> clazzes = new ClassHunter<MacroOperation>().getClassNames(MacroOperation.class);

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

    public static Modules getModules() {
        return modules;
    }

    public static void setModules(Modules modules) {
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
