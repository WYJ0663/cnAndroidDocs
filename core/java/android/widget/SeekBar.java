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
 * SeekBar 是 ProgressBar 的扩展，在其基础上增加了一个可滑动的滑片(注：就是那个可拖动的图标).
 * 用户可以触摸滑片并向左或向右拖动，再或者可以使用方向键都可以设置当前的进度等级.
 * 不建议把可以获取焦点的小部件放在 SeekBar 的左边或右边.
 * <p>
 * SeekBar 可以附加一个 {@link SeekBar.OnSeekBarChangeListener} 以获得用户操作的通知.
 *
 * @attr ref android.R.styleable#SeekBar_thumb
 * @author translate by madgoat（Android中文翻译组）
 * @author convert by cnmahj
 */
public class SeekBar extends AbsSeekBar {

    /**
     * 当进度改变后用于通知客户端的回调函数.
     * 这包括用户通过手势、方向键或轨迹球触发的改变， 以及编程触发的改变.
     */
    public interface OnSeekBarChangeListener {
        
        /**
         * 通知进度已经被修改.客户端可以使用 fromUser 参数区分用户触发的改变还是编程触发的改变.
         * 
         * @param seekBar 当前被修改进度的 SeekBar.
         * @param progress 	当前的进度值.此值的取值范围为 0 到 max 之间.
         * Max为用户通过 {@link ProgressBar#setMax(int)} 设置的值，默认为100.
         * @param fromUser 如果是用户触发的改变则返回 True.
         */
        void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser);
    
        /**
         * 通知用户已经开始一个触摸拖动手势.客户端可能需要使用这个来禁用 seekbar 的滑动功能.
         * @param seekBar 触摸手势开始的 SeekBar.
         */
        void onStartTrackingTouch(SeekBar seekBar);
        
        /**
         * 通知用户触摸手势已经结束.户端可能需要使用这个来启用 seekbar 的滑动功能.
         * @param seekBar 触摸手势开始了的 SeekBar.
         */
        void onStopTrackingTouch(SeekBar seekBar);
    }

    private OnSeekBarChangeListener mOnSeekBarChangeListener;
    
    public SeekBar(Context context) {
        this(context, null);
    }
    
    public SeekBar(Context context, AttributeSet attrs) {
        this(context, attrs, com.android.internal.R.attr.seekBarStyle);
    }

    public SeekBar(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    void onProgressRefresh(float scale, boolean fromUser) {
        super.onProgressRefresh(scale, fromUser);

        if (mOnSeekBarChangeListener != null) {
            mOnSeekBarChangeListener.onProgressChanged(this, getProgress(), fromUser);
        }
    }

    /**
     * 设置一个监听器以接受 seekbar 进度改变时的通知.
     * 同时提供用户在 SeekBar 上开始和停止触摸手势时的通知.
     * 
     * @param l SeekBar 的通知监听者
     * 
     * @see SeekBar.OnSeekBarChangeListener
     */
    public void setOnSeekBarChangeListener(OnSeekBarChangeListener l) {
        mOnSeekBarChangeListener = l;
    }
    
    @Override
    void onStartTrackingTouch() {
        super.onStartTrackingTouch();
        if (mOnSeekBarChangeListener != null) {
            mOnSeekBarChangeListener.onStartTrackingTouch(this);
        }
    }
    
    @Override
    void onStopTrackingTouch() {
        super.onStopTrackingTouch();
        if (mOnSeekBarChangeListener != null) {
            mOnSeekBarChangeListener.onStopTrackingTouch(this);
        }
    }

    @Override
    public void onInitializeAccessibilityEvent(AccessibilityEvent event) {
        super.onInitializeAccessibilityEvent(event);
        event.setClassName(SeekBar.class.getName());
    }

    @Override
    public void onInitializeAccessibilityNodeInfo(AccessibilityNodeInfo info) {
        super.onInitializeAccessibilityNodeInfo(info);
        info.setClassName(SeekBar.class.getName());
    }
}
