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

import android.app.INotificationManager;
import android.app.ITransientNotification;
import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.PixelFormat;
import android.os.Handler;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityManager;

/**
 * Toast是为用户提供简短信息的视图.Toast类帮助你创建和显示该视图.
 * {@more}
 *
 * <p>
 * 该视图以浮于应用程序之上的形式呈现给用户.
 * 因为它并不获得焦点，即使用户正在输入也不会受到影响.
 * 它的目标是尽可能不中断用户操作，并使用户看到你提供的信息.
 * 有两个典型的例子就是音量控制和设置信息保存成功提示.</p>
 * <p>
 * 使用该类最简单的方法就是调用其静态方法，让他来构造你需要的一切并返回一个新的 Toast 对象.
 *
 * <div class="special reference">
 * <h3>开发者指南</h3>
 * <p>关于创建 Toast 提示的使用，参见
 * <a href="{@docRoot}guide/topics/ui/notifiers/toasts.html">
 * Toast 提示</a> 开发者指南.</p>
 * </div>
 * @author translate by cnmahj/jiahuibin（Android中文翻译组）
 * @author convert by cnmahj
 */ 
public class Toast {
    static final String TAG = "Toast";
    static final boolean localLOGV = false;

    /**
     * 持续显示视图或文本提示较短时间.该时间长度可定制.该值为默认值.
     * @see #setDuration
     */
    public static final int LENGTH_SHORT = 0;

    /**
     * 持续显示视图或文本提示较长时间.该时间长度可定制.
     * @see #setDuration
     */
    public static final int LENGTH_LONG = 1;

    final Context mContext;
    final TN mTN;
    int mDuration;
    View mNextView;

    /**
     * 构造一个空的 Toast 对象.在调用 {@link #show} 之前，必须先调用 {@link #setView}.
     *
     * @param context  使用的上下文.通常是你的 {@link android.app.Application} 或
     * {@link android.app.Activity} 对象.
     */
    public Toast(Context context) {
        mContext = context;
        mTN = new TN();
        mTN.mY = context.getResources().getDimensionPixelSize(
                com.android.internal.R.dimen.toast_y_offset);
    }
    
    /**
     * 按照指定的存续期间显示提示信息.
     */
    public void show() {
        if (mNextView == null) {
            throw new RuntimeException("setView must have been called");
        }

        INotificationManager service = getService();
        String pkg = mContext.getPackageName();
        TN tn = mTN;
        tn.mNextView = mNextView;

        try {
            service.enqueueToast(pkg, tn, mDuration);
        } catch (RemoteException e) {
            // Empty
        }
    }

    /**
     * 如果视图已经显示则将其关闭，还没有显示则不再显示.一般不需要调用该方法.
     * 正常情况下，视图会在超过存续期间后消失.
     */
    public void cancel() {
        mTN.hide();

        try {
            getService().cancelToast(mContext.getPackageName(), mTN);
        } catch (RemoteException e) {
            // Empty
        }
    }
    
    /**
     * 设置要显示的视图.
     * @see #getView
     */
    public void setView(View view) {
        mNextView = view;
    }

    /**
     * 返回视图对象.
     * @see #setView
     */
    public View getView() {
        return mNextView;
    }

    /**
     * 设置存续期间.
     * @see #LENGTH_SHORT
     * @see #LENGTH_LONG
     */
    public void setDuration(int duration) {
        mDuration = duration;
    }

    /**
     * 返回存续期间.
     * @see #setDuration
     */
    public int getDuration() {
        return mDuration;
    }
    
    /**
     * 设置视图的栏外空白.
     *
     * @param horizontalMargin 容器的边缘与提示信息的横向空白（与容器宽度的比）.
     * @param verticalMargin 容器的边缘与提示信息的纵向空白（与容器高度的比）.
     */
    public void setMargin(float horizontalMargin, float verticalMargin) {
        mTN.mHorizontalMargin = horizontalMargin;
        mTN.mVerticalMargin = verticalMargin;
    }

    /**
     * 返回横向栏外空白.
     */
    public float getHorizontalMargin() {
        return mTN.mHorizontalMargin;
    }

