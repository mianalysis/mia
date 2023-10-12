package io.github.mianalysis.mia.process;

import io.github.mianalysis.mia.MIA;
import io.github.mianalysis.mia.object.parameters.AdjustParameters;
import io.github.mianalysis.mia.object.parameters.ModuleP;
import io.github.mianalysis.mia.object.parameters.ObjMeasurementSelectorP;
import io.github.mianalysis.mia.object.parameters.ParameterGroup;
import io.github.mianalysis.mia.object.parameters.SeparatorP;
import io.github.mianalysis.mia.object.parameters.abstrakt.BooleanType;
import io.github.mianalysis.mia.object.parameters.abstrakt.ChoiceType;
import io.github.mianalysis.mia.object.parameters.abstrakt.FileFolderType;
import io.github.mianalysis.mia.object.parameters.abstrakt.ParameterControl;
import io.github.mianalysis.mia.object.parameters.abstrakt.TextType;
import io.github.mianalysis.mia.object.parameters.text.MessageP;
import io.github.mianalysis.mia.object.parameters.text.TextAreaP;

public abstract class ParameterControlFactory {
    public static ParameterControlFactory factory = null;

    public abstract ParameterControl getAddParametersButton(ParameterGroup parameter);
    public abstract ParameterControl getAdjustParameterGroupButton(AdjustParameters parameter);
    public abstract ParameterControl getBooleanControl(BooleanType parameter);
    public abstract ParameterControl getChoiceTypeControl(ChoiceType parameter);
    public abstract ParameterControl getFileFolderParameter(FileFolderType parameter, String fileType);
    public abstract ParameterControl getMessageTypeControl(MessageP parameter, int controlHeight);
    public abstract ParameterControl getModuleChoice(ModuleP parameter);
    public abstract ParameterControl getRefSelectorParameter(ObjMeasurementSelectorP parameter);
    public abstract ParameterControl getSeriesSelector(TextType parameter);
    public abstract ParameterControl getSeparatorParameter(SeparatorP parameter);
    public abstract ParameterControl getTextAreaParameter(TextAreaP parameter, int controlHeight);
    public abstract ParameterControl getTextTypeControl(TextType parameter);


    public static ParameterControlFactory getActiveFactory() {
        if (factory == null)
            MIA.log.writeWarning(
                    "No ParameterControlFactory specified.  Set using \"ParameterControlFactory.setActiveFactory(factory)\" method.");

        return factory;

    }

    public static void setActiveFactory(ParameterControlFactory newFactory) {
        factory = newFactory;

    }
}
