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

package android.util;

import java.io.Closeable;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

/**
 * 向流中输出经过 JSON (<a href="http://www.ietf.org/rfc/rfc4627.txt">RFC 4627</a>)
 * 编码的值，每次只能输出一个字符串. 流中既包括文字值（字符串、数字、布尔值和空值），
 * 也包括作为对象、数组的开始和结束标志的分隔符。
 *
 * <h3>编码 JSON</h3>
 * 要将你的数据编码为 JSON，需要创建一个新的 {@code JsonWriter}。每个 JSON 
 * 文档必须包含一个顶级的数组或对象。按照你浏览数据结构时遇到的内容来调用写入器的方法，
 * 需要时可以嵌套数组和对象：
 * <ul>
 *   <li>要写入<strong>数组</strong>时，首先调用 {@link #beginArray()}。
 *       之后为每个数组元素调用{@link #value}方法写入适当的值或嵌套其他数组和对象。 
 *       最后使用 {@link #endArray()} 来关闭数组的输出。
 *   <li>要写入<strong>对象</strong>时，首先调用 {@link #beginObject()}。
 *       之后为写入对象的每个属性，使用属性名交替调用 {@link #name} 方法。
 *       使用 {@link #value} 方法写入适当之值或嵌套其他数组和对象。 
 *       最后使用 {@link #endObject()} 来关闭对象的输出。
 * </ul>
 *
 * <h3>示例</h3>
 * 假设我们要将信息编码为如下形式：
 * <pre> {@code
 * [
 *   {
 *     "id": 912345678901,
 *     "text": "在 Android 中应该如何写入 JSON 字符串？",
 *     "geo": null,
 *     "user": {
 *       "name": "android_newb",
 *       "followers_count": 41
 *      }
 *   },
 *   {
 *     "id": 912345678902,
 *     "text": "@android_newb 只要使用 android.util.JsonWriter 即可！",
 *     "geo": [50.454722, -104.606667],
 *     "user": {
 *       "name": "jesse",
 *       "followers_count": 2
 *     }
 *   }
 * ]</pre>
 * 该代码编码生成上述结构：
 * <pre>   {@code
 *   public void writeJsonStream(OutputStream out, List<Message> messages) throws IOException {
 *     JsonWriter writer = new JsonWriter(new OutputStreamWriter(out, "UTF-8"));
 *     writer.setIndent("  ");
 *     writeMessagesArray(writer, messages);
 *     writer.close();
 *   }
 *
 *   public void writeMessagesArray(JsonWriter writer, List<Message> messages) throws IOException {
 *     writer.beginArray();
 *     for (Message message : messages) {
 *       writeMessage(writer, message);
 *     }
 *     writer.endArray();
 *   }
 *
 *   public void writeMessage(JsonWriter writer, Message message) throws IOException {
 *     writer.beginObject();
 *     writer.name("id").value(message.getId());
 *     writer.name("text").value(message.getText());
 *     if (message.getGeo() != null) {
 *       writer.name("geo");
 *       writeDoublesArray(writer, message.getGeo());
 *     } else {
 *       writer.name("geo").nullValue();
 *     }
 *     writer.name("user");
 *     writeUser(writer, message.getUser());
 *     writer.endObject();
 *   }
 *
 *   public void writeUser(JsonWriter writer, User user) throws IOException {
 *     writer.beginObject();
 *     writer.name("name").value(user.getName());
 *     writer.name("followers_count").value(user.getFollowersCount());
 *     writer.endObject();
 *   }
 *
 *   public void writeDoublesArray(JsonWriter writer, List<Double> doubles) throws IOException {
 *     writer.beginArray();
 *     for (Double value : doubles) {
 *       writer.value(value);
 *     }
 *     writer.endArray();
 *   }</pre>
 *
 * <p>每个 {@code JsonWriter} 仅可以用于写入一个 JSON 流。该类的实例不是线程安全的。
 * 在多线程中调用会生成奇怪的 JSON 字符串，会导致 {@link IllegalStateException} 异常。
 */
public final class JsonWriter implements Closeable {

    /** The output data, containing at most one top-level array or object. */
    private final Writer out;

    private final List<JsonScope> stack = new ArrayList<JsonScope>();
    {
        stack.add(JsonScope.EMPTY_DOCUMENT);
    }

    /**
     * A string containing a full set of spaces for a single level of
     * indentation, or null for no pretty printing.
     */
    private String indent;

    /**
     * The name/value separator; either ":" or ": ".
     */
    private String separator = ":";

    private boolean lenient;

    /**
     * 创建一个向 {@code out} 中输出 JSON 编码流的新实例. 为了获得更好的性能，
     * 请确保 {@link Writer 写入器} 是已缓存的；如果需要可使用
     * {@link java.io.BufferedWriter BufferedWriter} 包装你的写入器。
     */
    public JsonWriter(Writer out) {
        if (out == null) {
            throw new NullPointerException("out == null");
        }
        this.out = out;
    }

