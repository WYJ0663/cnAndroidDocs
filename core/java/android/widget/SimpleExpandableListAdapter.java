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
import android.view.View;
import android.view.ViewGroup;
import android.view.LayoutInflater;

import java.util.List;
import java.util.Map;

/**
 * 首个List对应子元素中所代表的组，第二个List对应孙子元素在子元素组中的位置.
 * Map亦将支持这样的特殊元素.（子元素嵌套组元素的情况）
 * 将XML文件中定义的静态数据映射到组及其视图的简单的适配器.
 * 你可以用 Map的列表，为组指定其后台数据.每个数组元素对应一个可展开列表的一个组.
 * Maps 包含每行的数据.你还可以指定 XML 文件来定义用于显示组的视图，
 * 并通过 Map 的键值映射到指定的视图.该过程适用于组的子元素.
 * 单级以外的可展开列表的后台数据类型为List&lt;List&lt;Map&gt;&gt;，
 * 第一级列表对应可扩展视图组中的组视图，第二级列表对应组的子组视图，
 * 最后 Map 保持子组视图的子视图的数据.
 */
public class SimpleExpandableListAdapter extends BaseExpandableListAdapter {
    private List<? extends Map<String, ?>> mGroupData;
    private int mExpandedGroupLayout;
    private int mCollapsedGroupLayout;
    private String[] mGroupFrom;
    private int[] mGroupTo;
    
    private List<? extends List<? extends Map<String, ?>>> mChildData;
    private int mChildLayout;
    private int mLastChildLayout;
    private String[] mChildFrom;
    private int[] mChildTo;
    
    private LayoutInflater mInflater;
    
    /**
     * 构造函数
     * 
     * @param context 运行时，与 {@link SimpleExpandableListAdapter}
     *            关联的 {@link ExpandableListView} 的上下文.
     * @param groupData 一个Maps列表.列表中的每个项目对应列表中的一个组.
     *            Map 包含每个组的数据，应该包含“groupFrom”中所有项目.
     * @param groupFrom 用于从与组关联的每个 Map 中取得数据的键值列表.
     * @param groupTo 由“groupFrom”参数指定的用于显示的组视图.他们应该是文本视图.
     *            列表中的视图由“groupFrom”参数提供其显示的值.
     * @param groupLayout 定义组视图的视图布局资源标识.该布局文件应该至少包括
     *            groupTo中所定义的视图.
     * @param childData Map列表的列表.外层列表中的每个项目对应一个组（按组顺序排列）.
     *            内层列表中的每个项目对应一个子组项目（按组顺序排列）.
     *            Map 对应子视图中的数据（按childFrom的顺序排列）.
     *            Map包含每个子视图的数据，应该包含在“childFrom”中指定的虽有条目.
     * @param childFrom 用于从与子视图关联的每个 Map 中取得数据的键值列表.
     * @param childTo 由“childFrom”参数指定的用于显示的子视图.他们应该是文本视图.
     *            列表中的视图由“childFrom”参数提供其显示的值.
     * @param childLayout 定义子视图的视图布局资源标识.该布局文件应该至少包括
     *            childTo中所定义的视图.
     */
    public SimpleExpandableListAdapter(Context context,
            List<? extends Map<String, ?>> groupData, int groupLayout,
            String[] groupFrom, int[] groupTo,
            List<? extends List<? extends Map<String, ?>>> childData,
            int childLayout, String[] childFrom, int[] childTo) {
        this(context, groupData, groupLayout, groupLayout, groupFrom, groupTo, childData,
                childLayout, childLayout, childFrom, childTo);
    }

    /**
     * 构造函数
     * 
     * @param context 运行时，与 {@link SimpleExpandableListAdapter}
     *            关联的 {@link ExpandableListView} 的上下文.
     * @param groupData 一个Maps列表.列表中的每个项目对应列表中的一个组.
     *            Map 包含每个组的数据，应该包含“groupFrom”中所有项目.
     * @param groupFrom 用于从与组关联的每个 Map 中取得数据的键值列表.
     * @param groupTo 由“groupFrom”参数指定的用于显示的组视图.他们应该是文本视图.
     *            列表中的视图由“groupFrom”参数提供其显示的值.
     * @param expandedGroupLayout 定义组展开时视图的XML资源布局.
     *            该布局文件应当至少包括所有在groupTo中所定义的视图.
     * @param collapsedGroupLayout 定义组收起时视图的XML资源布局.
     *            该布局文件应当至少包括所有在groupTo中所定义的视图.
     * @param childData Map列表的列表.外层列表中的每个项目对应一个组（按组顺序排列）.
     *            内层列表中的每个项目对应一个子组项目（按组顺序排列）.
     *            Map 对应子视图中的数据（按childFrom的顺序排列）.
     *            Map包含每个子视图的数据，应该包含在“childFrom”中指定的虽有条目.
     * @param childFrom 用于从与子视图关联的每个 Map 中取得数据的键值列表.
     * @param childTo 由“childFrom”参数指定的用于显示的子视图.他们应该是文本视图.
     *            列表中的视图由“childFrom”参数提供其显示的值.
     * @param childLayout 定义子视图的视图布局资源标识.该布局文件应该至少包括
     *            childTo中所定义的视图.
     */
    public SimpleExpandableListAdapter(Context context,
            List<? extends Map<String, ?>> groupData, int expandedGroupLayout,
            int collapsedGroupLayout, String[] groupFrom, int[] groupTo,
            List<? extends List<? extends Map<String, ?>>> childData,
            int childLayout, String[] childFrom, int[] childTo) {
        this(context, groupData, expandedGroupLayout, collapsedGroupLayout,
                groupFrom, groupTo, childData, childLayout, childLayout,
                childFrom, childTo);
    }