    /**
     * 返回纵向栏外空白.
     */
    public float getVerticalMargin() {
        return mTN.mVerticalMargin;
    }

    /**
     * 设置提示信息在屏幕上的显示位置.
     * @see android.view.Gravity
     * @see #getGravity
     */
    public void setGravity(int gravity, int xOffset, int yOffset) {
        mTN.mGravity = gravity;
        mTN.mX = xOffset;
        mTN.mY = yOffset;
    }

     /**
     * 取得提示信息在屏幕上显示位置.
     * @see android.view.Gravity
     * @see #getGravity
     */
    public int getGravity() {
        return mTN.mGravity;
    }

    /**
     * 返回相对于指定显示位置的横向偏移像素量.
     */
    public int getXOffset() {
        return mTN.mX;
    }
    
    /**
     * 返回相对于指定显示位置的纵向偏移像素量.
     */
    public int getYOffset() {
        return mTN.mY;
    }
    
    /**
     * 生成一个包含文本的标准 Toast 视图对象.
     *
     * @param context  使用的上下文.通常是你的 {@link android.app.Application}
     *                 或 {@link android.app.Activity} 对象.
     * @param text     要显示的文本，可以是已格式化文本.
     * @param duration 该信息的存续期间.值为 {@link #LENGTH_SHORT} 或 {@link #LENGTH_LONG}.
     *
     */
    public static Toast makeText(Context context, CharSequence text, int duration) {
        Toast result = new Toast(context);

        LayoutInflater inflate = (LayoutInflater)
                context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View v = inflate.inflate(com.android.internal.R.layout.transient_notification, null);
        TextView tv = (TextView)v.findViewById(com.android.internal.R.id.message);
        tv.setText(text);
        
        result.mNextView = v;
        result.mDuration = duration;

        return result;
    }

    /**
     * 生成一个从资源中取得的包含文本视图的标准 Toast 对象.
     *
     * @param context  使用的上下文.通常是你的 {@link android.app.Application}
     *                 或 {@link android.app.Activity} 对象.
     * @param resId    要使用的字符串资源ID，可以是已格式化文本.
     * @param duration 该信息的存续期间.值为 {@link #LENGTH_SHORT} 或 {@link #LENGTH_LONG}.
     *
     * @throws Resources.NotFoundException 当资源未找到时
     */
    public static Toast makeText(Context context, int resId, int duration)
                                throws Resources.NotFoundException {
        return makeText(context, context.getResources().getText(resId), duration);
    }

    /**
     * 更新之前通过 makeText() 方法生成的 Toast 对象的文本内容.
     * @param resId 为 Toast 指定的新的字符串资源ID.
     */
    public void setText(int resId) {
        setText(mContext.getText(resId));
    }
    
    /**
     * 更新之前通过 makeText() 方法生成的 Toast 对象的文本内容.
     * @param s 为 Toast 指定的新的文本.
     */
    public void setText(CharSequence s) {
        if (mNextView == null) {
            throw new RuntimeException("This Toast was not created with Toast.makeText()");
        }
        TextView tv = (TextView) mNextView.findViewById(com.android.internal.R.id.message);
        if (tv == null) {
            throw new RuntimeException("This Toast was not created with Toast.makeText()");
        }
        tv.setText(s);
    }

    // =======================================================================================
    // All the gunk below is the interaction with the Notification Service, which handles
    // the proper ordering of these system-wide.
    // =======================================================================================

    private static INotificationManager sService;

    static private INotificationManager getService() {
        if (sService != null) {
            return sService;
        }
        sService = INotificationManager.Stub.asInterface(ServiceManager.getService("notification"));
        return sService;
    }

    private static class TN extends ITransientNotification.Stub {
        final Runnable mShow = new Runnable() {
            @Override
            public void run() {
                handleShow();
            }
        };

        final Runnable mHide = new Runnable() {
            @Override
            public void run() {
                handleHide();
                // Don't do this in handleHide() because it is also invoked by handleShow()
                mNextView = null;
            }
        };

        private final WindowManager.LayoutParams mParams = new WindowManager.LayoutParams();
        final Handler mHandler = new Handler();    

