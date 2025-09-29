package io.github.mianalysis.mia.module.system;

import org.scijava.Priority;
import org.scijava.plugin.Plugin;

import io.github.mianalysis.mia.module.Categories;
import io.github.mianalysis.mia.module.Category;
import io.github.mianalysis.mia.module.Module;
import io.github.mianalysis.mia.module.Modules;
import io.github.mianalysis.mia.module.inputoutput.abstrakt.AbstractSaver;
import io.github.mianalysis.mia.object.WorkspaceI;
import io.github.mianalysis.mia.object.parameters.ChoiceP;
import io.github.mianalysis.mia.object.parameters.ParameterState;
import io.github.mianalysis.mia.object.parameters.Parameters;
import io.github.mianalysis.mia.object.parameters.SeparatorP;
import io.github.mianalysis.mia.object.parameters.text.MessageP;
import io.github.mianalysis.mia.object.parameters.text.TextAreaP;
import io.github.mianalysis.mia.object.refs.collections.ImageMeasurementRefs;
import io.github.mianalysis.mia.object.refs.collections.MetadataRefs;
import io.github.mianalysis.mia.object.refs.collections.ObjMeasurementRefs;
import io.github.mianalysis.mia.object.refs.collections.ObjMetadataRefs;
import io.github.mianalysis.mia.object.refs.collections.ParentChildRefs;
import io.github.mianalysis.mia.object.refs.collections.PartnerRefs;
import io.github.mianalysis.mia.object.system.Status;

/**
 * Created by Stephen Cross on 24/09/2025.
 */

@Plugin(type = Module.class, priority = Priority.LOW, visible = true)
public class DisplayMessage extends AbstractSaver {
    public static final String MESSAGE_SEPARATOR = "Message controls";

    public static final String MESSAGE_TEXT = "Message text";

    public static final String MESSAGE_COLOUR = "Message colour";

    public static final String PREVIEW_SEPARATOR = "Message";

    public static final String MESSAGE_PREVIEW = "Message preview";

    public interface MessageColours {
        String BLACK = "Black (normal)";
        String BLUE = "Blue (message)";
        String ORANGE = "Orange (warning)";
        String RED = "Red (error)";

        String[] ALL = new String[] { BLACK, BLUE, ORANGE, RED };

    }

    public DisplayMessage(Modules modules) {
        super("Display message", modules);
    }

    @Override
    public Category getCategory() {
        return Categories.SYSTEM;
    }

    @Override
    public String getVersionNumber() {
        return "1.0.0";
    }

    @Override
    public String getDescription() {
        return "Display a message in processing view.  Messages can be colour-coded according to state (e.g. warning, info, etc.)";
    }

    @Override
    public Status process(WorkspaceI workspace) {// Local access to this is required for the action listeners
        return Status.PASS;

    }

    @Override
    public void initialiseParameters() {
        parameters.add(new SeparatorP(MESSAGE_SEPARATOR, this));
        parameters.add(new TextAreaP(MESSAGE_TEXT, this, true));
        parameters.add(new ChoiceP(MESSAGE_COLOUR, this, MessageColours.BLUE, MessageColours.ALL));
        parameters.add(new SeparatorP(PREVIEW_SEPARATOR, this));
        parameters.add(new MessageP(MESSAGE_PREVIEW, this, ParameterState.NORMAL));

    }

    @Override
    public Parameters updateAndGetParameters() {
        ParameterState state = ParameterState.NORMAL;

        switch ((String) parameters.getValue(MESSAGE_COLOUR, null)) {
            case MessageColours.BLACK:
                state = ParameterState.NORMAL;
                break;

            case MessageColours.BLUE:
                state = ParameterState.MESSAGE;
                break;

            case MessageColours.ORANGE:
                state = ParameterState.WARNING;
                break;

            case MessageColours.RED:
                state = ParameterState.ERROR;
                break;
        }

        MessageP messageP = (MessageP) parameters.get(MESSAGE_PREVIEW);
        messageP.setState(state);
        messageP.setValue(parameters.getValue(MESSAGE_TEXT, null));

        return parameters;

    }

    @Override
    public ImageMeasurementRefs updateAndGetImageMeasurementRefs() {
        return null;
    }

    @Override
    public ObjMeasurementRefs updateAndGetObjectMeasurementRefs() {
        return null;
    }

    @Override
    public ObjMetadataRefs updateAndGetObjectMetadataRefs() {
        return null;
    }

    @Override
    public MetadataRefs updateAndGetMetadataReferences() {
        return null;
    }

    @Override
    public ParentChildRefs updateAndGetParentChildRefs() {
        return null;
    }

    @Override
    public PartnerRefs updateAndGetPartnerRefs() {
        return null;
    }

    @Override
    public boolean verify() {
        return true;
    }
}
