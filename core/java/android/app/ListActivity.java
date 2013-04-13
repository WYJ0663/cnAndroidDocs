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

package android.app;

import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListAdapter;
import android.widget.ListView;

/**
 * 用于显示绑定的类似数组、游标等数据源的项目列表的活动，提供了用户选择条目时的事件处理句柄。
 * <p>
 * ListActivity 内嵌了可以绑定到各种不同数据源，通常为数组或保存了检索结果的游标的
 * {@link android.widget.ListView ListView} 对象。下面小节讨论了绑定、屏幕布局、
 * 行布局等内容。
 * <p>
 * <strong>屏幕布局</strong>
 * </p>
 * <p>
 * ListActivity 自带了一个对屏幕居中的全屏列表布局。 你可以通过在 onCreate() 中调用
 * setContentView() 来设置自己的视图布局。 你的布局中必须包含一个 ID 为 "@android:id/list"
 * (代码中为 {@link android.R.id#list}）的 ListView 对象。</p>
 * <p>
 * 可选项，你的自定义视图可以包含另一个任意类型的视图对象，用于在列表视图为空时显示。
 * 这个“空列表”提示的 ID 必须为“android:id/empty”。当空视图存在时，如果没有数据可显示，
 * 列表视图会被隐藏起来。</p>
 * <p>
 * 接下来的代码演示了一个（丑陋的）自定义屏幕布局。
 * 他拥有一个绿色背景的列表，和红色的“没有数据”消息。
 * </p>
 *
 * <pre>
 * &lt;?xml version=&quot;1.0&quot; encoding=&quot;utf-8&quot;?&gt;
 * &lt;LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
 *         android:orientation=&quot;vertical&quot;
 *         android:layout_width=&quot;match_parent&quot;
 *         android:layout_height=&quot;match_parent&quot;
 *         android:paddingLeft=&quot;8dp&quot;
 *         android:paddingRight=&quot;8dp&quot;&gt;
 *
 *     &lt;ListView android:id=&quot;@android:id/list&quot;
 *               android:layout_width=&quot;match_parent&quot;
 *               android:layout_height=&quot;match_parent&quot;
 *               android:background=&quot;#00FF00&quot;
 *               android:layout_weight=&quot;1&quot;
 *               android:drawSelectorOnTop=&quot;false&quot;/&gt;
 *
 *     &lt;TextView android:id=&quot;@android:id/empty&quot;
 *               android:layout_width=&quot;match_parent&quot;
 *               android:layout_height=&quot;match_parent&quot;
 *               android:background=&quot;#FF0000&quot;
 *               android:text=&quot;没有数据&quot;/&gt;
 * &lt;/LinearLayout&gt;
 * </pre>
 *
 * <p>
 * <strong>行布局</strong>
 * </p>
 * <p>
 * 你可以指定列表中个别行的布局。 你可以通过在活动内嵌的 ListAdapter 对象中指定布局资源来实现
 * （ListAdapter 将数据绑定到 ListView；在后面详述）。</p>
 * <p>
 * ListAdapter 的构造方法需要一个用于指定每行的布局资源的参数。
 * 它还有两个附加参数，用于指定那个字段与行布局资源中的那个对象关联。
 * 这两个参数通常是并列数组。
 * </p>
 * <p>
 * Android 提供了一些标准的行布局资源。 它们在  {@link android.R.layout} 类中，名字类似
 * simple_list_item_1、simple_list_item_2、two_line_list_item 等。
 * 接下来的 XML 布局是 two_line_list_item 的源代码，为每个列表项显示了上下两个数据字段。
 * </p>
 *
 * <pre>
 * &lt;?xml version=&quot;1.0&quot; encoding=&quot;utf-8&quot;?&gt;
 * &lt;LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
 *     android:layout_width=&quot;match_parent&quot;
 *     android:layout_height=&quot;wrap_content&quot;
 *     android:orientation=&quot;vertical&quot;&gt;
 *
 *     &lt;TextView android:id=&quot;@+id/text1&quot;
 *         android:textSize=&quot;16sp&quot;
 *         android:textStyle=&quot;bold&quot;
 *         android:layout_width=&quot;match_parent&quot;
 *         android:layout_height=&quot;wrap_content&quot;/&gt;
 *
 *     &lt;TextView android:id=&quot;@+id/text2&quot;
 *         android:textSize=&quot;16sp&quot;
 *         android:layout_width=&quot;match_parent&quot;
 *         android:layout_height=&quot;wrap_content&quot;/&gt;
 * &lt;/LinearLayout&gt;
 * </pre>
 *
 * <p>
 * 你必须在布局中标识绑定到每个 TextView 的数据。下一节讨论其语法。
 * </p>
 * <p>
 * <strong>绑定到数据</strong>
 * </p>
 * <p>
 * 你可以通过实现了{@link android.widget.ListAdapter ListAdapter} 接口的类将 ListActivity
 * 中的 ListView 对象绑定到数据。Android 提供了两个标准的列表适配器：
 * {@link android.widget.SimpleAdapter SimpleAdapter} 用于静态数据（Map）；
 * {@link android.widget.SimpleCursorAdapter SimpleCursorAdapter} 用于代表查询结果的游标。
 * </p>
 * <p>
 * 接下来的代码定制的 ListActivity，演示从联系人提供器中查询所有联系人，使用两行布局将 Name
 * 和 Company 字段绑定到活动的 ListView 上。
 * </p>
 *
 * <pre>
 * public class MyListAdapter extends ListActivity {
 *
 *     &#064;Override
 *     protected void onCreate(Bundle savedInstanceState){
 *         super.onCreate(savedInstanceState);
 *
 *         // We'll define a custom screen layout here (the one shown above), but
 *         // typically, you could just use the standard ListActivity layout.
 *         setContentView(R.layout.custom_list_activity_view);
 *
 *         // Query for all people contacts using the {@link android.provider.Contacts.People} convenience class.
 *         // Put a managed wrapper around the retrieved cursor so we don't have to worry about
 *         // requerying or closing it as the activity changes state.
 *         mCursor = this.getContentResolver().query(People.CONTENT_URI, null, null, null, null);
 *         startManagingCursor(mCursor);
 *
 *         // Now create a new list adapter bound to the cursor.
 *         // SimpleListAdapter is designed for binding to a Cursor.
 *         ListAdapter adapter = new SimpleCursorAdapter(
 *                 this, // Context.
 *                 android.R.layout.two_line_list_item,  // Specify the row template to use (here, two columns bound to the two retrieved cursor
 * rows).
 *                 mCursor,                                              // Pass in the cursor to bind to.
 *                 new String[] {People.NAME, People.COMPANY},           // Array of cursor columns to bind to.
 *                 new int[] {android.R.id.text1, android.R.id.text2});  // Parallel array of which template objects to bind to those columns.
 *
 *         // Bind to our new adapter.
 *         setListAdapter(adapter);
 *     }
 * }
 * </pre>
 *
 * @see #setListAdapter
 * @see android.widget.ListView
 */
