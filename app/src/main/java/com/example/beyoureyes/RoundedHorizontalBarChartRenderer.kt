package com.example.beyoureyes

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.graphics.RectF
import android.util.Log
import com.github.mikephil.charting.animation.ChartAnimator
import com.github.mikephil.charting.interfaces.dataprovider.BarDataProvider
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet
import com.github.mikephil.charting.renderer.HorizontalBarChartRenderer
import com.github.mikephil.charting.utils.Utils
import com.github.mikephil.charting.utils.ViewPortHandler

class RoundedHorizontalBarChartRenderer(
    chart: BarDataProvider?,
    animator: ChartAnimator?,
    viewPortHandler: ViewPortHandler?)
    : HorizontalBarChartRenderer(chart, animator, viewPortHandler) {
    private var mRightRadius = 5f
    private var mLeftRadius = 5f

    fun setRightRadius(mRightRadius: Float) {
        this.mRightRadius = mRightRadius
    }

    fun setLeftRadius(mLeftRadius: Float) {
        this.mLeftRadius = mLeftRadius
    }

    override fun drawDataSet(c: Canvas, dataSet: IBarDataSet, index: Int) {
        if (mChart.barData.dataSetCount == 1 && mChart.barData.entryCount == 1 && mRightRadius > 0) {

            val barValue = mChart.barData.dataSets[0]
            // if (barValue.getEntryForIndex(0).y < 20f) val a = 0

            val mBarShadowRectBuffer = RectF()
            val trans = mChart.getTransformer(dataSet.axisDependency)

            mBarBorderPaint.color = dataSet.barBorderColor
            mBarBorderPaint.strokeWidth = Utils.convertDpToPixel(dataSet.barBorderWidth)

            val drawBorder = dataSet.barBorderWidth > 0f

            val phaseX = mAnimator.phaseX
            val phaseY = mAnimator.phaseY

            // draw the bar shadow before the values
            if (mChart.isDrawBarShadowEnabled) {
                mShadowPaint.color = dataSet.barShadowColor
                val barData = mChart.barData
                val barWidth = barData.barWidth
                val barWidthHalf = barWidth / 2.0f
                var x: Float
                val e = dataSet.getEntryForIndex(0)
                x = e.x
                mBarShadowRectBuffer.top = x - barWidthHalf
                mBarShadowRectBuffer.bottom = x + barWidthHalf
                trans.rectValueToPixel(mBarShadowRectBuffer)
                if (mViewPortHandler.isInBoundsTop(mBarShadowRectBuffer.bottom)) {
                    if (mViewPortHandler.isInBoundsBottom(mBarShadowRectBuffer.top)){
                        mBarShadowRectBuffer.left = mViewPortHandler.contentLeft()
                        mBarShadowRectBuffer.right = mViewPortHandler.contentRight()
                        c.drawRoundRect(mBarShadowRectBuffer, mRightRadius, mRightRadius, mShadowPaint)
                    }
                }
            }

            // initialize the buffer
            val buffer = mBarBuffers[index]
            buffer.setPhases(phaseX, phaseY)
            buffer.setDataSet(index)
            buffer.setInverted(mChart.isInverted(dataSet.axisDependency))
            buffer.setBarWidth(mChart.barData.barWidth)

            buffer.feed(dataSet)

            trans.pointValuesToPixel(buffer.buffer)

            val isSingleColor = dataSet.colors.size == 1

            if (isSingleColor) {
                mRenderPaint.color = dataSet.color
            }

            if (mViewPortHandler.isInBoundsTop(buffer.buffer[3]) &&
                mViewPortHandler.isInBoundsBottom(buffer.buffer[1])){

                if (!isSingleColor) {
                    // Set the color for the currently drawn value. If the index
                    // is out of bounds, reuse colors.
                    mRenderPaint.color = dataSet.getColor(0)
                }

                val barRight = if (barValue.getEntryForIndex(0).y < 20f)
                    0.2f*mViewPortHandler.contentRight() else buffer.buffer[2]

                c.drawRoundRect(
                    RectF(buffer.buffer[0], buffer.buffer[1], barRight,
                        buffer.buffer[3]), mRightRadius, mRightRadius, mRenderPaint
                )

                if (drawBorder) {
                    c.drawRect(
                        buffer.buffer[0], buffer.buffer[1], barRight,
                        buffer.buffer[3], mBarBorderPaint
                    )
                }
            }

        } else {
            super.drawDataSet(c, dataSet, index)
        }
    }


    override fun drawValue(c: Canvas?, valueText: String?, x: Float, y: Float, color: Int) {
        if (mChart.barData.dataSetCount == 1 && mChart.barData.dataSets.size == 1) {
            val yValue = mChart.barData.dataSets[0].getEntryForIndex(0).y
            if (yValue < 20f) {
                super.drawValue(c, valueText, x+100f*(1f-(yValue/20f)), y, color)
            }
            else {
                super.drawValue(c, valueText, x, y, color)
            }
        } else {
            super.drawValue(c, valueText, x, y, color)
        }
    }
}