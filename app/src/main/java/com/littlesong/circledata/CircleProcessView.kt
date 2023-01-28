package com.littlesong.circledata

import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.graphics.*
import android.text.TextPaint
import android.util.AttributeSet
import android.util.Log
import android.view.View
import androidx.core.graphics.TypefaceCompatUtil
import java.text.DecimalFormat

/**
 * @Author      : 陈松
 * @Email       : song.chen-a1872@aqara.com
 * @Date        : 2023-01-12 21:39.
 * @Description :
 */
class CircleProcessView(context: Context?, attributeSet: AttributeSet?) :
    View(context, attributeSet) {
    companion object {
        const val TAG = "CircleProcessView"
    }

    var currentValue: String = ""

    // 是否开启抗锯齿
    private var antiAlias: Boolean = true

    //圆心位置
    private lateinit var centerPosition: Point

    //半径
    private var raduis: Float? = null

    //声明边界矩形
    private var mRectF: RectF? = null

    //声明背景圆画笔
    private lateinit var mBgCirPaint: Paint //画笔
    private var mBgCirColor: Int? = null //颜色
    private var mBgCirWidth: Float = 15f //圆环背景宽度

    //声明进度圆的画笔
    private lateinit var mCirPaint: Paint //画笔
    private var mCirColor: Int? = null //颜色
    private var mCirWidth: Float = 15f //主圆的宽度

    //绘制的起始角度和滑过角度(默认从顶部开始绘制，绘制360度)
    private var mStartAngle: Float = 270f
    private var mSweepAngle: Float = 360f

    //动画时间（默认一秒）
    private var mAnimTime: Int = 1000

    //属性动画
    private var mAnimator: ValueAnimator? = null

    //动画进度
    private var mAnimPercent: Float = 0f

    //进度值
    private var mValue: String? = null

    //最大值(默认为100)
    private var mMaxValue: Float = 100f

    //绘制数值
    private lateinit var mValuePaint: TextPaint
    private var mValueSize: Float? = null
    private var mValueColor: Int? = null

    //绘制进度的后缀-默认为百分号%
    private var mUnit: CharSequence? = "%"

    //绘制描述
    private var mHint: CharSequence? = null
    private lateinit var mHintPaint: TextPaint
    private var mHintSize: Float? = null
    private var mHintColor: Int? = null

    //颜色渐变色
    private var isGradient: Boolean? = null
    private var mGradientColors: IntArray? = intArrayOf(Color.RED, Color.GRAY, Color.BLUE)
    private var mGradientColor: Int? = null
    private var mSweepGradient: SweepGradient? = null

    //阴影
    private var mShadowColor: Int? = null
    private var mShadowSize: Float? = null
    private var mShadowIsShow: Boolean = false

    //保留的小数位数(默认2位)
    private var mDigit: Int = 2

    //是否需要动画(默认需要动画)
    private var isAnim: Boolean = true
    init{
        Log.d(Companion.TAG,"init")
        mAnimPercent = 0f
        centerPosition = Point()
        mRectF = RectF()
        mAnimator = ValueAnimator()
        initAttrs(attributeSet, context)
        initPaint()
    }
    @SuppressLint("Recycle", "CustomViewStyleable")
    private fun initAttrs(attributeSet: AttributeSet?, context: Context?) {
        // 获取状态属性
        val typedArray =
            context!!.obtainStyledAttributes(attributeSet, R.styleable.MyCircleProgressView)
        isAnim = typedArray.getBoolean(R.styleable.MyCircleProgressView_isanim, true)
        mDigit = typedArray.getInt(R.styleable.MyCircleProgressView_digit, 2)
        mBgCirColor = typedArray.getColor(R.styleable.MyCircleProgressView_mBgCirColor, Color.GRAY)
        mBgCirWidth = typedArray.getDimension(R.styleable.MyCircleProgressView_mBgCirWidth, 15f)
        mCirColor = typedArray.getColor(R.styleable.MyCircleProgressView_mCirColor, Color.YELLOW)
        mCirWidth = typedArray.getDimension(R.styleable.MyCircleProgressView_mCirWidth, 15f)
        mAnimTime = typedArray.getInt(R.styleable.MyCircleProgressView_animTime, 1000)
        mValue = typedArray.getString(R.styleable.MyCircleProgressView_value)
        mMaxValue = typedArray.getFloat(R.styleable.MyCircleProgressView_maxvalue, 100f)
        mStartAngle = typedArray.getFloat(R.styleable.MyCircleProgressView_startAngle, 270f)
        mSweepAngle = typedArray.getFloat(R.styleable.MyCircleProgressView_sweepAngle, 360f)
        mValueSize = typedArray.getDimension(R.styleable.MyCircleProgressView_valueSize, 15f)
        mValueColor = typedArray.getColor(R.styleable.MyCircleProgressView_valueColor, Color.BLACK)
        mHint = typedArray.getString(R.styleable.MyCircleProgressView_hint)
        mHintSize = typedArray.getDimension(R.styleable.MyCircleProgressView_hintSize, 15f)
        mHintColor = typedArray.getColor(R.styleable.MyCircleProgressView_hintColor, Color.GRAY)
        mUnit = typedArray.getString(R.styleable.MyCircleProgressView_unit)
        mShadowColor =
            typedArray.getColor(R.styleable.MyCircleProgressView_shadowColor, Color.BLACK)
        mShadowIsShow = typedArray.getBoolean(R.styleable.MyCircleProgressView_shadowShow, false)
        mShadowSize = typedArray.getFloat(R.styleable.MyCircleProgressView_shadowSize, 8f)
        isGradient = typedArray.getBoolean(R.styleable.MyCircleProgressView_isGradient, false)
        mGradientColor = typedArray.getResourceId(R.styleable.MyCircleProgressView_gradient, 0)
        if (mGradientColor != 0) {
            mGradientColors = resources.getIntArray(mGradientColor!!)
        }

        typedArray.recycle()

    }

    // 初始化画笔
    private fun initPaint() {
        /**
         *  四个地方，设置四种画笔
         *  主圆
         *  背景圆
         *  主题文字d
         *  提示文字
         */
        mCirPaint = Paint()
        mCirPaint.isAntiAlias = antiAlias
        mCirPaint.style = Paint.Style.STROKE
        mCirPaint.strokeWidth = mCirWidth
        mCirPaint.strokeCap = Paint.Cap.ROUND // 笔刷样式
        mCirPaint.color = mCirColor!!

        //背景色画笔
        mBgCirPaint = Paint()
        mBgCirPaint.isAntiAlias = antiAlias
        mBgCirPaint.style = Paint.Style.STROKE
        mBgCirPaint.strokeWidth = mBgCirWidth
        mBgCirPaint.strokeCap = Paint.Cap.ROUND

        mValuePaint = TextPaint()
        mValuePaint.isAntiAlias = antiAlias
        mValuePaint.textSize = mValueSize!!
        mValuePaint.color = mValueColor!!
        mValuePaint.textAlign = Paint.Align.CENTER // 从中间向两边绘制，不需要重新计算
        mValuePaint.typeface = Typeface.DEFAULT_BOLD  // 字体加粗

        mHintPaint = TextPaint()
        mHintPaint.isAntiAlias = antiAlias
        mHintPaint.textSize = mHintSize!!
        mHintPaint.color = mHintColor!!
        mHintPaint.textAlign = Paint.Align.CENTER


    }

    // size 变化时调用 第一次添加时也会调用，此时oldw = 0
    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        Log.d(TAG, "onSizeChanged  w = $w, h = $h, oldw = $oldw, oldh = $oldh")
        centerPosition.x = w / 2
        centerPosition.y = h / 2

        val maxCirWidth = Math.max(mCirWidth, mBgCirWidth)
        val minWidth = Math.min(
            w - paddingLeft - paddingRight - 2 * maxCirWidth,
            h - paddingBottom - paddingTop - 2 * maxCirWidth
        )
        raduis = minWidth / 2  // 半径
        mRectF!!.left = centerPosition.x - raduis!! - maxCirWidth / 2
        mRectF!!.top = centerPosition.y - raduis!! - maxCirWidth / 2
        mRectF!!.right = centerPosition.x + raduis!! + maxCirWidth / 2
        mRectF!!.bottom = centerPosition.y + raduis!! + maxCirWidth / 2

        if (isGradient == true) {
            setupGradientCircle() // 设置圆环画笔颜色渐变
        }


    }

    private fun setupGradientCircle() {
        mSweepGradient = SweepGradient(
            centerPosition.x.toFloat(),
            centerPosition.y.toFloat(),
            mGradientColors!!,
            null
        )
        mCirPaint.shader = mSweepGradient
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)
        Log.d(Companion.TAG, "onLayout")

    }
    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        Log.d(Companion.TAG, "onMeasure")
    }
    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        Log.d(Companion.TAG, "onDraw")
        drawText(canvas)
        drawCircle(canvas)
    }

    private fun drawCircle(canvas: Canvas?) {
        canvas?.save()
        if (mShadowIsShow) {
            mCirPaint.setShadowLayer(mShadowSize!!, 0f, 0f,mShadowColor!!)// 设置阴影
        }

        canvas?.drawArc(mRectF!!, mStartAngle, mSweepAngle, false, mBgCirPaint) // 绘制背景圆 todo

        canvas?.drawArc(mRectF!!, mStartAngle, mSweepAngle * mAnimPercent, false, mCirPaint)// 绘制主圆
        canvas?.restore()
    }

    private fun drawText(canvas: Canvas?) {
        canvas?.drawText(
            mValue + mUnit,
            centerPosition.x.toFloat(),
            centerPosition.y.toFloat(),
            mValuePaint
        )
        if (mHint != null || mHint != "") {
            canvas?.drawText(
                mHint.toString(),
                centerPosition.x.toFloat(),
                centerPosition.y.toFloat() - mHintPaint.ascent() + 15,// todo
                mHintPaint
            )
        }

    }

    /**
     * 设置当前需要展示的值
     */
    fun setValue(value: String, maxValue: Float): CircleProcessView {
        currentValue = value
        if (isNum(value)) {

            mValue = value
            mMaxValue = maxValue

            //当前的进度和最大的进度，去做动画的绘制
            val start = mAnimPercent
            val end = value.toFloat() / maxValue
            startAnim(start, end, mAnimTime)

        } else {
            mValue = value
        }
        return this
    }

    //判断当前的值是否是数字类型
    private fun isNum(str: String): Boolean {
        try {
            val toDouble = str.toDouble()
        } catch (e: Exception) {
            return false
        }
        return true
    }
    private fun startAnim(start: Float, end: Float, animTime: Int) {
        mAnimator = ValueAnimator.ofFloat(start, end)
        mAnimator?.duration = animTime.toLong()
        mAnimator?.addUpdateListener {
            mAnimPercent = it.animatedValue as Float  // 得到当前的动画进度
            mValue = if (isAnim) {
                roundByScale((mAnimPercent * mMaxValue).toDouble(), mDigit)
            } else {
                roundByScale(mValue?.toDouble(), mDigit)
            }
            // 不停重绘当前值，表现出动画效果
            invalidate()

        }
        mAnimator?.start()
    }

    private fun roundByScale(v: Double?, scale: Int): String? {
        if (scale < 0) {
            throw IllegalArgumentException("参数错误，必须设置大于0的数字")
        }
        if (scale == 0) {
            return DecimalFormat("0").format(v)
        }
        var formatStr = "0."

        for (i in 0 until scale) {
            formatStr += "0"
        }
        return DecimalFormat(formatStr).format(v);

    }

}