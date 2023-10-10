package io.github.mianalysis.mia.gui.parametercontrols;

import io.github.mianalysis.mia.object.parameters.AdjustParameters;
import io.github.mianalysis.mia.object.parameters.abstrakt.BooleanType;
import io.github.mianalysis.mia.object.parameters.abstrakt.ChoiceType;
import io.github.mianalysis.mia.object.parameters.abstrakt.FileFolderType;
import io.github.mianalysis.mia.object.parameters.abstrakt.ParameterControl;
import io.github.mianalysis.mia.object.parameters.abstrakt.TextType;
import io.github.mianalysis.mia.object.parameters.text.MessageP;
import io.github.mianalysis.mia.object.parameters.text.TextAreaP;
import io.github.mianalysis.mia.process.ParameterControlFactory;

public class SwingParameterControlFactory extends ParameterControlFactory {
    @Override
    public ParameterControl getAdjustParameterGroupButton(AdjustParameters parameters) {
        return new AdjustParameterGroupButton(parameters);
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
}
