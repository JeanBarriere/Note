package fr.jean_barriere.note.manager;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Parcelable;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;

import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Calendar;

import fr.jean_barriere.note.NoteApp;
import fr.jean_barriere.note.activity.MainActivity;
import fr.jean_barriere.note.fragment.NoteRecyclerViewFragment;
import fr.jean_barriere.note.item.Item;
import fr.jean_barriere.note.item.List;
import fr.jean_barriere.note.item.Note;

/*
** Created by jean on 1/13/17.
*/

public class DataManager {

    private static final Type NOTE_TYPE = new TypeToken<ArrayList<Note>>() {}.getType();
    private static final Type LIST_TYPE = new TypeToken<ArrayList<List>>() {}.getType();

    private static SharedPreferences prefs = NoteApp.getContext().getSharedPreferences("fr.jean_barriere.note.noteprefs", Context.MODE_PRIVATE);

    private static ArrayList<Note> notes = null;
    private static ArrayList<List> lists = null;
    private static ArrayList<NoteRecyclerViewFragment> fragments = null;
    private static ArrayList<MainActivity> activities = null;

    public static void deleteAll() {
        prefs.edit().clear().commit();
    }

    public static void saveNote(Note item) {
        notes = new Gson().fromJson(prefs.getString("notesList", null), NOTE_TYPE);
        if (notes == null)
            notes = new ArrayList<>();

        if (notes.contains(item))
            notes.set(notes.indexOf(item), item);
        else
            notes.add(item);

        prefs.edit().putString("notesList", new Gson().toJson(notes)).commit();
    }

    public static boolean isGoogleAccountLinked() {
        return prefs.getBoolean("googleAccount", false);
    }

    public static void setGoogleAccountLink(boolean value) {
        prefs.edit().putBoolean("googleAccount", value).commit();
    }

    public static void deleteNote(Note item) {
        notes = new Gson().fromJson(prefs.getString("notesList", null), NOTE_TYPE);
        if (notes != null && notes.contains(item)) {
            notes.remove(notes.indexOf(item));
            prefs.edit().putString("notesList", new Gson().toJson(notes)).commit();
        }
    }

    public static void saveList(List list) {
        lists = new Gson().fromJson(prefs.getString("listsList", null), LIST_TYPE);
        if (lists == null)
            lists = new ArrayList<>();

        if (lists.contains(list))
            lists.set(lists.indexOf(list), list);
        else
            lists.add(list);
        prefs.edit().putString("listsList", new Gson().toJson(lists)).commit();
    }

    public static void deleteList(List item) {
        lists = new Gson().fromJson(prefs.getString("listsList", null), LIST_TYPE);
        if (lists != null && lists.contains(item)) {
            lists.remove(lists.indexOf(item));
            prefs.edit().putString("listsList", new Gson().toJson(lists)).commit();
        }
    }

    public static ArrayList<Note> getNotes() {
        try {
            if (notes == null)
                notes = new Gson().fromJson(prefs.getString("notesList", null), NOTE_TYPE);
        } catch (JsonSyntaxException e) {
            deleteAll();
        } finally {
            return notes;
        }
    }

    public static ArrayList<List> getLists() {
        try {
            if (lists == null)
                lists = new Gson().fromJson(prefs.getString("listsList", null), LIST_TYPE);
        } catch (JsonSyntaxException e) {
            deleteAll();
        } finally {
            return lists;
        }
    }

    public static Item getItemFromId(String id) {
        notes = getNotes();
        lists = getLists();
        if (notes != null)
            for (Note note : notes)
                if (note.getId().equals(id))
                    return note;
        if (lists != null)
            for (List list : lists)
                if (list.getId().equals(id))
                    return list;
        return null;
    }

    public static void registerRefreshListFragment(NoteRecyclerViewFragment fragment) {
        if (fragments == null)
            fragments = new ArrayList<>();
        fragments.add(fragment);
    }

    public static void registerMainActivity(MainActivity activity) {
        if (activities == null)
            activities = new ArrayList<>();
        for (MainActivity act : activities) {
            if (act.id.equals(activity.id))
                return;
        }
        activities.add(activity);
    }

    public static void notifyRefreshListFragment() {
        if (fragments != null)
            for (NoteRecyclerViewFragment fragment : fragments)
                fragment.refresh();
        notifiyMainActivity();
    }

    public static void notifiyMainActivity() {
//        if (activities != null)
//            for (MainActivity activity : activities)
//                activity.updateDrive();
    }


    public static JsonObject getAsJsonObject() {
        boolean googleAccount = prefs.getBoolean("googleAccount", false);
        notes = new Gson().fromJson(prefs.getString("notesList", null), NOTE_TYPE);
        lists = new Gson().fromJson(prefs.getString("listsList", null), LIST_TYPE);
        JsonObject ret = new JsonObject();
        ret.add("lists", new Gson().toJsonTree(lists));
        ret.add("notes", new Gson().toJsonTree(notes));
        ret.add("googleAccount", new Gson().toJsonTree(googleAccount));
        System.out.println("ret = " + ret.toString());
        return ret;
    }

    public static void saveJsonAsData(JSONObject data) {
        try {
            if (data.isNull("lists"))
                lists = null;
            else
                lists = new Gson().fromJson(data.getJSONArray("lists").toString(), LIST_TYPE);
            if (data.isNull("notes"))
                notes = null;
            else
                notes = new Gson().fromJson(data.getJSONArray("notes").toString(), NOTE_TYPE);
            boolean value = data.getBoolean("googleAccount");

//            if (tmpNotes != null)
//                for (Note n : tmpNotes) {
//                    if (notes.contains(n)) {
//                        if (n.getModificationDateTime() != null && n.getModificationDateTime().after(notes.get(notes.indexOf(n))))
//                            notes.set(notes.indexOf(n), n);
//                    } else {
//                        Calendar firstAddedNote
//                        for (Note n : notes) {
//                            if (n.getCreationDateTime().before(n))
//                        }
//                        notes.add(n);
//                    }
//                }
//
//            if (tmpLists != null)
//                for (List l : tmpLists) {
//                    if (lists.contains(l)) {
//                        if (l.getModificationDateTime() != null && l.getModificationDateTime().after(lists.get(lists.indexOf(l))))
//                            lists.set(lists.indexOf(l), l);
//                    } else {
//                        lists.add(l);
//                    }
//                }

            prefs.edit().putString("listsList", new Gson().toJson(lists)).commit();
            prefs.edit().putString("notesList", new Gson().toJson(notes)).commit();
            prefs.edit().putBoolean("googleAccount", value).commit();
            notifyRefreshListFragment();
        } catch (JSONException e) { e.printStackTrace(); }
    }
}
