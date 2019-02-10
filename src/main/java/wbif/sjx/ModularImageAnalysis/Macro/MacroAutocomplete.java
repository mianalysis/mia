package wbif.sjx.ModularImageAnalysis.Macro;

import net.imagej.legacy.plugin.MacroExtensionAutoCompletionPlugin;
import org.fife.ui.autocomplete.BasicCompletion;
import org.fife.ui.autocomplete.CompletionProvider;
import org.scijava.plugin.Plugin;

import java.util.ArrayList;
import java.util.List;


@Plugin(type = MacroExtensionAutoCompletionPlugin.class)
public class MacroAutocomplete implements MacroExtensionAutoCompletionPlugin {
    @Override
    public List<BasicCompletion> getCompletions(CompletionProvider completionProvider) {
        ArrayList<BasicCompletion> completions = new ArrayList<>();
        System.err.println("Loading anns");
        ArrayList<MacroOperation> macroOperations = MacroHandler.getMacroOperations();
        for (MacroOperation macroOperation:macroOperations) {
            String command = "Ext."+macroOperation.getName();
                System.err.println("    Command "+command);
            String description = macroOperation.getDescription();

            completions.add(new BasicCompletion(completionProvider,command,null,description));

        }

        return completions;

    }
}
