package io.github.mianalysis.mia.process.math;

import java.awt.Color;

/*
 * Uses the CIE color-matching algorithm.  Taken from https://www.baeldung.com/cs/rgb-color-light-frequency (accessed 2024-10-14)
 */
public class WavelengthToColorConverter {
    public static Color convert(double wavelengthNM) {
        // Calculating X value
        double factorXt1 = wavelengthNM < 442 ? 0.0624 : 0.0374;
        double Xt1 = (wavelengthNM - 442) * factorXt1;

        double factorXt2 = wavelengthNM < 599.8 ? 0.0264 : 0.0323;
        double Xt2 = (wavelengthNM - 599.8) * factorXt2;

        double factorXt3 = wavelengthNM < 501.1 ? 0.0490 : 0.0382;
        double Xt3 = (wavelengthNM - 501.1) * factorXt3;

        double X = 0.362 * Math.exp(-0.5 * Xt1 * Xt1) + 1.056 * Math.exp(-0.5 * Xt2 * Xt2)
                - 0.065 * Math.exp(-0.5 * Xt3 * Xt3);

        // Calculating Y value
        double factorYt1 = wavelengthNM < 568.8 ? 0.0213 : 0.0247;
        double Yt1 = (wavelengthNM - 568.8) * factorYt1;

        double factorYt2 = wavelengthNM < 530.9 ? 0.0613 : 0.0322;
        double Yt2 = (wavelengthNM - 530.9) * factorYt2;

        double Y = 0.821 * Math.exp(-0.5 * Yt1 * Yt1) + 0.286 * Math.exp(-0.5 * Yt2 * Yt2);

        // Calculating Z value
        double factorZt1 = wavelengthNM < 437.0 ? 0.0845 : 0.0278;
        double Zt1 = (wavelengthNM - 437.0) * factorZt1;

        double factorZt2 = wavelengthNM < 459.0 ? 0.0385 : 0.0725;
        double Zt2 = (wavelengthNM - 459.0) * factorZt2;

        double Z = 1.217 * Math.exp(-0.5 * Zt1 * Zt1) + 0.681 * Math.exp(-0.5 * Zt2 * Zt2);

        // Calculating RGB
        double red = 3.2406255 * X - 1.537208 * Y - 0.4986286 * Z;
        double green = -0.9689307 * X + 1.8757561 * Y + 0.0415175 * Z;
        double blue = 0.0557101 * X - 0.2040211 * Y + 1.0569959 * Z;

        // Conversion to 8-bit
        int rInt = convertTo8bit(red);
        int gInt = convertTo8bit(green);
        int bInt = convertTo8bit(blue);

        return new Color(rInt, gInt, bInt);

    }

    protected static int convertTo8bit(double value) {
        if (value <= 0)
            return 0;
        else if (value <= 0.0031308)
            return (int) Math.round(255 * value * 12.92);
        else if (value <= 1)
            return (int) Math.round(255 * (1.055 * Math.pow(value, 1 / 2.4) - 0.055));
        else
            return 255;
    }
}
