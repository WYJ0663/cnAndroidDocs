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

package android.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.widget.RemoteViews.RemoteView;

import com.android.internal.R;

import java.lang.ref.WeakReference;

/**
 * ViewStub 是不可见的不占用布局空间的视图，用于在运行时延迟加载布局资源.
 * 
 * 当ViewStub可见，或者调用 {@link #inflate()}函数时，才会加载布局资源. 
 * 之后在父容器中用展开的一个或多个视图替换它本身.因此，调用
 * {@link #setVisibility(int)}或者{@link #inflate()}之前，ViewStub
 * 会一直存在于视图层次中.
 * 
 * 展开的视图会与ViewStub的布局参数一同添加到其父容器中.
 * 同样，你可以使用ViewStub的inflatedId属性定义或重写加载视图的ID.
 * 例如：
 * <pre>
 *     &lt;ViewStub android:id="@+id/stub"
 *               android:inflatedId="@+id/subTree"
 *               android:layout="@layout/mySubTree"
 *               android:layout_width="120dip"
 *               android:layout_height="40dip" /&gt;
 * </pre>
 *
 * 我们看到的用“stub”ID定义的 ViewStub. 加载布局资源“mySubTree”之后，会从其父容器中移除ViewStub.
 * 由加载的“mySubTree”布局资源创建的视图的ID，被inflatedId属性指定为“subTree”.
 * 为加载的视图最终分配120dpi的宽度和40dpi的高度.
 *
 * 执行加载布局资源的首选方式如下：
 *
 * <pre>
 *     ViewStub stub = (ViewStub) findViewById(R.id.stub);
 *     View inflated = stub.inflate();
 * </pre>
 *
 * 当执行 {@link #inflate()}时，ViewStub被加载的视图取代，并返回加载的视图.
 * 这使应用程序不必执行额外的findViewById()方法即可得到对加载的视图的引用.
 *
 * @attr ref android.R.styleable#ViewStub_inflatedId
 * @attr ref android.R.styleable#ViewStub_layout
 */
@RemoteView
public final class ViewStub extends View {
    private int mLayoutResource = 0;
    private int mInflatedId;

    private WeakReference<View> mInflatedViewRef;

    private LayoutInflater mInflater;
    private OnInflateListener mInflateListener;

    public ViewStub(Context context) {
        initialize(context);
    }

    /**
     * 使用指定的布局资源创建一个新的ViewStub对象.
     *
     * @param context 应用程序上下文.
     * @param layoutResource 对要加载的布局资源的引用.
     */
    public ViewStub(Context context, int layoutResource) {
        mLayoutResource = layoutResource;
        initialize(context);
    }

