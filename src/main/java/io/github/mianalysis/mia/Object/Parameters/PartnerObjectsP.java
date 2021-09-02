
package io.github.mianalysis.mia.Object.Parameters;

import com.drew.lang.annotations.NotNull;

import io.github.mianalysis.mia.module.Module;
import io.github.mianalysis.mia.module.Modules;
import io.github.mianalysis.mia.Object.Parameters.Abstract.ChoiceType;
import io.github.mianalysis.mia.Object.Parameters.Abstract.Parameter;
import io.github.mianalysis.mia.Object.Refs.Collections.PartnerRefs;

public class PartnerObjectsP extends ChoiceType {
    private String partnerObjectsName = "";

    public PartnerObjectsP(String name, Module module) {
        super(name, module);
    }

    public PartnerObjectsP(String name, Module module, String description) {
        super(name, module, description);
    }

    public PartnerObjectsP(String name, Module module, @NotNull String choice, @NotNull String partnerObjectsName) {
        super(name, module);
        this.choice = choice;
        this.partnerObjectsName = partnerObjectsName;
    }

    public PartnerObjectsP(String name, Module module, @NotNull String choice, @NotNull String partnerObjectsName, String description) {
        super(name, module, description);
        this.choice = choice;
        this.partnerObjectsName = partnerObjectsName;
    }

    @Override
    public <T extends Parameter> T duplicate(Module newModule) {
        PartnerObjectsP newParameter = new PartnerObjectsP(name,newModule,getChoice(),partnerObjectsName,getDescription());
        newParameter.setNickname(getNickname());
        newParameter.setVisible(isVisible());
        newParameter.setExported(isExported());

        return (T) newParameter;

    }

    public String getPartnerObjectsName() {
        return partnerObjectsName;
    }

    public void setPartnerObjectsName(String partnerObjectsName) {
        this.partnerObjectsName = partnerObjectsName;
    }

    @Override
    public String[] getChoices() {
        if (partnerObjectsName == null) return null;

        Modules modules = module.getModules();
        PartnerRefs relationships = modules.getPartnerRefs(module);

        return relationships.getPartnerNamesArray(partnerObjectsName);

    }
}
