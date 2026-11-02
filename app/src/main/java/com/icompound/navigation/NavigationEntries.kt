package com.icompound.navigation

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import java.util.Map.entry


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
//    entry<OnBoardingNavKey> {
//        val navigator = LocalNavigator.current
//
//        OnBoardingScreen {
//            navigator.navigateAndClearBackStack(LoginNavKey)
//            }
//        }
//
//        entry<LoginNavKey> {
//            val navigator = LocalNavigator.current
//
//            LoginScreen(
//                toHomeScreen = {
//                    navigator.navigateAndClearBackStack(HomeNavKey)
//                },
//                toSignUpScreen = {
//                    navigator.navigate(SignUpNavKey)
//                },
//                toCompleteProfileScreen = {
//                    navigator.navigate(CompleteProfileNavKey)
//                },
//                toForgetPassword = {
//                    navigator.navigate(ForgetPasswordNavKey)
//                },
//            )
//        }
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