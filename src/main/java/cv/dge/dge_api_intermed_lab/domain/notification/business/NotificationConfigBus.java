package cv.dge.dge_api_intermed_lab.domain.notification.business;

import cv.dge.dge_api_intermed_lab.infrastructure.notification.TNotificacaoConfigEmail;
import java.util.Optional;

public interface NotificationConfigBus {

    Optional<TNotificacaoConfigEmail> findByCodigoProcessoEtapa(String codigo, String processo, String etapa, String appCode);

    Optional<TNotificacaoConfigEmail> findByCodigo(String codigo, String appCode);
}
