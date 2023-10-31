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
    private static ParameterControlFactory factory = null;

    protected abstract ParameterControl createAddParametersControl(ParameterGroup parameter);
    protected abstract ParameterControl createAdjustParameterGroupControl(AdjustParameters parameter);
    protected abstract ParameterControl createBooleanTypeControl(BooleanType parameter);
    protected abstract ParameterControl createChoiceTypeControl(ChoiceType parameter);
    protected abstract ParameterControl createFileFolderSelectionControl(FileFolderType parameter, String fileType);
    protected abstract ParameterControl createMessageControl(MessageP parameter, int controlHeight);
    protected abstract ParameterControl createModuleChoiceControl(ModuleP parameter);
    protected abstract ParameterControl createRefSelectorControl(ObjMeasurementSelectorP parameter);
    protected abstract ParameterControl createSeriesSelectorControl(TextType parameter);
    protected abstract ParameterControl createSeparatorControl(SeparatorP parameter);
    protected abstract ParameterControl createTextAreaControl(TextAreaP parameter, int controlHeight);
    protected abstract ParameterControl createTextTypeControl(TextType parameter);


    public static ParameterControlFactory getActive() {
        if (factory == null)
            MIA.log.writeWarning(
                    "No ParameterControlFactory specified.  Set using \"ParameterControlFactory.setActiveFactory(factory)\" method.");

        return factory;

    }

    public static void setActive(ParameterControlFactory newFactory) {
        factory = newFactory;

    }

    public static ParameterControl getAddParametersControl(ParameterGroup parameter) {
        if (factory == null)
            return null;

        return factory.createAddParametersControl(parameter);

    };

    public static ParameterControl getAdjustParameterGroupControl(AdjustParameters parameter) {
        if (factory == null)
            return null;

        return factory.createAdjustParameterGroupControl(parameter);

    };

    public static ParameterControl getBooleanControl(BooleanType parameter) {
        if (factory == null)
            return null;

        return factory.createBooleanTypeControl(parameter);

    };

    public static ParameterControl getChoiceTypeControl(ChoiceType parameter) {
        if (factory == null)
            return null;

        return factory.createChoiceTypeControl(parameter);

    };

    public static ParameterControl getFileFolderSelectionControl(FileFolderType parameter, String fileType) {
        if (factory == null)
            return null;

        return factory.createFileFolderSelectionControl(parameter, fileType);

    };

    public static ParameterControl getMessageTypeControl(MessageP parameter, int controlHeight) {
        if (factory == null)
            return null;

        return factory.createMessageControl(parameter, controlHeight);

    };

    public static ParameterControl getModuleChoiceControl(ModuleP parameter) {
        if (factory == null)
            return null;

        return factory.createModuleChoiceControl(parameter);

    };

    public static ParameterControl getRefSelectorControl(ObjMeasurementSelectorP parameter) {
        if (factory == null)
            return null;

        return factory.createRefSelectorControl(parameter);

    };

    public static ParameterControl getSeriesSelectorControl(TextType parameter) {
        if (factory == null)
            return null;

        return factory.createSeriesSelectorControl(parameter);

    };

    public static ParameterControl getSeparatorControl(SeparatorP parameter) {
        if (factory == null)
            return null;

        return factory.createSeparatorControl(parameter);

    };

    public static ParameterControl getTextAreaControl(TextAreaP parameter, int controlHeight) {
        if (factory == null)
            return null;

        return factory.createTextAreaControl(parameter, controlHeight);

    };

    public static ParameterControl getTextTypeControl(TextType parameter) {
        if (factory == null)
            return null;

        return factory.createTextTypeControl(parameter);

    };
}
