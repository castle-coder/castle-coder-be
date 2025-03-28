package longrun;

public class RunpodRequest {
    private String serverEndpointId;
    private String prompt;

    // 기본 생성자, getter, setter
    public RunpodRequest() {
    }

    public String getServerEndpointId() {
        return serverEndpointId;
    }

    public void setServerEndpointId(String serverEndpointId) {
        this.serverEndpointId = serverEndpointId;
    }

    public String getPrompt() {
        return prompt;
    }

    public void setPrompt(String prompt) {
        this.prompt = prompt;
    }
}
