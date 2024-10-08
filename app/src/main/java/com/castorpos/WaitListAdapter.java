package com.castorpos;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.Collections;
import java.util.List;

public class WaitListAdapter extends RecyclerView.Adapter<WaitListAdapter.WaitListViewHolder> {

    private List<WaitListEntry> waitList;

    public WaitListAdapter(List<WaitListEntry> waitList) {
        this.waitList = waitList;
    }

    @NonNull
    @Override
    public WaitListViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.wait_list_item, parent, false);
        return new WaitListViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull WaitListViewHolder holder, int position) {
        WaitListEntry entry = waitList.get(position);
        holder.partyNameTextView.setText(entry.getPartyName());
        holder.partySizeTextView.setText(String.valueOf(entry.getPartySize()));
    }

    @Override
    public int getItemCount() {
        return waitList.size();
    }

    public void addWaitListEntry(WaitListEntry entry) {
        waitList.add(entry);
        notifyItemInserted(waitList.size() - 1);
    }

    public void moveItem(int fromPosition, int toPosition) {
        Collections.swap(waitList, fromPosition, toPosition);
        notifyItemMoved(fromPosition, toPosition);
    }

    public void removeItem(int position) {
        waitList.remove(position);
        notifyItemRemoved(position);
    }

    static class WaitListViewHolder extends RecyclerView.ViewHolder {
        TextView partyNameTextView;
        TextView partySizeTextView;

        public WaitListViewHolder(@NonNull View itemView) {
            super(itemView);
            partyNameTextView = itemView.findViewById(R.id.partyNameTextView);
            partySizeTextView = itemView.findViewById(R.id.partySizeTextView);
        }
    }
}
