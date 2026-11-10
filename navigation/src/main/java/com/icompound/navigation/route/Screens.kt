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

val bottomBarItems =
    listOf(
        BottomAppBarModel.Home(),
        BottomAppBarModel.Access(),
        BottomAppBarModel.Bills(),
        BottomAppBarModel.Location(),
        BottomAppBarModel.Services(),
    )

@Serializable
object SplashNavKey : NavScreen(
    hasTopPadding = false,
    hasBottomPadding = false,
)

@Serializable
object OnBoardingNavKey : NavScreen(
    hasTopPadding = false,
    hasBottomPadding = false,
)

@Serializable
object LoginNavKey : NavScreen()

@Serializable
object PhoneAuthNavKey : NavScreen()

@Serializable
object SignUpNavKey : NavScreen()

@Serializable
object HomeNavKey : NavScreen()

@Serializable
object ServicesNavKey : NavScreen()

@Serializable
object AccessNavKey : NavScreen()

@Serializable
object BillsNavKey : NavScreen()

@Serializable
object LocationNavKey : NavScreen()
