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
import android.util.AttributeSet;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;

import com.android.internal.R;


/**
 * <p>单选按钮是有选中和未选中两种状态的按钮.当其未选中时，用户可以通过按下或点击来选中它.
 * 然而，与 {@link android.widget.CheckBox} 不同，用户一旦选中了单选按钮就不能够取消选中.</p>
 *
 * <p>单选按钮通常与 {@link android.widget.RadioGroup} 同时使用.
 * 当多个单选按钮属于同一个单选按钮组时，选中其中一个的同时将取消其它单选按钮的选中状态.</p>
 *
 * <p>See the <a href="{@docRoot}guide/topics/ui/controls/radiobutton.html">Radio Buttons</a>
 * guide.</p>
 *
 * <p><strong>XML attributes</strong></p>
 * <p> 
 * 参见 {@link android.R.styleable#CompoundButton CompoundButton Attributes}、
 * {@link android.R.styleable#Button Button Attributes}、
 * {@link android.R.styleable#TextView TextView Attributes}、
 * {@link android.R.styleable#View View Attributes}.
 * </p>
 * @author translate by 农民伯伯
 * @author convert by cnmahj
 */
public class RadioButton extends CompoundButton {
    
    public RadioButton(Context context) {
        this(context, null);
    }
    
    public RadioButton(Context context, AttributeSet attrs) {
        this(context, attrs, com.android.internal.R.attr.radioButtonStyle);
    }

    public RadioButton(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    /**
     * {@inheritDoc}
     * <p>如果单选按钮已经选中，这个方法将不改变选中状态.</p>
     */
    @Override
    public void toggle() {
        // we override to prevent toggle when the radio is already
        // checked (as opposed to check boxes widgets)
        if (!isChecked()) {
            super.toggle();
        }
    }

    @Override
    public void onInitializeAccessibilityEvent(AccessibilityEvent event) {
        super.onInitializeAccessibilityEvent(event);
        event.setClassName(RadioButton.class.getName());
    }

    @Override
    public void onInitializeAccessibilityNodeInfo(AccessibilityNodeInfo info) {
        super.onInitializeAccessibilityNodeInfo(info);
        info.setClassName(RadioButton.class.getName());
    }
}
