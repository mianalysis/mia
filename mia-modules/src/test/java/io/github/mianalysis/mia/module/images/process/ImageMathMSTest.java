package io.github.mianalysis.mia.module.images.process;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.stream.Stream;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import com.drew.lang.annotations.Nullable;

import ij.IJ;
import ij.ImagePlus;
import io.github.mianalysis.enums.BitDepth;
import io.github.mianalysis.enums.Dimension;
import io.github.mianalysis.enums.OutputMode;
import io.github.mianalysis.mia.module.Modules;
import io.github.mianalysis.mia.object.WorkspaceI;
import io.github.mianalysis.mia.object.Workspaces;
import io.github.mianalysis.mia.object.image.ImageFactories;
import io.github.mianalysis.mia.object.image.ImageFactoryI;
import io.github.mianalysis.mia.object.image.ImageI;
import io.github.mianalysis.mia.object.measurements.MeasurementFactories;
import io.github.mianalysis.mia.object.system.Status;

public class ImageMathMSTest {
    interface Operation {
    }

    enum VariableOperation implements Operation {
        OADD,
        ODIVIDE,
        OMULTIPLY,
        OSUBTRACT
    }

    enum SingleOperation implements Operation {
        OABSOLUTE,
        OSQUARE,
        OSQRT
    }

    enum ValueSource {
        SFIXED,
        SIMAGE
    }

    enum Convert32 {
        C32T,
        C32F
    }

    public static Stream<Arguments> variableOperationInputProvider() {
        Stream.Builder<Arguments> argumentBuilder = Stream.builder();
        for (Operation operation : VariableOperation.values())
            for (OutputMode outputMode : OutputMode.values())
                for (ImageFactoryI imageFactory : ImageFactories.getFactories().values())
                    for (ValueSource valueSource : ValueSource.values())
                        argumentBuilder.add(Arguments.of(operation, outputMode, imageFactory, valueSource));

        return argumentBuilder.build();

    }

    public static Stream<Arguments> dimVariableOperationInputProvider() {
        Stream.Builder<Arguments> argumentBuilder = Stream.builder();
        for (Dimension dimension : Dimension.values())
            for (Operation operation : VariableOperation.values())
                for (OutputMode outputMode : OutputMode.values())
                    for (ImageFactoryI imageFactory : ImageFactories.getFactories().values())
                        for (ValueSource valueSource : ValueSource.values())
                            argumentBuilder.add(Arguments.of(dimension, operation, outputMode, imageFactory, valueSource));

        return argumentBuilder.build();

    }

    public static Stream<Arguments> bitDepthVariableOperationInputProvider() {
        Stream.Builder<Arguments> argumentBuilder = Stream.builder();
        for (BitDepth bitDepth : BitDepth.values())
            for (Operation operation : VariableOperation.values())
                for (OutputMode outputMode : OutputMode.values())
                    for (ImageFactoryI imageFactory : ImageFactories.getFactories().values())
                        for (ValueSource valueSource : ValueSource.values())
                            for (Convert32 convert32 : Convert32.values())
                                argumentBuilder.add(Arguments.of(bitDepth, operation, outputMode, convert32, imageFactory,
                                        valueSource));

        return argumentBuilder.build();

    }

    public static Stream<Arguments> dimSingleOperationInputProvider() {
        Stream.Builder<Arguments> argumentBuilder = Stream.builder();
        for (Dimension dimension : Dimension.values())
            for (Operation operation : SingleOperation.values())
                for (OutputMode outputMode : OutputMode.values())
                    for (ImageFactoryI imageFactory : ImageFactories.getFactories().values())
                        argumentBuilder.add(Arguments.of(dimension, operation, outputMode, imageFactory));

        return argumentBuilder.build();

    }

    public static Stream<Arguments> bitDepthSingleOperationInputProvider() {
        Stream.Builder<Arguments> argumentBuilder = Stream.builder();
        for (BitDepth bitDepth : BitDepth.values())
            for (Operation operation : SingleOperation.values())
                for (OutputMode outputMode : OutputMode.values())
                    for (ImageFactoryI imageFactory : ImageFactories.getFactories().values())
                        for (Convert32 convert32 : Convert32.values())
                            argumentBuilder.add(Arguments.of(bitDepth, operation, outputMode, convert32, imageFactory));

        return argumentBuilder.build();

    }

