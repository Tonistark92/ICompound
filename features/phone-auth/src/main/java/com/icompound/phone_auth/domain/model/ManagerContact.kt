package com.icompound.phone_auth.domain.model

import com.icompound.core.domain.model.BaseDomain


/** Returned by the "compound manager" endpoint */
data class ManagerContact(
    val name: String? = null,
    val phone: String,                  // raw dialable number, e.g. "+20100..."
    val email: String,                  // raw dialable number, e.g. "+20100..."
) : BaseDomain()