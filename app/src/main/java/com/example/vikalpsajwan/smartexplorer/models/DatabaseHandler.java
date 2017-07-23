package com.example.vikalpsajwan.smartexplorer.models;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Scanner;

/**
 * Created by Vikalp on 05/02/2017.
 */

public class DatabaseHandler extends SQLiteOpenHelper {
//    /**
//        interface instance to employ listener pattern
//     */
//    public DatabaseUpdatedResponse dbResponse;

    Context context;

    static final String dbName = "SmartExplorerDB";

    static final String contentTable = "content";
    static final String colContentId = "_id";
    static final String colContentName = "colContentName";
    static final String colContentDescription = "colContentDescription";
    static final String colContentAddress = "colContentAddress";
    static final String colContentType = "colContentType";

    static final String tagsTable = "tags";
    static final String coltagId = "_id";
    static final String coltagName = "coltagName";
    static final String colIsUniqueTag = "colIsUniqueTag";

    static final String contentTagTable = "contentTag";
    static final String colContentTagid = "_id";
    static final String colCtContentId = "colCtContentId";
    static final String colCtTagId = "colCtTagId";

    static final String contentTypesTable = "contentTypes";
    static final String colContentTypeId = "_id";
    static final String colContentExtension = "colContentExtension";
    static final String colContentCategory = "colContentCategory";

    static final String tagAccessTable = "tagAccess";
    static final String colTagAccessId = "_id";
    static final String colTaTagId = "colTaTagId";
    static final String colTaTimeStamp = "colTaTimeStamp";

//    static final String textTable = "text";
//    static final String colTextid = "_id";
//    static final String colTextContentid = "colTextContentid";
//    static final String colText = "colText";

    public static final String DB_UPDATED = "dBUpdated";

    private static DatabaseHandler dbHandler;
    public HashMap<Long, Tag> tagHash;
    public HashMap<String, ContentTypeEnum> fileExtensionHash;
    // data structure to load and hold the files data
    private ArrayList<SmartContent> smartContentData;
    private HashMap<Long, SmartContent> smartContentHash;
    // data structure to load and hold the tags data
    private ArrayList<Tag> tagData;
    // data Structure to store text Content in memory
    private HashMap<Long, TextContent> textContentHash;
    private HashMap<Long, ArrayList<Long>> tagAccessData;

    private DatabaseHandler(Context context) {
        super(context, dbName, null, 1);
    }

    public static String getColContentId() {
        return colContentId;
    }

    public static String getColContentName() {
        return colContentName;
    }

    public static String getColContentAddress() {
        return colContentAddress;
    }

