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

package com.ccs.lockscreen.security;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.os.Build;
import android.os.Debug;
import android.os.Handler;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.SystemClock;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.HapticFeedbackConstants;
import android.view.MotionEvent;
import android.view.View;
import android.view.accessibility.AccessibilityManager;
import android.view.animation.AnimationUtils;
import android.view.animation.Interpolator;
import android.widget.TextView;
import android.widget.Toast;

import com.ccs.lockscreen.R;
import com.ccs.lockscreen.myclocker.C;

import java.util.ArrayList;
import java.util.List;

public class LockPatternView extends View{
    private static final int ASPECT_SQUARE = 0; // View will be the minimum of width/height
    private static final int ASPECT_LOCK_WIDTH = 1; // Fixed width; height will be minimum of (w,h)
    private static final int ASPECT_LOCK_HEIGHT = 2; // Fixed height; width will be minimum of (w,h)
    private static final boolean PROFILE_DRAWING = false;
    /**
     * How many milliseconds we spend animating each circle of a lock pattern if
     * the animating mode is set. The entire animation should take this constant
     * * the length of the pattern to complete.
     */
    private static final int MILLIS_PER_CIRCLE_ANIMATING = 700;
    /**
     * This can be used to avoid updating the display for very small motions or
     * noisy panels. It didn't seem to have much impact on the devices tested,
     * so currently set to 0.
     */
    private static final float DRAG_THRESHHOLD = 0.0f;
    // Aspect to use when rendering this view
    private Context context;
    private CellState[][] mCellStates;
    private int mDotSize;
    private int mDotSizeActivated;
    private int mPathWidth;
    private boolean mDrawingProfilingStarted = false;
    private Paint mPaint = new Paint();
    private Paint mPathPaint = new Paint();
    private OnPatternListener callback;
    private ArrayList<Cell> mPattern = new ArrayList<Cell>(9);
    /**
     * Lookup table for the circles of the pattern we are currently drawing.
     * This will be the cells of the complete pattern unless we are animating,
     * in which case we use this to hold the cells we are drawing for the in
     * progress animation.
     */
    private boolean[][] mPatternDrawLookup = new boolean[3][3];
    /**
     * the in progress point: - during interaction: where the user's finger is -
     * during animation: the current tip of the animating line
     */
    private float mInProgressX = -1;
    private float mInProgressY = -1;
    private long mAnimatingPeriodStart;
    private DisplayMode mPatternDisplayMode = DisplayMode.Correct;
    private boolean mInputEnabled = true;
    private boolean mInStealthMode = false;
    private boolean mEnableHapticFeedback = true;
    private boolean mPatternInProgress = false;
    private float mHitFactor = 0.6f;
    private float mSquareWidth;
    private float mSquareHeight;
    private Path mCurrentPath = new Path();
    private Rect mInvalidate = new Rect();
    private Rect mTmpInvalidateRect = new Rect();
    private int mAspect;
    private int mRegularColor;
    private int mErrorColor;
    private int mSuccessColor;
    private Interpolator mFastOutSlowInInterpolator;
    private Interpolator mLinearOutSlowInInterpolator;
    private TextView txtPassWordDesc;
    private boolean isSettingMode, isFirstTry = true;
    private String strPattern1st = "", strPattern2nd = "";
    private int attempts = 0;

