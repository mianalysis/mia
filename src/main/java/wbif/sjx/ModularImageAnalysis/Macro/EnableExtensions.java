package wbif.sjx.ModularImageAnalysis.Macro;

import ij.macro.Functions;
import ij.plugin.PlugIn;
import wbif.sjx.ModularImageAnalysis.Process.DependencyValidator;

public class EnableExtensions implements PlugIn {
    private static boolean enabled = false;

    @Override
    public void run(String s) {
        // Checking necessary dependencies are available
        DependencyValidator.run();

        // Setting up macro extension
        if (!enabled) Functions.registerExtensions(new MacroHandler());
        enabled = true;

    }
}
