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

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

/**
 * <p>根据过滤模式限制数据的过滤器.</p>
 * <p>过滤器通常由实现了 {@link android.widget.Filterable} 接口的子类来生成.</p>
 *
 * <p>过滤操作是通过调用 {@link #filter(CharSequence)} 或
 * {@link #filter(CharSequence, android.widget.Filter.FilterListener)}
 * 这些异步方法来完成的.调用方法后，过滤请求会递交到请求队列中等待处理.
 * 任何对以上方法的调用，都将取消之前未执行的过滤请求.</p>
 * @see android.widget.Filterable
 * @author translate by henly.zhang
 * @author translate by cnmahj
 * @author convert by cnmahj
 */
public abstract class Filter {
    private static final String LOG_TAG = "Filter";
    
    private static final String THREAD_NAME = "Filter";
    private static final int FILTER_TOKEN = 0xD0D0F00D;
    private static final int FINISH_TOKEN = 0xDEADBEEF;

    private Handler mThreadHandler;
    private Handler mResultHandler;

    private Delayer mDelayer;

    private final Object mLock = new Object();

    /**
     * <p>创建一个新的异步过滤器.</p>	
     */
    public Filter() {
        mResultHandler = new ResultsHandler();
    }

    /**
     * Provide an interface that decides how long to delay the message for a given query.  Useful
     * for heuristics such as posting a delay for the delete key to avoid doing any work while the
     * user holds down the delete key.
     *
     * @param delayer The delayer.
     * @hide
     */
    public void setDelayer(Delayer delayer) {
        synchronized (mLock) {
            mDelayer = delayer;
        }
    }

    /**
     * <p>启动异步过滤操作.对该方法的调用将取消之前队列中等待处理的过滤请求，
     * 并递交新的过滤请求等待执行.</p>
     *
     * @param constraint 过滤数据的约束条件
     *
     * @see #filter(CharSequence, android.widget.Filter.FilterListener)
     */
    public final void filter(CharSequence constraint) {
        filter(constraint, null);
    }

    /**
     * <p>启动异步过滤操作.对该方法的调用将取消之前队列中等待处理的过滤请求，
     * 并递交新的过滤请求等待执行.</p>
     * <p>完成过滤操作之后，通知监听器.</p> 
     *
     * @param constraint 过滤数据的约束条件
     * @param listener 过滤操作完成后发出的通知的监听器
     *
     * @see #filter(CharSequence)
     * @see #performFiltering(CharSequence)
     * @see #publishResults(CharSequence, android.widget.Filter.FilterResults)
     */
    public final void filter(CharSequence constraint, FilterListener listener) {
        synchronized (mLock) {
            if (mThreadHandler == null) {
                HandlerThread thread = new HandlerThread(
                        THREAD_NAME, android.os.Process.THREAD_PRIORITY_BACKGROUND);
                thread.start();
                mThreadHandler = new RequestHandler(thread.getLooper());
            }

            final long delay = (mDelayer == null) ? 0 : mDelayer.getPostingDelay(constraint);
            
            Message message = mThreadHandler.obtainMessage(FILTER_TOKEN);
    
            RequestArguments args = new RequestArguments();
            // make sure we use an immutable copy of the constraint, so that
            // it doesn't change while the filter operation is in progress
            args.constraint = constraint != null ? constraint.toString() : null;
            args.listener = listener;
            message.obj = args;
    
            mThreadHandler.removeMessages(FILTER_TOKEN);
            mThreadHandler.removeMessages(FINISH_TOKEN);
            mThreadHandler.sendMessageDelayed(message, delay);
        }
    }

    /**
     * <p>根据约束条件调用工作线程过滤数据.子类必须实现该方法来执行过滤操作.
     * 过滤结果以 {@link android.widget.Filter.FilterResults} 形式返回，
     * 通过 {@link #publishResults(CharSequence, android.widget.Filter.FilterResults)}
     * 发布到 UI 线程.</p>
     *
     * <p><strong>约定：</strong> 当约束条件为 null 时，必须恢复原始数据.</p>
     *
     * @param constraint 用于过滤数据的约束条件
     * @return 过滤结果
     *
     * @see #filter(CharSequence, android.widget.Filter.FilterListener)
     * @see #publishResults(CharSequence, android.widget.Filter.FilterResults)
     * @see android.widget.Filter.FilterResults
     */
    protected abstract FilterResults performFiltering(CharSequence constraint);

