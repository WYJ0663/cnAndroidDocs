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

import android.app.ActivityOptions;
import android.app.PendingIntent;
import android.appwidget.AppWidgetHostView;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.UserHandle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.LayoutInflater.Filter;
import android.view.RemotableViewMethod;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView.OnItemClickListener;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;


/**
 * 描述了可以在其他进程中显示的视图层次的类. 视图层次来自布局资源文件，
 * 该类提供了一些用于编辑提取到的视图层次的基本操作.
 */
public class RemoteViews implements Parcelable, Filter {

    private static final String LOG_TAG = "RemoteViews";

    /**
     * The intent extra that contains the appWidgetId.
     * @hide
     */
    static final String EXTRA_REMOTEADAPTER_APPWIDGET_ID = "remoteAdapterAppWidgetId";

    /**
     * User that these views should be applied as. Requires
     * {@link android.Manifest.permission#INTERACT_ACROSS_USERS_FULL} when
     * crossing user boundaries.
     */
    private UserHandle mUser = android.os.Process.myUserHandle();

    /**
     * The package name of the package containing the layout
     * resource. (Added to the parcel)
     */
    private final String mPackage;

    /**
     * The resource ID of the layout file. (Added to the parcel)
     */
    private final int mLayoutId;

    /**
     * An array of actions to perform on the view tree once it has been
     * inflated
     */
    private ArrayList<Action> mActions;

    /**
     * A class to keep track of memory usage by this RemoteViews
     */
    private MemoryUsageCounter mMemoryUsageCounter;

    /**
     * Maps bitmaps to unique indicies to avoid Bitmap duplication.
     */
    private BitmapCache mBitmapCache;

    /**
     * Indicates whether or not this RemoteViews object is contained as a child of any other
     * RemoteViews.
     */
    private boolean mIsRoot = true;

    /**
     * Constants to whether or not this RemoteViews is composed of a landscape and portrait
     * RemoteViews.
     */
    private static final int MODE_NORMAL = 0;
    private static final int MODE_HAS_LANDSCAPE_AND_PORTRAIT = 1;

    /**
     * Used in conjunction with the special constructor
     * {@link #RemoteViews(RemoteViews, RemoteViews)} to keep track of the landscape and portrait
     * RemoteViews.
     */
    private RemoteViews mLandscape = null;
    private RemoteViews mPortrait = null;

    /**
     * This flag indicates whether this RemoteViews object is being created from a
     * RemoteViewsService for use as a child of a widget collection. This flag is used
     * to determine whether or not certain features are available, in particular,
     * setting on click extras and setting on click pending intents. The former is enabled,
     * and the latter disabled when this flag is true.
     */
    private boolean mIsWidgetCollectionChild = false;

    private static final OnClickHandler DEFAULT_ON_CLICK_HANDLER = new OnClickHandler();

    /**
     * 该接口指明，视图的子类允许使用 {@link RemoteViews} 机制.
     */
    @Target({ ElementType.TYPE })
    @Retention(RetentionPolicy.RUNTIME)
    public @interface RemoteView {
    }

    /**
     * 执行某些动作发生错误时抛出的异常.
     */
    public static class ActionException extends RuntimeException {
        public ActionException(Exception ex) {
            super(ex);
        }
        public ActionException(String message) {
            super(message);
        }
    }

    /** @hide */
    public static class OnClickHandler {
        public boolean onClickHandler(View view, PendingIntent pendingIntent,
                Intent fillInIntent) {
            try {
                // TODO: Unregister this handler if PendingIntent.FLAG_ONE_SHOT?
                Context context = view.getContext();
                ActivityOptions opts = ActivityOptions.makeScaleUpAnimation(view,
                        0, 0,
                        view.getMeasuredWidth(), view.getMeasuredHeight());
                context.startIntentSender(
                        pendingIntent.getIntentSender(), fillInIntent,
                        Intent.FLAG_ACTIVITY_NEW_TASK,
                        Intent.FLAG_ACTIVITY_NEW_TASK, 0, opts.toBundle());
            } catch (IntentSender.SendIntentException e) {
                android.util.Log.e(LOG_TAG, "Cannot send pending intent: ", e);
                return false;
            } catch (Exception e) {
                android.util.Log.e(LOG_TAG, "Cannot send pending intent due to " +
                        "unknown exception: ", e);
                return false;
            }
            return true;
        }
    }

    /**
     * Base class for all actions that can be performed on an
     * inflated view.
     *
     *  SUBCLASSES MUST BE IMMUTABLE SO CLONE WORKS!!!!!
     */
    private abstract static class Action implements Parcelable {
        public abstract void apply(View root, ViewGroup rootParent,
                OnClickHandler handler) throws ActionException;

        public static final int MERGE_REPLACE = 0;
        public static final int MERGE_APPEND = 1;
        public static final int MERGE_IGNORE = 2;

        public int describeContents() {
            return 0;
        }

        /**
         * Overridden by each class to report on it's own memory usage
         */
        public void updateMemoryUsageEstimate(MemoryUsageCounter counter) {
            // We currently only calculate Bitmap memory usage, so by default, don't do anything
            // here
            return;
        }

        public void setBitmapCache(BitmapCache bitmapCache) {
            // Do nothing
        }

        public int mergeBehavior() {
            return MERGE_REPLACE;
        }

        public abstract String getActionName();

        public String getUniqueKey() {
            return (getActionName() + viewId);
        }

        int viewId;
    }

    /**
     * Merges the passed RemoteViews actions with this RemoteViews actions according to
     * action-specific merge rules.
     * 
     * @param newRv
     * 
     * @hide
     */
    public void mergeRemoteViews(RemoteViews newRv) {
        if (newRv == null) return;
        // We first copy the new RemoteViews, as the process of merging modifies the way the actions
        // reference the bitmap cache. We don't want to modify the object as it may need to
        // be merged and applied multiple times.
        RemoteViews copy = newRv.clone();

        HashMap<String, Action> map = new HashMap<String, Action>();
        if (mActions == null) {
            mActions = new ArrayList<Action>();
        }

        int count = mActions.size();
        for (int i = 0; i < count; i++) {
            Action a = mActions.get(i);
            map.put(a.getUniqueKey(), a);
        }

        ArrayList<Action> newActions = copy.mActions;
        if (newActions == null) return;
        count = newActions.size();
        for (int i = 0; i < count; i++) {
            Action a = newActions.get(i);
            String key = newActions.get(i).getUniqueKey();
            int mergeBehavior = newActions.get(i).mergeBehavior();
            if (map.containsKey(key) && mergeBehavior == Action.MERGE_REPLACE) {
                mActions.remove(map.get(key));
                map.remove(key);
            }

            // If the merge behavior is ignore, we don't bother keeping the extra action
            if (mergeBehavior == Action.MERGE_REPLACE || mergeBehavior == Action.MERGE_APPEND) {
                mActions.add(a);
            }
        }

        // Because pruning can remove the need for bitmaps, we reconstruct the bitmap cache
        mBitmapCache = new BitmapCache();
        setBitmapCache(mBitmapCache);
    }

    private class SetEmptyView extends Action {
        int viewId;
        int emptyViewId;

        public final static int TAG = 6;

        SetEmptyView(int viewId, int emptyViewId) {
            this.viewId = viewId;
            this.emptyViewId = emptyViewId;
        }

        SetEmptyView(Parcel in) {
            this.viewId = in.readInt();
            this.emptyViewId = in.readInt();
        }

        public void writeToParcel(Parcel out, int flags) {
            out.writeInt(TAG);
            out.writeInt(this.viewId);
            out.writeInt(this.emptyViewId);
        }

        @Override
        public void apply(View root, ViewGroup rootParent, OnClickHandler handler) {
            final View view = root.findViewById(viewId);
            if (!(view instanceof AdapterView<?>)) return;

            AdapterView<?> adapterView = (AdapterView<?>) view;

            final View emptyView = root.findViewById(emptyViewId);
            if (emptyView == null) return;

            adapterView.setEmptyView(emptyView);
        }

        public String getActionName() {
            return "SetEmptyView";
        }
    }

    private class SetOnClickFillInIntent extends Action {
        public SetOnClickFillInIntent(int id, Intent fillInIntent) {
            this.viewId = id;
            this.fillInIntent = fillInIntent;
        }

        public SetOnClickFillInIntent(Parcel parcel) {
            viewId = parcel.readInt();
            fillInIntent = Intent.CREATOR.createFromParcel(parcel);
        }

        public void writeToParcel(Parcel dest, int flags) {
            dest.writeInt(TAG);
            dest.writeInt(viewId);
            fillInIntent.writeToParcel(dest, 0 /* no flags */);
        }

