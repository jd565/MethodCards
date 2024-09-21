package com.jpd.methodcards

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.jpd.methodcards.di.MethodCardNonWebDi
import com.jpd.methodcards.presentation.App

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        MethodCardNonWebDi.appContext = applicationContext

        setContent {
            App()
        }
    }
}
