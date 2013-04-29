/*
 * Copyright (C) 2010 The Android Open Source Project
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

import android.animation.Animator;
import android.content.ComponentCallbacks2;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.AndroidRuntimeException;
import android.util.AttributeSet;
import android.util.DebugUtils;
import android.util.Log;
import android.util.SparseArray;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnCreateContextMenuListener;
import android.view.ViewGroup;
import android.widget.AdapterView;

import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.HashMap;

final class FragmentState implements Parcelable {
    final String mClassName;
    final int mIndex;
    final boolean mFromLayout;
    final int mFragmentId;
    final int mContainerId;
    final String mTag;
    final boolean mRetainInstance;
    final boolean mDetached;
    final Bundle mArguments;
    
    Bundle mSavedFragmentState;
    
    Fragment mInstance;
    
    public FragmentState(Fragment frag) {
        mClassName = frag.getClass().getName();
        mIndex = frag.mIndex;
        mFromLayout = frag.mFromLayout;
        mFragmentId = frag.mFragmentId;
        mContainerId = frag.mContainerId;
        mTag = frag.mTag;
        mRetainInstance = frag.mRetainInstance;
        mDetached = frag.mDetached;
        mArguments = frag.mArguments;
    }
    
    public FragmentState(Parcel in) {
        mClassName = in.readString();
        mIndex = in.readInt();
        mFromLayout = in.readInt() != 0;
        mFragmentId = in.readInt();
        mContainerId = in.readInt();
        mTag = in.readString();
        mRetainInstance = in.readInt() != 0;
        mDetached = in.readInt() != 0;
        mArguments = in.readBundle();
        mSavedFragmentState = in.readBundle();
    }
    
    public Fragment instantiate(Activity activity, Fragment parent) {
        if (mInstance != null) {
            return mInstance;
        }
        
        if (mArguments != null) {
            mArguments.setClassLoader(activity.getClassLoader());
        }
        
        mInstance = Fragment.instantiate(activity, mClassName, mArguments);
        
        if (mSavedFragmentState != null) {
            mSavedFragmentState.setClassLoader(activity.getClassLoader());
            mInstance.mSavedFragmentState = mSavedFragmentState;
        }
        mInstance.setIndex(mIndex, parent);
        mInstance.mFromLayout = mFromLayout;
        mInstance.mRestored = true;
        mInstance.mFragmentId = mFragmentId;
        mInstance.mContainerId = mContainerId;
        mInstance.mTag = mTag;
        mInstance.mRetainInstance = mRetainInstance;
        mInstance.mDetached = mDetached;
        mInstance.mFragmentManager = activity.mFragments;
        if (FragmentManagerImpl.DEBUG) Log.v(FragmentManagerImpl.TAG,
                "Instantiated fragment " + mInstance);

        return mInstance;
    }
    
    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(mClassName);
        dest.writeInt(mIndex);
        dest.writeInt(mFromLayout ? 1 : 0);
        dest.writeInt(mFragmentId);
        dest.writeInt(mContainerId);
        dest.writeString(mTag);
        dest.writeInt(mRetainInstance ? 1 : 0);
        dest.writeInt(mDetached ? 1 : 0);
        dest.writeBundle(mArguments);
        dest.writeBundle(mSavedFragmentState);
    }
    
    public static final Parcelable.Creator<FragmentState> CREATOR
            = new Parcelable.Creator<FragmentState>() {
        public FragmentState createFromParcel(Parcel in) {
            return new FragmentState(in);
        }
        
        public FragmentState[] newArray(int size) {
            return new FragmentState[size];
        }
    };
}

/**
 * 片段是可以在{@link Activity 活动}中替换的，应用用户界面或行为的一小部分。
 * 与片段的交互通过 {@link FragmentManager} 完成，可以通过
 * {@link Activity#getFragmentManager() Activity.getFragmentManager()} 和
 * {@link Fragment#getFragmentManager() Fragment.getFragmentManager()}取得。
 *
 * <p>Fragment 类可以以多种方式使用，产生各种不同的结果。其核心是，
 * 运行于更大的 {@link Activity } 中，提供特殊的操作和接口。
 * 片段与其所在的活动紧密结合，不能单独使用。虽然片段定义了自己的生命周期，
 * 该生命周期依赖于其所在的活动：如果活动停止，其中的片段无法运行；
 * 当活动被销毁时，所以的片段也会被销毁。
 *
 * <p>片段的所有子类都必须包含一个公共的空构造函数。
 * 框架在需要的时候会重新实例化片段类，通常是在恢复状态期间，
 * 系统要找到该构造函数来进行实例化。如果空构造函数不存在，
 * 在状态恢复期间的某些情况下可能发生运行时异常。
 *
 * <p>这里包括的主题：
 * <ol>
 * <li><a href="#OlderPlatforms">旧平台</a>
 * <li><a href="#Lifecycle">生命周期</a>
 * <li><a href="#Layout">布局</a>
 * <li><a href="#BackStack">返回栈</a>
 * </ol>
 *
 * <div class="special reference">
 * <h3>开发者向导</h3>
 * <p>关于使用片段的更多信息，请阅读
 * <a href="{@docRoot}guide/topics/fundamentals/fragments.html">Fragments
 * </a> 开发者向导。</p>
 * </div>
 *
 * <a name="OlderPlatforms"></a>
 * <h3>旧平台</h3>
 *
 * Fragment API 引人自 {@link android.os.Build.VERSION_CODES#HONEYCOMB}，
 * 用于旧平台的版本可以通过 {@link android.support.v4.app.FragmentActivity} 使用。
 * 更多细节信息参见博客文章
 * <a href="http://android-developers.blogspot.com/2011/03/fragments-for-all.html">
 * 全平台可用的 Fragment</a> 。
 *
 * <a name="Lifecycle"></a>
 * <h3>生命周期</h3>
 *
 * <p>虽然片段的生命周期与拥有它的活动紧密结合，
 * 但它也在标准活动生命周期中留下了自己的痕迹。
 * 它包括基本的活动生命周期方法，比如 {@link #onResume}，重要的是，
 * 这些方法与活动的交互和 UI 生成相关联。
 *
 * <p>将片段带到 Resumed 状态的一系列核心生命周期方法如下：
 *
 * <ol>
 * <li> {@link #onAttach} 在片段关联到活动时调用一次。
 * <li> {@link #onCreate} 在初始创建片段时调用。
 * <li> {@link #onCreateView} 创建并返回关联到片段的视图层次。
 * <li> {@link #onActivityCreated} 通知片段，包含它的活动已经完成了其
 * {@link Activity#onCreate Activity.onCreate()} 的执行。
 * <li> {@link #onViewStateRestored} 通知片段，其视图层次保存的状态全部恢复完毕。
 * <li> {@link #onStart} 使片段对用户看见（基于包含它的活动被启动）。
 * <li> {@link #onResume} 使片段与用户交互（基于包含它的活动被恢复）
 * </ol>
 *
 * <p>当片段不再使用时，执行下述一系列的反向回调方法：
 *
 * <ol>
 * <li> {@link #onPause} 片段不再与用户交互，
 * 因为活动暂停或者活动通过片段操作正在编辑它。
 * <li> {@link #onStop} 片段的用户不再可见，
 * 因为活动已停止或者活动通过片段操作正在编辑它。
 * <li> {@link #onDestroyView} 允许片段清理关联到它的视图的资源。
 * <li> {@link #onDestroy} 调用来最终清理片段的状态。
 * <li> {@link #onDetach} 在片段不再关联到活动的前一刻调用。
 * </ol>
 *
 * <a name="Layout"></a>
 * <h3>布局</h3>
 *
 * <p>片段可以作为你的应用布局的一部分，允许你更好的模块化你的代码。
 * 并且更容易在运行中调整你的用户界面。作为示例，我们来看一个简单的程序，
 * 它包括项目列表并可以显示每条的详细信息。</p>
 *
 * <p>活动的布局 XML 文件中可以包含 <code>&lt;fragment&gt;</code> 标签，
 * 用于在布局中嵌入片段实例。例如，这是嵌入了一个片段的简单布局：</p>
 *
 * {@sample development/samples/ApiDemos/res/layout/fragment_layout.xml layout}
 *
 * <p>布局以正常的方式装入活动：</p>
 *
 * {@sample development/samples/ApiDemos/src/com/example/android/apis/app/FragmentLayout.java
 *      main}
 *
 * <p>相当简单的标题片段，显示了标题列表，它的大多数工作依赖于 
 * {@link ListFragment}。注意点击条目的实现：依赖于当前活动的布局，
 * 它也可以创建并显示一个新的片段，并在其中显示相信信息（后面会详细讨论），
 * 或者启动新的活动来显示详细信息。</p>
 *
 * {@sample development/samples/ApiDemos/src/com/example/android/apis/app/FragmentLayout.java
 *      titles}
 *
 * <p>详细片段显示选中条目的内容，
 * 仅仅是基于应用内部的字符串数组的索引显示文本字符串：</p>
 *
 * {@sample development/samples/ApiDemos/src/com/example/android/apis/app/FragmentLayout.java
 *      details}
 *
 * <p>当用户点击标题时，当前活动没有显示详细信息的容器，
 * 因此标题片段的点击代码启动新的活动来显示详细片段：</p>
 *
 * {@sample development/samples/ApiDemos/src/com/example/android/apis/app/FragmentLayout.java
 *      details_activity}
 *
 * <p>然而，屏幕大小可能足够显示标题列表和当前选中标题的内容。
 * 在横屏时使用该布局，该可选布局可以放在 layout-land 目录下：</p>
 *
 * {@sample development/samples/ApiDemos/res/layout-land/fragment_layout.xml layout}
 *
 * <p>注意如何调整之前的代码来使用该可选 UI 流程：标题片段要将详细片段嵌入到该活动中。
 * 如果运行时的配置是在内部显示详细信息，用于显示详细信息的活动会自行终止。
 *
 * <p>当配置变更导致这些片段的宿主活动重启时，其新实例可能使用缺少一些片段的，
 * 与之前不同的布局。这种情况下，之前的所有片段也仍然会在新的实例中被实例化并运行。
 * 然而，因为不再关联到 &lt;fragment&gt; 标签，因此在视图层次中不会创建它们的视图，
 * 并且 {@link #isInLayout} 会返回 false。（该代码还展示了，如何检测容器中的片段，
 * 已经脱离了容器的布局，以及如何避免在这种情况下创建视图层次。）
 * 
 * <p>&lt;fragment&gt; 标签的属性，用于控制将片段的视图加入到容器时的
 * LayoutParams。它们也可以由片段在 {@link #onInflate} 中作为参数处理。
 * 
 * <p>被实例化的片段必须具有某种唯一的标识符，以便于在容器活动需要销毁并重建时，
 * 可以重新与之前的实例建立关联。该操作以如下方式提供：
 * 
 * <ul>
 * <li> 如果没有明确的提供，则容器中的视图 ID 作为标识符；
 * <li><code>android:tag</code> 属性用在 &lt;fragment&gt; 中，为片段指定标签名；
 * <li><code>android:id</code> 属性用在 &lt;fragment&gt; 中，为片段指定标识符。
 * </ul>
 * 
 * <a name="BackStack"></a>
 * <h3>返回栈</h3>
 *
 * <p>变更了的片段间的切换可以放在拥有片段的活动内部的返回栈中。
 * 当用户按下活动的返回键，返回栈中的内容会在活动执行退出操作之前弹出。
 *
 * <p>例如，看一下由一个整型参数实例化，并在它的 UI 中的 TextView 中显示其值：</p>
 *
 * {@sample development/samples/ApiDemos/src/com/example/android/apis/app/FragmentStack.java
 *      fragment}
 *
 * <p>创建片段的新实例，替换任何当前显示的片段实例，并推送变更到返回栈中的函数可以这样写：
 *
 * {@sample development/samples/ApiDemos/src/com/example/android/apis/app/FragmentStack.java
 *      add_stack}
 *
 * <p>每次调用该函数，栈中就多一个新条目，按返回键会弹出，活动的 UI 返回用户之前的状态。
 */
