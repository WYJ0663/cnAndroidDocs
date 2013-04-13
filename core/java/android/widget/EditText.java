/*
 * Copyright (C) 2006 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package android.widget;

import android.content.Context;
import android.text.Editable;
import android.text.Selection;
import android.text.Spannable;
import android.text.TextUtils;
import android.text.method.ArrowKeyMovementMethod;
import android.text.method.MovementMethod;
import android.util.AttributeSet;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;


/*
 * This is supposed to be a *very* thin veneer over TextView.
 * Do not make any changes here that do anything that a TextView
 * with a key listener and a movement method wouldn't do!
 */

/**
 * EditText 只是对 TextView 进行了少量变更，以使其可以编辑.
 *
 * <p>参见 <a href="{@docRoot}guide/topics/ui/controls/text.html">Text Fields</a>
 * guide.</p>
 * <p>
 * <b>XML 属性</b>
 * <p>
 * 参见 {@link android.R.styleable#EditText EditText 属性},
 * {@link android.R.styleable#TextView TextView 属性},
 * {@link android.R.styleable#View View 属性}
 */
public class EditText extends TextView {
    public EditText(Context context) {
        this(context, null);
    }

    public EditText(Context context, AttributeSet attrs) {
        this(context, attrs, com.android.internal.R.attr.editTextStyle);
    }

    public EditText(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    protected boolean getDefaultEditable() {
        return true;
    }

    @Override
    protected MovementMethod getDefaultMovementMethod() {
        return ArrowKeyMovementMethod.getInstance();
    }

    @Override
    public Editable getText() {
        return (Editable) super.getText();
    }

    @Override
    public void setText(CharSequence text, BufferType type) {
        super.setText(text, BufferType.EDITABLE);
    }

    /**
     * {@link Selection#setSelection(Spannable, int, int)} 的封装简化版.
     */
    public void setSelection(int start, int stop) {
        Selection.setSelection(getText(), start, stop);
    }

    /**
     * {@link Selection#setSelection(Spannable, int)} 的封装简化版.
     */
    public void setSelection(int index) {
        Selection.setSelection(getText(), index);
    }

    /**
     * {@link Selection#selectAll} 的封装简化版.
     */
    public void selectAll() {
        Selection.selectAll(getText());
    }

    /**
     * {@link Selection#extendSelection} 的封装简化版.
     */
    public void extendSelection(int index) {
        Selection.extendSelection(getText(), index);
    }

    @Override
    public void setEllipsize(TextUtils.TruncateAt ellipsis) {
        if (ellipsis == TextUtils.TruncateAt.MARQUEE) {
            throw new IllegalArgumentException("EditText cannot use the ellipsize mode "
                    + "TextUtils.TruncateAt.MARQUEE");
        }
        super.setEllipsize(ellipsis);
    }

    @Override
    public void onInitializeAccessibilityEvent(AccessibilityEvent event) {
        super.onInitializeAccessibilityEvent(event);
        event.setClassName(EditText.class.getName());
    }

    @Override
    public void onInitializeAccessibilityNodeInfo(AccessibilityNodeInfo info) {
        super.onInitializeAccessibilityNodeInfo(info);
        info.setClassName(EditText.class.getName());
    }
}
