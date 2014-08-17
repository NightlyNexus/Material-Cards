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
    private static final int SCROLL_TO_TOP_MILLIS = 300;

    private RecyclerView.LayoutManager mLayoutManager;
    private RecyclerView mRecyclerView;
    private CardAdapter mAdapter;
    private ArrayList<PaletteColor> mColors = null;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return mRecyclerView = (RecyclerView) inflater.inflate(R.layout.fragment_color_palette, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (savedInstanceState != null) {
            mColors = savedInstanceState.getParcelableArrayList(COLORS_KEY);
        }
        if (mColors == null) {
            ArrayList<PaletteColor> colors = getArguments().getParcelableArrayList(ARG_COLORS);
            mColors = new ArrayList<PaletteColor>(colors);
        }
        mLayoutManager = new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false);
        mAdapter = new CardAdapter(getActivity(), mColors);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.addItemDecoration(new RecyclerView.ItemDecoration() {
            @Override
            public void getItemOffsets(Rect outRect, int itemPosition, RecyclerView parent) {
                outRect.set(0, 0, 0, (int) getResources().getDimension(R.dimen.card_spacing));
            }
        });
        mRecyclerView.setHasFixedSize(true);
    }

    public boolean isColorsAdded() {
        return mColors != null;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putParcelableArrayList(COLORS_KEY, mColors);
        super.onSaveInstanceState(outState);
    }

    public void replaceColorCardList(ArrayList<PaletteColor> colors) {
        mColors.clear();
        mColors.addAll(colors);
        mAdapter.notifyDataSetChanged();
        if (mAdapter.getItemCount() > 0) {
            mRecyclerView.scrollToPosition(0);
        }
    }

    public void scrollToTop() {
        if (mColors.size() > 0) {
            mRecyclerView.smoothScrollToPosition(0);
            //mRecyclerView.smoothScrollToPositionFromTop(0, 0, SCROLL_TO_TOP_MILLIS);
        }
    }
}