public class Fragment implements ComponentCallbacks2, OnCreateContextMenuListener {
    private static final HashMap<String, Class<?>> sClassMap =
            new HashMap<String, Class<?>>();
    
    static final int INVALID_STATE = -1;   // Invalid state used as a null value.
    static final int INITIALIZING = 0;     // Not yet created.
    static final int CREATED = 1;          // Created.
    static final int ACTIVITY_CREATED = 2; // The activity has finished its creation.
    static final int STOPPED = 3;          // Fully created, not started.
    static final int STARTED = 4;          // Created and started, not resumed.
    static final int RESUMED = 5;          // Created started and resumed.
    
    int mState = INITIALIZING;
    
    // Non-null if the fragment's view hierarchy is currently animating away,
    // meaning we need to wait a bit on completely destroying it.  This is the
    // animation that is running.
    Animator mAnimatingAway;

    // If mAnimatingAway != null, this is the state we should move to once the
    // animation is done.
    int mStateAfterAnimating;

    // When instantiated from saved state, this is the saved state.
    Bundle mSavedFragmentState;
    SparseArray<Parcelable> mSavedViewState;
    
    // Index into active fragment array.
    int mIndex = -1;
    
    // Internal unique name for this fragment;
    String mWho;
    
    // Construction arguments;
    Bundle mArguments;

    // Target fragment.
    Fragment mTarget;

    // For use when retaining a fragment: this is the index of the last mTarget.
    int mTargetIndex = -1;

    // Target request code.
    int mTargetRequestCode;

    // True if the fragment is in the list of added fragments.
    boolean mAdded;
    
    // If set this fragment is being removed from its activity.
    boolean mRemoving;

    // True if the fragment is in the resumed state.
    boolean mResumed;
    
    // Set to true if this fragment was instantiated from a layout file.
    boolean mFromLayout;
    
    // Set to true when the view has actually been inflated in its layout.
    boolean mInLayout;

    // True if this fragment has been restored from previously saved state.
    boolean mRestored;
    
    // Number of active back stack entries this fragment is in.
    int mBackStackNesting;
    
    // The fragment manager we are associated with.  Set as soon as the
    // fragment is used in a transaction; cleared after it has been removed
    // from all transactions.
    FragmentManagerImpl mFragmentManager;

    // Activity this fragment is attached to.
    Activity mActivity;

    // Private fragment manager for child fragments inside of this one.
    FragmentManagerImpl mChildFragmentManager;

    // If this Fragment is contained in another Fragment, this is that container.
    Fragment mParentFragment;

    // The optional identifier for this fragment -- either the container ID if it
    // was dynamically added to the view hierarchy, or the ID supplied in
    // layout.
    int mFragmentId;
    
    // When a fragment is being dynamically added to the view hierarchy, this
    // is the identifier of the parent container it is being added to.
    int mContainerId;
    
    // The optional named tag for this fragment -- usually used to find
    // fragments that are not part of the layout.
    String mTag;
    
    // Set to true when the app has requested that this fragment be hidden
    // from the user.
    boolean mHidden;
    
    // Set to true when the app has requested that this fragment be detached.
    boolean mDetached;

    // If set this fragment would like its instance retained across
    // configuration changes.
    boolean mRetainInstance;
    
    // If set this fragment is being retained across the current config change.
    boolean mRetaining;
    
    // If set this fragment has menu items to contribute.
    boolean mHasMenu;

