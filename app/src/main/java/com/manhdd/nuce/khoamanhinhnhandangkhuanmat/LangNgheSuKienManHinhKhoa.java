package com.manhdd.nuce.khoamanhinhnhandangkhuanmat;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.manhdd.nuce.khoamanhinhnhandangkhuanmat.manhinh.ManHinhKhoa;

public class LangNgheSuKienManHinhKhoa extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();

        // Mo man hinh khoa
        if(action.equals(Intent.ACTION_SCREEN_OFF) || action.equals(Intent.ACTION_BOOT_COMPLETED))
        {
            Intent i = new Intent(context, ManHinhKhoa.class);
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(i);
        }
    }
}
