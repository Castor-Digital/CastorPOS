package com.castorpos;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.graphics.Color;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.hoho.android.usbserial.driver.UsbSerialPort;
import com.hoho.android.usbserial.driver.UsbSerialProber;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.room.Room;

import java.text.DecimalFormat;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity implements ServerAdapter.OnServerClickListener {
    private ResultsSidebarFragment resultsSidebarFragment;
    private ServerSidebarFragment serverSidebarFragment;
    private List<SavedResult> savedResults = new ArrayList<>();
    private double originalAmount = 0.0;
    private ResultsDao resultsDao;
    private boolean cashMode = false;

    private static final String TAG = "MainActivity";
    private static final String ACTION_USB_PERMISSION = "com.android.example.USB_PERMISSION";
    private UsbManager usbManager;
    private UsbSerialPort serialPort;
    private EditText display;
    private AppDatabase database;
    private TextView operationDisplay;
    private StringBuilder currentInput = new StringBuilder();
    private double operand1 = 0;
    private double operand2 = 0;
    private String currentOperation = "";
    private DecimalFormat df = new DecimalFormat("$0.00");
    private double currentOperand;
    private DecimalFormat currencyFormat;
    private int numberOfCustomers;
    private String selectedServer;
    private Button doubleZeroButton;

    private final BroadcastReceiver usbReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (ACTION_USB_PERMISSION.equals(action)) {
                synchronized (this) {
                    UsbDevice device = intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
                    if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
                        if (device != null) {
                            sendSerialSignal();
                        }
                    } else {
                        Log.d(TAG, "permission denied for device " + device);
                    }
                }
            }
        }
    };

    private StringBuilder operationStringBuilder = new StringBuilder();
    private double currentResult = 0;
    private boolean newOperation = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getSupportActionBar().hide();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        usbManager = (UsbManager) getSystemService(Context.USB_SERVICE);

        display = findViewById(R.id.display);
        operationDisplay = findViewById(R.id.operation_display);
        Button buttonDiscount = findViewById(R.id.buttonDiscount);

        numberOfCustomers = 1;
        selectedServer = "";
        // Initialize currentOperand to 0.00
        currentOperand = 0.00;
        // Initialize the currency format
        currencyFormat = new DecimalFormat("$0.00");
        display.setText("$0.00");

        buttonDiscount.setOnClickListener(v -> applyDiscount());
        Button doubleZeroButton = findViewById(R.id.double_zero_button);
        doubleZeroButton.setOnClickListener(v -> {
            addDoubleZero();
        });

        // Initialize fragments
        resultsSidebarFragment = new ResultsSidebarFragment();
        serverSidebarFragment = new ServerSidebarFragment();

        // Add the ServerSidebarFragment and ResultsSidebarFragment to the layout
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.server_sidebar_container, serverSidebarFragment);
        fragmentTransaction.replace(R.id.results_sidebar_container, resultsSidebarFragment);
        fragmentTransaction.commit();

        PendingIntent permissionIntent = PendingIntent.getBroadcast(this, 0, new Intent(ACTION_USB_PERMISSION), PendingIntent.FLAG_IMMUTABLE);
        IntentFilter filter = new IntentFilter(ACTION_USB_PERMISSION);
        registerReceiver(usbReceiver, filter);

        // Initialize the database
        database = Room.databaseBuilder(getApplicationContext(), AppDatabase.class, "app_database")
                .fallbackToDestructiveMigration()
                .build();

        // Number Buttons
        int[] numberButtonIds = {
                R.id.button0, R.id.button1, R.id.button2, R.id.button3,
                R.id.button4, R.id.button5, R.id.button6, R.id.button7,
                R.id.button8, R.id.button9
        };

        View.OnClickListener numberClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Button b = (Button) v;
                currentInput.append(b.getText().toString());
                updateDisplay();
            }
        };

        for (int id : numberButtonIds) {
            findViewById(id).setOnClickListener(numberClickListener);
        }

        // Operation Buttons
        findViewById(R.id.buttonAdd).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setOperation("+");
            }
        });
        findViewById(R.id.buttonSubtract).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setOperation("-");
            }
        });
        findViewById(R.id.buttonMultiply).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setOperation("*");
            }
        });
        findViewById(R.id.buttonDivide).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setOperation("/");
            }
        });

        // Clear Button
        findViewById(R.id.buttonClear).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clear();
            }
        });

        // Equals Button
        findViewById(R.id.buttonEquals).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                calculateResult();
            }
        });

        // Save Result Button
        findViewById(R.id.buttonSave).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String resultText = display.getText().toString(); // Get the result text from your display or relevant source
                saveResult(resultText);
                sendSerialSignal(); //Open register on save (remove this line for testing on PC)
            }
        });

        // Open Register Button
        findViewById(R.id.buttonOpenRegister).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendSerialSignal();
            }
        });

        // Backspace Button
        findViewById(R.id.buttonBackspace).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (currentInput.length() > 0) {
                    currentInput.deleteCharAt(currentInput.length() - 1);
                    updateDisplay();
                }
            }
        });

        findViewById(R.id.buttonCash).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handleCashButton();
            }
        });

    }

    private void handleCashButton() {
        EditText display = findViewById(R.id.display);
        String displayText = display.getText().toString().replace("$", "");
        if (!displayText.isEmpty()) {
            originalAmount = Double.parseDouble(displayText);
            display.setText("$0.00"); // Show 0.00 for cash input
            cashMode = true;
        }
    }

    private void saveResult(String resultText) {
        if (validateConditions()) {
            String serverName = serverSidebarFragment.getSelectedServer();
            int customers = serverSidebarFragment.getNumberOfCustomers();  // Ensure this method exists in ServerSidebarFragment
            SavedResult savedResult = new SavedResult(resultText, serverName, customers);

            // Add result to the ResultsSidebarFragment
            resultsSidebarFragment.addResult(savedResult);

            // Insert result into the database on a background thread
            //database.resultsDao().insert(savedResult);
            AsyncTask<SavedResult, Void, Void> execute = new InsertResultTask().execute(savedResult);


            Toast.makeText(this, "Result saved: " + resultText, Toast.LENGTH_SHORT).show();
            Log.d(TAG, "Inserted new result: " + savedResult.getResultText());

            // Reset the current operand to 0.00
            operand1 = 0.00;
            operand2 = 0.00;
            currentInput.setLength(0); // Clear the current input
            display.setText(df.format(0.00)); // Update the display to show 0.00
            display.setTextColor(Color.parseColor("#222222"));
            }
    }

    @Override
    public void onServerSelected(String server) {
        selectedServer = server;
    }

    @Override
    public void onServerDeleted(String server) {

    }

    public void updateNumberOfCustomers(int customers) {
        numberOfCustomers = customers;
    }

    private boolean validateConditions() {
        String resultText = display.getText().toString().replace("$", "");
        double resultAmount = resultText.isEmpty() ? 0.00 : Double.parseDouble(resultText);

        if (selectedServer == null || selectedServer.isEmpty()) {
            Toast.makeText(this, "Please select a server.", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (numberOfCustomers <= 0) {
            Toast.makeText(this, "Please enter the number of customers.", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (resultAmount <= 0.00) {
            Toast.makeText(this, "Please enter a total.", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }



    private class InsertResultTask extends AsyncTask<SavedResult, Void, Void> {
        @Override
        protected Void doInBackground(SavedResult... results) {
            database.resultsDao().insert(results[0]);
            return null;
        }
    }

    private void updateDisplay() {
        String displayValue = "$0.00";
        if (currentInput.length() > 0) {
            double input = Double.parseDouble(currentInput.toString()) / 100;
            displayValue = df.format(input);
        }
        display.setText(displayValue);
        display.setTextColor(Color.parseColor("#222222"));
    }

    private void setOperation(String operation) {
        if (currentInput.length() > 0) {
            operand1 = Double.parseDouble(currentInput.toString()) / 100;
            currentOperation = operation;
            operationDisplay.setText(df.format(operand1) + " " + operation);
            currentInput.setLength(0);
        }
    }

    private void calculateResult() {
        if (currentInput.length() > 0 && !currentOperation.isEmpty()) {
            operand2 = Double.parseDouble(currentInput.toString()) / 100;
            double result = 0;

            switch (currentOperation) {
                case "+":
                    result = operand1 + operand2;
                    break;
                case "-":
                    result = operand1 - operand2;
                    break;
                case "*":
                    result = operand1 * operand2;
                    break;
                case "/":
                    if (operand2 != 0) {
                        result = operand1 / operand2;
                    } else {
                        display.setText("Error");
                        return;
                    }
                    break;
            }

            display.setText(df.format(result));
            operationDisplay.setText("");
            currentInput.setLength(0);
            operand1 = result;
            operand2 = 0;
            currentOperation = "";
        }
    }

    private void clear() {
        currentInput.setLength(0);
        operand1 = 0;
        operand2 = 0;
        currentOperation = "";
        display.setText("$0.00");
        operationDisplay.setText("");
        display.setTextColor(Color.parseColor("#222222"));
    }

    private void showSelectedServer(String serverName) {
        Toast.makeText(this, "Selected Server: " + serverName, Toast.LENGTH_SHORT).show();
    }

    //Discount 10 percent -> rounds to the nearest 5 cent value
    private void applyDiscount() {
        String displayText = display.getText().toString().replace("$", "");
        if (!displayText.isEmpty()) {
            try {
                currentOperand = Double.parseDouble(displayText);
                currentOperand = currentOperand * 0.9;
                currentOperand = Math.round(currentOperand * 20.0) / 20.0;
                display.setText(currencyFormat.format(currentOperand));
                display.setTextColor(Color.parseColor("#006400"));
            } catch (NumberFormatException e) {
                display.setText(currencyFormat.format(0.00));
            }
        }
    }

    private void addDoubleZero() {
        String currentAmount = display.getText().toString().replace("$", "").replace(",", "");
        try {
            double amount = Double.parseDouble(currentAmount);
            amount *= 100; // Shift the decimal two places to the right
            display.setText(String.format("$%,.2f", amount));
        } catch (NumberFormatException e) {
            display.setText("$0.00");
        }
    }

    private void sendSerialSignal() {
        List<UsbSerialPort> availablePorts = UsbSerialProber.getDefaultProber().findAllDrivers(usbManager).get(0).getPorts();
        if (availablePorts.isEmpty()) {
            Log.e(TAG, "No serial ports available.");
            return;
        }

        serialPort = availablePorts.get(0);
        try {
            UsbDevice device = serialPort.getDriver().getDevice();
            if (usbManager.hasPermission(device)) {
                serialPort.open(usbManager.openDevice(device));
                serialPort.setParameters(9600, 8, UsbSerialPort.STOPBITS_1, UsbSerialPort.PARITY_NONE);

                byte[] signal = {0x1B, 0x70, 0x00, 0x19, (byte) 0xFA}; // Your serial command
                serialPort.write(signal, 1000);
                Log.d(TAG, "Serial signal sent successfully");

                serialPort.close();
            } else {
                Log.e(TAG, "No permission to access USB device.");
            }
        } catch (IOException e) {
            Log.e(TAG, "Error sending serial signal.", e);
        }
    }
}