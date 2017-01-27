package fr.jean_barriere.note.notification;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import fr.jean_barriere.note.NoteApp;
import fr.jean_barriere.note.R;
import fr.jean_barriere.note.activity.EditListActivity;
import fr.jean_barriere.note.activity.EditNoteActivity;
import fr.jean_barriere.note.item.Item;
import fr.jean_barriere.note.manager.DataManager;

/*
** Created by jean on 1/19/17.
*/

public class NotificationPublisher extends BroadcastReceiver {

    public static String NOTIFICATION_ID = "notification-id";
    public static String NOTIFICATION = "notification";
    public static String PARENT_ID = "parent-id";
    public static String CONTENT = "content";

    public void onReceive(Context context, Intent intent) {

        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        int id = intent.getIntExtra(NOTIFICATION_ID, 0);
        String parentId = intent.getStringExtra(PARENT_ID);
        String content = intent.getStringExtra(CONTENT);

        Notification.Builder builder = new Notification.Builder(NoteApp.getContext());

        builder.setContentTitle(NoteApp.getContext().getString(R.string.app_name));
        builder.setContentText(content);
        builder.setSmallIcon(R.drawable.ic_launcher_white);

        Item parent = DataManager.getItemFromId(parentId);
        System.out.println("parent = " + parent);
        if (parent != null && parent.getType().equals(Item.Type.NOTE)) {
            Intent contentIntent = new Intent(NoteApp.getContext(), EditNoteActivity.class);
            contentIntent.putExtra("note", parent);
            contentIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            builder.setContentIntent(PendingIntent.getActivity(NoteApp.getContext(), 0, contentIntent, PendingIntent.FLAG_UPDATE_CURRENT));
        } else if (parent != null && parent.getType().equals(Item.Type.LIST)) {
            Intent contentIntent = new Intent(NoteApp.getContext(), EditListActivity.class);
            contentIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            contentIntent.putExtra("list", parent);
            builder.setContentIntent(PendingIntent.getActivity(NoteApp.getContext(), 0, contentIntent, PendingIntent.FLAG_UPDATE_CURRENT));
        }

        notificationManager.notify(id, builder.build());
    }
}