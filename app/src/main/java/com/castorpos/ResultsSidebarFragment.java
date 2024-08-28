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

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import java.util.ArrayList;
import java.util.List;

public class ResultsSidebarFragment extends Fragment {
    private RecyclerView recyclerView;
    private ResultsAdapter adapter;
    private static List<SavedResult> savedResults = new ArrayList<>();
    private static List<SavedResult> creditResults = new ArrayList<>();
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
        adapter = new ResultsAdapter(getContext(), savedResults, creditResults, this);
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
        executorService.execute(new Runnable() {
            @Override
            public void run() {
                List<SavedResult> results = database.resultsDao().getAllResults();

                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        adapter.setResults(results);
                    }
                });
            }
        });
    }

    public void saveResult(SavedResult result) {
        executorService.execute(new Runnable() {
            @Override
            public void run() {
                int count = database.resultsDao().resultExists(result.getTime());
                if (count == 0) { // Insert only if the result does not exist
                    database.resultsDao().insert(result);
                }
            }
        });
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

    public void deleteResult(SavedResult result) {
        int position = -1;

        // Check if the result is in the savedResults list and remove it
        if (savedResults.contains(result)) {
            position = savedResults.indexOf(result);
            savedResults.remove(result);
        }
        // Check if the result is in the creditResults list and remove it
        else if (creditResults.contains(result)) {
            position = savedResults.size() + creditResults.indexOf(result);
            creditResults.remove(result);
        }

        // Remove the result from the database
        AppDatabase database = AppDatabase.getDatabase(getContext());
        ResultsDao resultsDao = database.resultsDao();

        final int finalPosition = position;
        new Thread(() -> {
            resultsDao.delete(result);

            // Run on UI thread to update the adapter and sidebar
            if (getActivity() != null) {
                getActivity().runOnUiThread(() -> {
                    if (adapter != null) {
                        if (finalPosition != -1) {
                            adapter.notifyItemRemoved(finalPosition);
                        } else {
                            adapter.notifyDataSetChanged();
                        }
                    } else {
                        Log.e("ResultsSidebarFragment", "Adapter is null.");
                    }
                });
            } else {
                Log.e("ResultsSidebarFragment", "Activity is null.");
            }
        }).start();
    }

}
