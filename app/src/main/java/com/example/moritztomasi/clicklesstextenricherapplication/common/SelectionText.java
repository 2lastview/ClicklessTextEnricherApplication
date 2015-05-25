package com.example.moritztomasi.clicklesstextenricherapplication.common;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.EditText;

/**
 *
 */
public class SelectionText extends EditText {

    public SelectionText(Context context) {
        super(context);
    }

    public SelectionText(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public SelectionText(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    /**
     *
     * @param selStart
     * @param selEnd
     */
    @Override
    protected void onSelectionChanged(int selStart, int selEnd) {
        super.onSelectionChanged(selStart, selEnd);
        SelectionListener listener = (SelectionListener) getContext();
        listener.onSelected(selStart, selEnd);
    }

    /**
     *
     */
    public interface SelectionListener {

        /**
         *
         * @param selStart
         * @param selEnd
         */
        public void onSelected(int selStart, int selEnd);
    }
}
