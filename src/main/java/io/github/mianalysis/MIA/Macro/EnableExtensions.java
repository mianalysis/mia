package io.github.mianalysis.MIA.Macro;

import org.scijava.command.Command;
import org.scijava.plugin.Plugin;

import ij.IJ;
import ij.macro.Functions;
import io.github.mianalysis.MIA.Process.DependencyValidator;


@Plugin(type = Command.class, menuPath = "Plugins>MIA>Enable MIA Extensions")
public class EnableExtensions implements Command {
    @Override
    public void run() {
        // Run the dependency validator.  If updates were required, return.
        if (DependencyValidator.run()) return;

        MacroHandler macroHandler = MacroHandler.getMacroHandler();

        if (!IJ.macroRunning()) {
            IJ.error("Cannot install extensions from outside a macro.");
            return;
        }

        Functions.registerExtensions(macroHandler);

    }
}