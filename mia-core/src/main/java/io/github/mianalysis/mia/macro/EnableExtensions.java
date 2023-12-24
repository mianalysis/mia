package io.github.mianalysis.mia.macro;

import java.util.List;

import org.scijava.command.Command;
import org.scijava.plugin.Plugin;
import org.scijava.plugin.PluginInfo;

import ij.IJ;
import ij.macro.Functions;
import ij.macro.MacroExtension;
import io.github.mianalysis.mia.MIA;
import io.github.mianalysis.mia.process.ClassHunter;
import io.github.mianalysis.mia.process.DependencyValidator;

@Plugin(type = Command.class, menuPath = "Plugins>ModularImageAnalysis (MIA)>Enable MIA Extensions", visible = true)
public class EnableExtensions implements Command {
    @Override
    public void run() {
        // Run the dependency validator. If updates were required, return.
        if (DependencyValidator.run())
            return;

        MacroHandler macroHandler = MacroHandler.getMacroHandler();

        if (!IJ.macroRunning()) {
            MIA.log.writeMessage("To use macro extensions, please add run(\"Enable MIA Extensions\"); to your macro.");
            MIA.log.writeMessage("The following is a list of available macro operations:");
            List<PluginInfo<MacroOperation>> macroOperationInfos = ClassHunter.getPlugins(MacroOperation.class);
            for (PluginInfo<MacroOperation> macroOperationInfo : macroOperationInfos) {
                try {
                    MacroOperation macroOperation = (MacroOperation) Class.forName(macroOperationInfo.getClassName())
                            .getDeclaredConstructor(MacroExtension.class)
                            .newInstance(macroHandler);
                    String name = macroOperation.getClass().getSimpleName();
                    String arguments = macroOperation.getArgumentsDescription();
                    if (arguments.length() == 0)
                        arguments = "[None]";
                    String description = macroOperation.getDescription();
                    MIA.log.writeMessage(
                            "    - Name: " + name + ", Arguments: " + arguments + ", Description: " + description);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            return;
            
        }

        Functions.registerExtensions(macroHandler);

    }
}