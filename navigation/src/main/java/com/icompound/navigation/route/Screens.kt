package com.icompound.navigation.route

import androidx.navigation3.runtime.NavKey
import com.icompound.core.presentation.componenet.ToolBarState
import com.icompound.navigation.route.bottomnav.BottomAppBarModel
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

@Serializable
sealed class NavScreen(
    @Transient open val toolBarState: ToolBarState = ToolBarState.Hidden,
    @Transient open val hasTopPadding: Boolean = true,
    @Transient open val hasBottomPadding: Boolean = true,
) : NavKey

val bottomBarItems = listOf(
    BottomAppBarModel.Home,
)

@Serializable
object SplashNavKey : NavScreen(
    hasTopPadding = false,
    hasBottomPadding = false
)

@Serializable
object OnBoardingNavKey : NavScreen()

@Serializable
object LoginNavKey : NavScreen()

@Serializable
object SignUpNavKey : NavScreen()

@Serializable
object CompleteProfileNavKey : NavScreen()

//@Serializable
//object EditProfileNavKey : NavScreen(
//    toolBarState = ToolBarState.TitleWithBack(titleResId = R.string.edit_profile)
//)
//
//@Serializable
//object ForgetPasswordNavKey : NavScreen(
//    toolBarState = ToolBarState.AuthTitleWithBack(titleResId = R.string.forget_password_title)
//)
//
//@Serializable
//object HomeNavKey : NavScreen()
//
//@Serializable
//object ProfileNavKey : NavScreen()
//
//@Serializable
//object MyCertificatesNavKey : NavScreen(
//    toolBarState = ToolBarState.TitleWithBack(titleResId = R.string.my_certificates)
//)


