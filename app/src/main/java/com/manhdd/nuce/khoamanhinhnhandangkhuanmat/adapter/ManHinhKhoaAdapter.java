package com.manhdd.nuce.khoamanhinhnhandangkhuanmat.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.manhdd.nuce.khoamanhinhnhandangkhuanmat.R;
import com.manhdd.nuce.khoamanhinhnhandangkhuanmat.callback.ChonHinhNenCallback;

import java.util.ArrayList;

/**
 * Created by glenn on 3/8/18.
 */

public class ManHinhKhoaAdapter extends RecyclerView.Adapter<ManHinhKhoaAdapter.ManHinhKhoaViewHolder> {

    protected ArrayList<Integer> listIDHinhNen;
    protected ChonHinhNenCallback mCallback;
    protected int idManHinhDaLuu;

    public ManHinhKhoaAdapter(ChonHinhNenCallback callback, int idManHinhDaLuu) {
        mCallback = callback;
        this.idManHinhDaLuu = idManHinhDaLuu;

        listIDHinhNen = new ArrayList<>();
        listIDHinhNen.add(R.drawable.anh_man_hinh_khoa1);
        listIDHinhNen.add(R.drawable.anh_man_hinh_khoa2);
        listIDHinhNen.add(R.drawable.anh_man_hinh_khoa3);
    }

    @Override
    public ManHinhKhoaViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.adapter_hinh_nen_khoa, parent, false);
        return new ManHinhKhoaViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ManHinhKhoaViewHolder holder, final int position) {
        final int hinhNenID = listIDHinhNen.get(position);

        holder.ivHinhNenKhoa.setImageResource(hinhNenID);

        if(idManHinhDaLuu == hinhNenID) {
            holder.llHinhNenKhoa.setBackgroundResource(R.drawable.line_border_selected);
        } else {
            holder.llHinhNenKhoa.setBackgroundResource(0);
        }

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                idManHinhDaLuu = hinhNenID;
                notifyDataSetChanged();
                if(mCallback != null) {
                    mCallback.chonHinhNen(idManHinhDaLuu);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return listIDHinhNen.size();
    }

    public class ManHinhKhoaViewHolder extends RecyclerView.ViewHolder {

        private ImageView ivHinhNenKhoa;
        private LinearLayout llHinhNenKhoa;

        public ManHinhKhoaViewHolder(View itemView) {
            super(itemView);
            ivHinhNenKhoa = (ImageView) itemView.findViewById(R.id.iv_hinh_nen_khoa);
            llHinhNenKhoa = (LinearLayout) itemView.findViewById(R.id.ll_hinh_nen_khoa);
        }
    }

}
