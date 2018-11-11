package com.example.android.inventoryapp;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.example.android.inventoryapp.data.ProductContract;

import static com.example.android.inventoryapp.data.ProductProvider.LOG_TAG;

public class ProductCursorAdapter extends CursorAdapter {

    public ProductCursorAdapter(Context context, Cursor c) {
        super(context, c, 0 /* flags */);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        // Inflate a list item view using the layout specified in list_item.xml
        return LayoutInflater.from(context).inflate(R.layout.list_item, parent, false);
    }

    @Override
    public void bindView(View view,final Context context,final Cursor cursor) {

        TextView nameTextView = view.findViewById(R.id.name);
        TextView priceTextView = view.findViewById(R.id.price);
        TextView quantityTextView = view.findViewById(R.id.quantity);
        Button button = view.findViewById(R.id.sale);

        int nameColumnIndex = cursor.getColumnIndex(ProductContract.ProductEntry.COLUMN_PRODUCT_NAME);
        int priceColumnIndex = cursor.getColumnIndex(ProductContract.ProductEntry.COLUMN_PRODUCT_PRICE);
        int quanColumnIndex = cursor.getColumnIndex(ProductContract.ProductEntry.COLUMN_PRODUCT_QUANTITY);

        String proName = cursor.getString(nameColumnIndex);
        String proPrice = cursor.getString(priceColumnIndex);
        String proQuan = cursor.getString(quanColumnIndex);

                if (TextUtils.isEmpty(proQuan)) {
            proQuan = context.getString(R.string.unknown_quantity);
        }

        nameTextView.setText(proName);
        priceTextView.setText(proPrice);
        quantityTextView.setText(proQuan);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                View parentRow = (View) view.getParent();
                ListView lv = (ListView) parentRow.getParent();

                //position is the position of button which starts from 0
                int position = lv.getPositionForView(parentRow);

                int keyColumnIndex = cursor.getColumnIndex(ProductContract.ProductEntry._ID);
                try {
                    cursor.moveToFirst();
                } catch (IllegalStateException e) {
                    Log.e(LOG_TAG, "attempt failed to re-open an already-closed cursor object");
                    return;
                }

                long key = 0;

                //Gets the row ID or PRIMARY_KEY of database into key Int variable.
                for (int i = 1; i <= position + 1; i++) {
                    key = cursor.getLong(keyColumnIndex);
                    cursor.moveToNext();
                }
                Log.v(LOG_TAG, "Current row ID or Primary key of database is: " + key);

                //Moving the cursor to the position where the button was clicked.
                cursor.moveToPosition(position);
                int quantityColumnIndex = cursor.getColumnIndex(ProductContract.ProductEntry.COLUMN_PRODUCT_QUANTITY);

                //currentQuantity and
                String currentQuantity = cursor.getString(quantityColumnIndex);
                int currentQuantityInt = Integer.parseInt(currentQuantity);

                if(currentQuantityInt>0){
                    currentQuantityInt -= 1;
                    Log.v(LOG_TAG, "Current quantity is: " + currentQuantityInt + " and changing to: " + currentQuantityInt);
                    ContentValues values = new ContentValues();
                    values.put(ProductContract.ProductEntry.COLUMN_PRODUCT_QUANTITY, currentQuantityInt);
                    Uri currentProductUri = ContentUris.withAppendedId(ProductContract.ProductEntry.CONTENT_URI, key);
                    context.getContentResolver().update(currentProductUri, values, null, null);
                }
                else
                    Log.v(LOG_TAG,"Quantity cannot be decreased.");

            }
        });
    }
}