        @Override
        public void apply(View root, ViewGroup rootParent, final OnClickHandler handler) {
            final View target = root.findViewById(viewId);
            if (target == null) return;

            if (!mIsWidgetCollectionChild) {
                Log.e("RemoteViews", "The method setOnClickFillInIntent is available " +
                        "only from RemoteViewsFactory (ie. on collection items).");
                return;
            }
            if (target == root) {
                target.setTagInternal(com.android.internal.R.id.fillInIntent, fillInIntent);
            } else if (target != null && fillInIntent != null) {
                OnClickListener listener = new OnClickListener() {
                    public void onClick(View v) {
                        // Insure that this view is a child of an AdapterView
                        View parent = (View) v.getParent();
                        while (parent != null && !(parent instanceof AdapterView<?>)
                                && !(parent instanceof AppWidgetHostView)) {
                            parent = (View) parent.getParent();
                        }

                        if (parent instanceof AppWidgetHostView || parent == null) {
                            // Somehow they've managed to get this far without having
                            // and AdapterView as a parent.
                            Log.e("RemoteViews", "Collection item doesn't have AdapterView parent");
                            return;
                        }

                        // Insure that a template pending intent has been set on an ancestor
                        if (!(parent.getTag() instanceof PendingIntent)) {
                            Log.e("RemoteViews", "Attempting setOnClickFillInIntent without" +
                                    " calling setPendingIntentTemplate on parent.");
                            return;
                        }

                        PendingIntent pendingIntent = (PendingIntent) parent.getTag();

                        final float appScale = v.getContext().getResources()
                                .getCompatibilityInfo().applicationScale;
                        final int[] pos = new int[2];
                        v.getLocationOnScreen(pos);

                        final Rect rect = new Rect();
                        rect.left = (int) (pos[0] * appScale + 0.5f);
                        rect.top = (int) (pos[1] * appScale + 0.5f);
                        rect.right = (int) ((pos[0] + v.getWidth()) * appScale + 0.5f);
                        rect.bottom = (int) ((pos[1] + v.getHeight()) * appScale + 0.5f);

                        fillInIntent.setSourceBounds(rect);
                        handler.onClickHandler(v, pendingIntent, fillInIntent);
                    }

                };
                target.setOnClickListener(listener);
            }
        }

        public String getActionName() {
            return "SetOnClickFillInIntent";
        }

        Intent fillInIntent;

        public final static int TAG = 9;
    }

    private class SetPendingIntentTemplate extends Action {
        public SetPendingIntentTemplate(int id, PendingIntent pendingIntentTemplate) {
            this.viewId = id;
            this.pendingIntentTemplate = pendingIntentTemplate;
        }

        public SetPendingIntentTemplate(Parcel parcel) {
            viewId = parcel.readInt();
            pendingIntentTemplate = PendingIntent.readPendingIntentOrNullFromParcel(parcel);
        }

        public void writeToParcel(Parcel dest, int flags) {
            dest.writeInt(TAG);
            dest.writeInt(viewId);
            pendingIntentTemplate.writeToParcel(dest, 0 /* no flags */);
        }

        @Override
        public void apply(View root, ViewGroup rootParent, final OnClickHandler handler) {
            final View target = root.findViewById(viewId);
            if (target == null) return;

            // If the view isn't an AdapterView, setting a PendingIntent template doesn't make sense
            if (target instanceof AdapterView<?>) {
                AdapterView<?> av = (AdapterView<?>) target;
                // The PendingIntent template is stored in the view's tag.
                OnItemClickListener listener = new OnItemClickListener() {
                    public void onItemClick(AdapterView<?> parent, View view,
                            int position, long id) {
                        // The view should be a frame layout
                        if (view instanceof ViewGroup) {
                            ViewGroup vg = (ViewGroup) view;

                            // AdapterViews contain their children in a frame
                            // so we need to go one layer deeper here.
                            if (parent instanceof AdapterViewAnimator) {
                                vg = (ViewGroup) vg.getChildAt(0);
                            }
                            if (vg == null) return;

                            Intent fillInIntent = null;
                            int childCount = vg.getChildCount();
                            for (int i = 0; i < childCount; i++) {
                                Object tag = vg.getChildAt(i).getTag(com.android.internal.R.id.fillInIntent);
                                if (tag instanceof Intent) {
                                    fillInIntent = (Intent) tag;
                                    break;
                                }
                            }
                            if (fillInIntent == null) return;

                            final float appScale = view.getContext().getResources()
                                    .getCompatibilityInfo().applicationScale;
                            final int[] pos = new int[2];
                            view.getLocationOnScreen(pos);

                            final Rect rect = new Rect();
                            rect.left = (int) (pos[0] * appScale + 0.5f);
                            rect.top = (int) (pos[1] * appScale + 0.5f);
                            rect.right = (int) ((pos[0] + view.getWidth()) * appScale + 0.5f);
                            rect.bottom = (int) ((pos[1] + view.getHeight()) * appScale + 0.5f);

                            final Intent intent = new Intent();
                            intent.setSourceBounds(rect);
                            handler.onClickHandler(view, pendingIntentTemplate, fillInIntent);
                        }
                    }
                };
                av.setOnItemClickListener(listener);
                av.setTag(pendingIntentTemplate);
            } else {
                Log.e("RemoteViews", "Cannot setPendingIntentTemplate on a view which is not" +
                        "an AdapterView (id: " + viewId + ")");
                return;
            }
        }

        public String getActionName() {
            return "SetPendingIntentTemplate";
        }

        PendingIntent pendingIntentTemplate;

        public final static int TAG = 8;
    }

    private class SetRemoteViewsAdapterIntent extends Action {
        public SetRemoteViewsAdapterIntent(int id, Intent intent) {
            this.viewId = id;
            this.intent = intent;
        }

        public SetRemoteViewsAdapterIntent(Parcel parcel) {
            viewId = parcel.readInt();
            intent = Intent.CREATOR.createFromParcel(parcel);
        }

        public void writeToParcel(Parcel dest, int flags) {
            dest.writeInt(TAG);
            dest.writeInt(viewId);
            intent.writeToParcel(dest, flags);
        }

        @Override
        public void apply(View root, ViewGroup rootParent, OnClickHandler handler) {
            final View target = root.findViewById(viewId);
            if (target == null) return;

            // Ensure that we are applying to an AppWidget root
            if (!(rootParent instanceof AppWidgetHostView)) {
                Log.e("RemoteViews", "SetRemoteViewsAdapterIntent action can only be used for " +
                        "AppWidgets (root id: " + viewId + ")");
                return;
            }
            // Ensure that we are calling setRemoteAdapter on an AdapterView that supports it
            if (!(target instanceof AbsListView) && !(target instanceof AdapterViewAnimator)) {
                Log.e("RemoteViews", "Cannot setRemoteViewsAdapter on a view which is not " +
                        "an AbsListView or AdapterViewAnimator (id: " + viewId + ")");
                return;
            }

            // Embed the AppWidget Id for use in RemoteViewsAdapter when connecting to the intent
            // RemoteViewsService
            AppWidgetHostView host = (AppWidgetHostView) rootParent;
            intent.putExtra(EXTRA_REMOTEADAPTER_APPWIDGET_ID, host.getAppWidgetId());
            if (target instanceof AbsListView) {
                AbsListView v = (AbsListView) target;
                v.setRemoteViewsAdapter(intent);
                v.setRemoteViewsOnClickHandler(handler);
            } else if (target instanceof AdapterViewAnimator) {
                AdapterViewAnimator v = (AdapterViewAnimator) target;
                v.setRemoteViewsAdapter(intent);
                v.setRemoteViewsOnClickHandler(handler);
            }
        }

        public String getActionName() {
            return "SetRemoteViewsAdapterIntent";
        }

        Intent intent;

        public final static int TAG = 10;
    }

    /**
     * Equivalent to calling
     * {@link android.view.View#setOnClickListener(android.view.View.OnClickListener)}
     * to launch the provided {@link PendingIntent}.
     */
    private class SetOnClickPendingIntent extends Action {
        public SetOnClickPendingIntent(int id, PendingIntent pendingIntent) {
            this.viewId = id;
            this.pendingIntent = pendingIntent;
        }

        public SetOnClickPendingIntent(Parcel parcel) {
            viewId = parcel.readInt();

            // We check a flag to determine if the parcel contains a PendingIntent.
            if (parcel.readInt() != 0) {
                pendingIntent = PendingIntent.readPendingIntentOrNullFromParcel(parcel);
            }
        }

        public void writeToParcel(Parcel dest, int flags) {
            dest.writeInt(TAG);
            dest.writeInt(viewId);

            // We use a flag to indicate whether the parcel contains a valid object.
            dest.writeInt(pendingIntent != null ? 1 : 0);
            if (pendingIntent != null) {
                pendingIntent.writeToParcel(dest, 0 /* no flags */);
            }
        }

        @Override
        public void apply(View root, ViewGroup rootParent, final OnClickHandler handler) {
            final View target = root.findViewById(viewId);
            if (target == null) return;

            // If the view is an AdapterView, setting a PendingIntent on click doesn't make much
            // sense, do they mean to set a PendingIntent template for the AdapterView's children?
            if (mIsWidgetCollectionChild) {
                Log.w("RemoteViews", "Cannot setOnClickPendingIntent for collection item " +
                        "(id: " + viewId + ")");
                ApplicationInfo appInfo = root.getContext().getApplicationInfo();

                // We let this slide for HC and ICS so as to not break compatibility. It should have
                // been disabled from the outset, but was left open by accident.
                if (appInfo != null &&
                        appInfo.targetSdkVersion >= Build.VERSION_CODES.JELLY_BEAN) {
                    return;
                }
            }

            if (target != null) {
                // If the pendingIntent is null, we clear the onClickListener
                OnClickListener listener = null;
                if (pendingIntent != null) {
                    listener = new OnClickListener() {
                        public void onClick(View v) {
                            // Find target view location in screen coordinates and
                            // fill into PendingIntent before sending.
                            final float appScale = v.getContext().getResources()
                                    .getCompatibilityInfo().applicationScale;
                            final int[] pos = new int[2];
                            v.getLocationOnScreen(pos);

                            final Rect rect = new Rect();
                            rect.left = (int) (pos[0] * appScale + 0.5f);
                            rect.top = (int) (pos[1] * appScale + 0.5f);
                            rect.right = (int) ((pos[0] + v.getWidth()) * appScale + 0.5f);
                            rect.bottom = (int) ((pos[1] + v.getHeight()) * appScale + 0.5f);

                            final Intent intent = new Intent();
                            intent.setSourceBounds(rect);
                            handler.onClickHandler(v, pendingIntent, intent);
                        }
                    };
                }
                target.setOnClickListener(listener);
            }
        }

