package com.manhdd.nuce.khoamanhinhnhandangkhuanmat;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        startService(new Intent(this,LockScreenService.class));

        setContentView(R.layout.activity_main);
    }

}
