package cv.dge.dge_api_intermed_lab.application.acolhimento.service;

import cv.dge.dge_api_intermed_lab.application.acolhimento.dto.AcolhimentoRegistoRequest;
import cv.dge.dge_api_intermed_lab.application.acolhimento.dto.AcolhimentoRegistoResponse;
import cv.dge.dge_api_intermed_lab.application.acolhimento.dto.AcolhimentoReporterResponse;
import cv.dge.dge_api_intermed_lab.application.acolhimento.dto.CefpReporterDTO;
import cv.dge.dge_api_intermed_lab.application.acolhimento.dto.EntidadeReporterDTO;
import cv.dge.dge_api_intermed_lab.application.acolhimento.dto.UtenteReporterDTO;
import cv.dge.dge_api_intermed_lab.domain.acolhimento.model.Cefp;
import cv.dge.dge_api_intermed_lab.domain.acolhimento.model.DetalhesAcolhimento;
import cv.dge.dge_api_intermed_lab.domain.acolhimento.model.DetalhesEmpregoUtente;
import cv.dge.dge_api_intermed_lab.domain.acolhimento.model.Entidade;
import cv.dge.dge_api_intermed_lab.domain.acolhimento.model.ParamReport;
import cv.dge.dge_api_intermed_lab.domain.acolhimento.model.Utente;
import cv.dge.dge_api_intermed_lab.infrastructure.acolhimento.repository.CefpRepository;
import cv.dge.dge_api_intermed_lab.infrastructure.acolhimento.repository.DetalhesAcolhimentoRepository;
import cv.dge.dge_api_intermed_lab.infrastructure.acolhimento.repository.DetalhesEmpregoUtenteRepository;
import cv.dge.dge_api_intermed_lab.infrastructure.acolhimento.repository.EntidadeRepository;
import cv.dge.dge_api_intermed_lab.infrastructure.acolhimento.repository.ParamReportRepository;
import cv.dge.dge_api_intermed_lab.infrastructure.acolhimento.repository.UtenteRepository;

