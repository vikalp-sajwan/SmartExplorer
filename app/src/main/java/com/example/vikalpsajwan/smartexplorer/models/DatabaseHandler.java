package com.example.vikalpsajwan.smartexplorer.models;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by Vikalp on 05/02/2017.
 */

public class DatabaseHandler extends SQLiteOpenHelper {
    static final String dbName = "SmartExplorerDB";

    static final String markedFilesTable = "markedFiles";
    static final String colfileid = "_id";
    static final String colfilename = "colfilename";
    static final String colfileAddress = "colfileAddress";
    static final String colFileType = "colFileType";

    static final String tagsTable = "tags";
    static final String coltagid = "_id";
    static final String coltagName = "coltagName";
    static final String colIsUniqueTag = "colIsUniqueTag";

    static final String fileTagTable = "fileTag";
    static final String colftid = "_id";
    static final String colFtFileid = "colFtFileid";
    static final String colFtTagid = "colFtTagid";


    private static DatabaseHandler dbHandler;

    // data structure to load and hold the files data
    private ArrayList<SmartContent> smartContentData = new ArrayList<SmartContent>();
    private HashMap<Long, SmartContent> smartContentHash = new HashMap<Long, SmartContent>();

    // data structure to load and hold the tags data
    private ArrayList<Tag> tagData = new ArrayList<Tag>();
    private HashMap<Long, Tag> tagHash = new HashMap<Long, Tag>();

    private DatabaseHandler(Context context) {
        super(context, dbName, null, 1);
    }

    public static String getColfileid() {
        return colfileid;
    }

    public static String getColfilename() {
        return colfilename;
    }

    public static String getColfileAddress() {
        return colfileAddress;
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
        //create files details table
        db.execSQL("CREATE TABLE " +
                markedFilesTable +
                " (" +
                colfileid +
                " INTEGER PRIMARY KEY AUTOINCREMENT , " +
                colfileAddress +
                " TEXT , " +
                colfilename +
                " TEXT , " +
                colFileType +
                " INTEGER )"

        );
        // create tags table
        db.execSQL("CREATE TABLE " +
                tagsTable +
                " (" +
                coltagid +
                " INTEGER PRIMARY KEY AUTOINCREMENT , " +
                coltagName +
                " TEXT ," +
                colIsUniqueTag +
                " INTEGER )"
        );

        String[] commonTags = {"work", "home", "entertainment", "project", "presentation"};
        // insert some common tags in tags table

        ContentValues cv = new ContentValues();
        for (String tag : commonTags) {
            cv.put(DatabaseHandler.coltagName, tag);
            cv.put(DatabaseHandler.colIsUniqueTag, "0");
            db.insert(tagsTable, null, cv);
            cv.clear();
        }

        // create file and tag relationship table
        db.execSQL("CREATE TABLE " +
                fileTagTable +
                " (" +
                colftid +
                " INTEGER PRIMARY KEY AUTOINCREMENT , " +
                colFtFileid +
                " TEXT ," +
                colFtTagid +
                " INTEGER," +
                "FOREIGN KEY (" + colFtFileid + ") REFERENCES " + markedFilesTable + "(" + colfileid + ") ," +
                "FOREIGN KEY (" + colFtTagid + ") REFERENCES " + tagsTable + "(" + coltagid + ")" +
                ")"

        );

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // do nothing for now
    }

    /**
     * method to add a file entry in the markedFiles table in the database
     *
     * @param filename
     * @param fileAddress
     * @return
     */
    public long addFile(String filename, String fileAddress) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(DatabaseHandler.colfileAddress, fileAddress);
        cv.put(DatabaseHandler.colfilename, filename);
        cv.put(DatabaseHandler.colFileType, String.valueOf(ContentTypeEnum.Other.ordinal()));

