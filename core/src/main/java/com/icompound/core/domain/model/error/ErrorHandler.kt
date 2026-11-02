package com.icompound.core.domain.model.error

import com.icompound.core.domain.model.exception.AppException


interface ErrorHandler {
    fun getError(throwable: Throwable): AppException
}