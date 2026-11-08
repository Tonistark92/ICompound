package com.icompound.navigation

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.icompound.navigation.compose.LocalNavigator
import com.icompound.navigation.route.LoginNavKey
import com.icompound.navigation.route.PhoneAuthNavKey
import com.icompound.navigation.route.OnBoardingNavKey
import com.icompound.onboarding.presentation.OnBoardingScreen


fun EntryProviderScope<NavKey>.appNavigationEntries() {

//    entry<SplashNavKey> {
//        val navigator = LocalNavigator.current
//
//        SplashScreen(
//            toLoginScreen = {
//                navigator.navigateAndClearBackStack(LoginNavKey)
//            },
//            toOnBoardingScreen = {
//                navigator.navigateAndClearBackStack(OnBoardingNavKey)
//            },
//            toHomeScreen = {
//                navigator.navigateAndClearBackStack(HomeNavKey)
//            },
//        )
//    }
//
    entry<OnBoardingNavKey> {
        val navigator = LocalNavigator.current

        OnBoardingScreen {
            navigator.navigateAndClearBackStack(LoginNavKey)
            }
        }

        entry<PhoneAuthNavKey> {
            val navigator = LocalNavigator.current

        }
//
//        entry<SignUpNavKey> {
//            val navigator = LocalNavigator.current
//
//            SignUpScreen(
//                toLoginScreen = {
//                    navigator.navigateUp()
//                },
//            )
//        }
//
//        entry<ForgetPasswordNavKey> {
//            val navigator = LocalNavigator.current
//
//            ForgetPasswordScreen(
//                toLoginScreen = {
//                    navigator.navigateUp()
//                },
//            )
//        }

}