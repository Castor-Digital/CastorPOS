// ResultsSidebarFragment.java
package com.castorpos;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import androidx.fragment.app.Fragment;

import java.util.ArrayList;
import java.util.List;

public class ResultsSidebarFragment extends Fragment {

    private List<SavedResult> savedResults;
    private ResultsAdapter resultsAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_results_sidebar, container, false);

        savedResults = new ArrayList<>();
        ListView resultsListView = view.findViewById(R.id.results_list_view);
        resultsAdapter = new ResultsAdapter(getContext(), savedResults);
        resultsListView.setAdapter(resultsAdapter);

        return view;
    }

    public void addResult(SavedResult result) {
        savedResults.add(result);
        resultsAdapter.notifyDataSetChanged();
    }

    // Implement ResultsAdapter class if not already present
}

