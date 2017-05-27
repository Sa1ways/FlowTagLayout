package com.example.shawn.flowtaglayouttest;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;


/**
 * Created by shawn on 17-5-26.
 */

public class FlowTagsLayout extends ViewGroup implements View.OnTouchListener {

    public static final String TAG="FlowLayout";
    public static final int MODE_CLICK=0;
    public static final int MODE_SINGLE=1;
    public static final int MODE_MULTI=2;
    public static final int ACTION_PRESSED=0;
    public static final int ACTION_NORMAL=1;
    private int mTagPaddingLeftRight;
    private int mTagMarginLeftRight;
    private int mTagMarginTopBottom;
    private int mTagPaddingTopBottom;
    private int mSelectMode;
    private Drawable mDefaultDrawable;
    private Drawable mSelectedDrawable;
    private int mTagTextColor;
    private int mTagSelectedColor;
    private List<Boolean> mSelectedList=new ArrayList<>();
    public OnTagClickCallback mOnTagClickCallback;
    public OnTagChosenCallback mOnTagChosenCallback;

    public FlowTagsLayout(Context context) {
        this(context,null);
    }

    public FlowTagsLayout(Context context, AttributeSet attrs) {
        this(context, attrs,0);
    }

    public FlowTagsLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context,attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        TypedArray array=context.obtainStyledAttributes(attrs,R.styleable.FlowTagsLayout);
        mTagPaddingLeftRight=array.getDimensionPixelSize(R.styleable.FlowTagsLayout_TagPaddingLeftRight,dp2px(10));
        mTagPaddingTopBottom=array.getDimensionPixelSize(R.styleable.FlowTagsLayout_TagPaddingTopBottom,dp2px(5));
        mTagMarginLeftRight=array.getDimensionPixelSize(R.styleable.FlowTagsLayout_TagMarginLeftRight,dp2px(5));
        mTagMarginTopBottom=array.getDimensionPixelSize(R.styleable.FlowTagsLayout_TagMarginTopBottom,dp2px(5));
        mDefaultDrawable=array.getDrawable(R.styleable.FlowTagsLayout_TagDefaultDrawable);
        mSelectedDrawable=array.getDrawable(R.styleable.FlowTagsLayout_TagSelectedDrawable);
        mSelectMode=array.getInt(R.styleable.FlowTagsLayout_SelectMode,0);
        mTagTextColor=array.getColor(R.styleable.FlowTagsLayout_TagTextColor,Color.GRAY);
        mTagSelectedColor=array.getColor(R.styleable.FlowTagsLayout_TagSelectColor,Color.WHITE);
        array.recycle();
        for (int i = 0; i <getChildCount() ; i++) {
            mSelectedList.add(false);
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int widthSize=MeasureSpec.getSize(widthMeasureSpec);
        int heightMode=MeasureSpec.getMode(heightMeasureSpec);
        int heightSize=MeasureSpec.getSize(heightMeasureSpec);
        if(heightMode==MeasureSpec.EXACTLY){
             setMeasuredDimension(widthSize,heightSize);
        }else {
            //这里默认AT_MOST
            int height=0;
            int lineHeight=0,lineWidth=0;
            for (int i = 0; i <getChildCount() ; i++) {
                View childView=getChildAt(i);
                measureChild(childView,widthMeasureSpec,heightMeasureSpec);
                MarginLayoutParams params= (MarginLayoutParams) childView.getLayoutParams();
                int childWidth=childView.getMeasuredWidth()+params.leftMargin+params.rightMargin;
                int childHeight=childView.getMeasuredHeight()+params.topMargin+params.bottomMargin;

                if(childWidth>=widthSize){
                    height+=childHeight+lineHeight;
                    lineHeight=0;
                    lineWidth=0;
                    continue;
                }
                lineWidth+=childWidth;
                if(lineWidth<widthSize){
                    lineHeight=Math.max(childHeight,lineHeight);
                }else{
                    //mv to next line
                    height+=lineHeight;
                    lineHeight=childHeight;
                    lineWidth=childWidth;
                }
                if(i==(getChildCount()-1)){
                    height+=lineHeight;
                }
            }
            setMeasuredDimension(widthSize,height);
        }
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        int lineWidth=0;
        int lineHeight=0;
        for (int i = 0; i < getChildCount(); i++) {
            View childView=getChildAt(i);
            setTagAction(childView,i);

            MarginLayoutParams params= (MarginLayoutParams) childView.getLayoutParams();
            if(childView.getMeasuredWidth()>=getMeasuredWidth()){
                t+=lineHeight;
                childView.layout(l+params.leftMargin,t+params.topMargin,r-params.rightMargin,
                        t+childView.getMeasuredHeight()+params.topMargin);
                t+=childView.getMeasuredHeight()+params.topMargin+params.bottomMargin;
                lineHeight=0;
                lineWidth=0;
                continue;
            }
            lineWidth+=childView.getMeasuredWidth()+params.rightMargin+params.leftMargin;
            if(lineWidth<getMeasuredWidth()){
                lineHeight=Math.max(lineHeight,childView.getMeasuredHeight()+params.topMargin+params.bottomMargin);
            }else{
                t+=lineHeight;
                lineWidth=childView.getMeasuredWidth()+params.topMargin+params.bottomMargin;
                lineHeight=childView.getMeasuredHeight()+params.topMargin+params.bottomMargin;
            }
            childView.layout(lineWidth-(childView.getMeasuredWidth()+params.leftMargin)
                    ,t+params.topMargin,lineWidth-params.rightMargin,t+lineHeight-params.bottomMargin);
        }
    }

