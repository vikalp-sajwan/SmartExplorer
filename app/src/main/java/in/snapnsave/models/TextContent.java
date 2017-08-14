package in.snapnsave.models;

/**
 * Created by Vikalp on 09/04/2017.
 */

public class TextContent {
    private long contentID;

    public String[] getNonStopWords() {
        return nonStopWords;
    }

    private String[] nonStopWords;

    /**
     * Returns a string containing a concise, human-readable description of this
     * object. Subclasses are encouraged to override this method and provide an
     * implementation that takes into account the object's type and data. The
     * default implementation is equivalent to the following expression:
     * <pre>
     *   getClass().getName() + '@' + Integer.toHexString(hashCode())</pre>
     * <p>See <a href="{@docRoot}reference/java/lang/Object.html#writing_toString">Writing a useful
     * {@code toString} method</a>
     * if you intend implementing your own {@code toString} method.
     *
     * @return a printable representation of this object.
     */
    @Override
    public String toString() {
        String returnValue = new String();
        returnValue = returnValue.concat("content ID: "+ contentID +"\n");
        returnValue = returnValue.concat("content text: "+ contentText +"\n");
        returnValue = returnValue.concat("non stop-words : ");
        for (String word: nonStopWords) {
            returnValue= returnValue.concat(word + ",  ");
        }
        returnValue = returnValue.concat("\n\n");


        return returnValue;
    }

    public String getContentText() {
        return contentText;
    }

    private String contentText;

    public TextContent(long contentID, String contentText) {
        this.contentID = contentID;
        this.contentText = contentText;
        nonStopWords = contentText.split("\\s+");

    }

    public boolean isWordPresentInContent(String word) {
        for (int i = 0; i < nonStopWords.length; i++) {
            if (nonStopWords[i].equalsIgnoreCase(word))
                return true;
        }
        return false;
    }
}