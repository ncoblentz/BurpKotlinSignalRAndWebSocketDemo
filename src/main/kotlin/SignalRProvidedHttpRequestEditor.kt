import burp.api.montoya.MontoyaApi
import burp.api.montoya.http.message.HttpRequestResponse
import burp.api.montoya.http.message.params.HttpParameter
import burp.api.montoya.http.message.params.HttpParameterType
import burp.api.montoya.http.message.requests.HttpRequest
import burp.api.montoya.ui.Selection
import burp.api.montoya.ui.editor.EditorOptions
import burp.api.montoya.ui.editor.RawEditor
import burp.api.montoya.ui.editor.extension.EditorCreationContext
import burp.api.montoya.ui.editor.extension.EditorMode
import burp.api.montoya.ui.editor.extension.ExtensionProvidedHttpRequestEditor
import org.json.JSONObject
import java.awt.Component

class SignalRProvidedHttpRequestEditor(private val api: MontoyaApi, creationContext: EditorCreationContext?) : ExtensionProvidedHttpRequestEditor {

    private var signalrDataEditor: RawEditor
    private var data = ""
    private var httpRequestResponse : HttpRequestResponse? = null


    // Check to see if the editor should allow modification of the content (proxy vs. repeater for example)
    init {
        if(creationContext?.editorMode()?.equals(EditorMode.READ_ONLY) == true)
            signalrDataEditor = api.userInterface().createRawEditor(EditorOptions.READ_ONLY)
        else
            signalrDataEditor = api.userInterface().createRawEditor()
    }

    // Save the latest HTTP request and response in class instance variables
    // Parse and beautify the `data` parameter if present and store it in a class instance variable
    // Set the HTTP editor's content with that value
    override fun setRequestResponse(newHttpRequestResponse: HttpRequestResponse?) {
        httpRequestResponse = newHttpRequestResponse
        data=""
        httpRequestResponse?.request()?.let {
            api.logging().logToOutput("found request")
            api.logging().logToOutput("raw data:" + it.parameterValue("data",HttpParameterType.URL))
            data = JSONObject(api.utilities().urlUtils().decode(it.parameterValue("data",HttpParameterType.URL))).toString(4)
            api.logging().logToOutput("JSON Data: $data")
        }
        api.logging().logToOutput("Exited looking for data")

        // Set the editor content's data here
        signalrDataEditor.contents = burp.api.montoya.core.ByteArray.byteArray(data)
    }

    // When should we show the text editor. The criteria below checks:
    // - the HTTP request isn't null
    // - it includes a "transport" parameter with value "longPolling"
    // - it has a "data" parameter
    override fun isEnabledFor(httpRequestResponse: HttpRequestResponse?): Boolean {
        httpRequestResponse?.request()?.let {
            return it.hasParameter("transport",HttpParameterType.URL) &&
                    it.parameterValue("transport",HttpParameterType.URL)=="longPolling" &&
                    it.hasParameter("data",HttpParameterType.URL)
        }
        return false
    }

    // Set the name of the tab
    override fun caption(): String {
        return "SignalR Data"
    }

    // Return the Swing Component to Burp
    override fun uiComponent(): Component {
        return signalrDataEditor.uiComponent()
    }

    // Provide the selected (highlighted) data when asked for
    override fun selectedData(): Selection? {
        return if (signalrDataEditor.selection().isPresent) signalrDataEditor.selection().get() else null

    }

    // Did the user modify the content inside the text editor?
    override fun isModified(): Boolean {
        return signalrDataEditor.isModified
    }

    // When it's time to send the request or a user clicks on another tab, we need to process any changes and update the HTTP request
    override fun getRequest(): HttpRequest? {

        val request: HttpRequest?

        if (signalrDataEditor.isModified) {
            val modifiedData = api.utilities().urlUtils().encode(signalrDataEditor.contents).toString()
            request = httpRequestResponse?.request()?.withUpdatedParameters(HttpParameter.parameter("data",modifiedData,HttpParameterType.URL))
        }
        else
            request=httpRequestResponse?.request()


        return request
    }

}