    public void appendTag(String tagInfo){
        LayoutParams param=new LayoutParams(LayoutParams.WRAP_CONTENT
                , LayoutParams.WRAP_CONTENT);
        MarginLayoutParams marginParams=new MarginLayoutParams(param);
        marginParams.topMargin=mTagMarginTopBottom;
        marginParams.bottomMargin=mTagMarginTopBottom;
        marginParams.rightMargin=mTagMarginLeftRight;
        marginParams.leftMargin=mTagMarginLeftRight;
        TextView tv=new TextView(getContext());
        setTagState(tv,ACTION_NORMAL);
        tv.setText(tagInfo);
        tv.setPadding(mTagPaddingLeftRight,mTagPaddingTopBottom,mTagPaddingLeftRight,mTagPaddingTopBottom);
        addView(tv,marginParams);
        mSelectedList.add(false);
    }

    public void removeAllTags(){
        removeAllViews();
        mSelectedList.clear();
    }

    private void setTagAction(View childView, final int i) {
        if(mSelectMode==MODE_CLICK){
            childView.setOnTouchListener(this);
        }
        childView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                onTagClick(v, i);
            }
        });
    }

    public void onTagClick(View v,int which){
        switch (mSelectMode){
            case MODE_CLICK:
                if(mOnTagClickCallback!=null) mOnTagClickCallback.onTagClick(which);
                break;
            case MODE_SINGLE:
                //reset background
                for (int i = 0; i < getChildCount(); i++) {
                    TextView tag= (TextView) getChildAt(i);
                    setTagState(tag,ACTION_NORMAL);
                }
                //set select background
                setTagState((TextView) v,ACTION_PRESSED);
                if(mOnTagChosenCallback !=null) mOnTagChosenCallback.onTagChosen(which);
                break;
            case MODE_MULTI:
                if(!mSelectedList.get(which)) {
                    setTagState((TextView) v,ACTION_PRESSED);
                }else{
                    setTagState((TextView) v,ACTION_NORMAL);
                }
                break;
        }
        invalidate();
        mSelectedList.set(which,!mSelectedList.get(which));
        if(mOnTagChosenCallback !=null&&mSelectMode==2){
            List<Integer> chosenTags=new ArrayList<>();
            for (int i = 0; i < mSelectedList.size(); i++) {
                if(mSelectedList.get(i)) chosenTags.add(i);
            }
            int[] those=new int[chosenTags.size()];
            for (int i = 0; i < chosenTags.size(); i++) {
                those[i]=chosenTags.get(i).intValue();
            }
            mOnTagChosenCallback.onTagChosen(those);
        }
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        switch (event.getAction()){
            case MotionEvent.ACTION_DOWN:
                setTagState((TextView) v,ACTION_PRESSED);
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                setTagState((TextView) v,ACTION_NORMAL);
                break;
        }
        return false;
    }

    @SuppressLint("NewApi")
    private void setTagState(TextView v, int action){
        switch (action){
            case ACTION_NORMAL:
                v.setBackground(mDefaultDrawable);
                v.setTextColor(mTagTextColor);
                break;
            case ACTION_PRESSED:
                v.setBackground(mSelectedDrawable);
                v.setTextColor(mTagSelectedColor);
                break;
        }
    }

    @Override
    public LayoutParams generateLayoutParams(AttributeSet attrs) {
        return new MarginLayoutParams(getContext(),attrs);
    }

    @Override
    protected LayoutParams generateLayoutParams(LayoutParams p) {
        return new MarginLayoutParams(p);
    }

    @Override
    protected LayoutParams generateDefaultLayoutParams() {
        return new MarginLayoutParams(LayoutParams.MATCH_PARENT,LayoutParams.MATCH_PARENT);
    }

    public int dp2px(int value){
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,value,getContext()
                .getResources().getDisplayMetrics());
    }

    public void setTagMode(int mode){
        if(mode!=MODE_CLICK&&mode!=MODE_SINGLE&&mode!=MODE_MULTI) return;
        this.mSelectMode=mode;
        postInvalidate();
    }

    public void setOnTagClickCallback(OnTagClickCallback mOnTagClickCallback) {
        this.mOnTagClickCallback = mOnTagClickCallback;
    }

    public void setOnTagChosenCallback(OnTagChosenCallback mOnTagChosenCallback) {
        this.mOnTagChosenCallback = mOnTagChosenCallback;
    }

    public interface OnTagClickCallback{
        void onTagClick(int which);
    }


    public interface OnTagChosenCallback {
        void onTagChosen(int... those);
    }
}