    /**
     * 设置编码文档中用于每级缩进的缩进字符串. 如果{@code indent.isEmpty() 为空}，
     * 编码的文档更紧凑。非空则编码文档可读性更强。
     *
     * @param indent 仅包含空白字符的字符串
     */
    public void setIndent(String indent) {
        if (indent.isEmpty()) {
            this.indent = null;
            this.separator = ":";
        } else {
            this.indent = indent;
            this.separator = ": ";
        }
    }

    /**
     * 配置写入器放松其语法规则. 通常该写入器仅输出由 <a href="http://www.ietf.org/rfc/rfc4627.txt">
     * RFC 4627</a> 指定的，良好格式化的 JSON 字符串。设置写入器可以接受如下格式：
     * <ul>
     *   <li>任何类型的顶级值。严格的规则，仅支持类型为对象或数组的顶级值。
     *   <li>数值可以是 {@link Double#isNaN() NaNs} 或 {@link
     *       Double#isInfinite() 无穷大}。
     * </ul>
     */
    public void setLenient(boolean lenient) {
        this.lenient = lenient;
    }

    /**
     * 如果写入器使用宽松语法规则，返回真。
     */
    public boolean isLenient() {
        return lenient;
    }

    /**
     * 开始编码新的数组. 对该方法的调用必须与对 {@link #endArray} 的调用成对出现。
     *
     * @return 写入器。
     */
    public JsonWriter beginArray() throws IOException {
        return open(JsonScope.EMPTY_ARRAY, "[");
    }

    /**
     * 结束对当前数组的编码。
     *
     * @return 写入器。
     */
    public JsonWriter endArray() throws IOException {
        return close(JsonScope.EMPTY_ARRAY, JsonScope.NONEMPTY_ARRAY, "]");
    }

    /**
     * 开始编码新的对象. 对该方法的调用必须与对 {@link #endObject} 的调用成对出现。
     *
     * @return 写入器。
     */
    public JsonWriter beginObject() throws IOException {
        return open(JsonScope.EMPTY_OBJECT, "{");
    }

    /**
     * 结束对当前对象的编码。
     *
     * @return 写入器。
     */
    public JsonWriter endObject() throws IOException {
        return close(JsonScope.EMPTY_OBJECT, JsonScope.NONEMPTY_OBJECT, "}");
    }

    /**
     * Enters a new scope by appending any necessary whitespace and the given
     * bracket.
     */
    private JsonWriter open(JsonScope empty, String openBracket) throws IOException {
        beforeValue(true);
        stack.add(empty);
        out.write(openBracket);
        return this;
    }

    /**
     * Closes the current scope by appending any necessary whitespace and the
     * given bracket.
     */
    private JsonWriter close(JsonScope empty, JsonScope nonempty, String closeBracket)
            throws IOException {
        JsonScope context = peek();
        if (context != nonempty && context != empty) {
            throw new IllegalStateException("Nesting problem: " + stack);
        }

        stack.remove(stack.size() - 1);
        if (context == nonempty) {
            newline();
        }
        out.write(closeBracket);
        return this;
    }

    /**
     * Returns the value on the top of the stack.
     */
    private JsonScope peek() {
        return stack.get(stack.size() - 1);
    }

    /**
     * Replace the value on the top of the stack with the given value.
     */
    private void replaceTop(JsonScope topOfStack) {
        stack.set(stack.size() - 1, topOfStack);
    }

    /**
     * 为属性名编码。
     *
     * @param name 气候的值得名字。不能为空。
     * @return 写入器。
     */
    public JsonWriter name(String name) throws IOException {
        if (name == null) {
            throw new NullPointerException("name == null");
        }
        beforeName();
        string(name);
        return this;
    }

    /**
     * 为 {@code value} 编码.
     *
     * @param value 文本字符串的值，或者为空，编码为空的文本值。
     * @return 写入器。
     */
    public JsonWriter value(String value) throws IOException {
        if (value == null) {
            return nullValue();
        }
        beforeValue(false);
        string(value);
        return this;
    }

    /**
     * 为 {@code null} 编码.
     *
     * @return 写入器。
     */
    public JsonWriter nullValue() throws IOException {
        beforeValue(false);
        out.write("null");
        return this;
    }

    /**
     * 为 {@code value} 编码.
     *
     * @return 写入器。
     */
    public JsonWriter value(boolean value) throws IOException {
        beforeValue(false);
        out.write(value ? "true" : "false");
        return this;
    }

    /**
     * 为 {@code value} 编码.
     *
     * @param value 有限的值。除非写入器允许宽松语法，否则不允许为
     * {@link Double#isNaN() NaNs} 或 {@link Double#isInfinite() infinities}。
     * @return 写入器。
     */
    public JsonWriter value(double value) throws IOException {
        if (!lenient && (Double.isNaN(value) || Double.isInfinite(value))) {
            throw new IllegalArgumentException("Numeric values must be finite, but was " + value);
        }
        beforeValue(false);
        out.append(Double.toString(value));
        return this;
    }

