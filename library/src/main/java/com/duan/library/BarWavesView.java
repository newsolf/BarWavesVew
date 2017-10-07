package com.duan.library;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Shader;
import android.support.annotation.ColorInt;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;

import java.util.Arrays;

/**
 * Created by DuanJiaNing on 2017/9/24.
 * 当为控件指定宽和高时，mWaveInterval 将由控件计算
 * 不支持 paddding
 */

public class BarWavesView extends View {

    /**
     * 横条颜色
     */
    private int mBarColor;

    /**
     * 横条高度
     */
    private int mBarHeight;

    /**
     * 波浪条最小高度
     */
    private int mWaveMinHeight;

    /**
     * 波浪条极差（最高与最低的差值）
     */
    private int mWaveRange;

    /**
     * 波浪条宽度
     */
    private int mWaveWidth;

    /**
     * 波浪条数量
     */
    private int mWaveNumber;

    /**
     * 波浪条间隔
     */
    private int mWaveInterval;

    /**
     * 波浪条落下时是否使用动画
     */
    private boolean fallAnimEnable = true;

    private final Paint mPaint = new Paint();

    private int[][] mWaveColors;
    private float[] mWaveHeight;
    private ValueAnimator mAnim;

    private final static int sDEFAULT_BAR_COLOR = Color.LTGRAY;
    private final static int sDEFAULT_WAVE_COLOR = Color.YELLOW;

    /**
     * xml 中指定的值小于以下值时无效
     */
    private final static int sMIN_WAVE_NUMBER = 13;
    private final static int sMIN_BAR_HEIGHT = 0;
    private final static int sMIN_WAVE_HEIGHT = 0;
    private final static int sMIN_WAVE_RANGE = 10;
    private final static int sMIN_WAVE_INTERVAL = 0;
    private final static int sMIN_WAVE_WIDTH = 5;
    // 控件的宽度由波浪条数量、宽度、间距共同决定
    private final static int sMIN_WIDTH = sMIN_WAVE_NUMBER * sMIN_WAVE_WIDTH + (sMIN_WAVE_NUMBER - 1) * sMIN_WAVE_INTERVAL;
    private final static int sMIN_HEIGHT = sMIN_WAVE_HEIGHT + sMIN_WAVE_RANGE + sMIN_BAR_HEIGHT;

    public BarWavesView(Context context) {
        super(context);
        mPaint.setAntiAlias(true);
        mBarColor = sDEFAULT_BAR_COLOR;
        setWaveColors(sMIN_WAVE_NUMBER, sDEFAULT_WAVE_COLOR);
        setWaveHeights(0);
        initAnim();
    }