    // Set to true to allow the fragment's menu to be shown.
    boolean mMenuVisible = true;

    // Used to verify that subclasses call through to super class.
    boolean mCalled;
    
    // If app has requested a specific animation, this is the one to use.
    int mNextAnim;
    
    // The parent container of the fragment after dynamically added to UI.
    ViewGroup mContainer;
    
    // The View generated for this fragment.
    View mView;
    
    // Whether this fragment should defer starting until after other fragments
    // have been started and their loaders are finished.
    boolean mDeferStart;

    // Hint provided by the app that this fragment is currently visible to the user.
    boolean mUserVisibleHint = true;

    LoaderManagerImpl mLoaderManager;
    boolean mLoadersStarted;
    boolean mCheckedForLoaderManager;
    
    /**
     * 通过 {@link FragmentManager#saveFragmentInstanceState(Fragment)
     * FragmentManager.saveFragmentInstanceState} 函数从片段实例中取得的状态信息。
     */
    public static class SavedState implements Parcelable {
        final Bundle mState;

        SavedState(Bundle state) {
            mState = state;
        }

        SavedState(Parcel in, ClassLoader loader) {
            mState = in.readBundle();
            if (loader != null && mState != null) {
                mState.setClassLoader(loader);
            }
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeBundle(mState);
        }

        public static final Parcelable.ClassLoaderCreator<SavedState> CREATOR
                = new Parcelable.ClassLoaderCreator<SavedState>() {
            public SavedState createFromParcel(Parcel in) {
                return new SavedState(in, null);
            }

            public SavedState createFromParcel(Parcel in, ClassLoader loader) {
                return new SavedState(in, loader);
            }

            public SavedState[] newArray(int size) {
                return new SavedState[size];
            }
        };
    }

    /**
     * 当实例化失败时，由 {@link Fragment#instantiate(Context, String, Bundle)} 抛出。
     */
    static public class InstantiationException extends AndroidRuntimeException {
        public InstantiationException(String msg, Exception cause) {
            super(msg, cause);
        }
    }

    /**
     * 缺省构造函数。 <strong>每个</strong> 片段都必须有一个空构造函数，
     * 以便在恢复活动状态时可以实例化它。强烈建议不要在子类中其它包含
     * 参数的构造函数，因为在片段重新实例化时不会调用这些函数；
     * 作为代替，调用者通过 {@link #setArguments} 提供参数，
     * 随后片段通过 {@link #getArguments} 取得它们。
     * 
     * <p>应用一般不应该实现构造函数。片段准备好后，最先执行的应用代码在
     * {@link #onAttach(Activity)} 方法中，这是片段真正与活动关联的时间点。
     * 一些应用可能也想实现 {@link #onInflate} 方法，取得布局资源的属性，
     * 那么应该注意，因为该事件发生在片段关联到活动之后。
     */
    public Fragment() {
    }

    /**
     * 与 {@link #instantiate(Context, String, Bundle)} 类似，Bundle 参数为空。
     */
    public static Fragment instantiate(Context context, String fname) {
        return instantiate(context, fname, null);
    }

    /**
     * 使用给定类名创建片段的新实例。 这与调用其空构造函数是一样的。
     *
     * @param context 用于实例化片段的应用程序上下文。
     * 现在仅用于取得ClassLoader。
     * @param fname 要实例化的片段的类名。
     * @param args 提供给片段的参数捆包 Bundle，
     * 可以通过 {@link #getArguments()} 取得。可以为空。
     * @return 返回新的片段实例。
     * @throws InstantiationException 当实例化片段类失败时抛出。这是运行时异常，
     * 一般不会发生。
     */
    public static Fragment instantiate(Context context, String fname, Bundle args) {
        try {
            Class<?> clazz = sClassMap.get(fname);
            if (clazz == null) {
                // Class not found in the cache, see if it's real, and try to add it
                clazz = context.getClassLoader().loadClass(fname);
                sClassMap.put(fname, clazz);
            }
            Fragment f = (Fragment)clazz.newInstance();
            if (args != null) {
                args.setClassLoader(f.getClass().getClassLoader());
                f.mArguments = args;
            }
            return f;
        } catch (ClassNotFoundException e) {
            throw new InstantiationException("Unable to instantiate fragment " + fname
                    + ": make sure class name exists, is public, and has an"
                    + " empty constructor that is public", e);
        } catch (java.lang.InstantiationException e) {
            throw new InstantiationException("Unable to instantiate fragment " + fname
                    + ": make sure class name exists, is public, and has an"
                    + " empty constructor that is public", e);
        } catch (IllegalAccessException e) {
            throw new InstantiationException("Unable to instantiate fragment " + fname
                    + ": make sure class name exists, is public, and has an"
                    + " empty constructor that is public", e);
        }
    }
    
    final void restoreViewState(Bundle savedInstanceState) {
        if (mSavedViewState != null) {
            mView.restoreHierarchyState(mSavedViewState);
            mSavedViewState = null;
        }
        mCalled = false;
        onViewStateRestored(savedInstanceState);
        if (!mCalled) {
            throw new SuperNotCalledException("Fragment " + this
                    + " did not call through to super.onViewStateRestored()");
        }
    }

    final void setIndex(int index, Fragment parent) {
        mIndex = index;
        if (parent != null) {
            mWho = parent.mWho + ":" + mIndex;
        } else {
            mWho = "android:fragment:" + mIndex;
        }
    }

    final boolean isInBackStack() {
        return mBackStackNesting > 0;
    }

    /**
     * 子类不可覆盖 equals() 方法。
     */
    @Override final public boolean equals(Object o) {
        return super.equals(o);
    }

