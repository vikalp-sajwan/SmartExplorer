package com.example.vikalpsajwan.smartexplorer.UX;

import android.content.Context;
import android.graphics.Color;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.util.AttributeSet;

/**
 * Created by Vikalp on 10/05/2017.
 */

public class SpaceMultiAutoCompleteTextView extends android.support.v7.widget.AppCompatMultiAutoCompleteTextView {


    public SpaceMultiAutoCompleteTextView(Context context) {
        super(context);
        setTokenizer(new SpaceTokenizer());
        setPadding(5, 10, 5, 10);

    }

    public SpaceMultiAutoCompleteTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setTokenizer(new SpaceTokenizer());
        setBackgroundColor(Color.WHITE);
        setPadding(5, 10, 5, 10);
    }

    public SpaceMultiAutoCompleteTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setTokenizer(new SpaceTokenizer());
        setBackgroundColor(Color.WHITE);
        setPadding(5, 10, 5, 10);
    }


}


class SpaceTokenizer extends android.support.v7.widget.AppCompatMultiAutoCompleteTextView.CommaTokenizer {
    /**
     * Returns the start of the token that ends at offset
     * <code>cursor</code> within <code>text</code>.
     *
     * @param text
     * @param cursor
     */
    @Override
    public int findTokenStart(CharSequence text, int cursor) {
        int i = cursor;
        while (i > 0 && !Character.isWhitespace(text.charAt(i - 1))) {
            i--;
        }
        while (i < cursor && Character.isWhitespace(text.charAt(i))) {
            i++;
        }
        return i;
    }

    /**
     * Returns the end of the token (minus trailing punctuation)
     * that begins at offset <code>cursor</code> within <code>text</code>.
     *
     * @param text
     * @param cursor
     */
    @Override
    public int findTokenEnd(CharSequence text, int cursor) {
        int i = cursor;
        int len = text.length();

        while (i < len) {
            if (Character.isWhitespace(text.charAt(i))) {
                return i;
            }
            i++;
        }

        return len;
    }

    /**
     * Returns <code>text</code>, modified, if necessary, to ensure that
     * it ends with a token terminator (for example a space or comma).
     *
     * @param text
     */
    @Override
    public CharSequence terminateToken(CharSequence text) {
        int i = text.length();

        while (i > 0 && Character.isWhitespace(text.charAt(i - 1))) {
            i--;
        }

        if (i > 0 && Character.isWhitespace(text.charAt(i - 1))) {
            return text;
        } else {
            if (text instanceof Spanned) {
                SpannableString sp = new SpannableString(text + " ");
                TextUtils.copySpansFrom((Spanned) text, 0, text.length(),
                        Object.class, sp, 0);
                return sp;
            } else {
                return text + " ";
            }
        }
    }
}

