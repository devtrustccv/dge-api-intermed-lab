package cv.dge.dge_api_intermed_lab.application.orientacao.service;

import cv.dge.dge_api_intermed_lab.application.orientacao.dto.OrientacaoEntrevistaResponse;
import cv.dge.dge_api_intermed_lab.application.orientacao.mapper.OrientacaoMapper;
import cv.dge.dge_api_intermed_lab.domain.orientacao.business.OrientacaoBus;
import cv.dge.dge_api_intermed_lab.domain.orientacao.model.AcolhimentoServico;
import cv.dge.dge_api_intermed_lab.domain.orientacao.model.AgendamentoEntrevista;
import cv.dge.dge_api_intermed_lab.domain.acolhimento.model.DetalhesAcolhimento;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class OrientacaoEntrevistaServiceImpl implements OrientacaoEntrevistaService {

    private final OrientacaoBus orientacaoBus;
    private final OrientacaoMapper orientacaoMapper;

    @Transactional(readOnly = true)
    public OrientacaoEntrevistaResponse buscarPorId(Integer id) {
        AgendamentoEntrevista entrevista = orientacaoBus.findEntrevistaById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Entrevista nao encontrada."));

        DetalhesAcolhimento acolhimento = entrevista.getIdAcolhimento() == null
                ? null
                : orientacaoBus.findAcolhimentoById(entrevista.getIdAcolhimento()).orElse(null);

        AcolhimentoServico servico = buscarServicoDaEntrevista(entrevista);

        return orientacaoMapper.toEntrevistaResponse(entrevista, acolhimento, servico);
    }

    private AcolhimentoServico buscarServicoDaEntrevista(AgendamentoEntrevista entrevista) {
        String tipoServico = entrevista.getTipoServico();
        if (tipoServico != null && !tipoServico.isBlank()) {
            AcolhimentoServico servicoPorTipo = orientacaoBus
                    .findUltimoServicoByEntrevistaAndTipo(entrevista.getId(), tipoServico)
                    .orElse(null);
            if (servicoPorTipo != null) {
                return servicoPorTipo;
            }
        }

        return orientacaoBus.findUltimoServicoByEntrevista(entrevista.getId())
                .orElse(null);
    }

}