    public static DatabaseHandler getDBInstance(Context context) {
        if (dbHandler == null) {
            dbHandler = new DatabaseHandler(context);
            dbHandler.context = context;
            try {
                dbHandler.loadData();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            return dbHandler;
        } else {
            dbHandler.context = context;
            return dbHandler;
        }
    }

    public HashMap<Long, SmartContent> getSmartContentHash() {
        return smartContentHash;
    }

    public HashMap<Long, TextContent> getTextContentHash() {
        return textContentHash;
    }

    public ArrayList<SmartContent> getSmartContentData() {
        return smartContentData;
    }

    public ArrayList<Tag> getTagData() {
        return tagData;
    }

    public HashMap<Long, Tag> getTagHash() {
        return tagHash;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
 //       dbResponse = null;

        //create files details table
        db.execSQL("CREATE TABLE " +
                contentTable +
                " (" +
                colContentId +
                " INTEGER PRIMARY KEY AUTOINCREMENT , " +
                colContentAddress +
                " TEXT , " +
                colContentName +
                " TEXT , " +
                colContentDescription +
                " TEXT , " +
                colContentType +
                " INTEGER )"

        );
        // create tags table
        db.execSQL("CREATE TABLE " +
                tagsTable +
                " (" +
                coltagId +
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
            cv.put(DatabaseHandler.colIsUniqueTag, 0);
            db.insert(tagsTable, null, cv);
            cv.clear();
        }

        // create file and tag relationship table
        db.execSQL("CREATE TABLE " +
                contentTagTable +
                " (" +
                colContentTagid +
                " INTEGER PRIMARY KEY AUTOINCREMENT , " +
                colCtContentId +
                " INTEGER ," +
                colCtTagId +
                " INTEGER," +
                "FOREIGN KEY (" + colCtContentId + ") REFERENCES " + contentTable + "(" + colContentId + ") ," +
                "FOREIGN KEY (" + colCtTagId + ") REFERENCES " + tagsTable + "(" + coltagId + ")" +
                ")"

        );

        // create filetypes table which maps file extension to ContentTypeEnum
        db.execSQL("CREATE TABLE " +
                contentTypesTable +
                " (" +
                colContentTypeId +
                " INTEGER PRIMARY KEY AUTOINCREMENT , " +
                colContentExtension +
                " TEXT , " +
                colContentCategory +
                " INTEGER " +
                ")"

        );

        // create tagsAccess table
        db.execSQL("CREATE TABLE " +
                tagAccessTable +
                " (" +
                colTagAccessId +
                " INTEGER PRIMARY KEY AUTOINCREMENT , " +
                colTaTagId +
                " INTEGER ," +
                colTaTimeStamp +
                " INTEGER ," +
                "FOREIGN KEY (" + colTaTagId + ") REFERENCES " + tagsTable + "(" + coltagId + ")" +
                ")"
        );


        populateFileTypesTable(db);

    }

    private void populateFileTypesTable(SQLiteDatabase db) {
        String[] image = {
                "bmp",
                "gif",
                "ico",
                "jpeg",
                "jpg",
                "png",
                "pic",
                "svg",
                "tif",
                "tiff"
        };

        String[] video = {
                "3gp",
                "3gpp",
                "avi",
                "divx",
                "flv",
                "mov",
                "mpeg",
                "mpg",
                "webm",
                "wmv",
                "mp4",
                "mkv"
        };

        String[] audio = {
                "aa",
                "aac",
                "flac",
                "m3u",
                "m4a",
                "mid",
                "midi",
                "mp3",
                "ogg",
                "wav",
                "wma",
                "ra",
                "la",
                "m3u"
        };

        String[] document = {
                "pdf",
                "epub",
                "doc",
                "docx",
                "ppt",
                "note",
                "txt",
                "pptx",
                "rtf",
                "txt",
                "wps",
                "dotx",
                "uml",
                "xml",
                "html",
                "htm",
                "dot",
                "dotx",
                "log",
                "mobi",
        };

        ContentValues cv = new ContentValues();
        for (String extension : image) {
            cv.put(DatabaseHandler.colContentExtension, extension);
            cv.put(DatabaseHandler.colContentCategory, ContentTypeEnum.Image.ordinal());
            db.insert(contentTypesTable, null, cv);
            cv.clear();
        }
        for (String extension : video) {
            cv.put(DatabaseHandler.colContentExtension, extension);
            cv.put(DatabaseHandler.colContentCategory, ContentTypeEnum.Video.ordinal());
            db.insert(contentTypesTable, null, cv);
            cv.clear();
        }
        for (String extension : audio) {
            cv.put(DatabaseHandler.colContentExtension, extension);
            cv.put(DatabaseHandler.colContentCategory, ContentTypeEnum.Audio.ordinal());
            db.insert(contentTypesTable, null, cv);
            cv.clear();
        }
        for (String extension : document) {
            cv.put(DatabaseHandler.colContentExtension, extension);
            cv.put(DatabaseHandler.colContentCategory, ContentTypeEnum.Document.ordinal());
            db.insert(contentTypesTable, null, cv);
            cv.clear();
        }

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
    public long addFile(String filename, String fileAddress, String contentDescription, ContentTypeEnum contentType) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(DatabaseHandler.colContentAddress, fileAddress);
        cv.put(DatabaseHandler.colContentName, filename);
        cv.put(DatabaseHandler.colContentDescription, contentDescription);
        cv.put(DatabaseHandler.colContentType, contentType.ordinal());

        return db.insert(contentTable, null, cv);
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
        Cursor cur = db.rawQuery("SELECT " + coltagId + " FROM " +
                tagsTable +
                " WHERE " +
                coltagName +
                " = \"" +
                tagName + "\"", null);

        if (cur.getCount() == 0)
            return returnValue;
        else {
            cur.moveToFirst();
            int colIndex = cur.getColumnIndex(coltagId);
            returnValue = cur.getLong(colIndex);
        }
        if (cur != null)
            cur.close();
        return returnValue;
    }


    /**
     * method to return a list of tags which have at least one associated content
     * @return arraylist<Tag>
     */
    public ArrayList<Tag> getUsedTags(){
        ArrayList<Tag> returnTags = new ArrayList<>();

        for(Tag tag : tagData){
            if(tag.getAssociatedContent().size() > 0 )
                returnTags.add(tag);
        }

        return returnTags;

    }


    /**
     * method to add a new tag in the database
     *
     * @param tagName
     * @param isUnique
     * @return
     */
    public long addTag(String tagName, Boolean isUnique) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(DatabaseHandler.coltagName, tagName);
        if (isUnique)
            cv.put(DatabaseHandler.colIsUniqueTag, 1);
        else
            cv.put(DatabaseHandler.colIsUniqueTag, 0);

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
        cv.put(DatabaseHandler.colCtContentId, fileId);
        cv.put(DatabaseHandler.colCtTagId, tagId);
        db.insert(contentTagTable, null, cv);
    }

