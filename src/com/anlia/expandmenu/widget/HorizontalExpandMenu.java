package com.anlia.expandmenu.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.drawable.GradientDrawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.Transformation;
import android.widget.RelativeLayout;
import cn.creable.surveyOnUCMap.R;

import com.anlia.expandmenu.utils.DpOrPxUtils;
/**
 * Created by anlia on 2017/11/9.
 */

public class HorizontalExpandMenu extends RelativeLayout {

    private Context mContext;
    private AttributeSet mAttrs;

    private Path path;
    private Paint buttonIconPaint;//��ťicon����
    private ExpandMenuAnim anim;

    private int defaultWidth;//Ĭ�Ͽ��
    private int defaultHeight;//Ĭ�ϳ���
    private int viewWidth;
    private int viewHeight;
    private float backPathWidth;//������View������
    private float maxBackPathWidth;//������View���������
    private int menuLeft;//menu����leftֵ
    private int menuRight;//menu����rightֵ

    private int menuBackColor;//�˵�������ɫ
    private float menuStrokeSize;//�˵����߿��ߵ�size
    private int menuStrokeColor;//�˵����߿��ߵ���ɫ
    private float menuCornerRadius;//�˵���Բ�ǰ뾶

    private float buttonIconDegrees;//��ťicon�������ߵ���ת�Ƕ�
    private float buttonIconSize;//��ťicon���ŵĴ�С
    private float buttonIconStrokeWidth;//��ťicon���ŵĴ�ϸ
    private int buttonIconColor;//��ťicon��ɫ

    private int buttonStyle;//��ť����
    private int buttonRadius;//��ť����������Բ�뾶
    private float buttonTop;//��ť��������topֵ
    private float buttonBottom;//��ť��������bottomֵ

    private Point rightButtonCenter;//�Ұ�ť�е�
    private float rightButtonLeft;//�Ұ�ť��������leftֵ
    private float rightButtonRight;//�Ұ�ť��������rightֵ

    private Point leftButtonCenter;//��ť�е�
    private float leftButtonLeft;//��ť��������leftֵ
    private float leftButtonRight;//��ť��������rightֵ

    private boolean isFirstLayout;//�Ƿ��һ�β���λ�ã���Ҫ���ڳ�ʼ��menuLeft��menuRight��ֵ
    private boolean isExpand;//�˵��Ƿ�չ����Ĭ��Ϊչ��
    private boolean isAnimEnd;//�����Ƿ����
    private float downX = -1;
    private float downY = -1;
    private int expandAnimTime;//չ������˵��Ķ���ʱ��

    private View childView;

    /**
     * ����ť����λ�ã�Ĭ��Ϊ�ұ�
     */
    public static final int Right = 0;
    public static final int Left = 1;

    public HorizontalExpandMenu(Context context) {
        super(context);
        this.mContext = context;
        init();
    }

    public HorizontalExpandMenu(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.mContext = context;
        this.mAttrs = attrs;
        init();
    }

