package com.manhdd.nuce.khoamanhinhnhandangkhuanmat.manhinh;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.manhdd.nuce.khoamanhinhnhandangkhuanmat.DichVuManHinhKhoa;
import com.manhdd.nuce.khoamanhinhnhandangkhuanmat.R;

public class ManHinhCaiDat extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        startService(new Intent(this,DichVuManHinhKhoa.class));

        setContentView(R.layout.man_hinh_cai_dat);
    }

}
