package wbif.sjx.ModularImageAnalysis.Macro;

import ij.IJ;
import ij.macro.Functions;
import ij.plugin.PlugIn;
import org.scijava.command.Command;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;
import wbif.sjx.ModularImageAnalysis.Process.DependencyValidator;

//public class EnableExtensions implements PlugIn {
//    private static boolean enabled = false;
//
//    @Override
//    public void run(String s) {
//        // Checking necessary dependencies are available
//        DependencyValidator.run();
//
//        // Setting up macro extension
//        if (!enabled)
//        enabled = true;
//
//    }
//}

@Plugin(type = Command.class, menuPath = "Plugins>Bristol WBIF>Enable Ext")
public class EnableExtensions implements Command {
    @Override
    public void run() {
        MacroHandler macroHandler = MacroHandler.getMacroHandler();

        if (!IJ.macroRunning()) {
            IJ.error("Cannot install extensions from outside a macro.");
            return;
        }

        Functions.registerExtensions(macroHandler);
        System.err.println("Registered MIA functions");

    }
}