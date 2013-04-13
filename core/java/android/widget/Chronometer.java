/*
 * Copyright (C) 2008 The Android Open Source Project
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
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.text.format.DateUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.util.Slog;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.RemoteViews.RemoteView;

import java.util.Formatter;
import java.util.IllegalFormatException;
import java.util.Locale;

/**
 * 简单的计时器类.
 * <p>
 * 你可以给他设定基于 {@link SystemClock#elapsedRealtime} 的基准（开始）时间，用于计时.
 * 如果你没有设置该时间，该类将从你调用 {@link #start} 方法的时间开始计时.
 * 默认以“MM:SS”或“H:MM:SS”形式显示计时器的值，你可以使用 {@link #setFormat}
 * 方法使其显示任意字符串.
 * @attr ref android.R.styleable#Chronometer_format
 * @author translate by 德罗德
 * @author review by cnmahj
 * @author convert by cnmahj
 */
@RemoteView
public class Chronometer extends TextView {
    private static final String TAG = "Chronometer";

    /**
     * 定义计时器递增通知回调函数的监听器接口.
     */
    public interface OnChronometerTickListener {

        /**
         * 在计时器变化时的通知.
         */
        void onChronometerTick(Chronometer chronometer);

    }

    private long mBase;
    private boolean mVisible;
    private boolean mStarted;
    private boolean mRunning;
    private boolean mLogged;
    private String mFormat;
    private Formatter mFormatter;
    private Locale mFormatterLocale;
    private Object[] mFormatterArgs = new Object[1];
    private StringBuilder mFormatBuilder;
    private OnChronometerTickListener mOnChronometerTickListener;
    private StringBuilder mRecycle = new StringBuilder(8);
    
    private static final int TICK_WHAT = 2;
    
    /**
     * 初始化计时器对象.设置当前时间为基准（开始）时间.
     */
    public Chronometer(Context context) {
        this(context, null, 0);
    }

    /**
     * 使用标准视图布局信息初始化计时器.设置当前时间为基准（开始）时间.
     */
    public Chronometer(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    /**
     * 使用标准视图布局信息和风格初始化计时器.设置当前时间为基准（开始）时间.
     */
    public Chronometer(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        TypedArray a = context.obtainStyledAttributes(
                attrs,
                com.android.internal.R.styleable.Chronometer, defStyle, 0);
        setFormat(a.getString(com.android.internal.R.styleable.Chronometer_format));
        a.recycle();

        init();
    }

    private void init() {
        mBase = SystemClock.elapsedRealtime();
        updateText(mBase);
    }

    /**
     * 设置计时器计时的基准（开始）时间.
     *
     * @param base 基于 {@link SystemClock#elapsedRealtime} 的基准（开始）时间.
     */
    @android.view.RemotableViewMethod
    public void setBase(long base) {
        mBase = base;
        dispatchChronometerTick();
        updateText(SystemClock.elapsedRealtime());
    }

    /**
     * 返回通过 {@link #setBase} 设置的基准（开始）时间.
     */
    public long getBase() {
        return mBase;
    }

    /**
     * 设置用于格式化显示格式的字符串.计时器将用“MM:SS”或“H:MM:SS”
     * 形式的值替换格式化字符串中的第一个“%s”.
     * 如果格式化字符串为空，或者你从未调用过 setFormat() 方法，
     * 计时器将以“MM:SS”或“H:MM:SS”形式显示其值.
     * @param format 格式化字符串
     */
    @android.view.RemotableViewMethod
    public void setFormat(String format) {
        mFormat = format;
        if (format != null && mFormatBuilder == null) {
            mFormatBuilder = new StringBuilder(format.length() * 2);
        }
    }

    /**
     * 返回通过 {@link #setFormat} 设置的格式化字符串.
     */
    public String getFormat() {
        return mFormat;
    }

    /**
     * 设置计时器变化时调用的监听器.
     * 
     * @param listener 监听器.
     */
    public void setOnChronometerTickListener(OnChronometerTickListener listener) {
        mOnChronometerTickListener = listener;
    }

    /**
     * @return 监听计时器变化的监听器（可能为空）.
     */
    public OnChronometerTickListener getOnChronometerTickListener() {
        return mOnChronometerTickListener;
    }

    /**
     * 开始计时.该操作不会影响到由 {@link #setBase} 设置的基准（开始）时间，仅影响显示的视图.
     * 
     * 即使小部件不可见，计时器也会通过定期处理消息来工作.为了确保不发生资源泄漏，
     * 用户应确保针对每个 start() 方法调用，都调用了相应的 {@link #stop} 方法.
     */
    public void start() {
        mStarted = true;
        updateRunning();
    }

    /**
     * 停止计时.不会影响用 {@link #setBase} 方法设置的基准（开始）时间，只影响视图的显示.
     * 这将停止消息发送，有效地释放计时器通过 {@link #start} 运行时占用的资源.
     */
    public void stop() {
        mStarted = false;
        updateRunning();
    }

    /**
     * The same as calling {@link #start} or {@link #stop}.
     * @hide pending API council approval
     */
    @android.view.RemotableViewMethod
    public void setStarted(boolean started) {
        mStarted = started;
        updateRunning();
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        mVisible = false;
        updateRunning();
    }

    @Override
    protected void onWindowVisibilityChanged(int visibility) {
        super.onWindowVisibilityChanged(visibility);
        mVisible = visibility == VISIBLE;
        updateRunning();
    }

    private synchronized void updateText(long now) {
        long seconds = now - mBase;
        seconds /= 1000;
        String text = DateUtils.formatElapsedTime(mRecycle, seconds);

        if (mFormat != null) {
            Locale loc = Locale.getDefault();
            if (mFormatter == null || !loc.equals(mFormatterLocale)) {
                mFormatterLocale = loc;
                mFormatter = new Formatter(mFormatBuilder, loc);
            }
            mFormatBuilder.setLength(0);
            mFormatterArgs[0] = text;
            try {
                mFormatter.format(mFormat, mFormatterArgs);
                text = mFormatBuilder.toString();
            } catch (IllegalFormatException ex) {
                if (!mLogged) {
                    Log.w(TAG, "Illegal format string: " + mFormat);
                    mLogged = true;
                }
            }
        }
        setText(text);
    }

    private void updateRunning() {
        boolean running = mVisible && mStarted;
        if (running != mRunning) {
            if (running) {
                updateText(SystemClock.elapsedRealtime());
                dispatchChronometerTick();
                mHandler.sendMessageDelayed(Message.obtain(mHandler, TICK_WHAT), 1000);
            } else {
                mHandler.removeMessages(TICK_WHAT);
            }
            mRunning = running;
        }
    }
    
    private Handler mHandler = new Handler() {
        public void handleMessage(Message m) {
            if (mRunning) {
                updateText(SystemClock.elapsedRealtime());
                dispatchChronometerTick();
                sendMessageDelayed(Message.obtain(this, TICK_WHAT), 1000);
            }
        }
    };

    void dispatchChronometerTick() {
        if (mOnChronometerTickListener != null) {
            mOnChronometerTickListener.onChronometerTick(this);
        }
    }

    @Override
    public void onInitializeAccessibilityEvent(AccessibilityEvent event) {
        super.onInitializeAccessibilityEvent(event);
        event.setClassName(Chronometer.class.getName());
    }

    @Override
    public void onInitializeAccessibilityNodeInfo(AccessibilityNodeInfo info) {
        super.onInitializeAccessibilityNodeInfo(info);
        info.setClassName(Chronometer.class.getName());
    }
}
