package com.castorpos;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;


public class ResultsAdapter extends RecyclerView.Adapter<ResultsAdapter.ViewHolder> {
    private List<SavedResult> results;
    private OnItemClickListener onItemClickListener;

    public interface OnItemClickListener {
        void onItemDeleteClick(SavedResult result);
    }

    public ResultsAdapter(List<SavedResult> results, OnItemClickListener onItemClickListener) {
        this.results = results;
        this.onItemClickListener = onItemClickListener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.saved_result_list_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        String time = getCurrentTimeFormatted();
        holder.savedTime.setText(time);

        SavedResult result = results.get(position);
        holder.resultTextView.setText(result.getResultText());
        holder.serverNameTextView.setText(result.getServerName());
        holder.customersTextView.setText(String.valueOf(result.getCustomers()));

        holder.deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onItemClickListener.onItemDeleteClick(result);
            }
        });
    }

    private String getCurrentTimeFormatted() {
        SimpleDateFormat sdf = new SimpleDateFormat("hh:mm a", Locale.getDefault());
        return sdf.format(new Date());
    }

    @Override
    public int getItemCount() {
        return results.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView savedTime, resultTextView, serverNameTextView, customersTextView;
        public Button deleteButton;

        public ViewHolder(View view) {
            super(view);
            savedTime = itemView.findViewById(R.id.saved_time);
            resultTextView = view.findViewById(R.id.saved_result);
            serverNameTextView = view.findViewById(R.id.saved_server);
            customersTextView = view.findViewById(R.id.saved_customers);
            deleteButton = view.findViewById(R.id.delete_result_button);
        }
    }

    public void addResult(SavedResult result) {
        results.add(0, result); // Add new result at the top
        notifyItemInserted(0);
    }
}
