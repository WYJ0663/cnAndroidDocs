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

/**
 * 用来在一个潜在的大容器中布置对象的标准常量和工具。
 */
public class Gravity
{
    /** 表示未设置对其方式的常量。 */
    public static final int NO_GRAVITY = 0x0000;
    
    /** 表示按指定坐标轴对齐的原始标志位。 */
    public static final int AXIS_SPECIFIED = 0x0001;

    /** 表示控制居左/顶部对齐的原始标志位。 */
    public static final int AXIS_PULL_BEFORE = 0x0002;
    /** 表示控制居右/底部对齐的原始标志位。 */
    public static final int AXIS_PULL_AFTER = 0x0004;
    /** 
     * 表示控制对象的右侧/底部是否可以被容器相应的边截断的原始标志位，
     * 基于应用的对齐方向。
     */
    public static final int AXIS_CLIP = 0x0008;

    /** 定义为横轴的标志位。 */
    public static final int AXIS_X_SHIFT = 0;
    /** 定义为纵轴的标志位。 */
    public static final int AXIS_Y_SHIFT = 4;

    /** 将对象推到容器的顶部，不改变其大小。 */
    public static final int TOP = (AXIS_PULL_BEFORE|AXIS_SPECIFIED)<<AXIS_Y_SHIFT;
    /** 将对象推到容器的底部，不改变其大小。 */
    public static final int BOTTOM = (AXIS_PULL_AFTER|AXIS_SPECIFIED)<<AXIS_Y_SHIFT;
    /** 将对象推到容器的左侧，不改变其大小。 */
    public static final int LEFT = (AXIS_PULL_BEFORE|AXIS_SPECIFIED)<<AXIS_X_SHIFT;
    /** 将对象推到容器的右侧，不改变其大小。 */
    public static final int RIGHT = (AXIS_PULL_AFTER|AXIS_SPECIFIED)<<AXIS_X_SHIFT;

    /** 将对象置于其容器纵向居中处，不改变其大小。 */
    public static final int CENTER_VERTICAL = AXIS_SPECIFIED<<AXIS_Y_SHIFT;
    /** 增加对象的纵向大小，以使该对象充满其容器。 */
    public static final int FILL_VERTICAL = TOP|BOTTOM;

    /** 将对象置于其容器横向居中处，不改变其大小。 */
    public static final int CENTER_HORIZONTAL = AXIS_SPECIFIED<<AXIS_X_SHIFT;
    /** 增加对象的横向大小，以使该对象充满其容器。 */
    public static final int FILL_HORIZONTAL = LEFT|RIGHT;

    /** 将对象置于其容器横纵向居中处，不改变其大小。 */
    public static final int CENTER = CENTER_VERTICAL|CENTER_HORIZONTAL;

    /** 增加对象的横纵向大小，以使该对象充满其容器。 */
    public static final int FILL = FILL_VERTICAL|FILL_HORIZONTAL;

    /** Flag to clip the edges of the object to its container along the
     *  vertical axis. */
    public static final int CLIP_VERTICAL = AXIS_CLIP<<AXIS_Y_SHIFT;
    
    /** Flag to clip the edges of the object to its container along the
     *  horizontal axis. */
    public static final int CLIP_HORIZONTAL = AXIS_CLIP<<AXIS_X_SHIFT;

    /** Raw bit controlling whether the layout direction is relative or not (START/END instead of
     * absolute LEFT/RIGHT).
     */
    public static final int RELATIVE_LAYOUT_DIRECTION = 0x00800000;

    /**
     * Binary mask to get the absolute horizontal gravity of a gravity.
     */
    public static final int HORIZONTAL_GRAVITY_MASK = (AXIS_SPECIFIED |
            AXIS_PULL_BEFORE | AXIS_PULL_AFTER) << AXIS_X_SHIFT;
    /**
     * Binary mask to get the vertical gravity of a gravity.
     */
    public static final int VERTICAL_GRAVITY_MASK = (AXIS_SPECIFIED |
            AXIS_PULL_BEFORE | AXIS_PULL_AFTER) << AXIS_Y_SHIFT;

    /** Special constant to enable clipping to an overall display along the
     *  vertical dimension.  This is not applied by default by
     *  {@link #apply(int, int, int, Rect, int, int, Rect)}; you must do so
     *  yourself by calling {@link #applyDisplay}.
     */
    public static final int DISPLAY_CLIP_VERTICAL = 0x10000000;
    
