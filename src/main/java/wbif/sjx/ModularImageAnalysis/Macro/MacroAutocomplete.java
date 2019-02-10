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
        ArrayList<MacroOperation> macroOperations = new MacroHandler().getMacroOperations();
        for (MacroOperation macroOperation:macroOperations) {
            String command = "Ext."+macroOperation.getName()+" ("+macroOperation.getArgumentsDescription()+")";
            String description = macroOperation.getDescription();

            completions.add(new BasicCompletion(completionProvider,command,null,description));

        }

        return completions;

    }
}
