package io.github.mianalysis.mia.process.selectors;

import com.drew.lang.annotations.Nullable;

import java.util.LinkedList;
import java.util.Set;
import java.util.TreeSet;

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
import javax.swing.BoxLayout;
import javax.swing.ListSelectionModel;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import io.github.mianalysis.mia.MIA;

public class ClassSelector implements ActionListener, KeyListener {
    private boolean allowAdditions;
    private JFrame frame;
    private JTabbedPane tabbedPane;
    private SortedListModel<String> fullListModel = new SortedListModel<>();
    private JList<String> fullList = new JList<>(fullListModel);
    private SortedListModel<String> currentListModel = new SortedListModel<>();
    private JList<String> currentList = new JList<>(currentListModel);
    private RecentListModel<String> recentListModel = new RecentListModel<>();
    private JList<String> recentList = new JList<>(recentListModel);
    private SortedListModel<String> searchListModel = new SortedListModel<>();
    private JList<String> searchList = new JList<>(searchListModel);
    private String lastSelectedClass = null;
    private ActiveList activeList = ActiveList.FULL;

    private enum ActiveList {
        FULL, CURRENT, RECENT, SEARCH
    }

    // GUI buttons
    private static final String CREATE_CLASS = "+";
    private static final String APPLY_CLASS = "Apply class";
    