    private void init(){
        TypedArray typedArray = mContext.obtainStyledAttributes(mAttrs, R.styleable.HorizontalExpandMenu);
        defaultWidth = DpOrPxUtils.dip2px(mContext,200);
        defaultHeight = DpOrPxUtils.dip2px(mContext,40);

        menuBackColor = typedArray.getColor(R.styleable.HorizontalExpandMenu_back_color,Color.WHITE);
        menuStrokeSize = typedArray.getDimension(R.styleable.HorizontalExpandMenu_stroke_size,1);
        menuStrokeColor = typedArray.getColor(R.styleable.HorizontalExpandMenu_stroke_color,Color.GRAY);
        menuCornerRadius = typedArray.getDimension(R.styleable.HorizontalExpandMenu_corner_radius,DpOrPxUtils.dip2px(mContext,20));

        buttonStyle = typedArray.getInteger(R.styleable.HorizontalExpandMenu_button_style,Right);
        buttonIconDegrees = 90;
        buttonIconSize = typedArray.getDimension(R.styleable.HorizontalExpandMenu_button_icon_size,DpOrPxUtils.dip2px(mContext,8));
        buttonIconStrokeWidth = typedArray.getDimension(R.styleable.HorizontalExpandMenu_button_icon_stroke_width,8);
        buttonIconColor = typedArray.getColor(R.styleable.HorizontalExpandMenu_button_icon_color,Color.GRAY);

        expandAnimTime = typedArray.getInteger(R.styleable.HorizontalExpandMenu_expand_time,400);
        typedArray.recycle();

        isFirstLayout = true;
        isExpand = true;
        isAnimEnd = false;

        buttonIconPaint = new Paint();
        buttonIconPaint.setColor(buttonIconColor);
        buttonIconPaint.setStyle(Paint.Style.STROKE);
        buttonIconPaint.setStrokeWidth(buttonIconStrokeWidth);
        buttonIconPaint.setAntiAlias(true);

        path = new Path();
        leftButtonCenter = new Point();
        rightButtonCenter = new Point();
        anim = new ExpandMenuAnim();
        anim.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {}

            @Override
            public void onAnimationEnd(Animation animation) {
                isAnimEnd = true;
            }

            @Override
            public void onAnimationRepeat(Animation animation) {}
        });
        
        expandMenu(expandAnimTime);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int height = measureSize(defaultHeight, heightMeasureSpec);
        int width = measureSize(defaultWidth, widthMeasureSpec);
        viewHeight = height;
        viewWidth = width;
        buttonRadius = viewHeight/2;
        layoutRootButton();
        setMeasuredDimension(viewWidth,viewHeight);

        maxBackPathWidth = viewWidth- buttonRadius *2;
        backPathWidth = maxBackPathWidth;

        //���ִ��������û������background�������ڴ˴����һ������
        if(getBackground()==null){
            setMenuBackground();
        }
        
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
        //�����View����Ϊ0ʱ��onLayout��getLeft()��getRight()���ܻ�ȡ��Ӧ��ֵ��menuLeft��menuRight����menu��ʼ��left��rightֵ
        if(isFirstLayout){
            menuLeft = getLeft();
            menuRight = getRight();
            isFirstLayout = false;
        }
        if(getChildCount()>0){
            childView = getChildAt(0);
            if(isExpand){
                if(buttonStyle == Right){
                    childView.layout(leftButtonCenter.x,(int) buttonTop,(int) rightButtonLeft,(int) buttonBottom);
                }else {
                    childView.layout((int)(leftButtonRight),(int) buttonTop,rightButtonCenter.x,(int) buttonBottom);
                }

                //������View�ڲ˵��ڣ�LayoutParam���ͺ͵�ǰViewGroupһ��
                RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(viewWidth,viewHeight);
                layoutParams.setMargins(0,0,buttonRadius *3,0);
                childView.setLayoutParams(layoutParams);
            }else {
                childView.setVisibility(GONE);
            }
        }
        if(getChildCount()>1){//����ֱ����View������
            throw new IllegalStateException("HorizontalExpandMenu can host only one direct child");
        }
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        viewWidth = w;//��menu�Ŀ�ȸı�ʱ�����¸�viewWidth��ֵ
        if(isAnimEnd){//��ֹ���ֶ���������˵���λ�ô�С���������bug
            if(buttonStyle == Right){
                if(!isExpand){
//                    layout((int)(menuRight - buttonRadius *2-backPathWidth),getTop(), menuRight,getBottom());
                    layout((menuRight - buttonRadius *2),getTop(), menuRight,getBottom());
                }
            }else {
                if(!isExpand){
//                    layout(menuLeft,getTop(),(int)(menuLeft + buttonRadius *2+backPathWidth),getBottom());
                    layout(menuLeft,getTop(),(menuLeft + buttonRadius *2),getBottom());
                }
            }
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        layoutRootButton();
        if(buttonStyle == Right){
            drawRightIcon(canvas);
        }else {
            drawLeftIcon(canvas);
        }

        super.onDraw(canvas);//ע�⸸�����������ã�����icon���ڸ�
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        super.onTouchEvent(event);
        float x = event.getX();
        float y = event.getY();
        switch (event.getAction()){
            case MotionEvent.ACTION_DOWN:
                downX = event.getX();
                downY = event.getY();
                break;
            case MotionEvent.ACTION_MOVE:
                break;
            case MotionEvent.ACTION_UP:
                if(backPathWidth==maxBackPathWidth || backPathWidth==0){//��������ʱ��ť����Ч
                    switch (buttonStyle){
                        case Right:
                        	//TODO:�����ж������⣬�����׵���
                            //if(x==downX&&y==downY&&y>=buttonTop&&y<=buttonBottom&&x>=rightButtonLeft&&x<=rightButtonRight){
                                expandMenu(expandAnimTime);
                            //}
                            break;
                        case Left:
                            //if(x==downX&&y==downY&&y>=buttonTop&&y<=buttonBottom&&x>=leftButtonLeft&&x<=leftButtonRight){
                                expandMenu(expandAnimTime);
                            //}
                            break;
                    }
                }
                break;
        }
        return true;
    }

    private class ExpandMenuAnim extends Animation {
        public ExpandMenuAnim() {}

        @Override
        protected void applyTransformation(float interpolatedTime, Transformation t) {
            super.applyTransformation(interpolatedTime, t);
            float left = menuRight - buttonRadius *2;//��ť���ұߣ��˵�����ʱ��ť����leftֵ
            float right = menuLeft + buttonRadius *2;//��ť����ߣ��˵�����ʱ��ť����rightֵ
            if(childView!=null) {
                childView.setVisibility(GONE);
            }
            if(isExpand){//�򿪲˵�
                backPathWidth = maxBackPathWidth * interpolatedTime;
                buttonIconDegrees = 90 * interpolatedTime;

                if(backPathWidth==maxBackPathWidth){
                    if(childView!=null) {
                        childView.setVisibility(VISIBLE);
                    }
                }
            }else {//�رղ˵�
                backPathWidth = maxBackPathWidth - maxBackPathWidth * interpolatedTime;
                buttonIconDegrees = 90 - 90 * interpolatedTime;
            }
            if(buttonStyle == Right){
                layout((int)(left-backPathWidth),getTop(), menuRight,getBottom());//�����onLayout���²�����Viewλ��
            }else {
                layout(menuLeft,getTop(),(int)(right+backPathWidth),getBottom());
            }
            postInvalidate();
        }
    }

    private int measureSize(int defaultSize, int measureSpec) {
        int result = defaultSize;
        int specMode = View.MeasureSpec.getMode(measureSpec);
        int specSize = View.MeasureSpec.getSize(measureSpec);

        if (specMode == View.MeasureSpec.EXACTLY) {
            result = specSize;
        } else if (specMode == View.MeasureSpec.AT_MOST) {
            result = Math.min(result, specSize);
        }
        return result;
    }

    /**
     * ���ò˵����������Ҫ��ʾ��Ӱ������onLayout֮ǰ����
     */
    private void setMenuBackground(){
        GradientDrawable gd = new GradientDrawable();
        gd.setColor(menuBackColor);
        gd.setStroke((int)menuStrokeSize, menuStrokeColor);
        gd.setCornerRadius(menuCornerRadius);
        setBackground(gd);
    }

    /**
     * ������ť�е�;���λ��
     */
    private void layoutRootButton(){
        buttonTop = 0;
        buttonBottom = viewHeight;

        rightButtonCenter.x = viewWidth- buttonRadius;
        rightButtonCenter.y = viewHeight/2;
        rightButtonLeft = rightButtonCenter.x- buttonRadius;
        rightButtonRight = rightButtonCenter.x+ buttonRadius;

        leftButtonCenter.x = buttonRadius;
        leftButtonCenter.y = viewHeight/2;
        leftButtonLeft = leftButtonCenter.x- buttonRadius;
        leftButtonRight = leftButtonCenter.x+ buttonRadius;
    }

    /**
     * ������ߵİ�ť
     * @param canvas
     */
    private void drawLeftIcon(Canvas canvas){
        path.reset();
        path.moveTo(leftButtonCenter.x- buttonIconSize, leftButtonCenter.y);
        path.lineTo(leftButtonCenter.x+ buttonIconSize, leftButtonCenter.y);
        canvas.drawPath(path, buttonIconPaint);//������

        canvas.save();
        canvas.rotate(-buttonIconDegrees, leftButtonCenter.x, leftButtonCenter.y);//��ת�����������߿�����Ƕ���ת
        path.reset();
        path.moveTo(leftButtonCenter.x, leftButtonCenter.y- buttonIconSize);
        path.lineTo(leftButtonCenter.x, leftButtonCenter.y+ buttonIconSize);
        canvas.drawPath(path, buttonIconPaint);//������
        canvas.restore();
    }

    /**
     * �����ұߵİ�ť
     * @param canvas
     */
    private void drawRightIcon(Canvas canvas){
        path.reset();
        path.moveTo(rightButtonCenter.x- buttonIconSize, rightButtonCenter.y);
        path.lineTo(rightButtonCenter.x+ buttonIconSize, rightButtonCenter.y);
        canvas.drawPath(path, buttonIconPaint);//������

        canvas.save();
        canvas.rotate(buttonIconDegrees, rightButtonCenter.x, rightButtonCenter.y);//��ת�����������߿�����Ƕ���ת
        path.reset();
        path.moveTo(rightButtonCenter.x, rightButtonCenter.y- buttonIconSize);
        path.lineTo(rightButtonCenter.x, rightButtonCenter.y+ buttonIconSize);
        canvas.drawPath(path, buttonIconPaint);//������
        canvas.restore();
    }

    /**
     * չ������˵�
     * @param time ����ʱ��
     */
    private void expandMenu(int time){
        anim.setDuration(time);
        isExpand = isExpand ?false:true;
        this.startAnimation(anim);
        isAnimEnd = false;
    }
}

