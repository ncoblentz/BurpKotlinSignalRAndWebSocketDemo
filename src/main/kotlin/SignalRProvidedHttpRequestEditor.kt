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

    init {
        if(creationContext?.editorMode()?.equals(EditorMode.READ_ONLY) == true)
            signalrDataEditor = api.userInterface().createRawEditor(EditorOptions.READ_ONLY)
        else
            signalrDataEditor = api.userInterface().createRawEditor()
    }

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
        signalrDataEditor.contents = burp.api.montoya.core.ByteArray.byteArray(data)
    }

    override fun isEnabledFor(httpRequestResponse: HttpRequestResponse?): Boolean {
        httpRequestResponse?.request()?.let {
            return it.hasParameter("transport",HttpParameterType.URL) &&
                    it.parameterValue("transport",HttpParameterType.URL)=="longPolling" &&
                    it.hasParameter("data",HttpParameterType.URL)
        }
        return false
    }

    override fun caption(): String {
        return "SignalR Data"
    }

    override fun uiComponent(): Component {
        return signalrDataEditor.uiComponent()
    }

    override fun selectedData(): Selection? {
        return if (signalrDataEditor.selection().isPresent) signalrDataEditor.selection().get() else null

    }

    override fun isModified(): Boolean {
        return signalrDataEditor.isModified
    }

    override fun getRequest(): HttpRequest {

        val request: HttpRequest?

        if (signalrDataEditor.isModified) {
            val modifiedData = api.utilities().urlUtils().encode(signalrDataEditor.contents).toString()
            request = httpRequestResponse?.request()?.withUpdatedParameters(HttpParameter.parameter("data",modifiedData,HttpParameterType.URL))
        }
        else
            request=httpRequestResponse?.request()

        return request!!
    }

}
