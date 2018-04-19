package com.example.jakub.floristsinventory.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.Toast;

import com.example.jakub.floristsinventory.data.FlowersContract.FlowersEntry;

/**
 * Created by Jakub on 2017-12-30.
 */

public class FlowersProvider extends ContentProvider {

    //URI matcher code for the content URI for the flowers table

    private static final int FLOWERS = 100;

    //URI matcher code for the content URI for a single row in the flowers table

    private static final int FLOWERS_ID = 101;

    private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    static {

        sUriMatcher.addURI(FlowersContract.CONTENT_AUTHORITY, FlowersContract.PATH_FLOWERS, FLOWERS);
        sUriMatcher.addURI(FlowersContract.CONTENT_AUTHORITY, FlowersContract.PATH_FLOWERS + "/#", FLOWERS_ID);
    }

    private FlowersDbHelper flowersDbHelper;

    @Override
    public boolean onCreate() {
        flowersDbHelper = new FlowersDbHelper(getContext());
        return true;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection, @Nullable String[] selectionArgs,
                        @Nullable String sortOrder) {
        SQLiteDatabase sqLiteDatabase = flowersDbHelper.getReadableDatabase();

        Cursor cursor;

        int match = sUriMatcher.match(uri);
        switch (match) {
            case FLOWERS:
                cursor = sqLiteDatabase.query(FlowersEntry.TABLE_NAME, projection, selection, selectionArgs,
                        null, null, sortOrder);
                break;
            case FLOWERS_ID:

                selection = FlowersEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                cursor = sqLiteDatabase.query(FlowersEntry.TABLE_NAME, projection, selection, selectionArgs,
                        null, null, sortOrder);
                break;
            default:
                throw new IllegalArgumentException("Cannot query unknown URI " + uri);
        }
        cursor.setNotificationUri(getContext().getContentResolver(), uri);

        return cursor;
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues contentValues) {
        int match = sUriMatcher.match(uri);
        switch (match) {
            case FLOWERS:
                return insertFlower(uri, contentValues);
            default:
                throw new IllegalArgumentException("Insertion is not supported for " + uri);
        }
    }

    private Uri insertFlower(Uri uri, ContentValues values) {
        String name = values.getAsString(FlowersEntry.COLUMN_FLOWER_NAME);
        if (name == null || name.length() == 0) {
            throw new IllegalArgumentException("Flower requires a name");
        }

        Integer type = values.getAsInteger(FlowersEntry.COLUMN_FLOWER_TYPE);
        if (type == null || !FlowersEntry.isValidFlowerType(type)) {
            throw new IllegalArgumentException("Flower requires a type");
        }

        Float price = values.getAsFloat(FlowersEntry.COLUMN_FLOWER_PRICE);
        if (price != null && price < 0) {
            throw new IllegalArgumentException("Flower requires a correct price");
        }

        SQLiteDatabase sqLiteDatabase = flowersDbHelper.getWritableDatabase();

        long id = sqLiteDatabase.insert(FlowersEntry.TABLE_NAME, null, values);

        if (id == -1) {
            Log.e("instertFlower", "Failed to insert row for " + uri);
            return null;
        }

        getContext().getContentResolver().notifyChange(uri, null);
        return ContentUris.withAppendedId(uri, id);
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs) {
        int match = sUriMatcher.match(uri);
        SQLiteDatabase sqLiteDatabase = flowersDbHelper.getWritableDatabase();
        int deletedRows;

        switch (match) {
            case FLOWERS:
                deletedRows = sqLiteDatabase.delete(FlowersEntry.TABLE_NAME, selection, selectionArgs);
                break;
            case FLOWERS_ID:
                selection = FlowersEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                deletedRows = sqLiteDatabase.delete(FlowersEntry.TABLE_NAME, selection, selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("Can't delete for" + uri);
        }
        if (deletedRows != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return deletedRows;
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues contentValues, @Nullable String selection, @Nullable String[] selectionArgs) {
        int match = sUriMatcher.match(uri);
        switch (match) {
            case FLOWERS:
                return updateFlowers(uri, contentValues, selection, selectionArgs);
            case FLOWERS_ID:
                selection = FlowersEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                return updateFlowers(uri, contentValues, selection, selectionArgs);
            default:
                throw new IllegalArgumentException("Update is not supported for " + uri);
        }
    }

    private int updateFlowers(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        if (values.containsKey(FlowersEntry.COLUMN_FLOWER_NAME)) {
            String name = values.getAsString(FlowersEntry.COLUMN_FLOWER_NAME);
            if (name == null || name.length() == 0) {
                throw new IllegalArgumentException("Flower requires a name");
            }
        }

        if (values.containsKey(FlowersEntry.COLUMN_FLOWER_TYPE)) {
            Integer type = values.getAsInteger(FlowersEntry.COLUMN_FLOWER_TYPE);
            if (type == null || !FlowersEntry.isValidFlowerType(type)) {
                throw new IllegalArgumentException("Flower requires a type");
            }
        }

        if (values.containsKey(FlowersEntry.COLUMN_FLOWER_PRICE)) {
            Float price = values.getAsFloat(FlowersEntry.COLUMN_FLOWER_PRICE);
            if (price != null && price < 0) {
                throw new IllegalArgumentException("Flower requires a price");
            }
        }

        if (values.size() == 0) {
            return 0;
        }

        SQLiteDatabase sqLiteDatabase = flowersDbHelper.getWritableDatabase();
        int rowsUpdated = sqLiteDatabase.update(FlowersEntry.TABLE_NAME, values, selection, selectionArgs);
        if (rowsUpdated != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return rowsUpdated;
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        return null;
    }
}
