package io.github.mianalysis.mia.process.selectors;

import com.drew.lang.annotations.Nullable;

import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.AbstractListModel;
import javax.swing.BorderFactory;
import javax.swing.ListSelectionModel;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import io.github.mianalysis.mia.MIA;

public class ClassSelector implements ActionListener, KeyListener {
    private JFrame frame;
    private JTextField objectNumberField;
    private SortedListModel<String> fullListModel = new SortedListModel<>();
    private JList<String> fullList = new JList<>(fullListModel);
    private SortedListModel<String> currentListModel = new SortedListModel<>();
    private JList<String> currentList = new JList<>(currentListModel);
    private String lastSelectedClass = null;
    private ActiveList activeList = ActiveList.FULL;

    private enum ActiveList {
        FULL, CURRENT, RECENT
    }

    // GUI buttons
    private static final String APPLY_CLASS = "Apply";

    public ClassSelector(@Nullable TreeSet<String> classes) {
        fullListModel.addAll(classes);
        showOptionsPanel();
    }

    public boolean isActive() {
        return frame != null && frame.isVisible();
    }

    private void showOptionsPanel() {
        frame = new JFrame();

        frame.setAlwaysOnTop(true);

        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                MIA.log.writeDebug("TODO - Assign null class");
            };
        });

        frame.setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 0;
        c.gridwidth = 4;
        c.gridheight = 1;
        c.weightx = 1;
        c.anchor = GridBagConstraints.WEST;
        c.insets = new Insets(5, 5, 5, 5);

        String instructionText = "Do some stuff";
        JLabel headerLabel = new JLabel("<html>" + instructionText + "</html>");
        headerLabel.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 14));
        frame.add(headerLabel, c);

        JButton newObjectButton = new JButton(APPLY_CLASS);
        newObjectButton.addActionListener(this);
        newObjectButton.setActionCommand(APPLY_CLASS);
        c.gridy++;
        c.gridwidth = 1;
        frame.add(newObjectButton, c);

        // Object number panel
        JLabel objectNumberLabel = new JLabel("Existing object number");
        c.gridx = 0;
        c.gridy++;
        c.gridwidth = 2;
        frame.add(objectNumberLabel, c);

        objectNumberField = new JTextField();
        c.gridx++;
        c.gridwidth = 3;
        c.fill = GridBagConstraints.HORIZONTAL;
        frame.add(objectNumberField, c);

        fullList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        fullList.addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                activeList = ActiveList.FULL;
            }
        });

        JScrollPane fullListScrollPane = new JScrollPane(fullList);
        // fullListScrollPane.setPreferredSize(new Dimension(0, 200));
        fullListScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        fullListScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        fullListScrollPane.getVerticalScrollBar().setUnitIncrement(10);
        fullListScrollPane.setViewportBorder(BorderFactory.createEmptyBorder());

        c.gridx = 0;
        c.gridy++;
        c.gridwidth = 2;
        c.gridheight = 3;
        c.fill = GridBagConstraints.BOTH;
        frame.add(fullListScrollPane, c);

        currentList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        currentList.addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                activeList = ActiveList.CURRENT;
            }
        });

        JScrollPane currentListScrollPane = new JScrollPane(currentList);
        // currentListScrollPane.setPreferredSize(new Dimension(0, 200));
        currentListScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        currentListScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        currentListScrollPane.getVerticalScrollBar().setUnitIncrement(10);
        currentListScrollPane.setViewportBorder(BorderFactory.createEmptyBorder());

        c.gridx++;
        c.gridx++;
        c.gridy++;
        c.gridwidth = 2;
        c.gridheight = 3;
        c.fill = GridBagConstraints.BOTH;
        frame.add(currentListScrollPane, c);

        frame.pack();
        frame.setLocation(100, 100);
        frame.setVisible(false); // Don't show till needed
        // frame.setResizable(false);

    }

    public void setVisible(boolean visible) {
        if (visible)
            frame.pack();

        frame.setVisible(visible);

    }

    public String getLastSelectedClass() {
        return lastSelectedClass;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        switch (e.getActionCommand()) {
            case (APPLY_CLASS):
                switch (activeList) {
                    case FULL:
                        lastSelectedClass = fullList.getSelectedValue();
                        currentListModel.add(lastSelectedClass);
                        break;
                    case CURRENT:
                        lastSelectedClass = currentList.getSelectedValue();
                        break;
                    case RECENT:
                        currentListModel.add(lastSelectedClass);
                        break;
                }

                setVisible(false);

                // Adding added item to the list of classes in use
                

                break;
        }
    }

    @Override
    public void keyPressed(KeyEvent arg0) {
    }

    @Override
    public void keyReleased(KeyEvent arg0) {
    }

    @Override
    public void keyTyped(KeyEvent arg0) {
    }

    class SortedListModel<E> extends AbstractListModel<E> {
        TreeSet<E> items = new TreeSet<>();

        public SortedListModel() {

        }

        public SortedListModel(TreeSet<E> items) {
            this.items = items;
        }

        public void add(E newItem) {
            items.add(newItem);
            fireContentsChanged(this, 0, getSize());
        }

        public void addAll(Set<E> newItems) {
            items.addAll(newItems);
        }

        public int getSize() {
            return items.size();
        }

        public E getElementAt(int index) {
            return (E) items.toArray()[index];
        }
    }
}