public class ListActivity extends Activity {
    /**
     * This field should be made private, so it is hidden from the SDK.
     * {@hide}
     */
    protected ListAdapter mAdapter;
    /**
     * This field should be made private, so it is hidden from the SDK.
     * {@hide}
     */
    protected ListView mList;

    private Handler mHandler = new Handler();
    private boolean mFinishedStart = false;

    private Runnable mRequestFocus = new Runnable() {
        public void run() {
            mList.focusableViewAvailable(mList);
        }
    };

    /**
     * 该方法在选中一个列表项时调用。 子类应该覆盖该方法。 子类要访问关联到选择条目的数据时，
     * 可以调用 getListView().getItemAtPosition(position)。
     *
     * @param l 发生点击的 ListView
     * @param v 发生点击的 ListView 中的视图
     * @param position 视图在列表中的位置
     * @param id 点击条目的数据行 ID
     */
    protected void onListItemClick(ListView l, View v, int position, long id) {
    }

    /**
     * 取保列表视图在活动恢复所有视图状态之前已经创建。
     *
     *@see Activity#onRestoreInstanceState(Bundle)
     */
    @Override
    protected void onRestoreInstanceState(Bundle state) {
        ensureList();
        super.onRestoreInstanceState(state);
    }

    /**
     * @see Activity#onDestroy()
     */
    @Override
    protected void onDestroy() {
        mHandler.removeCallbacks(mRequestFocus);
        super.onDestroy();
    }

    /**
     * 内容变更时更新屏幕状态（当前列表和其他视图）。
     *
     * @see Activity#onContentChanged()
     */
    @Override
    public void onContentChanged() {
        super.onContentChanged();
        View emptyView = findViewById(com.android.internal.R.id.empty);
        mList = (ListView)findViewById(com.android.internal.R.id.list);
        if (mList == null) {
            throw new RuntimeException(
                    "Your content must have a ListView whose id attribute is " +
                    "'android.R.id.list'");
        }
        if (emptyView != null) {
            mList.setEmptyView(emptyView);
        }
        mList.setOnItemClickListener(mOnClickListener);
        if (mFinishedStart) {
            setListAdapter(mAdapter);
        }
        mHandler.post(mRequestFocus);
        mFinishedStart = true;
    }

    /**
     * 为列表视图设置 ListAdapter。
     */
    public void setListAdapter(ListAdapter adapter) {
        synchronized (this) {
            ensureList();
            mAdapter = adapter;
            mList.setAdapter(adapter);
        }
    }

    /**
     * 设置指定位置的适配器数据为当前选中列表项。
     *
     * @param position
     */
    public void setSelection(int position) {
        mList.setSelection(position);
    }

    /**
     * 获取当前选中列表项的位置。
     */
    public int getSelectedItemPosition() {
        return mList.getSelectedItemPosition();
    }

    /**
     * 获取当前选中列表项的游标行 ID。
     */
    public long getSelectedItemId() {
        return mList.getSelectedItemId();
    }

    /**
     * 获取活动的 ListView 小部件。
     */
    public ListView getListView() {
        ensureList();
        return mList;
    }

    /**
     * 获取关联到该活动的 ListView 的 ListAdapter。
     */
    public ListAdapter getListAdapter() {
        return mAdapter;
    }

    private void ensureList() {
        if (mList != null) {
            return;
        }
        setContentView(com.android.internal.R.layout.list_content_simple);

    }

    private AdapterView.OnItemClickListener mOnClickListener = new AdapterView.OnItemClickListener() {
        public void onItemClick(AdapterView<?> parent, View v, int position, long id)
        {
            onListItemClick((ListView)parent, v, position, id);
        }
    };
}
