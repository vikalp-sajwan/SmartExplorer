package com.example.vikalpsajwan.smartexplorer.UX;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;

import java.util.ArrayList;

/**
 *  Created by Vikalp on 06/05/2017.
 *  A background Async task that performs the search operation and shows the progress dialog while performing operation.
 *  doInBackground method takes searchString as parameter and returns the list of SmartContent IDs which were found as match.
 */



public class SearchUtility extends AsyncTask<String, Void , Long[]> {

    Context context;
    private ProgressDialog waitingDialog;

    public SearchUtility(Context context){
        this.context = context;
        waitingDialog = new ProgressDialog(context);
    }

    /**
     * Runs on the UI thread before {@link #doInBackground}.
     *
     * @see #onPostExecute
     * @see #doInBackground
     */
    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        waitingDialog.setMessage("Searching...");
        waitingDialog.show();
    }

    /**
     * <p>Runs on the UI thread after {@link #doInBackground}. The
     * specified result is the value returned by {@link #doInBackground}.</p>
     * <p>
     * <p>This method won't be invoked if the task was cancelled.</p>
     *
     * @param longs The result of the operation computed by {@link #doInBackground}.
     * @see #onPreExecute
     * @see #doInBackground
     * @see #onCancelled(Object)
     */
    @Override
    protected void onPostExecute(Long[] longs) {
        super.onPostExecute(longs);
        if(waitingDialog.isShowing()){
            waitingDialog.dismiss();
        }
    }

    /**
     * Override this method to perform a computation on a background thread. The
     * specified parameters are the parameters passed to {@link #execute}
     * by the caller of this task.
     * <p>
     * This method can call {@link #publishProgress} to publish updates
     * on the UI thread.
     *
     * @param searchString The parameters of the task.
     * @return A result, defined by the subclass of this task.
     * @see #onPreExecute()
     * @see #onPostExecute
     * @see #publishProgress
     */
    @Override
    protected Long[] doInBackground(String... searchString) {
        ArrayList<Long> searchResult = null;

        for(String string: searchString){
            String words[] = string.split(" ");

        }
        return null;

    }
}
