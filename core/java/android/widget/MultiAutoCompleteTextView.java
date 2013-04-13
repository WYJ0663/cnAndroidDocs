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

import android.content.Context;
import android.text.Editable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.method.QwertyKeyListener;
import android.util.AttributeSet;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;

/**
 * 一个继承自 {@link AutoCompleteTextView} 的可编辑的文本视图，
 * 能够根据用户的输入进行自动完成提示，而不需要用户输入整个内容.
 * <p>
 * 你必须提供 {@link Tokenizer} 用于查找不同的子串.
 *
 * <p>下面的代码片段展示了，如何创建根据用户输入的国家名称进行完成提示的文本视图：</p>
 * <pre class="prettyprint">
 * public class CountriesActivity extends Activity {
 *     protected void onCreate(Bundle savedInstanceState) {
 *         super.onCreate(savedInstanceState);
 *         setContentView(R.layout.autocomplete_7);
 *
 *         ArrayAdapter&lt;String&gt; adapter = new ArrayAdapter&lt;String&gt;(this,
 *                 android.R.layout.simple_dropdown_item_1line, COUNTRIES);
 *         MultiAutoCompleteTextView textView = (MultiAutoCompleteTextView) findViewById(R.id.edit);
 *         textView.setAdapter(adapter);
 *         textView.setTokenizer(new MultiAutoCompleteTextView.CommaTokenizer());
 *     }
 *
 *     private static final String[] COUNTRIES = new String[] {
 *         "比利时", "法国", "意大利", "德国", "西班牙"
 *     };
 * }</pre>
 * @author translate by 颖哥儿
 * @author translate by cnmahj
 * @author convert by cnmahj
 */

public class MultiAutoCompleteTextView extends AutoCompleteTextView {
    private Tokenizer mTokenizer;

    public MultiAutoCompleteTextView(Context context) {
        this(context, null);
    }

    public MultiAutoCompleteTextView(Context context, AttributeSet attrs) {
        this(context, attrs, com.android.internal.R.attr.autoCompleteTextViewStyle);
    }

    public MultiAutoCompleteTextView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    /* package */ void finishInit() { }

    /**
     * 设置用于根据用户输入的文本确定相关范围的分解器.
     */
    public void setTokenizer(Tokenizer t) {
        mTokenizer = t;
    }

    /**
     * 该方法不筛选编辑框中的所有内容，只筛选 {@link Tokenizer#findTokenStart} 
     * 到 {@link #getSelectionEnd} 的长度大于等于 {@link #getThreshold} 的内容.
     */
    @Override
    protected void performFiltering(CharSequence text, int keyCode) {
        if (enoughToFilter()) {
            int end = getSelectionEnd();
            int start = mTokenizer.findTokenStart(text, end);

            performFiltering(text, start, end, keyCode);
        } else {
            dismissDropDown();

            Filter f = getFilter();
            if (f != null) {
                f.filter(null);
            }
        }
    }

