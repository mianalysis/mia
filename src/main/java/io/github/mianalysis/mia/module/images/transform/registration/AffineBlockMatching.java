package io.github.mianalysis.mia.module.images.transform.registration;

import java.util.Collection;
import java.util.Vector;
import java.util.concurrent.ExecutionException;

import org.scijava.Priority;
import org.scijava.plugin.Plugin;

import ij.IJ;
import ij.process.FloatProcessor;
import ij.process.ImageProcessor;
import io.github.mianalysis.mia.MIA;
import io.github.mianalysis.mia.module.Module;
import io.github.mianalysis.mia.module.Modules;
import io.github.mianalysis.mia.module.images.transform.registration.abstrakt.AbstractAffineRegistration;
import io.github.mianalysis.mia.object.Workspace;
import io.github.mianalysis.mia.object.parameters.BooleanP;
import io.github.mianalysis.mia.object.parameters.Parameters;
import io.github.mianalysis.mia.object.parameters.SeparatorP;
import io.github.mianalysis.mia.object.parameters.text.DoubleP;
import io.github.mianalysis.mia.object.parameters.text.IntegerP;
import mpicbg.ij.blockmatching.BlockMatching;
import mpicbg.models.AbstractAffineModel2D;
import mpicbg.models.ErrorStatistic;
import mpicbg.models.PointMatch;
import mpicbg.models.SpringMesh;
import mpicbg.models.TranslationModel2D;
import mpicbg.models.Vertex;

@Plugin(type = Module.class, priority = Priority.LOW, visible = true)
public class AffineBlockMatching extends AbstractAffineRegistration {
    public static final String FEATURE_SEPARATOR = "Feature detection";
    public static final String LAYER_SCALE = "Layer scale";
    public static final String SEARCH_RADIUS = "Search radius (px)";
    public static final String BLOCK_RADIUS = "Block radius (px)";
    public static final String RESOLUTION = "Resolution";
    public static final String MIN_PMCC_R = "Minimal PMCC r";
    public static final String MAX_CURVATURE = "Maximal curvature ratio";
    public static final String ROD = "Closest/next closest ratio";
    public static final String LOCAL_REGION_SIGMA = "Local region sigma";
    public static final String MAX_ABS_LOCAL_DISPLACEMENT = "Maximal absolute local displacement (px)";
    public static final String MAX_REL_LOCAL_DISPLACEMENT = "Maximal relative local displacement (px)";

    public AffineBlockMatching(Modules modules) {
        super("Affine (block matching)", modules);
    }

    @Override
    public String getDescription() {
        return "Apply slice-by-slice (2D) affine-based image registration to a multi-dimensional stack.  Images can be aligned relative to the first frame in the stack, the previous frame or a separate image in the workspace.  The registration transform can also be calculated from a separate stack to the one that it will be applied to.  Registration can be performed along either the time or Z axes.  The non-registered axis (e.g. time axis when registering in Z) can be \"linked\" (all frames given the same registration) or \"independent\" (each stack registered separately)."

                + "<br><br>This module uses the <a href=\"https://github.com/fiji/blockmatching\">Block Matching</a> plugin and associated MPICBG tools to detect matching regions from the input images and calculate and apply the necessary 2D affine transforms.";
    }

    @Override
    public BMParam createParameterSet() {
        return new BMParam();
    }

    @Override
    public void getParameters(Param param, Workspace workspace) {
        super.getParameters(param, workspace);

        // Setting up the parameters
        BMParam bmParam = (BMParam) param;
        bmParam.scale = (float) (double) parameters.getValue(LAYER_SCALE);
        bmParam.searchR = parameters.getValue(SEARCH_RADIUS);
        bmParam.blockR = parameters.getValue(BLOCK_RADIUS);
        bmParam.resolution = parameters.getValue(RESOLUTION);
        bmParam.minR = (float) (double) parameters.getValue(MIN_PMCC_R);
        bmParam.maxCurvature = (float) (double) parameters.getValue(MAX_CURVATURE);
        bmParam.rod = (float) (double) parameters.getValue(ROD);
        bmParam.sigma = (float) (double) parameters.getValue(LOCAL_REGION_SIGMA);
        bmParam.maxAbsDisp = (float) (double) parameters.getValue(MAX_ABS_LOCAL_DISPLACEMENT);
        bmParam.maxRelDisp = (float) (double) parameters.getValue(MAX_REL_LOCAL_DISPLACEMENT);

    }

