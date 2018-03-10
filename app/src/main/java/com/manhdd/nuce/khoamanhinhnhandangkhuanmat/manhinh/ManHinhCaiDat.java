package com.manhdd.nuce.khoamanhinhnhandangkhuanmat.manhinh;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.TextView;

import com.manhdd.nuce.khoamanhinhnhandangkhuanmat.DichVuManHinhKhoa;
import com.manhdd.nuce.khoamanhinhnhandangkhuanmat.R;

public class ManHinhCaiDat extends AppCompatActivity {

    private Intent mIntent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final SharedPreferences sharedPreferences = getSharedPreferences("KhoaManHinhNhanDangKhuanMat", Context.MODE_PRIVATE);

        mIntent = new Intent(this, DichVuManHinhKhoa.class);

        if (sharedPreferences.getBoolean("is_first_run", true)) {
            startService(mIntent);
        }

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

        final Switch swSuDungManHinhKhoa = (Switch) findViewById(R.id.sw_su_dung_man_hinh_khoa);
        final boolean isEnable = sharedPreferences.getBoolean("is_enable", true);
        swSuDungManHinhKhoa.setChecked(isEnable);

        RelativeLayout rlSuDungManHinhKhoa = (RelativeLayout) findViewById(R.id.rl_su_dung_man_hinh_khoa);
        rlSuDungManHinhKhoa.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                boolean enable = swSuDungManHinhKhoa.isChecked();
                if (enable) {
                    stopService(mIntent);
                } else {
                    startService(mIntent);
                }
                swSuDungManHinhKhoa.setChecked(!enable);
                sharedPreferences.edit().putBoolean("is_enable", !enable).commit();
            }
        });

        TextView tvCaiDatHinhNen = (TextView) findViewById(R.id.tv_cai_dat_hinh_nen);
        tvCaiDatHinhNen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ManHinhCaiDat.this, ManHinhChonHinhNenKhoa.class);
                startActivity(intent);
            }
        });

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!Settings.canDrawOverlays(this)) {
                Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + getPackageName()));
                startActivityForResult(intent, 3344);
            }
        }
    }

}
