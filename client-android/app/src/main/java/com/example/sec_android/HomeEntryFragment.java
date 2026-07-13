package com.example.sec_android;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class HomeEntryFragment extends Fragment {
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home_entry, container, false);
        TextView orderButton = view.findViewById(R.id.btn_entry_order);
        TextView recommendButton = view.findViewById(R.id.btn_entry_recommend);

        orderButton.setOnClickListener(v ->
                startActivity(new Intent(requireContext(), OrderActivity.class)));
        recommendButton.setOnClickListener(v ->
                startActivity(new Intent(requireContext(), RecommendActivity.class)));
        return view;
    }
}
