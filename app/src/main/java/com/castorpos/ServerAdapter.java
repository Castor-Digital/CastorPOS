package com.castorpos;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;

import java.util.ArrayList;

public class ServerAdapter extends ArrayAdapter<String> {

    private Context mContext;
    private ArrayList<String> serverList;

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
        Button deleteButton = convertView.findViewById(R.id.delete_server_button);

        String serverName = getItem(position);
        serverNameTextView.setText(serverName);

        // Hide the delete button for the "To-Go" server
        if ("To-Go".equals(serverName)) {
            deleteButton.setVisibility(View.GONE);
        } else {
            deleteButton.setVisibility(View.VISIBLE);
            deleteButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    serverList.remove(position);
                    notifyDataSetChanged();
                }
            });
        }

        return convertView;
    }
}
