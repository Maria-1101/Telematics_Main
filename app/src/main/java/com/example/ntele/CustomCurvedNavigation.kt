package com.example.ntele

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
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

    private val outerCircleRadius = 60f  // Larger for floating effect
    private val innerCircleRadius = 50f
    private val curveDepth = 40f
    private val navBarHeight = 130f  // total nav bar height

    fun setOnItemSelectedListener(callback: (Int) -> Unit) {
        onItemSelected = callback
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val widthPerItem = width / itemCount.toFloat()
        val selectedCenterX = selectedIndex * widthPerItem + widthPerItem / 2

        val bottomY = height.toFloat()               // bottom of the view
        val topY = bottomY - navBarHeight            // top of nav bar area


        // Draw nav bar background path
        path.reset()

        val cornerRadius = 40f  // Changed from 50f to 20f

        val curveStart = selectedCenterX - outerCircleRadius
        val curveEnd = selectedCenterX + outerCircleRadius

        path.moveTo(0f, bottomY) // Start from bottom-left corner
        path.lineTo(0f, topY + cornerRadius) // Draw line up to top-left corner curve start
        path.quadTo(0f, topY, cornerRadius, topY) // Top-left corner curve
        path.lineTo(curveStart, topY) // Line to start of floating bump
// Floating bump curves
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

        path.lineTo(width - cornerRadius, topY) // Line to top-right corner curve start
        path.quadTo(width.toFloat(), topY, width.toFloat(), topY + cornerRadius) // Top-right corner curve
        path.lineTo(width.toFloat(), bottomY) // Line down to bottom-right corner

// Line along bottom edge to bottom-left corner
        path.lineTo(0f, bottomY)

        path.close()


        // Draw background
        paint.color = Color.WHITE
        canvas.drawPath(path, paint)

        // Draw items
        for (i in 0 until itemCount) {
            val cx = i * widthPerItem + widthPerItem / 2
            val isSelected = i == selectedIndex

            if (isSelected) {
                // Floating circle Y position above nav bar top line
                // For selected item floating circle Y position on topY line
                val circleY = topY

// Outer circle
                paint.color = ContextCompat.getColor(context, R.color.main_colour)
                canvas.drawCircle(cx, circleY, outerCircleRadius, paint)

// Inner circle
                paint.color = Color.WHITE
                canvas.drawCircle(cx, circleY, innerCircleRadius, paint)

// Icon in center of circle
                val icon = ContextCompat.getDrawable(context, icons[i])
                icon?.setBounds(
                    (cx - 30).toInt(),
                    (circleY - 30).toInt(),
                    (cx + 30).toInt(),
                    (circleY + 30).toInt()
                )
                icon?.draw(canvas)


                // Label under nav bar inside navBarHeight area
                paint.color = Color.BLACK
                paint.textSize = 28f
                paint.textAlign = Paint.Align.CENTER
                // Place label around the vertical center of nav bar plus some offset
                canvas.drawText(labels[i], cx, topY + navBarHeight / 2 + 40f, paint)

            } else {
                // Unselected icon Y inside nav bar
                val iconY = topY + navBarHeight / 2 - 20f

                // Icon
                val icon = ContextCompat.getDrawable(context, icons[i])
                icon?.setBounds(
                    (cx - 25).toInt(),
                    (iconY - 25).toInt(),
                    (cx + 25).toInt(),
                    (iconY + 25).toInt()
                )
                icon?.draw(canvas)

                // Label
                paint.color = Color.BLACK
                paint.textSize = 24f
                paint.textAlign = Paint.Align.CENTER
                canvas.drawText(labels[i], cx, iconY + 50f, paint)
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
