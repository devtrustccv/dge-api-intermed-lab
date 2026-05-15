package cv.dge.dge_api_intermed_lab.application.orientacao.service;

import cv.dge.dge_api_intermed_lab.application.orientacao.dto.RequisitoResponse;
import cv.dge.dge_api_intermed_lab.domain.acolhimento.model.Requisito;
import cv.dge.dge_api_intermed_lab.infrastructure.acolhimento.repository.RequisitoRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RequisitoService {

    private static final String ESTADO_ATIVO = "A";

    private final RequisitoRepository requisitoRepository;

    @Transactional(readOnly = true)
    public List<RequisitoResponse> listarAtivos() {
        return requisitoRepository.findAllByEstadoIgnoreCaseOrderByDateCreateDescIdDesc(ESTADO_ATIVO).stream()
                .map(this::toResponse)
                .toList();
    }

    private RequisitoResponse toResponse(Requisito requisito) {
        return new RequisitoResponse(
                requisito.getId(),
                requisito.getRequisito(),
                requisito.getTipoServico(),
                requisito.getDateCreate(),
                requisito.getUserCreate(),
                requisito.getEstado(),
                requisito.getDateUpdate(),
                requisito.getUserUpdate()
        );
    }
}
