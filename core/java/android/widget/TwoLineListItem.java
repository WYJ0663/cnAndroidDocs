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

import android.annotation.Widget;
import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.RelativeLayout;

/**
 * <p>这个布局是用在ListView中的，有两个子视图.
 * 该列表项包含两个 {@link android.widget.TextView TextViews} 元素（或其子类），
 * 其 ID 分别为 {@link android.R.id#text1 text1} 和 {@link android.R.id#text2 text2}；
 * 还有可选的第三个视图，其 ID 为 {@link android.R.id#selectedIcon selectedIcon}.
 * 第三个视图可以是视图类的任何子类（一般使用象 {@link android.widget.ImageView ImageView}
 * 这样的图像视图）.该视图在 TwoLineListItem 得到焦点时显示.
 * Android 为 TwoLineListView 提供了 {@link android.R.layout#two_line_list_item 标准布局资源}
 * 的支持（不包含选中时显示的图标），你也可以为该对象定制自己的 XML 布局.
 * 
 * @attr ref android.R.styleable#TwoLineListItem_mode
 * 
 * @deprecated This class can be implemented easily by apps using a {@link RelativeLayout}
 * or a {@link LinearLayout}.
 * @author translate by loveshirui（Android中文翻译组）
 * @author translate by cnmahj
 * @author convert by cnmahj
 */
@Deprecated
@Widget
public class TwoLineListItem extends RelativeLayout {

    private TextView mText1;
    private TextView mText2;

    public TwoLineListItem(Context context) {
        this(context, null, 0);
    }

    public TwoLineListItem(Context context, AttributeSet attrs) {
        this(context, attrs, 0); 
    }

    public TwoLineListItem(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        TypedArray a = context.obtainStyledAttributes(attrs,
                com.android.internal.R.styleable.TwoLineListItem, defStyle, 0);

        a.recycle();
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        
        mText1 = (TextView) findViewById(com.android.internal.R.id.text1);
        mText2 = (TextView) findViewById(com.android.internal.R.id.text2);
    }
    
    /**
     * 返回 ID 为 text1 的对象.
     * @return ID 为 text1 的对象.
     */
    public TextView getText1() {
        return mText1;
    }
    
    /**
     * 返回 ID 为 text2 的对象
     * @return ID 为 text2 的对象.
     */
    public TextView getText2() {
        return mText2;
    }

    @Override
    public void onInitializeAccessibilityEvent(AccessibilityEvent event) {
        super.onInitializeAccessibilityEvent(event);
        event.setClassName(TwoLineListItem.class.getName());
    }

    @Override
    public void onInitializeAccessibilityNodeInfo(AccessibilityNodeInfo info) {
        super.onInitializeAccessibilityNodeInfo(info);
        info.setClassName(TwoLineListItem.class.getName());
    }
}
