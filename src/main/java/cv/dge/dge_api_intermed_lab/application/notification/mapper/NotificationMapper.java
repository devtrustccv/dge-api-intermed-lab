package cv.dge.dge_api_intermed_lab.application.notification.mapper;

import cv.dge.dge_api_intermed_lab.application.notification.dto.NotificationRequestDTO;
import cv.dge.dge_api_intermed_lab.infrastructure.notification.TNotificacaoConfigEmail;
import org.springframework.stereotype.Component;

@Component
public class NotificationMapper {

    public NotificationRequestDTO toEmailRequest(TNotificacaoConfigEmail configEmail, String appName, String email) {
        NotificationRequestDTO dto = new NotificationRequestDTO();
        dto.setAppName(appName);
        dto.setAssunto(configEmail.getAssunto());
        dto.setMensagem(configEmail.getMensagem());
        dto.setEmail(email);
        return dto;
    }
}