        public String getActionName() {
            return "SetOnClickPendingIntent";
        }

        PendingIntent pendingIntent;

        public final static int TAG = 1;
    }

    /**
     * Equivalent to calling a combination of {@link Drawable#setAlpha(int)},
     * {@link Drawable#setColorFilter(int, android.graphics.PorterDuff.Mode)},
     * and/or {@link Drawable#setLevel(int)} on the {@link Drawable} of a given view.
     * <p>
     * These operations will be performed on the {@link Drawable} returned by the
     * target {@link View#getBackground()} by default.  If targetBackground is false,
     * we assume the target is an {@link ImageView} and try applying the operations
     * to {@link ImageView#getDrawable()}.
     * <p>
     * You can omit specific calls by marking their values with null or -1.
     */
    private class SetDrawableParameters extends Action {
        public SetDrawableParameters(int id, boolean targetBackground, int alpha,
                int colorFilter, PorterDuff.Mode mode, int level) {
            this.viewId = id;
            this.targetBackground = targetBackground;
            this.alpha = alpha;
            this.colorFilter = colorFilter;
            this.filterMode = mode;
            this.level = level;
        }

        public SetDrawableParameters(Parcel parcel) {
            viewId = parcel.readInt();
            targetBackground = parcel.readInt() != 0;
            alpha = parcel.readInt();
            colorFilter = parcel.readInt();
            boolean hasMode = parcel.readInt() != 0;
            if (hasMode) {
                filterMode = PorterDuff.Mode.valueOf(parcel.readString());
            } else {
                filterMode = null;
            }
            level = parcel.readInt();
        }

        public void writeToParcel(Parcel dest, int flags) {
            dest.writeInt(TAG);
            dest.writeInt(viewId);
            dest.writeInt(targetBackground ? 1 : 0);
            dest.writeInt(alpha);
            dest.writeInt(colorFilter);
            if (filterMode != null) {
                dest.writeInt(1);
                dest.writeString(filterMode.toString());
            } else {
                dest.writeInt(0);
            }
            dest.writeInt(level);
        }

        @Override
        public void apply(View root, ViewGroup rootParent, OnClickHandler handler) {
            final View target = root.findViewById(viewId);
            if (target == null) return;

            // Pick the correct drawable to modify for this view
            Drawable targetDrawable = null;
            if (targetBackground) {
                targetDrawable = target.getBackground();
            } else if (target instanceof ImageView) {
                ImageView imageView = (ImageView) target;
                targetDrawable = imageView.getDrawable();
            }

            if (targetDrawable != null) {
                // Perform modifications only if values are set correctly
                if (alpha != -1) {
                    targetDrawable.setAlpha(alpha);
                }
                if (colorFilter != -1 && filterMode != null) {
                    targetDrawable.setColorFilter(colorFilter, filterMode);
                }
                if (level != -1) {
                    targetDrawable.setLevel(level);
                }
            }
        }

        public String getActionName() {
            return "SetDrawableParameters";
        }

        boolean targetBackground;
        int alpha;
        int colorFilter;
        PorterDuff.Mode filterMode;
        int level;

        public final static int TAG = 3;
    }

    private class ReflectionActionWithoutParams extends Action {
        String methodName;

        public final static int TAG = 5;

        ReflectionActionWithoutParams(int viewId, String methodName) {
            this.viewId = viewId;
            this.methodName = methodName;
        }

        ReflectionActionWithoutParams(Parcel in) {
            this.viewId = in.readInt();
            this.methodName = in.readString();
        }

        public void writeToParcel(Parcel out, int flags) {
            out.writeInt(TAG);
            out.writeInt(this.viewId);
            out.writeString(this.methodName);
        }

        @Override
        public void apply(View root, ViewGroup rootParent, OnClickHandler handler) {
            final View view = root.findViewById(viewId);
            if (view == null) return;

            Class klass = view.getClass();
            Method method;
            try {
                method = klass.getMethod(this.methodName);
            } catch (NoSuchMethodException ex) {
                throw new ActionException("view: " + klass.getName() + " doesn't have method: "
                        + this.methodName + "()");
            }

            if (!method.isAnnotationPresent(RemotableViewMethod.class)) {
                throw new ActionException("view: " + klass.getName()
                        + " can't use method with RemoteViews: "
                        + this.methodName + "()");
            }

            try {
                //noinspection ConstantIfStatement
                if (false) {
                    Log.d("RemoteViews", "view: " + klass.getName() + " calling method: "
                        + this.methodName + "()");
                }
                method.invoke(view);
            } catch (Exception ex) {
                throw new ActionException(ex);
            }
        }

        public int mergeBehavior() {
            // we don't need to build up showNext or showPrevious calls
            if (methodName.equals("showNext") || methodName.equals("showPrevious")) {
                return MERGE_IGNORE;
            } else {
                return MERGE_REPLACE;
            }
        }

        public String getActionName() {
            return "ReflectionActionWithoutParams";
        }
    }

    private static class BitmapCache {
        ArrayList<Bitmap> mBitmaps;

        public BitmapCache() {
            mBitmaps = new ArrayList<Bitmap>();
        }

        public BitmapCache(Parcel source) {
            int count = source.readInt();
            mBitmaps = new ArrayList<Bitmap>();
            for (int i = 0; i < count; i++) {
                Bitmap b = Bitmap.CREATOR.createFromParcel(source);
                mBitmaps.add(b);
            }
        }

        public int getBitmapId(Bitmap b) {
            if (b == null) {
                return -1;
            } else {
                if (mBitmaps.contains(b)) {
                    return mBitmaps.indexOf(b);
                } else {
                    mBitmaps.add(b);
                    return (mBitmaps.size() - 1);
                }
            }
        }

        public Bitmap getBitmapForId(int id) {
            if (id == -1 || id >= mBitmaps.size()) {
                return null;
            } else {
                return mBitmaps.get(id);
            }
        }

        public void writeBitmapsToParcel(Parcel dest, int flags) {
            int count = mBitmaps.size();
            dest.writeInt(count);
            for (int i = 0; i < count; i++) {
                mBitmaps.get(i).writeToParcel(dest, flags);
            }
        }

        public void assimilate(BitmapCache bitmapCache) {
            ArrayList<Bitmap> bitmapsToBeAdded = bitmapCache.mBitmaps;
            int count = bitmapsToBeAdded.size();
            for (int i = 0; i < count; i++) {
                Bitmap b = bitmapsToBeAdded.get(i);
                if (!mBitmaps.contains(b)) {
                    mBitmaps.add(b);
                }
            }
        }

        public void addBitmapMemory(MemoryUsageCounter memoryCounter) {
            for (int i = 0; i < mBitmaps.size(); i++) {
                memoryCounter.addBitmapMemory(mBitmaps.get(i));
            }
        }
    }

    private class BitmapReflectionAction extends Action {
        int bitmapId;
        Bitmap bitmap;
        String methodName;

        BitmapReflectionAction(int viewId, String methodName, Bitmap bitmap) {
            this.bitmap = bitmap;
            this.viewId = viewId;
            this.methodName = methodName;
            bitmapId = mBitmapCache.getBitmapId(bitmap);
        }

        BitmapReflectionAction(Parcel in) {
            viewId = in.readInt();
            methodName = in.readString();
            bitmapId = in.readInt();
            bitmap = mBitmapCache.getBitmapForId(bitmapId);
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeInt(TAG);
            dest.writeInt(viewId);
            dest.writeString(methodName);
            dest.writeInt(bitmapId);
        }

        @Override
        public void apply(View root, ViewGroup rootParent,
                OnClickHandler handler) throws ActionException {
            ReflectionAction ra = new ReflectionAction(viewId, methodName, ReflectionAction.BITMAP,
                    bitmap);
            ra.apply(root, rootParent, handler);
        }

        @Override
        public void setBitmapCache(BitmapCache bitmapCache) {
            bitmapId = bitmapCache.getBitmapId(bitmap);
        }

        public String getActionName() {
            return "BitmapReflectionAction";
        }

        public final static int TAG = 12;
    }

    /**
     * Base class for the reflection actions.
     */
    private class ReflectionAction extends Action {
        static final int TAG = 2;

        static final int BOOLEAN = 1;
        static final int BYTE = 2;
        static final int SHORT = 3;
        static final int INT = 4;
        static final int LONG = 5;
        static final int FLOAT = 6;
        static final int DOUBLE = 7;
        static final int CHAR = 8;
        static final int STRING = 9;
        static final int CHAR_SEQUENCE = 10;
        static final int URI = 11;
        // BITMAP actions are never stored in the list of actions. They are only used locally
        // to implement BitmapReflectionAction, which eliminates duplicates using BitmapCache.
        static final int BITMAP = 12;
        static final int BUNDLE = 13;
        static final int INTENT = 14;

        String methodName;
        int type;
        Object value;

        ReflectionAction(int viewId, String methodName, int type, Object value) {
            this.viewId = viewId;
            this.methodName = methodName;
            this.type = type;
            this.value = value;
        }