    //
    public LockPatternView(Context context,OnPatternListener callback,
            final TextView txtPassWordDesc,boolean isSettingMode){
        this(context,callback,txtPassWordDesc,isSettingMode,null);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public LockPatternView(Context context,OnPatternListener callback,
            final TextView txtPassWordDesc,boolean isSettingMode,AttributeSet attrs){
        super(context,attrs);
        this.context = context;
        this.callback = callback;
        this.txtPassWordDesc = txtPassWordDesc;
        this.isSettingMode = isSettingMode;

        TypedArray a = context.obtainStyledAttributes(attrs,R.styleable.LockPatternView);
        final String aspect = a.getString(R.styleable.LockPatternView_aspect);

        if("square".equals(aspect)){
            mAspect = ASPECT_SQUARE;
        }else if("lock_width".equals(aspect)){
            mAspect = ASPECT_LOCK_WIDTH;
        }else if("lock_height".equals(aspect)){
            mAspect = ASPECT_LOCK_HEIGHT;
        }else{
            mAspect = ASPECT_SQUARE;
        }

        setClickable(true);

        mPathPaint.setAntiAlias(true);
        mPathPaint.setDither(true);

        mRegularColor = ContextCompat.getColor(context,R.color.lock_pattern_view_regular_light);
        mErrorColor = ContextCompat.getColor(context,R.color.lock_pattern_view_error_light);
        mSuccessColor = ContextCompat.getColor(context,R.color.lock_pattern_view_success_light);

        mRegularColor = a.getColor(R.styleable.LockPatternView_regularColor,mRegularColor);
        mErrorColor = a.getColor(R.styleable.LockPatternView_errorColor,mErrorColor);
        mSuccessColor = a.getColor(R.styleable.LockPatternView_successColor,mSuccessColor);

        int pathColor = a.getColor(R.styleable.LockPatternView_pathColor,mRegularColor);
        mPathPaint.setColor(pathColor);

        mPathPaint.setStyle(Paint.Style.STROKE);
        mPathPaint.setStrokeJoin(Paint.Join.ROUND);
        mPathPaint.setStrokeCap(Paint.Cap.ROUND);

        mPathWidth = getResources().getDimensionPixelSize(R.dimen.lock_pattern_dot_line_width);
        mPathPaint.setStrokeWidth(mPathWidth);

        mDotSize = getResources().getDimensionPixelSize(R.dimen.lock_pattern_dot_size);
        mDotSizeActivated = getResources().getDimensionPixelSize(R.dimen.lock_pattern_dot_size_activated);

        mPaint.setAntiAlias(true);
        mPaint.setDither(true);

        mCellStates = new CellState[3][3];
        for(int i = 0; i<3; i++){
            for(int j = 0; j<3; j++){
                mCellStates[i][j] = new CellState();
                mCellStates[i][j].size = mDotSize;
            }
        }

        if(C.isAndroid(Build.VERSION_CODES.LOLLIPOP)){
            mFastOutSlowInInterpolator = AnimationUtils.loadInterpolator(context,android.R.interpolator.fast_out_slow_in);
            mLinearOutSlowInInterpolator = AnimationUtils.loadInterpolator(context,android.R.interpolator.linear_out_slow_in);
        }
    }

    @Override
    public boolean onHoverEvent(MotionEvent event){
        if(((AccessibilityManager)getContext().getSystemService(Context.ACCESSIBILITY_SERVICE)).isTouchExplorationEnabled()){
            final int action = event.getAction();
            switch(action){
                case MotionEvent.ACTION_HOVER_ENTER:
                    event.setAction(MotionEvent.ACTION_DOWN);
                    break;
                case MotionEvent.ACTION_HOVER_MOVE:
                    event.setAction(MotionEvent.ACTION_MOVE);
                    break;
                case MotionEvent.ACTION_HOVER_EXIT:
                    event.setAction(MotionEvent.ACTION_UP);
                    break;
            }
            onTouchEvent(event);
            event.setAction(action);
        }
        return super.onHoverEvent(event);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event){
        if(!mInputEnabled || !isEnabled()){
            return false;
        }

        switch(event.getAction()){
            case MotionEvent.ACTION_DOWN:
                handleActionDown(event);
                return true;
            case MotionEvent.ACTION_UP:
                handleActionUp(event);
                return true;
            case MotionEvent.ACTION_MOVE:
                handleActionMove(event);
                return true;
            case MotionEvent.ACTION_CANCEL:
                if(mPatternInProgress){
                    mPatternInProgress = false;
                    resetPattern();
                    notifyPatternCleared();
                }
                if(PROFILE_DRAWING){
                    if(mDrawingProfilingStarted){
                        Debug.stopMethodTracing();
                        mDrawingProfilingStarted = false;
                    }
                }
                return true;
        }
        return false;
    }

    @Override
    protected void onSizeChanged(int w,int h,int oldw,int oldh){
        final int width = w-getPaddingLeft()-getPaddingRight();
        mSquareWidth = width/3.0f;

        final int height = h-getPaddingTop()-getPaddingBottom();
        mSquareHeight = height/3.0f;
    }

    @Override
    protected void onDraw(Canvas canvas){
        final ArrayList<Cell> pattern = mPattern;
        final int count = pattern.size();
        final boolean[][] drawLookup = mPatternDrawLookup;

        if(mPatternDisplayMode==DisplayMode.Animate){
            //Log.e("onDraw","DisplayMode.Animate: "+mPattern);
            // figure out which circles to draw
            // + 1 so we pause on complete pattern
            final int oneCycle = (count+1)*MILLIS_PER_CIRCLE_ANIMATING;
            final int spotInCycle = (int)(SystemClock.elapsedRealtime()-mAnimatingPeriodStart)%oneCycle;
            final int numCircles = spotInCycle/MILLIS_PER_CIRCLE_ANIMATING;

            clearPatternDrawLookup();
            for(int i = 0; i<numCircles; i++){
                final Cell cell = pattern.get(i);
                drawLookup[cell.getRow()][cell.getColumn()] = true;
            }

            // figure out in progress portion of ghosting line

            final boolean needToUpdateInProgressPoint = numCircles>0 && numCircles<count;

            if(needToUpdateInProgressPoint){
                final float percentageOfNextCircle = ((float)(spotInCycle%MILLIS_PER_CIRCLE_ANIMATING))/MILLIS_PER_CIRCLE_ANIMATING;

                final Cell currentCell = pattern.get(numCircles-1);
                final float centerX = getCenterXForColumn(currentCell.column);
                final float centerY = getCenterYForRow(currentCell.row);

                final Cell nextCell = pattern.get(numCircles);
                final float dx = percentageOfNextCircle*(getCenterXForColumn(nextCell.column)-centerX);
                final float dy = percentageOfNextCircle*(getCenterYForRow(nextCell.row)-centerY);
                mInProgressX = centerX+dx;
                mInProgressY = centerY+dy;
            }
            // TODO: Infinite loop here...
            invalidate();
        }

        final Path currentPath = mCurrentPath;
        currentPath.rewind();

        // draw the circles
        for(int i = 0; i<3; i++){
            float centerY = getCenterYForRow(i);
            for(int j = 0; j<3; j++){
                CellState cellState = mCellStates[i][j];
                float centerX = getCenterXForColumn(j);
                float size = cellState.size*cellState.scale;
                float translationY = cellState.translateY;
                drawCircle(canvas,(int)centerX,(int)centerY+translationY,size,drawLookup[i][j],cellState.alpha);
            }
        }

        // TODO: the path should be created and cached every time we hit-detect
        // a cell
        // only the last segment of the path should be computed here
        // draw the path of the pattern (unless we are in stealth mode)
        final boolean drawPath = !mInStealthMode;

        if(drawPath){
            //Log.e("onDraw","drawPath: "+mPattern);
            mPathPaint.setColor(getCurrentColor(true /* partOfPattern */));

            boolean anyCircles = false;
            float lastX = 0f;
            float lastY = 0f;
            for(int i = 0; i<count; i++){
                Cell cell = pattern.get(i);

                // only draw the part of the pattern stored in
                // the lookup table (this is only different in the case
                // of animation).
                if(!drawLookup[cell.row][cell.column]){
                    break;
                }
                anyCircles = true;

                float centerX = getCenterXForColumn(cell.column);
                float centerY = getCenterYForRow(cell.row);
                if(i!=0){
                    CellState state = mCellStates[cell.row][cell.column];
                    currentPath.rewind();
                    currentPath.moveTo(lastX,lastY);
                    if(state.lineEndX!=Float.MIN_VALUE && state.lineEndY!=Float.MIN_VALUE){
                        currentPath.lineTo(state.lineEndX,state.lineEndY);
                    }else{
                        currentPath.lineTo(centerX,centerY);
                    }
                    canvas.drawPath(currentPath,mPathPaint);
                }
                lastX = centerX;
                lastY = centerY;
            }

            // draw last in progress section
            if((mPatternInProgress || mPatternDisplayMode==DisplayMode.Animate) && anyCircles){
                currentPath.rewind();
                currentPath.moveTo(lastX,lastY);
                currentPath.lineTo(mInProgressX,mInProgressY);

                mPathPaint.setAlpha((int)(calculateLastSegmentAlpha(mInProgressX,mInProgressY,lastX,lastY)*255f));
                canvas.drawPath(currentPath,mPathPaint);
            }
        }
    }

    @Override
    protected Parcelable onSaveInstanceState(){
        Parcelable superState = super.onSaveInstanceState();
        return new SavedState(superState,patternToString(mPattern),mPatternDisplayMode.ordinal(),mInputEnabled,mInStealthMode,mEnableHapticFeedback);
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state){
        final SavedState ss = (SavedState)state;
        super.onRestoreInstanceState(ss.getSuperState());
        setPattern(DisplayMode.Correct,stringToPattern(ss.getSerializedPattern()));
        mPatternDisplayMode = DisplayMode.values()[ss.getDisplayMode()];
        mInputEnabled = ss.isInputEnabled();
        mInStealthMode = ss.isInStealthMode();
        mEnableHapticFeedback = ss.isTactileFeedbackEnabled();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec,int heightMeasureSpec){
        final int minimumWidth = getSuggestedMinimumWidth();
        final int minimumHeight = getSuggestedMinimumHeight();
        int viewWidth = resolveMeasured(widthMeasureSpec,minimumWidth);
        int viewHeight = resolveMeasured(heightMeasureSpec,minimumHeight);

        switch(mAspect){
            case ASPECT_SQUARE:
                viewWidth = viewHeight = Math.min(viewWidth,viewHeight);
                break;
            case ASPECT_LOCK_WIDTH:
                viewHeight = Math.min(viewWidth,viewHeight);
                break;
            case ASPECT_LOCK_HEIGHT:
                viewWidth = Math.min(viewWidth,viewHeight);
                break;
        }
        // Log.v(TAG, "LockPatternView dimensions: " + viewWidth + "x" +
        // viewHeight);
        setMeasuredDimension(viewWidth,viewHeight);
    }

    private int resolveMeasured(int measureSpec,int desired){
        int result = 0;
        int specSize = MeasureSpec.getSize(measureSpec);
        switch(MeasureSpec.getMode(measureSpec)){
            case MeasureSpec.UNSPECIFIED:
                result = desired;
                break;
            case MeasureSpec.AT_MOST:
                result = Math.max(specSize,desired);
                break;
            case MeasureSpec.EXACTLY:
            default:
                result = specSize;
        }
        return result;
    }

    public void setPattern(DisplayMode displayMode,List<Cell> pattern){
        mPattern.clear();
        mPattern.addAll(pattern);
        clearPatternDrawLookup();
        for(Cell cell : pattern){
            mPatternDrawLookup[cell.getRow()][cell.getColumn()] = true;
        }

        setDisplayMode(displayMode);
    }

    public List<LockPatternView.Cell> stringToPattern(String string){
        List<LockPatternView.Cell> result = Lists.newArrayList();

        final byte[] bytes = string.getBytes();
        for(int i = 0; i<bytes.length; i++){
            byte b = bytes[i];
            result.add(LockPatternView.Cell.of(b/3,b%3));
        }
        return result;
    }

    private void clearPatternDrawLookup(){
        for(int i = 0; i<3; i++){
            for(int j = 0; j<3; j++){
                mPatternDrawLookup[i][j] = false;
            }
        }
    }

    public void setDisplayMode(DisplayMode displayMode){
        setDisplayMode(displayMode,null);
    }

    public void setDisplayMode(DisplayMode displayMode,List<Cell> pattern){
        mPatternDisplayMode = displayMode;
        if(pattern==null){
            pattern = mPattern;
        }
        if(displayMode==DisplayMode.Animate){
            if(pattern.size()==0){
                throw new IllegalStateException("you must have a pattern to "+"animate if you want to set the display mode to animate");
            }
            mAnimatingPeriodStart = SystemClock.elapsedRealtime();
            final Cell first = pattern.get(0);
            mInProgressX = getCenterXForColumn(first.getColumn());
            mInProgressY = getCenterYForRow(first.getRow());
            clearPatternDrawLookup();
        }
        invalidate();
    }

    private float getCenterXForColumn(int column){
        return getPaddingLeft()+column*mSquareWidth+mSquareWidth/2f;
    }

    private float getCenterYForRow(int row){
        return getPaddingTop()+row*mSquareHeight+mSquareHeight/2f;
    }

    public String patternToString(List<LockPatternView.Cell> pattern){
        if(pattern==null){
            return "";
        }
        final int patternSize = pattern.size();

        String str = "";
        byte[] res = new byte[patternSize];
        for(int i = 0; i<patternSize; i++){
            LockPatternView.Cell cell = pattern.get(i);
            int val = cell.getRow()*3+cell.getColumn();
            str += val+";";
            res[i] = (byte)(val);
        }
        //Log.e("patternToString",": "+res+"/"+str);
        //return new String(res);
        return str;
    }

    private void drawCircle(Canvas canvas,float centerX,float centerY,float size,
            boolean partOfPattern,float alpha){
        mPaint.setColor(getCurrentColor(partOfPattern));
        mPaint.setAlpha((int)(alpha*255));
        canvas.drawCircle(centerX,centerY,size/2,mPaint);
    }

    private int getCurrentColor(boolean partOfPattern){
        if(!partOfPattern || mInStealthMode || mPatternInProgress){
            // unselected circle
            return mRegularColor;
        }else if(mPatternDisplayMode==DisplayMode.Wrong){
            // the pattern is wrong
            return mErrorColor;
        }else if(mPatternDisplayMode==DisplayMode.Correct || mPatternDisplayMode==DisplayMode.Animate){
            return mSuccessColor;
        }else{
            throw new IllegalStateException("unknown display mode "+mPatternDisplayMode);
        }
    }

    private float calculateLastSegmentAlpha(float x,float y,float lastX,float lastY){
        float diffX = x-lastX;
        float diffY = y-lastY;
        float dist = (float)Math.sqrt(diffX*diffX+diffY*diffY);
        float frac = dist/mSquareWidth;
        return Math.min(1f,Math.max(0f,(frac-0.3f)*4f));
    }

    public boolean isInStealthMode(){
        return mInStealthMode;
    }

    //
    public void setInStealthMode(boolean inStealthMode){
        mInStealthMode = inStealthMode;
    }

    public boolean isTactileFeedbackEnabled(){
        return mEnableHapticFeedback;
    }

    public void setTactileFeedbackEnabled(boolean tactileFeedbackEnabled){
        mEnableHapticFeedback = tactileFeedbackEnabled;
    }

    public CellState[][] getCellStates(){
        return mCellStates;
    }

    public void setOnPatternListener(OnPatternListener onPatternListener){
        callback = onPatternListener;
    }

    public void clearPattern(){
        resetPattern();
    }

    private void resetPattern(){
        mPattern.clear();
        clearPatternDrawLookup();
        mPatternDisplayMode = DisplayMode.Correct;
        invalidate();
    }

    public void disableInput(){
        mInputEnabled = false;
    }

    public void enableInput(){
        mInputEnabled = true;
    }

    public void reset(){
        isFirstTry = true;
        strPattern1st = "";
        strPattern2nd = "";
        txtPassWordDesc.setText(R.string.type_new_password);
        resetPattern();
    }

    //
    private void performHapticFeedback(){
        performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY,HapticFeedbackConstants.FLAG_IGNORE_VIEW_SETTING | HapticFeedbackConstants.FLAG_IGNORE_GLOBAL_SETTING);
    }

