package cv.dge.dge_api_intermed_lab.application.acolhimento.service;

import cv.dge.dge_api_intermed_lab.application.acolhimento.dto.AcolhimentoCompletoResponse;
import cv.dge.dge_api_intermed_lab.application.acolhimento.dto.AcolhimentoDadosEmpregoResponse;
import cv.dge.dge_api_intermed_lab.application.acolhimento.dto.AcolhimentoEntidadeResponse;
import cv.dge.dge_api_intermed_lab.application.acolhimento.dto.AcolhimentoPessoaResponse;
import cv.dge.dge_api_intermed_lab.application.acolhimento.dto.CefpReporterDTO;
import cv.dge.dge_api_intermed_lab.application.acolhimento.dto.EntidadeReporterDTO;
import cv.dge.dge_api_intermed_lab.application.acolhimento.dto.UtenteReporterDTO;
import cv.dge.dge_api_intermed_lab.application.document.dto.DocumentoResponseDTO;
import cv.dge.dge_api_intermed_lab.application.document.service.DocumentService;
import cv.dge.dge_api_intermed_lab.application.geografia.service.GlobalGeografiaService;
import cv.dge.dge_api_intermed_lab.application.orientacao.dto.OrientacaoEntrevistaResponse;
import cv.dge.dge_api_intermed_lab.application.orientacao.dto.OrientacaoServicoResponse;
import cv.dge.dge_api_intermed_lab.domain.acolhimento.model.AcolhimentoServico;
import cv.dge.dge_api_intermed_lab.domain.acolhimento.model.AgendamentoEntrevista;
import cv.dge.dge_api_intermed_lab.domain.acolhimento.model.Cefp;
import cv.dge.dge_api_intermed_lab.domain.acolhimento.model.DetalhesAcolhimento;
import cv.dge.dge_api_intermed_lab.domain.acolhimento.model.DetalhesEmpregoUtente;
import cv.dge.dge_api_intermed_lab.domain.acolhimento.model.Entidade;
import cv.dge.dge_api_intermed_lab.domain.acolhimento.model.Utente;
import cv.dge.dge_api_intermed_lab.infrastructure.acolhimento.repository.AcolhimentoServicoRepository;
import cv.dge.dge_api_intermed_lab.infrastructure.acolhimento.repository.AgendamentoEntrevistaRepository;
import cv.dge.dge_api_intermed_lab.infrastructure.acolhimento.repository.CefpRepository;
import cv.dge.dge_api_intermed_lab.infrastructure.acolhimento.repository.DetalhesAcolhimentoRepository;
import cv.dge.dge_api_intermed_lab.infrastructure.acolhimento.repository.DetalhesEmpregoUtenteRepository;
import cv.dge.dge_api_intermed_lab.infrastructure.acolhimento.repository.EntidadeRepository;
import cv.dge.dge_api_intermed_lab.infrastructure.acolhimento.repository.UtenteRepository;
import java.text.Normalizer;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
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
public class AcolhimentoConsultaService {

    private static final String MSG_ID_PESSOA_OBRIGATORIO = "idPessoa e obrigatorio.";
    private static final String MSG_ID_ENTIDADE_OBRIGATORIO = "globalIdEntidade e obrigatorio.";
    private static final String MSG_ACOLHIMENTO_NAO_ENCONTRADO = "Nenhum acolhimento encontrado para o idPessoa informado.";
    private static final String MSG_ACOLHIMENTO_ENTIDADE_NAO_ENCONTRADO = "Nenhum acolhimento encontrado para o globalIdEntidade informado.";

    private final DetalhesAcolhimentoRepository detalhesAcolhimentoRepository;
    private final DetalhesEmpregoUtenteRepository detalhesEmpregoUtenteRepository;
    private final AgendamentoEntrevistaRepository agendamentoEntrevistaRepository;
    private final AcolhimentoServicoRepository acolhimentoServicoRepository;
    private final UtenteRepository utenteRepository;
    private final CefpRepository cefpRepository;
    private final EntidadeRepository entidadeRepository;
    private final DocumentService documentService;
    private final GlobalGeografiaService globalGeografiaService;

    @Value("${document.acolhimento.tipo-relacao:acolhimento}")
    private String tipoRelacaoDocumentoAcolhimento;

    @Value("${document.acolhimento.app-code:emprego}")
    private String appCodeDocumentoAcolhimento;

