package in.snapnsave.UX;

/**
 * Created by Vikalp on 02/06/2017.
 *
 * interface to employ listener(observer) pattern to notify the mainActivity whenever Database is updated
 * and LoadData() is called in DatabaseHandler object
 */

public interface DatabaseUpdatedResponse {
    /**
     * method to be called when loadData() is  called in DatabaseHandler object
     */
    void dataLoadFinish();
}
