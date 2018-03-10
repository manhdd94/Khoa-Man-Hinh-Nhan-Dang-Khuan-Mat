package com.manhdd.nuce.khoamanhinhnhandangkhuanmat.manhinh;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.manhdd.nuce.khoamanhinhnhandangkhuanmat.R;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by glenn on 1/30/18.
 */

public class ManHinhKhoa extends AppCompatActivity {

    public WindowManager winManager;
    public RelativeLayout wrapperView;

    private View rootView;
    private TextView tvGio, tvPhut, tvNgayThang;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        taoManHinhFullScreen();
//        setContentView(R.layout.man_hinh_khoa);

        tvGio = (TextView) rootView.findViewById(R.id.tv_gio);
        tvPhut = (TextView) rootView.findViewById(R.id.tv_phut);
        tvNgayThang = (TextView) rootView.findViewById(R.id.tv_ngay_thang);
        capNhatThoiGian();

        ImageView ivHinhNen = (ImageView) rootView.findViewById(R.id.iv_hinh_nen);
        SharedPreferences sharedPreferences = getSharedPreferences("KhoaManHinhNhanDangKhuanMat", Context.MODE_PRIVATE);
        int idManHinhDaLuu = sharedPreferences.getInt("id_man_hinh_khoa", R.drawable.anh_man_hinh_khoa1);
        ivHinhNen.setBackgroundResource(idManHinhDaLuu);

        ImageView ivNhanDangKhuanMat = (ImageView) rootView.findViewById(R.id.iv_nhan_dang_khuan_mat);
        ivNhanDangKhuanMat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ManHinhKhoa.this, ManHinhNhanDang.class);
                intent.putExtra("is_setting", false);
                startActivity(intent);
            }
        });
    }

    @Override
    public void onBackPressed() {

    }

    @Override
    public void onStop() {
        super.onStop();
    }

    public void taoManHinhFullScreen() {
//        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
//                WindowManager.LayoutParams.FLAG_FULLSCREEN);
//        if(Build.VERSION.SDK_INT < 19) { //View.SYSTEM_UI_FLAG_IMMERSIVE is only on API 19+
//            this.getWindow().getDecorView()
//                    .setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
//        } else {
//            this.getWindow().getDecorView()
//                    .setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE
//                            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
//                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
//                            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION // hide nav bar
//                            | View.SYSTEM_UI_FLAG_IMMERSIVE);
//        }

        WindowManager.LayoutParams localLayoutParams = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            localLayoutParams = new WindowManager.LayoutParams(
                    WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                    WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                            | WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
                            | WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
                    PixelFormat.TRANSLUCENT);
        } else {
            localLayoutParams = new WindowManager.LayoutParams(
                    WindowManager.LayoutParams.TYPE_SYSTEM_ERROR,
                    WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                            | WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
                            | WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
                    PixelFormat.TRANSLUCENT);
        }
        this.winManager = ((WindowManager) getApplicationContext()
                .getSystemService(WINDOW_SERVICE));
        this.wrapperView = new RelativeLayout(getBaseContext());
        getWindow().setAttributes(localLayoutParams);
        rootView = View.inflate(this, R.layout.man_hinh_khoa, this.wrapperView);
        this.winManager.addView(this.wrapperView, localLayoutParams);
    }

    @Override
    public void onDestroy() {
        this.winManager.removeView(this.wrapperView);
        this.wrapperView.removeAllViews();
        super.onDestroy();
    }

    private void capNhatThoiGian() {
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {

            @Override
            public void run() {
                runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
                        // TODO Auto-generated method stub
                        String gio = "";
                        String phut = "";
                        String ngay = "";
                        Calendar calendar = Calendar.getInstance();

                        SimpleDateFormat hourFormat = new SimpleDateFormat("HH");
                        SimpleDateFormat minutesFormat = new SimpleDateFormat("mm");

                        gio = hourFormat.format(calendar.getTime());
                        tvGio.setText(gio);
                        phut = minutesFormat.format(calendar.getTime());
                        tvPhut.setText(phut);

                        String weekDay;
                        SimpleDateFormat df2 = new SimpleDateFormat("E", Locale
                                .getDefault());
                        weekDay = df2.format(calendar.getTime());
                        String month;
                        SimpleDateFormat df3 = new SimpleDateFormat("MMM");
                        month = df3.format(calendar.getTime());
                        String date = "" + calendar.get(Calendar.DAY_OF_MONTH);
                        ngay = weekDay + ", " + month + "." + date;
                        tvNgayThang.setText(ngay.toUpperCase());
                    }
                });

            }

        }, 0, 1000);
    }

}
