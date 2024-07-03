package com.castorpos;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.room.Room;
import java.util.List;
import java.util.Collections;

public class ResultsSidebarFragment extends Fragment {
    private RecyclerView recyclerView;
    private ResultsAdapter adapter;
    private List<SavedResult> savedResults; // This holds data fetched from the database
    private AppDatabase database;
    private BroadcastReceiver clearDataReceiver;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_results_sidebar, container, false);

        // Initialize the database
        database = Room.databaseBuilder(getContext(), AppDatabase.class, "app_database")
                .fallbackToDestructiveMigration()
                .build();

        recyclerView = view.findViewById(R.id.results_recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        // Load saved results in a background thread
        new LoadResultsTask().execute();

        view.findViewById(R.id.buttonTotalsScreen).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), TotalsScreen.class);
                startActivity(intent);
            }
        });

        // Register the broadcast receiver for clearing data
        clearDataReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                clearResults();
            }
        };
        LocalBroadcastManager.getInstance(getContext()).registerReceiver(clearDataReceiver, new IntentFilter("clear-all-data"));

        return view;
    }

    private void addNewResult(SavedResult result) {
        adapter.addResult(result);
        recyclerView.scrollToPosition(0); // Scroll to the top to show the latest result
    }

    @Override
    public void onDestroyView() {
        LocalBroadcastManager.getInstance(getContext()).unregisterReceiver(clearDataReceiver);
        super.onDestroyView();
    }

    public void addResult(SavedResult result) {
        savedResults.add(result);
        adapter.notifyItemInserted(savedResults.size() - 1);
    }

    private void deleteResult(SavedResult result) {
        new DeleteResultTask().execute(result);
    }

    private void clearResults() {
        savedResults.clear();
        adapter.notifyDataSetChanged();
    }

    private class LoadResultsTask extends AsyncTask<Void, Void, List<SavedResult>> {
        @Override
        protected List<SavedResult> doInBackground(Void... voids) {
            return database.resultsDao().getAllResultsDirect();
        }

        @Override
        protected void onPostExecute(List<SavedResult> results) {
            savedResults = results;
            Collections.reverse(results);
            adapter = new ResultsAdapter(savedResults, new ResultsAdapter.OnItemClickListener() {
                @Override
                public void onItemDeleteClick(SavedResult result) {
                    deleteResult(result);
                }
            });
            recyclerView.setAdapter(adapter);
        }
    }

    private class DeleteResultTask extends AsyncTask<SavedResult, Void, SavedResult> {
        @Override
        protected SavedResult doInBackground(SavedResult... results) {
            database.resultsDao().delete(results[0]);
            return results[0];
        }

        @Override
        protected void onPostExecute(SavedResult result) {
            int position = savedResults.indexOf(result);
            if (position >= 0) {
                savedResults.remove(position);
                adapter.notifyItemRemoved(position);
                // Broadcast that the total revenue needs to be updated
                Intent intent = new Intent("update-total-revenue");
                LocalBroadcastManager.getInstance(getContext()).sendBroadcast(intent);
            }
        }
    }
}
