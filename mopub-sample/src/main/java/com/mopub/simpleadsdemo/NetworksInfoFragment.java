package com.mopub.simpleadsdemo;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.mopub.common.MoPub;

import java.util.ArrayList;
import java.util.List;

import static android.support.v7.widget.DividerItemDecoration.VERTICAL;

public class NetworksInfoFragment extends Fragment {
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.networks_info_fragment, container, false);

        final List<String> networks = MoPub.getAdapterConfigurationInfo();
        if (networks != null && !networks.isEmpty()) {
            RecyclerView recyclerView = view.findViewById(R.id.networks_recycler_view);
            recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
            recyclerView.setAdapter(new NetworksInfoAdapter(networks));
            recyclerView.addItemDecoration(new DividerItemDecoration(recyclerView.getContext(), VERTICAL));

            view.findViewById(R.id.text_no_adapters).setVisibility(View.GONE);
        }

        final Button closeButton = view.findViewById(R.id.networks_close_btn);
        closeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Activity activity = getActivity();
                if (activity != null) {
                    activity.onBackPressed();
                }
            }
        });
        return view;
    }

    class NetworksInfoAdapter extends RecyclerView.Adapter<NetworksInfoAdapter.ViewHolder> {
        @NonNull
        final List<String> mNetworksInfo;

        NetworksInfoAdapter(@Nullable final List<String> networksInfo) {
            if (networksInfo != null) {
                mNetworksInfo = networksInfo;
            } else {
                mNetworksInfo = new ArrayList<>();
            }
        }

        @NonNull
        @Override
        public NetworksInfoAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
            final View itemView = LayoutInflater.from(viewGroup.getContext())
                    .inflate(android.R.layout.simple_selectable_list_item, viewGroup, false);
            final NetworksInfoAdapter.ViewHolder viewHolder = new NetworksInfoAdapter.ViewHolder(itemView);
            itemView.setTag(viewHolder);
            return viewHolder;
        }

        @Override
        public void onBindViewHolder(@NonNull NetworksInfoAdapter.ViewHolder viewHolder, int i) {
            viewHolder.nameTextView.setText(mNetworksInfo.get(i));
        }

        @Override
        public int getItemCount() {
            return mNetworksInfo.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            final TextView nameTextView;

            ViewHolder(View itemView) {
                super(itemView);
                nameTextView = itemView.findViewById(android.R.id.text1);
            }
        }
    }
}
