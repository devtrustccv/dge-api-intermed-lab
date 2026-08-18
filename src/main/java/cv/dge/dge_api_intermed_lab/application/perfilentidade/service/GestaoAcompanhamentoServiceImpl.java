package cv.dge.dge_api_intermed_lab.application.perfilentidade.service;

import cv.dge.dge_api_intermed_lab.application.perfilentidade.dto.AcompanhamentoEstagiarioFiltro;
import cv.dge.dge_api_intermed_lab.application.perfilentidade.dto.AcompanhamentoEstagiarioListaResponse;
import cv.dge.dge_api_intermed_lab.application.perfilentidade.dto.AcompanhamentoEstagiarioSelectResponse;
import cv.dge.dge_api_intermed_lab.application.perfilentidade.dto.AcompanhamentoOfertaSelectResponse;
import cv.dge.dge_api_intermed_lab.application.perfilentidade.enums.EmpregoDominio;
import cv.dge.dge_api_intermed_lab.infrastructure.perfilentidade.repository.GestaoAcompanhamentoRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class GestaoAcompanhamentoServiceImpl implements GestaoAcompanhamentoService {

    private final GestaoAcompanhamentoRepository acompanhamentoRepository;

    @Override
    @Transactional(readOnly = true)
    public List<AcompanhamentoEstagiarioListaResponse> listarEstagiariosSelecionados(
            AcompanhamentoEstagiarioFiltro filtro
    ) {
        return acompanhamentoRepository.listarEstagiariosSelecionados(filtro).stream()
                .map(this::enriquecer)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<AcompanhamentoEstagiarioSelectResponse> listarEstagiariosSelecionadosParaFiltro() {
        return acompanhamentoRepository.listarEstagiariosSelecionadosParaFiltro();
    }

    @Override
    @Transactional(readOnly = true)
    public List<AcompanhamentoOfertaSelectResponse> listarOfertasComEstagiariosSelecionados() {
        return acompanhamentoRepository.listarOfertasComEstagiariosSelecionados();
    }

    private AcompanhamentoEstagiarioListaResponse enriquecer(AcompanhamentoEstagiarioListaResponse item) {
        return new AcompanhamentoEstagiarioListaResponse(
                item.candidaturaId(),
                item.estagiarioId(),
                item.estagiario(),
                item.ofertaId(),
                item.oferta(),
                item.entrevistaId(),
                valorDominio(EmpregoDominio.DOMINIO_PARECER_ENTREVISTA, item.parecerEntrevista()),
                EmpregoDominio.descricao(EmpregoDominio.DOMINIO_PARECER_ENTREVISTA, item.parecerEntrevista()),
                valorDominio(EmpregoDominio.DOMINIO_ESTADO_ENTREVISTA, item.estadoEntrevista()),
                EmpregoDominio.descricao(EmpregoDominio.DOMINIO_ESTADO_ENTREVISTA, item.estadoEntrevista()),
                item.colocacaoId(),
                item.colocacaoId() != null
        );
    }

    private String valorDominio(String dominio, String valor) {
        return EmpregoDominio.valorOficial(dominio, valor).orElse(valor);
    }
}
