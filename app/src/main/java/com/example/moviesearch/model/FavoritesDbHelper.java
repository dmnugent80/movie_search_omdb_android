package com.example.moviesearch.model;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.text.TextUtils;
import android.util.Log;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class FavoritesDbHelper extends SQLiteOpenHelper {
    private static final String TAG = FavoritesDbHelper.class.getSimpleName();

    private static SQLiteDatabase sDB;
    private static FavoritesDbHelper sInstance;
    private static Context sContext;
    private static final String DATABASE_NAME = "FavoritesDB";
    private static final int DATABASE_VERSION = 1;

    protected static final String FAVORITES = "favorites";

    private enum COL {

        TITLE("TEXT"),
        ID("TEXT"),
        POSTER_URL("TEXT");

        private String mTypeStr;

        COL(String typ) {
            mTypeStr = typ;
        }

        String typeStr() {
            return mTypeStr;
        }

        boolean isText() {
            return mTypeStr.split("\\s+")[0].trim().equals("TEXT");
        }

        boolean isInteger() {
            return mTypeStr.split("\\s+")[0].trim().equals("INTEGER");
        }
    }

    /**
     * Constructor should be private to prevent direct instantiation.
     * Make a call to the static method "getInstance()" instead.
     */
    private FavoritesDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    protected static void init(Context context) {
        getDatabase(context);
    }

    public static synchronized FavoritesDbHelper getInstance(Context context) {
        if (sInstance == null) {
            /*
             * Use the application context, which will ensure that you
             * don't accidentally leak an Activity's context.
             * See this article for more information:
             * https://android-developers.googleblog.com/2009/01/avoiding-memory-leaks.html
             */
            sContext = context.getApplicationContext();
            sInstance = new FavoritesDbHelper(sContext);
        }

        return sInstance;
    }

    private static synchronized SQLiteDatabase getDatabase(Context context) {
        if (sDB == null) {
            sInstance = getInstance(context);
            try {
                // The database connection is cached
                // so it's not expensive to call getWritableDatabase() multiple times.
                sDB = sInstance.getWritableDatabase();
            } catch (SQLiteException e) {
                Log.e(TAG, "SQLiteException in getDatabase()");
            } catch (IllegalStateException e) {
                Log.e(TAG, "IllegalStateException in getDatabase()");
            }
        }

        return sDB;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        Log.d(TAG, "onCreate start");

        createTable(db, FAVORITES,
                COL.TITLE,
                COL.ID,
                COL.POSTER_URL);

        Log.d(TAG, "onCreate end");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.d(TAG, String.format("onUpgrade(%d -> %d)", oldVersion, newVersion));

        db.execSQL("DROP TABLE IF EXISTS " + FAVORITES + ";");
        onCreate(db);

        Log.d(TAG, "onUpgrade ended");
    }

    /////////////////////// write table creation helper methods below //////////////////////////

    private void createTable(SQLiteDatabase db, String tableName, String tableDefinition) {
        String query = String.format("CREATE TABLE IF NOT EXISTS %s (%s);", tableName, tableDefinition);
        Log.d(TAG, "createTable query: " + query);

        try {
            db.execSQL(query);
        } catch (SQLiteException e) {
            Log.e(TAG, "SQLiteException in createTable()");
        } catch (IllegalStateException e) {
            Log.e(TAG, "IllegalStateException in createTable()");
        }
    }

    private void createTable(SQLiteDatabase db, String name, COL... columns) {
        List<String> colStrs = new ArrayList<>();
        for (COL col : columns) {
            String str = String.format("%s %s", col, col.typeStr());
            colStrs.add(str);
        }
        createTable(db, name, TextUtils.join(",", colStrs));
    }

    private void addColumn(SQLiteDatabase db, String table, COL col) {
        if (!columnExists(db, table, col)) {
            String cmd = String.format("ALTER TABLE %s ADD COLUMN %s %s;",
                    table, col, col.typeStr());

            try {
                db.execSQL(cmd);
            } catch (SQLiteException e) {
                Log.e(TAG, "SQLiteException in addColumn()");
            } catch (IllegalStateException e) {
                Log.e(TAG, "IllegalStateException in addColumn()");
            }
        }
    }

    private boolean columnExists(SQLiteDatabase db, String table, COL col) {
        boolean found = false;
        Cursor cursor = db.rawQuery("PRAGMA table_info(" + table + ")", null);

        if (cursor != null) {
            try {
                int index = cursor.getColumnIndex("name");

                while (!found && cursor.moveToNext()) {
                    found = cursor.getString(index).equalsIgnoreCase(col.toString());
                }
            } catch (IllegalStateException e) {
                Log.d(TAG, "IllegalStateException in columnExists()");
            } finally {
                cursor.close();
            }
        }

        // if (BuildConfig.ENABLE_LOGGING) Log.d(TAG, "columnExists(%s) => %b", col, found);
        return found;
    }

    protected void clearTable(Context context, String tableName) {
        SQLiteDatabase db = getDatabase(context);

        try {
            db.delete(tableName, null, null);
        } catch (SQLiteException e) {
            Log.e(TAG, "SQLiteException in clearTable()");
        } catch (IllegalStateException e) {
            Log.e(TAG, "IllegalStateException in clearTable()");
        }
    }

    private int getColumnIndex(Cursor cursor, COL col) {
        return cursor.getColumnIndex(col.toString());
    }

    private String[] columnsFor(COL... cols) {
        String[] result = new String[cols.length];
        for (int ii = 0; ii < result.length; ++ii) {
            result[ii] = cols[ii].toString();
        }
        return result;
    }

    private void put(ContentValues values, Object... pairs) {
        for (int i = 0; i < pairs.length / 2; i++) {
            final COL key = (COL) pairs[2 * i];
            final Object value = pairs[2 * i + 1];

            String type = key.typeStr();
            String colName = key.toString();

            if (type.startsWith("INTEGER")) {
                if (value instanceof Long) {
                    values.put(colName, (Long) value);
                } else {
                    values.put(colName, (Integer) value);
                }
            } else if (type.startsWith("TEXT")) {
                values.put(colName, (String) value);
            } else if (type.startsWith("BLOB")) {
                values.put(colName, (byte[]) value);
            } else {
                Log.d(TAG, "put: don't handle type " + type);
            }
        }
    }

    public void addFavorite(Context context, String title, String id, String url) {
        Log.d(TAG, "addFavorite");

        SQLiteDatabase db = getDatabase(context);
        db.beginTransaction();

        try {
            ContentValues contentValues = new ContentValues();
            put(contentValues, COL.TITLE, title,
                    COL.ID, id,
                    COL.POSTER_URL, url);

            db.insert(FAVORITES, null, contentValues);

            // to save the data set the transaction successful
            db.setTransactionSuccessful();

            Log.d(TAG, "addFavorite transaction success");
        } catch (Exception e) {
            Log.d(TAG, "addFavorite transaction failure");
            e.printStackTrace();
        } finally {
            db.endTransaction();
            Log.d(TAG, "addFavorite transaction end");
        }
    }

    public void removeFavorite(Context context, String title) {
        Log.d(TAG, "addFavorite");

        SQLiteDatabase db = getDatabase(context);
        db.beginTransaction();

        try {
            //TODO: use ID instead of title
            String[] whereArgs = new String[] {title};
            String whereClause = COL.TITLE + " = ?";
            db.delete(FAVORITES, whereClause, whereArgs);

            // to save the data set the transaction successful
            db.setTransactionSuccessful();

            Log.d(TAG, "removeFavorite transaction success");
        } catch (Exception e) {
            Log.d(TAG, "removeFavorite transaction failure");
            e.printStackTrace();
        } finally {
            db.endTransaction();
            Log.d(TAG, "removeFavorite transaction end");
        }
    }

    /**
     * Return list of favorites
     */
    public List<Movie> getFavoritesList(Context context) {
        List<Movie> favorites = new ArrayList<>();

        SQLiteDatabase db = getDatabase(context);
        Cursor cursor = null;

        try {
            cursor = db.rawQuery("select * from " + FAVORITES, null);

            while (cursor.moveToNext()) {
                int indexTitle = getColumnIndex(cursor, COL.TITLE);
                int indexYear = getColumnIndex(cursor, COL.ID);
                int indexPosterUrl = getColumnIndex(cursor, COL.POSTER_URL);

                String title = cursor.getString(indexTitle);
                String year = cursor.getString(indexYear);
                String posterUrl = cursor.getString(indexPosterUrl);

                Movie movie = new Movie(title, year, posterUrl);

                favorites.add(movie);
            }
        } catch (SQLiteException e) {
            Log.e(TAG, "SQLiteException in getSessions()");
        } catch (IllegalStateException e) {
            Log.e(TAG, "IllegalStateException in getSessions()");
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        return favorites;
    }


    /**
     * Return Set of IDs for all favorites
     */
    public Set<String> getFavoritesSet(Context context) {
        Set<String> favoriteSet = new HashSet<>();

        SQLiteDatabase db = getDatabase(context);
        Cursor cursor = null;

        try {
            cursor = db.rawQuery("select * from " + FAVORITES, null);

            while (cursor.moveToNext()) {
                int indexId = getColumnIndex(cursor, COL.ID);
                String id = cursor.getString(indexId);
                favoriteSet.add(id);
            }
        } catch (SQLiteException e) {
            Log.e(TAG, "SQLiteException in getFavoritesSet()");
        } catch (IllegalStateException e) {
            Log.e(TAG, "IllegalStateException in getFavoritesSet()");
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        return favoriteSet;
    }

    private void deleteDB(Context context, String oldDbName) {
        File dbFile = context.getDatabasePath(oldDbName);

        if (dbFile != null && dbFile.exists() && dbFile.delete()) {
            Log.d(TAG, "%s delete successfully " + oldDbName);
        }
    }

}
