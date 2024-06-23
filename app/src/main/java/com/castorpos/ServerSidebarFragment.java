package com.castorpos;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
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
    private int selectedServerPosition = -1; // No server selected by default
    private EditText customersInput;
    private ServerSelectionListener listener;
    private TextView serverTextView;
    private String selectedServer;

    public void updateDisplay() {
        if (serverTextView != null && selectedServer != null) {
            serverTextView.setText("Selected Server: " + selectedServer);
        }
    }

    public interface ServerSelectionListener {
        void onServerSelected(String server);
    }

    public void setServerSelectionListener(ServerSelectionListener listener) {
        this.listener = listener;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_server_sidebar, container, false);

        serverNameInput = view.findViewById(R.id.server_name_input);
        addServerButton = view.findViewById(R.id.add_server_button);
        serverListView = view.findViewById(R.id.server_list_view);
        customersInput = view.findViewById(R.id.customers_input);

        Button c1 = view.findViewById(R.id.c1);
        Button c2 = view.findViewById(R.id.c2);
        Button c3 = view.findViewById(R.id.c3);
        Button c4 = view.findViewById(R.id.c4);

        c1.setOnClickListener(v -> customersInput.setText("1"));
        c2.setOnClickListener(v -> customersInput.setText("2"));
        c3.setOnClickListener(v -> customersInput.setText("3"));
        c4.setOnClickListener(v -> customersInput.setText("4"));

        // Initialize the server list and adapter
        serverList = new ArrayList<>();
        serverList.add("To-Go");
        serverAdapter = new ServerAdapter(requireContext(), serverList);
        serverListView.setAdapter(serverAdapter);

        addServerButton.setOnClickListener(v -> addServer());

        serverNameInput.setOnEditorActionListener((TextView v, int actionId, KeyEvent event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE ||
                    actionId == EditorInfo.IME_ACTION_NEXT ||
                    (event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER && event.getAction() == KeyEvent.ACTION_DOWN)) {

                addServer();
                return true;
            }
            return false;
        });

        serverListView.setOnItemClickListener((parent, view1, position, id) -> {
            selectedServerPosition = position;
            serverAdapter.setSelectedPosition(position);
            Log.d("ServerSidebarFragment", "Server selected: " + serverList.get(position));
        });

        return view;
    }

    private void addServer() {
        String serverName = serverNameInput.getText().toString().trim();
        if (!serverName.isEmpty()) {
            if (!serverList.contains(serverName)) {
                serverList.add(serverName);
                serverAdapter.notifyDataSetChanged();
                serverNameInput.setText("");

                // Hide the keyboard
                InputMethodManager imm = (InputMethodManager) requireContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                if (imm != null) {
                    imm.hideSoftInputFromWindow(serverNameInput.getWindowToken(), 0);
                }

            } else {
                Toast.makeText(getContext(), "Server already exists", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(getContext(), "Server name cannot be empty", Toast.LENGTH_SHORT).show();
        }
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

    public int getNumberOfCustomers() {
        String input = customersInput.getText().toString().trim();
        if (input.isEmpty()) {
            return -1; // or any default value indicating no input
        } else {
            try {
                return Integer.parseInt(input);
            } catch (NumberFormatException e) {
                Toast.makeText(getContext(), "Invalid number of customers", Toast.LENGTH_SHORT).show();
                return -1;
            }
        }
    }

    public String getSelectedServer() {
        if (selectedServerPosition >= 0 && selectedServerPosition < serverList.size()) {
            return serverList.get(selectedServerPosition);
        }
        return "";
    }

    public int getSelectedServerPosition() {
        return selectedServerPosition;
    }
}
