/*
 * Copyright (C) 2007 The Android Open Source Project
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
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.RemoteViews.RemoteView;

import java.util.Map;

/**
 * <p>
 * Displays a button with an image (instead of text) that can be pressed 
 * or clicked by the user. 默认情况下，ImageButton 看起来像一个普通的
 * {@link android.widget.Button 按钮}，拥有标准的背景色，并在不同状态时变更颜色.
 * 按钮上的图片可用通过 XML 布局文件的 {@code &lt;ImageButton&gt;} XML 元素的
 * {@code android:src} 属性或这代码中的 {@link #setImageResource(int)} 方法指定.</p>
 * 
 * <p>要移除标准按钮背景图像，可以定义自己的背景图片或设置背景为透明.</p>
 * <p>为了表示不同的按钮状态（得到焦点，被选中等），你可以为每种状态定义不同的图片.
 * 例如，默认状态为蓝色图片、获得焦点时显示橙色图片、按下时显示黄色图片.
 * 使用 XML 布局文件的可绘制对象“selector”可以简单的实现该功能.例如：</p>
 * <pre>
 * &lt;?xml version="1.0" encoding="utf-8"?&gt;
 * &lt;selector xmlns:android="http://schemas.android.com/apk/res/android"&gt;
 *     &lt;item android:state_pressed="true"
 *           android:drawable="@drawable/button_pressed" /&gt; &lt;!-- pressed --&gt;
 *     &lt;item android:state_focused="true"
 *           android:drawable="@drawable/button_focused" /&gt; &lt;!-- focused --&gt;
 *     &lt;item android:drawable="@drawable/button_normal" /&gt; &lt;!-- default --&gt;
 * &lt;/selector&gt;</pre>
 *
 * <p>保存上面的 XML 到 {@code res/drawable/} 文件夹下.将其作为你的
 * ImageButton 的可绘制对象的源（使用 {@code android:src} 属性）.
 * Android 会自动根据按钮的状态，显示 XML 文件中定义的相应图片.</p>
 *
 * <p>{@code &lt;item&gt;} 元素的顺序很重要，因为是按顺序判断当前状态要显示的图片.
 * 将为默认状态指定的图片放在最后，是因为它只会在 {@code android:state_pressed}
 * 和 {@code android:state_focused} 都为否时才能显示.</p>
 *
 * <p>See the <a href="{@docRoot}guide/topics/ui/controls/button.html">Buttons</a>
 * guide.</p>
 *
 * <p><strong>XML 属性</strong></p>
 * <p>
 * 参见 {@link android.R.styleable#ImageView Button 属性} 和
 * {@link android.R.styleable#View View 属性}.
 * </p>
 * @author translate by 农民伯伯
 * @author review by cnmahj
 * @author convert by cnmahj
 */
@RemoteView
public class ImageButton extends ImageView {
    public ImageButton(Context context) {
        this(context, null);
    }

    public ImageButton(Context context, AttributeSet attrs) {
        this(context, attrs, com.android.internal.R.attr.imageButtonStyle);
    }

    public ImageButton(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        setFocusable(true);
    }

    @Override
    protected boolean onSetAlpha(int alpha) {
        return false;
    }

    @Override
    public void onInitializeAccessibilityEvent(AccessibilityEvent event) {
        super.onInitializeAccessibilityEvent(event);
        event.setClassName(ImageButton.class.getName());
    }

    @Override
    public void onInitializeAccessibilityNodeInfo(AccessibilityNodeInfo info) {
        super.onInitializeAccessibilityNodeInfo(info);
        info.setClassName(ImageButton.class.getName());
    }
}
