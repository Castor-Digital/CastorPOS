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

    public ResultsAdapter(Context context, List<SavedResult> savedResults, List<SavedResult> creditResults) {
        this.context = context;
        this.savedResults = savedResults;
        this.creditResults = creditResults;
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
            if (position < savedResults.size()) {
                savedResults.remove(position);
            } else {
                creditResults.remove(position - savedResults.size());
            }
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

    public void removeResult(SavedResult result) {
        // Check if the result is in the savedResults list and remove it
        if (savedResults.contains(result)) {
            savedResults.remove(result);
        }
        // Check if the result is in the creditResults list and remove it
        else if (creditResults.contains(result)) {
            creditResults.remove(result);
        }
        // Notify the adapter that the data set has changed
        notifyDataSetChanged();
    }

    public void setResults(List<SavedResult> results) {
        this.savedResults.clear();
        this.savedResults.addAll(results);
        notifyDataSetChanged();
    }
}