package com.jpd.methodcards.presentation.listener

import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

data class Complex(val real: Double, val imaginary: Double = 0.0) {
    operator fun plus(other: Complex): Complex = Complex(real + other.real, imaginary + other.imaginary)
    operator fun minus(other: Complex): Complex = Complex(real - other.real, imaginary - other.imaginary)
    operator fun times(other: Complex): Complex {
        val newReal = real * other.real - imaginary * other.imaginary
        val newImaginary = real * other.imaginary + imaginary * other.real
        return Complex(newReal, newImaginary)
    }

    fun conjugate(): Complex = Complex(real, -imaginary)
    fun magnitude(): Double = sqrt(real * real + imaginary * imaginary)
}

fun fft(x: DoubleArray): Array<Complex> {
    val n = x.size
    require(n > 0 && n and (n - 1) == 0) { "Size of input array must be a power of 2" } // Check if n is a power of 2

    return fftRecursive(x.map { Complex(it) }.toTypedArray())
}

private fun fftRecursive(x: Array<Complex>): Array<Complex> {
    val n = x.size
    if (n == 1) return x

    // Split into even and odd
    val even = Array(n / 2) { x[2 * it] }
    val odd = Array(n / 2) { x[2 * it + 1] }

    // Recurse
    val evenFFT = fftRecursive(even)
    val oddFFT = fftRecursive(odd)

    // Combine
    val y = Array(n) { Complex(0.0, 0.0) }
    for (k in 0 until n / 2) {
        val kth = -2 * PI * k / n
        val wk = Complex(cos(kth), sin(kth))
        y[k] = evenFFT[k] + wk * oddFFT[k]
        y[k + n / 2] = evenFFT[k] - wk * oddFFT[k]
    }
    return y
}

fun bitReverseCopy(x: DoubleArray): Array<Complex> {
    val n = x.size
    val xComplex = x.map { Complex(it) }.toTypedArray()
    val reversedX = Array(n) { Complex(0.0) }
    for (i in 0 until n) {
        reversedX[bitReverse(i, n)] = xComplex[i]
    }
    return reversedX
}

fun bitReverse(x: Int, n: Int): Int {
    var numBits = 0
    var temp = n
    while (temp > 1) {
        temp = temp shr 1
        numBits++
    }
    var reversedX = 0
    var originalX = x
    for (i in 0 until numBits) {
        reversedX = reversedX shl 1
        reversedX = reversedX or (originalX and 1)
        originalX = originalX shr 1
    }
    return reversedX
}
