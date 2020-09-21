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

import me.aflak.bluetooth.Bluetooth;

public class ControlPanel extends AppCompatActivity implements Bluetooth.CommunicationCallback {
    private String name;
    private Bluetooth b;
    private EditText message;
    private Button send;
    private TextView text;
    private ScrollView scrollView;

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

    private boolean registered = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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

        lReady.setText("00");
        b = new Bluetooth(this);
        b.enableBluetooth();

        b.setCommunicationCallback(this);

        int pos = getIntent().getExtras().getInt("pos");
        name = b.getPairedDevices().get(pos).getName();

        Display("Connecting...");
        b.connectToDevice(b.getPairedDevices().get(pos));

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

            case R.id.switchPages:
                intent = new Intent(this, Chat.class);
                startActivity(intent);
                finish();
                return true;

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
                lReady.setText(s);
//                text.append(s + "\n");
//                scrollView.fullScroll(View.FOCUS_DOWN);
            }
        });
    }

    @Override
    public void onConnect(BluetoothDevice device) {
        Display("Connected to " + device.getName() + " - " + device.getAddress());
        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                send.setEnabled(true);
            }
        });
    }

    @Override
    public void onDisconnect(BluetoothDevice device, String message) {
        Display("Disconnected!");
        Display("Connecting again...");
        b.connectToDevice(device);
    }

    @Override
    public void onMessage(String message) {
        Display(name + ": " + message);
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
                Intent intent1 = new Intent(ControlPanel.this, Select.class);

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
