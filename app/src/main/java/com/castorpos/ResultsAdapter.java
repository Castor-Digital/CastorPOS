// ResultsAdapter.java
package com.castorpos;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;

import java.util.List;

public class ResultsAdapter extends ArrayAdapter<SavedResult> {

    public ResultsAdapter(Context context, List<SavedResult> results) {
        super(context, 0, results);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        SavedResult result = getItem(position);

        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.saved_result_list_item, parent, false);
        }

        TextView resultTextView = convertView.findViewById(R.id.saved_result);
        // TextView serverTextView = convertView.findViewById(R.id.saved_server);
        TextView customersTextView = convertView.findViewById(R.id.saved_customers);
        Button deleteButton = convertView.findViewById(R.id.delete_result_button);

        resultTextView.setText(result.getResult() + ", Server: " + result.getServer());
        customersTextView.setText(result.getCustomers() + " customer(s)");

        deleteButton.setOnClickListener(v -> {
            remove(result);
            notifyDataSetChanged();
        });

        return convertView;
    }
}
