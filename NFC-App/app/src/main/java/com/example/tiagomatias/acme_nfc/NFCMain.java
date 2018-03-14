package com.example.tiagomatias.acme_nfc;

import android.content.Intent;
import android.nfc.NdefMessage;
import android.nfc.NfcAdapter;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.os.Parcelable;
import android.widget.Button;
import android.view.View;
import java.util.ArrayList;

public class NFCMain extends AppCompatActivity {
    ArrayList<String> order;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nfc_main);

        final Button button = findViewById(R.id.readTag);

        button.setOnClickListener(new View.OnClickListener() { public void onClick(View v) {
                showOrder(v);
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        if (NfcAdapter.ACTION_NDEF_DISCOVERED.equals(getIntent().getAction())) {
            processIntent(getIntent());
        }
    }

    @Override
    public void onNewIntent(Intent intent) {
        setIntent(intent);
    }

    void processIntent(Intent intent) {
        Parcelable[] rawMsgs = intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES);
        NdefMessage msg = (NdefMessage) rawMsgs[0];
        int err = this.processMessage(new String(msg.getRecords()[0].getPayload()));
    }

    private int processMessage(String message) {
        return 0;
    }

    private void showOrder(View view) {
        Intent intent = new Intent(this, DisplayOrderActivity.class);
        intent.putExtra("order", order);
        startActivity(intent);
    }
}
