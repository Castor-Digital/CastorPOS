
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
        adapter = new ResultsAdapter(getContext(), savedResults, creditResults, database, executorService);
        recyclerView.setAdapter(adapter);
        loadResults();  // Load and display results

        // Navigate to TotalsScreen
        view.findViewById(R.id.buttonTotalsScreen).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), TotalsScreen.class);
                startActivity(intent);
            }
        });
        return view;
    }

    // Fetch results from the database and refresh the RecyclerView
    void loadResults() {
        executorService.execute(() -> {
            // Fetch cash and credit results from the database
            List<SavedResult> cashResults = database.resultsDao().getCashResults();
            List<SavedResult> loadedCreditResults = database.resultsDao().getCreditResults();

            getActivity().runOnUiThread(() -> {
                // First, clear the lists but only before repopulating to ensure we are not reassigning
                savedResults.clear();
                creditResults.clear();

                // Add the fetched results to the existing lists
                savedResults.addAll(cashResults);
                creditResults.addAll(loadedCreditResults);

                // Notify the adapter that the data has changed
                adapter.notifyDataSetChanged();
            });
        });
    }


}
