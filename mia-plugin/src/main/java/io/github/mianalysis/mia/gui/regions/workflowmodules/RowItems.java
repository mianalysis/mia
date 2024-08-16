package io.github.mianalysis.mia.gui.regions.workflowmodules;

import com.drew.lang.annotations.Nullable;

public class RowItems {
    private ModuleEnabledButton moduleEnabledButton;
    private ShowOutputButton showOutputButton;
    private EvalButton evalButton;
    private SeparatorButton separatorButton;

    public RowItems(ModuleEnabledButton moduleEnabledButton, @Nullable ShowOutputButton showOutputButton,
            @Nullable EvalButton evalButton, @Nullable SeparatorButton separatorButton) {
        this.moduleEnabledButton = moduleEnabledButton;
        this.showOutputButton = showOutputButton;
        this.evalButton = evalButton;
        this.separatorButton = separatorButton;
    }

    public ModuleEnabledButton getModuleEnabledButton() {
        return moduleEnabledButton;
    }

    public ShowOutputButton getShowOutputButton() {
        return showOutputButton;
    }

    public EvalButton getEvalButton() {
        return evalButton;
    }

    public SeparatorButton getSeparatorButton() {
        return separatorButton;
    }
}
