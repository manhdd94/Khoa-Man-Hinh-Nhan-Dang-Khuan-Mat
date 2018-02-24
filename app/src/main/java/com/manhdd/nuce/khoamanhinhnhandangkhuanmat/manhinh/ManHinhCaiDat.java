package com.manhdd.nuce.khoamanhinhnhandangkhuanmat.manhinh;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;

import com.manhdd.nuce.khoamanhinhnhandangkhuanmat.DichVuManHinhKhoa;
import com.manhdd.nuce.khoamanhinhnhandangkhuanmat.R;

public class ManHinhCaiDat extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        startService(new Intent(this,DichVuManHinhKhoa.class));

        setContentView(R.layout.man_hinh_cai_dat);

        TextView tvCaiDatKhuanMat = (TextView) findViewById(R.id.tv_cai_dat_khuan_mat);
        tvCaiDatKhuanMat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(ManHinhCaiDat.this, ManHinhNhanDang.class);
                i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                i.putExtra("is_setting", true);
                startActivity(i);
            }
        });
    }

}