        ReflectionAction(Parcel in) {
            this.viewId = in.readInt();
            this.methodName = in.readString();
            this.type = in.readInt();
            //noinspection ConstantIfStatement
            if (false) {
                Log.d("RemoteViews", "read viewId=0x" + Integer.toHexString(this.viewId)
                        + " methodName=" + this.methodName + " type=" + this.type);
            }

            // For some values that may have been null, we first check a flag to see if they were
            // written to the parcel.
            switch (this.type) {
                case BOOLEAN:
                    this.value = in.readInt() != 0;
                    break;
                case BYTE:
                    this.value = in.readByte();
                    break;
                case SHORT:
                    this.value = (short)in.readInt();
                    break;
                case INT:
                    this.value = in.readInt();
                    break;
                case LONG:
                    this.value = in.readLong();
                    break;
                case FLOAT:
                    this.value = in.readFloat();
                    break;
                case DOUBLE:
                    this.value = in.readDouble();
                    break;
                case CHAR:
                    this.value = (char)in.readInt();
                    break;
                case STRING:
                    this.value = in.readString();
                    break;
                case CHAR_SEQUENCE:
                    this.value = TextUtils.CHAR_SEQUENCE_CREATOR.createFromParcel(in);
                    break;
                case URI:
                    if (in.readInt() != 0) {
                        this.value = Uri.CREATOR.createFromParcel(in);
                    }
                    break;
                case BITMAP:
                    if (in.readInt() != 0) {
                        this.value = Bitmap.CREATOR.createFromParcel(in);
                    }
                    break;
                case BUNDLE:
                    this.value = in.readBundle();
                    break;
                case INTENT:
                    if (in.readInt() != 0) {
                        this.value = Intent.CREATOR.createFromParcel(in);
                    }
                    break;
                default:
                    break;
            }
        }

        public void writeToParcel(Parcel out, int flags) {
            out.writeInt(TAG);
            out.writeInt(this.viewId);
            out.writeString(this.methodName);
            out.writeInt(this.type);
            //noinspection ConstantIfStatement
            if (false) {
                Log.d("RemoteViews", "write viewId=0x" + Integer.toHexString(this.viewId)
                        + " methodName=" + this.methodName + " type=" + this.type);
            }

            // For some values which are null, we record an integer flag to indicate whether
            // we have written a valid value to the parcel.
            switch (this.type) {
                case BOOLEAN:
                    out.writeInt((Boolean) this.value ? 1 : 0);
                    break;
                case BYTE:
                    out.writeByte((Byte) this.value);
                    break;
                case SHORT:
                    out.writeInt((Short) this.value);
                    break;
                case INT:
                    out.writeInt((Integer) this.value);
                    break;
                case LONG:
                    out.writeLong((Long) this.value);
                    break;
                case FLOAT:
                    out.writeFloat((Float) this.value);
                    break;
                case DOUBLE:
                    out.writeDouble((Double) this.value);
                    break;
                case CHAR:
                    out.writeInt((int)((Character)this.value).charValue());
                    break;
                case STRING:
                    out.writeString((String)this.value);
                    break;
                case CHAR_SEQUENCE:
                    TextUtils.writeToParcel((CharSequence)this.value, out, flags);
                    break;
                case URI:
                    out.writeInt(this.value != null ? 1 : 0);
                    if (this.value != null) {
                        ((Uri)this.value).writeToParcel(out, flags);
                    }
                    break;
                case BITMAP:
                    out.writeInt(this.value != null ? 1 : 0);
                    if (this.value != null) {
                        ((Bitmap)this.value).writeToParcel(out, flags);
                    }
                    break;
                case BUNDLE:
                    out.writeBundle((Bundle) this.value);
                    break;
                case INTENT:
                    out.writeInt(this.value != null ? 1 : 0);
                    if (this.value != null) {
                        ((Intent)this.value).writeToParcel(out, flags);
                    }
                    break;
                default:
                    break;
            }
        }

        private Class getParameterType() {
            switch (this.type) {
                case BOOLEAN:
                    return boolean.class;
                case BYTE:
                    return byte.class;
                case SHORT:
                    return short.class;
                case INT:
                    return int.class;
                case LONG:
                    return long.class;
                case FLOAT:
                    return float.class;
                case DOUBLE:
                    return double.class;
                case CHAR:
                    return char.class;
                case STRING:
                    return String.class;
                case CHAR_SEQUENCE:
                    return CharSequence.class;
                case URI:
                    return Uri.class;
                case BITMAP:
                    return Bitmap.class;
                case BUNDLE:
                    return Bundle.class;
                case INTENT:
                    return Intent.class;
                default:
                    return null;
            }
        }

        @Override
        public void apply(View root, ViewGroup rootParent, OnClickHandler handler) {
            final View view = root.findViewById(viewId);
            if (view == null) return;

            Class param = getParameterType();
            if (param == null) {
                throw new ActionException("bad type: " + this.type);
            }

            Class klass = view.getClass();
            Method method;
            try {
                method = klass.getMethod(this.methodName, getParameterType());
            }
            catch (NoSuchMethodException ex) {
                throw new ActionException("view: " + klass.getName() + " doesn't have method: "
                        + this.methodName + "(" + param.getName() + ")");
            }

            if (!method.isAnnotationPresent(RemotableViewMethod.class)) {
                throw new ActionException("view: " + klass.getName()
                        + " can't use method with RemoteViews: "
                        + this.methodName + "(" + param.getName() + ")");
            }

            try {
                //noinspection ConstantIfStatement
                if (false) {
                    Log.d("RemoteViews", "view: " + klass.getName() + " calling method: "
                        + this.methodName + "(" + param.getName() + ") with "
                        + (this.value == null ? "null" : this.value.getClass().getName()));
                }
                method.invoke(view, this.value);
            }
            catch (Exception ex) {
                throw new ActionException(ex);
            }
        }

        public int mergeBehavior() {
            // smoothScrollBy is cumulative, everything else overwites.
            if (methodName.equals("smoothScrollBy")) {
                return MERGE_APPEND;
            } else {
                return MERGE_REPLACE;
            }
        }

        public String getActionName() {
            // Each type of reflection action corresponds to a setter, so each should be seen as
            // unique from the standpoint of merging.
            return "ReflectionAction" + this.methodName + this.type;
        }
    }

    private void configureRemoteViewsAsChild(RemoteViews rv) {
        mBitmapCache.assimilate(rv.mBitmapCache);
        rv.setBitmapCache(mBitmapCache);
        rv.setNotRoot();
    }

    void setNotRoot() {
        mIsRoot = false;
    }

    /**
     * Equivalent to calling {@link ViewGroup#addView(View)} after inflating the
     * given {@link RemoteViews}, or calling {@link ViewGroup#removeAllViews()}
     * when null. This allows users to build "nested" {@link RemoteViews}.
     */
    private class ViewGroupAction extends Action {
        public ViewGroupAction(int viewId, RemoteViews nestedViews) {
            this.viewId = viewId;
            this.nestedViews = nestedViews;
            if (nestedViews != null) {
                configureRemoteViewsAsChild(nestedViews);
            }
        }

        public ViewGroupAction(Parcel parcel, BitmapCache bitmapCache) {
            viewId = parcel.readInt();
            boolean nestedViewsNull = parcel.readInt() == 0;
            if (!nestedViewsNull) {
                nestedViews = new RemoteViews(parcel, bitmapCache);
            } else {
                nestedViews = null;
            }
        }

        public void writeToParcel(Parcel dest, int flags) {
            dest.writeInt(TAG);
            dest.writeInt(viewId);
            if (nestedViews != null) {
                dest.writeInt(1);
                nestedViews.writeToParcel(dest, flags);
            } else {
                // signifies null
                dest.writeInt(0);
            }
        }

        @Override
        public void apply(View root, ViewGroup rootParent, OnClickHandler handler) {
            final Context context = root.getContext();
            final ViewGroup target = (ViewGroup) root.findViewById(viewId);
            if (target == null) return;
            if (nestedViews != null) {
                // Inflate nested views and add as children
                target.addView(nestedViews.apply(context, target, handler));
            } else {
                // Clear all children when nested views omitted
                target.removeAllViews();
            }
        }

        @Override
        public void updateMemoryUsageEstimate(MemoryUsageCounter counter) {
            if (nestedViews != null) {
                counter.increment(nestedViews.estimateMemoryUsage());
            }
        }

        @Override
        public void setBitmapCache(BitmapCache bitmapCache) {
            if (nestedViews != null) {
                nestedViews.setBitmapCache(bitmapCache);
            }
        }

        public String getActionName() {
            return "ViewGroupAction" + this.nestedViews == null ? "Remove" : "Add";
        }

        public int mergeBehavior() {
            return MERGE_APPEND;
        }

        RemoteViews nestedViews;

        public final static int TAG = 4;
    }

    /**
     * Helper action to set compound drawables on a TextView. Supports relative
     * (s/t/e/b) or cardinal (l/t/r/b) arrangement.
     */
    private class TextViewDrawableAction extends Action {
        public TextViewDrawableAction(int viewId, boolean isRelative, int d1, int d2, int d3, int d4) {
            this.viewId = viewId;
            this.isRelative = isRelative;
            this.d1 = d1;
            this.d2 = d2;
            this.d3 = d3;
            this.d4 = d4;
        }

        public TextViewDrawableAction(Parcel parcel) {
            viewId = parcel.readInt();
            isRelative = (parcel.readInt() != 0);
            d1 = parcel.readInt();
            d2 = parcel.readInt();
            d3 = parcel.readInt();
            d4 = parcel.readInt();
        }

