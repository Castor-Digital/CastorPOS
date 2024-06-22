package com.castorpos;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import java.util.ArrayList;

public class ServerSidebarFragment extends Fragment {

    private EditText serverNameInput;
    private Button addServerButton;
    private ListView serverListView;
    private ServerAdapter serverAdapter;
    private ArrayList<String> serverList;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_server_sidebar, container, false);

        serverNameInput = view.findViewById(R.id.server_name_input);
        addServerButton = view.findViewById(R.id.add_server_button);
        serverListView = view.findViewById(R.id.server_list_view);

        // Initialize the server list with the default "To-Go" server
        serverList = new ArrayList<>();
        serverList.add("To-Go");
        serverAdapter = new ServerAdapter(requireContext(), serverList);
        serverListView.setAdapter(serverAdapter);

        addServerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String serverName = serverNameInput.getText().toString().trim();
                if (!serverName.isEmpty()) {
                    addServer(serverName);
                    serverNameInput.setText("");
                } else {
                    Toast.makeText(requireContext(), "Please enter a server name", Toast.LENGTH_SHORT).show();
                }
            }
        });

        return view;
    }

    // Method to add a server to the list
    public void addServer(String serverName) {
        serverList.add(serverName);
        serverAdapter.notifyDataSetChanged();
    }

    // Method to remove a server from the list
    public void removeServer(int position) {
        if (position >= 0 && position < serverList.size()) {
            serverList.remove(position);
            serverAdapter.notifyDataSetChanged();
        }
    }
}
