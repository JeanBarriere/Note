package fr.jean_barriere.note.adapter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import java.util.ArrayList;

import fr.jean_barriere.note.NoteApp;
import fr.jean_barriere.note.R;
import fr.jean_barriere.note.item.Item;
import fr.jean_barriere.note.fragment.NoteRecyclerViewFragment;
import fr.jean_barriere.note.manager.DataManager;

/*
** Created by jean on 1/11/17.
*/

public class NotePagerAdapter extends FragmentStatePagerAdapter {

    public NotePagerAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case 0:
                return NoteRecyclerViewFragment.newInstance(Item.Type.ALL);
            case 1:
                return NoteRecyclerViewFragment.newInstance(Item.Type.NOTE);
            case 2:
                return NoteRecyclerViewFragment.newInstance(Item.Type.LIST);
        }
        return null;
    }

    @Override
    public int getCount() {
        return 3;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        switch (position) {
            case 0 : return NoteApp.getContext().getString(R.string.home_viewpager_all);
            case 1 : return NoteApp.getContext().getString(R.string.home_viewpager_notes);
            case 2 : return NoteApp.getContext().getString(R.string.home_viewpager_lists);
            default: return NoteApp.getContext().getString(R.string.home_viewpager_none);
        }
    }
}
