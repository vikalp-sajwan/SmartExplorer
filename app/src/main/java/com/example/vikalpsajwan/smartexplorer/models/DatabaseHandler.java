package com.example.vikalpsajwan.smartexplorer.models;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import android.widget.TextView;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;

import static android.R.attr.id;

/**
 * Created by Vikalp on 05/02/2017.
 */

public class DatabaseHandler extends SQLiteOpenHelper {
    static final String dbName = "SmartExplorerDB";

    static final String markedFilesTable = "markedFiles";
    static final String colfileid = "_id";
    static final String colFilename = "colFilename";
    static final String colfileAddress = "colfileAddress";
    static final String colFileType = "colFileType";

    static final String tagsTable = "tags";
    static final String coltagid = "_id";
    static final String coltagName = "coltagName";
    static final String colIsUniqueTag = "colIsUniqueTag";

    static final String fileTagTable = "fileTag";
    static final String colFileTagid = "_id";
    static final String colFtFileid = "colFtFileid";
    static final String colFtTagid = "colFtTagid";

    static final String fileTypesTable = "fileTypes";
    static final String colFileTypeid = "_id";
    static final String colFileExtension = "colFileExtension";
    static final String colFileCategory = "colFileCategory";

    static final String textTable = "text";
    static final String colTextid = "_id";
    static final String colTextContentid = "colTextContentid";
    static final String colText = "colText";


    private static DatabaseHandler dbHandler;
    public HashMap<Long, Tag> tagHash = new HashMap<Long, Tag>();
    public HashMap<String, ContentTypeEnum> fileExtensionHash = new HashMap<String, ContentTypeEnum>();
    // data structure to load and hold the files data
    private ArrayList<SmartContent> smartContentData = new ArrayList<SmartContent>();
    private HashMap<Long, SmartContent> smartContentHash = new HashMap<Long, SmartContent>();

    // data structure to load and hold the tags data
    private ArrayList<Tag> tagData = new ArrayList<Tag>();
    // data Structure to store text Content in memory
    private HashMap<Long, TextContent> textContent = new HashMap<Long, TextContent>();

    private DatabaseHandler(Context context) {
        super(context, dbName, null, 1);
    }

    public static String getColfileid() {
        return colfileid;
    }

    public static String getColFilename() {
        return colFilename;
    }

    public static String getColfileAddress() {
        return colfileAddress;
    }