    private void saveSettings(boolean isPasswordSetCompleted){
        final SharedPreferences.Editor editor = context.getSharedPreferences(C.PREFS_NAME,0).edit();
        if(isPasswordSetCompleted){
            editor.putString(C.PATTERN_LOCK,strPattern2nd);
            editor.putBoolean("cBoxReturnLS",true);
        }else{
            editor.putString(C.PATTERN_LOCK,"");
        }
        editor.commit();
    }

    private void handleActionDown(MotionEvent event){
        //Log.e("handleActionDown","mPattern: "+LockPatternUtils.patternToString(mPattern));
        resetPattern();
        final float x = event.getX();
        final float y = event.getY();
        final Cell hitCell = detectAndAddHit(x,y);
        if(hitCell!=null){
            mPatternInProgress = true;
            mPatternDisplayMode = DisplayMode.Correct;
            notifyPatternStarted();
        }else if(mPatternInProgress){
            mPatternInProgress = false;
            notifyPatternCleared();
        }
        if(hitCell!=null){
            final float startX = getCenterXForColumn(hitCell.column);
            final float startY = getCenterYForRow(hitCell.row);

            final float widthOffset = mSquareWidth/2f;
            final float heightOffset = mSquareHeight/2f;

            invalidate((int)(startX-widthOffset),(int)(startY-heightOffset),(int)(startX+widthOffset),(int)(startY+heightOffset));
        }
        mInProgressX = x;
        mInProgressY = y;
        if(PROFILE_DRAWING){
            if(!mDrawingProfilingStarted){
                Debug.startMethodTracing("LockPatternDrawing");
                mDrawingProfilingStarted = true;
            }
        }
    }

