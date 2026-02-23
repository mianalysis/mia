
package io.github.mianalysis.mia.object.parameters;

import com.drew.lang.annotations.NotNull;

import io.github.mianalysis.mia.module.ModuleI;
import io.github.mianalysis.mia.module.ModulesI;
import io.github.mianalysis.mia.object.parameters.abstrakt.ChoiceType;
import io.github.mianalysis.mia.object.parameters.abstrakt.Parameter;
import io.github.mianalysis.mia.object.refs.collections.PartnerRefs;

public class PartnerObjectsP extends ChoiceType {
    private String partnerObjectsName = "";

    public PartnerObjectsP(String name, ModuleI module) {
        super(name, module);
    }

    public PartnerObjectsP(String name, ModuleI module, String description) {
        super(name, module, description);
    }

    public PartnerObjectsP(String name, ModuleI module, @NotNull String choice, @NotNull String partnerObjectsName) {
        super(name, module);
        this.choice = choice;
        this.partnerObjectsName = partnerObjectsName;
    }

    public PartnerObjectsP(String name, ModuleI module, @NotNull String choice, @NotNull String partnerObjectsName, String description) {
        super(name, module, description);
        this.choice = choice;
        this.partnerObjectsName = partnerObjectsName;
    }

    @Override
    public <T extends Parameter> T duplicate(ModuleI newModule) {
        PartnerObjectsP newParameter = new PartnerObjectsP(name,newModule,getRawStringValue(),partnerObjectsName,getDescription());
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

        ModulesI modules = module.getModules();
        PartnerRefs relationships = modules.getPartnerRefs(module);

        return relationships.getPartnerNamesArray(partnerObjectsName);

    }
}