        public void writeToParcel(Parcel dest, int flags) {
            dest.writeInt(TAG);
            dest.writeInt(viewId);
            dest.writeInt(isRelative ? 1 : 0);
            dest.writeInt(d1);
            dest.writeInt(d2);
            dest.writeInt(d3);
            dest.writeInt(d4);
        }

        @Override
        public void apply(View root, ViewGroup rootParent, OnClickHandler handler) {
            final Context context = root.getContext();
            final TextView target = (TextView) root.findViewById(viewId);
            if (target == null) return;
            if (isRelative) {
                target.setCompoundDrawablesRelativeWithIntrinsicBounds(d1, d2, d3, d4);
            } else {
                target.setCompoundDrawablesWithIntrinsicBounds(d1, d2, d3, d4);
            }
        }

        public String getActionName() {
            return "TextViewDrawableAction";
        }

        boolean isRelative = false;
        int d1, d2, d3, d4;

        public final static int TAG = 11;
    }

    /**
     * Helper action to set text size on a TextView in any supported units.
     */
    private class TextViewSizeAction extends Action {
        public TextViewSizeAction(int viewId, int units, float size) {
            this.viewId = viewId;
            this.units = units;
            this.size = size;
        }

        public TextViewSizeAction(Parcel parcel) {
            viewId = parcel.readInt();
            units = parcel.readInt();
            size  = parcel.readFloat();
        }

        public void writeToParcel(Parcel dest, int flags) {
            dest.writeInt(TAG);
            dest.writeInt(viewId);
            dest.writeInt(units);
            dest.writeFloat(size);
        }

        @Override
        public void apply(View root, ViewGroup rootParent, OnClickHandler handler) {
            final Context context = root.getContext();
            final TextView target = (TextView) root.findViewById(viewId);
            if (target == null) return;
            target.setTextSize(units, size);
        }

        public String getActionName() {
            return "TextViewSizeAction";
        }

        int units;
        float size;

        public final static int TAG = 13;
    }

    /**
     * Helper action to set padding on a View.
     */
    private class ViewPaddingAction extends Action {
        public ViewPaddingAction(int viewId, int left, int top, int right, int bottom) {
            this.viewId = viewId;
            this.left = left;
            this.top = top;
            this.right = right;
            this.bottom = bottom;
        }

        public ViewPaddingAction(Parcel parcel) {
            viewId = parcel.readInt();
            left = parcel.readInt();
            top = parcel.readInt();
            right = parcel.readInt();
            bottom = parcel.readInt();
        }

        public void writeToParcel(Parcel dest, int flags) {
            dest.writeInt(TAG);
            dest.writeInt(viewId);
            dest.writeInt(left);
            dest.writeInt(top);
            dest.writeInt(right);
            dest.writeInt(bottom);
        }

        @Override
        public void apply(View root, ViewGroup rootParent, OnClickHandler handler) {
            final Context context = root.getContext();
            final View target = root.findViewById(viewId);
            if (target == null) return;
            target.setPadding(left, top, right, bottom);
        }

        public String getActionName() {
            return "ViewPaddingAction";
        }

        int left, top, right, bottom;

        public final static int TAG = 14;
    }

    /**
     * Simple class used to keep track of memory usage in a RemoteViews.
     *
     */
    private class MemoryUsageCounter {
        public void clear() {
            mMemoryUsage = 0;
        }

        public void increment(int numBytes) {
            mMemoryUsage += numBytes;
        }

        public int getMemoryUsage() {
            return mMemoryUsage;
        }

        public void addBitmapMemory(Bitmap b) {
            final Bitmap.Config c = b.getConfig();
            // If we don't know, be pessimistic and assume 4
            int bpp = 4;
            if (c != null) {
                switch (c) {
                case ALPHA_8:
                    bpp = 1;
                    break;
                case RGB_565:
                case ARGB_4444:
                    bpp = 2;
                    break;
                case ARGB_8888:
                    bpp = 4;
                    break;
                }
            }
            increment(b.getWidth() * b.getHeight() * bpp);
        }

        int mMemoryUsage;
    }

    /**
     * 创建用于显示包含于指定布局文件中的视图的 RemoteViews 对象.
     * 
     * @param packageName 包含布局资源的包名
     * @param layoutId 布局资源 ID
     */
    public RemoteViews(String packageName, int layoutId) {
        mPackage = packageName;
        mLayoutId = layoutId;
        mBitmapCache = new BitmapCache();

        // setup the memory usage statistics
        mMemoryUsageCounter = new MemoryUsageCounter();
        recalculateMemoryUsage();
    }

    /** {@hide} */
    public void setUser(UserHandle user) {
        mUser = user;
    }

    private boolean hasLandscapeAndPortraitLayouts() {
        return (mLandscape != null) && (mPortrait != null);
    }

    /**
     * Create a new RemoteViews object that will inflate as the specified
     * landspace or portrait RemoteViews, depending on the current configuration.
     *
     * @param landscape The RemoteViews to inflate in landscape configuration
     * @param portrait The RemoteViews to inflate in portrait configuration
     */
    public RemoteViews(RemoteViews landscape, RemoteViews portrait) {
        if (landscape == null || portrait == null) {
            throw new RuntimeException("Both RemoteViews must be non-null");
        }
        if (landscape.getPackage().compareTo(portrait.getPackage()) != 0) {
            throw new RuntimeException("Both RemoteViews must share the same package");
        }
        mPackage = portrait.getPackage();
        mLayoutId = portrait.getLayoutId();

        mLandscape = landscape;
        mPortrait = portrait;

        // setup the memory usage statistics
        mMemoryUsageCounter = new MemoryUsageCounter();

        mBitmapCache = new BitmapCache();
        configureRemoteViewsAsChild(landscape);
        configureRemoteViewsAsChild(portrait);

        recalculateMemoryUsage();
    }

    /**
     * 从 parcel 中读取 RemoteViews 对象.
     * 
     * @param parcel
     */
    public RemoteViews(Parcel parcel) {
        this(parcel, null);
    }

    private RemoteViews(Parcel parcel, BitmapCache bitmapCache) {
        int mode = parcel.readInt();

        // We only store a bitmap cache in the root of the RemoteViews.
        if (bitmapCache == null) {
            mBitmapCache = new BitmapCache(parcel);
        } else {
            setBitmapCache(bitmapCache);
            setNotRoot();
        }

        if (mode == MODE_NORMAL) {
            mPackage = parcel.readString();
            mLayoutId = parcel.readInt();
            mIsWidgetCollectionChild = parcel.readInt() == 1 ? true : false;

            int count = parcel.readInt();
            if (count > 0) {
                mActions = new ArrayList<Action>(count);
                for (int i=0; i<count; i++) {
                    int tag = parcel.readInt();
                    switch (tag) {
                    case SetOnClickPendingIntent.TAG:
                        mActions.add(new SetOnClickPendingIntent(parcel));
                        break;
                    case SetDrawableParameters.TAG:
                        mActions.add(new SetDrawableParameters(parcel));
                        break;
                    case ReflectionAction.TAG:
                        mActions.add(new ReflectionAction(parcel));
                        break;
                    case ViewGroupAction.TAG:
                        mActions.add(new ViewGroupAction(parcel, mBitmapCache));
                        break;
                    case ReflectionActionWithoutParams.TAG:
                        mActions.add(new ReflectionActionWithoutParams(parcel));
                        break;
                    case SetEmptyView.TAG:
                        mActions.add(new SetEmptyView(parcel));
                        break;
                    case SetPendingIntentTemplate.TAG:
                        mActions.add(new SetPendingIntentTemplate(parcel));
                        break;
                    case SetOnClickFillInIntent.TAG:
                        mActions.add(new SetOnClickFillInIntent(parcel));
                        break;
                    case SetRemoteViewsAdapterIntent.TAG:
                        mActions.add(new SetRemoteViewsAdapterIntent(parcel));
                        break;
                    case TextViewDrawableAction.TAG:
                        mActions.add(new TextViewDrawableAction(parcel));
                        break;
                    case TextViewSizeAction.TAG:
                        mActions.add(new TextViewSizeAction(parcel));
                        break;
                    case ViewPaddingAction.TAG:
                        mActions.add(new ViewPaddingAction(parcel));
                        break;
                    case BitmapReflectionAction.TAG:
                        mActions.add(new BitmapReflectionAction(parcel));
                        break;
                    default:
                        throw new ActionException("Tag " + tag + " not found");
                    }
                }
            }
        } else {
            // MODE_HAS_LANDSCAPE_AND_PORTRAIT
            mLandscape = new RemoteViews(parcel, mBitmapCache);
            mPortrait = new RemoteViews(parcel, mBitmapCache);
            mPackage = mPortrait.getPackage();
            mLayoutId = mPortrait.getLayoutId();
        }

        // setup the memory usage statistics
        mMemoryUsageCounter = new MemoryUsageCounter();
        recalculateMemoryUsage();
    }


    public RemoteViews clone() {
        Parcel p = Parcel.obtain();
        writeToParcel(p, 0);
        p.setDataPosition(0);
        return new RemoteViews(p);
    }

    public String getPackage() {
        return mPackage;
    }

    /**
     * Reutrns the layout id of the root layout associated with this RemoteViews. In the case
     * that the RemoteViews has both a landscape and portrait root, this will return the layout
     * id associated with the portrait layout.
     *
     * @return the layout id.
     */
    public int getLayoutId() {
        return mLayoutId;
    }

