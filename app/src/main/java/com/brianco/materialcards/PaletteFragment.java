package com.brianco.materialcards;

import android.app.Fragment;
import android.graphics.Rect;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.brianco.materialcards.adapter.CardAdapter;
import com.brianco.materialcards.model.PaletteColor;

import java.util.ArrayList;

public class PaletteFragment extends Fragment {

    public static final String ARG_COLORS = "ARG_COLORS";

    private static final String COLORS_KEY = "COLORS_KEY";

    private RecyclerView mRecyclerView;
    private RecyclerView.LayoutManager mLayoutManager;
    private CardAdapter mAdapter;
    private ArrayList<PaletteColor> mColors;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return mRecyclerView = (RecyclerView) inflater.inflate(R.layout.fragment_color_palette, container, false);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null) {
            mColors = savedInstanceState.getParcelableArrayList(COLORS_KEY);
        } else {
            ArrayList<PaletteColor> colors = getArguments().getParcelableArrayList(ARG_COLORS);
            mColors = new ArrayList<PaletteColor>(colors);
        }
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mLayoutManager = new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false);
        mAdapter = new CardAdapter(getActivity(), mColors);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.addItemDecoration(new RecyclerView.ItemDecoration() {
            @Override
            public void getItemOffsets(Rect outRect, View view,
                                       RecyclerView parent, RecyclerView.State state) {
                if (parent.getChildPosition(view) < mAdapter.getItemCount() - 1)
                outRect.set(0, 0, 0, (int) getResources().getDimension(R.dimen.card_spacing));
            }
        });
        mRecyclerView.setHasFixedSize(true);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putParcelableArrayList(COLORS_KEY, mColors);
        super.onSaveInstanceState(outState);
    }

    public void replaceColorCardList(ArrayList<PaletteColor> colors) {
        mColors.clear();
        mColors.addAll(colors);
        mAdapter.notifyItemRangeChanged(0, mColors.size());
        if (mAdapter.getItemCount() > 0) {
            mRecyclerView.scrollToPosition(0);
        }
    }

    public void scrollToTop() {
        if (mAdapter.getItemCount() > 0) {
            mRecyclerView.smoothScrollToPosition(0);
        }
    }
}
