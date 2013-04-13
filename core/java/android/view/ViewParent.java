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

package android.view;

import android.graphics.Rect;
import android.view.accessibility.AccessibilityEvent;

/**
 * 定义了作为父视图应有功能的类. 是视图与父视图交互的接口.
 */
public interface ViewParent {
    /**
     * 当某些变更导致该父视图的子视图的布局失效时调用该方法.该方法按照视图树的顺序调用.
     */
    public void requestLayout();

    /**
     * 指出该父视图是否请求了布局操作.
     *
     * @return 如果请求了布局，返回真；否则返回假.
     */
    public boolean isLayoutRequested();

    /**
     * 当子视图需要收集视图层次中透明区域并报告给窗口排版组件时调用.
     * 需要在视图层次中“打洞”的视图，比如SurfaceView可以利用该API
     * 来提高系统性能.当视图层次中没有这样的视图时，不需要该优化，
     * 使用它会稍微降低一些视图层次的性能.
     * 
     * @param child 请求计算透明区域的视图.
     * 
     */
    public void requestTransparentRegion(View child);

    /**
     * 需要重绘子视图的部分或全部区域.
     * 
     * @param child 需要重绘的视图.
     * @param r 子视图需要重绘的区域.
     */
    public void invalidateChild(View child, Rect r);

    /**
     * 需要重绘子视图的部分或全部区域.
     *
     * <p>位置数组中分别保存了待绘制子视图的左上位置的整型数组。</p>
     *
     * <p>如果指定的区域的父视图被设为无效，则返回父视图；如果指定矩形不会导致父视图无效，
     * 或者不存在父视图，该方法返回空。</p>
     *
     * <p>当返回非空值时，必须将位置数组中的值更新为本ViewParent的左上坐标。</p>
     *
     * @param location 包含设置无效区域的子视图左上坐标的双元素整型数组.
     * @param r 子视图中设为无效的区域.
     *
     * @return 该ViewParent的父视图或空.
     */
    public ViewParent invalidateChildInParent(int[] location, Rect r);

    /**
     * 如果存在父视图，返回真，否则返回假.
     *
     * @return 父视图或者在没有父视图时返回空.
     */
    public ViewParent getParent();

    /**
     * 当父视图的子视图请求获得焦点时，调用此方法.
     * 
     * @param child 请求获得焦点的子视图.此视图将包含具有焦点视图，但其本身不一定具有焦点.
     * @param focused 事实上拥有焦点的子视图，他可能是 child 的下层视图.
     */
    public void requestChildFocus(View child, View focused);

    /**
     * 告诉视图层次，全局视图属性需要重新评价.
     * 
     * @param child 属性变更的视图.
     */
    public void recomputeViewAttributes(View child);
    
    /**
     * 当该视图的子视图需要放弃焦点时调用.
     * 
     * @param child 放弃焦点的视图.
     */
    public void clearChildFocus(View child);

    /**
     * Compute the visible part of a rectangular region defined in terms of a child view's
     * coordinates.
     *
     * <p>Returns the clipped visible part of the rectangle <code>r</code>, defined in the
     * <code>child</code>'s local coordinate system. <code>r</code> is modified by this method to
     * contain the result, expressed in the global (root) coordinate system.</p>
     *
     * <p>The resulting rectangle is always axis aligned. If a rotation is applied to a node in the
     * View hierarchy, the result is the axis-aligned bounding box of the visible rectangle.</p>
     *
     * @param child A child View, whose rectangular visible region we want to compute
     * @param r The input rectangle, defined in the child coordinate system. Will be overwritten to
     * contain the resulting visible rectangle, expressed in global (root) coordinates
     * @param offset The input coordinates of a point, defined in the child coordinate system.
     * As with the <code>r</code> parameter, this will be overwritten to contain the global (root)
     * coordinates of that point.
     * A <code>null</code> value is valid (in case you are not interested in this result)
     * @return true if the resulting rectangle is not empty, false otherwise
     */
    public boolean getChildVisibleRect(View child, Rect r, android.graphics.Point offset);

    /**
     * 在指定的方向找到最近的可以获得焦点的视图.
     * 
     * @param v 当前具有焦点的视图.
     * @param direction FOCUS_UP、FOCUS_DOWN、FOCUS_LEFT、FOCUS_RIGHT之一.
     */
    public View focusSearch(View v, int direction);

    /**
     * 改变子视图的前后顺序，将其移动到所有视图的最前面.
     * 
     * @param child
     */
    public void bringChildToFront(View child);

    /**
     * 告诉父视图，一个新的可得焦点视图可用了.该方法用于处理，
     * 从没有的可焦点的视图，到出现第一个可得焦点视图时的转变.
     * 
     * @param v 新的可得焦点视图.
     */
    public void focusableViewAvailable(View v);

