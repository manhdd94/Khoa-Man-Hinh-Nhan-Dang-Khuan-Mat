package com.manhdd.nuce.khoamanhinhnhandangkhuanmat.manhinh;

import android.app.ActivityManager;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatEditText;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.CompoundButton;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.manhdd.nuce.khoamanhinhnhandangkhuanmat.DichVuManHinhKhoa;
import com.manhdd.nuce.khoamanhinhnhandangkhuanmat.R;
import com.manhdd.nuce.khoamanhinhnhandangkhuanmat.chung.CustomButton;

public class ManHinhCaiDat extends AppCompatActivity {

    private Intent mIntent;
    private SharedPreferences sharedPreferences;
    private int mStep;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        sharedPreferences = getSharedPreferences("KhoaManHinhNhanDangKhuanMat", Context.MODE_PRIVATE); // goi thanh phan luu du lieu

        mIntent = new Intent(this, DichVuManHinhKhoa.class);

        if (sharedPreferences.getBoolean("is_first_run", true)) {
            hienThiYeuCauCaiDatKhuanMat();
        }

        setContentView(R.layout.man_hinh_cai_dat); // gán view vào

        getSupportActionBar().setTitle("Cài đặt màn hình khoá"); // chữ trên thanh tiêu đề

        TextView tvCaiDatKhuanMat = (TextView) findViewById(R.id.tv_cai_dat_khuan_mat);
        tvCaiDatKhuanMat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                moManHinhCaiDatKhuanMat();
            }
        });

        final Switch swSuDungManHinhKhoa = (Switch) findViewById(R.id.sw_su_dung_man_hinh_khoa);
        final boolean isEnable = sharedPreferences.getBoolean("is_enable", true);
        swSuDungManHinhKhoa.setChecked(isEnable);

        if (isEnable && !sharedPreferences.getBoolean("is_first_run", true) && !isMyServiceRunning(DichVuManHinhKhoa.class)) {
            startService(mIntent);
        }

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
                sharedPreferences.edit().putBoolean("is_enable", !enable).apply(); // luu config bat/tat
            }
        });

        swSuDungManHinhKhoa.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b) {
                    startService(mIntent);
                } else {
                    stopService(mIntent);
                }
                sharedPreferences.edit().putBoolean("is_enable", b).apply();
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

        TextView tvCaiDatMatMa = (TextView) findViewById(R.id.tv_cai_dat_mat_ma);
        tvCaiDatMatMa.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                hienThiCaiDatMatMa();
            }
        });

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!Settings.canDrawOverlays(this)) {
                Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + getPackageName()));
                startActivityForResult(intent, 3344);
            }
        }
    }

    private void hienThiYeuCauCaiDatKhuanMat() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Bạn cần cài đặt khuân mặt để sử dụng màn hình khoá")
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        moManHinhCaiDatKhuanMat();
                    }
                })
                .setCancelable(false)
                .show();
    }

    private void moManHinhCaiDatKhuanMat() {
        Intent i = new Intent(ManHinhCaiDat.this, ManHinhNhanDang.class);
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        i.putExtra("is_setting", true);
        startActivity(i);
    }

    private void hienThiCaiDatMatMa() {
        final Dialog dialog = new Dialog(this);
        dialog.setCancelable(true); // để khi bấm ra ngoài sẽ tắt dialog

        LayoutInflater inflater = LayoutInflater.from(this);
        View view = inflater.inflate(R.layout.dialog_cai_dat_mat_ma, null); // gọi view
        dialog.setContentView(view); // custom giao diện dialog

        // set chieu rong cho dialog
        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        lp.copyFrom(dialog.getWindow().getAttributes());
        lp.width = getResources().getDisplayMetrics().widthPixels;
        dialog.getWindow().setAttributes(lp);

        final String matKhauCuDaLuu = sharedPreferences.getString("mat_khau", "");

        final TextView tvMatKhau = (TextView) view.findViewById(R.id.tv_nhap_mat_khau);
        if (!matKhauCuDaLuu.equals("")) {
            tvMatKhau.setText("Nhập mật khẩu cũ");
            mStep = 1;
        } else {
            tvMatKhau.setText("Nhập mật khẩu mới");
            mStep = 2;
        }

        final AppCompatEditText etMatKhau = (AppCompatEditText) view.findViewById(R.id.et_mat_khau);

        view.findViewById(R.id.tv_so_1).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                etMatKhau.setText(etMatKhau.getText().insert(etMatKhau.getSelectionEnd(), "1"));
                etMatKhau.setSelection(etMatKhau.getText().length());
            }
        });

        view.findViewById(R.id.tv_so_2).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                etMatKhau.setText(etMatKhau.getText().insert(etMatKhau.getSelectionEnd(), "2"));
                etMatKhau.setSelection(etMatKhau.getText().length());
            }
        });

        view.findViewById(R.id.tv_so_3).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                etMatKhau.setText(etMatKhau.getText().insert(etMatKhau.getSelectionEnd(), "3"));
                etMatKhau.setSelection(etMatKhau.getText().length());
            }
        });

        view.findViewById(R.id.tv_so_4).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                etMatKhau.setText(etMatKhau.getText().insert(etMatKhau.getSelectionEnd(), "4"));
                etMatKhau.setSelection(etMatKhau.getText().length());
            }
        });

        view.findViewById(R.id.tv_so_5).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                etMatKhau.setText(etMatKhau.getText().insert(etMatKhau.getSelectionEnd(), "5"));
                etMatKhau.setSelection(etMatKhau.getText().length());
            }
        });

        view.findViewById(R.id.tv_so_6).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                etMatKhau.setText(etMatKhau.getText().insert(etMatKhau.getSelectionEnd(), "6"));
                etMatKhau.setSelection(etMatKhau.getText().length());
            }
        });

        view.findViewById(R.id.tv_so_7).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                etMatKhau.setText(etMatKhau.getText().insert(etMatKhau.getSelectionEnd(), "7"));
                etMatKhau.setSelection(etMatKhau.getText().length());
            }
        });

        view.findViewById(R.id.tv_so_8).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                etMatKhau.setText(etMatKhau.getText().insert(etMatKhau.getSelectionEnd(), "8"));
                etMatKhau.setSelection(etMatKhau.getText().length());
            }
        });

        view.findViewById(R.id.tv_so_9).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                etMatKhau.setText(etMatKhau.getText().insert(etMatKhau.getSelectionEnd(), "9"));
                etMatKhau.setSelection(etMatKhau.getText().length());
            }
        });

        view.findViewById(R.id.tv_so_0).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                etMatKhau.setText(etMatKhau.getText().insert(etMatKhau.getSelectionEnd(), "0"));
                etMatKhau.setSelection(etMatKhau.getText().length());
            }
        });

        view.findViewById(R.id.rl_xoa).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String pass = etMatKhau.getText().toString();
                if (pass.length() > 0) {
                    etMatKhau.setText(pass.substring(0, pass.length() - 1));
                }
            }
        });

        CustomButton btLuu = (CustomButton) view.findViewById(R.id.bt_luu);
        btLuu.changeBackgroundColor(Color.parseColor("#FFA500"));
        btLuu.setTextColor(Color.WHITE);
        btLuu.setText("Tiếp tục");
        btLuu.setOnClickListener(new View.OnClickListener() {

            String matKhauMoi = "";

            @Override
            public void onClick(View view) {
                if (mStep == 1) {
                    String pass = etMatKhau.getText().toString();
                    if (pass.equals(matKhauCuDaLuu)) {
                        mStep = 2;
                        tvMatKhau.setText("Nhập mật khẩu mới");
                        etMatKhau.setText("");
                    } else {
                        Toast.makeText(ManHinhCaiDat.this, "Mật khẩu không chính xác", Toast.LENGTH_SHORT).show();
                    }
                } else if (mStep == 2) {
                    String pass = etMatKhau.getText().toString();
                    if (!pass.equals("")) {
                        matKhauMoi = pass;
                        mStep = 3;
                        tvMatKhau.setText("Xác nhận mật khẩu mới");
                        etMatKhau.setText("");
                    } else {
                        Toast.makeText(ManHinhCaiDat.this, "Vui lòng nhập mật khẩu mới", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    String pass = etMatKhau.getText().toString();
                    if (pass.equals(matKhauMoi)) {
                        sharedPreferences.edit().putString("mat_khau", pass).apply();
                        dialog.dismiss();
                    } else {
                        Toast.makeText(ManHinhCaiDat.this, "Vui lòng xác nhận mật khẩu mới", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });

        dialog.show();
    }

    private boolean isMyServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

}
