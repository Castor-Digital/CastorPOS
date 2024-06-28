package com.castorpos;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;

public class ServerSidebarFragment extends Fragment implements ServerAdapter.OnServerClickListener {
    private RecyclerView recyclerView;
    private ServerAdapter adapter;
    private List<String> servers;
    private EditText serverNameEditText;
    private EditText customersInputEditText;
    private Button addButton;
    private Button editButton;
    private String selectedServer;

    public ServerSidebarFragment() {
        servers = new ArrayList<>();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_server_sidebar, container, false);

        recyclerView = view.findViewById(R.id.server_recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        adapter = new ServerAdapter(servers, this);
        recyclerView.setAdapter(adapter);

        serverNameEditText = view.findViewById(R.id.server_name_edit_text);
        customersInputEditText = view.findViewById(R.id.customers_input);
        addButton = view.findViewById(R.id.add_button);

        Button c1 = view.findViewById(R.id.c1);
        Button c2 = view.findViewById(R.id.c2);
        Button c3 = view.findViewById(R.id.c3);
        Button c4 = view.findViewById(R.id.c4);

        c1.setOnClickListener(v -> updateNumberOfCustomers(1));
        c2.setOnClickListener(v -> updateNumberOfCustomers(2));
        c3.setOnClickListener(v -> updateNumberOfCustomers(3));
        c4.setOnClickListener(v -> updateNumberOfCustomers(4));

        addButton.setOnClickListener(v -> {
            String serverName = serverNameEditText.getText().toString();
            if (!serverName.isEmpty()) {
                servers.add(serverName);
                adapter.notifyItemInserted(servers.size() - 1);
                serverNameEditText.setText("");
            }
        });

        return view;
    }

    @Override
    public void onServerSelected(String server) {
        selectedServer = server;
        if (getActivity() instanceof MainActivity) {
            ((MainActivity) getActivity()).onServerSelected(server);
        }
    }

    @Override
    public void onServerDeleted(String server) {
        int position = servers.indexOf(server);
        if (position >= 0) {
            servers.remove(position);
            adapter.notifyItemRemoved(position);
            if (server.equals(selectedServer)) {
                selectedServer = null;
            }
        }
    }

    public String getSelectedServer() {
        return selectedServer;
    }

    private void updateNumberOfCustomers(int customers) {
        customersInputEditText.setText(String.valueOf(customers));
        if (getActivity() instanceof MainActivity) {
            ((MainActivity) getActivity()).updateNumberOfCustomers(customers);
        }
    }

    public int getNumberOfCustomers() {
        String customers = customersInputEditText.getText().toString();
        if (customers.isEmpty()) {
            return 0;
        }
        return Integer.parseInt(customers);
    }
}
