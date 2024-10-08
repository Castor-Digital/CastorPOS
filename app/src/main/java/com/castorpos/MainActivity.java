package com.castorpos;

import androidx.core.content.ContextCompat;
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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import android.widget.Toast;

public class MainActivity extends AppCompatActivity implements ServerAdapter.OnServerClickListener {
    private ResultsSidebarFragment resultsSidebarFragment;
    private ServerSidebarFragment serverSidebarFragment;
    private double originalAmount = 0.0;
    private boolean cashMode = false;

    private static final String TAG = "MainActivity";
    private static final String ACTION_USB_PERMISSION = "com.android.castorpos.USB_PERMISSION";
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
    private int numberOfCustomers = 1;;
    private String selectedServer;
    private ExecutorService executorService;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getSupportActionBar().hide();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        usbManager = (UsbManager) getSystemService(Context.USB_SERVICE);

        display = findViewById(R.id.display);
        operationDisplay = findViewById(R.id.operation_display);

        executorService = Executors.newSingleThreadExecutor();

        PendingIntent permissionIntent = PendingIntent.getBroadcast(this, 0, new Intent(ACTION_USB_PERMISSION), PendingIntent.FLAG_IMMUTABLE);
        IntentFilter filter = new IntentFilter(ACTION_USB_PERMISSION);
        ContextCompat.registerReceiver(this, usbReceiver, filter, ContextCompat.RECEIVER_EXPORTED);