    /**
     * <p>在 UI 线程中执行，用于向用户接口发布过滤结果.子类必须实现该方法，
     * 用于发布 {@link #performFiltering} 的处理结果.</p>
     * @param constraint 用于过滤数据的约束条件
     * @param results 过滤操作的结果
     *
     * @see #filter(CharSequence, android.widget.Filter.FilterListener)
     * @see #performFiltering(CharSequence)
     * @see android.widget.Filter.FilterResults
     */
    protected abstract void publishResults(CharSequence constraint,
            FilterResults results);

    /**
     * 将已过滤的集合的内容转换为 CharSequence.子类必须实现该方法，以转换处理结果.
     * <p>默认实现是对于 null 返回空字符串，其他的返回参数的默认字符串.</p>
     * @param resultValue 要转换为 CharSequence 文本的对象
     * @return 代表转换结果的	CharSequence.
     */
    public CharSequence convertResultToString(Object resultValue) {
        return resultValue == null ? "" : resultValue.toString();
    }

    /**
     * <p>用于保持过滤操作的结果.结果包含过滤操作的结果及其个数.</p>
     */
    protected static class FilterResults {
        public FilterResults() {
            // nothing to see here
        }

        /**
         * <p>包含过滤操作的所有结果.</p>
         * 
         */
        public Object values;

        /**
         * <p>包含过滤操作的结果数量.</p>	
         */
        public int count;
    }

    /**
     * <p>用于接受过滤操作完成后发出的通知的监听器.</p>
     */
    public static interface FilterListener {
        /**
         * <p>过滤操作结束的通知.</p>	
         *
         * @param count 过滤结果的数量.
         */
        public void onFilterComplete(int count);
    }

    /**
     * <p>Worker thread handler. When a new filtering request is posted from
     * {@link android.widget.Filter#filter(CharSequence, android.widget.Filter.FilterListener)},
     * it is sent to this handler.</p>
     */
    private class RequestHandler extends Handler {
        public RequestHandler(Looper looper) {
            super(looper);
        }
        
        /**
         * <p>Handles filtering requests by calling
         * {@link Filter#performFiltering} and then sending a message
         * with the results to the results handler.</p>
         *
         * @param msg the filtering request
         */
        public void handleMessage(Message msg) {
            int what = msg.what;
            Message message;
            switch (what) {
                case FILTER_TOKEN:
                    RequestArguments args = (RequestArguments) msg.obj;
                    try {
                        args.results = performFiltering(args.constraint);
                    } catch (Exception e) {
                        args.results = new FilterResults();
                        Log.w(LOG_TAG, "An exception occured during performFiltering()!", e);
                    } finally {
                        message = mResultHandler.obtainMessage(what);
                        message.obj = args;
                        message.sendToTarget();
                    }

                    synchronized (mLock) {
                        if (mThreadHandler != null) {
                            Message finishMessage = mThreadHandler.obtainMessage(FINISH_TOKEN);
                            mThreadHandler.sendMessageDelayed(finishMessage, 3000);
                        }
                    }
                    break;
                case FINISH_TOKEN:
                    synchronized (mLock) {
                        if (mThreadHandler != null) {
                            mThreadHandler.getLooper().quit();
                            mThreadHandler = null;
                        }
                    }
                    break;
            }
        }
    }

    /**
     * <p>Handles the results of a filtering operation. The results are
     * handled in the UI thread.</p>
     */
    private class ResultsHandler extends Handler {
        /**
         * <p>Messages received from the request handler are processed in the
         * UI thread. The processing involves calling
         * {@link Filter#publishResults(CharSequence,
         * android.widget.Filter.FilterResults)}
         * to post the results back in the UI and then notifying the listener,
         * if any.</p> 
         *
         * @param msg the filtering results
         */
        @Override
        public void handleMessage(Message msg) {
            RequestArguments args = (RequestArguments) msg.obj;

            publishResults(args.constraint, args.results);
            if (args.listener != null) {
                int count = args.results != null ? args.results.count : -1;
                args.listener.onFilterComplete(count);
            }
        }
    }

    /**
     * <p>Holds the arguments of a filtering request as well as the results
     * of the request.</p>
     */
    private static class RequestArguments {
        /**
         * <p>The constraint used to filter the data.</p>
         */
        CharSequence constraint;

        /**
         * <p>The listener to notify upon completion. Can be null.</p>
         */
        FilterListener listener;

        /**
         * <p>The results of the filtering operation.</p>
         */
        FilterResults results;
    }

    /**
     * @hide
     */
    public interface Delayer {

        /**
         * @param constraint The constraint passed to {@link Filter#filter(CharSequence)}
         * @return The delay that should be used for
         *         {@link Handler#sendMessageDelayed(android.os.Message, long)}
         */
        long getPostingDelay(CharSequence constraint);
    }
}
