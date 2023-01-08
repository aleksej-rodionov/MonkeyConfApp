package com.example.monkeyconfapp.util

import android.os.Looper

fun isCurrentThreadMain() = Looper.myLooper() == Looper.getMainLooper()

fun currentThreadName() = Thread.currentThread().name