package com.icompound.navigation.route.bottomnav

import com.icompound.navigation.R
import com.icompound.navigation.route.AccessNavKey
import com.icompound.navigation.route.BillsNavKey
import com.icompound.navigation.route.HomeNavKey
import com.icompound.navigation.route.LocationNavKey
import com.icompound.navigation.route.NavScreen
import com.icompound.navigation.route.ServicesNavKey


sealed interface BottomAppBarModel {
    val title: Int
    val icon: Int
    val iconSelected: Int
    val navScreen: NavScreen

    data class Home(
        override val title: Int = R.string.home,
        override val icon: Int = R.drawable.ic_home,
        override val iconSelected: Int = R.drawable.ic_home_selected,
        override val navScreen: NavScreen = HomeNavKey
    ) : BottomAppBarModel

    data class Services(
        override val title: Int = R.string.services,
        override val icon: Int = R.drawable.ic_services,
        override val iconSelected: Int = R.drawable.ic_services_selected,
        override val navScreen: NavScreen = ServicesNavKey
    ) : BottomAppBarModel

    data class Access(
        override val title: Int = R.string.access,
        override val icon: Int = R.drawable.ic_access,
        override val iconSelected: Int = R.drawable.ic_access_seleced,
        override val navScreen: NavScreen = AccessNavKey
    ) : BottomAppBarModel

    data class Location(
        override val title: Int = R.string.location,
        override val icon: Int = R.drawable.ic_location,
        override val iconSelected: Int = R.drawable.ic_location_selected,
        override val navScreen: NavScreen = LocationNavKey
    ) : BottomAppBarModel

    data class Bills(
        override val title: Int = R.string.bills,
        override val icon: Int = R.drawable.ic_bills,
        override val iconSelected: Int = R.drawable.ic_bills_selected,
        override val navScreen: NavScreen = BillsNavKey
    ) : BottomAppBarModel
}