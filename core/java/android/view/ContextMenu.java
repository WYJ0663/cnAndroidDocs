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

package android.view;

import android.app.Activity;
import android.graphics.drawable.Drawable;
import android.widget.AdapterView;

/**
 * 扩展自Menu的上下文菜单提供了修改上下文菜单头(header)的功能.
 * <p>
 * 上下文菜单不支持菜单项的快捷方式和图标.
 * <p>
 * 在长按时显示上下文菜单，大多数客户应该调用
 * {@link Activity#registerForContextMenu} 方法，并重写
 * {@link Activity#onCreateContextMenu} 方法.
 *
 * <div class="special reference">
 * <h3>开发者指南</h3>
 * <p>关于创建菜单的内容请阅读<a href="{@docRoot}guide/topics/ui/menus.html">菜单开发者指南</a>。</p>
 * </div>
 */
public interface ContextMenu extends Menu {
    /**
     * 将上下文菜单头的标题设为传入参数 <var>titleRes</var> 指定的资源ID.
     * 
     * @param titleRes 作为标题的字符串资源ID.
     * @return 设置标题后的上下文菜单.
     */
    public ContextMenu setHeaderTitle(int titleRes);

    /**
     * 将上下文菜单头的标题设为传入参数 <var>title</var>.
     * 
     * @param title 作为标题的字符串.
     * @return 设置标题后的上下文菜单.
     */
    public ContextMenu setHeaderTitle(CharSequence title);
    
    /**
     * 将上下文菜单头的图标设为传入参数 <var>iconRes</var>中的资源ID代表的图标.
     * 
     * @param iconRes 作为图标的图像的资源ID.
     * @return 设置图标后的上下文菜单.
     */
    public ContextMenu setHeaderIcon(int iconRes);

    /**
     * 将上下文菜单头的图标设为传入参数 <var>icon</var> 代表的图标的
     * {@link Drawable 可绘制对象}.
     * 
     * @param icon 用于显示图标的{@link Drawable 可绘制对象}.
     * @return 设置图标后的上下文菜单.
     */
    public ContextMenu setHeaderIcon(Drawable icon);
    
    /**
     * 将上下文菜单的头视图设置为参数<var>view</var>指定的{@link View 视图}.
     * 该操作替换菜单头的标题和图标（或者被其替换）.
     * 
     * @param view 用于显示菜单头的{@link View 视图}.
     * @return 设置头视图之后的上下文菜单.
     */
    public ContextMenu setHeaderView(View view);
    
    /**
     * 清除上下文菜单的菜单头.
     */
    public void clearHeader();
    
    /**
     * 用于创建上下文菜单的附加信息.例如，在{@link AdapterView}
     * 类中，用其传递启动上下文菜单时条目的确切位置.
     */
    public interface ContextMenuInfo {
    }
}
