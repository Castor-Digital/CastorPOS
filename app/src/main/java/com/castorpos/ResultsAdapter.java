
package com.castorpos;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;
import java.util.concurrent.ExecutorService;

public class ResultsAdapter extends RecyclerView.Adapter<ResultsAdapter.ViewHolder> {

    private Context context;
    private List<SavedResult> savedResults;
    private List<SavedResult> creditResults;
    private AppDatabase database;  // Add database reference for deletion
    private ExecutorService executorService;  // For background thread execution

    public ResultsAdapter(Context context, List<SavedResult> savedResults, List<SavedResult> creditResults, AppDatabase database, ExecutorService executorService) {
        this.context = context;
        this.savedResults = savedResults;
        this.creditResults = creditResults;
        this.database = database;  // Initialize database
        this.executorService = executorService;  // Initialize ExecutorService
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.saved_result_list_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        SavedResult result;
        if (position < savedResults.size()) {
            result = savedResults.get(position);
        } else {
            result = creditResults.get(position - savedResults.size());
        }

        // Bind data to view elements
        holder.savedTime.setText(result.getTime());
        holder.savedResult.setText(result.getResultText());
        holder.savedServer.setText(result.getServerName());
        holder.savedCustomers.setText(String.valueOf(result.getCustomers()));

        // Set up the delete button functionality
        holder.deleteResultButton.setOnClickListener(v -> {
            // Remove the result from the RecyclerView list
            if (position < savedResults.size()) {
                savedResults.remove(position);
            } else {
                creditResults.remove(position - savedResults.size());
            }

            // Notify the adapter of the removal
            notifyItemRemoved(position);

            // Remove the result from the database in the background
            executorService.execute(() -> {
                database.resultsDao().delete(result);  // Delete the result from the database
            });
        });
    }

    @Override
    public int getItemCount() {
        return savedResults.size() + creditResults.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView savedTime;
        TextView savedResult;
        TextView savedServer;
        TextView savedCustomers;
        Button deleteResultButton;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            savedTime = itemView.findViewById(R.id.saved_time);
            savedResult = itemView.findViewById(R.id.saved_result);
            savedServer = itemView.findViewById(R.id.saved_server);
            savedCustomers = itemView.findViewById(R.id.saved_customers);
            deleteResultButton = itemView.findViewById(R.id.delete_result_button);
        }
    }
}
