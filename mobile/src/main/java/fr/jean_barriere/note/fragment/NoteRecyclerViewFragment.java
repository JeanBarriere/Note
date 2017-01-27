package fr.jean_barriere.note.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.github.florent37.materialviewpager.header.MaterialViewPagerHeaderDecorator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import butterknife.BindView;
import butterknife.ButterKnife;
import fr.jean_barriere.note.manager.DataManager;
import fr.jean_barriere.note.R;
import fr.jean_barriere.note.adapter.NoteRecyclerViewAdapter;
import fr.jean_barriere.note.item.Item;

public class NoteRecyclerViewFragment extends Fragment {

    @BindView(R.id.recyclerView)
    RecyclerView mRecyclerView;

    private TextView mEmptyView;

    private NoteRecyclerViewAdapter mAdapter;

    private ArrayList<Item> mContentItems = new ArrayList<>();
    private Item.Type type;

    public static NoteRecyclerViewFragment newInstance(Item.Type type) {
        NoteRecyclerViewFragment fragment = new NoteRecyclerViewFragment();
        fragment.type = type;
        DataManager.registerRefreshListFragment(fragment);
        return fragment;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putSerializable("items", mContentItems);
        outState.putSerializable("type", type);
    }

    @Override
    public void onStart() {
        super.onStart();
        refresh();
    }

    @Override
    public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
        if (savedInstanceState != null) {
            DataManager.registerRefreshListFragment(this);
            mContentItems = (ArrayList<Item>) savedInstanceState.getSerializable("items");
            mAdapter = new NoteRecyclerViewAdapter(mContentItems, getActivity());
            mAdapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
                @Override
                public void onChanged() {
                    super.onChanged();
                    if (mContentItems.size() == 0) {
                        mRecyclerView.setVisibility(View.GONE);
                        mEmptyView.setVisibility(View.VISIBLE);
                    } else {
                        mRecyclerView.setVisibility(View.VISIBLE);
                        mEmptyView.setVisibility(View.GONE);
                    }
                }
            });
            type = (Item.Type) savedInstanceState.getSerializable("type");
            mRecyclerView.setAdapter(mAdapter);
            mAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_recyclerview, container, false);
    }

    public void refresh() {
        System.out.println("refresh called");
        mContentItems = new ArrayList<>();
        if (type == Item.Type.NOTE || type == Item.Type.ALL)
            mContentItems.addAll(DataManager.getNotes() == null ? new ArrayList<Item>() : new ArrayList<Item>(DataManager.getNotes()));
        if (type  == Item.Type.LIST || type == Item.Type.ALL)
            mContentItems.addAll(DataManager.getLists() == null ? new ArrayList<Item>() : new ArrayList<Item>(DataManager.getLists()));
        Collections.sort(mContentItems, new Comparator<Item>() {
            @Override
            public int compare(Item item, Item t1) {
                return (int)(item.getCreationDateTime().getTimeInMillis() - t1.getCreationDateTime().getTimeInMillis());
            }
        });
        if (mAdapter != null) {
            mAdapter.setContents(mContentItems);
            mAdapter.notifyDataSetChanged();
        }
    }

    public int getItemCount() {
        return mContentItems.size();
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ButterKnife.bind(this, view);

        mEmptyView = (TextView) view.findViewById(R.id.empty_view);
        RecyclerView.LayoutManager layoutManager;

        layoutManager = new LinearLayoutManager(getActivity());
        mRecyclerView.setLayoutManager(layoutManager);
        mRecyclerView.setHasFixedSize(true);

        //Use this now
        mRecyclerView.addItemDecoration(new MaterialViewPagerHeaderDecorator());

        mAdapter = new NoteRecyclerViewAdapter(mContentItems, getActivity());

        if (mContentItems.size() == 0) {
            mRecyclerView.setVisibility(View.GONE);
            mEmptyView.setVisibility(View.VISIBLE);
        } else {
            mRecyclerView.setVisibility(View.VISIBLE);
            mEmptyView.setVisibility(View.GONE);
        }

        mAdapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onChanged() {
                super.onChanged();
                if (mContentItems.size() == 0) {
                    mRecyclerView.setVisibility(View.GONE);
                    mEmptyView.setVisibility(View.VISIBLE);
                } else {
                    mRecyclerView.setVisibility(View.VISIBLE);
                    mEmptyView.setVisibility(View.GONE);
                }
            }
        });

        mRecyclerView.setAdapter(mAdapter);
        refresh();
    }
}