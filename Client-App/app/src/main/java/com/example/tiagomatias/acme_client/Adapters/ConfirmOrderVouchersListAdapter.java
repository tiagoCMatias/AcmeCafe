package com.example.tiagomatias.acme_client.Adapters;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import com.example.tiagomatias.acme_client.Models.OrderProduct;
import com.example.tiagomatias.acme_client.Models.Voucher;
import com.example.tiagomatias.acme_client.R;

import java.util.ArrayList;

/**
 * Created by Henrique on 27/03/2018.
 */

public class ConfirmOrderVouchersListAdapter extends BaseAdapter {

    public ArrayList<Voucher> vouchers;
    int layoutId;
    Context mContext;

    public ConfirmOrderVouchersListAdapter(Context mContext, int layoutResourceId, ArrayList<Voucher> vouchers){
        this.layoutId = layoutResourceId;
        this.mContext = mContext;
        this.vouchers = vouchers;
    }

    @Override
    public int getCount() {
        return vouchers.size();
    }

    @Override
    public Object getItem(int position) {
        return vouchers.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup viewGroup) {
        View row = convertView;
        VoucherHolder holder;


        if(row == null)
        {
            LayoutInflater inflater = ((Activity)mContext).getLayoutInflater();
            row = inflater.inflate(layoutId, viewGroup, false);

            holder = new ConfirmOrderVouchersListAdapter.VoucherHolder();
            holder.name = row.findViewById(R.id.voucher);


            row.setTag(holder);
        }
        else
        {
            holder = (VoucherHolder) row.getTag();
        }

        holder.name.setText(vouchers.get(position).getType());

        holder.name.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

            }
        });

        return row;
    }

    static class VoucherHolder
    {
        CheckBox name;
    }
}
