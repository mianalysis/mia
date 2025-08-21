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
    protected ParameterControl createAddParametersControl(ParameterGroup parameter) {
        return new AddParametersButton(parameter);
    }

    @Override
    protected ParameterControl createAdjustParameterGroupControl(AdjustParameters parameter) {
        return new AdjustParameterGroupButton(parameter);
    }

    @Override
    protected ParameterControl createBooleanTypeControl(BooleanType parameter) {
        return new BooleanParameter(parameter);
    }

    @Override
    protected ParameterControl createChoiceTypeControl(ChoiceType parameter) {
        return new ChoiceArrayParameter(parameter);
    }

    @Override
    protected ParameterControl createFileFolderSelectionControl(FileFolderType parameter, String fileType) {
        return new FileParameter(parameter, fileType);
    }

    @Override
    protected ParameterControl createMessageControl(MessageP parameter, int controlHeight) {
        return new MessageArea(parameter, controlHeight);
    }

    @Override
    protected ParameterControl createModuleChoiceControl(ModuleP parameter) {
        return new ModuleChoiceParameter(parameter);
    }

    @Override
    protected ParameterControl createRefSelectorControl(ObjMeasurementSelectorP parameter) {
        return new RefSelectorParameter(parameter);
    }

    @Override
    protected ParameterControl createSeparatorControl(SeparatorP parameter) {
        return new SeparatorParameter(parameter);
    }

    @Override
    protected ParameterControl createSeriesSelectorControl(TextType parameter) {
        return new SeriesSelector(parameter);
    }

    @Override
    protected ParameterControl createTextAreaControl(TextAreaP parameter, int controlHeight) {
        return new TextAreaParameter(parameter, controlHeight);
    }

    @Override
    protected ParameterControl createTextTypeControl(TextType parameter) {
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
                    return Color.WHITE;
                else
                    return Color.BLACK;
            case MESSAGE:
                return Colours.getDarkBlue(isDark);
            case WARNING:
                return Colours.getOrange(isDark);
            case ERROR:
                return Colours.getRed(isDark);
        }
    }
}