        // Open Register / NoSale Button
        findViewById(R.id.buttonOpenRegister).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                List<UsbSerialPort> availablePorts = UsbSerialProber.getDefaultProber().findAllDrivers(usbManager).get(0).getPorts();
                if (!availablePorts.isEmpty()) {
                    UsbDevice device = availablePorts.get(0).getDriver().getDevice();
                    if (!usbManager.hasPermission(device)) {
                        usbManager.requestPermission(device, permissionIntent);
                    } else {
                        sendSerialSignal();
                    }
                } else {
                    Log.e(TAG, "No serial ports available.");
                }
            }
        });

        numberOfCustomers = 1;
        selectedServer = "";
        // Initialize currentOperand to 0.00
        currentOperand = 0.00;
        // Initialize the currency format
        currencyFormat = new DecimalFormat("$0.00");
        display.setText("$0.00");

        // Initialize fragments
        resultsSidebarFragment = new ResultsSidebarFragment();
        serverSidebarFragment = new ServerSidebarFragment();

        // Add the ServerSidebarFragment and ResultsSidebarFragment to the layout
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.server_sidebar_container, serverSidebarFragment);
        fragmentTransaction.replace(R.id.results_sidebar_container, resultsSidebarFragment);
        fragmentTransaction.commit();

        // Initialize the database
        database = Room.databaseBuilder(getApplicationContext(), AppDatabase.class, "app_database")
                .fallbackToDestructiveMigration()
                .build();

        // Number Buttons
        int[] numberButtonIds = {
                R.id.button0, R.id.button1, R.id.button2, R.id.button3,
                R.id.button4, R.id.button5, R.id.button6, R.id.button7,
                R.id.button8, R.id.button9, R.id.buttonDoubleZero
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
        findViewById(R.id.buttonCash).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setOperation("$");
            }
        });
        findViewById(R.id.buttonClear).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clearDisplay();
            }
        });
        findViewById(R.id.buttonEquals).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                calculateResult();
            }
        });
        Button buttonSave = findViewById(R.id.buttonSave);
        buttonSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String resultText = display.getText().toString(); // Get the result text from your display or relevant source
                saveResult(resultText, false);
            }
        });

        Button buttonCredit = findViewById(R.id.buttonCredit);
        buttonCredit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String resultText = display.getText().toString(); // Get the result text from your display or relevant source
                saveResult(resultText, true);
            }
        });

        findViewById(R.id.buttonBackspace).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (currentInput.length() > 0) {
                    currentInput.deleteCharAt(currentInput.length() - 1);
                    updateDisplay();
                }
            }
        });

        findViewById(R.id.buttonDoubleZero).setOnClickListener(v -> addDoubleZero());
        findViewById(R.id.buttonDiscount).setOnClickListener(v -> applyDiscount());

    }

    /* -------------------- Methods -------------------- */

    //sendSerialSignal - for opening cash register
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

    // saveResult - saves the total, server, # of customers
    private void saveResult(String resultText, boolean isCredit) {
        if (validateConditions()) {
            String serverName = serverSidebarFragment.getSelectedServer();
            int customers = serverSidebarFragment.getNumberOfCustomers();

            // Calculate the change only if we are in cash mode
            double change = 0.00;
            if (cashMode) {
                double cashReceived = Double.parseDouble(display.getText().toString().replace("$", ""));
                change = cashReceived - originalAmount;
                resultText = df.format(originalAmount); // Save the original bill total, not the change
            }

            // Get the current time
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
            String currentTime = sdf.format(new Date());

            // Create a new SavedResult object
            SavedResult savedResult = new SavedResult(resultText, serverName, customers, isCredit, currentTime);

            // Insert the result into the database on a background thread
            executorService.execute(() -> {
                database.resultsDao().insert(savedResult);  // Use your ResultsDao insert method

                // Reload the results on the ResultsSidebarFragment after inserting
                runOnUiThread(() -> {
                    resultsSidebarFragment.loadResults();  // Reload results in sidebar
                });
            });

            Toast.makeText(this, "Result saved: " + resultText, Toast.LENGTH_SHORT).show();

            // If applicable, send a signal to open the cash drawer for cash payments
            // if (!isCredit) {
            //    sendSerialSignal();  // Send signal to open drawer for cash results
            // }

            // Reset the current operand and display
            operand1 = 0.00;
            operand2 = 0.00;
            currentInput.setLength(0);
            display.setText(df.format(change));  // Show the change on the display
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

    //validateConditions - prerequisites for saving result, must have server/customers/total
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

    //updateDisplay - sets the calculator display
    private void updateDisplay() {
        if (currentInput.length() == 0) {
            display.setText(df.format(0));
            return;
        }

        // Parse the current input as cents
        long inputAsCents = Long.parseLong(currentInput.toString());
        double valueInDollars = inputAsCents / 100.0;

        // Set the display text
        display.setText(df.format(valueInDollars));
        display.setTextColor(Color.parseColor("#222222"));
    }


    //setOperation - sets the math operation based on input
    private void setOperation(String operation) {
        if (currentInput.length() > 0) {
            if (currentOperation.isEmpty()) {
                operand1 = Long.parseLong(currentInput.toString()) / 100.0;
            } else {
                calculateResult(); // Chain operations
                operand1 = Double.parseDouble(display.getText().toString().replace("$", ""));
            }
            currentOperation = operation;
            operationDisplay.setText(df.format(operand1) + " " + operation);
            currentInput.setLength(0);
        } else {
            Toast.makeText(this, "Enter a number first", Toast.LENGTH_SHORT).show();
        }
    }

    //calculateResult - performs operation
    private void calculateResult() {
        if (currentInput.length() > 0 && !currentOperation.isEmpty()) {
            operand2 = Long.parseLong(currentInput.toString()) / 100.0;
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
                case "$":
                    result = Math.abs(operand1 - operand2);
                    break;
                default:
                    return;
            }
            display.setText(df.format(result));
            operationDisplay.setText(df.format(operand1) + " " + currentOperation + " " + df.format(operand2) + " =");
            currentInput.setLength(0);
            currentOperation = "";
            operand1 = result; // Store result for chaining
        } else {
            Toast.makeText(this, "Complete the operation first", Toast.LENGTH_SHORT).show();
        }
    }

    //clearDisplay - clears calculator display and any stored operation info
    private void clearDisplay() {
        currentInput.setLength(0);
        display.setText(df.format(0));
        operationDisplay.setText("");
        currentOperation = "";
        operand1 = 0;
        operand2 = 0;
    }

    //applyDiscount - discounts the total by 10 percent and rounds to the nearest 5 cent value
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

    //addDoubleZero - appends 00, for dollar amounts or math operations
    private void addDoubleZero() {
        String displayText = display.getText().toString().replace("$", "");
        if (!displayText.isEmpty()) {
            try {
                currentOperand = Double.parseDouble(displayText);
                currentOperand = currentOperand * 100;

                //had to manually append both 0's for now. Works
                currentInput.append(0);
                currentInput.append(0);

                display.setText(currencyFormat.format(currentOperand));
            } catch (NumberFormatException e) {
                display.setText(currencyFormat.format(0.00));
            }
        }
    }
}