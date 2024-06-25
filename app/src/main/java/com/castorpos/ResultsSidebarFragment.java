package com.castorpos;

import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import java.util.List;

public class ResultsSidebarFragment extends Fragment {
    private RecyclerView recyclerView;
    private ResultsAdapter adapter;
    private List<SavedResult> results;

    // Factory method to create a new instance of this fragment
    public static ResultsSidebarFragment newInstance(List<SavedResult> results) {
        ResultsSidebarFragment fragment = new ResultsSidebarFragment();
        fragment.setResults(results);
        return fragment;
    }

    // Setter method to set the results list
    public void setResults(List<SavedResult> results) {
        this.results = results;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_results_sidebar, container, false);

        recyclerView = view.findViewById(R.id.results_recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        adapter = new ResultsAdapter(results);
        recyclerView.setAdapter(adapter);
        

        return view;
    }

    public void addResult(SavedResult result) {
        results.add(result);
        adapter.notifyItemInserted(results.size() - 1);
    }
}
