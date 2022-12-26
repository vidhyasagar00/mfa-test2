package com.net.routee.setUp

import android.content.Context
import androidx.test.platform.app.InstrumentationRegistry
import com.net.routee.interfaces.APICallback
import com.net.routee.preference.SharedPreference
import com.net.routee.retrofit.*
import kotlinx.coroutines.*
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.json.JSONObject
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import retrofit2.Call
import retrofit2.Response
import kotlin.test.assertFalse


@Suppress("BlockingMethodInNonBlockingContext")
class ApplicationInteractionClassTest {

    private lateinit var instrumentationContext: Context
    private lateinit var applicationInteractionClass: ApplicationInteractionClass

    /**
     * Server -
     * Creating a MockWebServer for reading the required json from resources.
     */
    private var server: MockWebServer = MockWebServer()


    /**
     * getting context from InstrumentationRegistry
     */
    @Before
    fun setup() {

        instrumentationContext = InstrumentationRegistry.getInstrumentation().context
        applicationInteractionClass = ApplicationInteractionClass(instrumentationContext)

    }

    /**
     * Tear down
     *
     */
    @After
    fun tearDown() {
        server.shutdown()
    }

    @Test
    fun setInitialConfiguration() {
        val preference = SharedPreference(instrumentationContext)
        val exampleJson = JSONObject().put("key", "value")

        applicationInteractionClass.setInitialConfiguration(exampleJson.toString())
        val savedJsonString = preference.getInitialConfiguration()
        assertEquals(exampleJson.toString(), savedJsonString)
    }

    /**
     * It was to read a json file which is statically stored as request object
     *
     * @return a json object.
     */
    private fun readJsonFroMock(): JSONObject {
        val requestBody = MockResponse()
            .setBody(MockResponseFileReader("configure_object.json").content)
        server.enqueue(requestBody)
        return JSONObject(requestBody.getBody()?.readUtf8().toString())
    }


    /**
     * Check the status code of initialize API with correct URL.
     *
     */
    @Test
    fun initializeAPIWithCorrectStatusCode() {

        val configurationObject = readJsonFroMock()
        val applicationDetails =
            ApplicationDetails(applicationUUID = configurationObject.getString("applicationUUID"))
        runBlocking {
            val job: Job = launch(context = Dispatchers.Default) {
                val response = APISupport.postAPI(
                    configurationObject.getString("configurationUrl"),
                    applicationDetails,
                )?.execute()
                assertTrue(response?.code() == 200)
            }
            job.join()
        }
    }

    /**
     * Check the status code of initialize API with wrong URL.
     *
     */
    @Test
    fun initializeAPIWithWrongStatusCode() {
        val configurationObject = readJsonFroMock()
        val applicationDetails =
            ApplicationDetails(applicationUUID = configurationObject.getString("applicationUUID"))
        runBlocking {
            val job: Job = launch(context = Dispatchers.Default) {
                val response = APISupport.postAPI(
                    configurationObject.getString("configurationUrl") + "wrong url",
                    applicationDetails,
                )?.execute()

                assertTrue(response?.code() == 404)
            }
            job.join()
        }
    }