    /** Special constant to enable clipping to an overall display along the
     *  horizontal dimension.  This is not applied by default by
     *  {@link #apply(int, int, int, Rect, int, int, Rect)}; you must do so
     *  yourself by calling {@link #applyDisplay}.
     */
    public static final int DISPLAY_CLIP_HORIZONTAL = 0x01000000;
    
    /** Push object to x-axis position at the start of its container, not changing its size. */
    public static final int START = RELATIVE_LAYOUT_DIRECTION | LEFT;

    /** Push object to x-axis position at the end of its container, not changing its size. */
    public static final int END = RELATIVE_LAYOUT_DIRECTION | RIGHT;

    /**
     * Binary mask for the horizontal gravity and script specific direction bit.
     */
    public static final int RELATIVE_HORIZONTAL_GRAVITY_MASK = START | END;

    /**
     * Apply a gravity constant to an object. This suppose that the layout direction is LTR.
     * 
     * @param gravity The desired placement of the object, as defined by the
     *                constants in this class.
     * @param w The horizontal size of the object.
     * @param h The vertical size of the object.
     * @param container The frame of the containing space, in which the object
     *                  will be placed.  Should be large enough to contain the
     *                  width and height of the object.
     * @param outRect Receives the computed frame of the object in its
     *                container.
     */
    public static void apply(int gravity, int w, int h, Rect container, Rect outRect) {
        apply(gravity, w, h, container, 0, 0, outRect);
    }

    /**
     * Apply a gravity constant to an object and take care if layout direction is RTL or not.
     *
     * @param gravity The desired placement of the object, as defined by the
     *                constants in this class.
     * @param w The horizontal size of the object.
     * @param h The vertical size of the object.
     * @param container The frame of the containing space, in which the object
     *                  will be placed.  Should be large enough to contain the
     *                  width and height of the object.
     * @param outRect Receives the computed frame of the object in its
     *                container.
     * @param layoutDirection The layout direction.
     *
     * @see View#LAYOUT_DIRECTION_LTR
     * @see View#LAYOUT_DIRECTION_RTL
     */
    public static void apply(int gravity, int w, int h, Rect container,
            Rect outRect, int layoutDirection) {
        int absGravity = getAbsoluteGravity(gravity, layoutDirection);
        apply(absGravity, w, h, container, 0, 0, outRect);
    }

    /**
     * Apply a gravity constant to an object.
     * 
     * @param gravity The desired placement of the object, as defined by the
     *                constants in this class.
     * @param w The horizontal size of the object.
     * @param h The vertical size of the object.
     * @param container The frame of the containing space, in which the object
     *                  will be placed.  Should be large enough to contain the
     *                  width and height of the object.
     * @param xAdj Offset to apply to the X axis.  If gravity is LEFT this
     *             pushes it to the right; if gravity is RIGHT it pushes it to
     *             the left; if gravity is CENTER_HORIZONTAL it pushes it to the
     *             right or left; otherwise it is ignored.
     * @param yAdj Offset to apply to the Y axis.  If gravity is TOP this pushes
     *             it down; if gravity is BOTTOM it pushes it up; if gravity is
     *             CENTER_VERTICAL it pushes it down or up; otherwise it is
     *             ignored.
     * @param outRect Receives the computed frame of the object in its
     *                container.
     */
    public static void apply(int gravity, int w, int h, Rect container,
            int xAdj, int yAdj, Rect outRect) {
        switch (gravity&((AXIS_PULL_BEFORE|AXIS_PULL_AFTER)<<AXIS_X_SHIFT)) {
            case 0:
                outRect.left = container.left
                        + ((container.right - container.left - w)/2) + xAdj;
                outRect.right = outRect.left + w;
                if ((gravity&(AXIS_CLIP<<AXIS_X_SHIFT))
                        == (AXIS_CLIP<<AXIS_X_SHIFT)) {
                    if (outRect.left < container.left) {
                        outRect.left = container.left;
                    }
                    if (outRect.right > container.right) {
                        outRect.right = container.right;
                    }
                }
                break;
            case AXIS_PULL_BEFORE<<AXIS_X_SHIFT:
                outRect.left = container.left + xAdj;
                outRect.right = outRect.left + w;
                if ((gravity&(AXIS_CLIP<<AXIS_X_SHIFT))
                        == (AXIS_CLIP<<AXIS_X_SHIFT)) {
                    if (outRect.right > container.right) {
                        outRect.right = container.right;
                    }
                }
                break;
            case AXIS_PULL_AFTER<<AXIS_X_SHIFT:
                outRect.right = container.right - xAdj;
                outRect.left = outRect.right - w;
                if ((gravity&(AXIS_CLIP<<AXIS_X_SHIFT))
                        == (AXIS_CLIP<<AXIS_X_SHIFT)) {
                    if (outRect.left < container.left) {
                        outRect.left = container.left;
                    }
                }
                break;
            default:
                outRect.left = container.left + xAdj;
                outRect.right = container.right + xAdj;
                break;
        }
        
        switch (gravity&((AXIS_PULL_BEFORE|AXIS_PULL_AFTER)<<AXIS_Y_SHIFT)) {
            case 0:
                outRect.top = container.top
                        + ((container.bottom - container.top - h)/2) + yAdj;
                outRect.bottom = outRect.top + h;
                if ((gravity&(AXIS_CLIP<<AXIS_Y_SHIFT))
                        == (AXIS_CLIP<<AXIS_Y_SHIFT)) {
                    if (outRect.top < container.top) {
                        outRect.top = container.top;
                    }
                    if (outRect.bottom > container.bottom) {
                        outRect.bottom = container.bottom;
                    }
                }
                break;
            case AXIS_PULL_BEFORE<<AXIS_Y_SHIFT:
                outRect.top = container.top + yAdj;
                outRect.bottom = outRect.top + h;
                if ((gravity&(AXIS_CLIP<<AXIS_Y_SHIFT))
                        == (AXIS_CLIP<<AXIS_Y_SHIFT)) {
                    if (outRect.bottom > container.bottom) {
                        outRect.bottom = container.bottom;
                    }
                }
                break;
            case AXIS_PULL_AFTER<<AXIS_Y_SHIFT:
                outRect.bottom = container.bottom - yAdj;
                outRect.top = outRect.bottom - h;
                if ((gravity&(AXIS_CLIP<<AXIS_Y_SHIFT))
                        == (AXIS_CLIP<<AXIS_Y_SHIFT)) {
                    if (outRect.top < container.top) {
                        outRect.top = container.top;
                    }
                }
                break;
            default:
                outRect.top = container.top + yAdj;
                outRect.bottom = container.bottom + yAdj;
                break;
        }
    }

