package fr.jean_barriere.note.adapter;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Paint;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.rengwuxian.materialedittext.MaterialEditText;

import java.util.*;

import fr.jean_barriere.note.*;
import fr.jean_barriere.note.item.List;
import fr.jean_barriere.note.item.ListItem;
import fr.jean_barriere.note.manager.DataManager;

/*
** Created by jean on 1/17/17.
*/

public class ListItemArrayAdapter extends ArrayAdapter<ListItem> {

    private Context ctx;
    private Activity mainActivity;

    private fr.jean_barriere.note.item.List parentList;

    public ListItemArrayAdapter(Activity activity, ArrayList<ListItem> objects) {
        super(activity, 0, objects);
        this.ctx = activity;
        this.mainActivity = activity;
    }

    public ListItemArrayAdapter(Activity activity, ArrayList<ListItem> objects, List parent) {
        super(activity, 0, objects);
        this.ctx = activity;
        this.mainActivity = activity;
        this.parentList = parent;
    }

    @Override
    public View getView(final int position, View convertView, final ViewGroup parent) {
        final ListItem item = getItem(position);
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.list_item_list_item, parent, false);
        }

        final TextView tvText = (TextView) convertView.findViewById(R.id.list_item_text);
        final CheckBox chxDone = (CheckBox) convertView.findViewById(R.id.list_item_done);
        final ImageView ivOptions = (ImageView) convertView.findViewById(R.id.list_item_options);
        FrameLayout flRoot = (FrameLayout) convertView.findViewById(R.id.list_item_flroot);

            ivOptions.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(ctx);
                    String[] items = new String[3];
                    items[0] = chxDone.isChecked() ? "Undone" : "Done";
                    items[1] = "Edit";
                    items[2] = "Delete";
                    builder.setTitle("")
                            .setItems(items, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    switch (which) {
                                        case 0:
                                            chxDone.setChecked(!chxDone.isChecked());
                                            break;
                                        case 1:
                                            if (mainActivity != null && mainActivity.findViewById(R.id.add_list_content) != null) {
                                                ((MaterialEditText) mainActivity.findViewById(R.id.add_list_content)).setText(item.getValue());
                                                ((MaterialEditText) mainActivity.findViewById(R.id.add_list_content)).setSelection(((MaterialEditText) mainActivity.findViewById(R.id.add_list_content)).length());
                                                remove(item);
                                                notifyDataSetChanged();
                                            } else if (mainActivity != null && mainActivity.findViewById(R.id.add_list_item) != null) {
                                                ((MaterialEditText) mainActivity.findViewById(R.id.add_list_item)).setText(item.getValue());
                                                ((MaterialEditText) mainActivity.findViewById(R.id.add_list_item)).setSelection(((MaterialEditText) mainActivity.findViewById(R.id.add_list_item)).length());
                                                remove(item);
                                                notifyDataSetChanged();
                                            } else {
                                                Toast.makeText(mainActivity, getContext().getString(R.string.cannot_edit), Toast.LENGTH_LONG).show();
                                            }
                                            break;
                                        case 2:
                                            remove(item);
                                            notifyDataSetChanged();
                                            break;
                                    }
                                }
                            }).create().show();
                    return true;
                }
            });

            ivOptions.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    chxDone.setChecked(!chxDone.isChecked());
                }
            });

            flRoot.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    ivOptions.performClick();
                }
            });
            flRoot.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    ivOptions.performLongClick();
                    return true;
                }
            });

        tvText.setText((item.getValue() == null ? "" : item.getValue()));
        chxDone.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (item.isDone() != b && parentList != null) {
                    parentList.getValues().get(position).setDone(b);
                    DataManager.saveList(parentList);
                    DataManager.notifyRefreshListFragment();
                }
                item.setDone(b);
                if (item.isDone())
                    tvText.setPaintFlags(tvText.getPaintFlags() |  Paint.STRIKE_THRU_TEXT_FLAG);
                else
                    tvText.setPaintFlags(tvText.getPaintFlags() & ~Paint.STRIKE_THRU_TEXT_FLAG);
            }
        });
        chxDone.setChecked(item.isDone());
        return convertView;
    }
}
