package com.example.jakub.floristsinventory.data;

import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Created by Jakub on 2017-12-30.
 */

public class FlowersContract {
    public FlowersContract() {
    }

    public static final String CONTENT_AUTHORITY = "com.example.jakub.floristsinventory";
    public static final String PATH_FLOWERS = "flowers";
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    public static final class FlowersEntry implements BaseColumns {
        public static final Uri CONTENT_URI = Uri.withAppendedPath(BASE_CONTENT_URI, PATH_FLOWERS);

        public static final String TABLE_NAME = "flowers";
        public static final String _ID = BaseColumns._ID;
        public static final String COLUMN_FLOWER_NAME = "name";
        public static final String COLUMN_FLOWER_QUANTITY = "quantity";
        public static final String COLUMN_FLOWER_PRICE = "price";
        public static final String COLUMN_FLOWER_TYPE = "type";
        public static final String COLUMN_FLOWER_IMAGE = "image";

        public static final int TYPE_UNKNOWN = 0;
        public static final int TYPE_BIRTHDAY = 1;
        public static final int TYPE_WEDDING = 2;
        public static final int TYPE_FUNERAL = 3;

        public static boolean isValidFlowerType(int flowerType) {
            if (flowerType == TYPE_BIRTHDAY || flowerType == TYPE_WEDDING ||
                    flowerType == TYPE_FUNERAL || flowerType == TYPE_UNKNOWN) {
                return true;
            }
            return false;
        }

    }
}