    /**
     * 该方法不根据编辑框中的文本长度来判断，而是根据 {@link Tokenizer#findTokenStart} 
     * 到 {@link #getSelectionEnd} 的长度是否大于等于 {@link #getThreshold} 来判断.
     */
    @Override
    public boolean enoughToFilter() {
        Editable text = getText();

        int end = getSelectionEnd();
        if (end < 0 || mTokenizer == null) {
            return false;
        }

        int start = mTokenizer.findTokenStart(text, end);

        if (end - start >= getThreshold()) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * 该方法不验证编辑框中的整个文本，而是逐个验证文本标记.空标记将被移除.
     */
    @Override
    public void performValidation() {
        Validator v = getValidator();

        if (v == null || mTokenizer == null) {
            return;
        }

        Editable e = getText();
        int i = getText().length();
        while (i > 0) {
            int start = mTokenizer.findTokenStart(e, i);
            int end = mTokenizer.findTokenEnd(e, start);

            CharSequence sub = e.subSequence(start, end);
            if (TextUtils.isEmpty(sub)) {
                e.replace(start, i, "");
            } else if (!v.isValid(sub)) {
                e.replace(start, i,
                          mTokenizer.terminateToken(v.fixText(sub)));
            }

            i = start;
        }
    }

    /**
     * <p>对下拉列表中的内容进行筛选.采用的模式是利用编辑框对指定范围的文本进行筛选.
     * （The filtering pattern is the specified range of text from the edit box）
     * 子类可覆盖此方法，以便于采用一个不同的模式.
     * 例如，使用<code>text</code>的更小的子串进行筛选.</p>
     */
    protected void performFiltering(CharSequence text, int start, int end,
                                    int keyCode) {
        getFilter().filter(text.subSequence(start, end), this);
    }

    /**
     * <p>用 {@link Tokenizer#terminateToken} 方法处理完的
     * <code>text</code> 来替换从 {@link Tokenizer#findTokenStart} 
     * 到 {@link #getSelectionEnd} 之间的内容.
     * 另外，替换后的文本会标记为 AutoText 替换，如果用户立即按下 DEL 键，
     * 会取消该替换操作.
     * 子类可覆盖此方法，用于向编辑框中插入其它内容.</p>
     *
     * @param text 选中的下拉列表中的建议文本
     */
    @Override
    protected void replaceText(CharSequence text) {
        clearComposingText();

        int end = getSelectionEnd();
        int start = mTokenizer.findTokenStart(getText(), end);

        Editable editable = getText();
        String original = TextUtils.substring(editable, start, end);

        QwertyKeyListener.markAsReplaced(editable, start, end, original);
        editable.replace(start, end, mTokenizer.terminateToken(text));
    }

    @Override
    public void onInitializeAccessibilityEvent(AccessibilityEvent event) {
        super.onInitializeAccessibilityEvent(event);
        event.setClassName(MultiAutoCompleteTextView.class.getName());
    }

    @Override
    public void onInitializeAccessibilityNodeInfo(AccessibilityNodeInfo info) {
        super.onInitializeAccessibilityNodeInfo(info);
        info.setClassName(MultiAutoCompleteTextView.class.getName());
    }

    public static interface Tokenizer {
        /**
         * 返回 <code>text</code> 中，到 <code>cursor</code> 结束的标记的开始位置.
         */
        public int findTokenStart(CharSequence text, int cursor);

        /**
         * 返回 <code>text</code> 中，从 <code>cursor</code> 开始的标记的结束位置.
         * 不包含尾随分隔符.
         */
        public int findTokenEnd(CharSequence text, int cursor);

        /**
         * 返回包含分割符的 <code>text</code>，如果不包含，则添加分隔符并返回修改后的值.
         */
        public CharSequence terminateToken(CharSequence text);
    }

    /**
     * 这个简易的分解器可用于对由逗号和若干空格分割的列表进行分解.
     */
    public static class CommaTokenizer implements Tokenizer {
        public int findTokenStart(CharSequence text, int cursor) {
            int i = cursor;

            while (i > 0 && text.charAt(i - 1) != ',') {
                i--;
            }
            while (i < cursor && text.charAt(i) == ' ') {
                i++;
            }

            return i;
        }

        public int findTokenEnd(CharSequence text, int cursor) {
            int i = cursor;
            int len = text.length();

            while (i < len) {
                if (text.charAt(i) == ',') {
                    return i;
                } else {
                    i++;
                }
            }

            return len;
        }

        public CharSequence terminateToken(CharSequence text) {
            int i = text.length();

            while (i > 0 && text.charAt(i - 1) == ' ') {
                i--;
            }

            if (i > 0 && text.charAt(i - 1) == ',') {
                return text;
            } else {
                if (text instanceof Spanned) {
                    SpannableString sp = new SpannableString(text + ", ");
                    TextUtils.copySpansFrom((Spanned) text, 0, text.length(),
                                            Object.class, sp, 0);
                    return sp;
                } else {
                    return text + ", ";
                }
            }
        }
    }
}
