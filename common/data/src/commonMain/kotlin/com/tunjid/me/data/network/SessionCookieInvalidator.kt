/*
 * Copyright 2021 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.tunjid.me.data.network

import com.tunjid.me.data.local.SessionCookieDao
import io.ktor.client.*
import io.ktor.client.features.*
import io.ktor.client.features.observer.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.util.*
import kotlinx.serialization.Serializable

@Serializable
internal data class NetworkError(
    val errorCode: String?,
    val message: String?,
    val model: String?,
)

class ErrorInterceptorConfig {
    internal var networkErrorConverter: ((String) -> NetworkError)? = null
    internal var sessionCookieDao: SessionCookieDao? = null
}

/**
 * Invalidates session cookies that have expired or are otherwise invalid
 */
internal class SessionCookieInvalidator(
    private val networkErrorConverter: ((String) -> NetworkError)?,
    private val sessionCookieDao: SessionCookieDao?
) {

    companion object : HttpClientFeature<ErrorInterceptorConfig, SessionCookieInvalidator> {
        override val key: AttributeKey<SessionCookieInvalidator> =
            AttributeKey("ClientNetworkErrorInterceptor")

        override fun prepare(block: ErrorInterceptorConfig.() -> Unit): SessionCookieInvalidator {
            val config = ErrorInterceptorConfig().apply(block)
            return SessionCookieInvalidator(
                networkErrorConverter = config.networkErrorConverter,
                sessionCookieDao = config.sessionCookieDao
            )
        }

        override fun install(feature: SessionCookieInvalidator, scope: HttpClient) {
            val observer: ResponseHandler = responseHandler@{ response ->
                val converter = feature.networkErrorConverter ?: return@responseHandler
                val sessionCookieDao = feature.sessionCookieDao ?: return@responseHandler

                if (!response.status.isSuccess())
                    try {
                        val responseText = response.readText()
                        val error = converter(responseText)

                        if (error.errorCode == NetworkErrorCodes.NotLoggedIn.code) {
                            sessionCookieDao.saveSessionCookie(sessionCookie = null)
                        }
                    } catch (_: Throwable) {
                    }
            }

            ResponseObserver.install(ResponseObserver(observer), scope)
        }
    }
}