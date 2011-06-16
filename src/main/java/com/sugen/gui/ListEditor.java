package com.sugen.gui;

import com.sugen.event.*;

import java.awt.datatransfer.*;
import java.io.IOException;
import java.util.*;
import javax.swing.undo.*;

/**
 * Provides integration with an AppClipboard via the Editor interface.
 *
 * <p>Selections from the specified SelectionModel
 * will be copied to the clipboard as a serialized
 * Collection. They will be cut from and pasted onto the Collection
 * specified by setCollection(Collection).
 *
 * <p>This class is not thread safe.
 *
 * @author Jonathan Bingham
 */
public class ListEditor
    extends AbstractEditor {
    protected final static DataFlavor collectionFlavor =
        new DataFlavor(Collection.class, Collection.class.getName());

    /** @serial */
    protected List collection;
    /** @serial */
    protected SelectionModel selectionModel;
    /** @serial */
    protected Transferable transferModel = new Transferable() {
        /**
         * @return the Collection containing the current selections
         */
        public Object getTransferData(DataFlavor flavor) {
            return selectionModel;
        }

        public DataFlavor[] getTransferDataFlavors() {
            return new DataFlavor[] {
                collectionFlavor};
        }

        public boolean isDataFlavorSupported(DataFlavor flavor) {
            return collectionFlavor.getRepresentationClass().equals(
                flavor.getRepresentationClass());
        }
    };

    public ListEditor() {
    }

    /** @serial */
    protected SelectionListener selectionListener = new SelectionListener() {
        public void selectionAdded(CollectionEvent e) {
            selectionChanged(e);
        }

        public void selectionRemoved(CollectionEvent e) {
            selectionChanged(e);
        }

        public void selectionChanged(CollectionEvent e) {
            if(e.getSource() != selectionModel)
                selectionModel = (SelectionModel)e.getSource();
            updateAvailableEdits();
        }
    };

    protected void updateAvailableEdits() {
        //Determine what editor operations are available
        //As far as CollectionEditor knows, paste is always available
        int edits = PASTE_MASK;
        if(selectionModel != null && !selectionModel.isEmpty())
            edits = edits | CUT_MASK | COPY_MASK | DELETE_MASK;
        if(undoManager.canUndo())
            edits = edits | UNDO_MASK;
        if(undoManager.canRedo())
            edits = edits | REDO_MASK;

        int oldMask = availableEdits;
        availableEdits = edits;
        if(oldMask != availableEdits)
            propertySupport.firePropertyChange(PROPERTY_ACTIVE_EDITS,
                                               null, //new Integer(oldMask),
                                               new Integer(availableEdits));
    }

    /**
     * Cut and delete from, and paste onto, this Collection.
     */
    public void setCollection(Collection c) {
        if(!(c instanceof List))
            throw new IllegalArgumentException(
                "ListEditor's Collection must be a List");
        collection = (List)c;
    }

    public Collection getCollection() {
        return collection;
    }

    /**
     * Copy and paste from the selections in this SelectionModel.
     */
    public void setSelectionModel(SelectionModel model) {
        if(selectionModel != null)
            selectionModel.removeListener(selectionListener);
        selectionModel = model;
        if(selectionModel != null)
            selectionModel.addListener(selectionListener);
    }

    public SelectionModel getSelectionModel() {
        return selectionModel;
    }

    /**
     * Remove currently selected Sequences from the input Collection and return
     * the SelectionModel.
     **/
    public Transferable cut() {
        UndoableCut undoableCut = new UndoableCut(selectionModel);
        undoManager.addEdit(undoableCut);
        updateAvailableEdits();
        return(Transferable)transferModel;
    }

    /** @return reference to the SelectionModel. */
    public Transferable copy() {
        return transferModel;
    }

    /**
     * Appends a Collection onto the input Collection.
     */
    public void paste(Transferable obj) {
        try {
            Collection c = (Collection)
                obj.getTransferData(obj.getTransferDataFlavors()[0]);
            if(collection != null && c != null) {
                UndoablePaste undoablePaste = new UndoablePaste(c);
                undoManager.addEdit(undoablePaste);
                updateAvailableEdits();
            }
        }
        catch(UnsupportedFlavorException ufe) {}
        catch(IOException ioe) {}
    }

    public void delete() {
        cut();
    }

    protected class UndoablePaste
        extends AbstractUndoableEdit {
        transient protected Collection pasteCollection;

        public UndoablePaste(Collection paste) {
            pasteCollection = new ArrayList(paste);
            paste();
        }

        protected void paste() {
            collection.addAll(pasteCollection);
            if(selectionModel != null)
                selectionModel.setCollection(pasteCollection);
        }

        public void undo() {
            super.undo();
            collection.removeAll(pasteCollection);

            if(selectionModel != null && !selectionModel.isEmpty())
                selectionModel.clear();
        }

        public void redo() {
            super.redo();
            paste();
        }
    }

    protected class UndoableCut
        extends AbstractUndoableEdit {
        transient protected Collection cutCollection;
        transient protected int[] indices;

        public UndoableCut(Collection cut) {
            cutCollection = new ArrayList(cut);

            indices = new int[cut.size()];
            Iterator iter = cutCollection.iterator();
            int i = 0;
            while(iter.hasNext())
                indices[i++] = collection.indexOf(iter.next());

            cut();
        }

        protected void cut() {
            for(int i = indices.length - 1; i >= 0; i--) {
                if(i != -1)
                    collection.remove(indices[i]);
            }
            if(selectionModel != null)
                selectionModel.clear();
        }

        public void undo() {
            super.undo();
            Iterator iter = cutCollection.iterator();
            int i = indices.length - 1;
            while(iter.hasNext()) {
                if(indices[i] != -1)
                    collection.add(indices[i], iter.next());
            }
            if(selectionModel != null)
                selectionModel.setCollection(cutCollection);
        }

        public void redo() {
            super.redo();
            cut();
        }
    }
}