        int mGravity = Gravity.CENTER_HORIZONTAL | Gravity.BOTTOM;
        int mX, mY;
        float mHorizontalMargin;
        float mVerticalMargin;

       
        View mView;
        View mNextView;

        WindowManager mWM;

        TN() {
            // XXX This should be changed to use a Dialog, with a Theme.Toast
            // defined that sets up the layout params appropriately.
            final WindowManager.LayoutParams params = mParams;
            params.height = WindowManager.LayoutParams.WRAP_CONTENT;
            params.width = WindowManager.LayoutParams.WRAP_CONTENT;
            params.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                    | WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
                    | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON;
            params.format = PixelFormat.TRANSLUCENT;
            params.windowAnimations = com.android.internal.R.style.Animation_Toast;
            params.type = WindowManager.LayoutParams.TYPE_TOAST;
            params.setTitle("Toast");
        }

        /**
         * schedule handleShow into the right thread
         */
        @Override
        public void show() {
            if (localLOGV) Log.v(TAG, "SHOW: " + this);
            mHandler.post(mShow);
        }

        /**
         * schedule handleHide into the right thread
         */
        @Override
        public void hide() {
            if (localLOGV) Log.v(TAG, "HIDE: " + this);
            mHandler.post(mHide);
        }

        public void handleShow() {
            if (localLOGV) Log.v(TAG, "HANDLE SHOW: " + this + " mView=" + mView
                    + " mNextView=" + mNextView);
            if (mView != mNextView) {
                // remove the old view if necessary
                handleHide();
                mView = mNextView;
                Context context = mView.getContext().getApplicationContext();
                if (context == null) {
                    context = mView.getContext();
                }
                mWM = (WindowManager)context.getSystemService(Context.WINDOW_SERVICE);
                // We can resolve the Gravity here by using the Locale for getting
                // the layout direction
                final Configuration config = mView.getContext().getResources().getConfiguration();
                final int gravity = Gravity.getAbsoluteGravity(mGravity, config.getLayoutDirection());
                mParams.gravity = gravity;
                if ((gravity & Gravity.HORIZONTAL_GRAVITY_MASK) == Gravity.FILL_HORIZONTAL) {
                    mParams.horizontalWeight = 1.0f;
                }
                if ((gravity & Gravity.VERTICAL_GRAVITY_MASK) == Gravity.FILL_VERTICAL) {
                    mParams.verticalWeight = 1.0f;
                }
                mParams.x = mX;
                mParams.y = mY;
                mParams.verticalMargin = mVerticalMargin;
                mParams.horizontalMargin = mHorizontalMargin;
                if (mView.getParent() != null) {
                    if (localLOGV) Log.v(TAG, "REMOVE! " + mView + " in " + this);
                    mWM.removeView(mView);
                }
                if (localLOGV) Log.v(TAG, "ADD! " + mView + " in " + this);
                mWM.addView(mView, mParams);
                trySendAccessibilityEvent();
            }
        }

        private void trySendAccessibilityEvent() {
            AccessibilityManager accessibilityManager =
                    AccessibilityManager.getInstance(mView.getContext());
            if (!accessibilityManager.isEnabled()) {
                return;
            }
            // treat toasts as notifications since they are used to
            // announce a transient piece of information to the user
            AccessibilityEvent event = AccessibilityEvent.obtain(
                    AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED);
            event.setClassName(getClass().getName());
            event.setPackageName(mView.getContext().getPackageName());
            mView.dispatchPopulateAccessibilityEvent(event);
            accessibilityManager.sendAccessibilityEvent(event);
        }        

        public void handleHide() {
            if (localLOGV) Log.v(TAG, "HANDLE HIDE: " + this + " mView=" + mView);
            if (mView != null) {
                // note: checking parent() just to make sure the view has
                // been added...  i have seen cases where we get here when
                // the view isn't yet added, so let's try not to crash.
                if (mView.getParent() != null) {
                    if (localLOGV) Log.v(TAG, "REMOVE! " + mView + " in " + this);
                    mWM.removeView(mView);
                }

                mView = null;
            }
        }
    }
}
