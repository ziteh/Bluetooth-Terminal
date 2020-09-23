package me.aflak.bluetoothterminal;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.text.method.ScrollingMovementMethod;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import me.aflak.bluetooth.Bluetooth;

public class Chat extends AppCompatActivity implements Bluetooth.CommunicationCallback {
    private String name;
    private Bluetooth b;
    //    private EditText message;
//    private Button send;
//    private TextView text;
//    private ScrollView scrollView;
    private boolean registered = false;

    private TextView rReady;
    private TextView rLimit;
    private TextView rMotion;
    private TextView rAnglePOT;
    private TextView rFrontFSR;
    private TextView rBackFSR;

    private TextView lReady;
    private TextView lLimit;
    private TextView lMotion;
    private TextView lAnglePOT;
    private TextView lFrontFSR;
    private TextView lBackFSR;

    private Button reset;
    private Button stop;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_main);
        setContentView(R.layout.control_panel);

        // Right
        rReady = (TextView) findViewById(R.id.textViewRReady);
        rLimit = (TextView) findViewById(R.id.textViewRLimit);
        rMotion = (TextView) findViewById(R.id.textViewRMotion);
        rAnglePOT = (TextView) findViewById(R.id.textViewRAnglePOT);
        rFrontFSR = (TextView) findViewById(R.id.textViewRFrontFSR);
        rBackFSR = (TextView) findViewById(R.id.textViewRBackFSR);

        // Left
        lReady = (TextView) findViewById(R.id.textViewLReady);
        lLimit = (TextView) findViewById(R.id.textViewLLimit);
        lMotion = (TextView) findViewById(R.id.textViewLMotion);
        lAnglePOT = (TextView) findViewById(R.id.textViewLAnglePOT);
        lFrontFSR = (TextView) findViewById(R.id.textViewLFrontFSR);
        lBackFSR = (TextView) findViewById(R.id.textViewLBackFSR);

        reset = (Button) findViewById(R.id.buttonReset);
        stop = (Button) findViewById(R.id.buttonStop);

        lReady.setText("wait...");

//        text = (TextView) findViewById(R.id.text);
//        message = (EditText) findViewById(R.id.message);
//        send = (Button) findViewById(R.id.send);
//        scrollView = (ScrollView) findViewById(R.id.scrollView);
//
//        text.setMovementMethod(new ScrollingMovementMethod());
//        send.setEnabled(false);

        b = new Bluetooth(this);
        b.enableBluetooth();

        b.setCommunicationCallback(this);

        int pos = getIntent().getExtras().getInt("pos");
        name = b.getPairedDevices().get(pos).getName();

        Toast.makeText(getApplicationContext(), "Connecting...", Toast.LENGTH_SHORT).show();
        b.connectToDevice(b.getPairedDevices().get(pos));

        reset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                b.send("R");
            }
        });

        stop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                b.send("S");
            }
        });

//        send.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                String msg = message.getText().toString();
//                message.setText("");
//                b.send(msg);
//                Display("You: " + msg);
//            }
//        });

        IntentFilter filter = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
        registerReceiver(mReceiver, filter);
        registered = true;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (registered) {
            unregisterReceiver(mReceiver);
            registered = false;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.close:
                b.removeCommunicationCallback();
                b.disconnect();
                Intent intent = new Intent(this, Select.class);
                startActivity(intent);
                finish();
                return true;

//            case R.id.switchPages:
//                intent = new Intent(this, ControlPanel.class);
//                startActivity(intent);
//                finish();
//                return true;

            case R.id.rate:
                Uri uri = Uri.parse("market://details?id=" + this.getPackageName());
                Intent goToMarket = new Intent(Intent.ACTION_VIEW, uri);
                goToMarket.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY | Intent.FLAG_ACTIVITY_NEW_DOCUMENT | Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
                try {
                    startActivity(goToMarket);
                } catch (ActivityNotFoundException e) {
                    startActivity(new Intent(Intent.ACTION_VIEW,
                            Uri.parse("http://play.google.com/store/apps/details?id=" + this.getPackageName())));
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void Display(final String s) {
        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
//                lReady.setText(s);
                String[] data = s.split(",");
                if (data.length == 15) {
                    switch (data[1]) {
                        case "1":
                            rReady.setText("Ready");
                            break;

                        case "0":
                        default:
                            rReady.setText("⚠Fault");
                            break;
                    }

                    switch (data[2]) {
                        case "0":
                            rLimit.setText("Unlimited");
                            break;

                        case "1":
                            rLimit.setText("Full Extension");
                            break;

                        case "2":
                            rLimit.setText("Full Flexion");
                            break;

                        default:
                            rLimit.setText("?");
                            break;
                    }

                    switch (data[3]) {
                        case "1":
                            rMotion.setText("Extensioning");
                            break;

                        case "2":
                            rMotion.setText("Flexioning");
                            break;

                        default:
                            rMotion.setText("Not in motion");
                            break;
                    }
                    rAnglePOT.setText(data[4]);
                    rFrontFSR.setText(data[5]);
                    rBackFSR.setText(data[6]);

                    switch (data[8]) {
                        case "1":
                            lReady.setText("Ready");
                            break;

                        case "0":
                        default:
                            lReady.setText("⚠Fault");
                            break;
                    }

                    switch (data[9]) {
                        case "0":
                            lLimit.setText("Unlimited");
                            break;

                        case "1":
                            lLimit.setText("Full Extension");
                            break;

                        case "2":
                            lLimit.setText("Full Flexion");
                            break;

                        default:
                            lLimit.setText("?");
                            break;
                    }

                    switch (data[10]) {
                        case "1":
                            lMotion.setText("Extensioning");
                            break;

                        case "2":
                            lMotion.setText("Flexioning");
                            break;

                        default:
                            lMotion.setText("Not in motion");
                            break;
                    }
                    lAnglePOT.setText(data[11]);
                    lFrontFSR.setText(data[12]);
                    lBackFSR.setText(data[13]);
                } else {
                    Toast.makeText(getApplicationContext(), s, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    public void onConnect(BluetoothDevice device) {
        Display("Connected to " + device.getName() + " - " + device.getAddress());
//        this.runOnUiThread(new Runnable() {
//            @Override
//            public void run() {
//                send.setEnabled(true);
//            }
//        });
    }

    @Override
    public void onDisconnect(BluetoothDevice device, String message) {
        Display("Disconnected!");
        Display("Connecting again...");
        b.connectToDevice(device);
    }

    @Override
    public void onMessage(String message) {
        Display(message);
    }

    @Override
    public void onError(String message) {
        Display("Error: " + message);
    }

    @Override
    public void onConnectError(final BluetoothDevice device, String message) {
        Display("Error: " + message);
        Display("Trying again in 3 sec.");
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        b.connectToDevice(device);
                    }
                }, 2000);
            }
        });
    }

    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();

            if (action.equals(BluetoothAdapter.ACTION_STATE_CHANGED)) {
                final int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR);
                Intent intent1 = new Intent(Chat.this, Select.class);

                switch (state) {
                    case BluetoothAdapter.STATE_OFF:
                        if (registered) {
                            unregisterReceiver(mReceiver);
                            registered = false;
                        }
                        startActivity(intent1);
                        finish();
                        break;
                    case BluetoothAdapter.STATE_TURNING_OFF:
                        if (registered) {
                            unregisterReceiver(mReceiver);
                            registered = false;
                        }
                        startActivity(intent1);
                        finish();
                        break;
                }
            }
        }
    };
}