    public static DatabaseHandler getDBInstance(Context context) {
        if (dbHandler == null) {
            dbHandler = new DatabaseHandler(context);
            try {
                dbHandler.loadData();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            return dbHandler;
        } else {
            return dbHandler;
        }
    }

    public HashMap<Long, TextContent> getTextContent() {
        return textContent;
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
        //create files details table
        db.execSQL("CREATE TABLE " +
                markedFilesTable +
                " (" +
                colfileid +
                " INTEGER PRIMARY KEY AUTOINCREMENT , " +
                colfileAddress +
                " TEXT , " +
                colFilename +
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
            cv.put(DatabaseHandler.colIsUniqueTag, 0);
            db.insert(tagsTable, null, cv);
            cv.clear();
        }

        // create file and tag relationship table
        db.execSQL("CREATE TABLE " +
                fileTagTable +
                " (" +
                colFileTagid +
                " INTEGER PRIMARY KEY AUTOINCREMENT , " +
                colFtFileid +
                " INTEGER ," +
                colFtTagid +
                " INTEGER," +
                "FOREIGN KEY (" + colFtFileid + ") REFERENCES " + markedFilesTable + "(" + colfileid + ") ," +
                "FOREIGN KEY (" + colFtTagid + ") REFERENCES " + tagsTable + "(" + coltagid + ")" +
                ")"

        );

        // create filetypes table which maps file extension to ContentTypeEnum
        db.execSQL("CREATE TABLE " +
                fileTypesTable +
                " (" +
                colFileTypeid +
                " INTEGER PRIMARY KEY AUTOINCREMENT , " +
                colFileExtension +
                " TEXT , " +
                colFileCategory +
                " INTEGER " +
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
            cv.put(DatabaseHandler.colFileExtension, extension);
            cv.put(DatabaseHandler.colFileCategory, ContentTypeEnum.Image.ordinal());
            db.insert(fileTypesTable, null, cv);
            cv.clear();
        }
        for (String extension : video) {
            cv.put(DatabaseHandler.colFileExtension, extension);
            cv.put(DatabaseHandler.colFileCategory, ContentTypeEnum.Video.ordinal());
            db.insert(fileTypesTable, null, cv);
            cv.clear();
        }
        for (String extension : audio) {
            cv.put(DatabaseHandler.colFileExtension, extension);
            cv.put(DatabaseHandler.colFileCategory, ContentTypeEnum.Audio.ordinal());
            db.insert(fileTypesTable, null, cv);
            cv.clear();
        }
        for (String extension : document) {
            cv.put(DatabaseHandler.colFileExtension, extension);
            cv.put(DatabaseHandler.colFileCategory, ContentTypeEnum.Document.ordinal());
            db.insert(fileTypesTable, null, cv);
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
    public long addFile(String filename, String fileAddress, ContentTypeEnum contentType) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(DatabaseHandler.colfileAddress, fileAddress);
        cv.put(DatabaseHandler.colFilename, filename);
        cv.put(DatabaseHandler.colFileType, contentType.ordinal());

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
    public ArrayList<SmartContent> searchContentByString(String searchString) {

        SQLiteDatabase db = dbHandler.getReadableDatabase();

//        Cursor cur = db.rawQuery("SELECT " + markedFilesTable + "." + colfileid + " FROM " +
//                        markedFilesTable + ", " + tagsTable + ", " + fileTagTable +
//                        " WHERE " + markedFilesTable + "." + colfileid + "=" + fileTagTable + "." + colFtFileid +
//                        " AND " + tagsTable + "." + coltagid + "=" + fileTagTable + "." + colFtTagid +
//                        " AND (" +
//                        coltagName +
//                        " LIKE '%" +
//                        searchString +
//                        "%'" +
//                        " OR " +
//                        colFilename +
//                        " LIKE '%" +
//                        searchString +
//                        "%')",
//                null
//        );
//
//        Cursor cur = db.rawQuery("SELECT newTable." + colfileid + " FROM " +
//                        "(SELECT " + markedFilesTable + "." + colfileid + ", " + colFilename + ", " + colFtTagid + " FROM " +
//                        markedFilesTable + " LEFT OUTER JOIN " + fileTagTable + " ON "+
//                        markedFilesTable + "." + colfileid + " = " + fileTagTable + "." + colFtFileid + ") AS newTable, " +
//                        tagsTable +
//                        " ON " +
//                        "newTable." + colfileid + " = " + fileTagTable + "." + colFtFileid
//                        " WHERE " +
////                        tagsTable + "." + coltagid + " = newTable." + colFtTagid +
////                        " AND (" +
//                        coltagName +
//                        " LIKE '%" +
//                        searchString +
//                        "%'" +
//                        " OR " +
//                        colFilename +
//                        " LIKE '%" +
//                        searchString +
//                        "%'",
//                null
//        );

        Cursor cur = db.rawQuery("SELECT " + markedFilesTable + "." + colfileid + " FROM " +
                        markedFilesTable +
                        " LEFT OUTER JOIN " +
                        "(SELECT " + fileTagTable + "." + colFtTagid + ", " + coltagName + ", " + colFtFileid + " FROM " +
                        fileTagTable + ", " + tagsTable +
                        " WHERE " +
                        tagsTable + "." + coltagid + " = " + fileTagTable + "." + colFtTagid + ") AS newTable " +
                        " ON " +
                        "newTable." + colFtFileid + " = " + markedFilesTable + "." + colfileid +
                        " WHERE " +
                        coltagName +
                        " LIKE '%" +
                        searchString +
                        "%'" +
                        " OR " +
                        colFilename +
                        " LIKE '%" +
                        searchString +
                        "%'",
                null
        );

        ArrayList<SmartContent> result = new ArrayList<>();
        HashMap<Long, Boolean> temp = new HashMap<>();
        SmartContent sC;
        while (cur.moveToNext()) {
            sC = dbHandler.smartContentHash.get(cur.getLong(0));
            if (temp.get(sC.getContentID()) == null) {
                result.add(sC);
                temp.put(sC.getContentID(), true);
            }
        }

        return result;
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
     * method to load file and tags data from the
     * database and save them in appropriate data objects in memory
     */
    public void loadData() throws FileNotFoundException {
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
        cur = db.rawQuery("SELECT * FROM " + markedFilesTable, null);
        while (cur.moveToNext()) {
            SmartContent sC = new SmartContent(cur.getLong(0), cur.getString(1), cur.getString(2), cur.getInt(3));
            smartContentHash.put(cur.getLong(0), sC);
            smartContentData.add(sC);
            String contentText = new String();

            // if content is of type text then copy its text in inmemory textContent structure
            if (sC.getContentUnit().getContentType() == ContentTypeEnum.Note
                    || sC.getContentUnit().getContentType() == ContentTypeEnum.Location) {
                File file = new File(sC.getContentUnit().getAddress());
                Scanner scanner = new Scanner(new FileInputStream(file));
                try {
                    while (scanner.hasNextLine()) {
                        contentText = contentText.concat(scanner.nextLine() + "\n");
                    }
                } finally {
                    scanner.close();
                }
            }

            textContent.put(sC.getContentID(),new TextContent(sC.getContentID(), contentText));
        }

        if (cur != null)
            cur.close();

        // getting tag file association data
        cur = db.rawQuery("SELECT * FROM " + fileTagTable, null);
        while (cur.moveToNext()) {
            Long contentID = cur.getLong(1);
            Long tagID = cur.getLong(2);
            smartContentHash.get(contentID).addTag(tagHash.get(tagID));
            tagHash.get(tagID).addAssociatedContent(smartContentHash.get(contentID));
        }

        //getting file extension data
        cur = db.rawQuery("SELECT * FROM " + fileTypesTable, null);
        while (cur.moveToNext()) {
            String extension = cur.getString(1);
            int fileCategory = cur.getInt(2);

            fileExtensionHash.put(extension, ContentTypeEnum.enumFromInt(fileCategory));
        }
        if (cur != null)
            cur.close();
    }

    public Tag addTagInMemory(long tagId, String tagName, boolean isUnique) {
        Tag tag = new Tag(tagId, tagName, isUnique);
        tagData.add(tag);
        tagHash.put(tagId, tag);
        return tag;
    }

    public SmartContent addSmartContentInMemory(long id, String address, String name, ArrayList<Long> associatedTags, ContentTypeEnum contentType) {
        SmartContent sC = new SmartContent(id, address, name, contentType.ordinal());
        for (long tagId : associatedTags) {
            sC.addTag(tagHash.get(tagId));
        }
        smartContentData.add(sC);
        smartContentHash.put(id, sC);
        return sC;
    }


    // method to demonstrate tag and smartContent objects in memory
    public void populateDemoTV(TextView demoTV) {
        for (int i = 0; i < tagData.size(); i++) {
            demoTV.append("tag name: " + tagData.get(i).getTagName() + "\n"
                    + "tag id: " + tagData.get(i).getTagId() + "\n"
                    + "Is Unique: " + tagData.get(i).isUniqueContent() + "\n" +
                    "Associated files: "
            );
            for (SmartContent sC : tagData.get(i).getAssociatedContent()) {
                demoTV.append(sC.getContentFileName() + ", ");
            }
            demoTV.append("\n\n");
        }

        demoTV.append("-----#############-----\n\n");

        for (int i = 0; i < smartContentData.size(); i++) {
            demoTV.append("content id: " + smartContentData.get(i).getContentID() + "\n" +
                    "alternate content: " + smartContentData.get(i).getAlternateContent() + "\n" +
                    "comment: " + smartContentData.get(i).getComment() + "\n" +
                    "file name:" + smartContentData.get(i).getContentFileName() + "\n" +
                    "file address:" + smartContentData.get(i).getContentUnit().getAddress() + "\n" +
                    "content type: " + smartContentData.get(i).getContentUnit().getContentType() + "\n" +
                    "Associate Tags: "
            );
            ArrayList<Tag> associatedTags = smartContentData.get(i).getAssociatedTags();
            for (int j = 0; j < associatedTags.size(); j++) {
                demoTV.append(associatedTags.get(j).getTagName() + "  ");
            }
            demoTV.append("\n\n");
        }

        demoTV.append(fileExtensionHash.toString());
    }


    public void saveExtensionType(String extension, ContentTypeEnum contentType) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(DatabaseHandler.colFileExtension, extension);
        cv.put(DatabaseHandler.colFileCategory, contentType.ordinal());

        db.insert(fileTypesTable, null, cv);

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
        db.delete(fileTagTable, colFtFileid + "=? and " + colFtTagid + "=?", new String[]{"" + contentID, "" + tagId});
    }

    public void deleteSmartContent(SmartContent sC) {
        // delete in database
        for (Tag tag : sC.getAssociatedTags()) {
            deleteFileTagEntry(sC.getContentID(), tag.getTagId());
        }
        deleteFile(sC.getContentID());

        // delete in storage
        File file = new File(sC.getContentUnit().getAddress());
        if (file.exists()) {
            file.delete();
        }

        // delete in memory
        for (Tag tag : sC.getAssociatedTags()) {
            tag.getAssociatedContent().remove(sC);
        }
        smartContentHash.remove(sC.getContentID());
        smartContentData.remove(sC);

    }

    private void deleteFile(long contentID) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(markedFilesTable, "_id=?", new String[]{"" + contentID});
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
}
