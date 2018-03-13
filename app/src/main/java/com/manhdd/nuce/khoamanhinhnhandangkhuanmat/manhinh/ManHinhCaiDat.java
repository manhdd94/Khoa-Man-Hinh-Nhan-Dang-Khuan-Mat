package com.manhdd.nuce.khoamanhinhnhandangkhuanmat.manhinh;

import android.app.ActivityManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatEditText;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.manhdd.nuce.khoamanhinhnhandangkhuanmat.DichVuManHinhKhoa;
import com.manhdd.nuce.khoamanhinhnhandangkhuanmat.R;

public class ManHinhCaiDat extends AppCompatActivity {

    private Intent mIntent;
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        sharedPreferences = getSharedPreferences("KhoaManHinhNhanDangKhuanMat", Context.MODE_PRIVATE);

        mIntent = new Intent(this, DichVuManHinhKhoa.class);

        if (sharedPreferences.getBoolean("is_first_run", true)) {
            hienThiYeuCauCaiDatKhuanMat();
        }

        setContentView(R.layout.man_hinh_cai_dat);

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

        if(isEnable && !sharedPreferences.getBoolean("is_first_run", true) && !isMyServiceRunning(DichVuManHinhKhoa.class)) {
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
                sharedPreferences.edit().putBoolean("is_enable", !enable).apply();
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
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setCancelable(false);
        builder.setTitle("Cài đặt mật khẩu dùng trong trường hợp khẩn cấp");

        LayoutInflater inflater = LayoutInflater.from(this);
        View view = inflater.inflate(R.layout.dialog_cai_dat_mat_ma, null);
        builder.setView(view);

        final String matKhauCuDaLuu = sharedPreferences.getString("mat_khau", "");

        final AppCompatEditText etMatKhauCu = (AppCompatEditText) view.findViewById(R.id.et_mat_khau_cu);
        final AppCompatEditText etMatKhauMoi = (AppCompatEditText) view.findViewById(R.id.et_mat_khau_moi);
        final AppCompatEditText etXacNhanMatKhauMoi = (AppCompatEditText) view.findViewById(R.id.et_xac_nhan_mat_khau_moi);

        if(matKhauCuDaLuu.equals("")) {
            etMatKhauCu.setVisibility(View.GONE);
        }

        builder.setPositiveButton("Lưu", null);

        builder.setNegativeButton("Bỏ qua", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        AlertDialog dialog = builder.create();
        dialog.setOnShowListener(new DialogInterface.OnShowListener() {

            @Override
            public void onShow(final DialogInterface dialog) {
                Button b = ((AlertDialog)dialog).getButton(AlertDialog.BUTTON_POSITIVE);
                b.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View view) {
                        if(etMatKhauCu.getVisibility() == View.VISIBLE) {
                            String matKhauCu = etMatKhauCu.getText().toString();
                            if(matKhauCu.equals("")) {
                                Toast.makeText(ManHinhCaiDat.this, "Vui lòng nhập mật khẩu cũ", Toast.LENGTH_SHORT).show();
                                return;
                            }
                            if(!matKhauCu.equals(matKhauCuDaLuu)) {
                                Toast.makeText(ManHinhCaiDat.this, "Mật khẩu cũ không đúng", Toast.LENGTH_SHORT).show();
                                return;
                            }
                        }

                        String matKhauMoi = etMatKhauMoi.getText().toString();
                        String xacNhanMatKhauMoi = etXacNhanMatKhauMoi.getText().toString();

                        if(matKhauMoi.equals("")) {
                            Toast.makeText(ManHinhCaiDat.this, "Vui lòng nhập mật khẩu mới", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        if(!xacNhanMatKhauMoi.equals(matKhauMoi)) {
                            Toast.makeText(ManHinhCaiDat.this, "Xác nhận mật khẩu không khớp", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        sharedPreferences.edit().putString("mat_khau", matKhauMoi).apply();
                        dialog.dismiss();
                    }
                });
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
