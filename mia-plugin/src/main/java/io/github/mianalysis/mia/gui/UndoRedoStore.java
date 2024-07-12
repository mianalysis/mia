package io.github.mianalysis.mia.gui;

import java.util.LinkedList;

import io.github.mianalysis.mia.module.Modules;

public class UndoRedoStore {
    private int limit = 10;
    private LinkedList<Modules> undoStore = new LinkedList<>();
    private LinkedList<Modules> redoStore = new LinkedList<>();

    public void addUndo(Modules modules) {
        undoStore.addFirst(modules.duplicate());
        checkLimit();

        // Clear the redo store
        redoStore = new LinkedList<>();

    }

    public Modules getNextUndo(Modules modules) {
        if (undoStore.size() == 0) return null;

        if (modules != null) redoStore.addFirst(modules);
        return undoStore.pop().duplicate();

    }

    public Modules getNextRedo(Modules modules) {
        if (redoStore.size() == 0) return null;

        if (modules != null) undoStore.addFirst(modules);
        return redoStore.pop().duplicate();

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