    @Transactional(readOnly = true)
    public AcolhimentoPessoaResponse buscarPorIdPessoa(Integer idPessoa) {
        if (idPessoa == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, MSG_ID_PESSOA_OBRIGATORIO);
        }

        List<DetalhesAcolhimento> acolhimentos = detalhesAcolhimentoRepository
                .findAllByIdPessoaOrderByDateCreateDescIdDesc(idPessoa);

        if (acolhimentos.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, MSG_ACOLHIMENTO_NAO_ENCONTRADO);
        }

        List<AcolhimentoCompletoResponse> respostas = acolhimentos.stream()
                .map(this::mapearAcolhimentoCompleto)
                .toList();

        return new AcolhimentoPessoaResponse(idPessoa, respostas.size(), respostas);
    }

    @Transactional(readOnly = true)
    public AcolhimentoEntidadeResponse buscarPorIdEntidade(Integer globalIdEntidade) {
        if (globalIdEntidade == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, MSG_ID_ENTIDADE_OBRIGATORIO);
        }

        List<Integer> idsEntidade = entidadeRepository.findAllByGlobalIdEntidade(globalIdEntidade).stream()
                .map(Entidade::getId)
                .toList();

        if (idsEntidade.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, MSG_ACOLHIMENTO_ENTIDADE_NAO_ENCONTRADO);
        }

        List<DetalhesAcolhimento> acolhimentos = detalhesAcolhimentoRepository
                .findAllByIdEntidadeInOrderByDateCreateDescIdDesc(idsEntidade);

        if (acolhimentos.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, MSG_ACOLHIMENTO_ENTIDADE_NAO_ENCONTRADO);
        }

        List<AcolhimentoCompletoResponse> respostas = acolhimentos.stream()
                .map(this::mapearAcolhimentoCompleto)
                .toList();

        return new AcolhimentoEntidadeResponse(globalIdEntidade, respostas.size(), respostas);
    }

    private AcolhimentoCompletoResponse mapearAcolhimentoCompleto(DetalhesAcolhimento acolhimento) {
        Map<String, Object> detalhes = normalizarDetalhesGeografia(acolhimento.getDetalhes());
        Cefp cefp = resolverCefpDoAcolhimento(acolhimento);
        Utente utente = resolverUtenteDoAcolhimento(acolhimento);
        Entidade entidade = resolverEntidadeDoAcolhimento(acolhimento);
        AcolhimentoDadosEmpregoResponse dadosEmprego = resolverDadosEmprego(acolhimento);
        AgendamentoEntrevista entrevista = agendamentoEntrevistaRepository
                .findFirstByIdAcolhimentoOrderByDateCreateDescIdDesc(acolhimento.getId())
                .orElse(null);
        AcolhimentoServico servico = entrevista == null ? null : resolverServico(acolhimento, entrevista);
        OrientacaoServicoResponse servicoResponse = mapearServico(servico);
        OrientacaoEntrevistaResponse entrevistaResponse = mapearEntrevista(entrevista, servicoResponse);
        List<DocumentoResponseDTO> documentos = documentService.getDocumentosPorRelacao(
                acolhimento.getId(),
                tipoRelacaoDocumentoAcolhimento,
                appCodeDocumentoAcolhimento
        );

        return new AcolhimentoCompletoResponse(
                acolhimento.getId(),
                acolhimento.getIdPessoa(),
                acolhimento.getIdUtente(),
                acolhimento.getIdEntidade(),
                acolhimento.getDenominacaoUtente(),
                acolhimento.getNif(),
                acolhimento.getCefpId(),
                acolhimento.getOrgId(),
                acolhimento.getTipoUtente(),
                acolhimento.getTipoUtenteDesc(),
                acolhimento.getTipoServico(),
                acolhimento.getTipoServicoDesc(),
                acolhimento.getCanal(),
                acolhimento.getCanalDesc(),
                acolhimento.getFonteInformacao(),
                acolhimento.getStatusEntrevista(),
                acolhimento.getNumInscricao(),
                acolhimento.getIdTecnicoAtendimento(),
                acolhimento.getTecnicoAtendimento(),
                acolhimento.getDateCreate(),
                acolhimento.getUserCreate(),
                acolhimento.getDateUpdate(),
                acolhimento.getUserUpdate(),
                toCefpReporterDTO(cefp),
                toUtenteReporterDTO(utente, detalhes),
                toEntidadeReporterDTO(entidade),
                dadosEmprego,
                entrevistaResponse,
                null,
                documentos,
                detalhes
        );
    }

    private AcolhimentoDadosEmpregoResponse resolverDadosEmprego(DetalhesAcolhimento acolhimento) {
        if (acolhimento.getIdPessoa() == null) {
            return null;
        }
        return detalhesEmpregoUtenteRepository
                .findFirstByIdPessoaOrderByDateCreateDescIdDesc(acolhimento.getIdPessoa())
                .map(this::mapearDadosEmprego)
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
            return acolhimentoServicoRepository.findFirstByIdEntrevistaAndTipoServicoOrderByIdDesc(
                    entrevista.getId(),
                    tipoServico
            );
        }

        return acolhimentoServicoRepository.findFirstByIdEntrevistaOrderByIdDesc(entrevista.getId());
    }

    private AcolhimentoServico buscarServicoPorAcolhimento(DetalhesAcolhimento acolhimento) {
        return acolhimentoServicoRepository.findFirstByIdAcolhimentoOrderByIdDesc(acolhimento.getId()).orElse(null);
    }

    private OrientacaoEntrevistaResponse mapearEntrevista(
            AgendamentoEntrevista entrevista,
            OrientacaoServicoResponse servico
    ) {
        if (entrevista == null) {
            return null;
        }
        return new OrientacaoEntrevistaResponse(
                entrevista.getId(),
                entrevista.getIdAcolhimento(),
                entrevista.getIdUtente(),
                entrevista.getIdTecnico(),
                entrevista.getNomeTecnico(),
                entrevista.getDataEntrevista(),
                entrevista.getHoraInicio(),
                entrevista.getHoraFim(),
                entrevista.getLocal(),
                entrevista.getStatusEntrevista(),
                entrevista.getIdCefp(),
                entrevista.getCefp(),
                entrevista.getTipoServico(),
                entrevista.getCanal(),
                entrevista.getLocalEntrevista(),
                entrevista.getResultadoEntrevista(),
                entrevista.getParecerIo(),
                entrevista.getObsParecerIo(),
                entrevista.getPathResultado(),
                entrevista.getDateCreate(),
                entrevista.getUserCreate(),
                entrevista.getDateUpdate(),
                entrevista.getUserUpdate(),
                null,
                servico
        );
    }

    private OrientacaoServicoResponse mapearServico(AcolhimentoServico servico) {
        if (servico == null) {
            return null;
        }
        return new OrientacaoServicoResponse(
                servico.getId(),
                servico.getIdEntrevista(),
                servico.getIdAcolhimento(),
                servico.getIdUtente(),
                servico.getTipoUtente(),
                servico.getTipoUtenteDesc(),
                servico.getTipoServico(),
                servico.getTipoServicoDesc(),
                servico.getNecessidadeAnalise(),
                servico.getDetalhesServico(),
                servico.getDetalhesAnalise()
        );
    }

    private AcolhimentoDadosEmpregoResponse mapearDadosEmprego(DetalhesEmpregoUtente emprego) {
        return new AcolhimentoDadosEmpregoResponse(
                emprego.getId(),
                emprego.getIdPessoa(),
                emprego.getIdUtente(),
                emprego.getSituacaoEmprego(),
                emprego.getProfissao(),
                emprego.getEmpresa(),
                emprego.getSetorAtividade(),
                emprego.getIlha(),
                emprego.getConcelho(),
                emprego.getZona(),
                emprego.getTelefone(),
                emprego.getNumTrabalhador(),
                emprego.getDuracao(),
                emprego.getDateCreate(),
                emprego.getUserCreate(),
                emprego.getDateUpdate(),
                emprego.getUserUpdate()
        );
    }

    private Cefp resolverCefpDoAcolhimento(DetalhesAcolhimento acolhimento) {
        if (acolhimento.getCefpId() != null) {
            return cefpRepository.findById(acolhimento.getCefpId()).orElse(null);
        }
        if (acolhimento.getOrgId() != null) {
            return cefpRepository.findFirstByOrganizationId(acolhimento.getOrgId()).orElse(null);
        }
        return null;
    }

    private Utente resolverUtenteDoAcolhimento(DetalhesAcolhimento acolhimento) {
        if (acolhimento.getIdUtente() == null) {
            return null;
        }
        return utenteRepository.findById(acolhimento.getIdUtente()).orElse(null);
    }

    private Entidade resolverEntidadeDoAcolhimento(DetalhesAcolhimento acolhimento) {
        if (acolhimento.getIdEntidade() == null) {
            return null;
        }
        return entidadeRepository.findById(acolhimento.getIdEntidade()).orElse(null);
    }

    private CefpReporterDTO toCefpReporterDTO(Cefp cefp) {
        if (cefp == null) {
            return null;
        }
        return new CefpReporterDTO(
                cefp.getId(),
                cefp.getOrganizationId(),
                cefp.getDenominacao(),
                cefp.getEndereco(),
                cefp.getTelefone(),
                cefp.getEmail()
        );
    }

    private UtenteReporterDTO toUtenteReporterDTO(Utente utente, Map<String, Object> detalhes) {
        if (utente == null) {
            return null;
        }
        return new UtenteReporterDTO(
                utente.getId(),
                utente.getPessoaId(),
                utente.getNome(),
                utente.getDataNascimento(),
                utente.getTipoDocumento(),
                utente.getNumDocumento(),
                dataEmissaoDocumento(detalhes),
                utente.getSexo(),
                utente.getNif(),
                utente.getHabilitacaoLiteraria()
        );
    }

    private LocalDate dataEmissaoDocumento(Map<String, Object> detalhes) {
        return data(primeiro(
                valor(detalhes, "dataEmissaoDocumento", "data_emissao_documento", "dataEmissao", "data_emissao", "data_de_emissao"),
                procurarProfundo(detalhes, "dataEmissaoDocumento", "data_emissao_documento", "dataEmissao", "data_emissao", "data_de_emissao")
        ));
    }

    private Object procurarProfundo(Object origem, String... chaves) {
        if (origem == null || chaves == null || chaves.length == 0) {
            return null;
        }
        if (origem instanceof Map<?, ?> mapa) {
            Object direto = valor(mapa, chaves);
            if (direto != null) {
                return direto;
            }
            for (Object valor : mapa.values()) {
                Object encontrado = procurarProfundo(valor, chaves);
                if (encontrado != null) {
                    return encontrado;
                }
            }
        }
        if (origem instanceof List<?> lista) {
            for (Object item : lista) {
                Object encontrado = procurarProfundo(item, chaves);
                if (encontrado != null) {
                    return encontrado;
                }
            }
        }
        return null;
    }

    private Object valor(Map<?, ?> origem, String... chaves) {
        if (origem == null || origem.isEmpty()) {
            return null;
        }
        for (String chave : chaves) {
            for (Map.Entry<?, ?> entrada : origem.entrySet()) {
                if (entrada.getKey() != null && entrada.getKey().toString().equalsIgnoreCase(chave)) {
                    return entrada.getValue();
                }
            }
        }
        return null;
    }

    private Object primeiro(Object... valores) {
        if (valores == null) {
            return null;
        }
        for (Object valor : valores) {
            if (valor != null && (!(valor instanceof String texto) || !texto.trim().isEmpty())) {
                return valor;
            }
        }
        return null;
    }

    private LocalDate data(Object valor) {
        if (valor == null) {
            return null;
        }
        String texto = valor.toString().trim();
        if (texto.isEmpty()) {
            return null;
        }
        List<DateTimeFormatter> formatos = List.of(
                DateTimeFormatter.ISO_LOCAL_DATE,
                DateTimeFormatter.ofPattern("dd/MM/yyyy"),
                DateTimeFormatter.ofPattern("dd-MM-yyyy")
        );
        for (DateTimeFormatter formato : formatos) {
            try {
                return LocalDate.parse(texto, formato);
            } catch (DateTimeParseException ignored) {
                // Tenta o proximo formato aceito pelo formulario.
            }
        }
        return null;
    }

    private EntidadeReporterDTO toEntidadeReporterDTO(Entidade entidade) {
        if (entidade == null) {
            return null;
        }
        return new EntidadeReporterDTO(
                entidade.getId(),
                entidade.getGlobalIdEntidade(),
                entidade.getDenominacao(),
                entidade.getNif(),
                entidade.getRegistoSocial(),
                entidade.getNaturezaJuridica()
        );
    }
}
