package fr.jean_barriere.note.item;

import java.io.Serializable;

/*
** Created by jean on 1/17/17.
*/

public class ListItem  implements Serializable {
    private String value;
    private boolean done;


    public ListItem(String value, boolean done) {
        this.value = value;
        this.done = done;
    }

    public String getValue() {
        return value;
    }

    public boolean isDone() {
        return done;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public void setDone(boolean done) {
        this.done = done;
    }
}
