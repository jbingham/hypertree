package com.sugen.gui;

import com.sugen.event.*;

import java.util.*;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

/**
 * Use as a converter between Swing ListSelectionModel and SelectionModel.
 *
 * @see javax.swing.ListSelectionModel
 * @see com.sugen.gui.SelectionModel
 *
 * @author Jonathan Bingham
 */
public class ListSelectionConverter
    extends SelectionModel implements ListSelectionListener, SelectionListener {
    /** @serial */
    protected boolean isAdjusting;
    /** @serial */
    protected final ListSelectionModel listSelectionModel;
    /** @serial */
    protected final List list;

    /**
     * @throws IllegalArgumentException if either parameter is null
     */
    public ListSelectionConverter(ListSelectionModel model, List data) {
        if(model == null || data == null)
            throw new IllegalArgumentException(
                "Null arguments not allowed in ListSelectionConverter constructor");
        listSelectionModel = model;
        model.addListSelectionListener(this);

        list = data;
    }

    //TODO: fireAdded/Removed instead of always Changed
    public void valueChanged(ListSelectionEvent e) {
        if(isAdjusting)
            return;

        isAdjusting = true;

        //Get current selections
        ListSelectionModel model = (ListSelectionModel)e.getSource();
        Collection newSelections = new ArrayList();
        for(int i = model.getMinSelectionIndex();
            i <= model.getMaxSelectionIndex();
            i++)
            if(model.isSelectedIndex(i))
                newSelections.add(list.get(i));
        setCollection(newSelections);
//System.out.println("ListSelectionConverter " + size());
        isAdjusting = false;
    }

    public void selectionAdded(CollectionEvent e) {
        if(isAdjusting)
            return;

        //System.out.println("SequenceTableUI.selectionAdded");
        isAdjusting = true;
        if(e.isMultiple()) {
            addAll(e.getAllChanged());
            listSelectionModel.setValueIsAdjusting(true);
            Iterator iter = e.getAllChanged().iterator();
            int index = -1;
            while(iter.hasNext()) {
                index = list.indexOf(iter.next());
                if(index != -1)
                    listSelectionModel.addSelectionInterval(index, index);
            }
            listSelectionModel.setValueIsAdjusting(false);
        }
        else {
            add(e.getChanged());
            int index = list.indexOf(e.getChanged());
            if(index != -1)
                listSelectionModel.addSelectionInterval(index, index);
        }

        isAdjusting = false;
    }

    public void selectionRemoved(CollectionEvent e) {
        if(isAdjusting)
            return;

        //System.out.println("SequenceTableUI.selectionRemoved");
        isAdjusting = true;
        if(e.isMultiple()) {
            removeAll(e.getAllChanged());
            listSelectionModel.setValueIsAdjusting(true);
            Iterator iter = e.getAllChanged().iterator();
            while(iter.hasNext()) {
                int index = list.indexOf(iter.next());
                if(index != -1)
                    listSelectionModel.removeIndexInterval(index, index);
            }
            listSelectionModel.setValueIsAdjusting(false);
        }
        else {
            remove(e.getChanged());
            int index = list.indexOf(e.getChanged());
            if(index != -1)
                listSelectionModel.removeSelectionInterval(index, index);
        }
        isAdjusting = false;
    }

    /**
     * Update selections to match those of the event.
     */
    public void selectionChanged(CollectionEvent e) {
        if(isAdjusting)
            return;

        //System.out.println("SequenceTableUI.selectionChanged");

        //Don't fire any new events until update complete
        isAdjusting = true;
        getCollection().clear(); //no event fired
        Collection c = e.getCollection();
        if(c != null) {
            getCollection().addAll(c); //no event fired
            listSelectionModel.setValueIsAdjusting(true);
            listSelectionModel.clearSelection();

            Iterator iter = c.iterator();
            while(iter.hasNext()) {
                int index = list.indexOf(iter.next());
                if(index != -1 && !listSelectionModel.isSelectedIndex(index))
                    listSelectionModel.addSelectionInterval(index, index);
            }
            listSelectionModel.setValueIsAdjusting(false);
        }
        isAdjusting = false;
    }
}
