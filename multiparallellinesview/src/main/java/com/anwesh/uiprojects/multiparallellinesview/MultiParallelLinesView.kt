package com.anwesh.uiprojects.multiparallellinesview

/**
 * Created by anweshmishra on 23/02/20.
 */

import android.view.View
import android.view.MotionEvent
import android.app.Activity
import android.content.Context
import android.graphics.Paint
import android.graphics.Canvas
import android.graphics.Color

val colors : Array<String> = arrayOf("#4CAF50", "#1565C0", "#f44336", "#673AB7", "#EF6C00")
val deg : Float = 90f
val lines : Int = 5
val scGap : Float = 0.02f
val strokeFactor : Int = 90
val sizeFactor : Float = 2.9f
val backColor : Int = Color.parseColor("#BDBDBD")
val delay : Long = 20

fun Int.inverse() : Float = 1f / this
fun Float.maxScale(i : Int, n : Int) : Float = Math.max(0f, this - i * n.inverse())
fun Float.divideScale(i : Int, n : Int) : Float = Math.min(n.inverse(), maxScale(i, n)) * n
fun Float.sinify() : Float = Math.sin(this * Math.PI).toFloat()

fun Canvas.drawParallelLine(i : Int, scale : Float, size : Float, paint : Paint) {
    val sf : Float = scale.sinify().divideScale(i, lines)
    val gap : Float = (size / lines) * (i + 1)
    for (j in 0..1) {
        save()
        translate(0f, gap * 0.5f * (1f - 2 * j) * sf)
        rotate(90f * sf)
        drawLine(0f, -gap / 2, 0f, gap / 2, paint)
        restore()
    }
}

fun Canvas.drawMultiParallelLine(scale : Float, size : Float, paint : Paint) {
    for (j in 0..(lines - 1)) {
        drawParallelLine(j, scale, size, paint)
    }
}

fun Canvas.drawMPLNode(i : Int, scale : Float, paint : Paint) {
    val w : Float = width.toFloat()
    val h : Float = height.toFloat()
    val size : Float = Math.min(w, h)
    paint.color = Color.parseColor(colors[i])
    paint.strokeWidth = size / strokeFactor
    paint.strokeCap = Paint.Cap.ROUND
    save()
    translate(w / 2, h / 2)
    drawMultiParallelLine(scale, size, paint)
    restore()
}

class MultiParallelLinesView(ctx : Context) : View(ctx) {

    override fun onDraw(canvas : Canvas) {

    }

    override fun onTouchEvent(event : MotionEvent) : Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {

            }
        }
        return true
    }

    data class State(var scale : Float = 0f, var dir : Float = 0f, var prevScale : Float = 0f) {

        fun update(cb : (Float) -> Unit) {
            scale += scGap * dir
            if (Math.abs(scale - prevScale) > 1) {
                scale = prevScale + dir
                dir = 0f
                prevScale = scale
                cb(prevScale)
            }
        }

        fun startUpdating(cb : () -> Unit) {
            if (dir == 0f) {
                dir = 1f - 2 * dir
                cb()
            }
        }
    }
}