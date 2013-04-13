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
import android.view.View;
import android.view.ViewGroup;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;

/**
 * 专用的 {@link android.widget.ViewSwitcher}，仅包含 {@link android.widget.TextView}
 * 类型的元素.
 *
 * TextSwitcher 用于使屏幕上的标签文本产生动画效果.
 * 当调用 {@link #setText(CharSequence)} 时，
 * TextSwitcher 使用动画形式隐藏当前的文本并显示新的文本.
 *
 * @author translate by madgoat（Android中文翻译组）
 * @author convert by cnmahj
 */
public class TextSwitcher extends ViewSwitcher {
    /**
     * 创建空的 TextSwitcher.
     *
     * @param context 应用程序上下文.
     */
    public TextSwitcher(Context context) {
        super(context);
    }

    /**
     * 通过给出的应用程序上下文和指定的属性集合来创建空的 TextSwitcher.
     *
     * @param context 应用程序上下文.
     * @param attrs 属性集合.
     */
    public TextSwitcher(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    /**
     * {@inheritDoc}
     *
     * @throws IllegalArgumentException 当子视图不是 {@link android.widget.TextView} 的实例时.
     */
    @Override
    public void addView(View child, int index, ViewGroup.LayoutParams params) {
        if (!(child instanceof TextView)) {
            throw new IllegalArgumentException(
                    "TextSwitcher children must be instances of TextView");
        }

        super.addView(child, index, params);
    }

    /**
     * 设置下一个视图的文本内容并切换到下一视图.
     * 可用于以动画形式隐藏当前的文本并显示新的文本.
     *
     * @param text 要显示的新的文本
     */
    public void setText(CharSequence text) {
        final TextView t = (TextView) getNextView();
        t.setText(text);
        showNext();
    }

    /**
     * 设置当前显示的文本视图的文字内容.该操作不会显示动画.
     *
     * @param text 要显示的新的文本
     */
    public void setCurrentText(CharSequence text) {
        ((TextView)getCurrentView()).setText(text);
    }

    @Override
    public void onInitializeAccessibilityEvent(AccessibilityEvent event) {
        super.onInitializeAccessibilityEvent(event);
        event.setClassName(TextSwitcher.class.getName());
    }

    @Override
    public void onInitializeAccessibilityNodeInfo(AccessibilityNodeInfo info) {
        super.onInitializeAccessibilityNodeInfo(info);
        info.setClassName(TextSwitcher.class.getName());
    }
}
