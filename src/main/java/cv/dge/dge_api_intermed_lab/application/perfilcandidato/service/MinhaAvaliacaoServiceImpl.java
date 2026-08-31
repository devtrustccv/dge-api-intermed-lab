package cv.dge.dge_api_intermed_lab.application.perfilcandidato.service;

import cv.dge.dge_api_intermed_lab.application.perfilcandidato.dto.MinhaAvaliacaoDesempenhoResponse;
import cv.dge.dge_api_intermed_lab.application.perfilcandidato.dto.MinhaAvaliacaoDetalheResponse;
import cv.dge.dge_api_intermed_lab.application.perfilcandidato.dto.MinhaAvaliacaoListaResponse;
import cv.dge.dge_api_intermed_lab.application.perfilentidade.enums.EmpregoDominio;
import cv.dge.dge_api_intermed_lab.infrastructure.perfilcandidato.repository.MinhaAvaliacaoRepository;
import cv.dge.dge_api_intermed_lab.infrastructure.perfilcandidato.repository.MinhaAvaliacaoRepository.AvaliacaoDesempenhoRegisto;
import cv.dge.dge_api_intermed_lab.infrastructure.perfilcandidato.repository.MinhaAvaliacaoRepository.AvaliacaoDetalheRegisto;
import cv.dge.dge_api_intermed_lab.infrastructure.perfilcandidato.repository.MinhaAvaliacaoRepository.AvaliacaoListaRegisto;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class MinhaAvaliacaoServiceImpl implements MinhaAvaliacaoService {

    private final MinhaAvaliacaoRepository avaliacaoRepository;

    @Override
    @Transactional(readOnly = true)
    public List<MinhaAvaliacaoListaResponse> listar(Long pessoaId) {
        validarPessoa(pessoaId);
        return avaliacaoRepository.listar(pessoaId).stream()
                .map(this::mapearLista)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public MinhaAvaliacaoDetalheResponse buscarPorId(Integer avaliacaoId, Long pessoaId) {
        validarPessoa(pessoaId);
        validarAvaliacao(avaliacaoId);
        AvaliacaoDetalheRegisto avaliacao = avaliacaoRepository.buscarPorId(avaliacaoId, pessoaId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "A avaliação selecionada não existe ou não pertence ao candidato. Atualize a lista e tente novamente."
                ));
        return mapearDetalhe(avaliacao);
    }

    private MinhaAvaliacaoListaResponse mapearLista(AvaliacaoListaRegisto avaliacao) {
        String tipoAvaliacao = normalizarDominio(
                EmpregoDominio.DOMINIO_TIPO_AVALIACAO,
                avaliacao.tipoAvaliacao()
        );
        return new MinhaAvaliacaoListaResponse(
                avaliacao.avaliacaoId(),
                tipoAvaliacao,
                EmpregoDominio.descricao(EmpregoDominio.DOMINIO_TIPO_AVALIACAO, tipoAvaliacao),
                avaliacao.periodoReferencia(),
                avaliacao.classificacao(),
                avaliacao.dataRegisto()
        );
    }

    private MinhaAvaliacaoDetalheResponse mapearDetalhe(AvaliacaoDetalheRegisto avaliacao) {
        String tipoAvaliacao = normalizarDominio(
                EmpregoDominio.DOMINIO_TIPO_AVALIACAO,
                avaliacao.tipoAvaliacao()
        );
        String grauSatisfacao = normalizarDominio(
                EmpregoDominio.DOMINIO_GRAU_SATISFACAO,
                avaliacao.grauSatisfacao()
        );
        return new MinhaAvaliacaoDetalheResponse(
                avaliacao.avaliacaoId(),
                tipoAvaliacao,
                EmpregoDominio.descricao(EmpregoDominio.DOMINIO_TIPO_AVALIACAO, tipoAvaliacao),
                avaliacao.periodoReferencia(),
                avaliacao.avaliacaoDesempenho().stream()
                        .map(this::mapearDesempenho)
                        .toList(),
                grauSatisfacao,
                EmpregoDominio.descricao(EmpregoDominio.DOMINIO_GRAU_SATISFACAO, grauSatisfacao),
                avaliacao.interesseContratacao(),
                avaliacao.classificacao(),
                avaliacao.observacao(),
                avaliacao.dataRegisto()
        );
    }

    private MinhaAvaliacaoDesempenhoResponse mapearDesempenho(AvaliacaoDesempenhoRegisto desempenho) {
        String tipoCompetencia = normalizarDominio(
                EmpregoDominio.DOMINIO_TIPO_COMPETENCIA,
                desempenho.tipoCompetencia()
        );
        String avaliacao = normalizarDominio(EmpregoDominio.DOMINIO_AVALIACAO, desempenho.avaliacao());
        return new MinhaAvaliacaoDesempenhoResponse(
                tipoCompetencia,
                EmpregoDominio.descricao(EmpregoDominio.DOMINIO_TIPO_COMPETENCIA, tipoCompetencia),
                avaliacao,
                EmpregoDominio.descricao(EmpregoDominio.DOMINIO_AVALIACAO, avaliacao)
        );
    }

    private String normalizarDominio(String dominio, String valor) {
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

    private void validarAvaliacao(Integer avaliacaoId) {
        if (avaliacaoId == null || avaliacaoId <= 0) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Não foi possível identificar a avaliação selecionada. Atualize a lista e tente novamente."
            );
        }
    }

    private boolean temTexto(String valor) {
        return valor != null && !valor.trim().isEmpty();
    }
}
