package com.castorpos;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.hoho.android.usbserial.driver.UsbSerialPort;
import com.hoho.android.usbserial.driver.UsbSerialProber;
import java.io.IOException;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private static final String ACTION_USB_PERMISSION = "com.android.example.USB_PERMISSION";
    private UsbManager usbManager;
    private UsbSerialPort serialPort;
    private EditText display;
    private TextView operationDisplay;
    private String currentOperator;
    private double firstOperand;
    private boolean isOperatorPressed;
    private StringBuilder inputBuilder;

    private EditText serverNameInput;
    private ListView serverListView;
    private ServerListAdapter serverAdapter;
    private List<String> serverList;

    private String selectedServer;
    private int numberOfCustomers;
    private ListView resultsListView;
    private SavedResultListAdapter resultsAdapter;
    private List<String> resultsList;

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
                        Log.d(TAG, "Permission denied for device " + device);
                    }
                }
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        usbManager = (UsbManager) getSystemService(USB_SERVICE);
        PendingIntent permissionIntent = PendingIntent.getBroadcast(this, 0, new Intent(ACTION_USB_PERMISSION), PendingIntent.FLAG_IMMUTABLE);
        IntentFilter filter = new IntentFilter(ACTION_USB_PERMISSION);
        registerReceiver(usbReceiver, filter);

        display = findViewById(R.id.display);
        operationDisplay = findViewById(R.id.operation_display);
        Button buttonOpenRegister = findViewById(R.id.buttonOpenRegister);

        inputBuilder = new StringBuilder();
        initializeCalculatorButtons();

        buttonOpenRegister.setOnClickListener(new View.OnClickListener() {
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

        Button buttonBackspace = findViewById(R.id.buttonBackspace);
        buttonBackspace.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (inputBuilder.length() > 0) {
                    inputBuilder.deleteCharAt(inputBuilder.length() - 1);
                    updateDisplay();
                }
            }
        });

        serverNameInput = findViewById(R.id.server_name_input);
        Button addServerButton = findViewById(R.id.add_server_button);
        serverListView = findViewById(R.id.server_list);
        serverList = new ArrayList<>();
        serverAdapter = new ServerListAdapter(this, serverList);
        serverListView.setAdapter(serverAdapter);

        addServerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String serverName = serverNameInput.getText().toString().trim();
                if (!serverName.isEmpty()) {
                    serverList.add(serverName);
                    serverAdapter.notifyDataSetChanged();
                    serverNameInput.setText("");
                }
            }
        });

        initializeCustomerButtons();

        resultsListView = findViewById(R.id.results_list);
        resultsList = new ArrayList<>();
        resultsAdapter = new SavedResultListAdapter(this, resultsList);
        resultsListView.setAdapter(resultsAdapter);

        Button buttonSave = findViewById(R.id.buttonSave);
        buttonSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (selectedServer == null) {
                    Toast.makeText(MainActivity.this, "Please select a server before saving.", Toast.LENGTH_SHORT).show();
                } else {
                    saveResult();
                }
            }
        });

        // Set default number of customers to 1
        findViewById(R.id.button1).performClick();
    }

    private void initializeCalculatorButtons() {
        int[] numberButtonIds = {
                R.id.button0, R.id.button1, R.id.button2,
                R.id.button3, R.id.button4, R.id.button5,
                R.id.button6, R.id.button7, R.id.button8, R.id.button9
        };

        for (int id : numberButtonIds) {
            findViewById(id).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Button button = (Button) v;
                    inputBuilder.append(button.getText());
                    updateDisplay();
                }
            });
        }

        findViewById(R.id.buttonAdd).setOnClickListener(createOperatorClickListener("+"));
        findViewById(R.id.buttonSubtract).setOnClickListener(createOperatorClickListener("-"));
        findViewById(R.id.buttonMultiply).setOnClickListener(createOperatorClickListener("*"));
        findViewById(R.id.buttonDivide).setOnClickListener(createOperatorClickListener("/"));

        findViewById(R.id.buttonEquals).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                double secondOperand = Double.parseDouble(inputBuilder.toString()) / 100.0;
                double result = 0.0;
                switch (currentOperator) {
                    case "+":
                        result = firstOperand + secondOperand;
                        break;
                    case "-":
                        result = firstOperand - secondOperand;
                        break;
                    case "*":
                        result = firstOperand * secondOperand;
                        break;
                    case "/":
                        if (secondOperand != 0) {
                            result = firstOperand / secondOperand;
                        } else {
                            display.setText("Error");
                            return;
                        }
                        break;
                }
                inputBuilder.setLength(0);
                inputBuilder.append(String.format(Locale.US, "%.2f", result * 100));
                updateDisplay();
                operationDisplay.setText("");
                isOperatorPressed = false;
            }
        });

        findViewById(R.id.buttonClear).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                inputBuilder.setLength(0);
                updateDisplay();
                operationDisplay.setText("");
                firstOperand = 0.0;
                currentOperator = "";
                isOperatorPressed = false;
            }
        });
    }

    private View.OnClickListener createOperatorClickListener(final String operator) {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                firstOperand = Double.parseDouble(inputBuilder.toString()) / 100.0;
                currentOperator = operator;
                isOperatorPressed = true;
                inputBuilder.setLength(0);
                updateDisplay();
                operationDisplay.setText(String.format(Locale.US, "$%.2f %s", firstOperand, currentOperator));
            }
        };
    }

    private void updateDisplay() {
        try {
            double value = Double.parseDouble(inputBuilder.toString()) / 100.0;
            String formattedValue = NumberFormat.getCurrencyInstance(Locale.US).format(value);
            display.setText(formattedValue);
        } catch (NumberFormatException e) {
            display.setText("$0.00");
        }
    }

    private void initializeCustomerButtons() {
        int[] customerButtonIds = {
                R.id.button1, R.id.button2, R.id.button3, R.id.button4
        };

        View.OnClickListener customerButtonClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                for (int id : customerButtonIds) {
                    Button button = findViewById(id);
                    button.setBackgroundColor(getResources().getColor(id == v.getId() ? R.color.selectedCustomerButton : R.color.defaultCustomerButton));
                }
                Button button = (Button) v;
                numberOfCustomers = Integer.parseInt(button.getText().toString());
            }
        };

        for (int id : customerButtonIds) {
            findViewById(id).setOnClickListener(customerButtonClickListener);
        }

        EditText customNumberInput = findViewById(R.id.custom_number_input);
        Button saveCustomNumberButton = findViewById(R.id.save_custom_number_button);
        saveCustomNumberButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String customNumberText = customNumberInput.getText().toString().trim();
                if (!customNumberText.isEmpty()) {
                    numberOfCustomers = Integer.parseInt(customNumberText);
                    customNumberInput.setText("");
                    Toast.makeText(MainActivity.this, "Custom number saved: " + numberOfCustomers, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void saveResult() {
        String currentOperand = display.getText().toString();
        String result = "Server: " + selectedServer + ", Customers: " + numberOfCustomers + ", Amount: " + currentOperand;
        resultsList.add(result);
        resultsAdapter.notifyDataSetChanged();
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

    private class ServerListAdapter extends BaseAdapter {
        private Context context;
        private List<String> serverList;

        public ServerListAdapter(Context context, List<String> serverList) {
            this.context = context;
            this.serverList = serverList;
        }

        @Override
        public int getCount() {
            return serverList.size();
        }

        @Override
        public Object getItem(int position) {
            return serverList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = LayoutInflater.from(context).inflate(R.layout.server_list_item, parent, false);
            }

            RadioButton serverRadioButton = convertView.findViewById(R.id.server_radio_button);
            TextView serverName = convertView.findViewById(R.id.server_name);
            Button deleteServerButton = convertView.findViewById(R.id.delete_server_button);

            serverName.setText(serverList.get(position));

            serverRadioButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    selectedServer = serverList.get(position);
                    notifyDataSetChanged();
                }
            });

            deleteServerButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    serverList.remove(position);
                    notifyDataSetChanged();
                }
            });

            serverRadioButton.setChecked(serverList.get(position).equals(selectedServer));

            return convertView;
        }
    }

    private class SavedResultListAdapter extends BaseAdapter {
        private Context context;
        private List<String> resultsList;

        public SavedResultListAdapter(Context context, List<String> resultsList) {
            this.context = context;
            this.resultsList = resultsList;
        }

        @Override
        public int getCount() {
            return resultsList.size();
        }

        @Override
        public Object getItem(int position) {
            return resultsList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = LayoutInflater.from(context).inflate(R.layout.saved_result_list_item, parent, false);
            }

            TextView savedResult = convertView.findViewById(R.id.saved_result);
            Button deleteResultButton = convertView.findViewById(R.id.delete_result_button);

            savedResult.setText(resultsList.get(position));

            deleteResultButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    resultsList.remove(position);
                    notifyDataSetChanged();
                }
            });

            return convertView;
        }
    }
}

