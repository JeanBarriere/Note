package fr.jean_barriere.note.item;

/*
** Created by jean on 1/17/17.
*/

import java.io.Serializable;
import java.util.Calendar;
import java.util.UUID;

public abstract class Item implements Serializable {
    public enum Type { NOTE, LIST, ALL }

    private final String id;
    private Calendar creationDateTime;
    private boolean reminder;
    private Calendar dueDate;
    private Calendar modificationDateTime;
    private NoteNotification noteNotification;
    private Type type;

    public Item(Type type) {
        this.id = UUID.randomUUID().toString();
        this.creationDateTime = Calendar.getInstance();
        this.modificationDateTime = Calendar.getInstance();
        this.type = type;
    }

    public Calendar getModificationDateTime() {
        return modificationDateTime;
    }

    public void setModificationDateTime(Calendar modificationDateTime) {
        this.modificationDateTime = modificationDateTime;
    }

    public void updateModificationDateTime() {
        this.modificationDateTime = Calendar.getInstance();
    }

    private void setNoteNotification(NoteNotification noteNotification) {
        this.noteNotification = noteNotification;
        noteNotification.schedule();
    }

    public void deleteNotification() {
        if (this.noteNotification != null) {
            this.noteNotification.cancel();
            this.noteNotification = null;
        }
    }

    public NoteNotification getNoteNotification() {
        return noteNotification;
    }

    public Type getType() {
        return type;
    }

    public Calendar getCreationDateTime() {
        return creationDateTime;
    }

    public String getId() {
        return id;
    }

    public void setDueDate(Calendar dueDate) {
        this.dueDate = dueDate;
        this.reminder = (dueDate != null);
    }

    public abstract void updateNotification();

    public void setNotification(String content) {
        if (content == null && this.reminder)
            deleteNotification();
        if (this.noteNotification != null)
            noteNotification.cancel();
        if (this.dueDate != null)
            this.dueDate.set(Calendar.SECOND, 0);
        if (this.reminder)
            setNoteNotification(new NoteNotification(id, content, this.dueDate.getTimeInMillis() - Calendar.getInstance().getTimeInMillis()));
    }

    public void removeDueDate() {
        this.reminder = false;
        this.dueDate = null;
        if (this.noteNotification != null)
            this.noteNotification.cancel();
    }

    public Calendar getDueDate() {
        if (dueDate != null && dueDate.before(Calendar.getInstance())) {
            return null;
        }
        return dueDate;
    }

    public void setReminder(boolean reminder) {
        this.reminder = reminder;
    }

    public boolean isReminder() {
        if (dueDate != null && dueDate.before(Calendar.getInstance()))
            return false;
        return reminder;
    }

    @Override
    public boolean equals(Object obj) {
        return (obj != null && (obj instanceof Item) && this.id.equals(((Item) obj).getId()));
    }
}
