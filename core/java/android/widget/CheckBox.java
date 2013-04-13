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


/**
 * <p>
 * 复选框是包含选中和未选中两种状态的特殊的双状态按钮.
 * 如下是在活动中使用复选框的例子：
 * </p>
 *
 * <pre class="prettyprint">
 * public class MyActivity extends Activity {
 *     protected void onCreate(Bundle icicle) {
 *         super.onCreate(icicle);
 *
 *         setContentView(R.layout.content_layout_id);
 *
 *         final CheckBox checkBox = (CheckBox) findViewById(R.id.checkbox_id);
 *         if (checkBox.isChecked()) {
 *             checkBox.setChecked(false);
 *         }
 *     }
 * }
 * </pre>
 *
 * <p>参见 <a href="{@docRoot}guide/topics/ui/controls/checkbox.html">Checkboxes</a>
 * guide.</p>
 *  
 * <p><strong>XML 属性</strong></p> 
 * <p>
 * 参见 {@link android.R.styleable#CompoundButton CompoundButton 属性}、
 * {@link android.R.styleable#Button Button 属性}、
 * {@link android.R.styleable#TextView TextView 属性} 和
 * {@link android.R.styleable#View View Attributes}.
 * </p>
 * @author translate by 农民伯伯
 * @author convert by cnmahj
 */
public class CheckBox extends CompoundButton {
    public CheckBox(Context context) {
        this(context, null);
    }
    
    public CheckBox(Context context, AttributeSet attrs) {
        this(context, attrs, com.android.internal.R.attr.checkboxStyle);
    }

    public CheckBox(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    public void onInitializeAccessibilityEvent(AccessibilityEvent event) {
        super.onInitializeAccessibilityEvent(event);
        event.setClassName(CheckBox.class.getName());
    }

    @Override
    public void onInitializeAccessibilityNodeInfo(AccessibilityNodeInfo info) {
        super.onInitializeAccessibilityNodeInfo(info);
        info.setClassName(CheckBox.class.getName());
    }
}
