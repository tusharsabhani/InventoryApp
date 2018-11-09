package com.example.android.inventoryapp;

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Toast;

import com.example.android.inventoryapp.data.ProductContract;
import com.example.android.inventoryapp.data.ProductDbHelper;

public class EditorActivity extends AppCompatActivity {

    private EditText mProdNameEditText;

    private EditText mPriceEditText;

    private EditText mQuanEditText;

    private EditText mSupNameEditText;

    private EditText mSupNoEditText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor);

        mProdNameEditText = findViewById(R.id.edit_name);
        mPriceEditText = findViewById(R.id.edit_prod_price);
        mQuanEditText = findViewById(R.id.edit_prod_quan);
        mSupNameEditText = findViewById(R.id.edit_sup_name);
        mSupNoEditText = findViewById(R.id.edit_sup_no);
    }

    private void insertProduct(){
        String nameString = mProdNameEditText.getText().toString().trim();
        String priceString = mPriceEditText.getText().toString().trim();
        int price = Integer.parseInt(priceString);
        String quanString = mQuanEditText.getText().toString().trim();
        int quantity = Integer.parseInt(quanString);
        String supNameString = mSupNameEditText.getText().toString().trim();
        String supNoString = mSupNoEditText.getText().toString().trim();
        int supNo = Integer.parseInt(supNoString);

        ProductDbHelper mDbHelper = new ProductDbHelper(this);

        SQLiteDatabase db = mDbHelper.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(ProductContract.ProductEntry.COLUMN_PRODUCT_NAME, nameString);
        values.put(ProductContract.ProductEntry.COLUMN_PRODUCT_PRICE, price);
        values.put(ProductContract.ProductEntry.COLUMN_PRODUCT_QUANTITY, quantity);
        values.put(ProductContract.ProductEntry.COLUMN_PRODUCT_SUP_NAME, supNameString);
        values.put(ProductContract.ProductEntry.COLUMN_PRODUCT_SUP_NO,supNo);

        long newRowId = db.insert(ProductContract.ProductEntry.TABLE_NAME, null, values);
        if (newRowId == -1) {
            // If the row ID is -1, then there was an error with insertion.
            Toast.makeText(this, "Error with saving product", Toast.LENGTH_SHORT).show();
        } else {
            // Otherwise, the insertion was successful and we can display a toast with the row ID.
            Toast.makeText(this, "Product saved with row id: " + newRowId, Toast.LENGTH_SHORT).show();
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu options from the res/menu/menu_editor.xml file.
        // This adds menu items to the app bar.
        getMenuInflater().inflate(R.menu.menu_editor, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // User clicked on a menu option in the app bar overflow menu
        switch (item.getItemId()) {
            // Respond to a click on the "Save" menu option
            case R.id.action_save:
                // Save pet to database
                insertProduct();
                //Exit activity
                finish();
                return true;
            // Respond to a click on the "Delete" menu option
            case R.id.action_delete:
                // Do nothing for now
                return true;
            // Respond to a click on the "Up" arrow button in the app bar
            case android.R.id.home:
                // Navigate back to parent activity (MainActivity)
                NavUtils.navigateUpFromSameTask(this);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
