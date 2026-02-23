package io.github.mianalysis.mia.gui;

import java.util.LinkedList;

import io.github.mianalysis.mia.module.ModulesI;

public class UndoRedoStore {
    private int limit = 100;
    private LinkedList<ModulesI> undoStore = new LinkedList<>();
    private LinkedList<ModulesI> redoStore = new LinkedList<>();

    public void addUndo(ModulesI modules) {
        undoStore.addFirst(modules.duplicate(true));
        checkLimit();

        // Clear the redo store
        redoStore = new LinkedList<>();

    }

    public ModulesI getNextUndo(ModulesI modules) {
        if (undoStore.size() == 0) return null;

        if (modules != null) redoStore.addFirst(modules);
        return undoStore.pop().duplicate(true);

    }

    public ModulesI getNextRedo(ModulesI modules) {
        if (redoStore.size() == 0) return null;

        if (modules != null) undoStore.addFirst(modules);
        return redoStore.pop().duplicate(true);

    }

    public void reset() {
        undoStore = new LinkedList<>();
        redoStore = new LinkedList<>();
    }

    void checkLimit() {
        while (undoStore.size() > limit) {
            undoStore.removeLast();
        }
    }

    public int getUndoSize() {
        return undoStore.size();
    }

    public int getRedoSize() {
        return redoStore.size();
    }

    public int getLimit() {
        return limit;
    }

    public void setLimit(int limit) {
        this.limit = limit;
    }

}