    /**
     * 为 {@code value} 编码.
     *
     * @return 写入器。
     */
    public JsonWriter value(long value) throws IOException {
        beforeValue(false);
        out.write(Long.toString(value));
        return this;
    }

    /**
     * 为 {@code value} 编码.
     *
     * @param value 有限的值。除非写入器允许宽松语法，否则不允许为
     * {@link Double#isNaN() NaNs} 或 {@link Double#isInfinite() infinities}。
     * @return 写入器。
     */
    public JsonWriter value(Number value) throws IOException {
        if (value == null) {
            return nullValue();
        }

        String string = value.toString();
        if (!lenient &&
                (string.equals("-Infinity") || string.equals("Infinity") || string.equals("NaN"))) {
            throw new IllegalArgumentException("Numeric values must be finite, but was " + value);
        }
        beforeValue(false);
        out.append(string);
        return this;
    }

    /**
     * 确保所有缓存的数据都写入了底层 {@link Writer}，并刷新该该写入器。
     */
    public void flush() throws IOException {
        out.flush();
    }

    /**
     * 刷新并关闭该写入器及底层 {@link Writer}。
     *
     * @throws IOException 如果 JSON 文档不完整。
     */
    public void close() throws IOException {
        out.close();

        if (peek() != JsonScope.NONEMPTY_DOCUMENT) {
            throw new IOException("Incomplete document");
        }
    }

    private void string(String value) throws IOException {
        out.write("\"");
        for (int i = 0, length = value.length(); i < length; i++) {
            char c = value.charAt(i);

            /*
             * From RFC 4627, "All Unicode characters may be placed within the
             * quotation marks except for the characters that must be escaped:
             * quotation mark, reverse solidus, and the control characters
             * (U+0000 through U+001F)."
             *
             * We also escape '\u2028' and '\u2029', which JavaScript interprets
             * as newline characters. This prevents eval() from failing with a
             * syntax error.
             * http://code.google.com/p/google-gson/issues/detail?id=341
             */
            switch (c) {
                case '"':
                case '\\':
                    out.write('\\');
                    out.write(c);
                    break;

                case '\t':
                    out.write("\\t");
                    break;

                case '\b':
                    out.write("\\b");
                    break;

                case '\n':
                    out.write("\\n");
                    break;

                case '\r':
                    out.write("\\r");
                    break;

                case '\f':
                    out.write("\\f");
                    break;

                case '\u2028':
                case '\u2029':
                    out.write(String.format("\\u%04x", (int) c));
                    break;

                default:
                    if (c <= 0x1F) {
                        out.write(String.format("\\u%04x", (int) c));
                    } else {
                        out.write(c);
                    }
                    break;
            }

        }
        out.write("\"");
    }

    private void newline() throws IOException {
        if (indent == null) {
            return;
        }

        out.write("\n");
        for (int i = 1; i < stack.size(); i++) {
            out.write(indent);
        }
    }

    /**
     * Inserts any necessary separators and whitespace before a name. Also
     * adjusts the stack to expect the name's value.
     */
    private void beforeName() throws IOException {
        JsonScope context = peek();
        if (context == JsonScope.NONEMPTY_OBJECT) { // first in object
            out.write(',');
        } else if (context != JsonScope.EMPTY_OBJECT) { // not in an object!
            throw new IllegalStateException("Nesting problem: " + stack);
        }
        newline();
        replaceTop(JsonScope.DANGLING_NAME);
    }

    /**
     * Inserts any necessary separators and whitespace before a literal value,
     * inline array, or inline object. Also adjusts the stack to expect either a
     * closing bracket or another element.
     *
     * @param root true if the value is a new array or object, the two values
     *     permitted as top-level elements.
     */
    private void beforeValue(boolean root) throws IOException {
        switch (peek()) {
            case EMPTY_DOCUMENT: // first in document
                if (!lenient && !root) {
                    throw new IllegalStateException(
                            "JSON must start with an array or an object.");
                }
                replaceTop(JsonScope.NONEMPTY_DOCUMENT);
                break;

            case EMPTY_ARRAY: // first in array
                replaceTop(JsonScope.NONEMPTY_ARRAY);
                newline();
                break;

            case NONEMPTY_ARRAY: // another in array
                out.append(',');
                newline();
                break;

            case DANGLING_NAME: // value for name
                out.append(separator);
                replaceTop(JsonScope.NONEMPTY_OBJECT);
                break;

            case NONEMPTY_DOCUMENT:
                throw new IllegalStateException(
                        "JSON must have only one top-level value.");

            default:
                throw new IllegalStateException("Nesting problem: " + stack);
        }
    }
}
