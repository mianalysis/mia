package io.github.mianalysis.mia.gui.parametercontrols;

import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.LinkedHashMap;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;

import io.github.mianalysis.mia.gui.GUI;
import io.github.mianalysis.mia.module.core.OutputControl;
import io.github.mianalysis.mia.object.parameters.AdjustParameters;
import io.github.mianalysis.mia.object.parameters.Parameters;
import io.github.mianalysis.mia.object.parameters.abstrakt.ParameterControl;

/**
 * Created by Stephen Cross on 01/02/2019.
 */
public class AdjustParameterGroupButton extends ParameterControl implements ActionListener {
    private static final String REMOVE = "Remove";
    private static final String MOVE_UP = "▲";
    private static final String MOVE_DOWN = "▼";

    private JPanel control;
    private JButton removeButton;
    private JButton moveUpButton;
    private JButton moveDownButton;

    public AdjustParameterGroupButton(AdjustParameters parameter) {
        super(parameter);

        control = new JPanel();
        control.setLayout(new GridBagLayout());

        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 0;
        c.weightx = 1;
        c.weighty = 0;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.insets = new Insets(0, 0, 0, 0);
        c.anchor = GridBagConstraints.WEST;

        removeButton = new JButton(REMOVE);
        removeButton.addActionListener(this);
        removeButton.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
        control.add(removeButton, c);

        moveUpButton = new JButton(MOVE_UP);
        moveUpButton.addActionListener(this);
        moveUpButton.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
        c.gridx++;
        c.insets = new Insets(0, 5, 0, 0);
        control.add(moveUpButton, c);

        moveDownButton = new JButton(MOVE_DOWN);
        moveDownButton.addActionListener(this);
        moveDownButton.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
        c.gridx++;
        c.insets = new Insets(0, 5, 0, 0);
        control.add(moveDownButton, c);

    }

    @Override
    public JComponent getComponent() {
        return control;
    }

    @Override
    public void updateControl() {

    }

    @Override
    public void actionPerformed(ActionEvent e) {
        GUI.addUndo();

        switch (e.getActionCommand()) {
            case REMOVE:
                ((AdjustParameters) parameter).getGroup()
                        .removeCollection(((AdjustParameters) parameter).getCollectionIndex());
                break;

            case MOVE_UP:
                LinkedHashMap<Integer, Parameters> collections = ((AdjustParameters) parameter).getGroup()
                        .getCollections(false);
                int moveIdx = ((AdjustParameters) parameter).getCollectionIndex();
                int prevIdx = -1;

                for (int idx : collections.keySet()) {
                    if (idx == moveIdx) {
                        // If prevIdx is -1 the current group is at the top of the list, so can't go
                        // higher
                        if (prevIdx == -1)
                            return;

                        Parameters moveCollection = collections.get(moveIdx);
                        Parameters targetCollection = collections.get(prevIdx);

                        collections.replace(prevIdx, moveCollection);
                        collections.replace(moveIdx, targetCollection);

                        break;

                    }

                    prevIdx = idx;

                }

                break;

            case MOVE_DOWN:
                collections = ((AdjustParameters) parameter).getGroup().getCollections(false);
                moveIdx = ((AdjustParameters) parameter).getCollectionIndex();
                boolean ready = false;
                for (int idx : collections.keySet()) {
                    if (idx == moveIdx) {
                        ready = true;
                        continue;
                    }

                    if (ready) {
                        Parameters moveCollection = collections.get(moveIdx);
                        Parameters targetCollection = collections.get(idx);

                        collections.replace(idx, moveCollection);
                        collections.replace(moveIdx, targetCollection);

                        break;
                    }
                }

                break;

        }

        int idx = GUI.getModules().indexOf(parameter.getModule());
        if (idx <= GUI.getLastModuleEval() & !(parameter.getModule() instanceof OutputControl))
            GUI.setLastModuleEval(idx - 1);

        GUI.updateModules(true, parameter.getModule());
        GUI.updateParameters(true, parameter.getModule());

        updateControl();

    }
}
