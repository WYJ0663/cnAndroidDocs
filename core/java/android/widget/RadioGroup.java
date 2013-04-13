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

import com.android.internal.R;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;


/**
 * <p>这个类用于创建一组单选按钮之间多重排斥的范围.
 * 在同一个单选按钮组中勾选一个按钮则会取消该组中其它已经勾选的按钮的选中状态.</p>
 *
 * <p>初始状态下，所有的单选按钮都出于未选中状态.虽然不能取消一个特定的单选按钮的选中状态，
 * 但可以通过单选按钮组来移除它的选中状态.</p>
 *
 * <p>选中的单选按钮是通过在 XML 布局文件中定义的唯一 ID 来识别的.</p>
 *
 * <p><strong>XML 属性</strong></p>
 * <p>参见 {@link android.R.styleable#RadioGroup RadioGroup 属性}、
 * {@link android.R.styleable#LinearLayout LinearLayout 属性}、
 * {@link android.R.styleable#ViewGroup ViewGroup 属性} 和
 * {@link android.R.styleable#View View 属性}.</p>
 * <p>更多的布局属性，参见
 * {@link android.widget.LinearLayout.LayoutParams LinearLayout.LayoutParams}.</p>
 * 
 * @see RadioButton
 * @author translate by 首当其冲
 * @author convert by cnmahj
 *
 */
public class RadioGroup extends LinearLayout {
    // holds the checked id; the selection is empty by default
    private int mCheckedId = -1;
    // tracks children radio buttons checked state
    private CompoundButton.OnCheckedChangeListener mChildOnCheckedChangeListener;
    // when true, mOnCheckedChangeListener discards events
    private boolean mProtectFromCheckedChange = false;
    private OnCheckedChangeListener mOnCheckedChangeListener;
    private PassThroughHierarchyChangeListener mPassThroughListener;

    /**
     * {@inheritDoc}
     */
    public RadioGroup(Context context) {
        super(context);
        setOrientation(VERTICAL);
        init();
    }

    /**
     * {@inheritDoc}
     */
    public RadioGroup(Context context, AttributeSet attrs) {
        super(context, attrs);

        // retrieve selected radio button as requested by the user in the
        // XML layout file
        TypedArray attributes = context.obtainStyledAttributes(
                attrs, com.android.internal.R.styleable.RadioGroup, com.android.internal.R.attr.radioButtonStyle, 0);

        int value = attributes.getResourceId(R.styleable.RadioGroup_checkedButton, View.NO_ID);
        if (value != View.NO_ID) {
            mCheckedId = value;
        }

        final int index = attributes.getInt(com.android.internal.R.styleable.RadioGroup_orientation, VERTICAL);
        setOrientation(index);

        attributes.recycle();
        init();
    }

