package cv.dge.dge_api_intermed_lab.domain.notification.business;

import cv.dge.dge_api_intermed_lab.infrastructure.notification.TNotificacaoConfigEmail;
import cv.dge.dge_api_intermed_lab.infrastructure.notification.repository.NotificacaoConfigEmailRepository;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class NotificationConfigBusImpl implements NotificationConfigBus {

    private final NotificacaoConfigEmailRepository repository;

    @Override
    public Optional<TNotificacaoConfigEmail> findByCodigoProcessoEtapa(
            String codigo,
            String processo,
            String etapa,
            String appCode
    ) {
        return repository.findFirstByCodigoAndAppCodeAndProcessoCodeAndEtapaCode(
                codigo, appCode, processo, etapa);
    }

    @Override
    public Optional<TNotificacaoConfigEmail> findByCodigo(String codigo, String appCode) {
        return repository.findFirstByCodigoAndAppCode(codigo, appCode);
    }
}
