package com.jeremyfeinstein.slidingmenu.lib.app;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.text.Editable;
import android.text.Layout;
import android.text.Selection;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextPaint;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.text.style.CharacterStyle;
import android.text.style.ImageSpan;
import android.text.style.UpdateAppearance;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import cn.schope.lightning.R;
import cn.schope.lightning.utils.UIUtils;

/**
 * StrongHope
 * Created by gongyasen on 2015/6/18.a
 */
public class CloudEditText extends EditText {
    private Paint textPaint = new Paint();
    private Rect textRect = new Rect();
    private Drawable rightDrawable;
    private int rightDrawableWidth;
    private int drawablePadding;
    private int itemPadding;

    public CloudEditText(Context context) {
        super(context);
        init();
    }

    public CloudEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public CloudEditText(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    private void init() {
        setMovementMethod(new LinkTouchMovementMethod());
        rightDrawable = getResources().getDrawable(R.drawable.exit_pressed);
        drawablePadding = UIUtils.dip2px(getContext(),10);
        itemPadding = UIUtils.dip2px(getContext(),3);
        rightDrawableWidth = rightDrawable.getIntrinsicWidth()+20 + drawablePadding;
    }

    @Override
    protected void onSelectionChanged(int selStart, int selEnd) {
//        int length = getText().length();
//        if(selStart < length){
//            setSelection(length);
//        }
        MyImageSpan[] spans = getText().getSpans(0, getText().length(), MyImageSpan.class);
        for (MyImageSpan myImageSpan : spans) {
//            System.out.println("SpanEnd:"+getText().getSpanEnd(myImageSpan)+";selStart:"+selStart);
            if (getText().getSpanEnd(myImageSpan) - 1 == selStart) {
                selStart = selStart + 1;
                setSelection(selStart);
                break;
            }
        }
        super.onSelectionChanged(selStart, selEnd);
    }

    /**
     * 校验当前输入Span的合法性,覆写这条用来实现自己的校验
     * @return
     */
    public boolean checkInputSpan(String showText,String returnText){
        return true;
    }

    /**
     * 添加一个Span
     */
    public void addSpan(String showText,String returnText){
        if(!checkInputSpan(showText,returnText)){
            return;
        }
        getText().append(showText);
        SpannableString spannableString = new SpannableString(getText());
        generateOneSpan(spannableString, new UnSpanText(spannableString.length() - showText.length(), spannableString.length(), showText, returnText));
        setText(spannableString);
        setSelection(spannableString.length());
    }

    /**
     * 获得所有的returnText列表
     */
    public List<String> getAllReturnStringList(){
        MyImageSpan[] spans = getText().getSpans(0, getText().length(), MyImageSpan.class);
        List<String> list = new ArrayList<String>();
        for(MyImageSpan myImageSpan : spans){
            list.add(myImageSpan.getReturnText());
        }
        List<UnSpanText> texts = getAllTexts(spans,getText());
        for (UnSpanText unSpanText : texts){
            list.add(unSpanText.returnText.toString());
        }
        return list;
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_ENTER && event.getAction() == KeyEvent.ACTION_DOWN) {
            flushSpans();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    /**
     * 刷新Span
     */
    private void flushSpans() {
        Editable editText = getText();
        Spannable spannableString = new SpannableString(editText);

//            int selectionStart = getSelectionStart();
        MyImageSpan[] spans = spannableString.getSpans(0, editText.length(), MyImageSpan.class);
//            int spansStringLength = getSpansStringLength(spans);
        List<UnSpanText> texts = getAllTexts(spans,editText);
        for (UnSpanText unSpanText : texts) {
            if(!checkInputSpan(unSpanText.showText.toString(), unSpanText.returnText)){
                return;
            }
            generateOneSpan(spannableString, unSpanText);
        }
        setText(spannableString);
        setSelection(spannableString.length());
    }

    /**
     * 生成一个Span
     * @param spannableString
     * @param unSpanText
     */
    private void generateOneSpan(Spannable spannableString, UnSpanText unSpanText) {
        View spanView = getSpanView(getContext(), unSpanText.showText.toString(), getMeasuredWidth());
        BitmapDrawable bitmpaDrawable = (BitmapDrawable) UIUtils.convertViewToDrawable(spanView);
        bitmpaDrawable.setBounds(0, 0, bitmpaDrawable.getIntrinsicWidth(), bitmpaDrawable.getIntrinsicHeight());
        MyImageSpan what = new MyImageSpan(bitmpaDrawable, unSpanText.showText.toString(),unSpanText.returnText);
//            this.spans.add(what);
        final int start = unSpanText.start;
        final int end = unSpanText.end;
        spannableString.setSpan(what, start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        TouchableSpan touchableSpan = new TouchableSpan(unSpanText) {
            @Override
            public boolean onTouchDelete(View widget, MotionEvent ev,float left,float right) {
                getText().replace(getText().getSpanStart(what), getText().getSpanEnd(what), "");
                getText().removeSpan(what);
                getText().removeSpan(this);
                return true;
            }

            @Override
            public void updateDrawState(TextPaint ds) {
            }
        };
        spannableString.setSpan(touchableSpan, start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
    }

    /**
     * 得到所有非Span的文本集合
     *
     * @param spans
     * @return
     */
    private List<UnSpanText> getAllTexts(MyImageSpan[] spans,Editable edittext) {
        List<UnSpanText> texts = new ArrayList<UnSpanText>();
        int start;
        int end;
        CharSequence text;

        List<Integer> sortStartEnds = new ArrayList<Integer>();
        sortStartEnds.add(0);
        for (MyImageSpan myImageSpan : spans){
            sortStartEnds.add(edittext.getSpanStart(myImageSpan));
            sortStartEnds.add(edittext.getSpanEnd(myImageSpan));
        }
        sortStartEnds.add(edittext.length());
        Collections.sort(sortStartEnds);
//        System.out.println("sortStartEnds:" + sortStartEnds);

        for (int i = 0; i < sortStartEnds.size(); i = i + 2) {
            start = sortStartEnds.get(i);
            end = sortStartEnds.get(i + 1);
            text = edittext.subSequence(start, end);
//            System.out.println("start:"+start+";end:"+end);
            if (!TextUtils.isEmpty(text)) {
                texts.add(new UnSpanText(start, end, text,text.toString()));
            }
        }

        return texts;
    }

    /**
     * 绘制Span
     *
     * @param measuredWidth
     */
    private BitmapDrawable drawImageSpan(int measuredWidth, String spanText) {
        textPaint.setColor(getCurrentTextColor());
        textPaint.setAntiAlias(true);
        textPaint.setTextSize(UIUtils.dip2px(getContext(),(int) getTextSize()));
        textPaint.getTextBounds(spanText, 0, spanText.length(), textRect);
        int textPadding = UIUtils.dip2px(getContext(),6);

        Bitmap b = Bitmap.createBitmap(textRect.right - textRect.left + (textPadding * 2), textRect.bottom - textRect.top + (textPadding * 2), Bitmap.Config.ARGB_8888);
        Canvas c = new Canvas(b);
        c.drawText(spanText, textPadding, textRect.bottom - textRect.top + textPadding, textPaint);
        Drawable background = getResources().getDrawable(R.drawable.cloud_edittext_common_mentions_background);
        background.setBounds(0, 0, textRect.right - textRect.left + (textPadding * 2), textRect.bottom - textRect.top + (textPadding * 2));
        background.draw(c);
        return new BitmapDrawable(b);
    }

    /**
     * 获取span中的总字符串长度
     *
     * @param spans
     */
    private int getSpansStringLength(MyImageSpan[] spans) {
        int length = 0;
        for (MyImageSpan myImageSpan : spans) {
            length += myImageSpan.getShowText().toString().length();
        }
        return length;
    }

    /**
     * 获得span视图
     *
     * @param context
     * @return
     */
    public View getSpanView(Context context, String text, int maxWidth){
        TextView view = new TextView(context);
        view.setMaxWidth(maxWidth);
        view.setCompoundDrawablesWithIntrinsicBounds(null, null, rightDrawable, null);
        view.setCompoundDrawablePadding(drawablePadding);
        view.setText(text);
        view.setSingleLine(true);
        view.setTextSize(getTextSize());
        view.setBackgroundResource(R.drawable.cloud_edittext_common_mentions_background);
        view.setTextColor(getCurrentTextColor());
        FrameLayout frameLayout = new FrameLayout(context);
        frameLayout.setPadding(itemPadding,itemPadding,itemPadding,itemPadding);
        frameLayout.addView(view);
        return frameLayout;
    }


    /**
     * 我的ImageSpan
     */
    private class MyImageSpan extends ImageSpan {
        private String showText;
        private String returnText;

        public MyImageSpan(Drawable d, String showText,String returnText) {
            super(d);
            this.showText = showText;
            this.returnText = returnText;
        }

        public String getReturnText() {
            return returnText;
        }

        public String getShowText() {
            return showText;
        }
    }

    /**
     * 表示一段非SPan字符串
     */
    private class UnSpanText {
        public int start;
        public int end;
        public CharSequence showText;
        public String returnText;

        public UnSpanText(int start, int end, CharSequence showText,String returnText) {
            this.start = start;
            this.end = end;
            this.showText = showText;
            this.returnText = returnText;
        }
    }

    /**
     * If an object of this type is attached to the text of a TextView
     * with a movement method of LinkTouchMovementMethod, the affected spans of
     * text can be selected.  If touched, the {@link #onTouchDelete} method will
     * be called.
     */
    public abstract class TouchableSpan extends CharacterStyle implements UpdateAppearance {
        private UnSpanText unSpanText;

        public TouchableSpan(UnSpanText unSpanText) {
            this.unSpanText = unSpanText;
        }

        public UnSpanText getUnSpanText(){
            return unSpanText;
        }

        /**
         * Performs the touch action associated with this span.
         * @return
         */
        public abstract boolean onTouchDelete(View widget, MotionEvent m,float left,float right);
        /**
         * Could make the text underlined or change link color.
         */
        @Override
        public abstract void updateDrawState(TextPaint ds);
    }


    /**
     * 修改走clickableSpan的Onclick为TouchableSpan的OnTouch
     */
    public class LinkTouchMovementMethod extends LinkMovementMethod{
        private Rect lineBounds = new Rect();

        @Override
        public boolean onTouchEvent(TextView widget, Spannable buffer,
                                    MotionEvent event) {
            int action = event.getAction();
            if (action == MotionEvent.ACTION_UP ||
                    action == MotionEvent.ACTION_DOWN) {
                int x = (int) event.getX();
                int y = (int) event.getY();
                x -= widget.getTotalPaddingLeft();
                y -= widget.getTotalPaddingTop();
                x += widget.getScrollX();
                y += widget.getScrollY();
                Layout layout = widget.getLayout();
                int line = layout.getLineForVertical(y);
                int off = layout.getOffsetForHorizontal(line, x);
                TouchableSpan[] link = buffer.getSpans(off, off, TouchableSpan.class);
//                System.out.println("offset:"+off);
                if (link.length != 0) {
                    if (action == MotionEvent.ACTION_UP) {
//                        System.out.println("getParagraphLeft:"+layout.get+";getParagraphRight:"+layout.getParagraphRight(line));
                        if(layout.getLineWidth(line) > x && 0 < x){
                            LeftRight point = getLeftWidth(buffer, layout, line, link[0]);
//                            System.out.println("point.right - x:"+(point.right - x)+";rightDrawableWidth = " + rightDrawableWidth);
                            if(point.right - x > 0 && point.right - x < rightDrawableWidth){
                                link[0].onTouchDelete(widget, event, point.left, point.right); //////// CHANGED HERE
                            }
                        }
                    } else if (action == MotionEvent.ACTION_DOWN) {
                        Selection.setSelection(buffer,
                                buffer.getSpanStart(link[0]),
                                buffer.getSpanEnd(link[0]));
//                        if(layout.getLineWidth(line) > x && 0 < x){
//                            LeftRight point = getLeftWidth(buffer, layout, line, link[0]);
//                            if(point.right - x < rightDrawableWidth){
//                                link[0].onTouchDelete(widget, event, point.left, point.right); //////// ADDED THIS
//                            }
//                        }
                    }
                    return true;
                } else {
                    Selection.removeSelection(buffer);
                }
            }
            return super.onTouchEvent(widget, buffer, event);
        }

        /**
         * 获得某个Span的Left
         * @param buffer
         * @return
         */
        private LeftRight getLeftWidth(Spannable buffer,Layout layout,int line,TouchableSpan span) {
            int lineStartOffset = layout.getOffsetForHorizontal(line, 0);
            MyImageSpan[] spans = buffer.getSpans(lineStartOffset, buffer.getSpanEnd(span), MyImageSpan.class);
//            System.out.println("单个spansSize:"+spans.length);
            int leftWidth = 0;
            int right = 0;
            for(MyImageSpan myImageSpan : spans){
                int intrinsicWidth = myImageSpan.getDrawable().getIntrinsicWidth();
                if (!myImageSpan.getShowText().equals(span.getUnSpanText().showText.toString())){
                    leftWidth += intrinsicWidth;
                }
                right += intrinsicWidth;
            }
            return new LeftRight(leftWidth,right);
        }

        /**
         * 表示左右位置
         */
        private class LeftRight{
            private final int left;
            private final int right;

            public LeftRight(int left,int right){
                this.left = left;
                this.right = right;
            }
        }

    }
}
