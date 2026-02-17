package com.frictionscroll.service

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Color
import android.graphics.PixelFormat
import android.graphics.drawable.GradientDrawable
import android.media.AudioManager
import android.os.Handler
import android.view.KeyEvent
import android.os.Looper
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import android.view.animation.AccelerateInterpolator
import android.view.animation.DecelerateInterpolator
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.frictionscroll.R

class OverlayController(private val context: Context) {

    private val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
    private val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    private var overlayView: View? = null
    private var animatorSet: AnimatorSet? = null
    private val handler = Handler(Looper.getMainLooper())
    private var didPauseMedia = false

    val isShowing: Boolean get() = overlayView != null

    fun show(delayMs: Long, onDisable: () -> Unit, onSnooze: () -> Unit) {
        if (overlayView != null) return

        // Root frame — warm beige dim background
        val root = FrameLayout(context).apply {
            setBackgroundColor(Color.argb((255 * 0.35f).toInt(), 0xE8, 0xDC, 0xC8))
        }

        // Card container
        val card = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER_HORIZONTAL
            setPadding(dp(32), dp(32), dp(32), dp(24))
            background = GradientDrawable().apply {
                setColor(Color.parseColor("#F5EDE0"))
                cornerRadius = dp(24).toFloat()
            }
        }

        // Cat mascot image
        val catImage = ImageView(context).apply {
            setImageResource(R.drawable.cat_mascot)
            pivotY = dp(120).toFloat() // pivot at bottom for breathing
        }
        val catParams = LinearLayout.LayoutParams(dp(120), dp(120)).apply {
            gravity = Gravity.CENTER_HORIZONTAL
            bottomMargin = dp(16)
        }

        // "just a breath" text
        val breathText = TextView(context).apply {
            text = "just a breath"
            setTextColor(Color.parseColor("#7A6F68"))
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 16f)
            gravity = Gravity.CENTER
        }
        val textParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        ).apply {
            gravity = Gravity.CENTER_HORIZONTAL
            bottomMargin = dp(20)
        }

        // Button row
        val buttonRow = LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER
        }

        val snoozeBtn = TextView(context).apply {
            text = "snooze 5 min"
            setTextColor(Color.parseColor("#8B7D6E"))
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 14f)
            setPadding(dp(16), dp(8), dp(16), dp(8))
            setOnClickListener {
                hide()
                onSnooze()
            }
        }

        val disableBtn = TextView(context).apply {
            text = "disable"
            setTextColor(Color.parseColor("#8B7D6E"))
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 14f)
            setPadding(dp(16), dp(8), dp(16), dp(8))
            setOnClickListener {
                hide()
                onDisable()
            }
        }

        buttonRow.addView(snoozeBtn)
        buttonRow.addView(disableBtn)

        card.addView(catImage, catParams)
        card.addView(breathText, textParams)
        card.addView(buttonRow)

        val cardParams = FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.WRAP_CONTENT,
            FrameLayout.LayoutParams.WRAP_CONTENT,
            Gravity.CENTER
        )
        root.addView(card, cardParams)

        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
            PixelFormat.TRANSLUCENT
        )

        windowManager.addView(root, params)
        overlayView = root

        // Send media pause key event to pause video (Instagram, TikTok, etc.)
        if (audioManager.isMusicActive) {
            dispatchMediaKey(KeyEvent.KEYCODE_MEDIA_PAUSE)
            didPauseMedia = true
        } else {
            didPauseMedia = false
        }

        // Fade in: 200ms
        root.alpha = 0f
        val fadeIn = ObjectAnimator.ofFloat(root, "alpha", 0f, 1f).apply {
            duration = 200
            interpolator = DecelerateInterpolator()
        }

        // Cat breathing: scale Y oscillation 1.0 → 1.03 → 1.0 over 2.6s
        val breathAnim = ObjectAnimator.ofFloat(catImage, "scaleY", 1.0f, 1.03f, 1.0f).apply {
            duration = 2600
            repeatCount = 0
        }

        // Blink animation at ~1.5s: quick alpha flash on eyes
        val blinkAnim = ObjectAnimator.ofFloat(catImage, "alpha", 1f, 0.85f, 1f).apply {
            duration = 200
            startDelay = 1300 // fires at ~1.5s into the breathing
        }

        // Fade out: 200ms, starting at 2.8s (after breathing completes)
        val fadeOut = ObjectAnimator.ofFloat(root, "alpha", 1f, 0f).apply {
            duration = 200
            interpolator = AccelerateInterpolator()
            startDelay = 2800
        }

        animatorSet = AnimatorSet().apply {
            playTogether(fadeIn, breathAnim, blinkAnim, fadeOut)
            start()
        }

        // Auto-dismiss after full sequence (3.0s)
        handler.postDelayed({ hide() }, delayMs)
    }

    fun hide() {
        handler.removeCallbacksAndMessages(null)
        animatorSet?.cancel()
        animatorSet = null
        // Resume media if we paused it
        if (didPauseMedia) {
            dispatchMediaKey(KeyEvent.KEYCODE_MEDIA_PLAY)
            didPauseMedia = false
        }
        overlayView?.let {
            try {
                windowManager.removeView(it)
            } catch (_: IllegalArgumentException) {
                // View already removed
            }
        }
        overlayView = null
    }

    private fun dispatchMediaKey(keyCode: Int) {
        val downEvent = KeyEvent(KeyEvent.ACTION_DOWN, keyCode)
        val upEvent = KeyEvent(KeyEvent.ACTION_UP, keyCode)
        audioManager.dispatchMediaKeyEvent(downEvent)
        audioManager.dispatchMediaKeyEvent(upEvent)
    }

    private fun dp(value: Int): Int =
        TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            value.toFloat(),
            context.resources.displayMetrics
        ).toInt()
}
