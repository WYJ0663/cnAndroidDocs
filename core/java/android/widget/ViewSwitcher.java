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
 * {@link ViewAnimator} 在两个视图间切换，并包含创建这些视图的工厂类.
 * 你可以用工厂类来创建这些视图，也可以自己添加视图.
 * ViewSwitcher 只允许包含两个子视图，且一次仅能显示其中一个.
 * @author translate by ivanlee
 * @author convert by cnmahj
 */
public class ViewSwitcher extends ViewAnimator {
    /**
     * The factory used to create the two children.
     */
    ViewFactory mFactory;

    /**
     * 创建一个空的视图切换器(ViewSwitcher).
     *
     * @param context 应用程序上下文.
     */
    public ViewSwitcher(Context context) {
        super(context);
    }

    /**
     * 使用指定的上下文和属性集合创建一个空的视图切换器(ViewSwitcher).
     *
     * @param context 应用程序上下文.
     * @param attrs 属性集合.
     */
    public ViewSwitcher(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    /**
     * {@inheritDoc}
     *
     * @throws IllegalStateException 当切换器中已经包含两个视图时.
     */
    @Override
    public void addView(View child, int index, ViewGroup.LayoutParams params) {
        if (getChildCount() >= 2) {
            throw new IllegalStateException("Can't add more than 2 views to a ViewSwitcher");
        }
        super.addView(child, index, params);
    }

    @Override
    public void onInitializeAccessibilityEvent(AccessibilityEvent event) {
        super.onInitializeAccessibilityEvent(event);
        event.setClassName(ViewSwitcher.class.getName());
    }

    @Override
    public void onInitializeAccessibilityNodeInfo(AccessibilityNodeInfo info) {
        super.onInitializeAccessibilityNodeInfo(info);
        info.setClassName(ViewSwitcher.class.getName());
    }

    /**
     * 返回下一个要显示的视图.
     *
     * @return 视图切换之后将要显示出的下一个视图.
     */
    public View getNextView() {
        int which = mWhichChild == 0 ? 1 : 0;
        return getChildAt(which);
    }

    private View obtainView() {
        View child = mFactory.makeView();
        LayoutParams lp = (LayoutParams) child.getLayoutParams();
        if (lp == null) {
            lp = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        }
        addView(child, lp);
        return child;
    }

    /**
     * 设置用来生成将在视图切换器中切换的两个视图的工厂类对象.也可以调用两次 
     * {@link #addView(android.view.View, int, android.view.ViewGroup.LayoutParams)}
     * 来代替工厂类对象.
     *
     * @param factory 用来生成切换器内容的视图工厂.
     */
    public void setFactory(ViewFactory factory) {
        mFactory = factory;
        obtainView();
        obtainView();
    }

    /**
     * 重置视图切换器(ViewSwitcher）来隐藏所有存在的视图，
     * 并使切换器达到一次动画都还没有播放的状态.
     */
    public void reset() {
        mFirstTime = true;
        View v;
        v = getChildAt(0);
        if (v != null) {
            v.setVisibility(View.GONE);
        }
        v = getChildAt(1);
        if (v != null) {
            v.setVisibility(View.GONE);
        }
    }

    /**
     * 用于在视图切换器(ViewSwitcher)中创建视图.
     */
    public interface ViewFactory {
        /**
         * 创建用于 {@link android.widget.ViewSwitcher} 的新{@link android.view.View 视图}
         *
         * @return 新{@link android.view.View 视图}
         */
        View makeView();
    }
}

