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

    private val renderer : Renderer = Renderer(this)

    override fun onDraw(canvas : Canvas) {
        renderer.render(canvas)
    }

    override fun onTouchEvent(event : MotionEvent) : Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                renderer.handleTap()
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

    data class Animator(var view : View, var animated : Boolean = false) {

        fun animate(cb : () -> Unit) {
            if (animated) {
                cb()
                try {
                    Thread.sleep(delay)
                    view.invalidate()
                } catch(ex : Exception) {

                }
            }
        }

        fun start() {
            if (!animated) {
                animated = true
                view.postInvalidate()
            }
        }

        fun stop() {
            if (animated) {
                animated = false
            }
        }
    }

    data class MPLNode(var i : Int, val state : State = State()) {

        private var next : MPLNode? = null
        private var prev : MPLNode? = null

        init {
            addNeighbor()
        }

        fun addNeighbor() {
            if (i < colors.size - 1) {
                next = MPLNode(i + 1)
                next?.prev = this
            }
        }

        fun draw(canvas : Canvas, paint : Paint) {
            canvas.drawMPLNode(i, state.scale, paint)
        }

        fun update(cb : (Float) -> Unit) {
            state.update(cb)
        }

        fun startUpdating(cb : () -> Unit) {
            state.startUpdating(cb)
        }

        fun getNext(dir : Int, cb : () -> Unit) : MPLNode {
            var curr : MPLNode? = prev
            if (dir == 1) {
                curr = next
            }
            if (curr != null) {
                return curr
            }
            cb()
            return this
        }
    }

    data class MultiParallelLines(var i : Int) {

        private var curr : MPLNode = MPLNode(0)
        private var dir : Int = 1

        fun draw(canvas : Canvas, paint : Paint) {
            curr.draw(canvas, paint)
        }

        fun update(cb : (Float) -> Unit) {
            curr.update {
                curr = curr.getNext(dir) {
                    dir *= -1
                }
                cb(it)
            }
        }

        fun startUpdating(cb : () -> Unit) {
            curr.startUpdating(cb)
        }
    }

    data class Renderer(var view : MultiParallelLinesView) {

        private val animator : Animator = Animator(view)
        private val mpl : MultiParallelLines = MultiParallelLines(0)
        private val paint : Paint = Paint(Paint.ANTI_ALIAS_FLAG)

        fun render(canvas : Canvas) {
            canvas.drawColor(backColor)
            mpl.draw(canvas, paint)
            animator.animate {
                mpl.update {
                    animator.stop()
                }
            }
        }

        fun handleTap() {
            mpl.startUpdating {
                animator.start()
            }
        }
    }
}