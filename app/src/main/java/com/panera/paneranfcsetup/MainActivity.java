package com.panera.paneranfcsetup;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.DialogInterface;
import android.content.Intent;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.Ndef;
import android.nfc.tech.NdefFormatable;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.io.IOException;

public class MainActivity extends Activity {

    private NfcAdapter mNfcAdapter;
    private PendingIntent mNfcPendingIntent;

    private EditText mTextValue;
    private Button mWriteButton, mClearButton, mReadNfc;
    private String mAppMime = "application/com.panera.bread";

    @SuppressWarnings("unused")
    private AlertDialog mDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mTextValue = findViewById(R.id.edit_value);
        mWriteButton = findViewById(R.id.button_write);
        mClearButton = findViewById(R.id.button_clear);
        mReadNfc = findViewById(R.id.button_read_nfc);

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

        mWriteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                writeText();
            }
        });

        mClearButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                clearText();
            }
        });

        mReadNfc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MainActivity.this, TagViewer.class));
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        enableTagWriteMode();
    }

    private void enableTagWriteMode() {
        Intent intent = new Intent(this, getClass());
        mNfcPendingIntent = PendingIntent.getActivity(this, 0, intent, 0);
        mNfcAdapter.enableForegroundDispatch(this, mNfcPendingIntent, null, null);
    }

    @Override
    protected void onPause() {
        disableTagWriteMode();
        super.onPause();
    }

    private void disableTagWriteMode() {
        mNfcAdapter.disableForegroundDispatch(this);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        if (areFieldsEmpty()) return;

        if (NfcAdapter.ACTION_NDEF_DISCOVERED.equals(intent.getAction())
                || NfcAdapter.ACTION_TAG_DISCOVERED.equals(intent.getAction())) {
            Tag detectedTag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);

            String tagMessage = "TNG" + mTextValue.getText().toString();
            NdefRecord mimeRecord = NdefRecord.createMime(mAppMime, tagMessage.getBytes());
            NdefMessage message = new NdefMessage(new NdefRecord[]{mimeRecord});
            if (writeTag(message, detectedTag)) {
                Toast.makeText(this, R.string.success_tag_written, Toast.LENGTH_SHORT)
                        .show();
            }
        }
    }

    /**
     * Check if one or both EditText are empty
     *
     * @return false if one or both empty, true otherwise.
     * Also, a error message is set.
     */
    private boolean areFieldsEmpty() {
        boolean state = false;
        if (mTextValue.getText().length() == 0) {
            mTextValue.setError(getString(R.string.error_text_empty));
            state = true;
        } else {
            mTextValue.setError(null);
        }
        return state;
    }

    /*
     * Writes an NdefMessage to a NFC tag
     */
    public boolean writeTag(NdefMessage message, Tag tag) {
        int size = message.toByteArray().length;
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
                if (ndef.getMaxSize() < size) {
                    mTextValue.setError(String.format(getString(R.string.error_value_toobig),
                            size, ndef.getMaxSize()));
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

    /**
     * Clear text of all EditText
     */
    private void clearText() {
        if (mTextValue.getText().length() != 0) {
            mTextValue.getText().clear();
        }
        Toast.makeText(getApplicationContext(), "Tag Cleared", Toast.LENGTH_LONG).show();
    }

    private void writeText() {
        enableTagWriteMode();

        new AlertDialog.Builder(MainActivity.this).setTitle("Touch tag to write")
                .setOnCancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialog) {
                        disableTagWriteMode();
                    }

                }).create().show();
    }
}
