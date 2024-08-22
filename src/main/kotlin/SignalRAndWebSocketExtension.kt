import burp.api.montoya.BurpExtension
import burp.api.montoya.MontoyaApi
import burp.api.montoya.http.message.params.HttpParameterType
import burp.api.montoya.proxy.http.InterceptedRequest
import burp.api.montoya.proxy.http.ProxyRequestHandler
import burp.api.montoya.proxy.http.ProxyRequestReceivedAction
import burp.api.montoya.proxy.http.ProxyRequestToBeSentAction
import burp.api.montoya.utilities.json.JsonNode
import com.nickcoblentz.montoya.appendNotes


// Montoya API Documentation: https://portswigger.github.io/burp-extensions-montoya-api/javadoc/burp/api/montoya/MontoyaApi.html
// Montoya Extension Examples: https://github.com/PortSwigger/burp-extensions-montoya-api-examples

class SignalRAndWebSocketExtension : BurpExtension, ProxyRequestHandler {
    private lateinit var api: MontoyaApi

    override fun initialize(api: MontoyaApi?) {

        // In Kotlin, you have to explicitly define variables as nullable with a ? as in MontoyaApi? above
        // This is necessary because the Java Library allows null to be passed into this function
        // requireNotNull is a built-in Kotlin function to check for null and throw an Illegal Argument exception if it is null
        // after checking for null, the Kotlin compiler knows that any reference to api below will not = null and you no longer have to check it
        requireNotNull(api) {"api : MontoyaApi is not allowed to be null"}

        // Assign the MontoyaApi instance (not nullable) to a class instance variable to be accessible from other functions in this class
        this.api = api

        // This will print to Burp Suite's Extension output and can be used to debug whether the extension loaded properly
        api.logging().logToOutput("Started loading the extension...")

        // Name our extension when it is displayed inside of Burp Suite
        api.extension().setName("SignalRAndWebSocketDemo")

        // Code for setting up your extension starts here...

        api.proxy().registerRequestHandler(this)

        // Code for setting up your extension ends here

        // See logging comment above
        api.logging().logToOutput("...Finished loading the extension")

    }
    
    override fun handleRequestReceived(interceptedRequest: InterceptedRequest?): ProxyRequestReceivedAction {
        return ProxyRequestReceivedAction.continueWith(interceptedRequest)
    }

    override fun handleRequestToBeSent(interceptedRequest: InterceptedRequest?): ProxyRequestToBeSentAction {
        interceptedRequest?.let {
            api.logging().logToOutput("Intercept request")
            if(it.hasParameter("data", HttpParameterType.URL)) {
                api.logging().logToOutput("has data")

                val jsonString: String = api.utilities().urlUtils().decode(it.parameterValue("data", HttpParameterType.URL))
                val json = JsonNode.jsonNode(jsonString).asObject()
                if (json.has("H")) {
                    api.logging().logToOutput("Found H")
                    val hub = json["H"].asString()
                    it.annotations().appendNotes("Hub=$hub");
                }

                if (json.has("M")) {
                    api.logging().logToOutput("Found M")
                    val method = json["M"].asString()
                    it.annotations().appendNotes("Method=$method");
                }
                api.logging().logToOutput("done appending notes")
            }
        }
        api.logging().logToOutput("returning")
        return ProxyRequestToBeSentAction.continueWith(interceptedRequest)
    }
}