package com.panera.paneranfcsetup;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

public class IntroActivity extends Activity {

    private ImageView mWriteTag, mClearTag;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_intro);

        mWriteTag = findViewById(R.id.button_write_tag);
        mClearTag = findViewById(R.id.button_clear_tag);

        mWriteTag.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(IntroActivity.this, SetupNfcActivity.class));
                overridePendingTransition(0, 0);
            }
        });
        mClearTag.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(IntroActivity.this, EraseNfcActivity.class));
                overridePendingTransition(0, 0);
            }
        });
    }
}
