package io.github.mianalysis.mia.lostandfound.images.transform.registration;

import java.util.HashMap;

import org.scijava.Priority;
import org.scijava.plugin.Plugin;

import io.github.mianalysis.mia.module.images.transform.registration.AffineManual;
import io.github.mianalysis.mia.module.images.transform.registration.abstrakt.AbstractAffineRegistration;
import io.github.mianalysis.mia.module.lostandfound.LostAndFoundItem;

@Plugin(type = LostAndFoundItem.class, priority = Priority.LOW, visible = true)
public class AffineManualLostFound extends LostAndFoundItem {

    @Override
    public String getModuleName() {
        return new AffineManual(null).getClass().getSimpleName();
    }

    @Override
    public String[] getPreviousModuleNames() {
        return new String[] { "ManualRegistration" };
    }

    @Override
    public HashMap<String, String> getPreviousParameterNames() {
        return new HashMap<String, String>();
    }

    @Override
    public HashMap<String, HashMap<String, String>> getPreviousParameterValues() {
        HashMap<String, String> values = null;
        HashMap<String, HashMap<String, String>> parameterValues = null;

        values = new HashMap<>();
        values.put("Affine", AbstractAffineRegistration.TransformationModes.AFFINE);
        values.put("Rigid", AbstractAffineRegistration.TransformationModes.RIGID);
        values.put("Similarity", AbstractAffineRegistration.TransformationModes.SIMILARITY);
        values.put("Translation", AbstractAffineRegistration.TransformationModes.TRANSLATION);
        parameterValues = new HashMap<>();
        parameterValues.put(AbstractAffineRegistration.TRANSFORMATION_MODE, values);

        return parameterValues;

    }
}
