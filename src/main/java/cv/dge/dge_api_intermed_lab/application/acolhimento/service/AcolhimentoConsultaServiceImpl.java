package cv.dge.dge_api_intermed_lab.application.acolhimento.service;

import cv.dge.dge_api_intermed_lab.application.acolhimento.dto.AcolhimentoCompletoResponse;
import cv.dge.dge_api_intermed_lab.application.acolhimento.dto.AcolhimentoDadosEmpregoResponse;
import cv.dge.dge_api_intermed_lab.application.acolhimento.dto.AcolhimentoEntidadeResponse;
import cv.dge.dge_api_intermed_lab.application.acolhimento.dto.AcolhimentoPessoaResponse;
import cv.dge.dge_api_intermed_lab.application.acolhimento.dto.UtenteResponse;
import cv.dge.dge_api_intermed_lab.application.acolhimento.mapper.AcolhimentoMapper;
import cv.dge.dge_api_intermed_lab.application.document.dto.DocumentoResponseDTO;
import cv.dge.dge_api_intermed_lab.application.document.service.DocumentService;
import cv.dge.dge_api_intermed_lab.application.geografia.service.GlobalGeografiaService;
import cv.dge.dge_api_intermed_lab.application.orientacao.dto.OrientacaoEntrevistaResponse;
import cv.dge.dge_api_intermed_lab.application.orientacao.dto.OrientacaoServicoResponse;
import cv.dge.dge_api_intermed_lab.application.orientacao.mapper.OrientacaoMapper;
import cv.dge.dge_api_intermed_lab.domain.acolhimento.business.AcolhimentoBus;
import cv.dge.dge_api_intermed_lab.domain.orientacao.model.AcolhimentoServico;
import cv.dge.dge_api_intermed_lab.domain.orientacao.model.AgendamentoEntrevista;
import cv.dge.dge_api_intermed_lab.domain.acolhimento.model.Cefp;
import cv.dge.dge_api_intermed_lab.domain.acolhimento.model.DetalhesAcolhimento;
import cv.dge.dge_api_intermed_lab.domain.acolhimento.model.Entidade;
import cv.dge.dge_api_intermed_lab.domain.acolhimento.model.Utente;
import java.text.Normalizer;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class AcolhimentoConsultaServiceImpl implements AcolhimentoConsultaService {

    private static final String MSG_ID_PESSOA_OBRIGATORIO = "idPessoa e obrigatorio.";
    private static final String MSG_ID_ENTIDADE_OBRIGATORIO = "globalIdEntidade e obrigatorio.";
    private static final String MSG_ACOLHIMENTO_NAO_ENCONTRADO = "Nenhum acolhimento encontrado para o idPessoa informado.";
    private static final String MSG_ACOLHIMENTO_ENTIDADE_NAO_ENCONTRADO = "Nenhum acolhimento encontrado para o globalIdEntidade informado.";

    private final AcolhimentoBus acolhimentoBus;
    private final AcolhimentoMapper acolhimentoMapper;
    private final OrientacaoMapper orientacaoMapper;
    private final DocumentService documentService;
    private final GlobalGeografiaService globalGeografiaService;

    @Value("${document.acolhimento.tipo-relacao:acolhimento}")
    private String tipoRelacaoDocumentoAcolhimento;

    @Value("${document.acolhimento.app-code:emprego}")
    private String appCodeDocumentoAcolhimento;

    @Override
    @Transactional(readOnly = true)
    public List<UtenteResponse> listarUtentes() {
        return acolhimentoBus.findAllUtentes().stream()
                .map(acolhimentoMapper::toUtenteResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public AcolhimentoPessoaResponse buscarPorIdPessoa(Integer idPessoa) {
        if (idPessoa == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, MSG_ID_PESSOA_OBRIGATORIO);
        }

        List<DetalhesAcolhimento> acolhimentos = acolhimentoBus.findAcolhimentosByPessoa(idPessoa);

        if (acolhimentos.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, MSG_ACOLHIMENTO_NAO_ENCONTRADO);
        }

        List<AcolhimentoCompletoResponse> respostas = acolhimentos.stream()
                .map(this::mapearAcolhimentoCompleto)
                .toList();

        return acolhimentoMapper.toPessoaResponse(idPessoa, respostas);
    }

    @Transactional(readOnly = true)
    public AcolhimentoEntidadeResponse buscarPorIdEntidade(Integer globalIdEntidade) {
        if (globalIdEntidade == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, MSG_ID_ENTIDADE_OBRIGATORIO);
        }

        List<Integer> idsEntidade = acolhimentoBus.findEntidadesByGlobalId(globalIdEntidade).stream()
                .map(Entidade::getId)
                .toList();

        if (idsEntidade.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, MSG_ACOLHIMENTO_ENTIDADE_NAO_ENCONTRADO);
        }

        List<DetalhesAcolhimento> acolhimentos = acolhimentoBus.findAcolhimentosByEntidades(idsEntidade);

        if (acolhimentos.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, MSG_ACOLHIMENTO_ENTIDADE_NAO_ENCONTRADO);
        }

        List<AcolhimentoCompletoResponse> respostas = acolhimentos.stream()
                .map(this::mapearAcolhimentoCompleto)
                .toList();

        return acolhimentoMapper.toEntidadeResponse(globalIdEntidade, respostas);
    }

    private AcolhimentoCompletoResponse mapearAcolhimentoCompleto(DetalhesAcolhimento acolhimento) {
        Map<String, Object> detalhes = normalizarDetalhesGeografia(acolhimento.getDetalhes());
        Cefp cefp = resolverCefpDoAcolhimento(acolhimento);
        Utente utente = resolverUtenteDoAcolhimento(acolhimento);
        Entidade entidade = resolverEntidadeDoAcolhimento(acolhimento);
        var dadosEmprego = resolverDadosEmprego(acolhimento);
        AgendamentoEntrevista entrevista = acolhimentoBus
                .findUltimaEntrevistaByAcolhimento(acolhimento.getId())
                .orElse(null);
        AcolhimentoServico servico = entrevista == null ? null : resolverServico(acolhimento, entrevista);
        OrientacaoServicoResponse servicoResponse = orientacaoMapper.toServicoResponse(servico);
        OrientacaoEntrevistaResponse entrevistaResponse = orientacaoMapper.toEntrevistaResponse(entrevista, null, servico);
        List<DocumentoResponseDTO> documentos = documentService.getDocumentosPorRelacao(
                acolhimento.getId(),
                tipoRelacaoDocumentoAcolhimento,
                appCodeDocumentoAcolhimento
        );

        return acolhimentoMapper.toCompletoResponse(
                acolhimento,
                cefp,
                utente,
                entidade,
                dadosEmprego,
                entrevistaResponse,
                documentos,
                detalhes
        );
    }

    private AcolhimentoDadosEmpregoResponse resolverDadosEmprego(DetalhesAcolhimento acolhimento) {
        if (acolhimento.getIdPessoa() == null) {
            return null;
        }
        return acolhimentoBus
                .findUltimoEmpregoByPessoa(acolhimento.getIdPessoa())
                .map(acolhimentoMapper::toDadosEmpregoResponse)
                .orElse(null);
    }

    private Map<String, Object> normalizarDetalhesGeografia(Map<String, Object> detalhes) {
        if (detalhes == null) {
            return null;
        }
        return mapaComNomesGeografia(detalhes, new HashMap<>());
    }

    private Map<String, Object> mapaComNomesGeografia(Map<?, ?> origem, Map<String, String> cache) {
        Map<String, Object> destino = new LinkedHashMap<>();
        origem.forEach((chave, valor) -> {
            if (chave != null) {
                String nomeChave = chave.toString();
                destino.put(nomeChave, valorComNomeGeografia(nomeChave, valor, cache));
            }
        });
        return destino;
    }

    private Object valorComNomeGeografia(String chave, Object valor, Map<String, String> cache) {
        if (valor instanceof Map<?, ?> mapa) {
            return mapaComNomesGeografia(mapa, cache);
        }
        if (valor instanceof Collection<?> colecao) {
            return colecao.stream()
                    .map(item -> valorComNomeGeografia(chave, item, cache))
                    .toList();
        }
        if (!chaveGeografica(chave)) {
            return valor;
        }

        String nome = resolverNomeGeografia(valor == null ? null : valor.toString(), chave, cache);
        return nome == null || nome.isBlank() ? valor : nome;
    }

    private boolean chaveGeografica(String chave) {
        String normalizada = normalizar(chave);
        return normalizada.contains("pais")
                || normalizada.contains("ilha")
                || normalizada.contains("concelho")
                || normalizada.contains("zona")
                || normalizada.contains("naturalidade");
    }

    private String resolverNomeGeografia(String codigo, String chave, Map<String, String> cache) {
        if (codigo == null || codigo.isBlank()) {
            return null;
        }

        String codigoLimpo = codigo.trim();
        String chaveCache = normalizar(chave) + ":" + codigoLimpo;
        if (cache.containsKey(chaveCache)) {
            return cache.get(chaveCache);
        }

        String nome = globalGeografiaService.buscarNomePorCodigo(codigoLimpo).orElse(null);
        cache.put(chaveCache, nome);
        return nome;
    }

    private String normalizar(String valor) {
        if (valor == null) {
            return "";
        }
        String semAcentos = Normalizer.normalize(valor, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "");
        return semAcentos.toLowerCase(Locale.ROOT);
    }

    private AcolhimentoServico resolverServico(DetalhesAcolhimento acolhimento, AgendamentoEntrevista entrevista) {
        if (entrevista != null) {
            return buscarServicoDaEntrevista(entrevista)
                    .orElse(null);
        }
        return buscarServicoPorAcolhimento(acolhimento);
    }

    private Optional<AcolhimentoServico> buscarServicoDaEntrevista(AgendamentoEntrevista entrevista) {
        String tipoServico = entrevista.getTipoServico();
        if (tipoServico != null && !tipoServico.isBlank()) {
            Optional<AcolhimentoServico> servicoPorTipo = acolhimentoBus
                    .findUltimoServicoByEntrevistaAndTipo(entrevista.getId(), tipoServico);
            if (servicoPorTipo.isPresent()) {
                return servicoPorTipo;
            }
        }

        return acolhimentoBus.findUltimoServicoByEntrevista(entrevista.getId());
    }

    private AcolhimentoServico buscarServicoPorAcolhimento(DetalhesAcolhimento acolhimento) {
        return acolhimentoBus.findUltimoServicoByAcolhimento(acolhimento.getId()).orElse(null);
    }

    private Cefp resolverCefpDoAcolhimento(DetalhesAcolhimento acolhimento) {
        if (acolhimento.getCefpId() != null) {
            return acolhimentoBus.findCefpById(acolhimento.getCefpId()).orElse(null);
        }
        if (acolhimento.getOrgId() != null) {
            return acolhimentoBus.findCefpByOrganizationId(acolhimento.getOrgId()).orElse(null);
        }
        return null;
    }

    private Utente resolverUtenteDoAcolhimento(DetalhesAcolhimento acolhimento) {
        if (acolhimento.getIdUtente() == null) {
            return null;
        }
        return acolhimentoBus.findUtenteById(acolhimento.getIdUtente()).orElse(null);
    }

    private Entidade resolverEntidadeDoAcolhimento(DetalhesAcolhimento acolhimento) {
        if (acolhimento.getIdEntidade() == null) {
            return null;
        }
        return acolhimentoBus.findEntidadeById(acolhimento.getIdEntidade()).orElse(null);
    }

}
