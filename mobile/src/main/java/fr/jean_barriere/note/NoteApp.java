package fr.jean_barriere.note;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;

import fr.jean_barriere.note.manager.DataManager;

/*
* Created by jean on 1/13/17.
*/

public class NoteApp extends Application {
    private static Context context;

    public void onCreate() {
        super.onCreate();
        NoteApp.context = getApplicationContext();
//        DataManager.deleteAll();
    }

    public static Context getContext() {
        return NoteApp.context;
    }
}
