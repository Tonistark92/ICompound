package com.icompound.phone_auth.domain.model

import com.icompound.core.data.repo.remote.auth.TokenAuthenticator
import com.icompound.core.domain.model.request.IRequestValidation
import com.icompound.core.domain.model.request.RemoteRequest
import com.icompound.core.domain.model.request.RequestContractType


/**
 * Input of GetManagerContactUC.
 *
 * The user is not logged in yet, so the call must NOT carry a bearer token: the X-No-Auth marker header
 * tells HeaderInterceptor to skip it and TokenAuthenticator not to try a refresh on a 401.
 *
 * It has no parameters today. If the endpoint needs one (e.g. a compound id), add it as a constructor
 * property, put it in remoteMap.requestQueries, and declare it in the QUERIES contract.
 */
class GetManagerContactRequest : IRequestValidation {

    override val remoteMap = RemoteRequest(
        requestHeaders = hashMapOf(TokenAuthenticator.NO_AUTH_HEADER to "true"),
    )

    override fun isInitialState() = false       // nothing to fill in

    override fun getRequestContracts() = hashMapOf(
        RequestContractType.HEADERS to hashMapOf(TokenAuthenticator.NO_AUTH_HEADER to true),
    )
}