    /*
     * This flag indicates whether this RemoteViews object is being created from a
     * RemoteViewsService for use as a child of a widget collection. This flag is used
     * to determine whether or not certain features are available, in particular,
     * setting on click extras and setting on click pending intents. The former is enabled,
     * and the latter disabled when this flag is true.
     */
    void setIsWidgetCollectionChild(boolean isWidgetCollectionChild) {
        mIsWidgetCollectionChild = isWidgetCollectionChild;
    }

    /**
     * Updates the memory usage statistics.
     */
    private void recalculateMemoryUsage() {
        mMemoryUsageCounter.clear();

        if (!hasLandscapeAndPortraitLayouts()) {
            // Accumulate the memory usage for each action
            if (mActions != null) {
                final int count = mActions.size();
                for (int i= 0; i < count; ++i) {
                    mActions.get(i).updateMemoryUsageEstimate(mMemoryUsageCounter);
                }
            }
            if (mIsRoot) {
                mBitmapCache.addBitmapMemory(mMemoryUsageCounter);
            }
        } else {
            mMemoryUsageCounter.increment(mLandscape.estimateMemoryUsage());
            mMemoryUsageCounter.increment(mPortrait.estimateMemoryUsage());
            mBitmapCache.addBitmapMemory(mMemoryUsageCounter);
        }
    }

    /**
     * Recursively sets BitmapCache in the hierarchy and update the bitmap ids.
     */
    private void setBitmapCache(BitmapCache bitmapCache) {
        mBitmapCache = bitmapCache;
        if (!hasLandscapeAndPortraitLayouts()) {
            if (mActions != null) {
                final int count = mActions.size();
                for (int i= 0; i < count; ++i) {
                    mActions.get(i).setBitmapCache(bitmapCache);
                }
            }
        } else {
            mLandscape.setBitmapCache(bitmapCache);
            mPortrait.setBitmapCache(bitmapCache);
        }
    }

    /**
     * Returns an estimate of the bitmap heap memory usage for this RemoteViews.
     */
    /** @hide */
    public int estimateMemoryUsage() {
        return mMemoryUsageCounter.getMemoryUsage();
    }

    /**
     * Add an action to be executed on the remote side when apply is called.
     *
     * @param a The action to add
     */
    private void addAction(Action a) {
        if (hasLandscapeAndPortraitLayouts()) {
            throw new RuntimeException("RemoteViews specifying separate landscape and portrait" +
                    " layouts cannot be modified. Instead, fully configure the landscape and" +
                    " portrait layouts individually before constructing the combined layout.");
        }
        if (mActions == null) {
            mActions = new ArrayList<Action>();
        }
        mActions.add(a);

        // update the memory usage stats
        a.updateMemoryUsageEstimate(mMemoryUsageCounter);
    }

    /**
     * 相当于展开给出的 {@link RemoteViews} 后，为么个视图调用 
     * {@link ViewGroup#addView(View)}.该函数允许用户构建“嵌套的”
     * {@link RemoteViews}.某些情况下 {@link RemoteViews} 的使用者可以使用
     * {@link #removeAllViews(int)} 函数来清除任何既存的子视图.
     *
     * @param viewId 用于添加子视图的父 {@link ViewGroup} 的 ID
     * @param nestedView 用于提供子视图的 {@link RemoteViews}
     */
    public void addView(int viewId, RemoteViews nestedView) {
        addAction(new ViewGroupAction(viewId, nestedView));
    }

    /**
     * 相当于调用 {@link ViewGroup#removeAllViews()} 方法.
     *
     * @param viewId 要移除所有子视图的父 {@link ViewGroup} 的 ID
     */
    public void removeAllViews(int viewId) {
        addAction(new ViewGroupAction(viewId, null));
    }

    /**
     * 相当于调用 {@link AdapterViewAnimator#showNext()}.
     *
     * @param viewId 调用 {@link AdapterViewAnimator#showNext()} 方法的视图 ID
     */
    public void showNext(int viewId) {
        addAction(new ReflectionActionWithoutParams(viewId, "showNext"));
    }

    /**
     * 相当于调用 {@link AdapterViewAnimator#showPrevious()}.
     *
     * @param viewId 调用 {@link AdapterViewAnimator#showPrevious()}方法的视图 ID
     */
    public void showPrevious(int viewId) {
        addAction(new ReflectionActionWithoutParams(viewId, "showPrevious"));
    }

    /**
     * 相当于调用 {@link AdapterViewAnimator#setDisplayedChild(int)}
     *
     * @param viewId 调用 {@link AdapterViewAnimator#setDisplayedChild(int)} 方法的视图 ID
     */
    public void setDisplayedChild(int viewId, int childIndex) {
        setInt(viewId, "setDisplayedChild", childIndex);
    }

    /**
     * 相当于调用 View.setVisibility.
     * 
     * @param viewId 要改变可视状态的视图 ID
     * @param visibility 视图的新的可是状态
     */
    public void setViewVisibility(int viewId, int visibility) {
        setInt(viewId, "setVisibility", visibility);
    }

    /**
     * 相当于调用 TextView.setText.
     * 
     * @param viewId 要改变显示的文本的视图 ID
     * @param text 视图显示的新文本
     */
    public void setTextViewText(int viewId, CharSequence text) {
        setCharSequence(viewId, "setText", text);
    }

    /**
     * Equivalent to calling {@link TextView#setTextSize(int, float)}
     *
     * @param viewId The id of the view whose text size should change
     * @param units The units of size (e.g. COMPLEX_UNIT_SP)
     * @param size The size of the text
     */
    public void setTextViewTextSize(int viewId, int units, float size) {
        addAction(new TextViewSizeAction(viewId, units, size));
    }

    /**
     * Equivalent to calling
     * {@link TextView#setCompoundDrawablesWithIntrinsicBounds(int, int, int, int)}.
     *
     * @param viewId The id of the view whose text should change
     * @param left The id of a drawable to place to the left of the text, or 0
     * @param top The id of a drawable to place above the text, or 0
     * @param right The id of a drawable to place to the right of the text, or 0
     * @param bottom The id of a drawable to place below the text, or 0
     */
    public void setTextViewCompoundDrawables(int viewId, int left, int top, int right, int bottom) {
        addAction(new TextViewDrawableAction(viewId, false, left, top, right, bottom));
    }

    /**
     * Equivalent to calling {@link
     * TextView#setCompoundDrawablesRelativeWithIntrinsicBounds(int, int, int, int)}.
     *
     * @param viewId The id of the view whose text should change
     * @param start The id of a drawable to place before the text (relative to the
     * layout direction), or 0
     * @param top The id of a drawable to place above the text, or 0
     * @param end The id of a drawable to place after the text, or 0
     * @param bottom The id of a drawable to place below the text, or 0
     */
    public void setTextViewCompoundDrawablesRelative(int viewId, int start, int top, int end, int bottom) {
        addAction(new TextViewDrawableAction(viewId, true, start, top, end, bottom));
    }

    /**
     * 相当于调用 ImageView.setImageResource.
     * 
     * @param viewId 要改变可绘制对象的视图 ID
     * @param srcId 新可绘制对象的资源 ID
     */
    public void setImageViewResource(int viewId, int srcId) {
        setInt(viewId, "setImageResource", srcId);
    }

    /**
     * 相当于调用 ImageView.setImageURI.
     * 
     * @param viewId 要改变可绘制对象的视图 ID
     * @param uri 新图像的 Uri
     */
    public void setImageViewUri(int viewId, Uri uri) {
        setUri(viewId, "setImageURI", uri);
    }

    /**
     * 相当于调用 ImageView.setImageBitmap.
     * 
     * @param viewId 要改变可绘制对象的视图 ID
     * @param bitmap 可绘制的新位图
     */
    public void setImageViewBitmap(int viewId, Bitmap bitmap) {
        setBitmap(viewId, "setImageBitmap", bitmap);
    }

    /**
     * 相当于调用 AdapterView.setEmptyView
     *
     * @param viewId 要设置为空视图的视图 ID.
     * @param emptyViewId 空视图的视图ID.
     */
    public void setEmptyView(int viewId, int emptyViewId) {
        addAction(new SetEmptyView(viewId, emptyViewId));
    }

    /**
     * 相当于调用 {@link Chronometer#setBase Chronometer.setBase}、
     * {@link Chronometer#setFormat Chronometer.setFormat}
     * 和 {@link Chronometer#start Chronometer.start()} 或
     * {@link Chronometer#stop Chronometer.stop()}.
     * 
     * @param viewId The id of the {@link Chronometer} to change
     * @param base 代表计时器起始值 0:00 的时间.该时间是基于
     *             {@link android.os.SystemClock#elapsedRealtime SystemClock.elapsedRealtime()}.
     * @param format 代表计时器格式的字符串，为 null 则只是简单的显示计时器的值.
     * @param started 真表示启动计时器，否则为假.
     */
    public void setChronometer(int viewId, long base, String format, boolean started) {
        setLong(viewId, "setBase", base);
        setString(viewId, "setFormat", format);
        setBoolean(viewId, "setStarted", started);
    }