    private void initAnim() {
        mAnim = ObjectAnimator.ofFloat(1.0f, 0.0f);
        mAnim.setInterpolator(new AccelerateDecelerateInterpolator());
        mAnim.setDuration(1300);
        mAnim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float v = (float) animation.getAnimatedValue();
                for (int i = 0; i < mWaveHeight.length; i++) {
                    mWaveHeight[i] *= v;
                }
                invalidate();
            }
        });
    }

    public BarWavesView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public BarWavesView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        mPaint.setAntiAlias(true);

        final TypedArray array = context.getTheme().obtainStyledAttributes(attrs, R.styleable.BarWavesView, defStyleAttr, 0);

        int tempWaveColor = array.getColor(R.styleable.BarWavesView_waveColor, sDEFAULT_WAVE_COLOR);
        mBarColor = array.getColor(R.styleable.BarWavesView_barColor, sDEFAULT_BAR_COLOR);

        mBarHeight = array.getDimensionPixelSize(R.styleable.BarWavesView_barHeight, 20);
        mBarHeight = mBarHeight < sMIN_BAR_HEIGHT ? sMIN_BAR_HEIGHT : mBarHeight;

        mWaveRange = array.getDimensionPixelSize(R.styleable.BarWavesView_waveRange, 30);
        mWaveRange = mWaveRange < sMIN_WAVE_RANGE ? sMIN_WAVE_RANGE : mWaveRange;

        mWaveMinHeight = array.getDimensionPixelSize(R.styleable.BarWavesView_waveMinHeight, 10);
        mWaveMinHeight = mWaveMinHeight < sMIN_WAVE_HEIGHT ? sMIN_WAVE_HEIGHT : mWaveMinHeight;

        mWaveWidth = array.getDimensionPixelSize(R.styleable.BarWavesView_waveWidth, 10);
        mWaveWidth = mWaveWidth < sMIN_WAVE_WIDTH ? sMIN_WAVE_WIDTH : mWaveWidth;

        mWaveInterval = array.getDimensionPixelSize(R.styleable.BarWavesView_waveInterval, 8);
        mWaveInterval = mWaveInterval < sMIN_WAVE_INTERVAL ? sMIN_WAVE_INTERVAL : mWaveInterval;

        mWaveNumber = array.getInteger(R.styleable.BarWavesView_waveNumber, sMIN_WAVE_NUMBER);
        mWaveNumber = mWaveNumber < sMIN_WAVE_NUMBER ? sMIN_WAVE_NUMBER : mWaveNumber;

        //释放资源
        array.recycle();

        setWaveColors(mWaveNumber, tempWaveColor);
        setWaveHeights(0);
        initAnim();

    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);
        int width;
        int height;

        if (widthMode == MeasureSpec.EXACTLY) {
            width = widthSize < sMIN_WIDTH ? sMIN_WIDTH : widthSize;
            // 手动计算波浪条间距，即宽为指定长度时，波浪条间距自动计算（xml 中指定将失效）
            adjustWidth(width);
        } else {//xml中宽度设为warp_content
            width = mWaveWidth * mWaveNumber + mWaveInterval * (mWaveNumber - 1);
        }

        if (heightMode == MeasureSpec.EXACTLY) {
            height = heightSize < sMIN_HEIGHT ? sMIN_HEIGHT : heightSize;
            adjustHeight(height);
        } else {
            height = mWaveMinHeight + mWaveRange + mBarHeight;
        }

        setMeasuredDimension(width, height);

    }

    private void adjustWidth(int width) {

        while (width < mWaveInterval * (mWaveNumber - 1) + mWaveWidth * mWaveNumber) {
            if (mWaveInterval > sMIN_WAVE_INTERVAL) {
                mWaveInterval--; // 首选调整波浪条间距
            } else {
                if (mWaveWidth > sMIN_WAVE_WIDTH) {
                    mWaveWidth--; // 其次选择调整波浪条宽度
                } else {
                    width++; // 万不得已选择调整设置的宽度
                }
            }
        }

    }

    private void adjustHeight(int height) {

        while (mWaveMinHeight + mWaveRange + mBarHeight > height) {
            if (mBarHeight > sMIN_BAR_HEIGHT) {
                mBarHeight--;
                continue;
            }

            if (mWaveMinHeight > sMIN_WAVE_HEIGHT) {
                mWaveMinHeight--;
                continue;
            }

            if (mWaveRange > sMIN_WAVE_RANGE) {
                mWaveRange--;
            }
        }
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
    }

    @Override
    protected void onDraw(Canvas canvas) {

        drawBar(canvas);

        drawWaves(canvas);

    }

    private void drawWaves(Canvas canvas) {

        for (int i = 0; i < mWaveNumber; i++) {
            float left = mWaveWidth * i + mWaveInterval * i;
            float right = left + mWaveWidth;

            float bottom = getHeight() - mBarHeight;
            float fs = mWaveHeight[i];
            float top;
            top = bottom - mWaveMinHeight - (fs * mWaveRange);
            LinearGradient lg = new LinearGradient(
                    left, bottom,
                    right, top,
                    mWaveColors[i],
                    null,
                    Shader.TileMode.CLAMP
            );
            mPaint.setAlpha(255);
            mPaint.setShader(lg);
            canvas.drawRect(left, top, right, bottom, mPaint);
        }

    }

    private void drawBar(Canvas canvas) {
        mPaint.setShader(null);
        mPaint.setAlpha(255);
        mPaint.setColor(mBarColor);
        float right = mWaveInterval * (mWaveNumber - 1) + mWaveWidth * mWaveNumber;
        float top = getHeight() - mBarHeight;
        canvas.drawRect(0, top, right, getHeight(), mPaint);
    }

    /**
     * 设置横条颜色
     *
     * @param color 颜色
     */
    public void setBarColor(@ColorInt int color) {
        this.mBarColor = color;
        invalidate();
    }

    /**
     * 统一设置所有波浪条的颜色
     *
     * @param color 颜色
     */
    public void setWaveColor(@ColorInt int color) {
        setWaveColors(mWaveNumber, color);
        invalidate();
    }

    /**
     * 设置每一个波浪条的渐变颜色
     *
     * @param color 颜色
     */
    public void setWaveColor(int[][] color) {
        if (color == null || color.length < mWaveNumber || color[0].length < 2) {
            return;
        }
        setWaveColors(mWaveNumber, color);
        invalidate();
    }

    /**
     * 设置每一个波浪条的纯颜色
     *
     * @param color 颜色
     */
    public void setWaveColor(int[] color) {
        if (color == null || color.length < mWaveNumber) {
            return;
        }
        int[][] cs = new int[color.length][2];
        for (int i = 0; i < cs.length; i++) {
            cs[i][0] = color[i];
            cs[i][1] = color[i];
        }
        setWaveColors(cs.length, cs);
        invalidate();

    }

    /**
     * 改变波浪条的高度
     *
     * @param hs 数值介于 0.0 - 1.0 的浮点数组，当值为 1.0 时波浪条将完全绘制（最高），0.0 时波浪条只绘制最低高度（最低）。
     */
    public void setWaveHeight(float[] hs) {
        if (fallAnimEnable && (mAnim.isStarted() || mAnim.isRunning())) {
            mAnim.cancel();
        }

        setWaveHeights(hs);
        invalidate();

        if (fallAnimEnable) {
            mAnim.start();
        }
    }

    public void setFallAnimEnable(boolean enable) {
        this.fallAnimEnable = enable;
    }

    private void setWaveHeights(float[] hs) {
        if (hs == null || hs.length != mWaveNumber) {
            return;
        }
        mWaveHeight = hs;
    }

    private void setWaveHeights(float h) {
        if (h > 1 || h < 0) {
            return;
        }
        mWaveHeight = new float[mWaveNumber];
        Arrays.fill(mWaveHeight, h);
    }

    // len 不能小于 mWaveNumber  数组第二维长度不能小于 2
    private void setWaveColors(int len, int[][] color) {
        mWaveColors = new int[len][color[0].length];
        for (int i = 0; i < mWaveColors.length; i++) {
            for (int j = 0; j < mWaveColors[i].length; j++) {
                mWaveColors[i][j] = color[i][j];
            }
        }
    }

    // len 不能小于 mWaveNumber
    private void setWaveColors(int len, int color) {
        mWaveColors = new int[len][2];
        for (int i = 0; i < mWaveColors.length; i++) {
            mWaveColors[i][0] = color;
            mWaveColors[i][1] = color;
        }
    }

    public int getBarColor() {
        return mBarColor;
    }

    public int getBarHeight() {
        return mBarHeight;
    }

    public int getWaveMinHeight() {
        return mWaveMinHeight;
    }

    public int getWaveMaxHeight() {
        return mWaveMinHeight + mWaveRange;
    }

    public int getWaveWidth() {
        return mWaveWidth;
    }

    public int getWaveNumber() {
        return mWaveNumber;
    }

    public float[] getWaveHeight() {
        return mWaveHeight;
    }
}
