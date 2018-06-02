package com.teeh.klimasensor.rest

import android.text.TextUtils
import okhttp3.Credentials
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.net.ssl.HostnameVerifier
import javax.net.ssl.SSLSession

class ServiceGenerator(endpoint: String, teehTrustManager: SslSetupUtil) {

    val API_BASE_URL = endpoint

    private val httpClient = OkHttpClient.Builder()

    private val builder = Retrofit.Builder()
            .baseUrl(API_BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())

    private var retrofit = builder.build()

    private val teehTrustManager = teehTrustManager

    fun <S> createService(serviceClass: Class<S>): S {
        return createService(serviceClass, null, null)
    }

    fun <S> createService(
            serviceClass: Class<S>, username: String?, password: String?): S {
        if (!TextUtils.isEmpty(username) && !TextUtils.isEmpty(password)) {
            val authToken = Credentials.basic(username, password)
            return createService(serviceClass, authToken)
        }

        return createService(serviceClass, null)
    }

    fun <S> createService(
            serviceClass: Class<S>, authToken: String?): S {
        if (!TextUtils.isEmpty(authToken)) {
            val interceptor = AuthenticationInterceptor(authToken!!)

            if (!httpClient.interceptors().contains(interceptor)) {
                httpClient.addInterceptor(interceptor)
                httpClient.sslSocketFactory(teehTrustManager.sslSocketFactory, teehTrustManager.x509TrustManager)
                httpClient.hostnameVerifier(getHostnameVerifier())

                builder.client(httpClient.build())
                retrofit = builder.build()
            }
        }

        return retrofit.create(serviceClass)
    }

    fun getHostnameVerifier(): HostnameVerifier {
        return object : HostnameVerifier {
            override fun verify(hostname: String?, session: SSLSession?): Boolean {
                return true
            }
        }
    }
}