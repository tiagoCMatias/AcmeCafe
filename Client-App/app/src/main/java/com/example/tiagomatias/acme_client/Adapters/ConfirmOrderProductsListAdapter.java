package com.example.tiagomatias.acme_client.Adapters;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.example.tiagomatias.acme_client.Models.OrderProduct;
import com.example.tiagomatias.acme_client.R;

import java.util.ArrayList;

/**
 * Created by Henrique on 20/03/2018.
 */

public class ConfirmOrderProductsListAdapter extends BaseAdapter {

    public ArrayList<OrderProduct> orderProducts;
    int layoutId;
    Context mContext;

    public ConfirmOrderProductsListAdapter(Context mContext, int layoutResourceId,  ArrayList<OrderProduct> orderProducts){
        this.layoutId = layoutResourceId;
        this.mContext = mContext;
        this.orderProducts = orderProducts;
    }

    @Override
    public int getCount() {
        return orderProducts.size();
    }

    @Override
    public Object getItem(int position) {
        return orderProducts.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup viewGroup) {

        View row = convertView;
        ProductHolder holder;


        if(row == null)
        {
            LayoutInflater inflater = ((Activity)mContext).getLayoutInflater();
            row = inflater.inflate(layoutId, viewGroup, false);

            holder = new ConfirmOrderProductsListAdapter.ProductHolder();
            holder.name = row.findViewById(R.id.product_name);
            holder.price = row.findViewById(R.id.product_price);
            holder.quantity = row.findViewById(R.id.product_quantity);


            row.setTag(holder);
        }
        else
        {
            holder = (ProductHolder) row.getTag();
        }

        holder.name.setText(orderProducts.get(position).getName());
        Double pricePerProduct = orderProducts.get(position).getPrice() * orderProducts.get(position).getQuantity();
        Double pricePerProductRound = Math.round(pricePerProduct * 100.0)/100.0;
        holder.price.setText(String.valueOf(pricePerProductRound) + " €");
        holder.quantity.setText(String.valueOf(orderProducts.get(position).getQuantity()) + " x ");

        return row;
    }


    static class ProductHolder
    {
        TextView name;
        TextView price;
        TextView quantity;
    }
}
