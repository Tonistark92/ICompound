package com.icompound.core.domain.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class User(
    @SerialName("code")
    val code: Int? = null, // 20775298
    @SerialName("email")
    val email: String? = null, // abdullah.muhammad.62001@gmail.com
    @SerialName("firstName")
    val firstName: String? = null,
    @SerialName("middleName")
    val middleName: String? = null,
    @SerialName("lastName")
    val lastName: String? = null,
    @SerialName("id")
    val id: String? = null, // 71815b25-8040-4282-82cd-64321d011da2
    @SerialName("token")
    val token: String? = null, // eyJhbGciOiJIUzUxMiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiI3MTgxNWIyNS04MDQwLTQyODItODJjZC02NDMyMWQwMTFkYTIiLCJlbWFpbCI6WyJhYmR1bGxhaC5tdWhhbW1hZC42MjAwMUBnbWFpbC5jb20iLCJhYmR1bGxhaC5tdWhhbW1hZC42MjAwMUBnbWFpbC5jb20iLCJhYmR1bGxhaC5tdWhhbW1hZC42MjAwMUBnbWFpbC5jb20iXSwiZ2l2ZW5fbmFtZSI6IkFiZHVsbGFoTXVoYW1tYWQiLCJqdGkiOiI3MzY2MWU0MC0yY2MwLTQ5MmYtYTRjMy1iMzNhZjEyZjFjYTkiLCJpYXQiOjE3ODM5NDI0NzEsIm5hbWVpZCI6IjcxODE1YjI1LTgwNDAtNDI4Mi04MmNkLTY0MzIxZDAxMWRhMiIsInVuaXF1ZV9uYW1lIjoiQWJkdWxsYWhNdWhhbW1hZCIsInVzZXJJZCI6IjcxODE1YjI1LTgwNDAtNDI4Mi04MmNkLTY0MzIxZDAxMWRhMiIsInJvbGUiOlsiQ3VzdG9tZXIiLCJDdXN0b21lciJdLCJuYmYiOjE3ODM5NDI0NzEsImV4cCI6MTc4NDU0NzI3MSwiaXNzIjoiaHR0cDovL2xvY2FsaG9zdDo3MjMyIiwiYXVkIjoiaHR0cDovL2xvY2FsaG9zdDo3MjMyIn0.xB7jM2iVzTtDbk0osLxDIGk-T6ZHuXtEPHtsb_lS5i1Dg8EYeHJ50lnzzdFlK8IHEXlt29yymceAIlAp9f4AWw
    @SerialName("username")
    val username: String? = null,
    @SerialName("phoneNumber")
    val phoneNumber: PhoneNumber? = null,
    @SerialName("whatsappNumber")
    val whatsappNumber: WhatsappNumber? = null,
    @SerialName("isActive")
    val isActive: Boolean? = null,
    @SerialName("isDeactivated")
    val isDeactivated: Boolean? = null,
    @SerialName("deactivatedAt")
    val deactivatedAt: String? = null,
    @SerialName("profilePicture")
    val profilePicture: String? = null,
    @SerialName("hasProfilePhoto")
    val hasProfilePhoto: Boolean? = null,
)