    @Test
    fun initializeAPIPerformingRetries() {
        val configurationObject = readJsonFroMock()
        var apiCallTimes = configurationObject.getString("configurationCallRetries").toDouble()
        val applicationDetails =
            ApplicationDetails(applicationUUID = configurationObject.getString("applicationUUID"))
        runBlocking {

            val job: Job = launch(context = Dispatchers.Default) {
                withContext(Dispatchers.Default) {
                    val apiCallback = object : APICallback {
                        override fun apiResult(result: APIResult<*>) {
                            if (result == APIErrorRes(404)) {
                                if (--apiCallTimes == 0.0) {
                                    assertFalse(true)
                                }
                            }
                        }
                    }

                    val call = APISupport.postAPI(
                        configurationObject.getString("configurationUrl") + "wrong url",
                        applicationDetails
                    )
                    call?.enqueue(object : CustomizedCallback<Any>(
                        configurationObject.getString("configurationCallRetries").toDouble(),
                        configurationObject.getString("configurationCallRetryDelay").toDouble()
                    ) {
                        override fun onResponse(
                            call: Call<Any>,
                            response: Response<Any>
                        ) {
                            if (response.isSuccessful) {
                                apiCallback.apiResult(APISuccess(response.body()))
                            } else {
                                apiCallback.apiResult(APIErrorRes(response.code()))
                                super.onResponse(call, response, apiCallback)
                            }
                        }

                        override fun onFailure(call: Call<Any>, t: Throwable) {
                            super.onFailure(call, t, apiCallback)
                        }
                    })

                }
            }
            job.join()
        }

    }

    /**
     * Check the status code of user id API with correct URL.
     *
     */
    @Test
    fun setUserIdWithCorrectStatusCode() {
//        configurationObject.getString("configurationCallRetries").toDouble(),
//        configurationObject.getString("configurationCallRetryDelay").toDouble()

        val configurationObject = readJsonFroMock()
        val applicationDetails =
            ApplicationDetails(
                applicationUUID = configurationObject.getString("applicationUUID"),
                deviceUUID = "asa",
                userId = "ASK001"
            )
        runBlocking {
            val job: Job = launch(context = Dispatchers.Default) {
                val response = APISupport.postAPI(
                    configurationObject.getString("fireBaseConsumer"),
                    applicationDetails,
                )?.execute()
                assertEquals(response?.code(), 200)
            }
            job.join()
        }
    }

    /**
     * Check the status code of user id API with wrong URL.
     *
     */
    @Test
    fun setUserIdWithWrongStatusCode() {
//        configurationObject.getString("configurationCallRetries").toDouble(),
//        configurationObject.getString("configurationCallRetryDelay").toDouble()

        val configurationObject = readJsonFroMock()
        val applicationDetails =
            ApplicationDetails(
                applicationUUID = configurationObject.getString("applicationUUID"),
                deviceUUID = "asa",
                userId = "ASK001"
            )
        runBlocking {
            val job: Job = launch(context = Dispatchers.Default) {
                val response = APISupport.postAPI(
                    configurationObject.getString("fireBaseConsumer") + "wrong url",
                    applicationDetails,
                )?.execute()
                assertEquals(response?.code(), 404)
            }
            job.join()
        }
    }

    /**
     * Check the status code of token change API with correct URL
     *
     */
    @Test
    fun tokenChangedWithCorrectStatusCode() {
        val configurationObject = readJsonFroMock()
        val applicationDetails =
            ApplicationDetails(
                applicationUUID = configurationObject.getString("applicationUUID"),
                deviceUUID = "asa",
                userId = "ASK001",
                token = "some random token"
            )
        runBlocking {
            val job: Job = launch(context = Dispatchers.Default) {

                val response = APISupport.postAPI(
                    configurationObject.getString("fireBaseConsumer"),
                    applicationDetails,
                )?.execute()
                assertEquals(response?.code(), 200)
            }
            job.join()
        }
    }

    /**
     * Check the status code of token change API with wrong URL
     *
     */
    @Test
    fun tokenChangedWithWrongStatusCode() {
        val configurationObject = readJsonFroMock()
        val applicationDetails =
            ApplicationDetails(
                applicationUUID = configurationObject.getString("applicationUUID"),
                deviceUUID = "asa",
                userId = "ASK001",
                token = "some random token"
            )
        runBlocking {
            val job: Job = launch(context = Dispatchers.Default) {
                val response = APISupport.postAPI(
                    configurationObject.getString("fireBaseConsumer") + "wrong url",
                    applicationDetails,
                )?.execute()
                assertEquals(response?.code(), 404)
            }
            job.join()
        }
    }


}