    @ParameterizedTest
    @MethodSource("dimVariableOperationInputProvider")
    void test8BitVariableOperation3p2(Dimension dimension, Operation operation, OutputMode outputMode,
            ImageFactoryI imageFactory, ValueSource valueSource)
            throws UnsupportedEncodingException {
        runTest(dimension, BitDepth.B8, operation, 3.2, outputMode, Convert32.C32F, imageFactory, valueSource);

    }

    @ParameterizedTest
    @MethodSource("variableOperationInputProvider")
    void test8bitD3ZVariableOperationm3p2(Operation operation, OutputMode outputMode, ImageFactoryI imageFactory,
            ValueSource valueSource)
            throws UnsupportedEncodingException {
        runTest(Dimension.D3Z, BitDepth.B8, operation, -3.2, outputMode, Convert32.C32F, imageFactory, valueSource);

    }

    @ParameterizedTest
    @MethodSource("variableOperationInputProvider")
    void test8bitD3ZVariableOperationNaN(Operation operation, OutputMode outputMode, ImageFactoryI imageFactory,
            ValueSource valueSource)
            throws UnsupportedEncodingException {
        runTest(Dimension.D3Z, BitDepth.B8, operation, Double.NaN, outputMode, Convert32.C32F, imageFactory, valueSource);

    }

    @ParameterizedTest
    @MethodSource("bitDepthVariableOperationInputProvider")
    void testBitDepthD3ZVariableOperration3p2(BitDepth bitDepth, Operation operation, OutputMode outputMode,
            Convert32 convert32, ImageFactoryI imageFactory, ValueSource valueSource)
            throws UnsupportedEncodingException {
        runTest(Dimension.D3Z, bitDepth, operation, 3.2, outputMode, convert32, imageFactory, valueSource);

    }

    @ParameterizedTest
    @MethodSource("dimSingleOperationInputProvider")
    void test8BitSingleOperation(Dimension dimension, Operation operation, OutputMode outputMode, ImageFactoryI imageFactory)
            throws UnsupportedEncodingException {
        runTest(dimension, BitDepth.B8, operation, Double.NaN, outputMode, Convert32.C32F, imageFactory, null);

    }

    @ParameterizedTest
    @MethodSource("bitDepthSingleOperationInputProvider")
    void testD3ZSingleOperation(BitDepth bitDepth, Operation operation, OutputMode outputMode, Convert32 convert32,
            ImageFactoryI imageFactory)
            throws UnsupportedEncodingException {
        runTest(Dimension.D3Z, bitDepth, operation, Double.NaN, outputMode, convert32, imageFactory, null);

    }

