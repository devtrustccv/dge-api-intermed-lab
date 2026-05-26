package cv.dge.dge_api_intermed_lab.application.notification.service;

import cv.dge.dge_api_intermed_lab.application.notification.dto.DefaultReponseDTO;
import cv.dge.dge_api_intermed_lab.application.notification.dto.NotificationRequestDTO;
import cv.dge.dge_api_intermed_lab.infrastructure.notification.TNotificacaoConfigEmail;

public interface NotificationService {

    DefaultReponseDTO enviarEmail(NotificationRequestDTO dto);

    TNotificacaoConfigEmail loadConfigNotification(String codigo, String processo, String etapa, String appCode);
}
