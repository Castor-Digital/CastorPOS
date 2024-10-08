package com.castorpos;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;

public class WaitListActivity extends AppCompatActivity {

    private WaitListAdapter adapter;
    private List<WaitListEntry> waitList;
    private EditText partyNameEditText;
    private EditText partySizeEditText;
    private Button addPartyButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wait_list);

        partyNameEditText = findViewById(R.id.partyNameEditText);
        partySizeEditText = findViewById(R.id.partySizeEditText);
        addPartyButton = findViewById(R.id.addPartyButton);

        waitList = new ArrayList<>();
        adapter = new WaitListAdapter(waitList);

        RecyclerView recyclerView = findViewById(R.id.waitListRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        // Add party button listener
        addPartyButton.setOnClickListener(v -> {
            addParty();
        });

        // Enable drag and drop
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(ItemTouchHelper.UP | ItemTouchHelper.DOWN, 0) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                int fromPosition = viewHolder.getAdapterPosition();
                int toPosition = target.getAdapterPosition();
                adapter.moveItem(fromPosition, toPosition);
                return true;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                // No swipe functionality needed
            }
        });
        itemTouchHelper.attachToRecyclerView(recyclerView);
    }

    // Add a party to the waitlist
    private void addParty() {
        String partyName = partyNameEditText.getText().toString().trim();
        String partySizeStr = partySizeEditText.getText().toString().trim();

        if (TextUtils.isEmpty(partyName) || TextUtils.isEmpty(partySizeStr)) {
            Toast.makeText(this, "Please enter both a name and party size", Toast.LENGTH_SHORT).show();
            return;
        }

        int partySize = Integer.parseInt(partySizeStr);
        WaitListEntry newEntry = new WaitListEntry(partyName, partySize);
        adapter.addWaitListEntry(newEntry);

        // Clear input fields
        partyNameEditText.setText("");
        partySizeEditText.setText("");
    }
}