    private void handleActionMove(MotionEvent event){
        // Handle all recent motion events so we don't skip any cells even when
        // the device
        // is busy...
        final float radius = mPathWidth;
        final int historySize = event.getHistorySize();
        mTmpInvalidateRect.setEmpty();
        boolean invalidateNow = false;
        for(int i = 0; i<historySize+1; i++){
            final float x = i<historySize ? event.getHistoricalX(i) : event.getX();
            final float y = i<historySize ? event.getHistoricalY(i) : event.getY();
            Cell hitCell = detectAndAddHit(x,y);
            final int patternSize = mPattern.size();
            if(hitCell!=null && patternSize==1){
                mPatternInProgress = true;
                notifyPatternStarted();
            }
            // note current x and y for rubber banding of in progress patterns
            final float dx = Math.abs(x-mInProgressX);
            final float dy = Math.abs(y-mInProgressY);
            if(dx>DRAG_THRESHHOLD || dy>DRAG_THRESHHOLD){
                invalidateNow = true;
            }

            if(mPatternInProgress && patternSize>0){
                final ArrayList<Cell> pattern = mPattern;
                final Cell lastCell = pattern.get(patternSize-1);
                float lastCellCenterX = getCenterXForColumn(lastCell.column);
                float lastCellCenterY = getCenterYForRow(lastCell.row);

                // Adjust for drawn segment from last cell to (x,y). Radius
                // accounts for line width.
                float left = Math.min(lastCellCenterX,x)-radius;
                float right = Math.max(lastCellCenterX,x)+radius;
                float top = Math.min(lastCellCenterY,y)-radius;
                float bottom = Math.max(lastCellCenterY,y)+radius;

                // Invalidate between the pattern's new cell and the pattern's
                // previous cell
                if(hitCell!=null){
                    final float width = mSquareWidth*0.5f;
                    final float height = mSquareHeight*0.5f;
                    final float hitCellCenterX = getCenterXForColumn(hitCell.column);
                    final float hitCellCenterY = getCenterYForRow(hitCell.row);

                    left = Math.min(hitCellCenterX-width,left);
                    right = Math.max(hitCellCenterX+width,right);
                    top = Math.min(hitCellCenterY-height,top);
                    bottom = Math.max(hitCellCenterY+height,bottom);
                }

                // Invalidate between the pattern's last cell and the previous
                // location
                mTmpInvalidateRect.union(Math.round(left),Math.round(top),Math.round(right),Math.round(bottom));
            }
        }
        mInProgressX = event.getX();
        mInProgressY = event.getY();

        // To save updates, we only invalidate if the user moved beyond a
        // certain amount.
        if(invalidateNow){
            mInvalidate.union(mTmpInvalidateRect);
            invalidate(mInvalidate);
            mInvalidate.set(mTmpInvalidateRect);
        }
    }

