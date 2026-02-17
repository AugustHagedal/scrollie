package com.frictionscroll.service

import android.content.Context
import android.graphics.Color
import android.graphics.PixelFormat
import android.os.CountDownTimer
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView

class OverlayController(private val context: Context) {

    private val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
    private var overlayView: View? = null
    private var countDownTimer: CountDownTimer? = null

    val isShowing: Boolean get() = overlayView != null

    fun show(delayMs: Long, onDisable: () -> Unit, onSnooze: () -> Unit) {
        if (overlayView != null) return

        val layout = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setBackgroundColor(Color.argb(220, 20, 20, 40))
            setPadding(dp(32), dp(32), dp(32), dp(32))
        }

        val titleText = TextView(context).apply {
            text = "Take a breath"
            setTextColor(Color.WHITE)
            textSize = 28f
            gravity = Gravity.CENTER
        }

        val countdownText = TextView(context).apply {
            setTextColor(Color.argb(200, 255, 255, 255))
            textSize = 48f
            gravity = Gravity.CENTER
        }

        val subtitleText = TextView(context).apply {
            text = "Rapid scrolling detected"
            setTextColor(Color.argb(160, 255, 255, 255))
            textSize = 16f
            gravity = Gravity.CENTER
        }

        val buttonLayout = LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER
            setPadding(0, dp(24), 0, 0)
        }

        val disableButton = Button(context).apply {
            text = "Disable Friction"
            setOnClickListener {
                hide()
                onDisable()
            }
        }

        val snoozeButton = Button(context).apply {
            text = "Snooze 5 min"
            setOnClickListener {
                hide()
                onSnooze()
            }
        }

        buttonLayout.addView(disableButton, LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        ).apply { setMargins(0, 0, dp(8), 0) })

        buttonLayout.addView(snoozeButton, LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        ))

        layout.addView(titleText, marginParams(bottom = dp(16)))
        layout.addView(countdownText, marginParams(bottom = dp(8)))
        layout.addView(subtitleText, marginParams(bottom = dp(16)))
        layout.addView(buttonLayout)

        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
            PixelFormat.TRANSLUCENT
        )

        windowManager.addView(layout, params)
        overlayView = layout

        countDownTimer = object : CountDownTimer(delayMs, 100) {
            override fun onTick(millisUntilFinished: Long) {
                val seconds = (millisUntilFinished / 1000.0)
                countdownText.text = String.format("%.1fs", seconds)
            }

            override fun onFinish() {
                hide()
            }
        }.start()
    }

    fun hide() {
        countDownTimer?.cancel()
        countDownTimer = null
        overlayView?.let {
            try {
                windowManager.removeView(it)
            } catch (_: IllegalArgumentException) {
                // View already removed
            }
        }
        overlayView = null
    }

    private fun dp(value: Int): Int =
        TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            value.toFloat(),
            context.resources.displayMetrics
        ).toInt()

    private fun marginParams(
        top: Int = 0,
        bottom: Int = 0
    ): LinearLayout.LayoutParams =
        LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        ).apply {
            setMargins(0, top, 0, bottom)
        }
}