    /**
     * 构造函数
     * 
     * @param context 运行时，与 {@link SimpleExpandableListAdapter}
     *            关联的 {@link ExpandableListView} 的上下文.
     * @param groupData 一个Maps列表.列表中的每个项目对应列表中的一个组.
     *            Map 包含每个组的数据，应该包含“groupFrom”中所有项目.
     * @param groupFrom 用于从与组关联的每个 Map 中取得数据的键值列表.
     * @param groupTo 由“groupFrom”参数指定的用于显示的组视图.他们应该是文本视图.
     *            列表中的视图由“groupFrom”参数提供其显示的值.
     * @param expandedGroupLayout 定义组展开时视图的XML资源布局.
     *            该布局文件应当至少包括所有在groupTo中所定义的视图.
     * @param collapsedGroupLayout 定义组收起时视图的XML资源布局.
     *            该布局文件应当至少包括所有在groupTo中所定义的视图.
     * @param childData Map列表的列表.外层列表中的每个项目对应一个组（按组顺序排列）.
     *            内层列表中的每个项目对应一个子组项目（按组顺序排列）.
     *            Map 对应子视图中的数据（按childFrom的顺序排列）.
     *            Map包含每个子视图的数据，应该包含在“childFrom”中指定的虽有条目.
     * @param childFrom 用于从与子视图关联的每个 Map 中取得数据的键值列表.
     * @param childTo 由“childFrom”参数指定的用于显示的子视图.他们应该是文本视图.
     *            列表中的视图由“childFrom”参数提供其显示的值.
     * @param childLayout 定义子视图的视图布局资源标识
     *            （除了在使用了lastChildLayout时的最后一个子视图）.
     *            该布局文件应该至少包括“childTo”中所定义的视图.
     * @param lastChildLayout 定义每个组中最后一个子视图的资源标识.
     *            布局文件至少应该包含在“childTo”中定义的视图.
     */
    public SimpleExpandableListAdapter(Context context,
            List<? extends Map<String, ?>> groupData, int expandedGroupLayout,
            int collapsedGroupLayout, String[] groupFrom, int[] groupTo,
            List<? extends List<? extends Map<String, ?>>> childData,
            int childLayout, int lastChildLayout, String[] childFrom,
            int[] childTo) {
        mGroupData = groupData;
        mExpandedGroupLayout = expandedGroupLayout;
        mCollapsedGroupLayout = collapsedGroupLayout;
        mGroupFrom = groupFrom;
        mGroupTo = groupTo;
        
        mChildData = childData;
        mChildLayout = childLayout;
        mLastChildLayout = lastChildLayout;
        mChildFrom = childFrom;
        mChildTo = childTo;
        
        mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }
    
    public Object getChild(int groupPosition, int childPosition) {
        return mChildData.get(groupPosition).get(childPosition);
    }

    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }

    public View getChildView(int groupPosition, int childPosition, boolean isLastChild,
            View convertView, ViewGroup parent) {
        View v;
        if (convertView == null) {
            v = newChildView(isLastChild, parent);
        } else {
            v = convertView;
        }
        bindView(v, mChildData.get(groupPosition).get(childPosition), mChildFrom, mChildTo);
        return v;
    }

    /**
     * 为子项目实例化一个新视图.
     * @param isLastChild 该子视图是否是组中的最后一个视图.
     * @param parent 该新视图的父视图.
     * @return 新的子视图.
     */
    public View newChildView(boolean isLastChild, ViewGroup parent) {
        return mInflater.inflate((isLastChild) ? mLastChildLayout : mChildLayout, parent, false);
    }
    
    private void bindView(View view, Map<String, ?> data, String[] from, int[] to) {
        int len = to.length;

        for (int i = 0; i < len; i++) {
            TextView v = (TextView)view.findViewById(to[i]);
            if (v != null) {
                v.setText((String)data.get(from[i]));
            }
        }
    }

    public int getChildrenCount(int groupPosition) {
        return mChildData.get(groupPosition).size();
    }

    public Object getGroup(int groupPosition) {
        return mGroupData.get(groupPosition);
    }

    public int getGroupCount() {
        return mGroupData.size();
    }

    public long getGroupId(int groupPosition) {
        return groupPosition;
    }

    public View getGroupView(int groupPosition, boolean isExpanded, View convertView,
            ViewGroup parent) {
        View v;
        if (convertView == null) {
            v = newGroupView(isExpanded, parent);
        } else {
            v = convertView;
        }
        bindView(v, mGroupData.get(groupPosition), mGroupFrom, mGroupTo);
        return v;
    }

    /**
     * 为组实例化新视图.
     * @param isExpanded 该视图当前是否处于展开状态.
     * @param parent 该新视图的父视图.
     * @return 新的组视图.
     */
    public View newGroupView(boolean isExpanded, ViewGroup parent) {
        return mInflater.inflate((isExpanded) ? mExpandedGroupLayout : mCollapsedGroupLayout,
                parent, false);
    }

    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }

    public boolean hasStableIds() {
        return true;
    }

}
