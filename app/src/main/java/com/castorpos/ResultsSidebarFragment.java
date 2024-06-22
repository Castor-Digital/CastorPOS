package com.castorpos;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import java.util.ArrayList;

public class ResultsSidebarFragment extends Fragment {

    private ListView resultsListView;
    private ArrayAdapter<String> resultsAdapter;
    private ArrayList<String> resultsList;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_results_sidebar, container, false);

        resultsListView = view.findViewById(R.id.results_list_view);

        // Initialize the results list and adapter
        resultsList = new ArrayList<>();
        resultsAdapter = new ArrayAdapter<>(requireContext(), R.layout.saved_result_list_item, R.id.saved_result, resultsList);
        resultsListView.setAdapter(resultsAdapter);

        return view;
    }

    // Method to add a result to the list
    public void addResult(String result) {
        resultsList.add(result);
        resultsAdapter.notifyDataSetChanged();
    }

    // Method to remove a result from the list
    public void removeResult(int position) {
        if (position >= 0 && position < resultsList.size()) {
            resultsList.remove(position);
            resultsAdapter.notifyDataSetChanged();
        }
    }
}
