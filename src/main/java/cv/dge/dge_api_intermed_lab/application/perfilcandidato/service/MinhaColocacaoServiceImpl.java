package cv.dge.dge_api_intermed_lab.application.perfilcandidato.service;

import cv.dge.dge_api_intermed_lab.application.document.service.DocumentService;
import cv.dge.dge_api_intermed_lab.application.perfilcandidato.dto.MinhaColocacaoDetalheResponse;
import cv.dge.dge_api_intermed_lab.application.perfilcandidato.dto.MinhaColocacaoListaResponse;
import cv.dge.dge_api_intermed_lab.application.perfilentidade.enums.EmpregoDominio;
import cv.dge.dge_api_intermed_lab.infrastructure.perfilcandidato.repository.MinhaColocacaoRepository;
import cv.dge.dge_api_intermed_lab.infrastructure.perfilcandidato.repository.MinhaColocacaoRepository.ColocacaoRegisto;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class MinhaColocacaoServiceImpl implements MinhaColocacaoService {

    private final MinhaColocacaoRepository colocacaoRepository;
    private final DocumentService documentService;

    @Override
    @Transactional(readOnly = true)
    public List<MinhaColocacaoListaResponse> listar(Long pessoaId) {
        validarPessoa(pessoaId);
        return colocacaoRepository.listar(pessoaId).stream()
                .map(this::mapearLista)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public MinhaColocacaoDetalheResponse buscarPorId(Integer colocacaoId, Long pessoaId) {
        validarPessoa(pessoaId);
        validarColocacao(colocacaoId);
        ColocacaoRegisto colocacao = colocacaoRepository.buscarPorId(colocacaoId, pessoaId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "A colocação selecionada não existe ou não pertence ao candidato. Atualize a lista e tente novamente."
                ));
        return mapearDetalhe(colocacao);
    }

    private MinhaColocacaoListaResponse mapearLista(ColocacaoRegisto colocacao) {
        String tipoOferta = normalizarValor(EmpregoDominio.DOMINIO_TIPO_OFERTA, colocacao.tipoOferta());
        return new MinhaColocacaoListaResponse(
                colocacao.colocacaoId(),
                colocacao.ofertaId(),
                tipoOferta,
                EmpregoDominio.descricao(EmpregoDominio.DOMINIO_TIPO_OFERTA, tipoOferta),
                colocacao.titulo(),
                colocacao.codigoReferencia(),
                colocacao.dataColocacao(),
                colocacao.contratoPath(),
                gerarContratoUrl(colocacao.contratoPath())
        );
    }

    private MinhaColocacaoDetalheResponse mapearDetalhe(ColocacaoRegisto colocacao) {
        String tipoOferta = normalizarValor(EmpregoDominio.DOMINIO_TIPO_OFERTA, colocacao.tipoOferta());
        String tipoContrato = normalizarValor(EmpregoDominio.DOMINIO_REGIME_CONTRATO, colocacao.tipoContrato());
        String estado = normalizarValor(EmpregoDominio.DOMINIO_ESTADO, colocacao.estado());
        return new MinhaColocacaoDetalheResponse(
                colocacao.colocacaoId(),
                colocacao.ofertaId(),
                tipoOferta,
                EmpregoDominio.descricao(EmpregoDominio.DOMINIO_TIPO_OFERTA, tipoOferta),
                colocacao.titulo(),
                colocacao.codigoReferencia(),
                colocacao.dataInicioPrevisto(),
                colocacao.dataFimPrevisto(),
                tipoContrato,
                EmpregoDominio.descricao(EmpregoDominio.DOMINIO_REGIME_CONTRATO, tipoContrato),
                colocacao.duracaoContrato(),
                colocacao.descricao(),
                estado,
                EmpregoDominio.descricao(EmpregoDominio.DOMINIO_ESTADO, estado),
                colocacao.dataColocacao(),
                colocacao.contratoPath(),
                gerarContratoUrl(colocacao.contratoPath())
        );
    }

    private String gerarContratoUrl(String contratoPath) {
        if (!temTexto(contratoPath)) {
            return null;
        }
        return documentService.gerarLinkPublico(contratoPath.trim());
    }

    private String normalizarValor(String dominio, String valor) {
        if (!temTexto(valor)) {
            return valor;
        }
        return EmpregoDominio.valorOficial(dominio, valor)
                .orElseGet(() -> EmpregoDominio.normalizar(valor));
    }

    private void validarPessoa(Long pessoaId) {
        if (pessoaId == null || pessoaId <= 0) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Não foi possível identificar o candidato. Atualize a página, entre novamente e tente de novo."
            );
        }
    }

    private void validarColocacao(Integer colocacaoId) {
        if (colocacaoId == null || colocacaoId <= 0) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Não foi possível identificar a colocação selecionada. Atualize a lista e tente novamente."
            );
        }
    }

    private boolean temTexto(String valor) {
        return valor != null && !valor.trim().isEmpty();
    }
}