    /**
     * 相当于调用 {@link ProgressBar#setMax ProgressBar.setMax}、
     * {@link ProgressBar#setProgress ProgressBar.setProgress} 和
     * {@link ProgressBar#setIndeterminate ProgressBar.setIndeterminate}.
     *
     * 如果 indeterminate 为真，则忽略 max 和 progress 的值.
     * 
     * @param viewId 要改变的{@link ProgressBar}的视图 ID
     * @param max 代表进度条 100% 的值
     * @param progress 进度条的当前值
     * @param indeterminate 为真表示进度不确定，否则为假.
     */
    public void setProgressBar(int viewId, int max, int progress,
            boolean indeterminate) {
        setBoolean(viewId, "setIndeterminate", indeterminate);
        if (!indeterminate) {
            setInt(viewId, "setMax", max);
            setInt(viewId, "setProgress", progress);
        }
    }

    /**
     * 相当于调用
     * {@link android.view.View#setOnClickListener(android.view.View.OnClickListener)}
     * 来执行提供的 {@link PendingIntent}.
     * 
     * 当为集合（例如：{@link ListView}、{@link StackView}等）中的项目设置单击动作时，该方法无效.
     * 请使用 {@link RemoteViews#setPendingIntentTemplate(int, PendingIntent)} 和
     * {@link RemoteViews#setOnClickFillInIntent(int, Intent)} 的组合来实现.
     *
     * @param viewId 单击时触发 {@link PendingIntent} 的视图 ID.
     * @param pendingIntent 用户单击时发送的 {@link PendingIntent}.
     */
    public void setOnClickPendingIntent(int viewId, PendingIntent pendingIntent) {
        addAction(new SetOnClickPendingIntent(viewId, pendingIntent));
    }

    /**
     * 在小部件中使用集合（比如： {@link ListView}、{@link StackView}等）时，为单独的条目设置
     * PendingIntents 代价很高，因此是不允许的.该方法可以为集合设置一个 PendingIntent 模板.
     * 单独的条目可以通过 {@link RemoteViews#setOnClickFillInIntent(int, Intent)}
     * 设置他们不同的单击行为.
     *
     * @param viewId 单击时使用该 PendingIntent 模板的集合对象的 ID
     * @param pendingIntentTemplate 当子条目单击时执行的 {@link PendingIntent}，
     *        可以与 viewId 指定的视图的子条目提供的意图组合使用.
     */
    public void setPendingIntentTemplate(int viewId, PendingIntent pendingIntentTemplate) {
        addAction(new SetPendingIntentTemplate(viewId, pendingIntentTemplate));
    }

    /**
     * 在小部件中使用集合（比如： {@link ListView}、{@link StackView}等）时，为单独的条目设置
     * PendingIntents 代价很高，因此是不允许的.作为替代，可以为集合对象设置一个
     * PendingIntent 模板，参见 {@link RemoteViews#setPendingIntentTemplate(int, PendingIntent)}.
     * 各个条目的单击动作可以通过在条目中设置的 fillInIntent 来区分. fillInIntent 与
     * PendingIntent 模板组合，即可知道条目单击时执行哪个意图.工作原理如下： PendingIntent 模板中
     * 留下一些空白的字段，由 fillInIntent 提供的内容覆盖这些字段，得到最后要使用的 PendingIntent.
     * 更多细节参见 {@link Intent#fillIn(Intent, int)}.
     *
     * @param viewId 设置 fillInIntent 的视图ID
     * @param fillInIntent 将与父视图的 PendingIntent 合并的意图，用于决定单击由 viewId 指定的视图时的动作.
     */
    public void setOnClickFillInIntent(int viewId, Intent fillInIntent) {
        addAction(new SetOnClickFillInIntent(viewId, fillInIntent));
    }

    /**
     * @hide
     * 相当于在指定视图的 {@link Drawable} 上执行{@link Drawable#setAlpha(int)}、
     * {@link Drawable#setColorFilter(int, android.graphics.PorterDuff.Mode)}和
     * {@link Drawable#setLevel(int)} 的组合.
     * <p>
     * You can omit specific calls by marking their values with null or -1.
     *
     * @param viewId The id of the view that contains the target
     *            {@link Drawable}
     * @param targetBackground If true, apply these parameters to the
     *            {@link Drawable} returned by
     *            {@link android.view.View#getBackground()}. Otherwise, assume
     *            the target view is an {@link ImageView} and apply them to
     *            {@link ImageView#getDrawable()}.
     * @param alpha Specify an alpha value for the drawable, or -1 to leave
     *            unchanged.
     * @param colorFilter Specify a color for a
     *            {@link android.graphics.ColorFilter} for this drawable, or -1
     *            to leave unchanged.
     * @param mode Specify a PorterDuff mode for this drawable, or null to leave
     *            unchanged.
     * @param level Specify the level for the drawable, or -1 to leave
     *            unchanged.
     */
    public void setDrawableParameters(int viewId, boolean targetBackground, int alpha,
            int colorFilter, PorterDuff.Mode mode, int level) {
        addAction(new SetDrawableParameters(viewId, targetBackground, alpha,
                colorFilter, mode, level));
    }

    /**
     * 相当于调用 {@link android.widget.TextView#setTextColor(int)}.
     * 
     * @param viewId The id of the view whose text color should change
     * @param color 设置所有状态（正常、选中、有焦点）下要显示的颜色
     */
    public void setTextColor(int viewId, int color) {
        setInt(viewId, "setTextColor", color);
    }

    /**
     * 相当于调用 {@link android.widget.AbsListView#setRemoteViewsAdapter(Intent)}.
     *
     * @param appWidgetId 包含指定视图的应用小部件的 ID.（该不推荐方法会忽略该参数）
     * @param viewId The id of the {@link AbsListView}
     * @param intent 为 RemoteViewsAdapter 提供数据的服务的意图.
     * @deprecated 该方法已过时，参见
     *      {@link android.widget.RemoteViews#setRemoteAdapter(int, Intent)}.
     */
    @Deprecated
    public void setRemoteAdapter(int appWidgetId, int viewId, Intent intent) {
        setRemoteAdapter(viewId, intent);
    }

    /**
     * 相当于调用 {@link android.widget.AbsListView#setRemoteViewsAdapter(Intent)}.
     * 只能用于应用小部件.
     *
     * @param viewId The id of the {@link AbsListView}
     * @param intent 为 RemoteViewsAdapter 提供数据的服务的意图.
     */
    public void setRemoteAdapter(int viewId, Intent intent) {
        addAction(new SetRemoteViewsAdapterIntent(viewId, intent));
    }

    /**
     * 相当于调用 {@link android.widget.AbsListView#smoothScrollToPosition(int, int)}.
     *
     * @param viewId 要改变的视图ID
     * @param position 滚动到的适配器位置.
     */
    public void setScrollPosition(int viewId, int position) {
        setInt(viewId, "smoothScrollToPosition", position);
    }

    /**
     * 相当于调用 {@link android.widget.AbsListView#smoothScrollToPosition(int, int)}.
     *
     * @param viewId 要改变的视图ID
     * @param offset 相对当前适配器位置的滚动量.
     */
    public void setRelativeScrollPosition(int viewId, int offset) {
        setInt(viewId, "smoothScrollByOffset", offset);
    }

    /**
     * Equivalent to calling {@link android.view.View#setPadding(int, int, int, int)}.
     *
     * @param viewId The id of the view to change
     * @param left the left padding in pixels
     * @param top the top padding in pixels
     * @param right the right padding in pixels
     * @param bottom the bottom padding in pixels
     */
    public void setViewPadding(int viewId, int left, int top, int right, int bottom) {
        addAction(new ViewPaddingAction(viewId, left, top, right, bottom));
    }

    /**
     * 调用 RemoteViews 布局中指定视图的需要一个布尔值参数的方法.
     *
     * @param viewId 要改变的视图 ID
     * @param methodName 调用的方法名
     * @param value 调用方法时使用的值
     */
    public void setBoolean(int viewId, String methodName, boolean value) {
        addAction(new ReflectionAction(viewId, methodName, ReflectionAction.BOOLEAN, value));
    }

    /**
     * 调用 RemoteViews 布局中指定视图的需要一个 byte 参数的方法.
     *
     * @param viewId The id of the view on which to call the method.
     * @param methodName 调用的方法名
     * @param value 调用方法时使用的值
     */
    public void setByte(int viewId, String methodName, byte value) {
        addAction(new ReflectionAction(viewId, methodName, ReflectionAction.BYTE, value));
    }

    /**
     * 调用 RemoteViews 布局中指定视图的需要一个短整型参数的方法.
     *
     * @param viewId 调用该方法的视图的 ID
     * @param methodName 调用的方法名
     * @param value 调用方法时使用的值
     */
    public void setShort(int viewId, String methodName, short value) {
        addAction(new ReflectionAction(viewId, methodName, ReflectionAction.SHORT, value));
    }

    /**
     * 调用 RemoteViews 布局中指定视图的需要一个整型参数的方法.
     *
     * @param viewId 调用该方法的视图的 ID
     * @param methodName 调用的方法名
     * @param value 调用方法时使用的值
     */
    public void setInt(int viewId, String methodName, int value) {
        addAction(new ReflectionAction(viewId, methodName, ReflectionAction.INT, value));
    }

    /**
     * 调用 RemoteViews 布局中指定视图的需要一个长整型参数的方法.
     *
     * @param viewId 调用该方法的视图的 ID
     * @param methodName 调用的方法名
     * @param value 调用方法时使用的值
     */
    public void setLong(int viewId, String methodName, long value) {
        addAction(new ReflectionAction(viewId, methodName, ReflectionAction.LONG, value));
    }

