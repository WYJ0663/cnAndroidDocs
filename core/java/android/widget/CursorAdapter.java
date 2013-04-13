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
import android.database.ContentObserver;
import android.database.Cursor;
import android.database.DataSetObserver;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

/**
 * 将 {@link android.database.Cursor 游标}的数据提供给 
 * {@link android.widget.ListView 列表视图}的适配器.
 * 游标必须包含列名为“_id”的列，否则该类无法工作.
 */
public abstract class CursorAdapter extends BaseAdapter implements Filterable,
        CursorFilter.CursorFilterClient {
    /**
     * This field should be made private, so it is hidden from the SDK.
     * {@hide}
     */
    protected boolean mDataValid;
    /**
     * This field should be made private, so it is hidden from the SDK.
     * {@hide}
     */
    protected boolean mAutoRequery;
    /**
     * This field should be made private, so it is hidden from the SDK.
     * {@hide}
     */
    protected Cursor mCursor;
    /**
     * This field should be made private, so it is hidden from the SDK.
     * {@hide}
     */
    protected Context mContext;
    /**
     * This field should be made private, so it is hidden from the SDK.
     * {@hide}
     */
    protected int mRowIDColumn;
    /**
     * This field should be made private, so it is hidden from the SDK.
     * {@hide}
     */
    protected ChangeObserver mChangeObserver;
    /**
     * This field should be made private, so it is hidden from the SDK.
     * {@hide}
     */
    protected DataSetObserver mDataSetObserver;
    /**
     * This field should be made private, so it is hidden from the SDK.
     * {@hide}
     */
    protected CursorFilter mCursorFilter;
    /**
     * This field should be made private, so it is hidden from the SDK.
     * {@hide}
     */
    protected FilterQueryProvider mFilterQueryProvider;

    /**
     * 如果设置，适配器会在收到内容变更时调用游标的 requery() 方法.
     * 请使用 {@link #FLAG_REGISTER_CONTENT_OBSERVER}.
     *
     * @deprecated 该选项已废止，因为他会在应用程序的 UI 线程中执行游标查询操作，
     * 导致响应缓慢甚至应用程序停止响应的错误.作为替代方案，请使用
     * {@link android.app.LoaderManager} 和 {@link android.content.CursorLoader}.
     */
    @Deprecated
    public static final int FLAG_AUTO_REQUERY = 0x01;

    /**
     * 如果设置，适配器会在游标上注册一个内容观测器，当通知到达时会调用 {@link #onContentChanged()}
     * 方法.使用该标志位时要注意：在注册观察器时需要先解除当前游标与适配器的关联，防止发生泄漏.
     * 如果使用带有 {@link android.content.CursorLoader} 的 CursorAdapter 则不需要该标志位.
     */
    public static final int FLAG_REGISTER_CONTENT_OBSERVER = 0x02;

    /**
     * 启用自动重查询控制的构造函数.
     *
     * @deprecated 该选项已废止，因为他会在应用程序的 UI 线程中执行游标查询操作，
     * 导致响应缓慢甚至应用程序停止响应的错误.作为替代方案，请使用
     * {@link android.app.LoaderManager} 和 {@link android.content.CursorLoader}.
     *
     * @param c 用于取得数据的游标.
     * @param context 上下文.
     */
    @Deprecated
    public CursorAdapter(Context context, Cursor c) {
        init(context, c, FLAG_AUTO_REQUERY);
    }

    /**
     * 允许使用自动重查询控制的构造函数.不推荐使用该函数，请用
     * {@link #CursorAdapter(Context, Cursor, int)} 代替.
     * 使用该构造函数时，总是会设置{@link #FLAG_REGISTER_CONTENT_OBSERVER}标志位.
     *
     * @param c 用于取得数据的游标
     * @param context 应用程序上下文
     * @param autoRequery 如果为真，适配器会在游标变更时调用 requery()方法，
     *                    总是显示最近的数据.在这里使用真已经废止.
     */
    public CursorAdapter(Context context, Cursor c, boolean autoRequery) {
        init(context, c, autoRequery ? FLAG_AUTO_REQUERY : FLAG_REGISTER_CONTENT_OBSERVER);
    }

    /**
     * 推荐的构造函数.
     *
     * @param c 用于取得数据的游标
     * @param context 应用程序上下文
     * @param flags 用于决定适配器行为的标志位.可以是 {@link #FLAG_AUTO_REQUERY} 和
     * {@link #FLAG_REGISTER_CONTENT_OBSERVER} 的任意组合.
     */
    public CursorAdapter(Context context, Cursor c, int flags) {
        init(context, c, flags);
    }

    /**
     * @deprecated 不要使用该函数.请使用正常的构造函数.将来会移除该函数.
     */
    @Deprecated
    protected void init(Context context, Cursor c, boolean autoRequery) {
        init(context, c, autoRequery ? FLAG_AUTO_REQUERY : FLAG_REGISTER_CONTENT_OBSERVER);
    }

    void init(Context context, Cursor c, int flags) {
        if ((flags & FLAG_AUTO_REQUERY) == FLAG_AUTO_REQUERY) {
            flags |= FLAG_REGISTER_CONTENT_OBSERVER;
            mAutoRequery = true;
        } else {
            mAutoRequery = false;
        }
        boolean cursorPresent = c != null;
        mCursor = c;
        mDataValid = cursorPresent;
        mContext = context;
        mRowIDColumn = cursorPresent ? c.getColumnIndexOrThrow("_id") : -1;
        if ((flags & FLAG_REGISTER_CONTENT_OBSERVER) == FLAG_REGISTER_CONTENT_OBSERVER) {
            mChangeObserver = new ChangeObserver();
            mDataSetObserver = new MyDataSetObserver();
        } else {
            mChangeObserver = null;
            mDataSetObserver = null;
        }

        if (cursorPresent) {
            if (mChangeObserver != null) c.registerContentObserver(mChangeObserver);
            if (mDataSetObserver != null) c.registerDataSetObserver(mDataSetObserver);
        }
    }

    /**
     * 返回游标.
     * @return 游标.
     */
    public Cursor getCursor() {
        return mCursor;
    }

    /**
     * @see android.widget.ListAdapter#getCount()
     */
    public int getCount() {
        if (mDataValid && mCursor != null) {
            return mCursor.getCount();
        } else {
            return 0;
        }
    }
    
    /**
     * @see android.widget.ListAdapter#getItem(int)
     */
    public Object getItem(int position) {
        if (mDataValid && mCursor != null) {
            mCursor.moveToPosition(position);
            return mCursor;
        } else {
            return null;
        }
    }

    /**
     * @see android.widget.ListAdapter#getItemId(int)
     */
    public long getItemId(int position) {
        if (mDataValid && mCursor != null) {
            if (mCursor.moveToPosition(position)) {
                return mCursor.getLong(mRowIDColumn);
            } else {
                return 0;
            }
        } else {
            return 0;
        }
    }
    
    @Override
    public boolean hasStableIds() {
        return true;
    }

    /**
     * @see android.widget.ListAdapter#getView(int, View, ViewGroup)
     */
    public View getView(int position, View convertView, ViewGroup parent) {
        if (!mDataValid) {
            throw new IllegalStateException("this should only be called when the cursor is valid");
        }
        if (!mCursor.moveToPosition(position)) {
            throw new IllegalStateException("couldn't move cursor to position " + position);
        }
        View v;
        if (convertView == null) {
            v = newView(mContext, mCursor, parent);
        } else {
            v = convertView;
        }
        bindView(v, mContext, mCursor);
        return v;
    }

    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent) {
        if (mDataValid) {
            mCursor.moveToPosition(position);
            View v;
            if (convertView == null) {
                v = newDropDownView(mContext, mCursor, parent);
            } else {
                v = convertView;
            }
            bindView(v, mContext, mCursor);
            return v;
        } else {
            return null;
        }
    }
    
    /**
     * 为游标指向的数据创建新视图.
     * @param context 上下文.访问应用程序全局信息的接口.
     * @param cursor 用于取得数据的游标.游标已经移到正确的位置.
     * @param parent 新创建的视图的容器.
     * @return 新创建的视图.
     */
    public abstract View newView(Context context, Cursor cursor, ViewGroup parent);

    /**
     * 为游标指向的数据创建新的下拉列表视图.
     * @param context 上下文.访问应用程序全局信息的接口.
     * @param cursor 用于取得数据的游标.游标已经移到正确的位置.
     * @param parent 新创建的视图的容器.
     * @return 新创建的视图.
     */
    public View newDropDownView(Context context, Cursor cursor, ViewGroup parent) {
        return newView(context, cursor, parent);
    }

    /**
     * 绑定游标指向的数据到既存视图.
     * @param view 之前由 newView 方法创建的视图.
     * @param context 上下文.访问应用程序全局信息的接口.
     * @param cursor 用于取得数据的游标.游标已经移到正确的位置.
     */
    public abstract void bindView(View view, Context context, Cursor cursor);
    
    /**
     * 将底层的游标更改为新的游标.该函数将关闭既存游标.
     * 
     * @param cursor 要使用的新游标.
     */
    public void changeCursor(Cursor cursor) {
        Cursor old = swapCursor(cursor);
        if (old != null) {
            old.close();
        }
    }

    /**
     * 使用新游标并返回旧游标.与 {@link #changeCursor(Cursor)} 不同，返回的旧游标
     * <em>没有</em>关闭.
     *
     * @param newCursor 要使用的新游标.
     * @return 返回之前使用的游标，如果没有则返回空.如果传入的新游标与当前游标相同，总是返回空.
     */
    public Cursor swapCursor(Cursor newCursor) {
        if (newCursor == mCursor) {
            return null;
        }
        Cursor oldCursor = mCursor;
        if (oldCursor != null) {
            if (mChangeObserver != null) oldCursor.unregisterContentObserver(mChangeObserver);
            if (mDataSetObserver != null) oldCursor.unregisterDataSetObserver(mDataSetObserver);
        }
        mCursor = newCursor;
        if (newCursor != null) {
            if (mChangeObserver != null) newCursor.registerContentObserver(mChangeObserver);
            if (mDataSetObserver != null) newCursor.registerDataSetObserver(mDataSetObserver);
            mRowIDColumn = newCursor.getColumnIndexOrThrow("_id");
            mDataValid = true;
            // notify the observers about the new cursor
            notifyDataSetChanged();
        } else {
            mRowIDColumn = -1;
            mDataValid = false;
            // notify the observers about the lack of a data set
            notifyDataSetInvalidated();
        }
        return oldCursor;
    }

    /**
     * <p>将游标转换为 CharSequence.子类应该覆盖该方法来进行结果转换.
     * 默认实现返回其值对应的字符串，对于空值返回空串.</p>
     *
     * @param cursor 要转换为 CharSequence 的游标.
     * @return 代表其值的 CharSequence.
     */
    public CharSequence convertToString(Cursor cursor) {
        return cursor == null ? "" : cursor.toString();
    }

    /**
     * 用指定条件查询.查询由关联到适配器的过滤器来执行.
     *
     * 查询服务由 {@link android.widget.FilterQueryProvider} 执行.
     * 如果没有指定提供者，返回未经筛选的当前游标.
     *
     * 该方法返回传入 {@link #changeCursor(Cursor)} 方法的结果游标，
     * 并关闭之前的游标.
     *
     * 该方法总在后台执行，不在应用程序的主进程（或者说UI进程）.
     * 
     * 约定：当检索条件为空或空字符串时，必须返回应用过滤器前的原始结果. 
     *
     * @param constraint 检索的约束条件.
     *
     * @return 代表新的查询结果的游标.
     *
     * @see #getFilter()
     * @see #getFilterQueryProvider()
     * @see #setFilterQueryProvider(android.widget.FilterQueryProvider)
     */
    public Cursor runQueryOnBackgroundThread(CharSequence constraint) {
        if (mFilterQueryProvider != null) {
            return mFilterQueryProvider.runQuery(constraint);
        }

        return mCursor;
    }

    public Filter getFilter() {
        if (mCursorFilter == null) {
            mCursorFilter = new CursorFilter(this);
        }
        return mCursorFilter;
    }

    /**
     * 返回查询时用于过滤数据的提供者.当提供者为空时，不应用过滤.
     *
     * @return 当前过滤器的提供者，如果不存在返回空.
     *
     * @see #setFilterQueryProvider(android.widget.FilterQueryProvider)
     * @see #runQueryOnBackgroundThread(CharSequence)
     */
    public FilterQueryProvider getFilterQueryProvider() {
        return mFilterQueryProvider;
    }

    /**
     * 设置用于过滤当前游标的查询过滤器提供者.当该提供者的客户端请求过滤时，
     * 执行提供者的
     * {@link android.widget.FilterQueryProvider#runQuery(CharSequence)}
     * 方法.
     *
     * @param filterQueryProvider 过滤器查询提供者；空可以清除之前设置的提供者.
     *
     * @see #getFilterQueryProvider()
     * @see #runQueryOnBackgroundThread(CharSequence)
     */
    public void setFilterQueryProvider(FilterQueryProvider filterQueryProvider) {
        mFilterQueryProvider = filterQueryProvider;
    }

    /**
     * 当游标的 {@link ContentObserver} 收到变更提示时调用该方法.
     * 缺省实现提供了自动再查询逻辑，但子类可以覆盖该方法.
     * 
     * @see ContentObserver#onChange(boolean)
     */
    protected void onContentChanged() {
        if (mAutoRequery && mCursor != null && !mCursor.isClosed()) {
            if (false) Log.v("Cursor", "Auto requerying " + mCursor + " due to update");
            mDataValid = mCursor.requery();
        }
    }

    private class ChangeObserver extends ContentObserver {
        public ChangeObserver() {
            super(new Handler());
        }

        @Override
        public boolean deliverSelfNotifications() {
            return true;
        }

        @Override
        public void onChange(boolean selfChange) {
            onContentChanged();
        }
    }

    private class MyDataSetObserver extends DataSetObserver {
        @Override
        public void onChanged() {
            mDataValid = true;
            notifyDataSetChanged();
        }

        @Override
        public void onInvalidated() {
            mDataValid = false;
            notifyDataSetInvalidated();
        }
    }

}
