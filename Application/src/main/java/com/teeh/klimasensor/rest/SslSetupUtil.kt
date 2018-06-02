package com.teeh.klimasensor.rest

import android.content.Context
import com.google.android.gms.common.config.GservicesValue.init
import com.teeh.klimasensor.common.config.ConfigService
import java.io.BufferedInputStream
import java.io.InputStream
import java.security.KeyStore
import java.security.cert.CertificateFactory
import java.security.cert.X509Certificate
import java.util.*
import javax.net.ssl.*


class SslSetupUtil(private val context: Context) {

    var sslSocketFactory: SSLSocketFactory? = null
    var x509TrustManager: X509TrustManager? = null

    val TEEHNET_CA_FILE = "teehnet.ca.file"

    init {

        // Load CAs from an InputStream
        // (could be from a resource or ByteArrayInputStream or ...)
        val cf: CertificateFactory = CertificateFactory.getInstance("X.509")
        // From https://www.washington.edu/itconnect/security/ca/load-der.crt

        val configService = ConfigService(context)

        val assetManager = context.assets
        val caInputStream = assetManager.open(configService.get(TEEHNET_CA_FILE))

        val caInput: InputStream = BufferedInputStream(caInputStream)
        val ca: X509Certificate = caInput.use {
            cf.generateCertificate(it) as X509Certificate
        }
        System.out .println("ca=" + ca.subjectDN)

        // Create a KeyStore containing our trusted CAs
        val keyStoreType = KeyStore.getDefaultType()
        val keyStore = KeyStore.getInstance(keyStoreType).apply {
            load(null, null)
            setCertificateEntry("ca", ca)
        }

        // Create a TrustManager that trusts the CAs inputStream our KeyStore
        val tmfAlgorithm: String = TrustManagerFactory.getDefaultAlgorithm()
        val tmf: TrustManagerFactory = TrustManagerFactory.getInstance(tmfAlgorithm).apply {
            init(keyStore)
        }

        if (tmf.trustManagers.size != 1 || !(tmf.trustManagers[0] is X509TrustManager)) {
            throw IllegalStateException("Unexpected default trust managers:" + Arrays.toString(tmf.trustManagers));
        }

        x509TrustManager = tmf.trustManagers[0] as X509TrustManager

        // Create an SSLContext that uses our TrustManager
        val sslContext: SSLContext = SSLContext.getInstance("TLS").apply {
            init(null, tmf.trustManagers, null)
        }

        sslSocketFactory = sslContext.socketFactory
    }
}