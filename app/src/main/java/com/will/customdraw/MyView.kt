package com.will.customdraw

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.util.Log
import android.view.View
import androidx.core.content.ContextCompat
import androidx.core.graphics.withRotation
import androidx.core.graphics.withTranslation
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import kotlinx.coroutines.*
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sign
import kotlin.math.sin

const val TAG : String = "MyView"
class MyView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr),LifecycleObserver {


    private var rotatingJob : Job? = null
    private var mRadius = 0f
    private var mWidth = 0f
    private var mHeight = 0f
    private var mAngle = 10f
    private val sineWaveSamplesPath = Path()
    //画一个填充的实心圆的画笔
    private val fillCirclePaint = Paint().apply {
        style = Paint.Style.FILL
        color = ContextCompat.getColor(context,R.color.colorWhite)
        isAntiAlias = true
    }
    //画坐标的画笔
    private val solidLinePaint = Paint().apply {
        style = Paint.Style.STROKE
        strokeWidth =5f
        color = ContextCompat.getColor(context,R.color.colorWhite)
        isAntiAlias = true
    }
    //画文本的画笔
    private val textPaint  = Paint().apply {
        textSize = 50f
        typeface = Typeface.DEFAULT_BOLD
        isAntiAlias = true
        color = ContextCompat.getColor(context,R.color.colorWhite)
    }
    //画圆虚线的画笔
    private val dashedLinePaint = Paint().apply {
        style = Paint.Style.STROKE
        //设置偏移量
        pathEffect = DashPathEffect(floatArrayOf(10f,10f),0f)
        strokeWidth = 5f
        isAntiAlias = true
        color = ContextCompat.getColor(context,R.color.colorYellow)
    }

    //画圆虚线的画笔
    private val vectorLinePaint = Paint().apply {
        style = Paint.Style.STROKE
        strokeWidth = 5f
        isAntiAlias = true
        color = ContextCompat.getColor(context,R.color.colorAccent)
    }



    override fun onFinishInflate() {
        super.onFinishInflate()
        Log.d(TAG, "onFinishInflate: ")
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        Log.d(TAG, "onAttachedToWindow: ")
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        Log.d(TAG, "onMeasure: ")
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        mWidth = w.toFloat()
        mHeight = h.toFloat()
        mRadius = if (w < h /2) w / 2.toFloat() else h /4.toFloat()
        mRadius -=20f
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)
        Log.d(TAG, "onLayout: ")
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        canvas?.apply {
            drawAxises(this)
            drawLabel(this)
            drawDashedCircle(this)
            drawVector(this)
            drawProjections(this)
            drawSineWave(this)
        }
    }



    private fun drawAxises(canvas: Canvas){
        //先把画布移动到中心点，在作画
        //先保存
        canvas.withTranslation(x = mWidth/2, y = mHeight/2) {
            drawLine(- mWidth/2,0f,mWidth/2,0f,solidLinePaint)
            drawLine(0f,- mHeight/2,0f,mHeight/2,solidLinePaint)
        }

        canvas.withTranslation(mWidth/2,mHeight /4 *3){
            drawLine(- mWidth/2,0f,mWidth/2,0f,solidLinePaint)
        }
    }

    private fun drawLabel(canvas: Canvas){
        canvas.apply {
            drawRect(20f,20f,500f,120f,solidLinePaint)
            drawText("指数函数与旋转矢量",35f,90f,textPaint)
        }
    }

    private fun drawDashedCircle(canvas: Canvas){
        canvas.withTranslation (mWidth/2,mHeight /4 *3){
            drawCircle(0f,0f,mRadius,dashedLinePaint)
        }
    }

    private fun drawVector(canvas: Canvas){
        canvas.withTranslation(mWidth/2,mHeight /4 * 3){
            withRotation(-mAngle){
                drawLine(0f,0f,mRadius,0f,vectorLinePaint)
            }
        }
    }

    private fun drawProjections(canvas: Canvas){
        canvas.withTranslation(mWidth/2,mHeight/2){
            drawCircle(mRadius * cos(mAngle.toRadians()),0f,20f,fillCirclePaint)
        }

        canvas.withTranslation(mWidth/2,mHeight/ 4 * 3){
            drawCircle(mRadius * cos(mAngle.toRadians()),0f,20f,fillCirclePaint)
        }

        canvas.withTranslation(mWidth / 2,mHeight /4 * 3){
            val x = mRadius * cos(mAngle.toRadians())
            val y = mRadius * sin(mAngle.toRadians())

            withTranslation(x,-y){
                drawLine(0f,0f,0f,y,solidLinePaint)
                drawLine(0f,0f,0f,-mHeight / 4 + y,dashedLinePaint)
            }
        }
    }

    private fun drawSineWave(canvas: Canvas){
        canvas.withTranslation(mWidth/2,mHeight/2){
            val simplesCount = 100
            val dy = mHeight /2 /simplesCount
            //每次画之前重置
            sineWaveSamplesPath.reset()
            sineWaveSamplesPath.moveTo(mRadius * cos(mAngle.toRadians()),0f)
            //重复
            repeat(simplesCount){
                val x = mRadius * cos (it * -0.15 + mAngle.toRadians()).toFloat()
                val y = -dy * it
                //沿着这条路径添加一条贝塞尔曲线,起点，终点
                sineWaveSamplesPath.quadTo(x,y,x,y)
            }
            drawPath(sineWaveSamplesPath,vectorLinePaint)
            drawTextOnPath("贝塞尔曲线",sineWaveSamplesPath,1000f,-20f,textPaint)
        }
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
    fun startRotating(){
        rotatingJob = CoroutineScope(Dispatchers.Main).launch {
            while (true){
                delay(100)
                mAngle +=5f
                invalidate()
            }
        }
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_PAUSE)
    fun pauseRotating(){
        rotatingJob?.cancel()
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        Log.d(TAG, "onDetachedFromWindow: ")
    }

    /**
     * 扩展函数，角度转换成弧度
     */
    private fun  Float.toRadians() = this / 180 * PI.toFloat()
}