    /**
     * Apply a gravity constant to an object.
     *
     * @param gravity The desired placement of the object, as defined by the
     *                constants in this class.
     * @param w The horizontal size of the object.
     * @param h The vertical size of the object.
     * @param container The frame of the containing space, in which the object
     *                  will be placed.  Should be large enough to contain the
     *                  width and height of the object.
     * @param xAdj Offset to apply to the X axis.  If gravity is LEFT this
     *             pushes it to the right; if gravity is RIGHT it pushes it to
     *             the left; if gravity is CENTER_HORIZONTAL it pushes it to the
     *             right or left; otherwise it is ignored.
     * @param yAdj Offset to apply to the Y axis.  If gravity is TOP this pushes
     *             it down; if gravity is BOTTOM it pushes it up; if gravity is
     *             CENTER_VERTICAL it pushes it down or up; otherwise it is
     *             ignored.
     * @param outRect Receives the computed frame of the object in its
     *                container.
     * @param layoutDirection The layout direction.
     *
     * @see View#LAYOUT_DIRECTION_LTR
     * @see View#LAYOUT_DIRECTION_RTL
     */
    public static void apply(int gravity, int w, int h, Rect container,
                             int xAdj, int yAdj, Rect outRect, int layoutDirection) {
        int absGravity = getAbsoluteGravity(gravity, layoutDirection);
        apply(absGravity, w, h, container, xAdj, yAdj, outRect);
    }

