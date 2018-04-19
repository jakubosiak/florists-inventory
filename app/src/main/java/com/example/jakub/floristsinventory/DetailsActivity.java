package com.example.jakub.floristsinventory;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.LoaderManager;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NavUtils;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.jakub.floristsinventory.data.FlowersContract.FlowersEntry;

/**
 * This activity allows user to add flowers to database or edit flowers from database.
 */

public class DetailsActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    private EditText mFlowerName;
    private EditText mFlowerPrice;
    private EditText mFlowerQuantity;
    private Spinner mFlowerSpinner;
    private int mFlowerType = 0;
    private ImageView mFlowerImage;

    // Uri of clicked row on listView
    private Uri currentFlowerRow;

    // Uri of image chosen from device
    private Uri imageUri;

    // Uri of image loaded from database
    private Uri cursorLoaderImage;

    // Button which decreases quantity of certain flower
    private Button minusButton;

    // Button which increases quantity of certain flower
    private Button plusButton;

    private Handler updateQuantityHandler = new Handler();
    private boolean autoIncrementQuantity = false;
    private boolean autoDecrementQuantity = false;

    // is true if one of views is touched
    private boolean touchView = false;

    //helper method to set image view height to 1/3 of device screen
    private void setImageHeight(Context context, ImageView image) {
        int newImageParams;

        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        int deviceHeight = displayMetrics.heightPixels;
        newImageParams = deviceHeight / 3;
        image.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, newImageParams));
    }

    @SuppressLint("HandlerLeak")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.details_activity);

        currentFlowerRow = getIntent().getData();
        if (currentFlowerRow == null) {
            setTitle(R.string.details_activity_add);
            //sets delete icon to invisible
            invalidateOptionsMenu();
        } else {
            setTitle(R.string.details_activity_edit);
            getLoaderManager().initLoader(0, null, this);
        }

        mFlowerName = (EditText) findViewById(R.id.flowers_name);
        mFlowerPrice = (EditText) findViewById(R.id.flowers_price);
        mFlowerQuantity = (EditText) findViewById(R.id.flowers_quantity);
        mFlowerSpinner = (Spinner) findViewById(R.id.spinner_type);
        mFlowerImage = (ImageView) findViewById(R.id.main_image);
        minusButton = (Button) findViewById(R.id.minus_button);
        plusButton = (Button) findViewById(R.id.plus_button);

        mFlowerName.setOnTouchListener(onTouchListener);
        mFlowerPrice.setOnTouchListener(onTouchListener);
        mFlowerQuantity.setOnTouchListener(onTouchListener);
        mFlowerSpinner.setOnTouchListener(onTouchListener);
        mFlowerImage.setOnTouchListener(onTouchListener);

        setupSpinner();
        autoUpdateQuantity();
        setImageHeight(this, mFlowerImage);
        mFlowerImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                trySelector();
            }
        });

    }

    View.OnTouchListener onTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            touchView = true;
            return false;
        }
    };

    private void setupSpinner() {
        ArrayAdapter typeSpinnerAdapter = ArrayAdapter.createFromResource(this, R.array.flowers_type_spinner,
                android.R.layout.simple_spinner_item);
        typeSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);
        mFlowerSpinner.setAdapter(typeSpinnerAdapter);
        mFlowerSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selection = (String) parent.getItemAtPosition(position);
                if (!TextUtils.isEmpty(selection)) {
                    if (selection.equals(getString(R.string.type_birthday))) {
                        mFlowerType = FlowersEntry.TYPE_BIRTHDAY;
                    } else if (selection.equals(getString(R.string.type_wedding))) {
                        mFlowerType = FlowersEntry.TYPE_WEDDING;
                    } else if (selection.equals(getString(R.string.type_unknown))) {
                        mFlowerType = FlowersEntry.TYPE_UNKNOWN;
                    } else if (selection.equals(getString(R.string.type_funeral))) {
                        mFlowerType = FlowersEntry.TYPE_FUNERAL;
                    }
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                mFlowerType = 0;
            }
        });
    }

    // helper method to hide system keyboard before finishing activity
    private void hideKeyboard() {
        View view = getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    // helper method to convert drawable from resources to String
    private String drawableToString(int resourceId) {
        return
                ContentResolver.SCHEME_ANDROID_RESOURCE + "://" +
                        getResources().getResourcePackageName(resourceId) + "/" +
                        getResources().getResourceTypeName(resourceId) + "/" +
                        getResources().getResourceEntryName(resourceId);
    }

    private void insertFlower() {
        String name = mFlowerName.getText().toString().trim();
        String priceText = mFlowerPrice.getText().toString().trim();
        String quantityText = mFlowerQuantity.getText().toString().trim();

        if (TextUtils.isEmpty(name) || TextUtils.isEmpty(priceText)) {
            hideKeyboard();
            Toast toast = Toast.makeText(this, getResources().getString(R.string.add_name_and_price), Toast.LENGTH_SHORT);
            toast.show();
            return;
        }

        float price = Float.parseFloat(priceText);
        int quantity;
        try {
            if (!TextUtils.isEmpty(quantityText) && Integer.parseInt(quantityText) >= 0) {
                quantity = Integer.parseInt(quantityText);
            } else {
                quantity = 0;
            }
        } catch (NumberFormatException e) {
            quantity = 0;
            Log.v("Number Format Exception", "String is higher than max Integer value: " + quantityText);
        }

        String image;
        if (imageUri != null) {
            image = String.valueOf(imageUri);
        } else if (currentFlowerRow != null) {
            image = String.valueOf(cursorLoaderImage);
        } else {
            image = drawableToString(R.drawable.thumbnail_tulips);
        }

        ContentValues contentValues = new ContentValues();
        contentValues.put(FlowersEntry.COLUMN_FLOWER_NAME, name);
        contentValues.put(FlowersEntry.COLUMN_FLOWER_PRICE, price);
        contentValues.put(FlowersEntry.COLUMN_FLOWER_TYPE, mFlowerType);
        contentValues.put(FlowersEntry.COLUMN_FLOWER_QUANTITY, quantity);
        contentValues.put(FlowersEntry.COLUMN_FLOWER_IMAGE, image);

        if (currentFlowerRow == null) {
            getContentResolver().insert(FlowersEntry.CONTENT_URI, contentValues);
        } else {
            getContentResolver().update(currentFlowerRow, contentValues, null, null);
        }
        hideKeyboard();
    }

    private void deleteFlower(Uri currentFlowerRow) {
        getContentResolver().delete(currentFlowerRow, null, null);
        hideKeyboard();
    }

    public void deleteConfirmation() {
        String[] stringsFromResources = getResources().getStringArray(R.array.delete_row);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(stringsFromResources[0]);
        builder.setPositiveButton(stringsFromResources[1], new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                deleteFlower(currentFlowerRow);
                finish();
            }
        });
        builder.setNegativeButton(stringsFromResources[2], new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if (dialogInterface != null) {
                    dialogInterface.dismiss();
                }
            }
        });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    @Override
    public void onBackPressed() {
        if (!touchView) {
            super.onBackPressed();
            return;
        }
        DialogInterface.OnClickListener dialogInterface = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                hideKeyboard();
                finish();
            }
        };
        checkForChangesAlert(dialogInterface);
    }

    private void checkForChangesAlert(DialogInterface.OnClickListener dialogInteface) {
        String[] stringsFromResources = getResources().getStringArray(R.array.changes_not_saved);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(stringsFromResources[0]);
        builder.setPositiveButton(stringsFromResources[1], dialogInteface);
        builder.setNegativeButton(stringsFromResources[2], new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if (dialogInterface != null) {
                    dialogInterface.dismiss();

                }
            }
        });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private void trySelector() {
        if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
            return;
        }
        openSelector();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        switch (requestCode) {
            case 1:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    openSelector();
                } else if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_DENIED) {
                    // if user denied permission and ticked 'do not ask again' then display mFlowerImage as R.drawable.thumbnail_tulips
                    if (!ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_EXTERNAL_STORAGE)) {
                        mFlowerImage.setImageResource(R.drawable.thumbnail_tulips);
                    }
                }
        }
    }

    private void openSelector() {
        Intent intent;
        if (Build.VERSION.SDK_INT < 19) {
            intent = new Intent(Intent.ACTION_GET_CONTENT);
        } else {
            intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
        }
        intent.setType("image/*");
        startActivityForResult(Intent.createChooser(intent, "Select picture"), 0);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 0 && resultCode == Activity.RESULT_OK) {
            if (data != null) {
                imageUri = data.getData();
                Glide.with(getApplicationContext()).load(imageUri).into(mFlowerImage);
            }
        }
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        if (currentFlowerRow == null) {
            MenuItem menuItem = menu.findItem(R.id.delete);
            menuItem.setVisible(false);
        }
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.details_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.save:
                insertFlower();
                finish();
                return true;
            case R.id.delete:
                if (currentFlowerRow != null) {
                    deleteConfirmation();
                }
                return true;
            case android.R.id.home:
                if (!touchView) {
                    NavUtils.navigateUpFromSameTask(DetailsActivity.this);
                    return true;
                }
                DialogInterface.OnClickListener dialogInteface = new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        hideKeyboard();
                        NavUtils.navigateUpFromSameTask(DetailsActivity.this);
                    }
                };
                checkForChangesAlert(dialogInteface);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        String[] projection = new String[]{
                FlowersEntry._ID,
                FlowersEntry.COLUMN_FLOWER_NAME,
                FlowersEntry.COLUMN_FLOWER_TYPE,
                FlowersEntry.COLUMN_FLOWER_PRICE,
                FlowersEntry.COLUMN_FLOWER_QUANTITY,
                FlowersEntry.COLUMN_FLOWER_IMAGE
        };

        return new CursorLoader(getApplicationContext(), currentFlowerRow, projection, null, null, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        if (cursor == null || cursor.getCount() < 1) {
            return;
        }

        if (cursor.moveToFirst()) {
            int nameColumn = cursor.getColumnIndex(FlowersEntry.COLUMN_FLOWER_NAME);
            int typeColumn = cursor.getColumnIndex(FlowersEntry.COLUMN_FLOWER_TYPE);
            int priceColumn = cursor.getColumnIndex(FlowersEntry.COLUMN_FLOWER_PRICE);
            int quantityColumn = cursor.getColumnIndex(FlowersEntry.COLUMN_FLOWER_QUANTITY);
            int imageColumn = cursor.getColumnIndex(FlowersEntry.COLUMN_FLOWER_IMAGE);

            String name = cursor.getString(nameColumn);
            int type = cursor.getInt(typeColumn);
            float price = cursor.getFloat(priceColumn);
            int quantity = cursor.getInt(quantityColumn);
            cursorLoaderImage = Uri.parse(cursor.getString(imageColumn));

            mFlowerName.setText(name);
            mFlowerSpinner.setSelection(type);
            mFlowerPrice.setText(String.valueOf(price));
            mFlowerQuantity.setText(String.valueOf(quantity));
            Glide.with(getApplicationContext()).load(cursorLoaderImage).into(mFlowerImage);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mFlowerName.setText("");
        mFlowerSpinner.setSelection(0);
        mFlowerPrice.setText(String.valueOf(""));
        mFlowerQuantity.setText(String.valueOf(""));
        mFlowerImage.setImageBitmap(null);
    }

    private void autoUpdateQuantity() {
        minusButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String currentQuantityText = mFlowerQuantity.getText().toString().trim();
                if (currentQuantityText.isEmpty()) {
                    currentQuantityText = "0";
                }
                int currentQuantity = Integer.parseInt(currentQuantityText);
                if (currentQuantity == 0) {
                    return;
                }
                mFlowerQuantity.setText(String.valueOf(currentQuantity - 1));
            }
        });
        minusButton.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                autoIncrementQuantity = true;
                updateQuantityHandler.post(new UpdateQuantity());
                return false;
            }
        });

        minusButton.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if ((motionEvent.getAction() == MotionEvent.ACTION_UP ||
                        motionEvent.getAction() == MotionEvent.ACTION_CANCEL) &&
                        autoIncrementQuantity) {
                    autoIncrementQuantity = false;
                }
                return false;
            }
        });

        plusButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String currentQuantityText = mFlowerQuantity.getText().toString().trim();
                if (currentQuantityText.isEmpty()) {
                    currentQuantityText = "0";
                }
                int currentQuantity = Integer.parseInt(currentQuantityText);
                mFlowerQuantity.setText(String.valueOf(currentQuantity + 1));
            }
        });

        plusButton.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                autoDecrementQuantity = true;
                updateQuantityHandler.post(new UpdateQuantity());
                return false;
            }
        });

        plusButton.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if ((motionEvent.getAction() == MotionEvent.ACTION_UP ||
                        motionEvent.getAction() == MotionEvent.ACTION_CANCEL) &&
                        autoDecrementQuantity) {
                    autoDecrementQuantity = false;
                }
                return false;
            }
        });
    }

    class UpdateQuantity implements Runnable {
        @Override
        public void run() {
            int currentQuantity;
            if (autoIncrementQuantity) {
                String currentQuantityText = mFlowerQuantity.getText().toString().trim();
                if (currentQuantityText.isEmpty()) {
                    currentQuantityText = "0";
                }
                currentQuantity = Integer.parseInt(currentQuantityText);
                if (currentQuantity == 0) {
                    return;
                }
                mFlowerQuantity.setText(String.valueOf(currentQuantity - 1));
                updateQuantityHandler.postDelayed(new UpdateQuantity(), 100);
            } else if (autoDecrementQuantity) {
                String currentQuantityText = mFlowerQuantity.getText().toString().trim();
                if (currentQuantityText.isEmpty()) {
                    currentQuantityText = "0";
                }
                currentQuantity = Integer.parseInt(currentQuantityText);
                mFlowerQuantity.setText(String.valueOf(currentQuantity + 1));
                updateQuantityHandler.postDelayed(new UpdateQuantity(), 100);
            }
        }
    }
}