    public ViewStub(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    @SuppressWarnings({"UnusedDeclaration"})
    public ViewStub(Context context, AttributeSet attrs, int defStyle) {
        TypedArray a = context.obtainStyledAttributes(attrs, com.android.internal.R.styleable.ViewStub,
                defStyle, 0);

        mInflatedId = a.getResourceId(R.styleable.ViewStub_inflatedId, NO_ID);
        mLayoutResource = a.getResourceId(R.styleable.ViewStub_layout, 0);

        a.recycle();

        a = context.obtainStyledAttributes(attrs, com.android.internal.R.styleable.View, defStyle, 0);
        mID = a.getResourceId(R.styleable.View_id, NO_ID);
        a.recycle();

        initialize(context);
    }

    private void initialize(Context context) {
        mContext = context;
        setVisibility(GONE);
        setWillNotDraw(true);
    }

    /**
     * 返回用于要加载视图的ID.如果该ID为{@link View#NO_ID}，
     * 则保持要展开视图原始的ID.
     *
     * @return 标识要展开视图的正整数ID；如果要保持其原始ID则返回{@link #NO_ID}.
     *
     * @see #setInflatedId(int)
     * @attr ref android.R.styleable#ViewStub_inflatedId
     */
    public int getInflatedId() {
        return mInflatedId;
    }

    /**
     * 定义用于要加载视图的ID.如果该ID为{@link View#NO_ID}，
     * 则保持要展开视图原始的ID.
     *
     * @param inflatedId 标识要展开视图的正整数ID；如果要保持其原始ID
     *                   则应使用{@link #NO_ID}.
     *                   
     * @see #getInflatedId()
     * @attr ref android.R.styleable#ViewStub_inflatedId
     */
    @android.view.RemotableViewMethod
    public void setInflatedId(int inflatedId) {
        mInflatedId = inflatedId;
    }

    /**
     * 返回{@link #setVisibility(int)}或{@link #inflate()}函数执行时用于替换
     * StubbedView 的布局资源.
     *
     * @return 用于载入新视图的布局资源ID.
     *
     * @see #setLayoutResource(int)
     * @see #setVisibility(int)
     * @see #inflate()
     * @attr ref android.R.styleable#ViewStub_layout
     */
    public int getLayoutResource() {
        return mLayoutResource;
    }

    /**
     * 指定当该 StubbedView 变为可见、不可见或者执行{@link #inflate()}方法时要载入的布局资源.
     * 布局载入创建的视图用于替换父视图中的StubbedView.
     * 
     * @param layoutResource 有效的布局资源ID（不等于0）.
     * 
     * @see #getLayoutResource()
     * @see #setVisibility(int)
     * @see #inflate()
     * @attr ref android.R.styleable#ViewStub_layout
     */
    @android.view.RemotableViewMethod
    public void setLayoutResource(int layoutResource) {
        mLayoutResource = layoutResource;
    }

    /**
     * Set {@link LayoutInflater} to use in {@link #inflate()}, or {@code null}
     * to use the default.
     */
    public void setLayoutInflater(LayoutInflater inflater) {
        mInflater = inflater;
    }

    /**
     * Get current {@link LayoutInflater} used in {@link #inflate()}.
     */
    public LayoutInflater getLayoutInflater() {
        return mInflater;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        setMeasuredDimension(0, 0);
    }

    @Override
    public void draw(Canvas canvas) {
    }

    @Override
    protected void dispatchDraw(Canvas canvas) {
    }

    /**
     * 可视性设为 {@link #VISIBLE} 或 {@link #INVISIBLE}时，执行
     * {@link #inflate()} 用载入的布局资源替换父视图中的StubbedView.
     * After that calls to this function are passed
     * through to the inflated view.
     *
     * @param visibility {@link #VISIBLE}、{@link #INVISIBLE}或者{@link #GONE}.
     *
     * @see #inflate() 
     */
    @Override
    @android.view.RemotableViewMethod
    public void setVisibility(int visibility) {
        if (mInflatedViewRef != null) {
            View view = mInflatedViewRef.get();
            if (view != null) {
                view.setVisibility(visibility);
            } else {
                throw new IllegalStateException("setVisibility called on un-referenced view");
            }
        } else {
            super.setVisibility(visibility);
            if (visibility == VISIBLE || visibility == INVISIBLE) {
                inflate();
            }
        }
    }

    /**
     * 展开由{@link #getLayoutResource()}指定的布局资源，并用该资源替换父视图中的
     * StubbedView.
     *
     * @return 展开的布局资源.
     *
     */
    public View inflate() {
        final ViewParent viewParent = getParent();

        if (viewParent != null && viewParent instanceof ViewGroup) {
            if (mLayoutResource != 0) {
                final ViewGroup parent = (ViewGroup) viewParent;
                final LayoutInflater factory;
                if (mInflater != null) {
                    factory = mInflater;
                } else {
                    factory = LayoutInflater.from(mContext);
                }
                final View view = factory.inflate(mLayoutResource, parent,
                        false);

                if (mInflatedId != NO_ID) {
                    view.setId(mInflatedId);
                }

                final int index = parent.indexOfChild(this);
                parent.removeViewInLayout(this);

                final ViewGroup.LayoutParams layoutParams = getLayoutParams();
                if (layoutParams != null) {
                    parent.addView(view, index, layoutParams);
                } else {
                    parent.addView(view, index);
                }

                mInflatedViewRef = new WeakReference<View>(view);

                if (mInflateListener != null) {
                    mInflateListener.onInflate(this, view);
                }

                return view;
            } else {
                throw new IllegalArgumentException("ViewStub must have a valid layoutResource");
            }
        } else {
            throw new IllegalStateException("ViewStub must have a non-null ViewGroup viewParent");
        }
    }

    /**
     * 指定展开监听器一遍获得ViewStub成功展开其布局资源的通知.
     *
     * @param inflateListener 成功展开后接受通知的OnInflateListener.
     *
     * @see android.view.ViewStub.OnInflateListener
     */
    public void setOnInflateListener(OnInflateListener inflateListener) {
        mInflateListener = inflateListener;
    }

    /**
     * 用于接收 ViewStub 成功展开其布局资源的监听器.
     *
     * @see android.view.ViewStub#setOnInflateListener(android.view.ViewStub.OnInflateListener) 
     */
    public static interface OnInflateListener {
        /**
         * ViewStub 成功展开其布局资源后执行.该方法在展开的视图加入视图层次之后、
         * 布局完成之前执行.
         *
         * @param stub 发生展开事件的 ViewStub.
         * @param inflated 展开的视图.
         */
        void onInflate(ViewStub stub, View inflated);
    }
}