        return db.insert(markedFilesTable, null, cv);
    }

    /**
     * method to check if a tag is present in database or not
     *
     * @param tagName name of the tag to be searched in database
     * @return -1 if tag is not present else the tagid
     */
    public long isTagPresent(String tagName) {
        SQLiteDatabase db = dbHandler.getReadableDatabase();
        long returnValue = -1;
        Cursor cur = db.rawQuery("SELECT " + coltagid + " FROM " +
                tagsTable +
                " WHERE " +
                coltagName +
                " = \"" +
                tagName + "\"", null);

        if (cur.getCount() == 0)
            return returnValue;
        else {
            cur.moveToFirst();
            int colIndex = cur.getColumnIndex(coltagid);
            returnValue = cur.getLong(colIndex);
        }
        if (cur != null)
            cur.close();
        return returnValue;
    }

    /**
     * method to add a new tag in the database
     *
     * @param tagName
     * @return
     */
    public long addTag(String tagName) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(DatabaseHandler.coltagName, tagName);
        cv.put(DatabaseHandler.colIsUniqueTag, "0");

        return db.insert(tagsTable, null, cv);
    }

    /**
     * method to add fileTag entry in the fileTag table
     *
     * @param fileId
     * @param tagId
     */
    public void addFileTagEntry(long fileId, long tagId) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(DatabaseHandler.colFtFileid, fileId);
        cv.put(DatabaseHandler.colFtTagid, tagId);
        db.insert(fileTagTable, null, cv);
    }

    /**
     * method to return all the files details saved in the database
     *
     * @return
     */
    public Cursor getAllFiles() {
        SQLiteDatabase db = dbHandler.getReadableDatabase();
        Cursor cur = db.rawQuery("SELECT * FROM " + markedFilesTable, null);
        return cur;
    }

    /**
     * method to return the files saved in the database based on a search by filename
     *
     * @param searchString
     * @return
     */
    public Cursor searchFilesByName(String searchString) {
        SQLiteDatabase db = dbHandler.getReadableDatabase();
        String pattern = "%" + searchString + "%";
        Cursor cur = db.rawQuery("SELECT * FROM " +
                        markedFilesTable +
                        " WHERE " +
                        colfilename +
                        " LIKE \"" +
                        pattern +
                        "\"",
                null
        );

        return cur;
    }

    /**
     * method to return the files saved in the database based on a search by tagname
     *
     * @param tag
     * @return the cursor containing resultset
     */
    public Cursor searchFilesByTag(String tag) {
        SQLiteDatabase db = dbHandler.getReadableDatabase();

        Cursor cur = db.rawQuery("SELECT " + markedFilesTable + "." + colfileid + ", " + colfilename + ", " + colfileAddress + " FROM " +
                        markedFilesTable + ", " + tagsTable + ", " + fileTagTable +
                        " WHERE " + markedFilesTable + "." + colfileid + "=" + fileTagTable + "." + colFtFileid +
                        " AND " + tagsTable + "." + coltagid + "=" + fileTagTable + "." + colFtTagid +
                        " AND " +
                        coltagName +
                        " LIKE \"" +
                        tag +
                        "\"",
                null
        );

        return cur;
    }

    /**
     * method to return all the tagnames stored in the database
     *
     * @return arraylist of tags stored in the database
     */
    public ArrayList<String> getTagNames() {
        SQLiteDatabase db = dbHandler.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT " + coltagName + " FROM " + tagsTable, null);
        ArrayList<String> tags = new ArrayList<String>();
        int columnIndex = cursor.getColumnIndex(coltagName);
        while (cursor.moveToNext()) {
            tags.add(cursor.getString(columnIndex));
        }
        return tags;
    }

    /**
     * method to query the tags associated with a file from the database
     *
     * @param fileid -  id of the file
     * @return list of tags
     */
    public ArrayList<String> getAssociatedTags(long fileid) {
        SQLiteDatabase db = dbHandler.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT " + coltagName + " FROM "
                        + tagsTable + ", " + fileTagTable +
                        " WHERE " + tagsTable + "." + coltagid + " = " + colFtTagid +
                        " AND " + colFtFileid + " = " + fileid
                , null);

        ArrayList<String> associatedTags = new ArrayList<String>();
        int columnIndex = cursor.getColumnIndex(coltagName);
        while (cursor.moveToNext()) {
            associatedTags.add(cursor.getString(columnIndex));
        }
        return associatedTags;
    }

    /**
     * helper function for the third party Database viewer feature
     *
     * @param Query
     * @return
     */
    public ArrayList<Cursor> getData(String Query) {
        //get writable database
        SQLiteDatabase sqlDB = this.getWritableDatabase();
        String[] columns = new String[]{"message"};
        //an array list of cursor to save two cursors one has results from the query
        //other cursor stores error message if any errors are triggered
        ArrayList<Cursor> alc = new ArrayList<Cursor>(2);
        MatrixCursor Cursor2 = new MatrixCursor(columns);
        alc.add(null);
        alc.add(null);


        try {
            String maxQuery = Query;
            //execute the query results will be save in Cursor c
            Cursor c = sqlDB.rawQuery(maxQuery, null);


            //add value to cursor2
            Cursor2.addRow(new Object[]{"Success"});

            alc.set(1, Cursor2);
            if (null != c && c.getCount() > 0) {

                alc.set(0, c);
                c.moveToFirst();

                return alc;
            }
            return alc;
        } catch (Exception ex) {

            Log.d("printing exception", ex.getMessage());

            //if any exceptions are triggered save the error message to cursor an return the arraylist
            Cursor2.addRow(new Object[]{"" + ex.getMessage()});
            alc.set(1, Cursor2);
            return alc;
        }


    }

    /**
     *  method to load file and tags data from the
     *  database and save them in appropriate data objects in memory
     */
    public void LoadData() {
        // getting tags data
        SQLiteDatabase db = dbHandler.getReadableDatabase();
        Cursor cur = db.rawQuery("SELECT * FROM " + tagsTable, null);

        boolean isUnique;
        while(cur.moveToNext()) {
            Tag tag = new Tag(cur.getLong(0), cur.getString(1), cur.getInt(2));
            tagData.add(tag);
            tagHash.put(cur.getLong(0), tag);
        }

        if (cur != null)
            cur.close();


        // getting file data
        cur = db.rawQuery("SELECT * FROM " + markedFilesTable, null);
        while(cur.moveToNext()){
            SmartContent sC = new SmartContent(cur.getLong(0), cur.getString(1), cur.getString(2), cur.getInt(3));
            smartContentHash.put(cur.getLong(0), sC);
            smartContentData.add(sC);
        }

        if (cur != null )
            cur.close();

        // getting tag file association data
        cur = db.rawQuery("SELECT * FROM " + fileTagTable, null);
        while(cur.moveToNext()){
            Long fileID = cur.getLong(1);
            Long tagID = cur.getLong(2);
            smartContentHash.get(fileID).addTag(tagHash.get(tagID));
        }

        if (cur != null )
            cur.close();
    }

    // method to demonstrate tag and smartContent objects in memory
    public void populateDemoTV(TextView demoTV) {
        for(Tag tag: tagData){
            demoTV.append("tag name: "+tag.getTagName()+"\n"
                    +"tag id: "+tag.getTagId()+"\n"
                    +"Is Unique: "+tag.isUniqueContent());
            demoTV.append("\n\n");
        }

        demoTV.append("-----#############-----\n\n");

        for(SmartContent sC: smartContentData){
            demoTV.append("content id: "+sC.getContentID()+"\n"+
                    "alternate content: "+sC.getAlternateContent()+"\n"+
                    "comment: "+sC.getComment()+"\n"+
                    "file name:"+sC.getContentFileName()+"\n"+
                    "file address:"+sC.getContentUnit().getAddress()+"\n"+
                    "content type: "+sC.getContentUnit().getContentType()+"\n"+
                    "Associate Tags: "
            );
            for(Tag tag: sC.getAssociatedTags()){
                demoTV.append(tag.getTagName()+", ");
            }
            demoTV.append("\n\n");
        }
    }
}
