package com.castorpos;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.TextView;

import java.util.ArrayList;

public class ServerAdapter extends ArrayAdapter<String> {

    private Context mContext;
    private ArrayList<String> serverList;
    private int selectedPosition = -1; // No server selected by default

    public ServerAdapter(Context context, ArrayList<String> list) {
        super(context, 0, list);
        mContext = context;
        serverList = list;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(mContext).inflate(R.layout.server_list_item, parent, false);
        }

        TextView serverNameTextView = convertView.findViewById(R.id.server_name);
        RadioButton radioButton = convertView.findViewById(R.id.server_radio_button);
        Button deleteButton = convertView.findViewById(R.id.delete_server_button);

        String serverName = getItem(position);
        serverNameTextView.setText(serverName);

        radioButton.setChecked(position == selectedPosition);

        View.OnClickListener selectionListener = v -> {
            selectedPosition = position;
            notifyDataSetChanged();
        };

        radioButton.setOnClickListener(selectionListener);
        convertView.setOnClickListener(selectionListener);

        // Hide the delete button for the "To-Go" server
        if ("To-Go".equals(serverName)) {
            deleteButton.setVisibility(View.GONE);
        } else {
            deleteButton.setVisibility(View.VISIBLE);
            deleteButton.setOnClickListener(v -> {
                serverList.remove(position);
                if (position == selectedPosition) {
                    selectedPosition = -1;
                } else if (position < selectedPosition) {
                    selectedPosition--;
                }
                notifyDataSetChanged();
            });
        }

        return convertView;
    }

    public void setSelectedPosition(int position) {
        selectedPosition = position;
        notifyDataSetChanged();
    }
}
