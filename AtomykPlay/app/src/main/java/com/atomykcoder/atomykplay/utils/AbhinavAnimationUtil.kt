package com.atomykcoder.atomykplay.utils

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.animation.PropertyValuesHolder
import android.view.View
import android.view.animation.DecelerateInterpolator

object AbhinavAnimationUtil {

    fun View.buttonShakeAnimation() {
        // Make the button unclickable for the duration of the animation - also acts as a timeout
        this.isClickable = false

        val shakeDuration = 500.toLong()
        val resetDuration = 250.toLong()

        // Idea from: https://stackoverflow.com/a/27943288/5475354
        val shakeXHolder = PropertyValuesHolder.ofFloat(View.TRANSLATION_X, 0f, 25f, -25f, 25f, -25f, 15f, -15f, 6f, -6f, 0f)//EARTHQUAKE IMITATION
        val scaleXHolder = PropertyValuesHolder.ofFloat(View.SCALE_X, 1.1f)
        val scaleYHolder = PropertyValuesHolder.ofFloat(View.SCALE_Y, 1.1f)

        // Animation shaking horizontally, and scaling up the button in both XY together
        ObjectAnimator
            .ofPropertyValuesHolder(this@buttonShakeAnimation, shakeXHolder, scaleXHolder, scaleYHolder)
            .setDuration(shakeDuration)
            .start();

        // Reset the button scale in XY to 1f and then make button clickable again.
        val resetAnimatorSet = AnimatorSet()
        resetAnimatorSet.apply {
            playTogether(
                ObjectAnimator.ofFloat(this@buttonShakeAnimation, "scaleX", 1f),
                ObjectAnimator.ofFloat(this@buttonShakeAnimation, "scaleY", 1f)
            )
            interpolator = DecelerateInterpolator()
            duration = resetDuration
            startDelay = shakeDuration // Note how the delay is equal to the duration of the "shake"
            addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    this@buttonShakeAnimation.isClickable = true
                }
            })
            start()
        }
    }
}