    private void handleActionUp(MotionEvent event){
        //Log.e("handleActionUp","mPattern: "+mPattern);
        if(!mPattern.isEmpty()){
            mPatternInProgress = false;
            cancelLineAnimations();
            notifyPatternDetected();
            invalidate();
        }
        if(PROFILE_DRAWING){
            if(mDrawingProfilingStarted){
                Debug.stopMethodTracing();
                mDrawingProfilingStarted = false;
            }
        }
    }

    private void notifyCellAdded(){
        //sendAccessEvent(R.string.lockscreen_access_pattern_cell_added);
        if(callback!=null){
            callback.onPatternCellAdded(mPattern);
        }
    }

    private void notifyPatternStarted(){
        sendAccessEvent(R.string.youtube);//do nothing
        //sendAccessEvent(R.string.lockscreen_access_pattern_start);
        if(callback!=null){
            callback.onPatternStart();
        }
    }

    private void notifyPatternDetected(){
        //sendAccessEvent(R.string.lockscreen_access_pattern_detected);
        if(callback!=null){
            callback.onPatternDetected(mPattern);
            final String currentPattern = patternToString(mPattern);
            if(currentPattern!=null && !currentPattern.isEmpty()){
                if(isSettingMode){
                    if(isFirstTry){
                        strPattern1st = currentPattern;
                        resetPattern();
                        isFirstTry = false;
                        txtPassWordDesc.setText(R.string.type_confirm_password);
                        //Log.e("notifyPatternDetected1",": "+strPattern1st+"/"+strPattern2nd);
                    }else{
                        strPattern2nd = currentPattern;
                        if(strPattern1st.equals(strPattern2nd)){
                            saveSettings(true);
                            callback.onSettingFinish(Activity.RESULT_OK);
                        }else{
                            saveSettings(false);

                            setDisplayMode(DisplayMode.Wrong,mPattern);
                            performHapticFeedback();
                            Toast t = Toast.makeText(context,R.string.password_not_matched,Toast.LENGTH_SHORT);
                            t.setGravity(Gravity.TOP,0,50);
                            t.show();
                            new Handler().postDelayed(new Runnable(){
                                @Override
                                public void run(){
                                    resetPattern();
                                    setDisplayMode(DisplayMode.Correct,mPattern);
                                }
                            },3000);
                        }
                        //Log.e("notifyPatternDetected2",": "+strPattern1st+"/"+strPattern2nd);
                    }
                }else{
                    SharedPreferences prefs = context.getSharedPreferences(C.PREFS_NAME,Context.MODE_PRIVATE);
                    String savedPattern = prefs.getString(C.PATTERN_LOCK,"");
                    //Log.e("notifyPatternDetected3",": "+currentPattern+"/"+savedPattern);
                    if(savedPattern.equals(currentPattern)){
                        callback.onFinish(true,false);
                    }else{
                        //attempt fails
                        attempts = callback.onAttemptsFailed(attempts);
                        setDisplayMode(DisplayMode.Wrong,mPattern);
                        performHapticFeedback();
                        Toast t = Toast.makeText(context,R.string.password_not_matched,Toast.LENGTH_SHORT);
                        t.setGravity(Gravity.TOP,0,50);
                        t.show();
                        new Handler().postDelayed(new Runnable(){
                            @Override
                            public void run(){
                                resetPattern();
                                setDisplayMode(DisplayMode.Correct,mPattern);
                            }
                        },1000);
                    }
                }
            }
        }
    }

