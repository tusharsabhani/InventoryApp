package com.example.android.inventoryapp;

import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.android.inventoryapp.data.ProductContract;

public class EditorActivity extends AppCompatActivity implements
        android.app.LoaderManager.LoaderCallbacks<Cursor>, View.OnClickListener {

    private static final int EXISTING_PRODUCT_LOADER = 0;

    private Uri mCurrentProductUri;

    private EditText mProdNameEditText;
    private EditText mPriceEditText;
    private EditText mQuanEditText;
    private EditText mSupNameEditText;
    private EditText mSupNoEditText;
    Button increaseQuan, decreaseQuan;
    ImageView call;

    private boolean mProductHasChanged = false;

    private View.OnTouchListener mToushListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            mProductHasChanged = true;
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor);

        Intent intent = getIntent();
        mCurrentProductUri = intent.getData();

        if (mCurrentProductUri == null) {
            setTitle(getString(R.string.editor_activity_title_new_pet));
            invalidateOptionsMenu();
        } else {
            setTitle(getString(R.string.editor_activity_title_edit_pet));
            getLoaderManager().initLoader(EXISTING_PRODUCT_LOADER, null, this);
        }

        mProdNameEditText = findViewById(R.id.edit_name);
        mPriceEditText = findViewById(R.id.edit_prod_price);
        mQuanEditText = findViewById(R.id.edit_prod_quan);
        mSupNameEditText = findViewById(R.id.edit_sup_name);
        mSupNoEditText = findViewById(R.id.edit_sup_no);

        mProdNameEditText.setOnTouchListener(mToushListener);
        mPriceEditText.setOnTouchListener(mToushListener);
        mQuanEditText.setOnTouchListener(mToushListener);
        mSupNameEditText.setOnTouchListener(mToushListener);
        mSupNoEditText.setOnTouchListener(mToushListener);

        mQuanEditText.setText("0");

        increaseQuan = findViewById(R.id.increase_quantity_button);
        decreaseQuan = findViewById(R.id.decrease_quantity_button);
        call = findViewById(R.id.contact_supplier);

        increaseQuan.setOnClickListener(this);
        decreaseQuan.setOnClickListener(this);
        call.setOnClickListener(this);
    }

    private void saveProduct() {
        String nameString = mProdNameEditText.getText().toString().trim();
        String priceString = mPriceEditText.getText().toString().trim();
        String quanString = mQuanEditText.getText().toString().trim();
        String supNameString = mSupNameEditText.getText().toString().trim();
        String supNoString = mSupNoEditText.getText().toString().trim();


        if (TextUtils.isEmpty(nameString) || TextUtils.isEmpty(priceString) ||
                TextUtils.isEmpty(quanString) || TextUtils.isEmpty(supNameString) ||
                TextUtils.isEmpty(supNoString)) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        if (mCurrentProductUri == null &&
                TextUtils.isEmpty(nameString) && TextUtils.isEmpty(priceString) &&
                TextUtils.isEmpty(quanString) && TextUtils.isEmpty(supNameString) &&
                TextUtils.isEmpty(supNoString)) {
            return;
        }


        ContentValues values = new ContentValues();
        values.put(ProductContract.ProductEntry.COLUMN_PRODUCT_NAME, nameString);
        values.put(ProductContract.ProductEntry.COLUMN_PRODUCT_PRICE, priceString);
        values.put(ProductContract.ProductEntry.COLUMN_PRODUCT_QUANTITY, quanString);
        values.put(ProductContract.ProductEntry.COLUMN_PRODUCT_SUP_NAME, supNameString);
        values.put(ProductContract.ProductEntry.COLUMN_PRODUCT_SUP_NO, supNoString);

        if (mCurrentProductUri == null) {
            Uri newUri = getContentResolver().insert(ProductContract.ProductEntry.CONTENT_URI, values);

            if (newUri == null) {
                Toast.makeText(this, getString(R.string.editor_insert_product_failed),
                        Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, getString(R.string.editor_update_product_successful),
                        Toast.LENGTH_SHORT).show();
                finish();
            }
        } else {
            int rowsAffected = getContentResolver().update(mCurrentProductUri, values, null, null);

            if (rowsAffected == 0) {
                Toast.makeText(this, getString(R.string.editor_update_product_failed),
                        Toast.LENGTH_SHORT).show();
            } else {
                // Otherwise, the update was successful and we can display a toast.
                Toast.makeText(this, getString(R.string.editor_update_product_successful),
                        Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_editor, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        if (mCurrentProductUri == null) {
            MenuItem menuItem = menu.findItem(R.id.action_delete);
            menuItem.setVisible(false);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_save:
                saveProduct();
                return true;
            case R.id.action_delete:
                showDeleteConfirmationDialog();
                return true;
            case android.R.id.home:
                if (!mProductHasChanged) {
                    NavUtils.navigateUpFromSameTask(EditorActivity.this);
                    return true;
                }
                DialogInterface.OnClickListener discardButtonClickListener =
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                NavUtils.navigateUpFromSameTask(EditorActivity.this);
                            }
                        };
                showUnsavedChangesDialog(discardButtonClickListener);
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        if (!mProductHasChanged) {
            super.onBackPressed();
            return;
        }
        DialogInterface.OnClickListener discardButtonClickListener =
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        finish();
                    }
                };
        showUnsavedChangesDialog(discardButtonClickListener);
    }


    @Override
    public android.content.Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        // Since the editor shows all pet attributes, define a projection that contains
        // all columns from the pet table
        String[] projection = {
                ProductContract.ProductEntry._ID,
                ProductContract.ProductEntry.COLUMN_PRODUCT_NAME,
                ProductContract.ProductEntry.COLUMN_PRODUCT_PRICE,
                ProductContract.ProductEntry.COLUMN_PRODUCT_QUANTITY,
                ProductContract.ProductEntry.COLUMN_PRODUCT_SUP_NAME,
                ProductContract.ProductEntry.COLUMN_PRODUCT_SUP_NO};

        return new CursorLoader(this,   // Parent activity context
                mCurrentProductUri,         // Query the content URI for the current product
                projection,             // Columns to include in the resulting Cursor
                null,                   // No selection clause
                null,                   // No selection arguments
                null);                  // Default sort order
    }

    @Override
    public void onLoadFinished(android.content.Loader<Cursor> loader, Cursor cursor) {

        if (cursor == null || cursor.getCount() < 1) {
            return;
        }
        if (cursor.moveToFirst()) {
            int nameColumnIndex = cursor.getColumnIndex(ProductContract.ProductEntry.COLUMN_PRODUCT_NAME);
            int priceColumnIndex = cursor.getColumnIndex(ProductContract.ProductEntry.COLUMN_PRODUCT_PRICE);
            int quanColumnIndex = cursor.getColumnIndex(ProductContract.ProductEntry.COLUMN_PRODUCT_QUANTITY);
            int supNameColumnIndex = cursor.getColumnIndex(ProductContract.ProductEntry.COLUMN_PRODUCT_SUP_NAME);
            int supNoColumnIndex = cursor.getColumnIndex(ProductContract.ProductEntry.COLUMN_PRODUCT_SUP_NO);

            String name = cursor.getString(nameColumnIndex);
            int price = cursor.getInt(priceColumnIndex);
            int quan = cursor.getInt(quanColumnIndex);
            String supName = cursor.getString(supNameColumnIndex);
            int supNo = cursor.getInt(supNoColumnIndex);

            mProdNameEditText.setText(name);
            mPriceEditText.setText(Integer.toString(price));
            mQuanEditText.setText(Integer.toString(quan));
            mSupNameEditText.setText(supName);
            mSupNoEditText.setText(Integer.toString(supNo));
        }
    }

    private void showUnsavedChangesDialog(
            DialogInterface.OnClickListener discardButtonClickListener) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.unsaved_changes_dialog_msg);
        builder.setPositiveButton(R.string.discard, discardButtonClickListener);
        builder.setNegativeButton(R.string.keep_editing, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Keep editing" button, so dismiss the dialog
                // and continue editing the pet.
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private void showDeleteConfirmationDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.delete_dialog_msg);
        builder.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Delete" button, so delete the pet.
                deleteProduct();
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Cancel" button, so dismiss the dialog
                // and continue editing the pet.
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });
        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private void deleteProduct() {
        if (mCurrentProductUri != null) {
            int rowsDeleted = getContentResolver().delete(mCurrentProductUri, null, null);
            if (rowsDeleted == 0) {
                Toast.makeText(this, getString(R.string.editor_delete_productt_failed),
                        Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, getString(R.string.editor_delete_productt_successful),
                        Toast.LENGTH_SHORT).show();
            }
        }
        // Close the activity
        finish();
    }

    @Override
    public void onLoaderReset(android.content.Loader<Cursor> loader) {
        mProdNameEditText.setText("");
        mPriceEditText.setText("");
        mQuanEditText.setText("");
        mSupNameEditText.setText("");
        mSupNoEditText.setText("");
    }

    public void onClick(View v) {
        int quantity = Integer.valueOf(mQuanEditText.getText().toString().trim());
        switch (v.getId()) {
            case R.id.increase_quantity_button:
                mQuanEditText.setText(String.valueOf(quantity + 1));
                break;
            case R.id.decrease_quantity_button:
                if (quantity > 0)
                    mQuanEditText.setText(String.valueOf(quantity - 1));
                else
                    mQuanEditText.setText("0");
                    break;
            case R.id.contact_supplier :
                String supplier_number = mSupNoEditText.getText().toString().trim();
                Intent in = new Intent(Intent.ACTION_DIAL);
                if(supplier_number.isEmpty()){
                    Toast.makeText(this, "Phone number is empty", Toast.LENGTH_SHORT).show();
                    return;
                }
                in.setData(Uri.parse("tel:" + supplier_number));
                if(in.resolveActivity(getPackageManager()) != null)
                    startActivity(in);
        }
    }
}