    @Override
    protected Object[] fitModel(ImageProcessor referenceIpr, ImageProcessor warpedIpr, Param param) {
        BMParam p = (BMParam) param;

        // Converting to FloatProcessors and padding
        FloatProcessor ipr1 = padImage(warpedIpr, p).convertToFloatProcessor();
        FloatProcessor ipr2 = padImage(referenceIpr, p).convertToFloatProcessor();

        TranslationModel2D translationModel = new TranslationModel2D();
        SpringMesh mesh = new SpringMesh(p.resolution, ipr1.getWidth(), ipr2.getHeight(), 1, 1000, 0.9f);
        Collection<Vertex> vertices = mesh.getVertices();
        Vector<PointMatch> candidates = new Vector<PointMatch>();

        try {
            BlockMatching.matchByMaximalPMCC(ipr1, ipr2, null, null, p.scale, translationModel, p.blockR, p.blockR,
                    p.searchR, p.searchR, p.minR, p.rod, p.maxCurvature, vertices, candidates, new ErrorStatistic(1));
        } catch (InterruptedException e) {
            // Do nothing as the user has selected this
            return null;
        } catch (ExecutionException e) {
            MIA.log.writeError(e);
            return null;
        }

        AbstractAffineModel2D model = getModel(p.transformationMode);
        model.localSmoothnessFilter(candidates, candidates, p.sigma, p.maxAbsDisp, p.maxRelDisp);

        return new Object[] { model, candidates };

    }

    static ImageProcessor padImage(ImageProcessor iprIn, BMParam param) {
        int padR = param.searchR + param.blockR;

        int widthIn = iprIn.getWidth();
        int heightIn = iprIn.getHeight();
        int widthOut = widthIn + 2 * padR;
        int heightOut = heightIn + 2 * padR;

        // Creating new ImageProcessor
        ImageProcessor iprOut = IJ.createImage("", widthOut, heightOut, 1, iprIn.getBitDepth()).getProcessor();

        // Setting pixel intensities
        for (int x = 0; x < widthIn; x++) {
            for (int y = 0; y < heightIn; y++) {
                iprOut.setf(x + padR, y + padR, iprIn.getf(x, y));
            }
        }

        return iprOut;

    }

    @Override
    protected void initialiseParameters() {
        super.initialiseParameters();

        parameters.add(new SeparatorP(FEATURE_SEPARATOR, this));
        parameters.add(new DoubleP(LAYER_SCALE, this, 1.0));
        parameters.add(new IntegerP(SEARCH_RADIUS, this, 50));
        parameters.add(new IntegerP(BLOCK_RADIUS, this, 50));
        parameters.add(new IntegerP(RESOLUTION, this, 24));
        parameters.add(new DoubleP(MIN_PMCC_R, this, 0.1));
        parameters.add(new DoubleP(MAX_CURVATURE, this, 1000.0));
        parameters.add(new DoubleP(ROD, this, 1.0));
        parameters.add(new DoubleP(LOCAL_REGION_SIGMA, this, 65.0));
        parameters.add(new DoubleP(MAX_ABS_LOCAL_DISPLACEMENT, this, 12.0));
        parameters.add(new DoubleP(MAX_REL_LOCAL_DISPLACEMENT, this, 3.0));

        addParameterDescriptions();

    }

