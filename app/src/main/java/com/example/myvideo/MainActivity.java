package com.example.myvideo;

import android.graphics.Color;
import android.os.Bundle;

import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuItem;
import androidx.appcompat.app.AppCompatActivity;

import android.util.Log;
import android.view.View;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private static String TAG = "MainActivity";
    private TextView subTitleView;
    private final int DEFINITION = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setSubTitleView3(findViewById(R.id.main_content));
    }
    public void setSubTitleView3(TextView textView){
        String text = "Discover the latest features and updates that push boundaries of messaging, entertainment and accessibility, making your world even more connected.";
        textView.setText(text, TextView.BufferType.SPANNABLE);
        textView.setMovementMethod(LinkMovementMethod.getInstance());
        getEachWord(textView);
    }

    public void getEachWord(TextView textView){
        Spannable spans = (Spannable)textView.getText();
        Integer[] indices = getIndices(
                textView.getText().toString().trim(), ' ');
        int start = 0;
        int end = 0;
        // to cater last/only word loop will run equal to the length of indices.length
        for (int i = 0; i <= indices.length; i++) {
            ClickableSpan clickSpan = getClickableSpan();
            // to cater last/only word
            end = (i < indices.length ? indices[i] : spans.length());
            spans.setSpan(clickSpan, start, end,
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            start = end + 1;
        }
        //改变选中文本的高亮颜色
        textView.setHighlightColor(Color.CYAN);
    }

    private ClickableSpan getClickableSpan(){
        return new ClickableSpan() {
            @Override
            public void onClick(View widget) {
                TextView tv = (TextView) widget;
                String s = tv
                        .getText()
                        .subSequence(tv.getSelectionStart(),
                                tv.getSelectionEnd()).toString();
                Log.d(TAG, s);
            }
            @Override
            public void updateDrawState(TextPaint ds) {
                ds.setColor(Color.BLACK);
                ds.setUnderlineText(false);
            }
        };
    }

    public static Integer[] getIndices(String s, char c) {
        int pos = s.indexOf(c, 0);
        List<Integer> indices = new ArrayList<Integer>();
        while (pos != -1) {
            indices.add(pos);
            pos = s.indexOf(c, pos + 1);
        }
        return (Integer[]) indices.toArray(new Integer[0]);
    }



    private void setOnClickSubtitle(TextView v) {
        subTitleView = v;
        SpannableStringBuilder s = new SpannableStringBuilder(subTitleView.getText());
        for (int i = 0; i < s.length(); i++) {
            s.setSpan(new ClickableSpan() {
                @Override
                public void onClick(View v) {
                }

                @Override
                public void updateDrawState(TextPaint ds) {
                    super.updateDrawState(ds);
                    ds.setColor(0xff000000);       //设置文件颜色
                    ds.setUnderlineText(false);      //设置下划线
                }
            }, i, Math.min(i + 5,s.length()), Spanned.SPAN_EXCLUSIVE_INCLUSIVE);
        }
        subTitleView.setText(s, TextView.BufferType.SPANNABLE);
        subTitleView.setMovementMethod(LinkMovementMethod.getInstance());
        subTitleView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //若没有绑定clickableSpan，无法使用subSequence方法
                //若tv.getSelectionStart()-1,则输出点击的文字以及其上一个文字
                //若tv.getSelectionEnd()+1,则输出点击的文字以及其下一个文字，如此类推
                //通过标点判断还可截取一段文字中我们所点击的那句话
                TextView tv = (TextView) v;
//                String total = tv.getText().toString();
                int start = tv.getSelectionStart();
                int end = tv.getSelectionEnd();
                String s = tv.getText().subSequence(start,end+10).toString();
//                String s = tv
//                        .getText()
//                        .subSequence(tv.getSelectionStart(),
//                                tv.getSelectionEnd()).toString();
                Log.d(TAG, s);
            }
        });
    }

    void setOnClickSubtitle2(TextView v){
        subTitleView = v;
        subTitleView.setCustomSelectionActionModeCallback(new ActionMode.Callback() {

            @Override
            public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
                // Remove the "select all" option
                menu.removeItem(android.R.id.selectAll);
                // Remove the "cut" option
                menu.removeItem(android.R.id.cut);
                // Remove the "copy all" option
                menu.removeItem(android.R.id.copy);
                return true;
            }

            @Override
            public boolean onCreateActionMode(ActionMode mode, Menu menu) {
                // Called when action mode is first created. The menu supplied
                // will be used to generate action buttons for the action mode

                // Here is an example MenuItem
                menu.add(0, DEFINITION, 0, "Definition").setIcon(R.drawable.ic_close_circle);
                return true;
            }

            @Override
            public void onDestroyActionMode(ActionMode mode) {
                // Called when an action mode is about to be exited and
                // destroyed
            }

            @Override
            public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
                switch (item.getItemId()) {
                    case DEFINITION:
                        int min = 0;
                        int max = subTitleView.getText().length();
                        if (subTitleView.isFocused()) {
                            final int selStart = subTitleView.getSelectionStart();
                            final int selEnd = subTitleView.getSelectionEnd();

                            min = Math.max(0, Math.min(selStart, selEnd));
                            max = Math.max(0, Math.max(selStart, selEnd));
                        }
                        // Perform your definition lookup with the selected text
                        final CharSequence selectedText = subTitleView.getText().subSequence(min, max);
                        // Finish and close the ActionMode
                        mode.finish();
                        return true;
                    default:
                        break;
                }
                return false;
            }

        });
    }

}
