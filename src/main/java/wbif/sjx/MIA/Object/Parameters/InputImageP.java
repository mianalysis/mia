package wbif.sjx.MIA.Object.Parameters;

import wbif.sjx.MIA.Module.Module;
import wbif.sjx.MIA.Object.Parameters.Abstract.ImageNamesType;
import wbif.sjx.MIA.Object.Parameters.Abstract.Parameter;

import javax.annotation.Nonnull;

public class InputImageP extends ImageNamesType {
    public InputImageP(String name, Module module) {
        super(name, module);
    }

    public InputImageP(String name, Module module, @Nonnull String imageName) {
        super(name, module);
        this.choice = imageName;
    }

    public InputImageP(String name, Module module, @Nonnull String imageName, String description) {
        super(name, module, description);
        this.choice = imageName;
    }

    public String getImageName() {
        return choice;
    }

    public void setImageName(String imageName) {
        this.choice = imageName;
    }

    @Override
    public <T extends Parameter> T duplicate() {
        InputImageP newParameter = new InputImageP(name,module,getImageName(),getDescription());

        newParameter.setNickname(getNickname());
        newParameter.setVisible(isVisible());
        newParameter.setExported(isExported());

        return (T) newParameter;

    }
}
