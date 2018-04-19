package com.example.jakub.floristsinventory;

import android.app.AlertDialog;
import android.app.LoaderManager;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.RelativeLayout;

import com.example.jakub.floristsinventory.data.FlowersContract.FlowersEntry;

public class CatalogActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {
    private FlowersCursorAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.catalog_activity);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), DetailsActivity.class);
                startActivity(intent);
            }
        });

        ListView listView = (ListView) findViewById(R.id.list);
        RelativeLayout emptyViewImage = (RelativeLayout) findViewById(R.id.empty_view);
        // setEmptyView if there is no data in database
        listView.setEmptyView(emptyViewImage);
        adapter = new FlowersCursorAdapter(getApplicationContext(), null);
        listView.setAdapter(adapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                Intent intent = new Intent(getApplicationContext(), DetailsActivity.class);
                // Create Uri for clicked row in listView
                Uri clickedRowUri = ContentUris.withAppendedId(FlowersEntry.CONTENT_URI, id);
                intent.setData(clickedRowUri);
                startActivity(intent);
            }
        });

        getLoaderManager().initLoader(0, null, this);
    }

    // helper method to convert drawable from resources to String
    private String drawableToString(int resourceId) {
        return
                ContentResolver.SCHEME_ANDROID_RESOURCE + "://" +
                        getResources().getResourcePackageName(resourceId) + "/" +
                        getResources().getResourceTypeName(resourceId) + "/" +
                        getResources().getResourceEntryName(resourceId);
    }

    private void insertCustomFlower() {
        ContentValues values = new ContentValues();
        values.put(FlowersEntry.COLUMN_FLOWER_NAME, getResources().getString(R.string.custom_flower_name));
        values.put(FlowersEntry.COLUMN_FLOWER_PRICE, 75.50);
        values.put(FlowersEntry.COLUMN_FLOWER_TYPE, 2);
        values.put(FlowersEntry.COLUMN_FLOWER_QUANTITY, 15);
        values.put(FlowersEntry.COLUMN_FLOWER_IMAGE, drawableToString(R.drawable.thumbnail_tulips));

        getContentResolver().insert(FlowersEntry.CONTENT_URI, values);
    }

    private void deleteAllEntriesConfirmation() {
        String[] stringsFromResources = getResources().getStringArray(R.array.delete_all_entries);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(stringsFromResources[0]);
        builder.setPositiveButton(stringsFromResources[1], new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                getContentResolver().delete(FlowersEntry.CONTENT_URI, null, null);
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
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.catalog_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.insert_custom_data:
                insertCustomFlower();
                return true;
            case R.id.delete_data:
                deleteAllEntriesConfirmation();
                return true;
            case R.id.order_by:
                Intent intent = new Intent(this, SettingsActivity.class);
                startActivity(intent);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        String[] projection = new String[]{
                FlowersEntry._ID,
                FlowersEntry.COLUMN_FLOWER_NAME,
                FlowersEntry.COLUMN_FLOWER_PRICE,
                FlowersEntry.COLUMN_FLOWER_TYPE,
                FlowersEntry.COLUMN_FLOWER_QUANTITY,
                FlowersEntry.COLUMN_FLOWER_IMAGE
        };
        Uri uri = FlowersEntry.CONTENT_URI;
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        String orderBy = sharedPreferences.getString(getResources().getString(R.string.order_by_key), null);

        return new CursorLoader(getApplicationContext(), uri, projection, null, null, orderBy);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        adapter.changeCursor(cursor);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        adapter.changeCursor(null);
    }
}