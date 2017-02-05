package com.example.vikalpsajwan.smartexplorer;

import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.TimeZone;


/**
 * @author Vikalp
 *         <p>
 *         SearchUtility class -- provides file searching functions in the internal storage of phone
 */

public class SearchUtility {

    private static SearchUtility searchUtilityObj;
    boolean mStorageAvailable = false;
    private File internalStorage;
    private ArrayList<File> searchResult;

    private SearchUtility() {
        searchResult = new ArrayList<File>();
        updateStorageState();
        if (mStorageAvailable) {
            internalStorage = Environment.getExternalStorageDirectory();
            // test log
            Log.v("SD card", internalStorage.toString());
        }
    }

    /**
     * singleton design method to get instance
     */
    public static SearchUtility getObject() {
        if (searchUtilityObj == null) {
            searchUtilityObj = new SearchUtility();
            return searchUtilityObj;
        } else {
            return searchUtilityObj;
        }
    }

    /**
     * checks the mount status of the storage and updates the mStorageAvailable flag
     */
    void updateStorageState() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            mStorageAvailable = true;
        } else {
            mStorageAvailable = false;
        }
    }

    /**
     * @param searchString - the string to match
     * @return search resultset in form of ArrayList
     */
    ArrayList<File> searchByName(String searchString) {
        if (mStorageAvailable) {
            searchByNameRecursive(internalStorage, searchString);
        }

        return searchResult;
    }

    /**
     * searches file based on Date of Modification
     *
     * @param day
     * @param month
     * @param year
     * @return search resultset in form of ArrayList
     */
    ArrayList<File> searchByDOM(int day, int month, int year) {
        if (mStorageAvailable) {
            searchByDOMRecursive(internalStorage, day, month, year);
        }
        return searchResult;
    }

    /**
     * searches file based on searchString and then filters those results on the basis of Date Of Modification
     *
     * @param searchString
     * @param day
     * @param month
     * @param year
     * @return search resultset in form of ArrayList
     */

    ArrayList<File> searchByNameDOM(String searchString, int day, int month, int year) {

        // for the secondary result filtered on the basis of Date Of Modification
        ArrayList<File> finalResult = new ArrayList<File>();
        if (mStorageAvailable) {
            searchByNameRecursive(internalStorage, searchString);
            for (File entry : searchResult) {

                GregorianCalendar DOM = new GregorianCalendar(TimeZone.getTimeZone("GMT"));
                DOM.setTimeInMillis(entry.lastModified());
                int mDay = DOM.get(GregorianCalendar.DAY_OF_MONTH);
                int mMonth = DOM.get(GregorianCalendar.MONTH);
                int mYear = DOM.get(GregorianCalendar.YEAR);

                if (mDay == day && mMonth == month && mYear == year) {
                    finalResult.add(entry);
                }

            }

        }
        return finalResult;
    }

    /**
     * searches the storage recursively for the matching filename files and stores the result in class ArrayList 'result'
     *
     * @param dir          - directory to search in
     * @param searchString - search string
     */
    private void searchByNameRecursive(File dir, String searchString) {
        File[] files = dir.listFiles();
        searchString = searchString.toLowerCase();
        for (int i = 0; i < files.length; i++) {
            String fileName = files[i].getName();
            // put in your filter here
            if (fileName.toLowerCase().contains(searchString)) {
                if (files[i].isFile()) {
                    searchResult.add(files[i]);
                    Log.i("added file", files[i].toString());

                }
            }
            if (files[i].isDirectory()) {
                searchByNameRecursive(files[i], searchString);
            }
        }
    }

    /**
     * searches the storage recursively for the matching Date Of Modification and stores the result in class ArrayList 'result'
     *
     * @param dir   - directory to search in
     * @param day
     * @param month
     * @param year
     */
    private void searchByDOMRecursive(File dir, int day, int month, int year) {
        File[] files = dir.listFiles();

        for (int i = 0; i < files.length; i++) {

            GregorianCalendar DOM = new GregorianCalendar(TimeZone.getTimeZone("GMT"));
            DOM.setTimeInMillis(files[i].lastModified());
            int mDay = DOM.get(GregorianCalendar.DAY_OF_MONTH);
            int mMonth = DOM.get(GregorianCalendar.MONTH);
            int mYear = DOM.get(GregorianCalendar.YEAR);

            if (mDay == day && mMonth == month && mYear == year && files[i].isFile()) {
                searchResult.add(files[i]);
            }
            if (files[i].isDirectory()) {
                searchByDOMRecursive(files[i], day, month, year);
            }
        }
    }
}