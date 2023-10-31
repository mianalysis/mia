package io.github.mianalysis.mia.lostandfound.images.transform.registration;

import java.util.HashMap;

import io.github.mianalysis.mia.module.images.transform.registration.AffineBlockMatching;
import io.github.mianalysis.mia.module.images.transform.registration.abstrakt.AbstractAffineRegistration;
import io.github.mianalysis.mia.module.lostandfound.LostAndFoundItem;

public class AffineBlockMatchingLostFound extends LostAndFoundItem {

    @Override
    public String getModuleName() {
        return new AffineBlockMatching(null).getClass().getSimpleName();
    }

    @Override
    public String[] getPreviousModuleNames() {
        return new String[]{"BlockMatchingRegistration"};
    }

    @Override
    public HashMap<String, String> getPreviousParameterNames() {
        HashMap<String,String> parameterNames = new HashMap<String,String>();
        parameterNames.put("Relative mode", AffineBlockMatching.REFERENCE_MODE);

        return parameterNames;
        
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
