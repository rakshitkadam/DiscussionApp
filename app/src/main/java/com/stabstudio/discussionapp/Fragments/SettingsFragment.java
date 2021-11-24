package com.stabstudio.discussionapp.Fragments;


import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.stabstudio.discussionapp.R;

import butterknife.BindView;
import butterknife.ButterKnife;


public class SettingsFragment extends Fragment {

    @BindView(R.id.notificationstatus) TextView status;
    @BindView(R.id.refreshlayout) SwipeRefreshLayout refreshLayout;
    @BindView(R.id.notification_recycler_view) RecyclerView recyclerView;

    public SettingsFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View vi = inflater.inflate(R.layout.fragment_settings, container, false);

        ButterKnife.bind(this, vi);
        status.setVisibility(View.VISIBLE);
        recyclerView.setVisibility(View.GONE);
        refreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refreshLayout.setRefreshing(false);
            }
        });

        return vi;
    }

}
