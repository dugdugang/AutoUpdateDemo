package com.jpyl.autoupdatedemo;

import android.app.Dialog;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void check(View view) {
        checkAndUpdate();
    }

    private void checkAndUpdate() {
        Intent intent = new Intent(MainActivity.this, UpdateService.class);
        intent.putExtra("apkUrl", "");
        startService(intent);
    }
}
