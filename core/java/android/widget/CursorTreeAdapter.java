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

import android.app.Activity;
import android.content.Context;
import android.database.ContentObserver;
import android.database.Cursor;
import android.database.DataSetObserver;
import android.os.Handler;
import android.util.Log;
import android.util.SparseArray;
import android.view.View;
import android.view.ViewGroup;

/**
 * 将一组{@link Cursor 游标}的数据提供给{@link ExpandableListView 可扩展列表视图}
 * 的适配器.  顶层{@link Cursor 游标}（由构造函数提供）提供分组的数据.由
 * {@link #getChildrenCursor(Cursor)}返回的一系列{@link Cursor 游标}
 * 用于为分组对应的子条目提供数据.要使该类可用，这些游标必须包含“_id”列.
 */
public abstract class CursorTreeAdapter extends BaseExpandableListAdapter implements Filterable,
        CursorFilter.CursorFilterClient {
    private Context mContext;
    private Handler mHandler;
    private boolean mAutoRequery;

    /** The cursor helper that is used to get the groups */
    MyCursorHelper mGroupCursorHelper;
    
    /**
     * The map of a group position to the group's children cursor helper (the
     * cursor helper that is used to get the children for that group)
     */
    SparseArray<MyCursorHelper> mChildrenCursorHelpers;

    // Filter related
    CursorFilter mCursorFilter;
    FilterQueryProvider mFilterQueryProvider;
    
    /**
     * 构造函数.当数据库的数据发生改变时，适配器将调用{@link Cursor#requery()}，
     * 重新查询以显示最新的数据.
     *
     * @param cursor 为分组提供数据的游标.
     * @param context 应用程序上下文.
     */
    public CursorTreeAdapter(Cursor cursor, Context context) {
        init(cursor, context, true);
    }

    /**
     * 构造函数.
     * 
     * @param cursor 为分组提供数据的游标.
     * @param context 应用程序上下文.
     * @param autoRequery 设置为true时，一旦数据库的数据发生变化，适配器会调用
     *        {@link Cursor#requery()}，以保持显示最新的数据.
     */
    public CursorTreeAdapter(Cursor cursor, Context context, boolean autoRequery) {
        init(cursor, context, autoRequery);
    }
    
    private void init(Cursor cursor, Context context, boolean autoRequery) {
        mContext = context;
        mHandler = new Handler();
        mAutoRequery = autoRequery;
        
        mGroupCursorHelper = new MyCursorHelper(cursor);
        mChildrenCursorHelpers = new SparseArray<MyCursorHelper>();
    }

    /**
     * Gets the cursor helper for the children in the given group.
     * 
     * @param groupPosition The group whose children will be returned
     * @param requestCursor Whether to request a Cursor via
     *            {@link #getChildrenCursor(Cursor)} (true), or to assume a call
     *            to {@link #setChildrenCursor(int, Cursor)} will happen shortly
     *            (false).
     * @return The cursor helper for the children of the given group
     */
    synchronized MyCursorHelper getChildrenCursorHelper(int groupPosition, boolean requestCursor) {
        MyCursorHelper cursorHelper = mChildrenCursorHelpers.get(groupPosition);
        
        if (cursorHelper == null) {
            if (mGroupCursorHelper.moveTo(groupPosition) == null) return null;
            
            final Cursor cursor = getChildrenCursor(mGroupCursorHelper.getCursor());
            cursorHelper = new MyCursorHelper(cursor);
            mChildrenCursorHelpers.put(groupPosition, cursorHelper);
        }
        
        return cursorHelper;
    }

    /**
     * 为指定分组的子条目取得游标.子类必须实现这个方法，为指定分组提供子条目的数据.
     * <p>
     * 为了避免UI阻塞，可以异步查询提供者，通过返回空，并在查询成功后调用
     * {@link #setChildrenCursor(int, Cursor)}即可.
     * <p>
     * 你有责任在活动的整个生命周期中管理该游标对象.有个好办法，你可以使用
     * {@link Activity#managedQuery}函数，它会为你完成该工作.在某些情况下，
     * 适配器会使游标停止工作，但该情况不会总是出现，因此请确保有效地管理好游标.
     * 
     * @param groupCursor 分组游标对象，决定返回哪个分组的子条目用游标.
     * @return 指定分组的子条目用游标，或者为空.
     */
    abstract protected Cursor getChildrenCursor(Cursor groupCursor);
    
    /**
     * 设置分组游标.
     * 
     * @param cursor 为分组设置的游标.如果有既存游标，会将其关闭.
     */
    public void setGroupCursor(Cursor cursor) {
        mGroupCursorHelper.changeCursor(cursor, false);
    }
    
    /**
     * 设置指定分组的子条目用游标.如果有既存游标，会将其关闭.
     * <p>
     * 防止UI阻塞，使用异步查询时使用该方法.
     * 
     * @param groupPosition 要设置子条目游标的分组.
     * @param childrenCursor 用于分组子条目的游标.
     */
    public void setChildrenCursor(int groupPosition, Cursor childrenCursor) {
        
        /*
         * Don't request a cursor from the subclass, instead we will be setting
         * the cursor ourselves.
         */
        MyCursorHelper childrenCursorHelper = getChildrenCursorHelper(groupPosition, false);

        /*
         * Don't release any cursor since we know exactly what data is changing
         * (this cursor, which is still valid).
         */
        childrenCursorHelper.changeCursor(childrenCursor, false);
    }
    
    public Cursor getChild(int groupPosition, int childPosition) {
        // Return this group's children Cursor pointing to the particular child
        return getChildrenCursorHelper(groupPosition, true).moveTo(childPosition);
    }

    public long getChildId(int groupPosition, int childPosition) {
        return getChildrenCursorHelper(groupPosition, true).getId(childPosition);
    }

    public int getChildrenCount(int groupPosition) {
        MyCursorHelper helper = getChildrenCursorHelper(groupPosition, true);
        return (mGroupCursorHelper.isValid() && helper != null) ? helper.getCount() : 0;
    }

    public Cursor getGroup(int groupPosition) {
        // Return the group Cursor pointing to the given group
        return mGroupCursorHelper.moveTo(groupPosition);
    }

    public int getGroupCount() {
        return mGroupCursorHelper.getCount();
    }

    public long getGroupId(int groupPosition) {
        return mGroupCursorHelper.getId(groupPosition);
    }

    public View getGroupView(int groupPosition, boolean isExpanded, View convertView,
            ViewGroup parent) {
        Cursor cursor = mGroupCursorHelper.moveTo(groupPosition);
        if (cursor == null) {
            throw new IllegalStateException("this should only be called when the cursor is valid");
        }
        
        View v;
        if (convertView == null) {
            v = newGroupView(mContext, cursor, isExpanded, parent);
        } else {
            v = convertView;
        }
        bindGroupView(v, mContext, cursor, isExpanded);
        return v;
    }

    /**
     * 根据游标指向的数据生成新的分组视图.
     * 
     * @param context 应用程序上下文.
     * @param cursor 用于取得分组数据的游标.游标已经定位到正确位置.
     * @param isExpanded 分组是否为展开状态.
     * @param parent 容纳该新视图的父视图.
     * @return 新生成的视图.
     */
    protected abstract View newGroupView(Context context, Cursor cursor, boolean isExpanded,
            ViewGroup parent);

    /**
     * 将游标指定的分组数据绑定到既存视图.
     * 
     * @param view 之前由 newChildView 返回的既存视图.
     * @param context 应用程序上下文.
     * @param cursor 用于取得数据的游标.游标已经移到正确位置.
     * @param isExpanded 分组是否已展开.
     */
    protected abstract void bindGroupView(View view, Context context, Cursor cursor,
            boolean isExpanded);

    public View getChildView(int groupPosition, int childPosition, boolean isLastChild,
            View convertView, ViewGroup parent) {
        MyCursorHelper cursorHelper = getChildrenCursorHelper(groupPosition, true);
        
        Cursor cursor = cursorHelper.moveTo(childPosition);
        if (cursor == null) {
            throw new IllegalStateException("this should only be called when the cursor is valid");
        }
        
        View v;
        if (convertView == null) {
            v = newChildView(mContext, cursor, isLastChild, parent);
        } else {
            v = convertView;
        }
        bindChildView(v, mContext, cursor, isLastChild);
        return v;
    }

    /**
     * 根据游标指向的数据生成新子视图.
     * 
     * @param context 应用程序上下文.
     * @param cursor 用于取得数据的游标.游标已定位于正确位置.
     * @param isLastChild 该子条目是否是分组的最后一个条目.
     * @param parent 容纳该新视图的父视图.
     * @return 新生成的视图.
     */
    protected abstract View newChildView(Context context, Cursor cursor, boolean isLastChild,
            ViewGroup parent);

    /**
     * 将游标指定的子数据绑定到既存视图.
     * 
     * @param view 之前由 newChildView 返回的既存视图.
     * @param context 应用程序上下文.
     * @param cursor 用于取得数据的游标.游标已经移到正确位置.
     * @param isLastChild 视图是否是分组中的最后一个子视图.
     */
    protected abstract void bindChildView(View view, Context context, Cursor cursor,
            boolean isLastChild);
    
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }

    public boolean hasStableIds() {
        return true;
    }

    private synchronized void releaseCursorHelpers() {
        for (int pos = mChildrenCursorHelpers.size() - 1; pos >= 0; pos--) {
            mChildrenCursorHelpers.valueAt(pos).deactivate();
        }
        
        mChildrenCursorHelpers.clear();
    }
    
    @Override
    public void notifyDataSetChanged() {
        notifyDataSetChanged(true);
    }

    /**
     * 通知数据变更，并包含是否是否缓存的游标的选项.
     * 
     * @param releaseCursors 是否释放并停止缓存的所有游标.
     */
    public void notifyDataSetChanged(boolean releaseCursors) {
        
        if (releaseCursors) {
            releaseCursorHelpers();
        }
        
        super.notifyDataSetChanged();
    }
    
    @Override
    public void notifyDataSetInvalidated() {
        releaseCursorHelpers();
        super.notifyDataSetInvalidated();
    }

    @Override
    public void onGroupCollapsed(int groupPosition) {
        deactivateChildrenCursorHelper(groupPosition);
    }

    /**
     * Deactivates the Cursor and removes the helper from cache.
     * 
     * @param groupPosition The group whose children Cursor and helper should be
     *            deactivated.
     */
    synchronized void deactivateChildrenCursorHelper(int groupPosition) {
        MyCursorHelper cursorHelper = getChildrenCursorHelper(groupPosition, true);
        mChildrenCursorHelpers.remove(groupPosition);
        cursorHelper.deactivate();
    }

    /**
     * @see CursorAdapter#convertToString(Cursor)
     */
    public String convertToString(Cursor cursor) {
        return cursor == null ? "" : cursor.toString();
    }

    /**
     * @see CursorAdapter#runQueryOnBackgroundThread(CharSequence)
     */
    public Cursor runQueryOnBackgroundThread(CharSequence constraint) {
        if (mFilterQueryProvider != null) {
            return mFilterQueryProvider.runQuery(constraint);
        }

        return mGroupCursorHelper.getCursor();
    }
    
    public Filter getFilter() {
        if (mCursorFilter == null) {
            mCursorFilter = new CursorFilter(this);
        }
        return mCursorFilter;
    }

    /**
     * @see CursorAdapter#getFilterQueryProvider()
     */
    public FilterQueryProvider getFilterQueryProvider() {
        return mFilterQueryProvider;
    }

    /**
     * @see CursorAdapter#setFilterQueryProvider(FilterQueryProvider)
     */
    public void setFilterQueryProvider(FilterQueryProvider filterQueryProvider) {
        mFilterQueryProvider = filterQueryProvider;
    }
    
    /**
     * @see CursorAdapter#changeCursor(Cursor)
     */
    public void changeCursor(Cursor cursor) {
        mGroupCursorHelper.changeCursor(cursor, true);
    }

    /**
     * @see CursorAdapter#getCursor()
     */
    public Cursor getCursor() {
        return mGroupCursorHelper.getCursor();
    }

    /**
     * Helper class for Cursor management:
     * <li> Data validity
     * <li> Funneling the content and data set observers from a Cursor to a
     *      single data set observer for widgets
     * <li> ID from the Cursor for use in adapter IDs
     * <li> Swapping cursors but maintaining other metadata
     */
    class MyCursorHelper {
        private Cursor mCursor;
        private boolean mDataValid;
        private int mRowIDColumn;
        private MyContentObserver mContentObserver;
        private MyDataSetObserver mDataSetObserver;
        
        MyCursorHelper(Cursor cursor) {
            final boolean cursorPresent = cursor != null;
            mCursor = cursor;
            mDataValid = cursorPresent;
            mRowIDColumn = cursorPresent ? cursor.getColumnIndex("_id") : -1;
            mContentObserver = new MyContentObserver();
            mDataSetObserver = new MyDataSetObserver();
            if (cursorPresent) {
                cursor.registerContentObserver(mContentObserver);
                cursor.registerDataSetObserver(mDataSetObserver);
            }
        }
        
        Cursor getCursor() {
            return mCursor;
        }

        int getCount() {
            if (mDataValid && mCursor != null) {
                return mCursor.getCount();
            } else {
                return 0;
            }
        }
        
        long getId(int position) {
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
        
        Cursor moveTo(int position) {
            if (mDataValid && (mCursor != null) && mCursor.moveToPosition(position)) {
                return mCursor;
            } else {
                return null;
            }
        }
        
        void changeCursor(Cursor cursor, boolean releaseCursors) {
            if (cursor == mCursor) return;

            deactivate();
            mCursor = cursor;
            if (cursor != null) {
                cursor.registerContentObserver(mContentObserver);
                cursor.registerDataSetObserver(mDataSetObserver);
                mRowIDColumn = cursor.getColumnIndex("_id");
                mDataValid = true;
                // notify the observers about the new cursor
                notifyDataSetChanged(releaseCursors);
            } else {
                mRowIDColumn = -1;
                mDataValid = false;
                // notify the observers about the lack of a data set
                notifyDataSetInvalidated();
            }
        }

        void deactivate() {
            if (mCursor == null) {
                return;
            }
            
            mCursor.unregisterContentObserver(mContentObserver);
            mCursor.unregisterDataSetObserver(mDataSetObserver);
            mCursor.close();
            mCursor = null;
        }
        
        boolean isValid() {
            return mDataValid && mCursor != null;
        }
        
        private class MyContentObserver extends ContentObserver {
            public MyContentObserver() {
                super(mHandler);
            }

            @Override
            public boolean deliverSelfNotifications() {
                return true;
            }

            @Override
            public void onChange(boolean selfChange) {
                if (mAutoRequery && mCursor != null) {
                    if (false) Log.v("Cursor", "Auto requerying " + mCursor +
                            " due to update");
                    mDataValid = mCursor.requery();
                }
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
}
