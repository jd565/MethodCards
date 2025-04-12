package com.jpd.methodcards.presentation.utils

import androidx.lifecycle.SavedStateHandle
import androidx.navigation.toRoute

inline fun <reified T : Any> SavedStateHandle.toRouteOrNull(): T? = runCatching { toRoute<T>() }.getOrNull()
