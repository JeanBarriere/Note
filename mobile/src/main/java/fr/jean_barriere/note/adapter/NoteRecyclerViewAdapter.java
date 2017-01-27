package fr.jean_barriere.note.adapter;

import android.app.Activity;
import android.content.Intent;
import android.database.DataSetObserver;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.util.Pair;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;

import fr.jean_barriere.note.activity.EditListActivity;
import fr.jean_barriere.note.activity.EditNoteActivity;
import fr.jean_barriere.note.item.Item;
import fr.jean_barriere.note.item.List;
import fr.jean_barriere.note.item.Note;
import fr.jean_barriere.note.R;

import static fr.jean_barriere.note.activity.MainActivity.REQUEST_REFRESH_LIST;
import static fr.jean_barriere.note.activity.MainActivity.REQUEST_REFRESH_NOTE;

public class NoteRecyclerViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    ArrayList<Item> contents;
    private Activity mainActivity;

    public NoteRecyclerViewAdapter(ArrayList<Item> contents, Activity activity) {
        this.contents = contents;
        this.mainActivity = activity;
    }

    public ArrayList<Item> getContents() {
        return contents;
    }

    public void setContents(ArrayList<Item> contents) {
        this.contents = contents;
    }

    @Override
    public int getItemViewType(int position) {
         return this.contents.get(position).getType() == Item.Type.NOTE ? 0 : 1;
    }

    @Override
    public int getItemCount() {
        return contents.size();
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = null;

        switch (viewType) {
            case 0: {
                view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.list_item_card_note, parent, false);
                return new RecyclerView.ViewHolder(view) {};
            }
            case 1 : {
                view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.list_item_card_list, parent, false);
                return new RecyclerView.ViewHolder(view) {};
            }
        }
        return null;
    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, final int position) {
        switch (getItemViewType(position)) {
            case 0:
                TextView tv = (TextView) holder.itemView.findViewById(R.id.note_content);
                tv.setText(((Note)contents.get(position)).getValue());
                ImageView iv = (ImageView) holder.itemView.findViewById(R.id.alarm);
                iv.setVisibility(contents.get(position).isReminder() ? View.VISIBLE : View.GONE);
                holder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent intent = new Intent(mainActivity, EditNoteActivity.class);
                        intent.putExtra("note", contents.get(position));
                        ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation(mainActivity, holder.itemView.findViewById(R.id.note_content), "content");
                        mainActivity.startActivityForResult(intent, REQUEST_REFRESH_NOTE, options.toBundle());
                    }
                });
                break;
            case 1:
                TextView tvv = (TextView) holder.itemView.findViewById(R.id.list_item_list_title);
                tvv.setText(((List)contents.get(position)).getTitle());
                final ListView lv = (ListView) holder.itemView.findViewById(R.id.list_item_card_list_values);
                final ListItemArrayAdapter adapter = new ListItemArrayAdapter(mainActivity, ((List)contents.get(position)).getValues(), (List)contents.get(position));
                lv.setAdapter(adapter);
                adapter.registerDataSetObserver(new DataSetObserver() {
                    @Override
                    public void onChanged() {
                        super.onChanged();
                        int totalHeight = 0;
                        for (int i = 0; i < adapter.getCount(); i++) {
                            View listItem = adapter.getView(i, null, lv);
                            listItem.measure(0, 0);
                            totalHeight += listItem.getMeasuredHeight();
                        }

                        ViewGroup.LayoutParams params = lv.getLayoutParams();
                        params.height = totalHeight + (lv.getDividerHeight() * (adapter.getCount() - 1));
                        lv.setLayoutParams(params);
                        lv.requestLayout();
                    }
                });
                adapter.notifyDataSetChanged();
                ImageView ivv = (ImageView) holder.itemView.findViewById(R.id.alarm);
                ivv.setVisibility(contents.get(position).isReminder() ? View.VISIBLE : View.GONE);
                holder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent intent = new Intent(mainActivity, EditListActivity.class);
                        intent.putExtra("list", contents.get(position));
                        Pair<View, String> p1 = Pair.create(holder.itemView.findViewById(R.id.list_item_list_title), "title");
                        Pair<View, String> p2 = Pair.create(holder.itemView.findViewById(R.id.list_item_card_list_values), "values");
                        ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation(mainActivity, p1, p2);
                        mainActivity.startActivityForResult(intent, REQUEST_REFRESH_LIST, options.toBundle());
                    }
                });
                break;
        }
    }
}
