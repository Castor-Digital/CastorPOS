package com.castorpos;

import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import java.util.ArrayList;
import java.util.List;

public class ResultsSidebarFragment extends Fragment {
    private RecyclerView recyclerView;
    private ResultsAdapter adapter;
    private List<SavedResult> savedResults = new ArrayList<>();
    private List<SavedResult> creditResults = new ArrayList<>();
    private AppDatabase database;
    private ExecutorService executorService;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_results_sidebar, container, false);
        recyclerView = view.findViewById(R.id.results_recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        database = AppDatabase.getDatabase(getContext());
        executorService = Executors.newSingleThreadExecutor();
        adapter = new ResultsAdapter(getContext(), savedResults, creditResults);
        recyclerView.setAdapter(adapter);
        loadResults();

        view.findViewById(R.id.buttonTotalsScreen).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), TotalsScreen.class);
                startActivity(intent);
            }
        });
        return view;
    }


    private void loadResults() {
        executorService.execute(() -> {
            List<SavedResult> results = database.resultsDao().getAllResults();
            if (results != null) {
                savedResults.clear();
                creditResults.clear();

                for (SavedResult result : results) {
                    if (result.isCredit()) {
                        creditResults.add(result);
                    } else {
                        savedResults.add(result);
                    }
                }

                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> adapter.notifyDataSetChanged());
                }
            }
        });
    }

    public void addResult(SavedResult result) {
        if (result.isCredit()) {
            creditResults.add(result);
            if (adapter != null) {
                adapter.notifyItemInserted(creditResults.size() - 1);
            }
        } else {
            savedResults.add(result);
            if (adapter != null) {
                adapter.notifyItemInserted(savedResults.size() - 1);
            }
        }
    }

    public void refreshSidebar() {
        new Thread(() -> {
            // Fetch fresh data from the database
            List<SavedResult> freshResults = AppDatabase.getDatabase(getContext()).resultsDao().getAllResults();

            // Update the adapter on the UI thread
            getActivity().runOnUiThread(() -> {
                adapter.setResults(freshResults);
                adapter.notifyDataSetChanged();
            });
        }).start();
    }


    // Method to delete result from the database and the sidebar
    public void deleteResult(SavedResult result) {
        // Remove from the database
        AppDatabase database = AppDatabase.getDatabase(getContext());
        ResultsDao resultsDao = database.resultsDao();

        new Thread(() -> {
            resultsDao.delete(result);

            // Run on UI thread to update the sidebar
            getActivity().runOnUiThread(() -> {
                adapter.removeResult(result);
            });
        }).start();
    }




}
