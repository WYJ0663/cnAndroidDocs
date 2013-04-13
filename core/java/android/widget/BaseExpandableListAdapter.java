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

import android.database.DataSetObservable;
import android.database.DataSetObserver;

/**
 * 用于根据数据为可扩展列表视图提供数据和视图的 {@link ExpandableListAdapter}
 * 的基类.
 * <p>
 * 继承该类的适配器应该确保基类实现的 {@link #getCombinedChildId(long, long)}
 * 和 {@link #getCombinedGroupId(long)} 方法，可以根据分组 ID 或子条目 ID
 * 来生成唯一的 ID.
 * <p>
 * @see SimpleExpandableListAdapter
 * @see SimpleCursorTreeAdapter
 */
public abstract class BaseExpandableListAdapter implements ExpandableListAdapter, 
        HeterogeneousExpandableList {
    private final DataSetObservable mDataSetObservable = new DataSetObservable();
    
    public void registerDataSetObserver(DataSetObserver observer) {
        mDataSetObservable.registerObserver(observer);
    }

    public void unregisterDataSetObserver(DataSetObserver observer) {
        mDataSetObservable.unregisterObserver(observer);
    }
    
    /**
     * @see DataSetObservable#notifyInvalidated()
     */
    public void notifyDataSetInvalidated() {
        mDataSetObservable.notifyInvalidated();
    }
    
    /**
     * @see DataSetObservable#notifyChanged()
     */
    public void notifyDataSetChanged() {
        mDataSetObservable.notifyChanged();
    }

    public boolean areAllItemsEnabled() {
        return true;
    }

    public void onGroupCollapsed(int groupPosition) {
    }

    public void onGroupExpanded(int groupPosition) {
    }

    /**
     * 如果预知到使用如下规则会发生冲突，请覆盖该方法：
     * <p>
     * 基类实现返回长整型值：
     * <li> 第0位：指明该 ID 是子条目（不置位）还是分组（置位），对于该方法，该位为 0.
     * <li> 第1-31位：分组ID的低31位.
     * <li> 第32-63位：子条目ID的低32位.
     * <p> 
     * {@inheritDoc}
     */
    public long getCombinedChildId(long groupId, long childId) {
        return 0x8000000000000000L | ((groupId & 0x7FFFFFFF) << 32) | (childId & 0xFFFFFFFF);
    }

    /**
     * 如果预知到使用如下规则会发生冲突，请覆盖该方法：
     * <p>
     * 基类实现返回长整型值：
     * <li> 第0位：指明该 ID 是子条目（不置位）还是分组（置位），对于该方法，该位为 1.
     * <li> 第1-31位：分组ID的低31位.
     * <li> 第32-63位：子条目ID的低32位.
     * <p> 
     * {@inheritDoc}
     */
    public long getCombinedGroupId(long groupId) {
        return (groupId & 0x7FFFFFFF) << 32;
    }

    /**
     * {@inheritDoc}
     */
    public boolean isEmpty() {
        return getGroupCount() == 0;
    }


    /**
     * {@inheritDoc}
     * @return 因为只定义了一个子类型，因此对于分组的子条目均返回0.
     */
    public int getChildType(int groupPosition, int childPosition) {
        return 0;
    }

    /**
     * {@inheritDoc}
     * @return BaseExpandableListAdapter 类默认返回 1.
     */
    public int getChildTypeCount() {
        return 1;
    }

    /**
     * {@inheritDoc}
     * @return 因为只定义了一个分组类型，因此对于任何分组均返回0.
     */
    public int getGroupType(int groupPosition) {
        return 0;
    }

    /**
     * {@inheritDoc}
     * @return BaseExpandableListAdapter 类默认返回 1.
     */
    public int getGroupTypeCount() {
        return 1;
    }
}