    /**
     * 子类不可覆盖 hashCode() 方法。
     */
    @Override final public int hashCode() {
        return super.hashCode();
    }
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(128);
        DebugUtils.buildShortClassTag(this, sb);
        if (mIndex >= 0) {
            sb.append(" #");
            sb.append(mIndex);
        }
        if (mFragmentId != 0) {
            sb.append(" id=0x");
            sb.append(Integer.toHexString(mFragmentId));
        }
        if (mTag != null) {
            sb.append(" ");
            sb.append(mTag);
        }
        sb.append('}');
        return sb.toString();
    }
    
    /**
     * 返回该片段的已知标识符。它是在布局中指定的 android:id 的值，
     * 或添加片段时的容器视图 ID。
     */
    final public int getId() {
        return mFragmentId;
    }
    
    /**
     * 取得片段的标签名，如果指定了。
     */
    final public String getTag() {
        return mTag;
    }
    
    /**
     * 为片段提供构造参数。该方法只能在片段关联到活动之前调用，就是说，
     * 你应该在构造片段后立即调用它。在片段销毁和重建之间该参数得到保持。
     */
    public void setArguments(Bundle args) {
        if (mIndex >= 0) {
            throw new IllegalStateException("Fragment already active");
        }
        mArguments = args;
    }

    /**
     * 返回片段实例化时得到的参数。
     */
    final public Bundle getArguments() {
        return mArguments;
    }

    /**
     * 设置片段首次构建后，用于恢复自己的初始保存状态，作为
     * {@link FragmentManager#saveFragmentInstanceState(Fragment)
     * FragmentManager.saveFragmentInstanceState} 的返回值。
     *
     * @param state 用于恢复的片段状态。
     */
    public void setInitialSavedState(SavedState state) {
        if (mIndex >= 0) {
            throw new IllegalStateException("Fragment already active");
        }
        mSavedFragmentState = state != null && state.mState != null
                ? state.mState : null;
    }

    /**
     * 该片段的可选目标。 可以用于，比如当片段由其它片段启动，
     * 在处理完后，要将结果返回给最初片段。这里设置的目标，
     * 通过 {@link FragmentManager#putFragment
     * FragmentManager.putFragment()} 在各个实例间保持。
     *
     * @param fragment 该对象的目标片段。
     * @param requestCode 可选的请求代码，便于在之后的回调函数
     *  {@link #onActivityResult(int, int, Intent)} 中处理。
     */
    public void setTargetFragment(Fragment fragment, int requestCode) {
        mTarget = fragment;
        mTargetRequestCode = requestCode;
    }

    /**
     * 返回由 {@link #setTargetFragment} 设置的目标片段。
     */
    final public Fragment getTargetFragment() {
        return mTarget;
    }

    /**
     * 返回由 {@link #setTargetFragment} 设置的请求代码。
     */
    final public int getTargetRequestCode() {
        return mTargetRequestCode;
    }

    /**
     * 返回与该片段关联的活动。
     */
    final public Activity getActivity() {
        return mActivity;
    }
    
    /**
     * 返回 <code>getActivity().getResources()</code> 的结果。
     */
    final public Resources getResources() {
        if (mActivity == null) {
            throw new IllegalStateException("Fragment " + this + " not attached to Activity");
        }
        return mActivity.getResources();
    }
    
    /**
     * 返回应用包的默认字符串表中已本地化、样式化完毕的 CharSequence。
     *
     * @param resId CharSequence 文本的资源标识符
     */
    public final CharSequence getText(int resId) {
        return getResources().getText(resId);
    }

    /**
     * 返回应用包的默认字符串表中已本地化的字符串。
     *
     * @param resId 字符串的资源标识符
     */
    public final String getString(int resId) {
        return getResources().getString(resId);
    }

    /**
     * 返回应用包的默认字符串表中已本地化，并已格式化的字符串，
     * 格式化参数在  {@link java.util.Formatter} 和
     * {@link java.lang.String#format} 中定义。
     *
     * @param resId 格式化字符串的资源标识符
     * @param formatArgs 格式化参数
     */

    public final String getString(int resId, Object... formatArgs) {
        return getResources().getString(resId, formatArgs);
    }

    /**
     * 返回用于与片段交互的，关联到该片段所在活动的 FragmentManager。
     * 注意，从片段放入 {@link FragmentTransaction} 到提交并关联到活动期间，
     * 该值先于 {@link #getActivity()} 成为非空。
     *
     * <p>如果该片段是其它片段的子片段，这里返回的 FragmentManager
     * 是其父片段的 {@link #getChildFragmentManager()} 的返回值。
     */
    final public FragmentManager getFragmentManager() {
        return mFragmentManager;
    }

    /**
     * 为放置和管理本片段内部的片段，返回私有 FragmentManager。
     */
    final public FragmentManager getChildFragmentManager() {
        if (mChildFragmentManager == null) {
            instantiateChildFragmentManager();
            if (mState >= RESUMED) {
                mChildFragmentManager.dispatchResume();
            } else if (mState >= STARTED) {
                mChildFragmentManager.dispatchStart();
            } else if (mState >= ACTIVITY_CREATED) {
                mChildFragmentManager.dispatchActivityCreated();
            } else if (mState >= CREATED) {
                mChildFragmentManager.dispatchCreate();
            }
        }
        return mChildFragmentManager;
    }

    /**
     * 返回包含此片段的父片段。 如果片段直接关联到活动，返回空。
     */
    final public Fragment getParentFragment() {
        return mParentFragment;
    }

    /**
     * 如果当前片段已添加到活动则返回真。
     */
    final public boolean isAdded() {
        return mActivity != null && mAdded;
    }

    /**
     * 如果片段已经明确的与 UI 解除了关联，返回真。意味着已经调用了
     * {@link FragmentTransaction#detach(Fragment)
     * FragmentTransaction.detach(Fragment)} 。
     */
    final public boolean isDetached() {
        return mDetached;
    }

    /**
     * 如果片段已经从活动中移除，返回真。这<em>不</em>意味着活动正要结束。
     * 而是代表片段处于从活动中被移除的过程中。
     */
    final public boolean isRemoving() {
        return mRemoving;
    }
    
    /**
     * 如果片段通过 &lt;fragment&gt; 标签，作为活动布局的一部分包含在视图层次中，
     * 返回真。通过 &lt;fragment&gt; 标签创建的片段，该值总为真，<em>除了</em>
     * 片段从之前的状态恢复后，没有出现在当前状态的布局中的情况以外。 
     */
    final public boolean isInLayout() {
        return mInLayout;
    }

    /**
     * 当片段处于 Resumed 状态时，返回真。 在 {@link #onResume()} 和
     * {@link #onPause()} 期间，该值为真。
     */
    final public boolean isResumed() {
        return mResumed;
    }
    
    /**
     * 当片段对用户可见时，返回真。 意味着：(1) 已添加，(2) 视图已关联到窗口，
     * (3) 没有处于隐藏状态。
     */
    final public boolean isVisible() {
        return isAdded() && !isHidden() && mView != null
                && mView.getWindowToken() != null && mView.getVisibility() == View.VISIBLE;
    }
    
    /**
     * 当片段处于隐藏状态时返回真。 默认片段是显示的。通过 {@link #onHiddenChanged}
     * 可以检知该状态的变更。注意，隐藏状态与其它状态是叠加的——就是说，
     * 片段要对用户可见，必须处于启动和非隐藏状态。
     */
    final public boolean isHidden() {
        return mHidden;
    }
    
    /**
     * Called when the hidden state (as returned by {@link #isHidden()} of
     * the fragment has changed.  Fragments start out not hidden; this will
     * be called whenever the fragment changes state from that.
     * @param hidden True if the fragment is now hidden, false if it is not
     * visible.
     */
    public void onHiddenChanged(boolean hidden) {
    }
    
    /**
     * 控制片段的实例在活动重建之间是否保持（比如配置变更时）。
     * 它只能用于不在返回栈中的片段。如果设置为真，在活动重新创建时，
     * 片段的生命周期会稍有不同：
     * <ul>
     * <li> 不会调用 {@link #onDestroy()} 方法 (但仍然会调用 {@link #onDetach()}，
     * 因为片段已经解除了与当前活动的关联)。
     * <li> 不会调用 {@link #onCreate(Bundle)}，因为片段不会重新创建。
     * <li> <b>仍然</b>会调用 {@link #onAttach(Activity)} 和
     *  {@link #onActivityCreated(Bundle)} 。
     * </ul>
     */
    public void setRetainInstance(boolean retain) {
        if (retain && mParentFragment != null) {
            throw new IllegalStateException(
                    "Can't retain fragements that are nested in other fragments");
        }
        mRetainInstance = retain;
    }
    
    final public boolean getRetainInstance() {
        return mRetainInstance;
    }
    
    /**
     * 指明该片段通过接收对 {@link #onCreateOptionsMenu} 及相关方法的调用，
     * 来添加选项菜单。
     * 
     * @param hasMenu 如果为真，则该片段会添加菜单项。
     */
    public void setHasOptionsMenu(boolean hasMenu) {
        if (mHasMenu != hasMenu) {
            mHasMenu = hasMenu;
            if (isAdded() && !isHidden()) {
                mFragmentManager.invalidateOptionsMenu();
            }
        }
    }

    /**
     * 设置该片段的菜单是否可见。 如你所知，在片段添加到视图层次、
     * 用户还没有看到它的时候，它的菜单项是不应该显示的。
     *
     * @param menuVisible 默认值为真，就是说通常片段的菜单是显示的。
     * 如果为假，则用户看不到菜单。
     */
    public void setMenuVisibility(boolean menuVisible) {
        if (mMenuVisible != menuVisible) {
            mMenuVisible = menuVisible;
            if (mHasMenu && isAdded() && !isHidden()) {
                mFragmentManager.invalidateOptionsMenu();
            }
        }
    }

    /**
     * 向系统中设置当前该片段对用户是否可见的标志。 该设置默认为真，
     * 该值在片段状态保存和恢复期间得到保持。
     *
     * <p>应用可以设置该值为假，代表片段滚动到可视区域外，或者对用户不直接可见。
     * 该值可用于系统对片段生命周期更新、载入顺序等操作的优化。</p>
     *
     * @param isVisibleToUser 为真，意味着片段UI当前对用户可见（默认值），
     *                        假代表不可见。
     */
    public void setUserVisibleHint(boolean isVisibleToUser) {
        if (!mUserVisibleHint && isVisibleToUser && mState < STARTED) {
            mFragmentManager.performPendingDeferredStart(this);
        }
        mUserVisibleHint = isVisibleToUser;
        mDeferStart = !isVisibleToUser;
    }

    /**
     * @return 该片段用户可见标志的当前值。
     * @see #setUserVisibleHint(boolean)
     */
    public boolean getUserVisibleHint() {
        return mUserVisibleHint;
    }

    /**
     * 返回当前片段的 LoaderManager，必要时创建它。
     */
    public LoaderManager getLoaderManager() {
        if (mLoaderManager != null) {
            return mLoaderManager;
        }
        if (mActivity == null) {
            throw new IllegalStateException("Fragment " + this + " not attached to Activity");
        }
        mCheckedForLoaderManager = true;
        mLoaderManager = mActivity.getLoaderManager(mWho, mLoadersStarted, true);
        return mLoaderManager;
    }

    /**
     * 调用包含该片段活动的 {@link Activity#startActivity(Intent)}。
     *
     * @param intent 执行的意图
     */
    public void startActivity(Intent intent) {
        startActivity(intent, null);
    }
    
    /**
     * 调用包含该片段活动的 {@link Activity#startActivity(Intent, Bundle)}。
     *
     * @param intent 执行的意图
     * @param options 活动如何启动的附加选项。
     * 更多细节参见 {@link android.content.Context#startActivity(Intent, Bundle)
     * Context.startActivity(Intent, Bundle)}。
     */
    public void startActivity(Intent intent, Bundle options) {
        if (mActivity == null) {
            throw new IllegalStateException("Fragment " + this + " not attached to Activity");
        }
        if (options != null) {
            mActivity.startActivityFromFragment(this, intent, -1, options);
        } else {
            // Note we want to go through this call for compatibility with
            // applications that may have overridden the method.
            mActivity.startActivityFromFragment(this, intent, -1);
        }
    }

    /**
     * 调用包含该片段活动的 {@link Activity#startActivityForResult(Intent, int)} 方法。
     */
    public void startActivityForResult(Intent intent, int requestCode) {
        startActivityForResult(intent, requestCode, null);
    }

    /**
     * 调用包含该片段活动的 {@link Activity#startActivityForResult(Intent, int, Bundle)} 方法。
     */
    public void startActivityForResult(Intent intent, int requestCode, Bundle options) {
        if (mActivity == null) {
            throw new IllegalStateException("Fragment " + this + " not attached to Activity");
        }
        if (options != null) {
            mActivity.startActivityFromFragment(this, intent, requestCode, options);
        } else {
            // Note we want to go through this call for compatibility with
            // applications that may have overridden the method.
            mActivity.startActivityFromFragment(this, intent, requestCode, options);
        }
    }
    
    /**
     * 接收之前调用 {@link #startActivityForResult(Intent, int)} 的结果。 
     * 该方法跟随关联的活动的 API {@link Activity#onActivityResult(int, int, Intent)} 被调用。
     * 
     * @param requestCode 调用 startActivityForResult() 时指定的整型请求代码，
     *                    允许你识别结果来自哪里。 
     * @param resultCode 子活动通过 setResult() 设置的整型返回值。
     * @param data A用于返回结果数据给调用者的意图（意图的“extras”可以存放各种数据）。
     */
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
    }
    
    /**
     * @hide Hack so that DialogFragment can make its Dialog before creating
     * its views, and the view construction can use the dialog's context for
     * inflation.  Maybe this should become a public API. Note sure.
     */
    public LayoutInflater getLayoutInflater(Bundle savedInstanceState) {
        return mActivity.getLayoutInflater();
    }
    
    /**
     * @deprecated 由 {@link #onInflate(Activity, AttributeSet, Bundle)} 代替。
     */
    @Deprecated
    public void onInflate(AttributeSet attrs, Bundle savedInstanceState) {
        mCalled = true;
    }

    /**
     * 在片段作为视图布局的一部分创建时调用，通常是在设置活动视图时。 
     * 该函数在根据布局文件的 <fragment> 标签创建片段后立即调用。
     * 注意，该函数在片段的 {@link #onAttach(Activity)} <em>之前</em>
     * 调用；你在这里能做的只有解析属性并保存。
     * 
     * <p>该调用在每次片段展开布局时都执行，即使在使用保存的状态生成新实例时。
     * 通常每次都实际执行参数解析工作，以便反应配置时的变更。</p>
     *
     * <p>这是片段的典型实现，可以取得通过属性和 {@link #getArguments()} 提供的参数。</p>
     *
     * {@sample development/samples/ApiDemos/src/com/example/android/apis/app/FragmentArguments.java
     *      fragment}
     *
     * <p>注意，XML 属性使用 "styleable" 资源来解析。这里定义了用到的样式。</p>
     *
     * {@sample development/samples/ApiDemos/res/values/attrs.xml fragment_arguments}
     * 
     * <p>片段可以在活动内容布局中像这样通过标签定义：</p>
     *
     * {@sample development/samples/ApiDemos/res/layout/fragment_arguments.xml from_attributes}
     *
     * <p>片段也可以根据运行时给定的参数捆包中的参数动态创建；这是在容器活动创建时的示例：</p>
     *
     * {@sample development/samples/ApiDemos/src/com/example/android/apis/app/FragmentArguments.java
     *      create}
     *
     * @param activity 展开该片段的活动。
     * @param attrs 被创建的片段标签中的属性。 
     * @param savedInstanceState 如果片段从之前保存的状态重新创建，这里是其状态。
     */
    public void onInflate(Activity activity, AttributeSet attrs, Bundle savedInstanceState) {
        onInflate(attrs, savedInstanceState);
        mCalled = true;
    }
    
    /**
     * 片段首次关联到活动时调用。该调用之后调用 {@link #onCreate(Bundle)}。
     */
    public void onAttach(Activity activity) {
        mCalled = true;
    }
    
    /**
     * 当片段载入动画时调用。
     */
    public Animator onCreateAnimator(int transit, boolean enter, int nextAnim) {
        return null;
    }
    
    /**
     * 片段最初创建时调用。 它在 {@link #onAttach(Activity)} 之后，
     * {@link #onCreateView(LayoutInflater, ViewGroup, Bundle)} 之前调用。
     * 
     * <p>注意，该函数可能在片段的活动仍处于创建的过程中时调用。
     * 因此，你不能假设在这个时点，活动内容的视图层次已经初始化了。
     * 如果你想在活动创建时执行，参见 {@link #onActivityCreated(Bundle)}。
     * 
     * @param savedInstanceState 如果片段由之前保存的状态重新创建，该值为其状态。
     */
    public void onCreate(Bundle savedInstanceState) {
        mCalled = true;
    }

    /**
     * Called to have the fragment instantiate its user interface view.
     * This is optional, and non-graphical fragments can return null (which
     * is the default implementation).  This will be called between
     * {@link #onCreate(Bundle)} and {@link #onActivityCreated(Bundle)}.
     * 
     * <p>If you return a View from here, you will later be called in
     * {@link #onDestroyView} when the view is being released.
     * 
     * @param inflater The LayoutInflater object that can be used to inflate
     * any views in the fragment,
     * @param container If non-null, this is the parent view that the fragment's
     * UI should be attached to.  The fragment should not add the view itself,
     * but this can be used to generate the LayoutParams of the view.
     * @param savedInstanceState If non-null, this fragment is being re-constructed
     * from a previous saved state as given here.
     * 
     * @return Return the View for the fragment's UI, or null.
     */
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        return null;
    }

    /**
     * Called immediately after {@link #onCreateView(LayoutInflater, ViewGroup, Bundle)}
     * has returned, but before any saved state has been restored in to the view.
     * This gives subclasses a chance to initialize themselves once
     * they know their view hierarchy has been completely created.  The fragment's
     * view hierarchy is not however attached to its parent at this point.
     * @param view The View returned by {@link #onCreateView(LayoutInflater, ViewGroup, Bundle)}.
     * @param savedInstanceState If non-null, this fragment is being re-constructed
     * from a previous saved state as given here.
     */
    public void onViewCreated(View view, Bundle savedInstanceState) {
    }
    
    /**
     * Get the root view for the fragment's layout (the one returned by {@link #onCreateView}),
     * if provided.
     * 
     * @return The fragment's root view, or null if it has no layout.
     */
    public View getView() {
        return mView;
    }
    
    /**
     * Called when the fragment's activity has been created and this
     * fragment's view hierarchy instantiated.  It can be used to do final
     * initialization once these pieces are in place, such as retrieving
     * views or restoring state.  It is also useful for fragments that use
     * {@link #setRetainInstance(boolean)} to retain their instance,
     * as this callback tells the fragment when it is fully associated with
     * the new activity instance.  This is called after {@link #onCreateView}
     * and before {@link #onViewStateRestored(Bundle)}.
     *
     * @param savedInstanceState If the fragment is being re-created from
     * a previous saved state, this is the state.
     */
    public void onActivityCreated(Bundle savedInstanceState) {
        mCalled = true;
    }

    /**
     * Called when all saved state has been restored into the view hierarchy
     * of the fragment.  This can be used to do initialization based on saved
     * state that you are letting the view hierarchy track itself, such as
     * whether check box widgets are currently checked.  This is called
     * after {@link #onActivityCreated(Bundle)} and before
     * {@link #onStart()}.
     *
     * @param savedInstanceState If the fragment is being re-created from
     * a previous saved state, this is the state.
     */
    public void onViewStateRestored(Bundle savedInstanceState) {
        mCalled = true;
    }

    /**
     * Called when the Fragment is visible to the user.  This is generally
     * tied to {@link Activity#onStart() Activity.onStart} of the containing
     * Activity's lifecycle.
     */
    public void onStart() {
        mCalled = true;
        
        if (!mLoadersStarted) {
            mLoadersStarted = true;
            if (!mCheckedForLoaderManager) {
                mCheckedForLoaderManager = true;
                mLoaderManager = mActivity.getLoaderManager(mWho, mLoadersStarted, false);
            }
            if (mLoaderManager != null) {
                mLoaderManager.doStart();
            }
        }
    }
    
    /**
     * Called when the fragment is visible to the user and actively running.
     * This is generally
     * tied to {@link Activity#onResume() Activity.onResume} of the containing
     * Activity's lifecycle.
     */
    public void onResume() {
        mCalled = true;
    }
    
    /**
     * Called to ask the fragment to save its current dynamic state, so it
     * can later be reconstructed in a new instance of its process is
     * restarted.  If a new instance of the fragment later needs to be
     * created, the data you place in the Bundle here will be available
     * in the Bundle given to {@link #onCreate(Bundle)},
     * {@link #onCreateView(LayoutInflater, ViewGroup, Bundle)}, and
     * {@link #onActivityCreated(Bundle)}.
     *
     * <p>This corresponds to {@link Activity#onSaveInstanceState(Bundle)
     * Activity.onSaveInstanceState(Bundle)} and most of the discussion there
     * applies here as well.  Note however: <em>this method may be called
     * at any time before {@link #onDestroy()}</em>.  There are many situations
     * where a fragment may be mostly torn down (such as when placed on the
     * back stack with no UI showing), but its state will not be saved until
     * its owning activity actually needs to save its state.
     *
     * @param outState Bundle in which to place your saved state.
     */
    public void onSaveInstanceState(Bundle outState) {
    }
    
    public void onConfigurationChanged(Configuration newConfig) {
        mCalled = true;
    }
    
    /**
     * Called when the Fragment is no longer resumed.  This is generally
     * tied to {@link Activity#onPause() Activity.onPause} of the containing
     * Activity's lifecycle.
     */
    public void onPause() {
        mCalled = true;
    }
    
    /**
     * Called when the Fragment is no longer started.  This is generally
     * tied to {@link Activity#onStop() Activity.onStop} of the containing
     * Activity's lifecycle.
     */
    public void onStop() {
        mCalled = true;
    }
    
    public void onLowMemory() {
        mCalled = true;
    }
    
    public void onTrimMemory(int level) {
        mCalled = true;
    }

    /**
     * Called when the view previously created by {@link #onCreateView} has
     * been detached from the fragment.  The next time the fragment needs
     * to be displayed, a new view will be created.  This is called
     * after {@link #onStop()} and before {@link #onDestroy()}.  It is called
     * <em>regardless</em> of whether {@link #onCreateView} returned a
     * non-null view.  Internally it is called after the view's state has
     * been saved but before it has been removed from its parent.
     */
    public void onDestroyView() {
        mCalled = true;
    }
    
    /**
     * Called when the fragment is no longer in use.  This is called
     * after {@link #onStop()} and before {@link #onDetach()}.
     */
    public void onDestroy() {
        mCalled = true;
        //Log.v("foo", "onDestroy: mCheckedForLoaderManager=" + mCheckedForLoaderManager
        //        + " mLoaderManager=" + mLoaderManager);
        if (!mCheckedForLoaderManager) {
            mCheckedForLoaderManager = true;
            mLoaderManager = mActivity.getLoaderManager(mWho, mLoadersStarted, false);
        }
        if (mLoaderManager != null) {
            mLoaderManager.doDestroy();
        }
    }

    /**
     * Called by the fragment manager once this fragment has been removed,
     * so that we don't have any left-over state if the application decides
     * to re-use the instance.  This only clears state that the framework
     * internally manages, not things the application sets.
     */
    void initState() {
        mIndex = -1;
        mWho = null;
        mAdded = false;
        mRemoving = false;
        mResumed = false;
        mFromLayout = false;
        mInLayout = false;
        mRestored = false;
        mBackStackNesting = 0;
        mFragmentManager = null;
        mActivity = null;
        mFragmentId = 0;
        mContainerId = 0;
        mTag = null;
        mHidden = false;
        mDetached = false;
        mRetaining = false;
        mLoaderManager = null;
        mLoadersStarted = false;
        mCheckedForLoaderManager = false;
    }

    /**
     * Called when the fragment is no longer attached to its activity.  This
     * is called after {@link #onDestroy()}.
     */
    public void onDetach() {
        mCalled = true;
    }
    
    /**
     * Initialize the contents of the Activity's standard options menu.  You
     * should place your menu items in to <var>menu</var>.  For this method
     * to be called, you must have first called {@link #setHasOptionsMenu}.  See
     * {@link Activity#onCreateOptionsMenu(Menu) Activity.onCreateOptionsMenu}
     * for more information.
     * 
     * @param menu The options menu in which you place your items.
     * 
     * @see #setHasOptionsMenu
     * @see #onPrepareOptionsMenu
     * @see #onOptionsItemSelected
     */
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
    }

    /**
     * Prepare the Screen's standard options menu to be displayed.  This is
     * called right before the menu is shown, every time it is shown.  You can
     * use this method to efficiently enable/disable items or otherwise
     * dynamically modify the contents.  See
     * {@link Activity#onPrepareOptionsMenu(Menu) Activity.onPrepareOptionsMenu}
     * for more information.
     * 
     * @param menu The options menu as last shown or first initialized by
     *             onCreateOptionsMenu().
     * 
     * @see #setHasOptionsMenu
     * @see #onCreateOptionsMenu
     */
    public void onPrepareOptionsMenu(Menu menu) {
    }

    /**
     * Called when this fragment's option menu items are no longer being
     * included in the overall options menu.  Receiving this call means that
     * the menu needed to be rebuilt, but this fragment's items were not
     * included in the newly built menu (its {@link #onCreateOptionsMenu(Menu, MenuInflater)}
     * was not called).
     */
    public void onDestroyOptionsMenu() {
    }
    
    /**
     * This hook is called whenever an item in your options menu is selected.
     * The default implementation simply returns false to have the normal
     * processing happen (calling the item's Runnable or sending a message to
     * its Handler as appropriate).  You can use this method for any items
     * for which you would like to do processing without those other
     * facilities.
     * 
     * <p>Derived classes should call through to the base class for it to
     * perform the default menu handling.
     * 
     * @param item The menu item that was selected.
     * 
     * @return boolean Return false to allow normal menu processing to
     *         proceed, true to consume it here.
     * 
     * @see #onCreateOptionsMenu
     */
    public boolean onOptionsItemSelected(MenuItem item) {
        return false;
    }

    /**
     * This hook is called whenever the options menu is being closed (either by the user canceling
     * the menu with the back/menu button, or when an item is selected).
     *  
     * @param menu The options menu as last shown or first initialized by
     *             onCreateOptionsMenu().
     */
    public void onOptionsMenuClosed(Menu menu) {
    }
    
    /**
     * Called when a context menu for the {@code view} is about to be shown.
     * Unlike {@link #onCreateOptionsMenu}, this will be called every
     * time the context menu is about to be shown and should be populated for
     * the view (or item inside the view for {@link AdapterView} subclasses,
     * this can be found in the {@code menuInfo})).
     * <p>
     * Use {@link #onContextItemSelected(android.view.MenuItem)} to know when an
     * item has been selected.
     * <p>
     * The default implementation calls up to
     * {@link Activity#onCreateContextMenu Activity.onCreateContextMenu}, though
     * you can not call this implementation if you don't want that behavior.
     * <p>
     * It is not safe to hold onto the context menu after this method returns.
     * {@inheritDoc}
     */
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
        getActivity().onCreateContextMenu(menu, v, menuInfo);
    }

    /**
     * Registers a context menu to be shown for the given view (multiple views
     * can show the context menu). This method will set the
     * {@link OnCreateContextMenuListener} on the view to this fragment, so
     * {@link #onCreateContextMenu(ContextMenu, View, ContextMenuInfo)} will be
     * called when it is time to show the context menu.
     * 
     * @see #unregisterForContextMenu(View)
     * @param view The view that should show a context menu.
     */
    public void registerForContextMenu(View view) {
        view.setOnCreateContextMenuListener(this);
    }
    
    /**
     * Prevents a context menu to be shown for the given view. This method will
     * remove the {@link OnCreateContextMenuListener} on the view.
     * 
     * @see #registerForContextMenu(View)
     * @param view The view that should stop showing a context menu.
     */
    public void unregisterForContextMenu(View view) {
        view.setOnCreateContextMenuListener(null);
    }
    
    /**
     * This hook is called whenever an item in a context menu is selected. The
     * default implementation simply returns false to have the normal processing
     * happen (calling the item's Runnable or sending a message to its Handler
     * as appropriate). You can use this method for any items for which you
     * would like to do processing without those other facilities.
     * <p>
     * Use {@link MenuItem#getMenuInfo()} to get extra information set by the
     * View that added this menu item.
     * <p>
     * Derived classes should call through to the base class for it to perform
     * the default menu handling.
     * 
     * @param item The context menu item that was selected.
     * @return boolean Return false to allow normal context menu processing to
     *         proceed, true to consume it here.
     */
    public boolean onContextItemSelected(MenuItem item) {
        return false;
    }
    
    /**
     * Print the Fragments's state into the given stream.
     *
     * @param prefix Text to print at the front of each line.
     * @param fd The raw file descriptor that the dump is being sent to.
     * @param writer The PrintWriter to which you should dump your state.  This will be
     * closed for you after you return.
     * @param args additional arguments to the dump request.
     */
    public void dump(String prefix, FileDescriptor fd, PrintWriter writer, String[] args) {
        writer.print(prefix); writer.print("mFragmentId=#");
                writer.print(Integer.toHexString(mFragmentId));
                writer.print(" mContainerId=#");
                writer.print(Integer.toHexString(mContainerId));
                writer.print(" mTag="); writer.println(mTag);
        writer.print(prefix); writer.print("mState="); writer.print(mState);
                writer.print(" mIndex="); writer.print(mIndex);
                writer.print(" mWho="); writer.print(mWho);
                writer.print(" mBackStackNesting="); writer.println(mBackStackNesting);
        writer.print(prefix); writer.print("mAdded="); writer.print(mAdded);
                writer.print(" mRemoving="); writer.print(mRemoving);
                writer.print(" mResumed="); writer.print(mResumed);
                writer.print(" mFromLayout="); writer.print(mFromLayout);
                writer.print(" mInLayout="); writer.println(mInLayout);
        writer.print(prefix); writer.print("mHidden="); writer.print(mHidden);
                writer.print(" mDetached="); writer.print(mDetached);
                writer.print(" mMenuVisible="); writer.print(mMenuVisible);
                writer.print(" mHasMenu="); writer.println(mHasMenu);
        writer.print(prefix); writer.print("mRetainInstance="); writer.print(mRetainInstance);
                writer.print(" mRetaining="); writer.print(mRetaining);
                writer.print(" mUserVisibleHint="); writer.println(mUserVisibleHint);
        if (mFragmentManager != null) {
            writer.print(prefix); writer.print("mFragmentManager=");
                    writer.println(mFragmentManager);
        }
        if (mActivity != null) {
            writer.print(prefix); writer.print("mActivity=");
                    writer.println(mActivity);
        }
        if (mParentFragment != null) {
            writer.print(prefix); writer.print("mParentFragment=");
                    writer.println(mParentFragment);
        }
        if (mArguments != null) {
            writer.print(prefix); writer.print("mArguments="); writer.println(mArguments);
        }
        if (mSavedFragmentState != null) {
            writer.print(prefix); writer.print("mSavedFragmentState=");
                    writer.println(mSavedFragmentState);
        }
        if (mSavedViewState != null) {
            writer.print(prefix); writer.print("mSavedViewState=");
                    writer.println(mSavedViewState);
        }
        if (mTarget != null) {
            writer.print(prefix); writer.print("mTarget="); writer.print(mTarget);
                    writer.print(" mTargetRequestCode=");
                    writer.println(mTargetRequestCode);
        }
        if (mNextAnim != 0) {
            writer.print(prefix); writer.print("mNextAnim="); writer.println(mNextAnim);
        }
        if (mContainer != null) {
            writer.print(prefix); writer.print("mContainer="); writer.println(mContainer);
        }
        if (mView != null) {
            writer.print(prefix); writer.print("mView="); writer.println(mView);
        }
        if (mAnimatingAway != null) {
            writer.print(prefix); writer.print("mAnimatingAway="); writer.println(mAnimatingAway);
            writer.print(prefix); writer.print("mStateAfterAnimating=");
                    writer.println(mStateAfterAnimating);
        }
        if (mLoaderManager != null) {
            writer.print(prefix); writer.println("Loader Manager:");
            mLoaderManager.dump(prefix + "  ", fd, writer, args);
        }
        if (mChildFragmentManager != null) {
            writer.print(prefix); writer.println("Child " + mChildFragmentManager + ":");
            mChildFragmentManager.dump(prefix + "  ", fd, writer, args);
        }
    }

    Fragment findFragmentByWho(String who) {
        if (who.equals(mWho)) {
            return this;
        }
        if (mChildFragmentManager != null) {
            return mChildFragmentManager.findFragmentByWho(who);
        }
        return null;
    }

    void instantiateChildFragmentManager() {
        mChildFragmentManager = new FragmentManagerImpl();
        mChildFragmentManager.attachActivity(mActivity, new FragmentContainer() {
            @Override
            public View findViewById(int id) {
                if (mView == null) {
                    throw new IllegalStateException("Fragment does not have a view");
                }
                return mView.findViewById(id);
            }
        }, this);
    }

    void performCreate(Bundle savedInstanceState) {
        if (mChildFragmentManager != null) {
            mChildFragmentManager.noteStateNotSaved();
        }
        mCalled = false;
        onCreate(savedInstanceState);
        if (!mCalled) {
            throw new SuperNotCalledException("Fragment " + this
                    + " did not call through to super.onCreate()");
        }
        if (savedInstanceState != null) {
            Parcelable p = savedInstanceState.getParcelable(Activity.FRAGMENTS_TAG);
            if (p != null) {
                if (mChildFragmentManager == null) {
                    instantiateChildFragmentManager();
                }
                mChildFragmentManager.restoreAllState(p, null);
                mChildFragmentManager.dispatchCreate();
            }
        }
    }

    View performCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        if (mChildFragmentManager != null) {
            mChildFragmentManager.noteStateNotSaved();
        }
        return onCreateView(inflater, container, savedInstanceState);
    }

    void performActivityCreated(Bundle savedInstanceState) {
        if (mChildFragmentManager != null) {
            mChildFragmentManager.noteStateNotSaved();
        }
        mCalled = false;
        onActivityCreated(savedInstanceState);
        if (!mCalled) {
            throw new SuperNotCalledException("Fragment " + this
                    + " did not call through to super.onActivityCreated()");
        }
        if (mChildFragmentManager != null) {
            mChildFragmentManager.dispatchActivityCreated();
        }
    }

    void performStart() {
        if (mChildFragmentManager != null) {
            mChildFragmentManager.noteStateNotSaved();
            mChildFragmentManager.execPendingActions();
        }
        mCalled = false;
        onStart();
        if (!mCalled) {
            throw new SuperNotCalledException("Fragment " + this
                    + " did not call through to super.onStart()");
        }
        if (mChildFragmentManager != null) {
            mChildFragmentManager.dispatchStart();
        }
        if (mLoaderManager != null) {
            mLoaderManager.doReportStart();
        }
    }

    void performResume() {
        if (mChildFragmentManager != null) {
            mChildFragmentManager.noteStateNotSaved();
            mChildFragmentManager.execPendingActions();
        }
        mCalled = false;
        onResume();
        if (!mCalled) {
            throw new SuperNotCalledException("Fragment " + this
                    + " did not call through to super.onResume()");
        }
        if (mChildFragmentManager != null) {
            mChildFragmentManager.dispatchResume();
            mChildFragmentManager.execPendingActions();
        }
    }

    void performConfigurationChanged(Configuration newConfig) {
        onConfigurationChanged(newConfig);
        if (mChildFragmentManager != null) {
            mChildFragmentManager.dispatchConfigurationChanged(newConfig);
        }
    }

    void performLowMemory() {
        onLowMemory();
        if (mChildFragmentManager != null) {
            mChildFragmentManager.dispatchLowMemory();
        }
    }

    void performTrimMemory(int level) {
        onTrimMemory(level);
        if (mChildFragmentManager != null) {
            mChildFragmentManager.dispatchTrimMemory(level);
        }
    }

    boolean performCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        boolean show = false;
        if (!mHidden) {
            if (mHasMenu && mMenuVisible) {
                show = true;
                onCreateOptionsMenu(menu, inflater);
            }
            if (mChildFragmentManager != null) {
                show |= mChildFragmentManager.dispatchCreateOptionsMenu(menu, inflater);
            }
        }
        return show;
    }

    boolean performPrepareOptionsMenu(Menu menu) {
        boolean show = false;
        if (!mHidden) {
            if (mHasMenu && mMenuVisible) {
                show = true;
                onPrepareOptionsMenu(menu);
            }
            if (mChildFragmentManager != null) {
                show |= mChildFragmentManager.dispatchPrepareOptionsMenu(menu);
            }
        }
        return show;
    }

    boolean performOptionsItemSelected(MenuItem item) {
        if (!mHidden) {
            if (mHasMenu && mMenuVisible) {
                if (onOptionsItemSelected(item)) {
                    return true;
                }
            }
            if (mChildFragmentManager != null) {
                if (mChildFragmentManager.dispatchOptionsItemSelected(item)) {
                    return true;
                }
            }
        }
        return false;
    }

    boolean performContextItemSelected(MenuItem item) {
        if (!mHidden) {
            if (onContextItemSelected(item)) {
                return true;
            }
            if (mChildFragmentManager != null) {
                if (mChildFragmentManager.dispatchContextItemSelected(item)) {
                    return true;
                }
            }
        }
        return false;
    }

    void performOptionsMenuClosed(Menu menu) {
        if (!mHidden) {
            if (mHasMenu && mMenuVisible) {
                onOptionsMenuClosed(menu);
            }
            if (mChildFragmentManager != null) {
                mChildFragmentManager.dispatchOptionsMenuClosed(menu);
            }
        }
    }

    void performSaveInstanceState(Bundle outState) {
        onSaveInstanceState(outState);
        if (mChildFragmentManager != null) {
            Parcelable p = mChildFragmentManager.saveAllState();
            if (p != null) {
                outState.putParcelable(Activity.FRAGMENTS_TAG, p);
            }
        }
    }

    void performPause() {
        if (mChildFragmentManager != null) {
            mChildFragmentManager.dispatchPause();
        }
        mCalled = false;
        onPause();
        if (!mCalled) {
            throw new SuperNotCalledException("Fragment " + this
                    + " did not call through to super.onPause()");
        }
    }

    void performStop() {
        if (mChildFragmentManager != null) {
            mChildFragmentManager.dispatchStop();
        }
        mCalled = false;
        onStop();
        if (!mCalled) {
            throw new SuperNotCalledException("Fragment " + this
                    + " did not call through to super.onStop()");
        }
        
        if (mLoadersStarted) {
            mLoadersStarted = false;
            if (!mCheckedForLoaderManager) {
                mCheckedForLoaderManager = true;
                mLoaderManager = mActivity.getLoaderManager(mWho, mLoadersStarted, false);
            }
            if (mLoaderManager != null) {
                if (mActivity == null || !mActivity.mChangingConfigurations) {
                    mLoaderManager.doStop();
                } else {
                    mLoaderManager.doRetain();
                }
            }
        }
    }

    void performDestroyView() {
        if (mChildFragmentManager != null) {
            mChildFragmentManager.dispatchDestroyView();
        }
        mCalled = false;
        onDestroyView();
        if (!mCalled) {
            throw new SuperNotCalledException("Fragment " + this
                    + " did not call through to super.onDestroyView()");
        }
        if (mLoaderManager != null) {
            mLoaderManager.doReportNextStart();
        }
    }

    void performDestroy() {
        if (mChildFragmentManager != null) {
            mChildFragmentManager.dispatchDestroy();
        }
        mCalled = false;
        onDestroy();
        if (!mCalled) {
            throw new SuperNotCalledException("Fragment " + this
                    + " did not call through to super.onDestroy()");
        }
    }
}
