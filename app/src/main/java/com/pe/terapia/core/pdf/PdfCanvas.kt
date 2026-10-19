package com.pe.terapia.core.pdf

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface

class PdfCanvas(
    private val canvas: Canvas
) {

    fun text(
        value: String,
        x: Float,
        y: Float,
        size: Float = 12f,
        bold: Boolean = false,
        color: Int = Color.BLACK
    ) {
        val paint = Paint(
            Paint.ANTI_ALIAS_FLAG
        ).apply {
            this.color = color
            textSize = size
            typeface = if (bold) {
                Typeface.create(
                    Typeface.DEFAULT,
                    Typeface.BOLD
                )
            } else {
                Typeface.DEFAULT
            }
        }

        canvas.drawText(
            value,
            x,
            y,
            paint
        )
    }

    fun line(
        x1: Float,
        y1: Float,
        x2: Float,
        y2: Float
    ) {
        val paint = Paint().apply {
            color = Color.LTGRAY
            strokeWidth = 1f
        }

        canvas.drawLine(
            x1, y1,
            x2, y2,
            paint
        )
    }
}