    /**
     * method to return all the files details saved in the database
     *
     * @return
     */
    public Cursor getAllFiles() {
        SQLiteDatabase db = dbHandler.getReadableDatabase();
        Cursor cur = db.rawQuery("SELECT * FROM " + contentTable, null);
        return cur;
    }

//    /**
//     * method to return the files saved in the database based on a search by filename
//     *
//     * @param searchString
//     * @return
//     */
//    public ArrayList<SmartContent> searchContentByString(String searchString) {
//
//        SQLiteDatabase db = dbHandler.getReadableDatabase();
//
////        Cursor cur = db.rawQuery("SELECT " + contentTable + "." + colContentId + " FROM " +
////                        contentTable + ", " + tagsTable + ", " + contentTagTable +
////                        " WHERE " + contentTable + "." + colContentId + "=" + contentTagTable + "." + colCtContentId +
////                        " AND " + tagsTable + "." + coltagId + "=" + contentTagTable + "." + colCtTagId +
////                        " AND (" +
////                        coltagName +
////                        " LIKE '%" +
////                        searchString +
////                        "%'" +
////                        " OR " +
////                        colContentName +
////                        " LIKE '%" +
////                        searchString +
////                        "%')",
////                null
////        );
////
////        Cursor cur = db.rawQuery("SELECT newTable." + colContentId + " FROM " +
////                        "(SELECT " + contentTable + "." + colContentId + ", " + colContentName + ", " + colCtTagId + " FROM " +
////                        contentTable + " LEFT OUTER JOIN " + contentTagTable + " ON "+
////                        contentTable + "." + colContentId + " = " + contentTagTable + "." + colCtContentId + ") AS newTable, " +
////                        tagsTable +
////                        " ON " +
////                        "newTable." + colContentId + " = " + contentTagTable + "." + colCtContentId
////                        " WHERE " +
//////                        tagsTable + "." + coltagId + " = newTable." + colCtTagId +
//////                        " AND (" +
////                        coltagName +
////                        " LIKE '%" +
////                        searchString +
////                        "%'" +
////                        " OR " +
////                        colContentName +
////                        " LIKE '%" +
////                        searchString +
////                        "%'",
////                null
////        );
//
//        Cursor cur = db.rawQuery("SELECT " + contentTable + "." + colContentId + " FROM " +
//                        contentTable +
//                        " LEFT OUTER JOIN " +
//                        "(SELECT " + contentTagTable + "." + colCtTagId + ", " + coltagName + ", " + colCtContentId + " FROM " +
//                        contentTagTable + ", " + tagsTable +
//                        " WHERE " +
//                        tagsTable + "." + coltagId + " = " + contentTagTable + "." + colCtTagId + ") AS newTable " +
//                        " ON " +
//                        "newTable." + colCtContentId + " = " + contentTable + "." + colContentId +
//                        " WHERE " +
//                        coltagName +
//                        " LIKE '%" +
//                        searchString +
//                        "%'" +
//                        " OR " +
//                        colContentName +
//                        " LIKE '%" +
//                        searchString +
//                        "%'",
//                null
//        );
//
//        ArrayList<SmartContent> result = new ArrayList<>();
//        HashMap<Long, Boolean> temp = new HashMap<>();
//        SmartContent sC;
//        while (cur.moveToNext()) {
//            sC = dbHandler.smartContentHash.get(cur.getLong(0));
//            if (temp.get(sC.getContentID()) == null) {
//                result.add(sC);
//                temp.put(sC.getContentID(), true);
//            }
//        }
//
//        return result;
//    }

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
                        + tagsTable + ", " + contentTagTable +
                        " WHERE " + tagsTable + "." + coltagId + " = " + colCtTagId +
                        " AND " + colCtContentId + " = " + fileid
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
     * method to load file and tags data from the
     * database and save them in appropriate data objects in memory
     */
    public void loadData() throws FileNotFoundException {
        tagHash = new HashMap<Long, Tag>();
        fileExtensionHash = new HashMap<String, ContentTypeEnum>();
        smartContentData = new ArrayList<SmartContent>();
        smartContentHash = new HashMap<Long, SmartContent>();
        tagData = new ArrayList<Tag>();
        textContentHash = new HashMap<Long, TextContent>();
        tagAccessData = new HashMap<>();

        // getting tags data
        SQLiteDatabase db = dbHandler.getReadableDatabase();
        Cursor cur = db.rawQuery("SELECT * FROM " + tagsTable, null);

        boolean isUnique;
        while (cur.moveToNext()) {
            if (cur.getInt(2) == 0)
                isUnique = false;
            else
                isUnique = true;
            addTagInMemory(cur.getLong(0), cur.getString(1), isUnique);
        }

        if (cur != null)
            cur.close();


        // getting file data
        cur = db.rawQuery("SELECT * FROM " + contentTable, null);
        while (cur.moveToNext()) {
            SmartContent sC = new SmartContent(cur.getLong(0), cur.getString(1), cur.getString(2), cur.getString(3), cur.getInt(4));
            smartContentHash.put(cur.getLong(0), sC);
            smartContentData.add(sC);
            String contentText = new String();

            // if content is of type text then copy its text in inmemory textContentHash structure
            if (sC.getContentUnit().getContentType() == ContentTypeEnum.Note
                    || sC.getContentUnit().getContentType() == ContentTypeEnum.Location) {
                File file = new File(sC.getContentUnit().getContentAddress());
                Scanner scanner = new Scanner(new FileInputStream(file));
                try {
                    while (scanner.hasNextLine()) {
                        contentText = contentText.concat(scanner.nextLine() + "\n");
                    }
                } finally {
                    scanner.close();
                }
                textContentHash.put(sC.getContentID(), new TextContent(sC.getContentID(), contentText));
            }

        }

        if (cur != null)
            cur.close();

        // getting tag file association data
        cur = db.rawQuery("SELECT * FROM " + contentTagTable, null);
        while (cur.moveToNext()) {
            Long contentID = cur.getLong(1);
            Long tagID = cur.getLong(2);
            smartContentHash.get(contentID).addTag(tagHash.get(tagID));
            tagHash.get(tagID).addAssociatedContent(smartContentHash.get(contentID));
        }

        //getting file extension data
        cur = db.rawQuery("SELECT * FROM " + contentTypesTable, null);
        while (cur.moveToNext()) {
            String extension = cur.getString(1);
            int fileCategory = cur.getInt(2);

            fileExtensionHash.put(extension, ContentTypeEnum.enumFromInt(fileCategory));
        }
        if (cur != null)
            cur.close();

        //getting the tag Access data
        cur = db.rawQuery("SELECT * FROM " + tagAccessTable, null);
        while (cur.moveToNext()){
            Long tagId = cur.getLong(1);
            Long timestamp = cur.getLong(2);

            if(tagAccessData.get(tagId) == null)
                tagAccessData.put(tagId, new ArrayList<Long>());
            tagAccessData.get(tagId).add(timestamp);

        }
        if (cur != null)
            cur.close();

        broadcastDBUpdated();
    }

