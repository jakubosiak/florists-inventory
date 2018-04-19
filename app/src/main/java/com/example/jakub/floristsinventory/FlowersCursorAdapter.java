package com.example.jakub.floristsinventory;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.jakub.floristsinventory.data.FlowersContract.FlowersEntry;

import java.text.DecimalFormat;

/**
 * Created by Jakub on 2018-01-04.
 */

public class FlowersCursorAdapter extends CursorAdapter {
    private Toast toast;

    public FlowersCursorAdapter(Context context, Cursor c) {
        super(context, c, 0);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup viewGroup) {
        return LayoutInflater.from(context).inflate(R.layout.list_item, viewGroup, false);
    }


    @Override
    public void bindView(View view, final Context context, final Cursor cursor) {
        TextView nameTextView = (TextView) view.findViewById(R.id.flowers_name);
        final TextView priceTextView = (TextView) view.findViewById(R.id.flowers_price);
        final TextView quantityTextView = (TextView) view.findViewById(R.id.flowers_quantity);
        ImageView imageImageView = (ImageView) view.findViewById(R.id.flowers_image);
        Button soldButton = (Button) view.findViewById(R.id.sold_button);
        Button saleButton = (Button) view.findViewById(R.id.sale_button);

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(view.getContext());
        String chosenCurrency = sharedPreferences.getString(context.getResources().getString(R.string.currency_key), context.getResources().getString(R.string.currency_defaultValue));

        DecimalFormat decimalFormat = new DecimalFormat("0.00");

        String nameFromDb = cursor.getString(cursor.getColumnIndex(FlowersEntry.COLUMN_FLOWER_NAME));
        String priceFromDb = decimalFormat.format(cursor.getFloat(cursor.getColumnIndex(FlowersEntry.COLUMN_FLOWER_PRICE)));
        String quantityFromDb = String.valueOf(cursor.getInt(cursor.getColumnIndex(FlowersEntry.COLUMN_FLOWER_QUANTITY)));
        Uri imageFromDb = Uri.parse(cursor.getString(cursor.getColumnIndex(FlowersEntry.COLUMN_FLOWER_IMAGE)));

        nameTextView.setText(nameFromDb);
        priceTextView.setText(priceFromDb + chosenCurrency);
        quantityTextView.setText(quantityFromDb);
        Glide.with(view.getContext()).load(imageFromDb).into(imageImageView);

        final int currentQuantity = cursor.getInt(cursor.getColumnIndex(FlowersEntry.COLUMN_FLOWER_QUANTITY));
        final int id = cursor.getInt(cursor.getColumnIndex(FlowersEntry._ID));
        soldButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (currentQuantity <= 0) {
                    if (toast != null) {
                        toast.cancel();
                    }
                    toast = Toast.makeText(context, context.getResources().getString(R.string.sold_button_if_0), Toast.LENGTH_SHORT);
                    toast.show();
                    return;
                }
                int updatedQuantity;
                updatedQuantity = currentQuantity - 1;
                ContentValues values = new ContentValues();
                values.put(FlowersEntry.COLUMN_FLOWER_QUANTITY, updatedQuantity);
                context.getContentResolver().update(ContentUris.withAppendedId(FlowersEntry.CONTENT_URI, id), values, null, null);
            }
        });

        final float currentPrice = cursor.getFloat(cursor.getColumnIndex(FlowersEntry.COLUMN_FLOWER_PRICE));
        final String saleValue = sharedPreferences.getString(context.getResources().getString(R.string.sale_price_key), context.getResources().getString(R.string.sale_price_defaultValue));
        if (!TextUtils.isEmpty(saleValue) && Integer.parseInt(saleValue) > 100) {
            saleButton.setText("-100%");
        } else {
            saleButton.setText("-" + saleValue + "%");
        }
        if ((!TextUtils.isEmpty(saleValue) && Integer.parseInt(saleValue) <= 0) || TextUtils.isEmpty(saleValue)) {
            saleButton.setVisibility(View.INVISIBLE);
        } else {
            saleButton.setVisibility(View.VISIBLE);
        }

        saleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                float setPrice = 0;
                if (Integer.parseInt(saleValue) <= 100 && currentPrice > 0.01) {
                    setPrice = currentPrice - (currentPrice * (Integer.parseInt(saleValue) / 100f));
                }
                ContentValues values = new ContentValues();
                values.put(FlowersEntry.COLUMN_FLOWER_PRICE, setPrice);
                context.getContentResolver().update(ContentUris.withAppendedId(FlowersEntry.CONTENT_URI, id), values, null, null);
            }
        });
    }
}