import java.math.BigDecimal;
import java.text.Normalizer;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class AcolhimentoService {

    private final UtenteRepository utenteRepository;
    private final DetalhesAcolhimentoRepository detalhesAcolhimentoRepository;
    private final DetalhesEmpregoUtenteRepository detalhesEmpregoUtenteRepository;
    private final CefpRepository cefpRepository;
    private final EntidadeRepository entidadeRepository;
    private final ParamReportRepository paramReportRepository;

    @Transactional(readOnly = true)
    public AcolhimentoReporterResponse buscarParaReporter(Integer idAcolhimento) {
        DetalhesAcolhimento acolhimento = detalhesAcolhimentoRepository.findById(idAcolhimento)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Acolhimento nao encontrado."));

        Cefp cefp = resolverCefpDoAcolhimento(acolhimento);
        Utente utente = resolverUtenteDoAcolhimento(acolhimento);
        Entidade entidade = resolverEntidadeDoAcolhimento(acolhimento);
        ParamReport paramReport = paramReportRepository.findFirstByOrderByIdDesc().orElse(null);

        return new AcolhimentoReporterResponse(
                acolhimento.getId(),
                paramReport == null ? null : paramReport.getLogotipoIefp(),
                paramReport == null ? null : paramReport.getLogotipoDge(),
                cefp == null ? null : cefp.getDenominacao(),
                cefp == null ? null : cefp.getEndereco(),
                cefp == null ? null : cefp.getTelefone(),
                cefp == null ? null : cefp.getEmail(),
                acolhimento.getDateCreate(),
                acolhimento.getNumInscricao(),
                utente == null ? null : utente.getId(),
                utente == null ? null : utente.getNome(),
                acolhimento.getTecnicoAtendimento(),
                toCefpReporterDTO(cefp),
                toUtenteReporterDTO(utente),
                toEntidadeReporterDTO(entidade),
                acolhimento.getDetalhes()
        );
    }

    @Transactional
    public AcolhimentoRegistoResponse registar(AcolhimentoRegistoRequest request) {
        if (request == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Corpo do pedido e obrigatorio.");
        }

        String tipoUtente = codigo(primeiro(
                request.getTipoUtente(),
                procurarProfundo(request.getDetalhes(), "tipoUtente", "tipo_utente")
        ));
        String tipoServico = codigo(primeiro(
                request.getTipoServico(),
                procurarProfundo(request.getDetalhes(), "tipoServico", "tipo_servico", "tipoServicoSolicitado", "tipo_servico_solicitado")
        ));

        if (emBranco(tipoServico)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "tipoServico e obrigatorio.");
        }

        String utilizador = texto(primeiro(request.getUserCreate(), procurarProfundo(request.getDetalhes(), "userCreate", "user_create")));

        Utente utente = guardarUtente(request, utilizador);
        String numInscricao = gerarNumeroInscricao();

        Map<String, Object> detalhes = montarDetalhes(request, utente);
        Cefp cefp = resolverCefp(request, detalhes);
        adicionarCefpNoJson(detalhes, cefp);
        detalhes.put("numInscricao", numInscricao);

        Integer cefpId = inteiro(primeiro(request.getCefpId(), procurarProfundo(detalhes, "cefpId", "cefp_id")));
        Integer orgId = inteiro(primeiro(request.getOrgId(), procurarProfundo(detalhes, "orgId", "org_id", "organizationId")));
        if (cefp != null) {
            cefpId = cefp.getId();
            orgId = cefp.getOrganizationId();
        }

        DetalhesAcolhimento acolhimento = new DetalhesAcolhimento();
        acolhimento.setIdPessoa(inteiro(primeiro(request.getIdPessoa(), utente.getPessoaId())));
        acolhimento.setIdUtente(utente.getId());
        acolhimento.setIdEntidade(inteiro(request.getIdEntidade()));
        acolhimento.setDenominacaoUtente(texto(primeiro(request.getDenominacaoUtente(), utente.getNome())));
        acolhimento.setNif(decimal(primeiro(request.getNif(), utente.getNif())));
        acolhimento.setCefpId(cefpId);
        acolhimento.setOrgId(orgId);
        acolhimento.setTipoUtente(tipoUtente);
        acolhimento.setTipoUtenteDesc(texto(primeiro(
                request.getTipoUtenteDesc(),
                procurarProfundo(detalhes, "tipoUtenteDesc", "tipo_utente_desc")
        )));
        acolhimento.setTipoServico(tipoServico);
        acolhimento.setTipoServicoDesc(texto(primeiro(
                request.getTipoServicoDesc(),
                procurarProfundo(detalhes, "tipoServicoDesc", "tipo_servico_desc")
        )));
        acolhimento.setCanal(codigo(primeiro(request.getCanal(), procurarProfundo(detalhes, "canal"))));
        acolhimento.setCanalDesc(texto(primeiro(request.getCanalDesc(), procurarProfundo(detalhes, "canalDesc", "canal_desc"))));
        acolhimento.setDetalhes(detalhes);
        acolhimento.setIdTecnicoAtendimento(inteiro(request.getIdTecnicoAtendimento()));
        acolhimento.setTecnicoAtendimento(texto(request.getTecnicoAtendimento()));
        acolhimento.setFonteInformacao(codigo(primeiro(
                request.getFonteInformacao(),
                procurarProfundo(detalhes, "fonteInformacao", "fonte_informacao")
        )));
        acolhimento.setStatusEntrevista(codigo(request.getStatusEntrevista()));
        acolhimento.setNumInscricao(numInscricao);
        acolhimento.setUserCreate(utilizador);

        DetalhesAcolhimento salvo = detalhesAcolhimentoRepository.save(acolhimento);
        guardarDetalhesEmpregoSeExistir(request, salvo.getIdPessoa(), utente.getId(), utilizador);

        return new AcolhimentoRegistoResponse(
                salvo.getId(),
                utente.getId(),
                salvo.getIdPessoa(),
                salvo.getNumInscricao(),
                salvo.getCefpId(),
                salvo.getOrgId(),
                salvo.getDetalhes()
        );
    }

    private Cefp resolverCefpDoAcolhimento(DetalhesAcolhimento acolhimento) {
        if (acolhimento.getCefpId() != null) {
            Optional<Cefp> porId = cefpRepository.findById(acolhimento.getCefpId());
            if (porId.isPresent()) {
                return porId.get();
            }
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

    private UtenteReporterDTO toUtenteReporterDTO(Utente utente) {
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
                utente.getSexo(),
                utente.getNif(),
                utente.getHabilitacaoLiteraria()
        );
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

    private Utente guardarUtente(AcolhimentoRegistoRequest request, String utilizador) {
        Map<String, Object> dadosUtente = request.getUtente();
        Integer idUtente = inteiro(primeiro(request.getIdUtente(), valor(dadosUtente, "id", "idUtente", "id_utente")));
        Integer idPessoa = inteiro(primeiro(
                request.getIdPessoa(),
                valor(dadosUtente, "idPessoa", "id_pessoa", "pessoaId", "pessoa_id"),
                procurarProfundo(request.getDetalhes(), "idPessoa", "id_pessoa", "pessoaId", "pessoa_id")
        ));
        String tipoDocumento = codigo(primeiro(
                valor(dadosUtente, "tipoDocumento", "tipo_documento", "tipoDocumentoIdentificacao"),
                procurarProfundo(request.getDetalhes(), "tipoDocumento", "tipo_documento", "tipoDocumentoIdentificacao")
        ));
        String numDocumento = texto(primeiro(
                valor(dadosUtente, "numDocumento", "num_documento", "numeroDocumento", "nDocumento", "noDocumento"),
                procurarProfundo(request.getDetalhes(), "numDocumento", "num_documento", "numeroDocumento", "nDocumento", "noDocumento")
        ));

        Utente utente = localizarUtente(idUtente, idPessoa, tipoDocumento, numDocumento).orElseGet(Utente::new);
        boolean novo = utente.getId() == null;

        aplicarSePresente(idPessoa, utente::setPessoaId);
        aplicarSePresente(texto(primeiro(
                request.getDenominacaoUtente(),
                valor(dadosUtente, "nome", "nomeCompleto", "nome_completo"),
                procurarProfundo(request.getDetalhes(), "nome", "nomeCompleto", "nome_completo")
        )), utente::setNome);
        aplicarSePresente(data(primeiro(
                valor(dadosUtente, "dataNascimento", "data_nascimento"),
                procurarProfundo(request.getDetalhes(), "dataNascimento", "data_nascimento")
        )), utente::setDataNascimento);
        aplicarSePresente(tipoDocumento, utente::setTipoDocumento);
        aplicarSePresente(numDocumento, utente::setNumDocumento);
        aplicarSePresente(codigo(primeiro(
                valor(dadosUtente, "sexo"),
                procurarProfundo(request.getDetalhes(), "sexo")
        )), utente::setSexo);
        aplicarSePresente(inteiro(primeiro(
                request.getNif(),
                valor(dadosUtente, "nif"),
                procurarProfundo(request.getDetalhes(), "nif")
        )), utente::setNif);
        aplicarSePresente(codigo(primeiro(
                valor(dadosUtente, "habilitacaoLiteraria", "habilitacao_literaria"),
                procurarProfundo(request.getDetalhes(), "habilitacaoLiteraria", "habilitacao_literaria")
        )), utente::setHabilitacaoLiteraria);

        if (novo) {
            utente.setUserCreate(utilizador);
        } else {
            utente.setDateUpdate(LocalDateTime.now());
            utente.setUserUpdate(utilizador);
        }

        return utenteRepository.save(utente);
    }

    private Optional<Utente> localizarUtente(Integer idUtente, Integer idPessoa, String tipoDocumento, String numDocumento) {
        if (idUtente != null) {
            Optional<Utente> porId = utenteRepository.findById(idUtente);
            if (porId.isPresent()) {
                return porId;
            }
        }
        if (idPessoa != null) {
            Optional<Utente> porPessoa = utenteRepository.findFirstByPessoaId(idPessoa);
            if (porPessoa.isPresent()) {
                return porPessoa;
            }
        }
        if (!emBranco(tipoDocumento) && !emBranco(numDocumento)) {
            Optional<Utente> porDocumento = utenteRepository.findFirstByTipoDocumentoIgnoreCaseAndNumDocumentoIgnoreCase(
                    tipoDocumento,
                    numDocumento
            );
            if (porDocumento.isPresent()) {
                return porDocumento;
            }
        }
        if (!emBranco(numDocumento)) {
            return utenteRepository.findFirstByNumDocumentoIgnoreCase(numDocumento);
        }
        return Optional.empty();
    }

    private Map<String, Object> montarDetalhes(AcolhimentoRegistoRequest request, Utente utente) {
        Map<String, Object> detalhes = new LinkedHashMap<>(request.getDetalhes());
        if (!request.getUtente().isEmpty()) {
            detalhes.putIfAbsent("utente", request.getUtente());
        }
        if (!request.getDadosEmprego().isEmpty()) {
            detalhes.putIfAbsent("dadosEmprego", request.getDadosEmprego());
        }
        if (!request.getDocumentos().isEmpty()) {
            detalhes.putIfAbsent("anexos", request.getDocumentos());
        }
        colocarSePresente(detalhes, "idPessoa", primeiro(request.getIdPessoa(), utente.getPessoaId()));
        colocarSePresente(detalhes, "idUtente", utente.getId());
        colocarSePresente(detalhes, "tipoUtente", request.getTipoUtente());
        colocarSePresente(detalhes, "tipoUtenteDesc", request.getTipoUtenteDesc());
        colocarSePresente(detalhes, "tipoServico", request.getTipoServico());
        colocarSePresente(detalhes, "tipoServicoDesc", request.getTipoServicoDesc());
        colocarSePresente(detalhes, "canal", request.getCanal());
        colocarSePresente(detalhes, "canalDesc", request.getCanalDesc());
        colocarSePresente(detalhes, "fonteInformacao", request.getFonteInformacao());
        return detalhes;
    }

    private Cefp resolverCefp(AcolhimentoRegistoRequest request, Map<String, Object> detalhes) {
        Integer cefpId = inteiro(primeiro(request.getCefpId(), procurarProfundo(detalhes, "cefpId", "cefp_id")));
        if (cefpId != null) {
            return cefpRepository.findById(cefpId).orElse(null);
        }

        Object endereco = primeiro(
                valor(detalhes, "endereco", "enderecoContato", "enderecoContacto", "endereco_e_contacto"),
                procurarProfundo(detalhes, "endereco", "enderecoContato", "enderecoContacto", "endereco_e_contacto")
        );
        Map<String, Object> enderecoMap = mapa(endereco);
        String ilha = texto(primeiro(
                valor(enderecoMap, "ilha"),
                procurarProfundo(detalhes, "ilha")
        ));
        String concelho = texto(primeiro(
                valor(enderecoMap, "concelho"),
                procurarProfundo(detalhes, "concelho")
        ));

        if (emBranco(ilha) || emBranco(concelho)) {
            return null;
        }
        return cefpRepository.findFirstByAreaAbrangida(ilha, concelho).orElse(null);
    }

    private void adicionarCefpNoJson(Map<String, Object> detalhes, Cefp cefp) {
        if (cefp == null) {
            return;
        }
        Map<String, Object> cefpJson = new LinkedHashMap<>();
        cefpJson.put("id", cefp.getId());
        cefpJson.put("denominacao", cefp.getDenominacao());
        cefpJson.put("organizationId", cefp.getOrganizationId());
        detalhes.put("cefp", cefpJson);
    }

    private void guardarDetalhesEmpregoSeExistir(
            AcolhimentoRegistoRequest request,
            Integer idPessoa,
            Integer idUtente,
            String utilizador
    ) {
        Map<String, Object> origem = new LinkedHashMap<>(request.getDadosEmprego());
        if (origem.isEmpty()) {
            origem.putAll(mapa(primeiro(
                    procurarProfundo(request.getDetalhes(), "dadosEmprego", "detalhesEmprego"),
                    procurarProfundo(request.getDetalhes(), "dadosAcademicosProfissionais", "dados_academicos_profissionais")
            )));
        }

        String situacao = codigo(primeiro(
                valor(origem, "situacaoEmprego", "situacao_emprego", "situacaoFaceEmprego", "situacao_face_emprego", "situacao_face_ao_emprego"),
                procurarProfundo(request.getDetalhes(), "situacaoEmprego", "situacao_emprego", "situacaoFaceEmprego", "situacao_face_emprego", "situacao_face_ao_emprego")
        ));
        String profissao = texto(primeiro(valor(origem, "profissao"), procurarProfundo(request.getDetalhes(), "profissao")));
        String empresa = texto(primeiro(valor(origem, "empresa"), procurarProfundo(request.getDetalhes(), "empresa")));
        String setorAtividade = texto(primeiro(
                valor(origem, "setorAtividade", "setor_atividade", "setorDeAtividade", "setor_de_atividade"),
                procurarProfundo(request.getDetalhes(), "setorAtividade", "setor_atividade", "setorDeAtividade", "setor_de_atividade")
        ));
        String duracao = texto(primeiro(
                valor(origem, "duracao", "duracaoDesemprego", "duracao_periodo_desemprego"),
                procurarProfundo(request.getDetalhes(), "duracao", "duracaoDesemprego", "duracao_periodo_desemprego")
        ));

        if (todosEmBranco(situacao, profissao, empresa, setorAtividade, duracao)) {
            return;
        }

        DetalhesEmpregoUtente emprego = new DetalhesEmpregoUtente();
        emprego.setIdPessoa(idPessoa);
        emprego.setIdUtente(idUtente);
        emprego.setSituacaoEmprego(situacao);
        emprego.setProfissao(profissao);
        emprego.setEmpresa(empresa);
        emprego.setSetorAtividade(setorAtividade);
        emprego.setIlha(texto(primeiro(valor(origem, "ilha", "ilha_empresa"), procurarProfundo(request.getDetalhes(), "ilha_empresa"))));
        emprego.setConcelho(texto(primeiro(valor(origem, "concelho", "concelho_empresa"), procurarProfundo(request.getDetalhes(), "concelho_empresa"))));
        emprego.setZona(texto(primeiro(valor(origem, "zona", "zona_empresa"), procurarProfundo(request.getDetalhes(), "zona_empresa"))));
        emprego.setTelefone(texto(primeiro(
                valor(origem, "telefone", "telemovel", "telefoneTelemovel", "telefone_telemovel", "zona_empresa_1"),
                procurarProfundo(request.getDetalhes(), "zona_empresa_1")
        )));
        emprego.setNumTrabalhador(texto(valor(origem, "numTrabalhador", "num_trabalhador", "numeroTrabalhadores")));
        emprego.setDuracao(duracao);
        emprego.setUserCreate(utilizador);

        detalhesEmpregoUtenteRepository.save(emprego);
    }

    private String gerarNumeroInscricao() {
        String prefixo = "ACO" + LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE) + "/";
        Integer sequencia = detalhesAcolhimentoRepository.proximoNumeroInscricao(prefixo);
        if (sequencia == null || sequencia < 1) {
            sequencia = 1;
        }
        return prefixo + sequencia;
    }

    private Object procurarProfundo(Object origem, String... chaves) {
        if (origem == null || chaves == null || chaves.length == 0) {
            return null;
        }
        if (origem instanceof Map<?, ?> mapa) {
            Object direto = valor(mapa, chaves);
            if (!vazio(direto)) {
                return direto;
            }
            for (Object valor : mapa.values()) {
                Object encontrado = procurarProfundo(valor, chaves);
                if (!vazio(encontrado)) {
                    return encontrado;
                }
            }
        }
        if (origem instanceof Collection<?> colecao) {
            for (Object item : colecao) {
                Object encontrado = procurarProfundo(item, chaves);
                if (!vazio(encontrado)) {
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
        List<String> normalizadas = List.of(chaves).stream().map(this::normalizar).toList();
        for (Map.Entry<?, ?> entrada : origem.entrySet()) {
            if (entrada.getKey() != null && normalizadas.contains(normalizar(entrada.getKey().toString()))) {
                return entrada.getValue();
            }
        }
        return null;
    }

    private Object primeiro(Object... valores) {
        if (valores == null) {
            return null;
        }
        for (Object valor : valores) {
            if (!vazio(valor)) {
                return valor;
            }
        }
        return null;
    }

    private void colocarSePresente(Map<String, Object> destino, String chave, Object valor) {
        if (!vazio(valor)) {
            destino.putIfAbsent(chave, valor);
        }
    }

    private <T> void aplicarSePresente(T valor, java.util.function.Consumer<T> consumer) {
        if (!vazio(valor)) {
            consumer.accept(valor);
        }
    }

    private boolean vazio(Object valor) {
        if (valor == null) {
            return true;
        }
        if (valor instanceof String texto) {
            return texto.trim().isEmpty();
        }
        if (valor instanceof Map<?, ?> mapa) {
            return mapa.isEmpty();
        }
        if (valor instanceof Collection<?> colecao) {
            return colecao.isEmpty();
        }
        return false;
    }

    private boolean emBranco(String valor) {
        return valor == null || valor.trim().isEmpty();
    }

    private boolean todosEmBranco(String... valores) {
        for (String valor : valores) {
            if (!emBranco(valor)) {
                return false;
            }
        }
        return true;
    }

    private String codigo(Object valor) {
        return textoDeMapa(valor, List.of("codigo", "code", "id", "valor", "value", "descricao", "description", "label", "nome"));
    }

    private String texto(Object valor) {
        return textoDeMapa(valor, List.of("descricao", "description", "label", "nome", "denominacao", "valor", "value", "codigo", "code", "id"));
    }

    private String textoDeMapa(Object valor, List<String> preferencias) {
        if (valor == null) {
            return null;
        }
        if (valor instanceof String texto) {
            String limpo = texto.trim();
            return limpo.isEmpty() ? null : limpo;
        }
        if (valor instanceof Number || valor instanceof Boolean) {
            return valor.toString();
        }
        if (valor instanceof Map<?, ?> mapa) {
            for (String chave : preferencias) {
                Object encontrado = valor(mapa, chave);
                String texto = textoDeMapa(encontrado, preferencias);
                if (!emBranco(texto)) {
                    return texto;
                }
            }
            if (mapa.size() == 1) {
                return textoDeMapa(mapa.values().iterator().next(), preferencias);
            }
            return null;
        }
        if (valor instanceof Collection<?> colecao) {
            for (Object item : colecao) {
                String texto = textoDeMapa(item, preferencias);
                if (!emBranco(texto)) {
                    return texto;
                }
            }
            return null;
        }
        String texto = valor.toString().trim();
        return texto.isEmpty() ? null : texto;
    }

    private Integer inteiro(Object valor) {
        if (valor == null) {
            return null;
        }
        if (valor instanceof Number numero) {
            return numero.intValue();
        }
        String texto = texto(valor);
        if (emBranco(texto)) {
            return null;
        }
        try {
            return Integer.valueOf(texto);
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    private BigDecimal decimal(Object valor) {
        if (valor == null) {
            return null;
        }
        if (valor instanceof BigDecimal decimal) {
            return decimal;
        }
        if (valor instanceof Number numero) {
            return BigDecimal.valueOf(numero.doubleValue());
        }
        String texto = texto(valor);
        if (emBranco(texto)) {
            return null;
        }
        try {
            return new BigDecimal(texto);
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    private LocalDate data(Object valor) {
        String texto = texto(valor);
        if (emBranco(texto)) {
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

    @SuppressWarnings("unchecked")
    private Map<String, Object> mapa(Object valor) {
        if (valor instanceof Map<?, ?> mapa) {
            Map<String, Object> convertido = new LinkedHashMap<>();
            mapa.forEach((chave, conteudo) -> {
                if (chave != null) {
                    convertido.put(chave.toString(), conteudo);
                }
            });
            return convertido;
        }
        return new LinkedHashMap<>();
    }

    private String normalizar(String valor) {
        if (valor == null) {
            return "";
        }
        String semAcentos = Normalizer.normalize(valor, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "");
        return semAcentos.replaceAll("[^A-Za-z0-9]", "").toLowerCase(Locale.ROOT);
    }
}
