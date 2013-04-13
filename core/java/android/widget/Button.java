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
import android.widget.RemoteViews.RemoteView;


/**
 * <p>
 * <code>Button</code> 代表按钮小部件.用户通过按下按钮，或者点击按钮来执行一个动作.
 * 以下是一个按钮在活动中典型的应用：
 * </p>
 *
 * <pre>
 * public class MyActivity extends Activity {
 *     protected void onCreate(Bundle icicle) {
 *         super.onCreate(icicle);
 *
 *         setContentView(R.layout.content_layout_id);
 *
 *         final Button button = (Button) findViewById(R.id.button_id);
 *         button.setOnClickListener(new View.OnClickListener() {
 *             public void onClick(View v) {
 *                 // 按钮单击时执行的动作
 *             }
 *         });
 *     }
 * }</pre>
 *
 * <p>另外，你可以在 XML 布局文件中，用 {@link android.R.attr#onClick android:onClick}
 * 属性为按钮指定方法名，以代替在活动中使用的
 * {@link android.view.View.OnClickListener OnClickListener}.例如：</p>
 *
 * <pre>
 * &lt;Button
 *     android:layout_height="wrap_content"
 *     android:layout_width="wrap_content"
 *     android:text="@string/self_destruct"
 *     android:onClick="selfDestruct" /&gt;</pre>
 *
 * <p>现在，当用户按下按钮，Android 系统会调用活动的 {@code selfDestruct(View)} 方法.
 * 为了完成该调用，指定的方法必须是公有并且只有一个 {@link android.view.View} 
 * 类型的参数.例如：</p>
 *
 * <pre>
 * public void selfDestruct(View view) {
 *     // 按钮单击时执行的动作
 * }</pre>
 *
 * <p>传入方法的参数{@link android.view.View}是发生单击事件的小部件的引用.</p>
 *
 * <h3>按钮样式</h3>
 *
 * <p>每个按钮的样式默认使用系统的按钮背景，不同的设备、不同的平台版本有不同按钮风格.
 * 如你不满意默认的按钮样式，想对其定制以符合您应用程序的设计，那么你可以使用
 * <a href="{@docRoot}guide/topics/resources/drawable-resource.html#StateList">
 * 状态列表可绘制对象</a>来更改按钮的背景图片.状态列表可绘制对象是在 XML 中定义的，
 * 可以根据当前按钮的状态改变其图片的可绘制对象资源.在 XML 中定义了状态列表可绘制对象后，
 * 你可以使用 {@link android.R.attr#background android:background} 属性将其应用于你的按钮.
 * 欲了解更多信息和示例，参见
 * <a href="{@docRoot}guide/topics/resources/drawable-resource.html#StateList">
 * 状态列表可绘制对象</a>.</p>
 *
 * <p>也可以参考 <a href="{@docRoot}guide/topics/ui/controls/button.html">Buttons</a>
 * guide.</p>
 *
 * <p><strong>XML 属性</strong></p>
 * <p> 
 * 参见 {@link android.R.styleable#Button Button 属性}、
 * {@link android.R.styleable#TextView TextView 属性} 和
 * {@link android.R.styleable#View View 属性}.
 * </p>
 */
@RemoteView
public class Button extends TextView {
    public Button(Context context) {
        this(context, null);
    }

    public Button(Context context, AttributeSet attrs) {
        this(context, attrs, com.android.internal.R.attr.buttonStyle);
    }

    public Button(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    public void onInitializeAccessibilityEvent(AccessibilityEvent event) {
        super.onInitializeAccessibilityEvent(event);
        event.setClassName(Button.class.getName());
    }

    @Override
    public void onInitializeAccessibilityNodeInfo(AccessibilityNodeInfo info) {
        super.onInitializeAccessibilityNodeInfo(info);
        info.setClassName(Button.class.getName());
    }
}