    public static void runTest(Dimension dimension, BitDepth bitDepth, Operation operation, double value,
            OutputMode outputMode, Convert32 convert32, ImageFactoryI imageFactory, @Nullable ValueSource valueSource)
            throws UnsupportedEncodingException {
        boolean applyToInput = outputMode.equals(OutputMode.APPLY_TO_INPUT);

        // Checks input image and expected images are available. If not found, the test
        // skips
        String inputName = "/msimages/noisygradient/NoisyGradient_" + dimension + "_" + bitDepth + ".zip";
        assumeTrue(FilterImageMSTest.class.getResource(inputName) != null);

        // Doing the main part of the test
        // Creating a new workspace
        Workspaces workspaces = new Workspaces();
        WorkspaceI workspace = workspaces.getNewWorkspace(null, 1);

        // Loading the test image and adding to workspace
        String inputPath = URLDecoder.decode(FilterImageMSTest.class.getResource(inputName).getPath(), "UTF-8");
        ImagePlus ipl = IJ.openImage(inputPath);
        ImageI image = imageFactory.create("Test_image", ipl);
        workspace.addImage(image);

        // Initialising module and setting parameters
        ImageMath imageMath = new ImageMath(new Modules());
        imageMath.updateParameterValue(ImageMath.INPUT_IMAGE, "Test_image");
        imageMath.updateParameterValue(ImageMath.APPLY_TO_INPUT, applyToInput);
        if (!applyToInput)
            imageMath.updateParameterValue(ImageMath.OUTPUT_IMAGE, "Test_output");

        switch (convert32) {
            case C32F:
                imageMath.updateParameterValue(ImageMath.OUTPUT_32BIT, false);
                break;
            case C32T:
                imageMath.updateParameterValue(ImageMath.OUTPUT_32BIT, true);
                break;
        }

        String valueString = "";
        if (operation instanceof VariableOperation) {
            valueString = "_V" + value;
            switch ((VariableOperation) operation) {
                case OADD:
                    imageMath.updateParameterValue(ImageMath.CALCULATION_MODE, ImageMath.CalculationModes.ADD);
                    break;
                case ODIVIDE:
                    imageMath.updateParameterValue(ImageMath.CALCULATION_MODE, ImageMath.CalculationModes.DIVIDE);
                    break;
                case OMULTIPLY:
                    imageMath.updateParameterValue(ImageMath.CALCULATION_MODE, ImageMath.CalculationModes.MULTIPLY);
                    break;
                case OSUBTRACT:
                    imageMath.updateParameterValue(ImageMath.CALCULATION_MODE, ImageMath.CalculationModes.SUBTRACT);
                    break;
            }

            switch (valueSource) {
                case SFIXED:
                    imageMath.updateParameterValue(ImageMath.VALUE_SOURCE, ImageMath.ValueSources.FIXED);
                    imageMath.updateParameterValue(ImageMath.MATH_VALUE, value);
                    break;
                case SIMAGE:
                    // We can still use the fixed value from the arguments, it's just assigned to
                    // the image.
                    image.addMeasurement(MeasurementFactories.getDefaultFactory().createMeasurement("MATH_VAL", value));
                    imageMath.updateParameterValue(ImageMath.VALUE_SOURCE, ImageMath.ValueSources.MEASUREMENT);
                    imageMath.updateParameterValue(ImageMath.IMAGE_FOR_MEASUREMENT, "Test_image");
                    imageMath.updateParameterValue(ImageMath.MEASUREMENT, "MATH_VAL");

                    // To make doubly-sure, the fixed value parameter is set to NaN
                    imageMath.updateParameterValue(ImageMath.MATH_VALUE, Double.NaN);
                    break;
            }
        }

        if (operation instanceof SingleOperation) {
            switch ((SingleOperation) operation) {
                case OABSOLUTE:
                    imageMath.updateParameterValue(ImageMath.CALCULATION_MODE, ImageMath.CalculationModes.ABSOLUTE);
                    break;
                case OSQRT:
                    imageMath.updateParameterValue(ImageMath.CALCULATION_MODE, ImageMath.CalculationModes.SQUAREROOT);
                    break;
                case OSQUARE:
                    imageMath.updateParameterValue(ImageMath.CALCULATION_MODE, ImageMath.CalculationModes.SQUARE);
                    break;
            }
        }

        // Loading the expected image
        String expectedName = "/msimages/imagemath/ImageMath_" + dimension + "_" + bitDepth + "_" + operation
                + valueString + "_" + convert32 + ".zip";
        assumeTrue(FilterImageMSTest.class.getResource(expectedName) != null);

        String expectedPath = URLDecoder.decode(FilterImageMSTest.class.getResource(expectedName).getPath(), "UTF-8");
        ImageI expectedImage = imageFactory.create("Expected", IJ.openImage(expectedPath));

        // Running Module
        Status status = imageMath.execute(workspace);
        assertEquals(Status.PASS, status);

        // Checking the images in the workspace
        if (applyToInput) {
            assertEquals(1, workspace.getImages().size());
            assertNotNull(workspace.getImage("Test_image"));

            ImageI outputImage = workspace.getImage("Test_image");
            assertEquals(expectedImage, outputImage);

        } else {
            assertEquals(2, workspace.getImages().size());
            assertNotNull(workspace.getImage("Test_image"));
            assertNotNull(workspace.getImage("Test_output"));

            ImageI outputImage = workspace.getImage("Test_output");
            assertEquals(expectedImage, outputImage);

        }
    }
}
