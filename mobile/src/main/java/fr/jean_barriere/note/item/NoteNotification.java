package fr.jean_barriere.note.item;

/*
** Created by jean on 1/19/17.
*/

import android.app.AlarmManager;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.SystemClock;

import java.io.Serializable;
import java.util.Calendar;
import java.util.UUID;

import fr.jean_barriere.note.NoteApp;
import fr.jean_barriere.note.R;
import fr.jean_barriere.note.activity.EditListActivity;
import fr.jean_barriere.note.activity.EditNoteActivity;
import fr.jean_barriere.note.manager.DataManager;
import fr.jean_barriere.note.notification.NotificationPublisher;

public class NoteNotification implements Serializable {
    private String content;
    private final Integer uid;
    private final String id;
    private Long delay;
    private String parentId;

    public NoteNotification(String parentId, String notificationContent, Long delay) {
        this.content = notificationContent;
        this.delay = delay;
        UUID uuid = UUID.randomUUID();
        this.parentId = parentId;
        this.uid = (int)(uuid.getMostSignificantBits() + uuid.getLeastSignificantBits());
        this.id = uuid.toString();
    }

    public static Notification build(String notificationContent) {
        Notification.Builder builder = new Notification.Builder(NoteApp.getContext());
        builder.setContentTitle(NoteApp.getContext().getString(R.string.app_name));
        builder.setContentText(notificationContent);
        builder.setSmallIcon(R.drawable.ic_launcher_white);
        return builder.build();
    }

    public void schedule() {
        Intent notificationIntent = new Intent(NoteApp.getContext(), NotificationPublisher.class);
        notificationIntent.putExtra(NotificationPublisher.NOTIFICATION_ID, uid);
        notificationIntent.putExtra(NotificationPublisher.PARENT_ID, parentId);
        notificationIntent.putExtra(NotificationPublisher.CONTENT, content);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(NoteApp.getContext(), 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        long futureInMillis = SystemClock.elapsedRealtime() + delay;
        AlarmManager alarmManager = (AlarmManager)NoteApp.getContext().getSystemService(Context.ALARM_SERVICE);
        alarmManager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, futureInMillis, pendingIntent);
    }

    public void cancel() {
        Intent notificationIntent = new Intent(NoteApp.getContext(), NotificationPublisher.class);
        notificationIntent.putExtra(NotificationPublisher.NOTIFICATION_ID, uid);
        notificationIntent.putExtra(NotificationPublisher.PARENT_ID, parentId);
        notificationIntent.putExtra(NotificationPublisher.CONTENT, content);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(NoteApp.getContext(), 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        AlarmManager alarmManager = (AlarmManager)NoteApp.getContext().getSystemService(Context.ALARM_SERVICE);
        alarmManager.cancel(pendingIntent);
        pendingIntent.cancel();
    }


    public String getId() {
        return id;
    }

    public void setDelay(Long delay) {
        this.delay = delay;
        cancel();
        schedule();
    }

    public Long getDelay() {
        return delay;
    }

    @Override
    public boolean equals(Object obj) {
        return (obj != null && ((obj instanceof Integer && this.id == obj) || (obj instanceof NoteNotification && this.id.equals(((NoteNotification)obj).getId()))));
    }
}
