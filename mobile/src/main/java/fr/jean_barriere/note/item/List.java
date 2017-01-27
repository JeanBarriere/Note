package fr.jean_barriere.note.item;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;

/*
** Created by jean on 1/16/17.
*/

public class List extends Item implements Serializable {
    private String title;
    private ArrayList<ListItem> values;

    public List(String title, ArrayList<ListItem> values) {
        super(Type.LIST);
        this.title = title;
        this.values = new ArrayList<>(values);
        setReminder(false);
    }

    public List(String title, ArrayList<ListItem> values, Calendar dueDate) {
        super(Type.LIST);
        this.title = title;
        this.values = new ArrayList<>(values);
        super.setDueDate(dueDate);
        super.setNotification(title);
    }

    @Override
    public void updateNotification() {
        if (isReminder())
            super.setNotification(title);
    }

    public ArrayList<ListItem> getValues() {
        return values;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setValues(ArrayList<ListItem> values) {
        this.values = values;
    }
}
