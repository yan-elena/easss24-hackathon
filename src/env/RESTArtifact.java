import cartago.Artifact;
import cartago.OPERATION;
import cartago.OpFeedbackParam;
import web.HttpClient;
import web.HttpRequest;
import web.HttpResponse;
import web.RequestObject;
import web.WebResponse;
import web.WebUtils;

public class RESTArtifact extends Artifact {
    @OPERATION
    void postRequest(String uri, String body, String mediaType, OpFeedbackParam<Integer> code, OpFeedbackParam<String> content) {
        RequestObject requestObject = new RequestObject();
        requestObject.method="POST";
        requestObject.url=uri;
        requestObject.content=body;
        requestObject.type=mediaType;
        try {
            WebResponse response = WebUtils.sendRequest(requestObject);
            code.set(response.getCode());
            content.set(response.getContent());
        } catch (Throwable th) {
            th.printStackTrace();
            System.exit(0);
        }
    }

    private HttpClient client = HttpClient.newHttpClient();

    @OPERATION
    void execute(HttpRequest request, OpFeedbackParam<HttpResponse> response) {
        response.set(client.execute(request));
    }

    @OPERATION
    void getRequest(String uri, String mediaType, OpFeedbackParam<Integer> code, OpFeedbackParam<String> content) {
        RequestObject requestObject = new RequestObject();
        requestObject.method="GET";
        requestObject.url=uri;
        requestObject.content=null;
        requestObject.type=mediaType;
        WebResponse response = WebUtils.sendRequest(requestObject);
        code.set(response.getCode());
        content.set(response.getContent());
    }

    @OPERATION
    void putRequest(String uri, String body, String mediaType, OpFeedbackParam<Integer> code, OpFeedbackParam<String> content) {
        RequestObject requestObject = new RequestObject();
        requestObject.method="PUT";
        requestObject.url=uri;
        requestObject.content=body;
        requestObject.type=mediaType;
        WebResponse response = WebUtils.sendRequest(requestObject);
        code.set(response.getCode());
        content.set(response.getContent());
    }

}