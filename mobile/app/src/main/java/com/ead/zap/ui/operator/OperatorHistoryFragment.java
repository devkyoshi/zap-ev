package com.ead.zap.ui.operator;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.ead.zap.R;
import com.ead.zap.ui.operator.adapters.SessionHistoryAdapter;
import com.ead.zap.ui.operator.models.SessionHistoryItem;

import java.util.ArrayList;
import java.util.List;

public class OperatorHistoryFragment extends Fragment {

    private RecyclerView recyclerViewHistory;
    private SessionHistoryAdapter adapter;
    private List<SessionHistoryItem> historyItems;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_operator_history, container, false);
        
        initViews(view);
        setupRecyclerView();
        loadHistoryData();
        
        return view;
    }

    private void initViews(View view) {
        recyclerViewHistory = view.findViewById(R.id.recycler_view_history);
    }

    private void setupRecyclerView() {
        historyItems = new ArrayList<>();
        adapter = new SessionHistoryAdapter(historyItems);
        recyclerViewHistory.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerViewHistory.setAdapter(adapter);
    }

    private void loadHistoryData() {
        // Mock data for demonstration
        historyItems.add(new SessionHistoryItem("BK12345", "John Doe", "ST001", "A1", "Oct 2, 2025", "2:00 PM - 4:00 PM", "5.2 kWh", "Completed"));
        historyItems.add(new SessionHistoryItem("BK12344", "Jane Smith", "ST001", "B2", "Oct 2, 2025", "12:00 PM - 1:30 PM", "3.8 kWh", "Completed"));
        historyItems.add(new SessionHistoryItem("BK12343", "Mike Johnson", "ST002", "A1", "Oct 1, 2025", "3:00 PM - 5:00 PM", "6.1 kWh", "Completed"));
        historyItems.add(new SessionHistoryItem("BK12342", "Sarah Williams", "ST001", "C3", "Oct 1, 2025", "10:00 AM - 11:30 AM", "4.3 kWh", "Completed"));
        historyItems.add(new SessionHistoryItem("BK12341", "David Brown", "ST002", "B1", "Sep 30, 2025", "4:00 PM - 6:30 PM", "7.2 kWh", "Completed"));
        
        adapter.notifyDataSetChanged();
    }
}