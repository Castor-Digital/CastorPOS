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

public class ResultsAdapter extends RecyclerView.Adapter<ResultsAdapter.ViewHolder> {

    private Context context;
    private List<SavedResult> savedResults;
    private List<SavedResult> creditResults;
    private ResultsSidebarFragment fragment;

    public ResultsAdapter(Context context, List<SavedResult> savedResults, List<SavedResult> creditResults, ResultsSidebarFragment resultsSidebarFragment) {
        this.context = context;
        this.savedResults = savedResults;
        this.creditResults = creditResults;
        this.fragment = resultsSidebarFragment;
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

        holder.savedTime.setText(result.getTime()); // Ensure SavedResult has a getTime() method
        holder.savedResult.setText(result.getResultText());
        holder.savedServer.setText(result.getServerName());
        holder.savedCustomers.setText(String.valueOf(result.getCustomers()));

        holder.deleteResultButton.setOnClickListener(v -> {
            SavedResult resultToDelete;

            // Determine which list the item is in based on its position
            if (position < savedResults.size()) {
                resultToDelete = savedResults.get(position);
                savedResults.remove(position);
            } else {
                int creditPosition = position - savedResults.size();
                resultToDelete = creditResults.get(creditPosition);
                creditResults.remove(creditPosition);
            }

            // Call deleteResult to handle both UI and database removal
            fragment.deleteResult(resultToDelete);

            // Notify the adapter that the item has been removed
            notifyItemRemoved(position);
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

    public void setResults(List<SavedResult> results) {
        this.savedResults.clear();
        this.savedResults.addAll(results);
        notifyDataSetChanged();
    }
}