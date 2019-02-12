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
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;

public class SetupNfcActivity extends Activity {

    private NfcAdapter mNfcAdapter;
    private PendingIntent mNfcPendingIntent;

    private AutoCompleteTextView mTextValue;
    private ImageView mWriteButton;
    private String mAppMime = "application/com.panera.bread";
    private TextView mBackButton;

    @SuppressWarnings("unused")
    private AlertDialog mDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setup_nfc);

        mWriteButton = findViewById(R.id.button_write);

        mTextValue = findViewById(R.id.edit_value);
        String[] foods = getResources().getStringArray(R.array.tap_n_go_food_list);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, foods);
        mTextValue.setAdapter(adapter);
        mTextValue.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                mWriteButton.setImageDrawable(ContextCompat.getDrawable(getApplicationContext(), R.drawable.button_green_start_beaming_enabled));
                enableTagWriteMode();
            }
        });

        mBackButton = findViewById(R.id.textView1);
        mBackButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

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

        // TODO: Add verify tag feature?
//        mReadNfc.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                startActivity(new Intent(SetupNfcActivity.this, TagViewer.class));
//                overridePendingTransition(0, 0);
//            }
//        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        enableTagWriteMode();
        mWriteButton.setImageDrawable(ContextCompat.getDrawable(getApplicationContext(), R.drawable.button_green_start_beaming_disabled));
    }

    private void enableTagWriteMode() {
        Intent successIntent = new Intent(SetupNfcActivity.this, NfcWriteSuccessActivity.class);
        mNfcPendingIntent = PendingIntent.getActivity(this, 0, successIntent, 0);
        overridePendingTransition(0, 0);
        mNfcAdapter.enableForegroundDispatch(this, mNfcPendingIntent, null, null);
    }

    @Override
    protected void onPause() {
        super.onPause();
        disableTagWriteMode();
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
}
