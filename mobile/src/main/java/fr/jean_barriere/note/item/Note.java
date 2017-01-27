package fr.jean_barriere.note.item;

import java.io.Serializable;
import java.util.Calendar;

/*
** Created by jean on 1/13/17.
*/

public class Note extends Item implements Serializable {
    private String value;

    public Note(String value) {
        super(Type.NOTE);
        this.value = value;
        setReminder(false);
    }

    public Note(String value, Calendar dueDate) {
        super(Type.NOTE);
        this.value = value;
        super.setDueDate(dueDate);
        super.setNotification(value);
    }

    @Override
    public void updateNotification() {
        if(isReminder())
            super.setNotification(value);
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
        super.setNotification(value);
    }

}