    private void notifyPatternCleared(){
        //sendAccessEvent(R.string.lockscreen_access_pattern_cleared);
        if(callback!=null){
            callback.onPatternCleared();
        }
    }

    /**
     * Determines whether the point x, y will add a new point to the current
     * pattern (in addition to finding the cell, also makes heuristic choices
     * such as filling in gaps based on current pattern).
     *
     * @param x The x coordinate.
     * @param y The y coordinate.
     */
    private Cell detectAndAddHit(float x,float y){
        final Cell cell = checkForNewHit(x,y);
        if(cell!=null){
            // check for gaps in existing pattern
            Cell fillInGapCell = null;
            final ArrayList<Cell> pattern = mPattern;
            if(!pattern.isEmpty()){
                final Cell lastCell = pattern.get(pattern.size()-1);
                int dRow = cell.row-lastCell.row;
                int dColumn = cell.column-lastCell.column;

                int fillInRow = lastCell.row;
                int fillInColumn = lastCell.column;

                if(Math.abs(dRow)==2 && Math.abs(dColumn)!=1){
                    fillInRow = lastCell.row+((dRow>0) ? 1 : -1);
                }

                if(Math.abs(dColumn)==2 && Math.abs(dRow)!=1){
                    fillInColumn = lastCell.column+((dColumn>0) ? 1 : -1);
                }

                fillInGapCell = Cell.of(fillInRow,fillInColumn);
            }

            if(fillInGapCell!=null && !mPatternDrawLookup[fillInGapCell.row][fillInGapCell.column]){
                addCellToPattern(fillInGapCell);
            }
            addCellToPattern(cell);
            if(mEnableHapticFeedback){
                performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY,HapticFeedbackConstants.FLAG_IGNORE_VIEW_SETTING | HapticFeedbackConstants.FLAG_IGNORE_GLOBAL_SETTING);
            }
            return cell;
        }
        return null;
    }

    private void addCellToPattern(Cell newCell){
        //Log.e("addCellToPattern","addCellToPattern: "+newCell);
        mPatternDrawLookup[newCell.getRow()][newCell.getColumn()] = true;
        mPattern.add(newCell);
        if(!mInStealthMode){
            startCellActivatedAnimation(newCell);
        }
        notifyCellAdded();
    }

    private void startCellActivatedAnimation(Cell cell){
        final CellState cellState = mCellStates[cell.row][cell.column];
        startSizeAnimation(mDotSize,mDotSizeActivated,96,mLinearOutSlowInInterpolator,cellState,new Runnable(){

            @Override
            public void run(){
                startSizeAnimation(mDotSizeActivated,mDotSize,192,mFastOutSlowInInterpolator,cellState,null);
            }
        });
        startLineEndAnimation(cellState,mInProgressX,mInProgressY,getCenterXForColumn(cell.column),getCenterYForRow(cell.row));
    }

    private void startLineEndAnimation(final CellState state,final float startX,final float startY,
            final float targetX,final float targetY){
        ValueAnimator valueAnimator = ValueAnimator.ofFloat(0,1);
        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener(){

            @Override
            public void onAnimationUpdate(ValueAnimator animation){
                float t = (Float)animation.getAnimatedValue();
                state.lineEndX = (1-t)*startX+t*targetX;
                state.lineEndY = (1-t)*startY+t*targetY;
                invalidate();
            }
        });
        valueAnimator.addListener(new AnimatorListenerAdapter(){

            @Override
            public void onAnimationEnd(Animator animation){
                state.lineAnimator = null;
            }
        });
        valueAnimator.setInterpolator(mFastOutSlowInInterpolator);
        valueAnimator.setDuration(100);
        valueAnimator.start();
        state.lineAnimator = valueAnimator;
    }

    private void startSizeAnimation(float start,float end,long duration,Interpolator interpolator,
            final CellState state,final Runnable endRunnable){
        ValueAnimator valueAnimator = ValueAnimator.ofFloat(start,end);
        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener(){

            @Override
            public void onAnimationUpdate(ValueAnimator animation){
                state.size = (Float)animation.getAnimatedValue();
                invalidate();
            }
        });
        if(endRunnable!=null){
            valueAnimator.addListener(new AnimatorListenerAdapter(){

                @Override
                public void onAnimationEnd(Animator animation){
                    endRunnable.run();
                }
            });
        }
        valueAnimator.setInterpolator(interpolator);
        valueAnimator.setDuration(duration);
        valueAnimator.start();
    }

    private Cell checkForNewHit(float x,float y){
        final int rowHit = getRowHit(y);
        if(rowHit<0){
            return null;
        }
        final int columnHit = getColumnHit(x);
        if(columnHit<0){
            return null;
        }

        if(mPatternDrawLookup[rowHit][columnHit]){
            return null;
        }
        return Cell.of(rowHit,columnHit);
    }

    private int getRowHit(float y){

        final float squareHeight = mSquareHeight;
        float hitSize = squareHeight*mHitFactor;

        float offset = getPaddingTop()+(squareHeight-hitSize)/2f;
        for(int i = 0; i<3; i++){

            final float hitTop = offset+squareHeight*i;
            if(y>=hitTop && y<=hitTop+hitSize){
                return i;
            }
        }
        return -1;
    }

    private int getColumnHit(float x){
        final float squareWidth = mSquareWidth;
        float hitSize = squareWidth*mHitFactor;

        float offset = getPaddingLeft()+(squareWidth-hitSize)/2f;
        for(int i = 0; i<3; i++){

            final float hitLeft = offset+squareWidth*i;
            if(x>=hitLeft && x<=hitLeft+hitSize){
                return i;
            }
        }
        return -1;
    }

    private void sendAccessEvent(int resId){
        announceForAccessibility(getContext().getString(resId));
    }

    private void cancelLineAnimations(){
        for(int i = 0; i<3; i++){
            for(int j = 0; j<3; j++){
                CellState state = mCellStates[i][j];
                if(state.lineAnimator!=null){
                    state.lineAnimator.cancel();
                    state.lineEndX = Float.MIN_VALUE;
                    state.lineEndY = Float.MIN_VALUE;
                }
            }
        }
    }

    public enum DisplayMode{Correct,Animate,Wrong}

    public static interface OnPatternListener{
        void onSettingFinish(final int result);

        void onFinish(final boolean unlock,final boolean isEmergencyCall);

        int onAttemptsFailed(int attempts);

        void onPatternStart();

        void onPatternCleared();

        void onPatternCellAdded(List<Cell> pattern);

        void onPatternDetected(List<Cell> pattern);
    }

    public static class Cell{
        // keep # objects limited to 9
        static Cell[][] sCells = new Cell[3][3];

        static{
            for(int i = 0; i<3; i++){
                for(int j = 0; j<3; j++){
                    sCells[i][j] = new Cell(i,j);
                }
            }
        }

        int row;
        int column;

        private Cell(int row,int column){
            checkRange(row,column);
            this.row = row;
            this.column = column;
        }

        private static void checkRange(int row,int column){
            if(row<0 || row>2){
                throw new IllegalArgumentException("row must be in range 0-2");
            }
            if(column<0 || column>2){
                throw new IllegalArgumentException("column must be in range 0-2");
            }
        }

        /**
         * @param row    The row of the cell.
         * @param column The column of the cell.
         */
        public static synchronized Cell of(int row,int column){
            checkRange(row,column);
            return sCells[row][column];
        }

        public int getRow(){
            return row;
        }

        public int getColumn(){
            return column;
        }

        public String toString(){
            return "(row="+row+",clmn="+column+")";
        }
    }

    public static class CellState{
        public float scale = 1.0f;
        public float translateY = 0.0f;
        public float alpha = 1.0f;
        public float size;
        public float lineEndX = Float.MIN_VALUE;
        public float lineEndY = Float.MIN_VALUE;
        public ValueAnimator lineAnimator;
    }

    private static class SavedState extends BaseSavedState{

        @SuppressWarnings("unused")
        public static final Parcelable.Creator<SavedState> CREATOR = new Creator<SavedState>(){

            public SavedState createFromParcel(Parcel in){
                return new SavedState(in);
            }

            public SavedState[] newArray(int size){
                return new SavedState[size];
            }
        };
        private String mSerializedPattern;
        private int mDisplayMode;
        private boolean mInputEnabled;
        private boolean mInStealthMode;
        private boolean mTactileFeedbackEnabled;

        /**
         * Constructor called from {@link LockPatternView#onSaveInstanceState()}
         */
        private SavedState(Parcelable superState,String serializedPattern,int displayMode,
                boolean inputEnabled,boolean inStealthMode,boolean tactileFeedbackEnabled){
            super(superState);
            mSerializedPattern = serializedPattern;
            mDisplayMode = displayMode;
            mInputEnabled = inputEnabled;
            mInStealthMode = inStealthMode;
            mTactileFeedbackEnabled = tactileFeedbackEnabled;
        }

        /**
         * Constructor called from {@link #CREATOR}
         */
        private SavedState(Parcel in){
            super(in);
            mSerializedPattern = in.readString();
            mDisplayMode = in.readInt();
            mInputEnabled = (Boolean)in.readValue(null);
            mInStealthMode = (Boolean)in.readValue(null);
            mTactileFeedbackEnabled = (Boolean)in.readValue(null);
        }

        public String getSerializedPattern(){
            return mSerializedPattern;
        }

        public int getDisplayMode(){
            return mDisplayMode;
        }

        public boolean isInputEnabled(){
            return mInputEnabled;
        }

        public boolean isInStealthMode(){
            return mInStealthMode;
        }

        public boolean isTactileFeedbackEnabled(){
            return mTactileFeedbackEnabled;
        }

        @Override
        public void writeToParcel(Parcel dest,int flags){
            super.writeToParcel(dest,flags);
            dest.writeString(mSerializedPattern);
            dest.writeInt(mDisplayMode);
            dest.writeValue(mInputEnabled);
            dest.writeValue(mInStealthMode);
            dest.writeValue(mTactileFeedbackEnabled);
        }
    }
}