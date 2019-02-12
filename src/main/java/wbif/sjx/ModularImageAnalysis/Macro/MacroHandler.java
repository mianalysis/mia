package wbif.sjx.ModularImageAnalysis.Macro;

import ij.macro.ExtensionDescriptor;
import ij.macro.MacroExtension;
import wbif.sjx.ModularImageAnalysis.Object.Workspace;
import wbif.sjx.ModularImageAnalysis.Process.ClassHunter;

import javax.crypto.Mac;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Set;

public class MacroHandler implements MacroExtension {
    private static MacroHandler macroHandler = null;
    private static ArrayList<MacroOperation> macroOperations = null;
    private static Workspace workspace;

    public static MacroHandler getMacroHandler() {
        if (macroHandler == null) {
            macroHandler = new MacroHandler();
            try {
                workspace = new Workspace(0,File.createTempFile("Temp","File"),0);
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
                return macroOperation.action(objects,workspace);

            }
        }

        return null;

    }

    @Override
    public ExtensionDescriptor[] getExtensionFunctions() {
        ExtensionDescriptor[] descriptors = new ExtensionDescriptor[macroOperations.size()];
        for (int i=0;i<macroOperations.size();i++) {
            descriptors[i] = (ExtensionDescriptor) macroOperations.get(i);
            System.err.println("Loading extension: "+descriptors[i].name);
        }

        return descriptors;

    }

    private static ArrayList<MacroOperation> initialiseMacroOperations(MacroHandler macroHandler) {
        System.err.println("Searching for MIA macros");
        // Using Reflections to get all MacroOperations
        Set<Class<? extends MacroOperation>> clazzes= new ClassHunter<MacroOperation>().getClasses(MacroOperation.class,false);

        // Iterating over each Module, adding MacroOperations to an ArrayList
        ArrayList<MacroOperation> macroOperations = new ArrayList<>();
        for (Class<? extends MacroOperation> clazz:clazzes) {
            try {
                macroOperations.add(clazz.getDeclaredConstructor(MacroExtension.class).newInstance(macroHandler));
            } catch (InstantiationException | IllegalAccessException | NoSuchMethodException | InvocationTargetException e) {
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

    public ArrayList<MacroOperation> getMacroOperations() {
        return macroOperations;
    }
}
