package com.panera.paneranfcsetup;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Intent;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.Ndef;
import android.nfc.tech.NdefFormatable;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;

public class EraseNfcActivity extends Activity {

    private NfcAdapter mNfcAdapter;
    private PendingIntent mNfcPendingIntent;
    private ImageView mEraseTag;
    private TextView mBackButton, mSignOut;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_erase);

        mBackButton = findViewById(R.id.textView1);
        mBackButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        mSignOut = findViewById(R.id.textView2);
        mSignOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                navigateUpTo(new Intent(EraseNfcActivity.this, LoginActivity.class));
            }
        });

        mEraseTag = findViewById(R.id.button_erase_tag);

        mNfcAdapter = NfcAdapter.getDefaultAdapter(this);
        if (mNfcAdapter == null) {
            Toast.makeText(this, "This device doesn't support NFC",
                    Toast.LENGTH_SHORT).show();
            finish();
            return;
        } else if (!mNfcAdapter.isEnabled()) {
            Toast.makeText(this, "NFC is disabled", Toast.LENGTH_SHORT).show();
            //TODO: make alert dialog to ask to open nfc settings or close app
        }

        mEraseTag.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mEraseTag.setImageDrawable(ContextCompat.getDrawable(getApplicationContext(), R.drawable.button_red_start_erasing_enabled));
                enableTagWriteMode();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        mEraseTag.setImageDrawable(ContextCompat.getDrawable(getApplicationContext(), R.drawable.button_erase_nfc));
    }

    @Override
    protected void onPause() {
        super.onPause();
        disableTagWriteMode();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        if (NfcAdapter.ACTION_NDEF_DISCOVERED.equals(intent.getAction())
                || NfcAdapter.ACTION_TAG_DISCOVERED.equals(intent.getAction())) {
            Tag detectedTag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);

            String tagMessage = "";
            NdefRecord mimeRecord = NdefRecord.createMime("", tagMessage.getBytes());
            NdefMessage message = new NdefMessage(new NdefRecord[]{mimeRecord});
            if (writeTag(message, detectedTag)) {
                disableTagWriteMode();
            }
        }
    }

    private void enableTagWriteMode() {
        Intent successIntent = new Intent(EraseNfcActivity.this, NfcEraseSuccessActivity.class);
        mNfcPendingIntent = PendingIntent.getActivity(this, 0, successIntent, 0);
        overridePendingTransition(0, 0);
        mNfcAdapter.enableForegroundDispatch(this, mNfcPendingIntent, null, null);
    }

    private void disableTagWriteMode() {
        mNfcAdapter.disableForegroundDispatch(this);
    }

    /*
     * Writes an NdefMessage to a NFC tag
     */
    public boolean writeTag(NdefMessage message, Tag tag) {
        try {
            Ndef ndef = Ndef.get(tag);
            if (ndef != null) {
                ndef.connect();
                if (!ndef.isWritable()) {
                    Toast.makeText(getApplicationContext(),
                            "Error: tag not writable", Toast.LENGTH_SHORT)
                            .show();
                    return false;
                }
                ndef.writeNdefMessage(message);

                return true;
            } else {
                NdefFormatable format = NdefFormatable.get(tag);
                if (format != null) {
                    try {
                        format.connect();
                        format.format(message);
                        return true;
                    } catch (IOException e) {
                        return false;
                    }
                } else {
                    return false;
                }
            }
        } catch (Exception e) {
            return false;
        }
    }
}
