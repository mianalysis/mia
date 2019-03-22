package wbif.sjx.ModularImageAnalysis.Object.Parameters;

import wbif.sjx.ModularImageAnalysis.GUI.GUI;
import wbif.sjx.ModularImageAnalysis.Module.Module;
import wbif.sjx.ModularImageAnalysis.Object.Parameters.Abstract.ChoiceType;
import wbif.sjx.ModularImageAnalysis.Object.Parameters.Abstract.Parameter;

import javax.annotation.Nonnull;

public class ImageMeasurementP extends ChoiceType {
    private String imageName = "";

    public ImageMeasurementP(String name, Module module) {
        super(name, module);
    }

    public ImageMeasurementP(String name, Module module, String description) {
        super(name, module, description);
    }

    public ImageMeasurementP(String name, Module module, @Nonnull String choice, @Nonnull String imageName) {
        super(name, module);
        this.imageName = imageName;
        this.choice = choice;
    }

    public ImageMeasurementP(String name, Module module, @Nonnull String choice, @Nonnull String imageName, String description) {
        super(name, module, description);
        this.imageName = imageName;
        this.choice = choice;
    }

    public String getImageName() {
        return imageName;
    }

    public void setImageName(String imageName) {
        this.imageName = imageName;
    }

    @Override
    public String[] getChoices() {
        return GUI.getModules().getImageMeasurementRefs(imageName,module).getMeasurementNames();
    }

    @Override
    public <T extends Parameter> T duplicate() {
        return (T) new ImageMeasurementP(name,module,getChoice(),imageName,getDescription());
    }
}
