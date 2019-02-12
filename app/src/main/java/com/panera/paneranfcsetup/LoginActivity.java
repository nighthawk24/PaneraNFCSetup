package com.panera.paneranfcsetup;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class LoginActivity extends Activity {

    private ImageView mButtonSignIn;
    private TextView mHelp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_login);

        mButtonSignIn = findViewById(R.id.button_signin);
        mButtonSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(LoginActivity.this, IntroActivity.class));
                overridePendingTransition(0, 0);
                finish();
            }
        });

        mHelp = findViewById(R.id.textView);
        mHelp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast toast = new Toast(getApplicationContext());
                toast.setText("Contact Panera Admin at 800-411-9000");
                toast.setDuration(Toast.LENGTH_LONG);
                toast.show();
            }
        });

    }

    @Override
    protected void onResume() {
        super.onResume();
    }
}
