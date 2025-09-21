package com.ead.zap.ui.owner;


import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.fragment.app.Fragment;
import com.ead.zap.R;

public class OwnerHomeFragment extends Fragment {

    public OwnerHomeFragment() {
        // Required empty public constructor
    }



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_owner_home, container, false);

        // You can add any initialization code here
        // For example, setting up click listeners, loading data, etc.

        return view;
    }
}