package com.panera.paneranfcsetup;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

public class NfcWriteSuccessActivity extends Activity {

    private ImageView button1, button2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_success_write_tag);

        View.OnClickListener done = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        };

        button1 = findViewById(R.id.button1);
        button2 = findViewById(R.id.button2);
        button1.setOnClickListener(done);
        button2.setOnClickListener(done);
    }
}
