package cv.dge.dge_api_intermed_lab.application.notification.service;

import cv.dge.dge_api_intermed_lab.utils.RestClientHelper;
import cv.dge.dge_api_intermed_lab.application.notification.dto.DefaultReponseDTO;
import cv.dge.dge_api_intermed_lab.application.notification.dto.NotificationRequestDTO;
import cv.dge.dge_api_intermed_lab.domain.notification.business.NotificationConfigBus;
import cv.dge.dge_api_intermed_lab.infrastructure.notification.TNotificacaoConfigEmail;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.util.HashMap;
import java.util.Map;

@Service
public class NotificationServiceImpl implements NotificationService {

    private final RestClientHelper restClientHelper;

    private final NotificationConfigBus notificationConfigBus;

    @Value("${api.base.service.url}")
    private String notificationBaseUrl;

    public NotificationServiceImpl(RestClientHelper restClientHelper, NotificationConfigBus notificationConfigBus) {
        this.restClientHelper = restClientHelper;
        this.notificationConfigBus = notificationConfigBus;
    }

    public DefaultReponseDTO enviarEmail(NotificationRequestDTO dto) {
        String url = notificationBaseUrl + "/notification";

        // Construir corpo multipart
        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("appName", dto.getAppName());
        body.add("Subject", dto.getAssunto());
        body.add("message", dto.getMensagem());
        body.add("email", dto.getEmail());
        adicionarSePreenchido(body, "tipoProcesso", dto.getTipoProcesso());
        adicionarSePreenchido(body, "idProcesso", dto.getIdProcesso());
        adicionarSePreenchido(body, "tipoRelacao", dto.getTipoRelacao());
        adicionarSePreenchido(body, "idRelacao", dto.getIdRelacao());
        adicionarSePreenchido(body, "isAlert", dto.getIsAlert());




        Map<String, String> headers = new HashMap<>();
        headers.put(HttpHeaders.CONTENT_TYPE, MediaType.MULTIPART_FORM_DATA_VALUE);

        // Enviar a requisição
        ResponseEntity<DefaultReponseDTO> response = restClientHelper.sendRequest(
                url,
                HttpMethod.POST,
                body,
                DefaultReponseDTO.class,
                headers
        );

        return response.getBody();
    }

    private void adicionarSePreenchido(MultiValueMap<String, Object> body, String campo, String valor) {
        if (valor != null && !valor.isBlank()) {
            body.add(campo, valor);
        }
    }

    public TNotificacaoConfigEmail loadConfigNotification(String codigo, String processo, String etapa, String appCode) {

        if (processo != null && etapa != null) {
            return notificationConfigBus.findByCodigoProcessoEtapa(codigo, processo, etapa, appCode).orElse(null);
        }

        return notificationConfigBus.findByCodigo(codigo, appCode).orElse(null);
    }

}
