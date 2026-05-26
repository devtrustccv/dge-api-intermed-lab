package cv.dge.dge_api_intermed_lab.application.orientacao.service;

import cv.dge.dge_api_intermed_lab.application.orientacao.dto.RequisitoResponse;
import cv.dge.dge_api_intermed_lab.application.orientacao.mapper.OrientacaoMapper;
import cv.dge.dge_api_intermed_lab.domain.orientacao.business.OrientacaoBus;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RequisitoServiceImpl implements RequisitoService {

    private static final String ESTADO_ATIVO = "A";
    private static final String TIPO_SERVICO_SUBSIDIO_DESEMPREGO = "SUBSIDIO_DESEMPREGO";

    private final OrientacaoBus orientacaoBus;
    private final OrientacaoMapper orientacaoMapper;

    @Transactional(readOnly = true)
    public List<RequisitoResponse> listarAtivos() {
        return orientacaoBus.listarRequisitosAtivosPorTipoServico(ESTADO_ATIVO, TIPO_SERVICO_SUBSIDIO_DESEMPREGO)
                .stream()
                .map(orientacaoMapper::toRequisitoResponse)
                .toList();
    }
}