    /**
     * 调用 RemoteViews 布局中指定视图的需要一个浮点型参数的方法.
     *
     * @param viewId 调用该方法的视图的 ID
     * @param methodName 调用的方法名
     * @param value 调用方法时使用的值
     */
    public void setFloat(int viewId, String methodName, float value) {
        addAction(new ReflectionAction(viewId, methodName, ReflectionAction.FLOAT, value));
    }

    /**
     * 调用 RemoteViews 布局中指定视图的需要一个双精度型参数的方法.
     *
     * @param viewId 调用该方法的视图的 ID
     * @param methodName 调用的方法名
     * @param value 调用方法时使用的值
     */
    public void setDouble(int viewId, String methodName, double value) {
        addAction(new ReflectionAction(viewId, methodName, ReflectionAction.DOUBLE, value));
    }

    /**
     * 调用 RemoteViews 布局中指定视图的需要一个字符参数的方法.
     *
     * @param viewId 调用该方法的视图的 ID
     * @param methodName 调用的方法名
     * @param value 调用方法时使用的值
     */
    public void setChar(int viewId, String methodName, char value) {
        addAction(new ReflectionAction(viewId, methodName, ReflectionAction.CHAR, value));
    }

    /**
     * 调用 RemoteViews 布局中指定视图的需要一个字符串参数的方法.
     *
     * @param viewId 调用该方法的视图的 ID
     * @param methodName 调用的方法名
     * @param value 调用方法时使用的值
     */
    public void setString(int viewId, String methodName, String value) {
        addAction(new ReflectionAction(viewId, methodName, ReflectionAction.STRING, value));
    }

    /**
     * 调用 RemoteViews 布局中指定视图的需要一个 CharSequence 参数的方法.
     *
     * @param viewId 调用该方法的视图的 ID
     * @param methodName 调用的方法名
     * @param value 调用方法时使用的值
     */
    public void setCharSequence(int viewId, String methodName, CharSequence value) {
        addAction(new ReflectionAction(viewId, methodName, ReflectionAction.CHAR_SEQUENCE, value));
    }

    /**
     * 调用 RemoteViews 布局中指定视图的需要一个 Uri 参数的方法.
     *
     * @param viewId 调用该方法的视图的 ID
     * @param methodName 调用的方法名
     * @param value 调用方法时使用的值
     */
    public void setUri(int viewId, String methodName, Uri value) {
        // Resolve any filesystem path before sending remotely
        value = value.getCanonicalUri();
        addAction(new ReflectionAction(viewId, methodName, ReflectionAction.URI, value));
    }

    /**
     * 调用 RemoteViews 布局中指定视图的需要一个位图参数的方法.
     * @more
     * <p class="note">如果对象用于跨进程传递，位图会展开并保存到 parcel 中，因此最终会使用该大量内存，
     * 处理可能相当缓慢.</p>
     *
     * @param viewId 调用该方法的视图的 ID
     * @param methodName 调用的方法名
     * @param value 调用方法时使用的值
     */
    public void setBitmap(int viewId, String methodName, Bitmap value) {
        addAction(new BitmapReflectionAction(viewId, methodName, value));
    }

    /**
     * 调用 RemoteViews 布局中指定视图的需要一个 Bundle 参数的方法.
     *
     * @param viewId 调用该方法的视图的 ID
     * @param methodName 调用的方法名
     * @param value 调用方法时使用的值
     */
    public void setBundle(int viewId, String methodName, Bundle value) {
        addAction(new ReflectionAction(viewId, methodName, ReflectionAction.BUNDLE, value));
    }

    /**
     * Call a method taking one Intent on a view in the layout for this RemoteViews.
     *
     * @param viewId The id of the view on which to call the method.
     * @param methodName The name of the method to call.
     * @param value The {@link android.content.Intent} to pass the method.
     */
    public void setIntent(int viewId, String methodName, Intent value) {
        addAction(new ReflectionAction(viewId, methodName, ReflectionAction.INTENT, value));
    }

    /**
     * Equivalent to calling View.setContentDescription(CharSequence).
     *
     * @param viewId The id of the view whose content description should change.
     * @param contentDescription The new content description for the view.
     */
    public void setContentDescription(int viewId, CharSequence contentDescription) {
        setCharSequence(viewId, "setContentDescription", contentDescription);
    }

    /**
     * Equivalent to calling View.setLabelFor(int).
     *
     * @param viewId The id of the view whose property to set.
     * @param labeledId The id of a view for which this view serves as a label.
     */
    public void setLabelFor(int viewId, int labeledId) {
        setInt(viewId, "setLabelFor", labeledId);
    }

    private RemoteViews getRemoteViewsToApply(Context context) {
        if (hasLandscapeAndPortraitLayouts()) {
            int orientation = context.getResources().getConfiguration().orientation;
            if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
                return mLandscape;
            } else {
                return mPortrait;
            }
        }
        return this;
    }

    /**
     * 展开本对象代表的视图树，并应用所有动作.
     * 
     * <p><strong>调用者注意：该函数可能抛出异常.</strong>
     * 
     * @param context 默认使用的上下文
     * @param parent 用于放置处理结果视图树的父视图.该方法并<strong>不</strong>
     * 实际添加视图树.需要调用者在适当的时候添加
     * @return 展开的视图树
     */
    public View apply(Context context, ViewGroup parent) {
        return apply(context, parent, null);
    }

    /** @hide */
    public View apply(Context context, ViewGroup parent, OnClickHandler handler) {
        RemoteViews rvToApply = getRemoteViewsToApply(context);

        View result;

        Context c = prepareContext(context);

        LayoutInflater inflater = (LayoutInflater)
                c.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        inflater = inflater.cloneInContext(c);
        inflater.setFilter(this);

        result = inflater.inflate(rvToApply.getLayoutId(), parent, false);

        rvToApply.performApply(result, parent, handler);

        return result;
    }

    /**
     * 对给出的视图应用所有动作.
     *
     * <p><strong>调用者注意：该函数可能抛出异常.</strong>
     * 
     * @param context 默认使用的上下文
     * @param v 要应用所有动作的视图.应该是 {@link #apply(Context,ViewGroup)} 返回的视图.
     */
    public void reapply(Context context, View v) {
        reapply(context, v, null);
    }

    /** @hide */
    public void reapply(Context context, View v, OnClickHandler handler) {
        RemoteViews rvToApply = getRemoteViewsToApply(context);

        // In the case that a view has this RemoteViews applied in one orientation, is persisted
        // across orientation change, and has the RemoteViews re-applied in the new orientation,
        // we throw an exception, since the layouts may be completely unrelated.
        if (hasLandscapeAndPortraitLayouts()) {
            if (v.getId() != rvToApply.getLayoutId()) {
                throw new RuntimeException("Attempting to re-apply RemoteViews to a view that" +
                        " that does not share the same root layout id.");
            }
        }

        prepareContext(context);
        rvToApply.performApply(v, (ViewGroup) v.getParent(), handler);
    }

    private void performApply(View v, ViewGroup parent, OnClickHandler handler) {
        if (mActions != null) {
            handler = handler == null ? DEFAULT_ON_CLICK_HANDLER : handler;
            final int count = mActions.size();
            for (int i = 0; i < count; i++) {
                Action a = mActions.get(i);
                a.apply(v, parent, handler);
            }
        }
    }

    private Context prepareContext(Context context) {
        Context c;
        String packageName = mPackage;

        if (packageName != null) {
            try {
                c = context.createPackageContextAsUser(
                        packageName, Context.CONTEXT_RESTRICTED, mUser);
            } catch (NameNotFoundException e) {
                Log.e(LOG_TAG, "Package name " + packageName + " not found");
                c = context;
            }
        } else {
            c = context;
        }

        return c;
    }

    /* (non-Javadoc)
     * Used to restrict the views which can be inflated
     *
     * @see android.view.LayoutInflater.Filter#onLoadClass(java.lang.Class)
     */
    public boolean onLoadClass(Class clazz) {
        return clazz.isAnnotationPresent(RemoteView.class);
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        if (!hasLandscapeAndPortraitLayouts()) {
            dest.writeInt(MODE_NORMAL);
            // We only write the bitmap cache if we are the root RemoteViews, as this cache
            // is shared by all children.
            if (mIsRoot) {
                mBitmapCache.writeBitmapsToParcel(dest, flags);
            }
            dest.writeString(mPackage);
            dest.writeInt(mLayoutId);
            dest.writeInt(mIsWidgetCollectionChild ? 1 : 0);
            int count;
            if (mActions != null) {
                count = mActions.size();
            } else {
                count = 0;
            }
            dest.writeInt(count);
            for (int i=0; i<count; i++) {
                Action a = mActions.get(i);
                a.writeToParcel(dest, 0);
            }
        } else {
            dest.writeInt(MODE_HAS_LANDSCAPE_AND_PORTRAIT);
            // We only write the bitmap cache if we are the root RemoteViews, as this cache
            // is shared by all children.
            if (mIsRoot) {
                mBitmapCache.writeBitmapsToParcel(dest, flags);
            }
            mLandscape.writeToParcel(dest, flags);
            mPortrait.writeToParcel(dest, flags);
        }
    }

    /**
     * 用于实例化 RemoteViews 对象的 Parcelable.Creator.
     */
    public static final Parcelable.Creator<RemoteViews> CREATOR = new Parcelable.Creator<RemoteViews>() {
        public RemoteViews createFromParcel(Parcel parcel) {
            return new RemoteViews(parcel);
        }

        public RemoteViews[] newArray(int size) {
            return new RemoteViews[size];
        }
    };
}
