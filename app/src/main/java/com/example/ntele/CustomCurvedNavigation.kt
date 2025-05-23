package com.example.ntele

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.util.TypedValue
import android.view.MotionEvent
import android.view.View
import androidx.core.content.ContextCompat

class CustomCurvedNavigation @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : View(context, attrs) {

    private val itemCount = 3
    private var selectedIndex = 0
    private var onItemSelected: ((Int) -> Unit)? = null

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val path = Path()

    private val icons = listOf(
        R.drawable.home,
        R.drawable.my_vehicle,
        R.drawable.service_icon
    )
    private val labels = listOf("Home", "My Vehicle", "Service")

    // Convert dp to px
    private fun dpToPx(dp: Float): Float =
        TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, resources.displayMetrics)

    // Use dp units for consistent UI
    private val outerCircleRadius = dpToPx(30f)
    private val innerCircleRadius = dpToPx(25f)
    private val curveDepth = dpToPx(20f)
    private val navBarHeight = dpToPx(65f)
    private val iconSize = dpToPx(30f)
    private val textSizeSelected = dpToPx(14f)
    private val textSizeUnselected = dpToPx(12f)
    private val cornerRadius = dpToPx(60f)

    fun setOnItemSelectedListener(callback: (Int) -> Unit) {
        onItemSelected = callback
    }

    fun setSelectedItem(index: Int) {
        if (index in 0 until itemCount && index != selectedIndex) {
            selectedIndex = index
            invalidate()
            onItemSelected?.invoke(index)
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val widthPerItem = width / itemCount.toFloat()
        val selectedCenterX = selectedIndex * widthPerItem + widthPerItem / 2

        val bottomY = height.toFloat()
        val topY = bottomY - navBarHeight

        path.reset()
        val curveStart = selectedCenterX - outerCircleRadius
        val curveEnd = selectedCenterX + outerCircleRadius

        // Start from bottom-left
        path.moveTo(0f, bottomY)

        // Left vertical line
        path.lineTo(0f, topY + cornerRadius)

        // Top-left corner curve
        path.quadTo(0f, topY, cornerRadius, topY)

        // Line to start of curve under selected item
        path.lineTo(curveStart, topY)

        // Floating curve under selected item
        path.cubicTo(
            curveStart + outerCircleRadius / 2, topY,
            selectedCenterX - outerCircleRadius / 2, topY - curveDepth,
            selectedCenterX, topY - curveDepth
        )
        path.cubicTo(
            selectedCenterX + outerCircleRadius / 2, topY - curveDepth,
            curveEnd - outerCircleRadius / 2, topY,
            curveEnd, topY
        )

        // Line to top-right corner
        path.lineTo(width - cornerRadius, topY)

        // Top-right corner curve
        path.quadTo(width.toFloat(), topY, width.toFloat(), topY + cornerRadius)

        // Right vertical line
        path.lineTo(width.toFloat(), bottomY)

        // Bottom line
        path.lineTo(0f, bottomY)
        path.close()

        // Draw background
        paint.color = Color.WHITE
        canvas.drawPath(path, paint)

        // Draw icons and labels
        for (i in 0 until itemCount) {
            val cx = i * widthPerItem + widthPerItem / 2
            val isSelected = i == selectedIndex

            if (isSelected) {
                val circleY = topY

                // Outer circle
                paint.color = ContextCompat.getColor(context, R.color.main_colour)
                canvas.drawCircle(cx, circleY, outerCircleRadius, paint)

                // Inner circle
                paint.color = Color.WHITE
                canvas.drawCircle(cx, circleY, innerCircleRadius, paint)

                // Icon
                val icon = ContextCompat.getDrawable(context, icons[i])
                icon?.setBounds(
                    (cx - iconSize / 2).toInt(),
                    (circleY - iconSize / 2).toInt(),
                    (cx + iconSize / 2).toInt(),
                    (circleY + iconSize / 2).toInt()
                )
                icon?.draw(canvas)

                // Label
                paint.color = Color.BLACK
                paint.textSize = textSizeSelected
                paint.textAlign = Paint.Align.CENTER
                canvas.drawText(labels[i], cx, topY + navBarHeight / 2 + dpToPx(20f), paint)

            } else {
                val iconY = topY + navBarHeight / 2 - dpToPx(10f)

                // Icon
                val icon = ContextCompat.getDrawable(context, icons[i])
                icon?.setBounds(
                    (cx - iconSize / 2).toInt(),
                    (iconY - iconSize / 2).toInt(),
                    (cx + iconSize / 2).toInt(),
                    (iconY + iconSize / 2).toInt()
                )
                icon?.draw(canvas)

                // Label
                paint.color = Color.BLACK
                paint.textSize = textSizeUnselected
                paint.textAlign = Paint.Align.CENTER
                canvas.drawText(labels[i], cx, iconY + iconSize / 2 + dpToPx(16f), paint)
            }
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (event.action == MotionEvent.ACTION_DOWN) {
            val index = (event.x / (width / itemCount)).toInt()
            if (index != selectedIndex) {
                selectedIndex = index
                invalidate()
                onItemSelected?.invoke(index)
            }
        }
        return true
    }
}