    @Override
    public Parameters updateAndGetParameters() {
        Parameters returnedParameters = new Parameters();

        returnedParameters.addAll(super.updateAndGetParameters());

        returnedParameters.add(parameters.getParameter(FEATURE_SEPARATOR));
        returnedParameters.add(parameters.getParameter(LAYER_SCALE));
        returnedParameters.add(parameters.getParameter(SEARCH_RADIUS));
        returnedParameters.add(parameters.getParameter(BLOCK_RADIUS));
        returnedParameters.add(parameters.getParameter(RESOLUTION));
        returnedParameters.add(parameters.getParameter(MIN_PMCC_R));
        returnedParameters.add(parameters.getParameter(MAX_CURVATURE));
        returnedParameters.add(parameters.getParameter(ROD));
        returnedParameters.add(parameters.getParameter(LOCAL_REGION_SIGMA));
        returnedParameters.add(parameters.getParameter(MAX_ABS_LOCAL_DISPLACEMENT));
        returnedParameters.add(parameters.getParameter(MAX_REL_LOCAL_DISPLACEMENT));

        return returnedParameters;

    }

    @Override
    protected void addParameterDescriptions() {
        super.addParameterDescriptions();

        String siteRef1 = "Description taken from <a href=\"https://imagej.net/Feature_Extraction\">https://imagej.net/Feature_Extraction</a>";
        String siteRef2 = "Description taken from <a href=\"https://imagej.net/Elastic_Alignment_and_Montage.html\">https://imagej.net/Elastic_Alignment_and_Montage.html</a>";

        parameters.get(LAYER_SCALE).setDescription(
                "Scale factor applied to input image prior to alignment of blocks.  This can be used to reduce the computational cost of performing the registration.  For example, a scale of 0.5 will reduce the size of the image used in the alignment process.  Note: The final registration will be applied to the original size images.");

        parameters.get(SEARCH_RADIUS).setDescription(
                "The maximum range a single block can move from its original position whilst searching for the highest quality alignment.");

        parameters.get(BLOCK_RADIUS).setDescription(
                "Size of the individual blocks that will be used in searching for the highest quality alignment.  These need to be sufficiently large to include recognisable features in a single block.");

        parameters.get(RESOLUTION).setDescription(
                "The number of vertices in the spring mesh, with higher numbers giving smoother results.");

        parameters.get(MIN_PMCC_R).setDescription(
                "\"The PMCC coefficent <i>r</i> of a patch around the vertex and the overlapping patch in the other image is used as the quality measure for a match.  The threshold for minimal PMCC <i>r</i> can be higher for aligning the same signal than for aligning changing signals. Higher values will lead to more matches rejected and thus less false positives.\".  "
                        + siteRef2);

        parameters.get(MAX_CURVATURE).setDescription(
                "\"The maximal curvature ratio is the threshold for edge responses. The value must be >1.0. Higher values will accept more matches alongside elongated structures and thus lead to potentially more false positives.\".  "
                        + siteRef2);

        parameters.get(ROD).setDescription(
                "\"Correspondence candidates from local descriptor matching are accepted only if the Euclidean distance to the nearest neighbour is significantly smaller than that to the next nearest neighbour. Lowe (2004) suggests a ratio of r=0.8 which requires some increase when matching things that appear significantly distorted.\".  "
                        + siteRef1);

        parameters.get(LOCAL_REGION_SIGMA).setDescription(
                "\"The local smoothness filter inspects each match and compares how well the estimated translational offset agrees with all other matches weighted by their distance to the inspected match. To that end, a local linear transformation (typically rigid) is calculated using weighted least squares. The weight for each match is defined by a Gaussian radial distribution function (RDF) centered at the reference match.  This parameter controls sigma for this RDF. A match is rejected if its transfer error relative to the estimated linear transformation is larger than an absolute threshold or larger than k× the average transfer error of all weighted matches (k is specified in the relative field).  \""
                        + siteRef2);

        parameters.get(MAX_ABS_LOCAL_DISPLACEMENT)
                .setDescription("See description for \"" + LOCAL_REGION_SIGMA + "\".");

        parameters.get(MAX_REL_LOCAL_DISPLACEMENT)
                .setDescription("See description for \"" + LOCAL_REGION_SIGMA + "\".");

    }

    public class BMParam extends AffineParam {
        float scale = 1.0f;
        int searchR = 50;
        int blockR = 50;
        int resolution = 24;
        float minR = 0.1f;
        float maxCurvature = 1000.0f;
        float rod = 1.0f;
        float sigma = 65.0f;
        float maxAbsDisp = 12.0f;
        float maxRelDisp = 3.0f;

    }
}