    private void init() {
        mChildOnCheckedChangeListener = new CheckedStateTracker();
        mPassThroughListener = new PassThroughHierarchyChangeListener();
        super.setOnHierarchyChangeListener(mPassThroughListener);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setOnHierarchyChangeListener(OnHierarchyChangeListener listener) {
        // the user listener is delegated to our pass-through listener
        mPassThroughListener.mOnHierarchyChangeListener = listener;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();

        // checks the appropriate radio button as requested in the XML file
        if (mCheckedId != -1) {
            mProtectFromCheckedChange = true;
            setCheckedStateForView(mCheckedId, true);
            mProtectFromCheckedChange = false;
            setCheckedId(mCheckedId);
        }
    }

    @Override
    public void addView(View child, int index, ViewGroup.LayoutParams params) {
        if (child instanceof RadioButton) {
            final RadioButton button = (RadioButton) child;
            if (button.isChecked()) {
                mProtectFromCheckedChange = true;
                if (mCheckedId != -1) {
                    setCheckedStateForView(mCheckedId, false);
                }
                mProtectFromCheckedChange = false;
                setCheckedId(button.getId());
            }
        }

        super.addView(child, index, params);
    }

    /**
     * <p>将通过参数传入的 ID 所对应的单选按钮设置为选中状态.
     * 用 -1 作为选择标识符，将清除按钮组的选择状态，相当于执行 {@link #clearCheck()} 方法.</p>
     * @param id 该组中所要勾选的单选按钮的唯一标识符（id）.
     *
     * @see #getCheckedRadioButtonId()
     * @see #clearCheck()
     */
    public void check(int id) {
        // don't even bother
        if (id != -1 && (id == mCheckedId)) {
            return;
        }

        if (mCheckedId != -1) {
            setCheckedStateForView(mCheckedId, false);
        }

        if (id != -1) {
            setCheckedStateForView(id, true);
        }

        setCheckedId(id);
    }

    private void setCheckedId(int id) {
        mCheckedId = id;
        if (mOnCheckedChangeListener != null) {
            mOnCheckedChangeListener.onCheckedChanged(this, mCheckedId);
        }
    }

    private void setCheckedStateForView(int viewId, boolean checked) {
        View checkedView = findViewById(viewId);
        if (checkedView != null && checkedView instanceof RadioButton) {
            ((RadioButton) checkedView).setChecked(checked);
        }
    }

    /**
     * <p>返回该单选按钮组中所选择的单选按钮的标识 ID，如果没有选中的单选按钮则返回 -1.</p>
     *
     * @return 单选按钮组中选中的单选按钮的标识 ID.
     *
     * @see #check(int)
     * @see #clearCheck()
     *
     * @attr ref android.R.styleable#RadioGroup_checkedButton
     */
    public int getCheckedRadioButtonId() {
        return mCheckedId;
    }

    /**
     * <p>清除选择状态.清除选择状态后，该单选按钮组中没有选中的按钮，
     * 调用 {@link #getCheckedRadioButtonId()} 函数返回 -1（原文为null）.</p>
     *
     * @see #check(int)
     * @see #getCheckedRadioButtonId()
     */
    public void clearCheck() {
        check(-1);
    }

    /**
     * <p>注册一个当该单选按钮组中的单选按钮勾选状态发生改变时所要调用的回调函数.</p>
     *
     * @param listener 当单选按钮勾选状态发生改变时所要调用的回调函数.
     */
    public void setOnCheckedChangeListener(OnCheckedChangeListener listener) {
        mOnCheckedChangeListener = listener;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public LayoutParams generateLayoutParams(AttributeSet attrs) {
        return new RadioGroup.LayoutParams(getContext(), attrs);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected boolean checkLayoutParams(ViewGroup.LayoutParams p) {
        return p instanceof RadioGroup.LayoutParams;
    }

    @Override
    protected LinearLayout.LayoutParams generateDefaultLayoutParams() {
        return new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
    }

    @Override
    public void onInitializeAccessibilityEvent(AccessibilityEvent event) {
        super.onInitializeAccessibilityEvent(event);
        event.setClassName(RadioGroup.class.getName());
    }

    @Override
    public void onInitializeAccessibilityNodeInfo(AccessibilityNodeInfo info) {
        super.onInitializeAccessibilityNodeInfo(info);
        info.setClassName(RadioGroup.class.getName());
    }

    /**
     * <p>当 XML 文件中没有指定该视图的子控件的高度和宽度时，
     * 该类将其设为默认值 {@link #WRAP_CONTENT}；指定了则使用指定的值.</p>
     *
     * <p>本类支持的所有子视图属性的一览表，参见
     * {@link android.R.styleable#LinearLayout_Layout LinearLayout 属性}.</p>
     *
     */
    public static class LayoutParams extends LinearLayout.LayoutParams {
        /**
         * {@inheritDoc}
         */
        public LayoutParams(Context c, AttributeSet attrs) {
            super(c, attrs);
        }

        /**
         * {@inheritDoc}
         */
        public LayoutParams(int w, int h) {
            super(w, h);
        }

        /**
         * {@inheritDoc}
         */
        public LayoutParams(int w, int h, float initWeight) {
            super(w, h, initWeight);
        }

        /**
         * {@inheritDoc}
         */
        public LayoutParams(ViewGroup.LayoutParams p) {
            super(p);
        }

        /**
         * {@inheritDoc}
         */
        public LayoutParams(MarginLayoutParams source) {
            super(source);
        }

        /**
         * <p>当 XML 文件中未指定时，将子视图的宽度设为
         * {@link android.view.ViewGroup.LayoutParams#WRAP_CONTENT}、高度设为
         * {@link android.view.ViewGroup.LayoutParams#WRAP_CONTENT}.</p>
         *
         * @param a 命名的属性集合
         * @param widthAttr 从集合 a 中取得宽度属性用的索引
         * @param heightAttr 从集合 a 中取得高度属性用的索引
         */
        @Override
        protected void setBaseAttributes(TypedArray a,
                int widthAttr, int heightAttr) {

            if (a.hasValue(widthAttr)) {
                width = a.getLayoutDimension(widthAttr, "layout_width");
            } else {
                width = WRAP_CONTENT;
            }
            
            if (a.hasValue(heightAttr)) {
                height = a.getLayoutDimension(heightAttr, "layout_height");
            } else {
                height = WRAP_CONTENT;
            }
        }
    }

    /**
     * <p>当单选按钮组中的单选按钮的勾选状态发生改变时，所要调用的回调函数的接口类.</p>
     */
    public interface OnCheckedChangeListener {
        /**
         * <p>变更选中的单选按钮时，调用该方法.清除选择状态时，<code>checkedId</code> 为 -1.</p>
         *
         * @param group 选中状态发生变化的按钮组.
         * @param checkedId 新选中的单选按钮的标识符.
         */
        public void onCheckedChanged(RadioGroup group, int checkedId);
    }

    private class CheckedStateTracker implements CompoundButton.OnCheckedChangeListener {
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            // prevents from infinite recursion
            if (mProtectFromCheckedChange) {
                return;
            }

            mProtectFromCheckedChange = true;
            if (mCheckedId != -1) {
                setCheckedStateForView(mCheckedId, false);
            }
            mProtectFromCheckedChange = false;

            int id = buttonView.getId();
            setCheckedId(id);
        }
    }

    /**
     * <p>A pass-through listener acts upon the events and dispatches them
     * to another listener. This allows the table layout to set its own internal
     * hierarchy change listener without preventing the user to setup his.</p>
     */
    private class PassThroughHierarchyChangeListener implements
            ViewGroup.OnHierarchyChangeListener {
        private ViewGroup.OnHierarchyChangeListener mOnHierarchyChangeListener;

        /**
         * {@inheritDoc}
         */
        public void onChildViewAdded(View parent, View child) {
            if (parent == RadioGroup.this && child instanceof RadioButton) {
                int id = child.getId();
                // generates an id if it's missing
                if (id == View.NO_ID) {
                    id = View.generateViewId();
                    child.setId(id);
                }
                ((RadioButton) child).setOnCheckedChangeWidgetListener(
                        mChildOnCheckedChangeListener);
            }

            if (mOnHierarchyChangeListener != null) {
                mOnHierarchyChangeListener.onChildViewAdded(parent, child);
            }
        }

        /**
         * {@inheritDoc}
         */
        public void onChildViewRemoved(View parent, View child) {
            if (parent == RadioGroup.this && child instanceof RadioButton) {
                ((RadioButton) child).setOnCheckedChangeWidgetListener(null);
            }

            if (mOnHierarchyChangeListener != null) {
                mOnHierarchyChangeListener.onChildViewRemoved(parent, child);
            }
        }
    }
}
