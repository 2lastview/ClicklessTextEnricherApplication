/**
 * Copyright 2015 Moritz Tomasi
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
*/

package com.example.moritztomasi.clicklesstextenricherapplication.common;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.EditText;

/**
 * This is a custom extension of {@link EditText}. This extension is needed because every time
 * text is selected in a SelectionText, the {@link SelectionListener}'s method {@link SelectionListener#onSelected(int, int)}
 * should be triggered in the containing and implementing Activity.
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
     * This method gets called every time the selection of the SelectionText has been changed.
     *
     * @param selStart Start index of the selection
     * @param selEnd End index of the selection
     */
    @Override
    protected void onSelectionChanged(int selStart, int selEnd) {
        super.onSelectionChanged(selStart, selEnd);
        SelectionListener listener = (SelectionListener) getContext();
        listener.onSelected(selStart, selEnd);
    }

    public interface SelectionListener {

        public void onSelected(int selStart, int selEnd);
    }
}
