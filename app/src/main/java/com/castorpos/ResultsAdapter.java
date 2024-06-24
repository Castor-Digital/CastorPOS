package com.castorpos;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class ResultsAdapter extends RecyclerView.Adapter<ResultsAdapter.ViewHolder> {
    private List<SavedResult> results;

    public ResultsAdapter(List<SavedResult> results) {
        this.results = results;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.saved_result_list_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        SavedResult result = results.get(position);
        holder.resultTextView.setText(result.getResultText());
        holder.serverNameTextView.setText(result.getServerName());
        holder.customersTextView.setText(String.valueOf(result.getCustomers())); // Display number of customers
    }

    @Override
    public int getItemCount() {
        return results.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView resultTextView;
        public TextView serverNameTextView;
        public TextView customersTextView; // Add this field

        public ViewHolder(View view) {
            super(view);
            resultTextView = view.findViewById(R.id.saved_result);
            serverNameTextView = view.findViewById(R.id.saved_server);
            customersTextView = view.findViewById(R.id.saved_customers); // Add this field
        }
    }
}
