package com.manhdd.nuce.khoamanhinhnhandangkhuanmat.manhinh;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.manhdd.nuce.khoamanhinhnhandangkhuanmat.R;
import com.manhdd.nuce.khoamanhinhnhandangkhuanmat.adapter.ManHinhKhoaAdapter;
import com.manhdd.nuce.khoamanhinhnhandangkhuanmat.callback.ChonHinhNenCallback;
import com.manhdd.nuce.khoamanhinhnhandangkhuanmat.chung.CustomButton;

/**
 * Created by glenn on 3/8/18.
 */

public class ManHinhChonHinhNenKhoa extends AppCompatActivity {

    private CustomButton btLuuHinhNen;
    private RecyclerView rvListHinhNen;
    private ImageView ivHinhNenKhoa;
    private int idManHinhDaLuu;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.man_hinh_chon_hinh_nen_khoa);

        getSupportActionBar().setTitle("Cài đặt hình nền");

        final SharedPreferences sharedPreferences = getSharedPreferences("KhoaManHinhNhanDangKhuanMat", Context.MODE_PRIVATE);
        idManHinhDaLuu = sharedPreferences.getInt("id_man_hinh_khoa", R.drawable.anh_man_hinh_khoa1);

        ivHinhNenKhoa = (ImageView) findViewById(R.id.iv_hinh_nen_khoa);
        ivHinhNenKhoa.setImageResource(idManHinhDaLuu);

        rvListHinhNen = (RecyclerView) findViewById(R.id.rv_list_hinh_nen);
        rvListHinhNen.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        ManHinhKhoaAdapter adapter = new ManHinhKhoaAdapter(new ChonHinhNenCallback() {
            @Override
            public void chonHinhNen(int idHinhNen) {
                idManHinhDaLuu = idHinhNen;
                ivHinhNenKhoa.setImageResource(idHinhNen);
            }
        }, idManHinhDaLuu);
        rvListHinhNen.setAdapter(adapter);

        btLuuHinhNen = (CustomButton) findViewById(R.id.bt_luu_hinh_nen);
        btLuuHinhNen.changeBackgroundColor(Color.parseColor("#FFA500"));
        btLuuHinhNen.setTextColor(Color.WHITE);
        btLuuHinhNen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sharedPreferences.edit().putInt("id_man_hinh_khoa", idManHinhDaLuu).commit();
                Toast.makeText(ManHinhChonHinhNenKhoa.this, "Lưu hình nền thành công", Toast.LENGTH_SHORT).show();
                finish();
            }
        });

    }

}
