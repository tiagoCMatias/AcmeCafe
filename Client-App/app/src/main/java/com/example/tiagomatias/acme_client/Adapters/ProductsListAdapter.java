package com.example.tiagomatias.acme_client.Adapters;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.TextView;

import com.example.tiagomatias.acme_client.Models.Product;
import com.example.tiagomatias.acme_client.R;

import java.util.ArrayList;

/**
 * Created by Henrique on 09/03/2018.
 */

public class ProductsListAdapter extends BaseAdapter {

    ArrayList<Product> products;
    int layoutId;
    Context mContext;

    public ProductsListAdapter(Context mContext, int layoutResourceId,  ArrayList<Product> products) {
        this.layoutId = layoutResourceId;
        this.mContext = mContext;
        this.products = products;
    }

    @Override
    public int getCount() {
        return products.size();
    }

    @Override
    public Object getItem(int position) {
        return products.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup viewGroup) {

        View row = convertView;
        ProductHolder holder = null;

        if(row == null)
        {
            LayoutInflater inflater = ((Activity)mContext).getLayoutInflater();
            row = inflater.inflate(layoutId, viewGroup, false);

            holder = new ProductHolder();
            holder.name = row.findViewById(R.id.product_name);
            holder.price = row.findViewById(R.id.product_price);


            row.setTag(holder);
        }
        else
        {
            holder = (ProductHolder) row.getTag();
        }

        holder.name.setText(products.get(position).getName());
        holder.price.setText(products.get(position).getPrice().toString());

        return row;
    }

    static class ProductHolder
    {
        TextView name;
        TextView price;
    }
}
