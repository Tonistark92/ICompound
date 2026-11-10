package com.icompound.navigation.di

import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import com.icompound.navigation.navigator.Navigator
import com.icompound.navigation.navigator.NavigatorImpl
import org.koin.dsl.module

val navigationModule =
    module {
        single<Navigator> {
            NavigatorImpl(
                backStack = get<NavBackStack<NavKey>>(),
            )
        }
    }