    /**
     * 为指定的视图或者其父类显示上下文菜单.
     * <p>
     * 大部分情况下，子类不需要重写该方法.但是，如果直接将子类添加到窗口管理器（例如：使用
     * {@link ViewManager#addView(View, android.view.ViewGroup.LayoutParams)}
     * 函数），此时就需要重写来显示上下文菜单.</p>
     * 
     * @param originalView 首先显示的上下文菜单的原始视图.
     * @return 如果显示了上下文菜单返回真.
     */
    public boolean showContextMenuForChild(View originalView);

    /**
     * 通知父类，如果有必要可以向指定的上下文菜单中添加菜单项
     * （递归通知其父类）.
     * 
     * @param menu 被填充的菜单.
     */
    public void createContextMenu(ContextMenu menu);

    /**
     * 为指定的视图启动动作模式.
     * <p>
     * 大多数情况子类不需要覆盖该方法。如果子类直接向视图管理器添加了视图（比如执行了
     * {@link ViewManager#addView(View, android.view.ViewGroup.LayoutParams)}），
     * 那么就应该覆盖该方法，启动动作模式。</p>
     *
     * @param originalView 首次调用动作模式的原始视图
     * @param callback 用于处理动作模式生命周期事件的回调函数
     * @return 如果创建了新的动作模式则返回之，否则为空。
     */
    public ActionMode startActionModeForChild(View originalView, ActionMode.Callback callback);

    /**
     * 当子视图的可绘制对象状态发生改变时调用该方法.
     *
     * @param child 可绘制对象发生改变的子视图.
     */
    public void childDrawableStateChanged(View child);
    
    /**
     * 当子视图不希望他的父类及其祖先使用
     * {@link ViewGroup#onInterceptTouchEvent(MotionEvent)}
     * 打断触控事件时调用.
     * <p>
     * 父视图应该调用其父类的该方法.父类必须在触控事件期间遵守该请求，
     * 就是说，父类只有在收到抬起事件或取消事件时才可以清楚该标志.</p>
     * 
     * @param disallowIntercept 如果子视图不希望父类打断触控事件，设为真.
     */
    public void requestDisallowInterceptTouchEvent(boolean disallowIntercept);
    
    /**
     * 当视图组里的某个子视图需要定位到屏幕上的特定矩形区域时，调用此方法.
     * {@link ViewGroup} 重写此方法时可以认为：
     * <ul>
     *   <li>child 是该视图的直接子视图.</li>
     *   <li>rectangle 使用子视图的坐标系.</li>
     * </ul>
     *
     * <p>{@link ViewGroup}重写此方法时应该遵守如下约定：</p>
     * <ul>
     *   <li>如果矩形可见，不做任何变更.</li>
     *   <li>视窗滚动到矩形可见即可.</li>
     * <ul>
     *
     * @param child 发出请求的直接子视图.
     * @param rectangle 子视图希望显示在屏幕上的、基于子视图坐标系的矩形.
     * @param immediate 设为真时，禁止动画形式或延迟的滚动；设为假时不禁止.
     * @return 该方法是否滚动了屏幕.
     */
    public boolean requestChildRectangleOnScreen(View child, Rect rectangle,
            boolean immediate);

    /**
     * 由子类调用，向父类请求发送一个 {@link AccessibilityEvent} 事件.
     * 子类已经为自己在事件中填写了记录，请求父类发送该事件。父类可以选择为自己添加记录。
     * <p>
     * 注意：辅助事件发生自把自己的状态填入记录的单独的视图，请求其父类来执行发送工作。
     *      父类可以选择在将事件请求发送给其父类时，添加自身的记录到事件中。
     *      父类也可以选择忽略发送事件的请求。辅助事件由视图树中最顶层的视图发送。</p>
     *
     * @param child 请求发送事件的子视图
     * @param event 要发送的事件
     * @return 如果发送了事件则返回真。
     */
    public boolean requestSendAccessibilityEvent(View child, AccessibilityEvent event);

    /**
     * Called when a child view now has or no longer is tracking transient state.
     *
     * @param child Child view whose state has changed
     * @param hasTransientState true if this child has transient state
     *
     * @hide
     */
    public void childHasTransientStateChanged(View child, boolean hasTransientState);

    /**
     * Ask that a new dispatch of {@link View#fitSystemWindows(Rect)
     * View.fitSystemWindows(Rect)} be performed.
     */
    public void requestFitSystemWindows();

    /**
     * Gets the parent of a given View for accessibility. Since some Views are not
     * exposed to the accessibility layer the parent for accessibility is not
     * necessarily the direct parent of the View, rather it is a predecessor.
     *
     * @return The parent or <code>null</code> if no such is found.
     */
    public ViewParent getParentForAccessibility();

    /**
     * A child notifies its parent that its state for accessibility has changed.
     * That is some of the child properties reported to accessibility services has
     * changed, hence the interested services have to be notified for the new state.
     *
     * @hide
     */
    public void childAccessibilityStateChanged(View child);
}
