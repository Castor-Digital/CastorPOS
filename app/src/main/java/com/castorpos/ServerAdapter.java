package com.castorpos;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class ServerAdapter extends RecyclerView.Adapter<ServerAdapter.ViewHolder> {
    private List<String> servers;
    private int selectedPosition = -1;
    private OnServerClickListener listener;

    public interface OnServerClickListener {
        void onServerClick(int position);
    }

    public ServerAdapter(List<String> servers, OnServerClickListener listener) {
        this.servers = servers;
        this.listener = listener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.server_list_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        String server = servers.get(position);
        holder.serverNameTextView.setText(server);
        holder.itemView.setSelected(selectedPosition == position);
        holder.itemView.setOnClickListener(v -> {
            selectedPosition = position;
            notifyDataSetChanged();
            listener.onServerClick(position);
        });

        holder.deleteButton.setOnClickListener(v -> {
            servers.remove(position);
            notifyItemRemoved(position);
            notifyItemRangeChanged(position, servers.size());
            if (selectedPosition == position) {
                selectedPosition = -1;
            }
        });
    }

    @Override
    public int getItemCount() {
        return servers.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView serverNameTextView;
        public Button deleteButton;

        public ViewHolder(View view) {
            super(view);
            serverNameTextView = view.findViewById(R.id.server_name);
            deleteButton = view.findViewById(R.id.delete_server_button); // Ensure this ID is correct
        }
    }
}
