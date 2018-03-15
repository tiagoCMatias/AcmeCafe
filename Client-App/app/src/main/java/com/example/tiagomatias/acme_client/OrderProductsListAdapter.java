package com.example.tiagomatias.acme_client;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.TextView;

import com.example.tiagomatias.acme_client.Models.OrderProduct;

import java.util.ArrayList;

/**
 * Created by Henrique on 13/03/2018.
 */

public class OrderProductsListAdapter extends BaseAdapter {

    ArrayList<OrderProduct> orderProducts;
    int layoutId;
    Context mContext;

    public OrderProductsListAdapter(Context mContext, int layoutResourceId,  ArrayList<OrderProduct> orderProducts){
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
        return 0;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup viewGroup) {

        View row = convertView;
        ProductHolder holder = null;


        if(row == null)
        {
            LayoutInflater inflater = ((Activity)mContext).getLayoutInflater();
            row = inflater.inflate(layoutId, viewGroup, false);

            holder = new ProductHolder();
            holder.name = row.findViewById(R.id.product_name);
            holder.price = row.findViewById(R.id.product_price);
            holder.quantity = row.findViewById(R.id.quantity);


            row.setTag(holder);
        }
        else
        {
            holder = (ProductHolder) row.getTag();
        }

        holder.name.setText(orderProducts.get(position).getName());
        holder.price.setText(orderProducts.get(position).getPrice().toString());
        holder.quantity.setText(orderProducts.get(position).getQuantity().toString());

        ImageButton minus =  row.findViewById(R.id.minus);
        final ProductHolder finalHolder = holder;
        minus.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Integer quantity = orderProducts.get(position).getQuantity();
                if(quantity > 0){
                    quantity -= 1;
                    orderProducts.get(position).setQuantity(quantity);
                    finalHolder.quantity.setText(orderProducts.get(position).getQuantity().toString());
                }
            }
        });

        ImageButton plus = (ImageButton) row.findViewById(R.id.plus);
        final ProductHolder finalHolderP = holder;
        plus.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                    Integer quantity = orderProducts.get(position).getQuantity();
                    quantity += 1;
                    orderProducts.get(position).setQuantity(quantity);
                    finalHolderP.quantity.setText(orderProducts.get(position).getQuantity().toString());
            }
        });

        return row;
    }

    static class ProductHolder
    {
        TextView name;
        TextView price;
        TextView quantity;
    }
}
