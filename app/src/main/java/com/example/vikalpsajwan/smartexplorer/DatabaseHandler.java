package com.example.vikalpsajwan.smartexplorer;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by Vikalp on 05/02/2017.
 */

public class DatabaseHandler extends SQLiteOpenHelper {
    static final String dbName = "SmartExplorerDB";
    static final String markedFilesTable = "MarkedFiles";
    static final String colPKid = "_id";
    static final String colFilename = "Filename";
    static final String colFileAddress = "FileAddress";

    private static DatabaseHandler dbHandler;

    private DatabaseHandler(Context context) {
        super(context, dbName, null, 1);
    }

    public static DatabaseHandler getDBInstance(Context context) {
        if (dbHandler == null) {
            dbHandler = new DatabaseHandler(context);
            return dbHandler;
        } else {
            return dbHandler;
        }
    }


    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " +
                markedFilesTable +
                " (" +
                colPKid +
                " INTEGER PRIMARY KEY AUTOINCREMENT , " +
                colFileAddress +
                " TEXT ," +
                colFilename +
                " TEXT)"
        );
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // do nothing for now
    }

    public void addFile(String filename, String fileAddress) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(colFileAddress, fileAddress);
        cv.put(colFilename, filename);

        db.insert(markedFilesTable, null, cv);
        db.close();
    }

    Cursor getAllMarkedFiles() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cur = db.rawQuery("SELECT * FROM " + markedFilesTable, null);
        return cur;
    }

    Cursor searchMarkedFilesByName(String searchString) {
        SQLiteDatabase db = this.getReadableDatabase();
        String pattern = "%" + searchString + "%";
        Cursor cur = db.rawQuery("SELECT * FROM " +
                        markedFilesTable +
                        " WHERE " +
                        colFilename +
                        " LIKE \"" +
                        pattern +
                        "\"",
                null
        );

        return cur;
    }


}
