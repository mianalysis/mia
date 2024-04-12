package ch.epfl.biop.wrappers.elastix.ij2commands;

import ch.epfl.biop.wrappers.elastix.DefaultElastixTask;
import ch.epfl.biop.wrappers.elastix.Mod_RegisterHelper;
import ch.epfl.biop.wrappers.elastix.RHZipFile;
import ch.epfl.biop.wrappers.elastix.RegParamAffine_Default;
import ch.epfl.biop.wrappers.elastix.RegParamAffine_Fast;
import ch.epfl.biop.wrappers.elastix.RegParamBSpline_Default;
import ch.epfl.biop.wrappers.elastix.RegParamRigid_Default;
import ch.epfl.biop.wrappers.elastix.RegistrationParameters;

public class Mod_Elastix_Register extends Elastix_Register {
	public int nThreads = 1;

    @Override
	public void run() {
		boolean multiChannelRegistration = false;
		int nChannels = 1;

		if ((movingImage.getNChannels()>1)||(fixedImage.getNChannels()>1)) {
			if (fixedImage.getNChannels()==movingImage.getNChannels()) {
				multiChannelRegistration = true;
				nChannels = fixedImage.getNChannels();
			} else {
				System.out.println("Can't perform multichannel registration because the number of channel is not identical between moving and fixed image");
			}
		}


		rh = new Mod_RegisterHelper();
		((Mod_RegisterHelper) rh).setNThreads(nThreads);
		rh.setMovingImage(movingImage);
		rh.setFixedImage(fixedImage);
		if (rigid) {
			RegistrationParameters[] rps = new RegistrationParameters[nChannels];
			for (int iCh = 0;iCh<nChannels;iCh++) {
				rps[iCh] = new RegParamRigid_Default();
			}
			//if (multiChannelRegistration) rp = RegistrationParameters.useAlphaMutualInformation(rp,nChannels);
			rh.addTransform(RegistrationParameters.combineRegistrationParameters(rps));
		}
		if (fast_affine) {
			RegistrationParameters[] rps = new RegistrationParameters[nChannels];
			for (int iCh = 0;iCh<nChannels;iCh++) {
				rps[iCh] = new RegParamAffine_Fast();
			}
			//if (multiChannelRegistration) rp = RegistrationParameters.useAlphaMutualInformation(rp,nChannels);
			rh.addTransform(RegistrationParameters.combineRegistrationParameters(rps));
		}
		if (affine) {
			RegistrationParameters[] rps = new RegistrationParameters[nChannels];
			for (int iCh = 0;iCh<nChannels;iCh++) {
				rps[iCh] = new RegParamAffine_Default();
			}
			//if (multiChannelRegistration) rp = RegistrationParameters.useAlphaMutualInformation(rp,nChannels);
			rh.addTransform(RegistrationParameters.combineRegistrationParameters(rps));
		}
		if (spline) {
			RegistrationParameters[] rps = new RegistrationParameters[nChannels];
			for (int iCh = 0;iCh<nChannels;iCh++) {
				rps[iCh] = new RegParamBSpline_Default();
			}
			RegistrationParameters rp = RegistrationParameters.combineRegistrationParameters(rps);
			//if (multiChannelRegistration) rp = RegistrationParameters.useAlphaMutualInformation(rp,nChannels);
			rp.FinalGridSpacingInVoxels = splineGridSpacing;
			rh.addTransform(rp);
		}
		try {
			rh.align(new DefaultElastixTask());
			rh.to(RHZipFile.class);
		} catch (Exception e) {
			e.printStackTrace();
		}
    }
}
