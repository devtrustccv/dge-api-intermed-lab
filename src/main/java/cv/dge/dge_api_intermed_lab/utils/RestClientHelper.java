package cv.dge.dge_api_intermed_lab.utils;

import java.util.Map;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class RestClientHelper {

    private final RestTemplate restTemplate;

    public RestClientHelper(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public <T> ResponseEntity<T> sendRequest(
            String url,
            HttpMethod method,
            Object body,
            Class<T> responseType,
            Map<String, String> headersMap
    ) {
        HttpHeaders headers = new HttpHeaders();
        if (headersMap != null) {
            headersMap.forEach(headers::add);
        }
        return restTemplate.exchange(url, method, new HttpEntity<>(body, headers), responseType);
    }
}
