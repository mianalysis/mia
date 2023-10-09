package io.github.mianalysis.mia.process.math;

import org.apache.commons.math3.fitting.leastsquares.ParameterValidator;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.RealVector;

/**
 * Created by sc13967 on 21/06/2017.
 */
public class Validator implements ParameterValidator {
    private RealMatrix limits;

    public Validator(RealMatrix limits) {
        this.limits = limits;

    }

    @Override
    public RealVector validate(RealVector realVector) {
        for (int i=0;i<realVector.getDimension();i++) {
            if (realVector.getEntry(i) < limits.getEntry(i,0)) {
                realVector.setEntry(i,limits.getEntry(i,0));
            }

            if (realVector.getEntry(i) > limits.getEntry(i,1)) {
                realVector.setEntry(i,limits.getEntry(i,1));
            }
        }

        return realVector;

    }
}