    public ClassSelector(@Nullable TreeSet<String> classes, boolean allowAdditions) {
        this.allowAdditions = allowAdditions;
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
                lastSelectedClass = "None";
            };
        });

        frame.setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 0;
        c.insets = new Insets(5, 5, 5, 5);

        String instructionText = "Select class, then click \"Apply\"";
        JLabel headerLabel = new JLabel("<html>" + instructionText + "</html>");
        headerLabel.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 14));
        frame.add(headerLabel, c);

        tabbedPane = new JTabbedPane();
        c.gridx = 0;
        c.gridy++;
        c.gridwidth = 4;
        c.fill = GridBagConstraints.BOTH;
        frame.add(tabbedPane, c);

        fullList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        fullList.addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                activeList = ActiveList.FULL;
            }
        });

        JScrollPane fullListScrollPane = new JScrollPane(fullList);
        fullListScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        fullListScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        fullListScrollPane.getVerticalScrollBar().setUnitIncrement(10);
        fullListScrollPane.setViewportBorder(BorderFactory.createEmptyBorder());
        tabbedPane.add("All classes", fullListScrollPane);

        currentList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        currentList.addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                activeList = ActiveList.CURRENT;
            }
        });

        JScrollPane currentListScrollPane = new JScrollPane(currentList);
        currentListScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        currentListScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        currentListScrollPane.getVerticalScrollBar().setUnitIncrement(10);
        currentListScrollPane.setViewportBorder(BorderFactory.createEmptyBorder());
        tabbedPane.add("Current classes", currentListScrollPane);

        recentList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        recentList.addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                activeList = ActiveList.RECENT;
            }
        });

        JScrollPane recentListScrollPane = new JScrollPane(recentList);
        recentListScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        recentListScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        recentListScrollPane.getVerticalScrollBar().setUnitIncrement(10);
        recentListScrollPane.setViewportBorder(BorderFactory.createEmptyBorder());
        tabbedPane.add("Recent classes", recentListScrollPane);

        searchList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        searchList.addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                activeList = ActiveList.SEARCH;
            }
        });

        JPanel searchPane = new JPanel();
        searchPane.setLayout(new BoxLayout(searchPane, BoxLayout.PAGE_AXIS));

        JScrollPane searchListScrollPane = new JScrollPane(searchList);
        searchListScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        searchListScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        searchListScrollPane.getVerticalScrollBar().setUnitIncrement(10);
        searchListScrollPane.setViewportBorder(BorderFactory.createEmptyBorder());
        searchPane.add(searchListScrollPane);

        JTextField searchField = new JTextField();
        searchField.getDocument().addDocumentListener(new DocumentListener() {
            void updateSearchList() {
                String searchString = searchField.getText();

                TreeSet<String> searchItems = new TreeSet<>();
                for (String item : fullListModel.items())
                    if (item.contains(searchString))
                        searchItems.add(item);
                searchListModel.setItems(searchItems);
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                updateSearchList();
            }

            @Override
            public void insertUpdate(DocumentEvent e) {
                updateSearchList();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                updateSearchList();
            }
        });
        searchPane.add(searchField);
        tabbedPane.add("Search", searchPane);

        JButton createClassButton = new JButton(CREATE_CLASS);
        createClassButton.addActionListener(this);
        createClassButton.setActionCommand(CREATE_CLASS);
        c.gridx = 0;
        c.gridy++;
        c.gridwidth = 1;
        c.weightx = 1;
        c.fill = GridBagConstraints.VERTICAL;
        c.anchor = GridBagConstraints.WEST;
        if (allowAdditions)
            frame.add(createClassButton, c);

        JButton applyClassButton = new JButton(APPLY_CLASS);
        applyClassButton.addActionListener(this);
        applyClassButton.setActionCommand(APPLY_CLASS);
        c.gridx = 3;
        c.anchor = GridBagConstraints.EAST;
        frame.add(applyClassButton, c);

        frame.pack();
        frame.setLocation(100, 100);
        frame.setVisible(false); // Don't show till needed

    }

    public void setVisible(boolean visible) {
        if (visible)
            frame.pack();

        frame.setVisible(visible);

    }

    public String getLastSelectedClass() {
        return lastSelectedClass;
    }

    public TreeSet<String> getAllClasses() {
        return fullListModel.items();
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        switch (e.getActionCommand()) {
            case (APPLY_CLASS):
                switch (activeList) {
                    case FULL:
                        lastSelectedClass = fullList.getSelectedValue();
                        currentListModel.add(lastSelectedClass);
                        recentListModel.add(lastSelectedClass);
                        break;
                    case CURRENT:
                        lastSelectedClass = currentList.getSelectedValue();
                        recentListModel.add(lastSelectedClass);
                        break;
                    case RECENT:
                        lastSelectedClass = recentList.getSelectedValue();
                        recentListModel.add(lastSelectedClass); // This brings it to the top of the list
                        currentListModel.add(lastSelectedClass);
                        break;
                    case SEARCH:
                        lastSelectedClass = searchList.getSelectedValue();
                        recentListModel.add(lastSelectedClass); // This brings it to the top of the list
                        currentListModel.add(lastSelectedClass);
                        break;
                }

                setVisible(false);

                break;

            case (CREATE_CLASS):
                String newClass = JOptionPane.showInputDialog("Enter new class name");
                fullListModel.add(newClass);
                int idx = fullListModel.getIndex(newClass);
                fullList.setSelectedIndex(idx);
                fullList.ensureIndexIsVisible(idx);
                tabbedPane.setSelectedIndex(0); // Selecting "All items" tab
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
        private TreeSet<E> items = new TreeSet<>();

        public SortedListModel() {

        }

        public SortedListModel(TreeSet<E> items) {
            this.items = items;
        }

        public void setItems(TreeSet<E> items) {
            this.items = items;
            fireContentsChanged(this, 0, getSize());
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

        public TreeSet<E> items() {
            return items;
        }

        public int getIndex(E item) {
            int idx = 0;
            for (E listItem : items)
                if (listItem == item)
                    return idx;
                else
                    idx++;

            // Return -1 if not found
            return -1;

        }
    }

    class RecentListModel<E> extends AbstractListModel<E> {
        private int capacity = 100;
        private LinkedList<E> items = new LinkedList<>();

        public RecentListModel() {

        }

        public RecentListModel(LinkedList<E> items) {
            this.items = items;
        }

        public void add(E newItem) {
            // If adding again (to move to top of the list), remove first
            if (items.contains(newItem))
                items.remove(newItem);

            items.addFirst(newItem);

            if (items.size() > capacity)
                items.removeLast();

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