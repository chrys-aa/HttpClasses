import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import javax.net.ssl.SSLContext
import java.security.NoSuchAlgorithmException
import java.security.KeyManagementException
import java.security.cert.X509Certificate
import java.util.concurrent.Executor
import java.util.concurrent.Executors
import javax.net.ssl.X509TrustManager

fun main() {
    val url = "https://self-signed.badssl.com/" // Replace with the URL you want to call
    HttpCallWithPKIXErrorHandling.makeHttpRequest(url)
}

class HttpCallWithPKIXErrorHandling {
    companion object {
        fun makeHttpRequest(url: String) {
            var client: HttpClient? = null
            val clientExecutor = Executors.newCachedThreadPool();
            try {
                client = createHttpClientWithCustomSSLContext(clientExecutor)

                val request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .GET()
                    .build()

                val response = client.send(request, HttpResponse.BodyHandlers.ofString())

                if (response.statusCode() == 200) {
                    println("HTTP Request Successful")
                    println("Response Body: ${response.body()}")
                } else {
                    println("HTTP Request Failed with Status Code: ${response.statusCode()}")
                }
            } catch (e: Exception) {
                println("Error: ${e.message}")
            } finally {
                clientExecutor.shutdown()
            }
        }

        private fun createHttpClientWithCustomSSLContext(executor: Executor): HttpClient {
            val customSSLContext = createCustomSSLContext()
            return HttpClient.newBuilder()
                .executor(executor)
                .sslContext(customSSLContext) //If removed will get PKIX error.
                .build()
        }

        private fun createCustomSSLContext(): SSLContext {
            try {
                val sslContext = SSLContext.getInstance("TLS")
                sslContext.init(null, arrayOf(TrustAllTrustManager()), null)
                return sslContext
            } catch (e: NoSuchAlgorithmException) {
                throw RuntimeException("Failed to create custom SSL context", e)
            } catch (e: KeyManagementException) {
                throw RuntimeException("Failed to initialize custom SSL context", e)
            }
        }
    }
    private class TrustAllTrustManager : X509TrustManager {
        override fun checkClientTrusted(chain: Array<out X509Certificate>?, authType: String?) {
        }

        override fun checkServerTrusted(chain: Array<out X509Certificate>?, authType: String?) {
        }

        override fun getAcceptedIssuers(): Array<X509Certificate> {
            return emptyArray()
        }
    }

}
