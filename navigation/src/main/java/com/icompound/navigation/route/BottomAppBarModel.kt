package com.icompound.navigation.route


sealed interface BottomAppBarModel {
    val title: Int
    val icon: Int
    val iconSelected: Int
    val navScreen: NavScreen

//    data class Home(
//        override val title: Int = R.string.home,
//        override val icon: Int = R.drawable.ic_home,
//        override val iconSelected: Int = R.drawable.ic_home_selected,
//        override val navScreen: NavScreen = HomeNavKey
//    ) : BottomAppBarModel
//
//    data class MyDiplomas(
//        override val title: Int = R.string.my_diplomas,
//        override val icon: Int = R.drawable.ic_my_diplomas,
//        override val iconSelected: Int = R.drawable.ic_my_diplomas_selected,
//        override val navScreen: NavScreen = MyDiplomasNavKey
//    ) : BottomAppBarModel
//
//    data class Schedule(
//        override val title: Int = R.string.schedule,
//        override val icon: Int = R.drawable.ic_schedule,
//        override val iconSelected: Int = R.drawable.ic_schedule_selected,
//        override val navScreen: NavScreen = ScheduleNavKey
//    ) : BottomAppBarModel
//
//    data class Profile(
//        override val title: Int = R.string.profile,
//        override val icon: Int = R.drawable.ic_profile,
//        override val iconSelected: Int = R.drawable.ic_profile_selected,
//        override val navScreen: NavScreen = ProfileNavKey
//    ) : BottomAppBarModel
}