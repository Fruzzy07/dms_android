package com.example.dms.ui

import android.content.Context
import android.graphics.Matrix
import android.graphics.PointF
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import androidx.appcompat.widget.AppCompatImageView

/**
 * Простой ImageView с поддержкой pinch‑zoom и перетаскивания,
 * без сторонних библиотек.
 */
class ZoomImageView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : AppCompatImageView(context, attrs, defStyleAttr) {

    private val matrixValues = FloatArray(9)
    private val imageMatrixInternal = Matrix()
    private val lastTouch = PointF()

    private var scaleDetector: ScaleGestureDetector
    private var currentScale = 1f
    private val minScale = 1f
    private val maxScale = 4f

    private var isDragging = false

    init {
        scaleType = ScaleType.MATRIX
        imageMatrix = imageMatrixInternal

        post { fitImageToView() }

        scaleDetector = ScaleGestureDetector(context, object : ScaleGestureDetector.SimpleOnScaleGestureListener() {
            override fun onScale(detector: ScaleGestureDetector): Boolean {
                val scaleFactor = detector.scaleFactor
                val newScale = (currentScale * scaleFactor).coerceIn(minScale, maxScale)
                val factor = newScale / currentScale
                currentScale = newScale

                imageMatrixInternal.postScale(
                    factor,
                    factor,
                    detector.focusX,
                    detector.focusY
                )
                imageMatrix = imageMatrixInternal
                return true
            }
        })
    }

    override fun setImageMatrix(matrix: Matrix?) {
        // блокируем внешние изменения матрицы, работаем только со своей
        super.setImageMatrix(imageMatrixInternal)
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        fitImageToView()
    }

    private fun fitImageToView() {
        val drawable = drawable ?: return
        val viewWidth = width.toFloat()
        val viewHeight = height.toFloat()
        if (viewWidth == 0f || viewHeight == 0f) return

        val intrinsicWidth = drawable.intrinsicWidth.toFloat()
        val intrinsicHeight = drawable.intrinsicHeight.toFloat()
        if (intrinsicWidth <= 0f || intrinsicHeight <= 0f) return

        imageMatrixInternal.reset()

        val scale = minOf(
            viewWidth / intrinsicWidth,
            viewHeight / intrinsicHeight
        )
        currentScale = scale.coerceAtLeast(minScale)

        val dx = (viewWidth - intrinsicWidth * currentScale) / 2f
        val dy = (viewHeight - intrinsicHeight * currentScale) / 2f

        imageMatrixInternal.postScale(currentScale, currentScale)
        imageMatrixInternal.postTranslate(dx, dy)
        super.setImageMatrix(imageMatrixInternal)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        scaleDetector.onTouchEvent(event)

        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                lastTouch.set(event.x, event.y)
                isDragging = true
            }
            MotionEvent.ACTION_MOVE -> {
                if (isDragging && !scaleDetector.isInProgress) {
                    val dx = event.x - lastTouch.x
                    val dy = event.y - lastTouch.y
                    imageMatrixInternal.postTranslate(dx, dy)
                    imageMatrix = imageMatrixInternal
                    lastTouch.set(event.x, event.y)
                }
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                isDragging = false
            }
        }

        return true
    }
}