    /**
     * Apply additional gravity behavior based on the overall "display" that an
     * object exists in.  This can be used after
     * {@link #apply(int, int, int, Rect, int, int, Rect)} to place the object
     * within a visible display.  By default this moves or clips the object
     * to be visible in the display; the gravity flags
     * {@link #DISPLAY_CLIP_HORIZONTAL} and {@link #DISPLAY_CLIP_VERTICAL}
     * can be used to change this behavior.
     * 
     * @param gravity Gravity constants to modify the placement within the
     * display.
     * @param display The rectangle of the display in which the object is
     * being placed.
     * @param inoutObj Supplies the current object position; returns with it
     * modified if needed to fit in the display.
     */
    public static void applyDisplay(int gravity, Rect display, Rect inoutObj) {
        if ((gravity&DISPLAY_CLIP_VERTICAL) != 0) {
            if (inoutObj.top < display.top) inoutObj.top = display.top;
            if (inoutObj.bottom > display.bottom) inoutObj.bottom = display.bottom;
        } else {
            int off = 0;
            if (inoutObj.top < display.top) off = display.top-inoutObj.top;
            else if (inoutObj.bottom > display.bottom) off = display.bottom-inoutObj.bottom;
            if (off != 0) {
                if (inoutObj.height() > (display.bottom-display.top)) {
                    inoutObj.top = display.top;
                    inoutObj.bottom = display.bottom;
                } else {
                    inoutObj.top += off;
                    inoutObj.bottom += off;
                }
            }
        }
        
        if ((gravity&DISPLAY_CLIP_HORIZONTAL) != 0) {
            if (inoutObj.left < display.left) inoutObj.left = display.left;
            if (inoutObj.right > display.right) inoutObj.right = display.right;
        } else {
            int off = 0;
            if (inoutObj.left < display.left) off = display.left-inoutObj.left;
            else if (inoutObj.right > display.right) off = display.right-inoutObj.right;
            if (off != 0) {
                if (inoutObj.width() > (display.right-display.left)) {
                    inoutObj.left = display.left;
                    inoutObj.right = display.right;
                } else {
                    inoutObj.left += off;
                    inoutObj.right += off;
                }
            }
        }
    }

    /**
     * Apply additional gravity behavior based on the overall "display" that an
     * object exists in.  This can be used after
     * {@link #apply(int, int, int, Rect, int, int, Rect)} to place the object
     * within a visible display.  By default this moves or clips the object
     * to be visible in the display; the gravity flags
     * {@link #DISPLAY_CLIP_HORIZONTAL} and {@link #DISPLAY_CLIP_VERTICAL}
     * can be used to change this behavior.
     *
     * @param gravity Gravity constants to modify the placement within the
     * display.
     * @param display The rectangle of the display in which the object is
     * being placed.
     * @param inoutObj Supplies the current object position; returns with it
     * modified if needed to fit in the display.
     * @param layoutDirection The layout direction.
     *
     * @see View#LAYOUT_DIRECTION_LTR
     * @see View#LAYOUT_DIRECTION_RTL
     */
    public static void applyDisplay(int gravity, Rect display, Rect inoutObj, int layoutDirection) {
        int absGravity = getAbsoluteGravity(gravity, layoutDirection);
        applyDisplay(absGravity, display, inoutObj);
    }

    /**
     * <p>Indicate whether the supplied gravity has a vertical pull.</p>
     *
     * @param gravity the gravity to check for vertical pull
     * @return true if the supplied gravity has a vertical pull
     */
    public static boolean isVertical(int gravity) {
        return gravity > 0 && (gravity & VERTICAL_GRAVITY_MASK) != 0;
    }

    /**
     * <p>Indicate whether the supplied gravity has an horizontal pull.</p>
     *
     * @param gravity the gravity to check for horizontal pull
     * @return true if the supplied gravity has an horizontal pull
     */
    public static boolean isHorizontal(int gravity) {
        return gravity > 0 && (gravity & RELATIVE_HORIZONTAL_GRAVITY_MASK) != 0;
    }

    /**
     * <p>Convert script specific gravity to absolute horizontal value.</p>
     *
     * if horizontal direction is LTR, then START will set LEFT and END will set RIGHT.
     * if horizontal direction is RTL, then START will set RIGHT and END will set LEFT.
     *
     *
     * @param gravity The gravity to convert to absolute (horizontal) values.
     * @param layoutDirection The layout direction.
     * @return gravity converted to absolute (horizontal) values.
     */
    public static int getAbsoluteGravity(int gravity, int layoutDirection) {
        int result = gravity;
        // If layout is script specific and gravity is horizontal relative (START or END)
        if ((result & RELATIVE_LAYOUT_DIRECTION) > 0) {
            if ((result & Gravity.START) == Gravity.START) {
                // Remove the START bit
                result &= ~START;
                if (layoutDirection == View.LAYOUT_DIRECTION_RTL) {
                    // Set the RIGHT bit
                    result |= RIGHT;
                } else {
                    // Set the LEFT bit
                    result |= LEFT;
                }
            } else if ((result & Gravity.END) == Gravity.END) {
                // Remove the END bit
                result &= ~END;
                if (layoutDirection == View.LAYOUT_DIRECTION_RTL) {
                    // Set the LEFT bit
                    result |= LEFT;
                } else {
                    // Set the RIGHT bit
                    result |= RIGHT;
                }
            }
            // Don't need the script specific bit any more, so remove it as we are converting to
            // absolute values (LEFT or RIGHT)
            result &= ~RELATIVE_LAYOUT_DIRECTION;
        }
        return result;
    }
}