    //#####******  NOT SO IMPORTANT, CAN BE OMITTED
    public void updateTagAccessDataFromDB(){
        tagAccessData = new HashMap<>();
        //getting the tag Access data
        SQLiteDatabase db = dbHandler.getReadableDatabase();
        Cursor cur = db.rawQuery("SELECT * FROM " + tagAccessTable, null);
        while (cur.moveToNext()){
            Long tagId = cur.getLong(1);
            Long timestamp = cur.getLong(2);

            if(tagAccessData.get(tagId) == null)
                tagAccessData.put(tagId, new ArrayList<Long>());
            tagAccessData.get(tagId).add(timestamp);

        }
        if (cur != null)
            cur.close();

    }

    /**
     *  Method to broadcast that the database has been updated after some operation
     *  so, any activity that is in memory should update its data to show proper output
     */
    public void broadcastDBUpdated(){
        // Send an Intent with an action named "custom-event-name". The Intent sent should
        // be received by the ReceiverActivity.

        Log.d("sender", "Broadcasting message");
        Intent intent = new Intent(DB_UPDATED);
        intent.putExtra("message", "This is my message!");

        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);

        // dbResponse.dataLoadFinish();
    }

    public Tag addTagInMemory(long tagId, String tagName, boolean isUnique) {
        Tag tag = new Tag(tagId, tagName, isUnique);
        tagData.add(tag);
        tagHash.put(tagId, tag);
        return tag;
    }

    public void addSmartContentInMemory(SmartContent sC) {

        smartContentData.add(sC);
        smartContentHash.put(sC.getContentID(), sC);

    }


    // method to demonstrate tag and smartContent objects in memory
    public String populateDemoTV() {
        String returnValue = new String();
        for (int i = 0; i < tagData.size(); i++) {
            returnValue = returnValue.concat("tag name: " + tagData.get(i).getTagName() + "\n"
                    + "tag id: " + tagData.get(i).getTagId() + "\n"
                    + "Is Unique: " + tagData.get(i).isUniqueContent() + "\n" +
                    "Associated files: "
            );
            ArrayList<SmartContent> associatedContent = tagData.get(i).getAssociatedContent();
            for (int j = 0; j < associatedContent.size(); j++) {
                returnValue = returnValue.concat(associatedContent.get(j).getContentName() + ", ");
            }
            returnValue = returnValue.concat("\n\n");
        }

        returnValue = returnValue.concat("-----#############-----\n\n");

        for (int i = 0; i < smartContentData.size(); i++) {
            returnValue = returnValue.concat("content id: " + smartContentData.get(i).getContentID() + "\n" +
                    "alternate content: " + smartContentData.get(i).getAlternateContent() + "\n" +
                    "Description: " + smartContentData.get(i).getContentDescription() + "\n" +
                    "file name:" + smartContentData.get(i).getContentName() + "\n" +
                    "file address:" + smartContentData.get(i).getContentUnit().getContentAddress() + "\n" +
                    "content type: " + smartContentData.get(i).getContentUnit().getContentType() + "\n" +
                    "Associate Tags: "
            );
            ArrayList<Tag> associatedTags = smartContentData.get(i).getAssociatedTags();
            for (int j = 0; j < associatedTags.size(); j++) {
                returnValue = returnValue.concat(associatedTags.get(j).getTagName() + "  ");
            }
            returnValue = returnValue.concat("\nname words: ");
            for (String word : smartContentData.get(i).getContentNameWords()) {
                returnValue = returnValue.concat(word + ",  ");
            }


            returnValue = returnValue.concat("\n\n");
        }

        returnValue = returnValue.concat("-----#############-----\n\n");

        returnValue = returnValue.concat("text content: \n");
        returnValue = returnValue.concat(textContentHash.entrySet().toString());

        returnValue = returnValue.concat("-----#############-----\n\n");


        returnValue = returnValue.concat(fileExtensionHash.toString());

        return returnValue;
    }


    public void saveExtensionType(String extension, ContentTypeEnum contentType) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(DatabaseHandler.colContentExtension, extension);
        cv.put(DatabaseHandler.colContentCategory, contentType.ordinal());

        db.insert(contentTypesTable, null, cv);

        fileExtensionHash.put(extension, contentType);
    }

    public void updateTagUniqueness(long tagId, boolean isUnique) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        if (isUnique == true)
            cv.put(DatabaseHandler.colIsUniqueTag, 1);
        else
            cv.put(DatabaseHandler.colIsUniqueTag, 0);
        db.update(DatabaseHandler.tagsTable, cv, "_id=" + tagId, null);
    }

    public void deleteFileTagEntry(long contentID, long tagId) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(contentTagTable, colCtContentId + "=? and " + colCtTagId + "=?", new String[]{"" + contentID, "" + tagId});
    }

    public void deleteSmartContent(long contentID) {
        SmartContent sC = smartContentHash.get(contentID);

        // delete in database
        for (Tag tag : sC.getAssociatedTags()) {
            deleteFileTagEntry(sC.getContentID(), tag.getTagId());
        }
        deleteFile(sC.getContentID());

        // delete text in main memory in case of text type content
        textContentHash.remove(sC.getContentID());

        // delete in storage
        File file = new File(sC.getContentUnit().getContentAddress());
        if (file.exists()) {
            file.delete();
        }

        // delete in memory
        for (Tag tag : sC.getAssociatedTags()) {
            tag.getAssociatedContent().remove(sC);
//            ArrayList<SmartContent> associatedContent = tag.getAssociatedContent();
//            for(int i = 0; i< associatedContent.size(); i++){
//                if(associatedContent.get(i).getContentID() == sC.getContentID()) {
//                    associatedContent.remove(i);
//                    break;
//                }
//            }
        }
        smartContentHash.remove(sC.getContentID());

//        for(int i = 0; i<smartContentData.size(); i++){
//            if(smartContentData.get(i).getContentID() == sC.getContentID()) {
//                smartContentData.remove(i);
//                break;
//            }
//        }
        smartContentData.remove(sC);

        broadcastDBUpdated();

    }

    private void deleteFile(long contentID) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(contentTable, "_id=?", new String[]{"" + contentID});
    }

    /**
     * edits the content information in database and calls loadData() in last to update in-memory data
     */
    public void editContent(long contentID, String contentName, String contentDescription, ArrayList<String> newTags, ArrayList<Boolean> newTagsUniqueness){
        SmartContent sC = smartContentHash.get(contentID);

        // rename the file in storage
        File from = new File(sC.getContentUnit().getContentAddress());
        File to = new File(context.getExternalFilesDir(null) + File.separator + contentName);
        from.renameTo(to);


        SQLiteDatabase db = this.getWritableDatabase();

        // update data in contents Table
        ContentValues cv = new ContentValues();
        cv.put(DatabaseHandler.colContentAddress, to.getAbsolutePath());
        cv.put(DatabaseHandler.colContentName, contentName);
        cv.put(DatabaseHandler.colContentDescription, contentDescription);

        db.update(DatabaseHandler.contentTable, cv, "_id=" + contentID, null);


        // delete all the entries associated with this content in content_tag table
        db.delete(contentTagTable, colCtContentId + "=?", new String[]{"" + contentID});

        for(int i =0; i<newTags.size(); i++){
            long tagID = isTagPresent(newTags.get(i));

            if(tagID == -1 )     // not present - create tag in tag table and add entry in contentTag table
            {
                cv = new ContentValues();
                cv.put(DatabaseHandler.coltagName, newTags.get(i));
                if(newTagsUniqueness.get(i))
                    cv.put(DatabaseHandler.colIsUniqueTag, 1);
                else
                    cv.put(DatabaseHandler.colIsUniqueTag, 0);

                tagID = db.insert(tagsTable, null, cv);
            }
            else{
                Tag tag = tagHash.get(tagID);


                // if already present tag was unique and now it is changed to notUnique
                // just change the uniqueness of tag in tag table
                // if it was unique and its uniqueness is not changed, do nothing
                if(tag.isUniqueContent() && newTagsUniqueness.get(i)==false){
                    cv = new ContentValues();
                    cv.put(DatabaseHandler.colIsUniqueTag, 0);
                    db.update(DatabaseHandler.tagsTable, cv, "_id=" + tag.getTagId(), null);


                }else if(!tag.isUniqueContent() && newTagsUniqueness.get(i)==true){
                    //already present tag was not unique and now it is changed to Unique
                    // change uniqueness of tag in tag table and delete all associated content except present one
                    // if it was not unique and is still same, then do nothing
                    // do not delete the current smartContent
                    ArrayList<SmartContent> associatedFiles = tag.getAssociatedContent();
                    while(associatedFiles.size()>1){
                        if(associatedFiles.get(0).getContentID()!=sC.getContentID())
                            dbHandler.deleteSmartContent(associatedFiles.get(0).getContentID());
                        else
                            dbHandler.deleteSmartContent(associatedFiles.get(1).getContentID());

                    }

                    cv = new ContentValues();
                    cv.put(DatabaseHandler.colIsUniqueTag, 1);
                    db.update(DatabaseHandler.tagsTable, cv, "_id=" + tag.getTagId(), null);


                }

            }

            // make entry in contentTag table
            cv = new ContentValues();
            cv.put(DatabaseHandler.colCtContentId, sC.getContentID());
            cv.put(DatabaseHandler.colCtTagId, tagID);
            db.insert(contentTagTable, null, cv);

        }


        try {
            loadData();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

    }

    /**
     * return the last 7 entries of SmartContent data
     *
     * @return
     */
    public ArrayList<SmartContent> getRecentContentData() {
        ArrayList<SmartContent> result = new ArrayList<SmartContent>();
        if (smartContentData.size() <= 7) {
            for (int i = smartContentData.size() - 1; i >= 0; i--) {
                result.add(smartContentData.get(i));
            }
        } else {   //return 7 last entries
            for (int i = smartContentData.size() - 1; i >= smartContentData.size() - 7; i--) {
                result.add(smartContentData.get(i));
            }
        }

        return result;
    }

    public ArrayList<String> getAutoCompleteTerms() {
        ArrayList<String> words = new ArrayList<String>();
        for (Tag tag : tagData) {
            if (!words.contains(tag.getTagName()))
                words.add(tag.getTagName());
        }

        Collection<TextContent> collection = textContentHash.values();
        Iterator itr = collection.iterator();
        while (itr.hasNext()) {
            TextContent tc = (TextContent) itr.next();

            for (String word : tc.getNonStopWords())
                if (!words.contains(word))
                    words.add(word);

        }

        for (SmartContent sc : smartContentData) {
            for (String word : sc.getContentNameWords())
                if (!words.contains(word))
                    words.add(word);
        }
        return words;

    }

    /**
     * creates a new entry in tagAccess table with current timestamp
     * one tag can have at most 15 entries
     * @param tagId
     */
    public void updateTagAccess(long tagId){
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues cv = new ContentValues();
        //check if the tag has 15 entries already
        //if already 15, overwrite the one with oldest timestamp
        //else insert a new entry

        Cursor cursor = db.rawQuery("SELECT " + colTagAccessId + " FROM "
                        + tagAccessTable +
                        " WHERE " + colTaTagId + " = " + tagId +
                        " ORDER BY " + colTaTimeStamp + " ASC "
                , null);

        if(cursor.getCount() < 15){
            cv.put(DatabaseHandler.colTaTagId, tagId);
            cv.put(DatabaseHandler.colTaTimeStamp, System.currentTimeMillis());
            db.insert(tagAccessTable, null, cv);
        }else{
            cursor.moveToNext();
            long _id = cursor.getLong(0);
            cv.put(DatabaseHandler.colTaTimeStamp, System.currentTimeMillis());
            db.update(DatabaseHandler.tagAccessTable, cv, "_id=" + _id, null);
        }
    }

    public ArrayList<SmartContent> getAssociatedContent(long tagId){
        Tag tag = tagHash.get(tagId);
        ArrayList<SmartContent> associatedContent = (ArrayList<SmartContent>)tag.getAssociatedContent().clone();
        return associatedContent;

    }

    /**
     * returns the list of tags related to @param tag
     * the relation here is that the tags are also used in the content in which @param tag is used
     * @param tagId
     * @return
     */
    public ArrayList<Tag> getRelatedTags(Long tagId) {
        Tag tag = dbHandler.tagHash.get(tagId);
        ArrayList<SmartContent> associatedContent = tag.getAssociatedContent();
        ArrayList<Tag> returnList = new ArrayList<>();
        for(SmartContent sc : associatedContent){
            ArrayList<Tag> associatedTags = sc.getAssociatedTags();
            for(Tag ascTag: associatedTags){
                if(ascTag != tag && !returnList.contains(ascTag)){
                    returnList.add(ascTag);
                }
            }
        }

        return returnList;
    }

    /**
     * method to calculate and return the tag score based on the last access times
     * and number of associated content
     * @param tag
     * @return score
     */
    public float getTagScore(Tag tag) {
        float score = 0.0f;

        score += 1.5f + Math.log10(tag.getAssociatedContent().size());

        ArrayList<Long> timestamps = tagAccessData.get(tag.getTagId());
        for(Long timestamp: timestamps){
            score += 10 - Math.log10(System.currentTimeMillis() - timestamp);
        }

        return score;
    }
}
