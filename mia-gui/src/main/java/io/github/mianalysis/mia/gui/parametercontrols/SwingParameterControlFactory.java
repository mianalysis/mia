package io.github.mianalysis.mia.gui.parametercontrols;

import java.awt.Color;

import io.github.mianalysis.mia.object.parameters.AdjustParameters;
import io.github.mianalysis.mia.object.parameters.GenericButtonP;
import io.github.mianalysis.mia.object.parameters.ModuleP;
import io.github.mianalysis.mia.object.parameters.ObjMeasurementSelectorP;
import io.github.mianalysis.mia.object.parameters.ParameterGroup;
import io.github.mianalysis.mia.object.parameters.ParameterState;
import io.github.mianalysis.mia.object.parameters.SeparatorP;
import io.github.mianalysis.mia.object.parameters.abstrakt.BooleanType;
import io.github.mianalysis.mia.object.parameters.abstrakt.ChoiceType;
import io.github.mianalysis.mia.object.parameters.abstrakt.FileFolderType;
import io.github.mianalysis.mia.object.parameters.abstrakt.ParameterControl;
import io.github.mianalysis.mia.object.parameters.abstrakt.TextType;
import io.github.mianalysis.mia.object.parameters.text.MessageP;
import io.github.mianalysis.mia.object.parameters.text.TextAreaP;
import io.github.mianalysis.mia.object.system.Colours;
import io.github.mianalysis.mia.process.ParameterControlFactory;

public class SwingParameterControlFactory extends ParameterControlFactory {
    @Override
    public ParameterControl getAddParametersButton(ParameterGroup parameter) {
        return new AddParametersButton(parameter);
    }

    @Override
    public ParameterControl getAdjustParameterGroupButton(AdjustParameters parameter) {
        return new AdjustParameterGroupButton(parameter);
    }

    @Override
    public ParameterControl getBooleanControl(BooleanType parameter) {
        return new BooleanParameter(parameter);
    }

    @Override
    public ParameterControl getChoiceTypeControl(ChoiceType parameter) {
        return new ChoiceArrayParameter(parameter);
    }

    @Override
    public ParameterControl getFileFolderParameter(FileFolderType parameter, String fileType) {
        return new FileParameter(parameter, fileType);
    }

    @Override
    public ParameterControl getMessageTypeControl(MessageP parameter, int controlHeight) {
        return new MessageArea(parameter, controlHeight);
    }

    @Override
    public ParameterControl getModuleChoice(ModuleP parameter) {
        return new ModuleChoiceParameter(parameter);
    }

    @Override
    public ParameterControl getRefSelectorParameter(ObjMeasurementSelectorP parameter) {
        return new RefSelectorParameter(parameter);
    }

    @Override
    public ParameterControl getSeparatorParameter(SeparatorP parameter) {
        return new SeparatorParameter(parameter);
    }

    @Override
    public ParameterControl getSeriesSelector(TextType parameter) {
        return new SeriesSelector(parameter);
    }

    @Override
    public ParameterControl getTextAreaParameter(TextAreaP parameter, int controlHeight) {
        return new TextAreaParameter(parameter, controlHeight);
    }

    @Override
    public ParameterControl getTextTypeControl(TextType parameter) {
        return new TextParameter(parameter);
    }

    public static ParameterControl getGenericButton(GenericButtonP parameter) {
        return new GenericButton(parameter);
    }

    public static Color getColor(ParameterState state, boolean isDark) {
        switch (state) {
            case NORMAL:
            default:
                if (isDark)
                    return Color.BLACK;
                else
                    return Color.WHITE;
            case MESSAGE:
                return Colours.getDarkBlue(isDark);
            case WARNING:
                return Colours.getOrange(isDark);
            case ERROR:
                return Colours.getRed(isDark);
        }
    }
}
