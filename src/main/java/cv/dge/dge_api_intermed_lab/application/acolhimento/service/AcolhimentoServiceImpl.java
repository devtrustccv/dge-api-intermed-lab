package cv.dge.dge_api_intermed_lab.application.acolhimento.service;

import cv.dge.dge_api_intermed_lab.application.acolhimento.dto.AcolhimentoRegistoRequest;
import cv.dge.dge_api_intermed_lab.application.acolhimento.dto.AcolhimentoRegistoResponse;
import cv.dge.dge_api_intermed_lab.application.acolhimento.dto.AcolhimentoReporterResponse;
import cv.dge.dge_api_intermed_lab.application.acolhimento.mapper.AcolhimentoMapper;
import static cv.dge.dge_api_intermed_lab.application.acolhimento.mapper.AcolhimentoValueHelper.*;
import cv.dge.dge_api_intermed_lab.application.document.mapper.DocRelacaoMapper;
import cv.dge.dge_api_intermed_lab.application.document.service.DocumentService;
import cv.dge.dge_api_intermed_lab.application.geografia.service.GlobalGeografiaService;
import cv.dge.dge_api_intermed_lab.application.notification.mapper.NotificationMapper;
import cv.dge.dge_api_intermed_lab.application.notification.service.NotificationService;
import cv.dge.dge_api_intermed_lab.domain.acolhimento.model.Cefp;
import cv.dge.dge_api_intermed_lab.domain.acolhimento.model.DetalhesAcolhimento;
import cv.dge.dge_api_intermed_lab.domain.acolhimento.model.DetalhesEmpregoUtente;
import cv.dge.dge_api_intermed_lab.domain.acolhimento.model.Entidade;
import cv.dge.dge_api_intermed_lab.domain.acolhimento.model.ParamReport;
import cv.dge.dge_api_intermed_lab.domain.acolhimento.model.Utente;
import cv.dge.dge_api_intermed_lab.domain.acolhimento.business.AcolhimentoBus;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class AcolhimentoServiceImpl implements AcolhimentoService {
    private static final String MSG_PEDIDO_OBRIGATORIO = "Corpo do pedido e obrigatorio.";
    private static final String MSG_TIPO_SERVICO_OBRIGATORIO = "tipoServico e obrigatorio.";
    private static final String MSG_ACOLHIMENTO_NAO_ENCONTRADO = "Acolhimento nao encontrado.";
    private static final String MSG_ERRO_UPLOAD = "Erro ao enviar ficheiro do acolhimento.";

    private final AcolhimentoBus acolhimentoBus;
    private final AcolhimentoMapper acolhimentoMapper;
    private final DocRelacaoMapper docRelacaoMapper;
    private final NotificationMapper notificationMapper;
    private final GlobalGeografiaService globalGeografiaService;
    private final DocumentService documentService;
    private final NotificationService notificationService;

    @Value("${document.acolhimento.tipo-relacao:acolhimento}")
    private String tipoRelacaoDocumentoAcolhimento;

    @Value("${document.acolhimento.app-code:emprego}")
    private String appCodeDocumentoAcolhimento;

    @Value("${document.acolhimento.estado:A}")
    private String estadoDocumentoAcolhimento;

    @Transactional(readOnly = true)
    public AcolhimentoReporterResponse buscarParaReporter(Integer idAcolhimento) {
        DetalhesAcolhimento acolhimento = acolhimentoBus.findAcolhimentoById(idAcolhimento)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, MSG_ACOLHIMENTO_NAO_ENCONTRADO));

        Cefp cefp = resolverCefpDoAcolhimento(acolhimento);
        Utente utente = resolverUtenteDoAcolhimento(acolhimento);
        Entidade entidade = resolverEntidadeDoAcolhimento(acolhimento);
        ParamReport paramReport = acolhimentoBus.findUltimoParamReport().orElse(null);
        Map<String, Object> detalhesReporter = detalhesComNomesGeografia(acolhimento.getDetalhes());

        return acolhimentoMapper.toReporterResponse(acolhimento, paramReport, cefp, utente, entidade, detalhesReporter);
    }

    @Transactional
    public AcolhimentoRegistoResponse registar(AcolhimentoRegistoRequest request) {
        return registar(request, List.of());
    }

    @Transactional
    public AcolhimentoRegistoResponse registar(AcolhimentoRegistoRequest request, List<MultipartFile> ficheiros) {
        validarRequest(request);

        String tipoUtente = resolverTipoUtente(request);
        String tipoServico = resolverTipoServico(request);
        String utilizador = resolverUtilizador(request);

        Utente utente = guardarUtente(request, utilizador);
        String numInscricao = gerarNumeroInscricao();

        Map<String, Object> detalhes = montarDetalhes(request, utente);
        Cefp cefp = resolverCefp(request, detalhes);

        DetalhesAcolhimento acolhimento = criarAcolhimento(
                request,
                utente,
                detalhes,
                cefp,
                tipoUtente,
                tipoServico,
                numInscricao,
                utilizador
        );

        DetalhesAcolhimento salvo = acolhimentoBus.saveAcolhimento(acolhimento);
        guardarDetalhesEmpregoSeExistir(request, salvo.getIdPessoa(), utente.getId(), utilizador);
        guardarAnexosSeExistirem(salvo, request, ficheiros);
        enviarEmailAcolhimentoUtente(salvo, utente);

        return acolhimentoMapper.toRegistoResponse(salvo, utente);
    }

    private void enviarEmailAcolhimentoUtente(DetalhesAcolhimento acolhimento, Utente utente) {
        String email = texto(primeiro(
                valor(acolhimento.getDetalhes(), "email"),
                procurarProfundo(acolhimento.getDetalhes(), "email")
        ));
        if (emBranco(email)) {
            return;
        }

        var configEmail = notificationService.loadConfigNotification(
                "acolhimento_utente_cidadao",
                null,
                null,
                appCodeDocumentoAcolhimento
        );
        if (configEmail == null) {
            throw new IllegalArgumentException(
                    "Configuracao de email com o codigo [acolhimento_utente_cidadao] nao existe em "
                            + appCodeDocumentoAcolhimento
                            + "."
            );
        }

        notificationService.enviarEmail(notificationMapper.toEmailRequest(configEmail, appCodeDocumentoAcolhimento, email));
    }

    private void validarRequest(AcolhimentoRegistoRequest request) {
        if (request == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, MSG_PEDIDO_OBRIGATORIO);
        }
    }

    private String resolverTipoUtente(AcolhimentoRegistoRequest request) {
        return codigo(primeiro(
                request.getTipoUtente(),
                procurarProfundo(request.getDetalhes(), "tipoUtente", "tipo_utente")
        ));
    }

    private String resolverTipoServico(AcolhimentoRegistoRequest request) {
        String tipoServico = codigo(primeiro(
                request.getTipoServico(),
                procurarProfundo(request.getDetalhes(), "tipoServico", "tipo_servico", "tipoServicoSolicitado", "tipo_servico_solicitado")
        ));
        if (emBranco(tipoServico)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, MSG_TIPO_SERVICO_OBRIGATORIO);
        }
        return tipoServico;
    }

    private String resolverUtilizador(AcolhimentoRegistoRequest request) {
        return texto(primeiro(
                request.getUserCreate(),
                procurarProfundo(request.getDetalhes(), "userCreate", "user_create")
        ));
    }

    private DetalhesAcolhimento criarAcolhimento(
            AcolhimentoRegistoRequest request,
            Utente utente,
            Map<String, Object> detalhes,
            Cefp cefp,
            String tipoUtente,
            String tipoServico,
            String numInscricao,
            String utilizador
    ) {
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
        return acolhimento;
    }

    private void guardarAnexosSeExistirem(
            DetalhesAcolhimento acolhimento,
            AcolhimentoRegistoRequest request,
            List<MultipartFile> ficheiros
    ) {
        List<MultipartFile> ficheirosValidos = ficheiros == null
                ? List.of()
                : ficheiros.stream().filter(this::temConteudo).toList();

        if (ficheirosValidos.isEmpty()) {
            return;
        }

        List<Map<String, Object>> anexos = listaMapas(acolhimento.getDetalhes() == null ? null : acolhimento.getDetalhes().get("anexos"));
        for (int indice = 0; indice < ficheirosValidos.size(); indice++) {
            MultipartFile ficheiro = ficheirosValidos.get(indice);
            Map<String, Object> metadados = anexoNoIndice(anexos, request, indice);
            String tipoDocumento = texto(valor(metadados, "tipo_documento_anexo", "tipo_documento", "documento", "idTpDoc", "id_tp_doc"));
            if (emBranco(tipoDocumento)) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "tipo_documento_anexo e obrigatorio para cada ficheiro.");
            }

            String nomeOriginal = nomeOriginal(ficheiro, indice);
            String nomeBase = nomeBaseDocumento(nomeOriginal, metadados, indice);

            try {
                String path = documentService.save(docRelacaoMapper.toUploadDto(
                        acolhimento.getId(),
                        tipoRelacaoDocumentoAcolhimento,
                        texto(primeiro(valor(metadados, "estado"), estadoDocumentoAcolhimento)),
                        tipoDocumento,
                        texto(primeiro(
                                valor(metadados, "tipo_documento_anexo_desc", "tipo_documento_desc", "documento_desc", "name", "nome"),
                                nomeOriginal
                        )),
                        nomeBase,
                        construirPathDocumentoAcolhimento(acolhimento.getId(), nomeBase, ficheiro),
                        appCodeDocumentoAcolhimento,
                        ficheiro
                ));
                atualizarAnexoComPath(anexos, indice, metadados, path);
            } catch (RuntimeException ex) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_GATEWAY,
                        MSG_ERRO_UPLOAD + " Verifique se o servico de documentos/MinIO esta disponivel.",
                        ex
                );
            }
        }

        Map<String, Object> detalhesAtualizados = new LinkedHashMap<>();
        if (acolhimento.getDetalhes() != null) {
            detalhesAtualizados.putAll(acolhimento.getDetalhes());
        }
        detalhesAtualizados.put("anexos", anexos);
        acolhimento.setDetalhes(detalhesAtualizados);
        acolhimentoBus.saveAcolhimento(acolhimento);
    }

    private Map<String, Object> anexoNoIndice(List<Map<String, Object>> anexos, AcolhimentoRegistoRequest request, int indice) {
        if (anexos != null && indice >= 0 && indice < anexos.size()) {
            return anexos.get(indice);
        }
        return documentoNoIndice(request, indice);
    }

    private void atualizarAnexoComPath(List<Map<String, Object>> anexos, int indice, Map<String, Object> metadados, String path) {
        while (anexos.size() <= indice) {
            anexos.add(new LinkedHashMap<>());
        }
        Map<String, Object> anexo = anexos.get(indice);
        if (anexo.isEmpty()) {
            anexo.putAll(mapearAnexosDetalhes(List.of(metadados)).stream().findFirst().orElseGet(LinkedHashMap::new));
        }
        String publicUrl = documentService.gerarLinkPublico(path);
        anexo.put("ver_documento", valorOuVazio(publicUrl));
        anexo.put("ver_documento_desc", valorOuVazio(publicUrl));
    }

    private Map<String, Object> documentoNoIndice(AcolhimentoRegistoRequest request, int indice) {
        if (request == null || request.getDocumentos() == null || indice < 0 || indice >= request.getDocumentos().size()) {
            return new LinkedHashMap<>();
        }
        return mapa(request.getDocumentos().get(indice));
    }

    private String nomeOriginal(MultipartFile ficheiro, int indice) {
        String nome = StringUtils.cleanPath(
                Optional.ofNullable(ficheiro.getOriginalFilename()).orElse("anexo-" + (indice + 1))
        );
        return emBranco(nome) ? "anexo-" + (indice + 1) : nome;
    }

    private String nomeBaseDocumento(String nomeOriginal, Map<String, Object> metadados, int indice) {
        String nomeConfigurado = texto(primeiro(
                valor(metadados, "tipo_documento_anexo_desc", "tipo_documento_desc", "documento_desc", "fileName", "file_name", "name", "nome"),
                removerExtensao(nomeOriginal),
                "anexo-" + (indice + 1)
        ));
        String nomeBase = removerExtensao(nomeConfigurado);
        return emBranco(nomeBase) ? "anexo-" + (indice + 1) : nomeBase;
    }

    private String removerExtensao(String nomeFicheiro) {
        if (emBranco(nomeFicheiro)) {
            return null;
        }
        int indice = nomeFicheiro.lastIndexOf('.');
        return indice <= 0 ? nomeFicheiro : nomeFicheiro.substring(0, indice);
    }

    private String construirPathDocumentoAcolhimento(Integer idAcolhimento, String nomeBase, MultipartFile ficheiro) {
        String ext = "";
        String original = ficheiro == null ? null : ficheiro.getOriginalFilename();
        if (!emBranco(original)) {
            int indice = original.lastIndexOf('.');
            ext = indice >= 0 ? original.substring(indice) : "";
        }

        String nomeSeguro = sanitizarSegmentoPath(nomeBase);
        String tipoRelacaoSeguro = sanitizarSegmentoPath(tipoRelacaoDocumentoAcolhimento);
        return appCodeDocumentoAcolhimento
                + "/"
                + LocalDateTime.now().getYear()
                + "/modulos/"
                + tipoRelacaoSeguro
                + "/"
                + idAcolhimento
                + "/"
                + nomeSeguro
                + ext;
    }

    private String sanitizarSegmentoPath(String valor) {
        String limpo = texto(valor);
        if (emBranco(limpo)) {
            return "documento";
        }
        return limpo
                .trim()
                .replaceAll("[\\\\/:*?\"<>|]+", "_")
                .replace(' ', '_');
    }

    private boolean temConteudo(MultipartFile ficheiro) {
        return ficheiro != null && !ficheiro.isEmpty();
    }

    private Cefp resolverCefpDoAcolhimento(DetalhesAcolhimento acolhimento) {
        if (acolhimento.getCefpId() != null) {
            Optional<Cefp> porId = acolhimentoBus.findCefpById(acolhimento.getCefpId());
            if (porId.isPresent()) {
                return porId.get();
            }
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

    private Map<String, Object> detalhesComNomesGeografia(Map<String, Object> detalhes) {
        if (detalhes == null) {
            return null;
        }
        return mapaComNomesGeografia(detalhes, new LinkedHashMap<>());
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
            if (chaveGeografica(chave)) {
                String nome = resolverNomeGeografia(texto(valor), chave, cache);
                if (!emBranco(nome)) {
                    return nome;
                }
            }
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
        String nome = resolverNomeGeografia(texto(valor), chave, cache);
        return emBranco(nome) ? valor : nome;
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
        if (emBranco(codigo)) {
            return null;
        }
        String chaveCache = normalizar(chave) + ":" + codigo.trim();
        if (cache.containsKey(chaveCache)) {
            return cache.get(chaveCache);
        }

        String nome = globalGeografiaService.buscarNomePorCodigo(codigo).orElse(null);
        cache.put(chaveCache, nome);
        return nome;
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

        return acolhimentoBus.saveUtente(utente);
    }

    private Optional<Utente> localizarUtente(Integer idUtente, Integer idPessoa, String tipoDocumento, String numDocumento) {
        if (idPessoa != null) {
            Optional<Utente> porPessoa = acolhimentoBus.findUtenteByPessoa(idPessoa);
            if (porPessoa.isPresent()) {
                return porPessoa;
            }
        }
        if (idUtente != null) {
            Optional<Utente> porId = acolhimentoBus.findUtenteById(idUtente);
            if (porId.isPresent()) {
                return porId;
            }
        }
        if (!emBranco(tipoDocumento) && !emBranco(numDocumento)) {
            Optional<Utente> porDocumento = acolhimentoBus.findUtenteByDocumento(
                    tipoDocumento,
                    numDocumento
            );
            if (porDocumento.isPresent()) {
                return porDocumento;
            }
        }
        if (!emBranco(numDocumento)) {
            return acolhimentoBus.findUtenteByNumDocumento(numDocumento);
        }
        return Optional.empty();
    }

    private Map<String, Object> montarDetalhes(AcolhimentoRegistoRequest request, Utente utente) {
        Map<String, Object> origem = new LinkedHashMap<>(request.getDetalhes());
        if (!request.getUtente().isEmpty()) {
            origem.putIfAbsent("utente", request.getUtente());
        }
        if (!request.getDadosEmprego().isEmpty()) {
            origem.putIfAbsent("dadosEmprego", request.getDadosEmprego());
        }
        if (!request.getDocumentos().isEmpty()) {
            origem.putIfAbsent("anexos", request.getDocumentos());
        }

        Map<String, Object> detalhes = new LinkedHashMap<>();
        colocarDetalhe(detalhes, "csu", campoFormulario(origem, "csu"));
        colocarDetalhe(detalhes, "como_obteve_informacao", primeiro(
                request.getFonteInformacao(),
                campoFormulario(origem, "como_obteve_informacao", "como_obteve_informacao_sobre_os_servicos_do_iefp", "fonteInformacao", "fonte_informacao")
        ));
        colocarDetalhe(detalhes, "ilha", campoFormulario(origem, "ilha"));
        colocarDetalhe(detalhes, "zona", campoFormulario(origem, "zona"));
        colocarDetalhe(detalhes, "area_", campoFormulario(origem, "area_", "area"));
        colocarDetalhe(detalhes, "email", campoFormulario(origem, "email"));
        colocarDetalhe(detalhes, "outro", campoFormulario(origem, "outro"));
        detalhes.put("anexos", mapearAnexosDetalhes(primeiro(
                request.getDocumentos(),
                campoFormulario(origem, "anexos", "documentos", "docs", "formlist_1")
        )));
        colocarDetalhe(detalhes, "empresa", campoFormulario(origem, "empresa"));
        colocarDetalhe(detalhes, "concelho", campoFormulario(origem, "concelho"));
        colocarDetalhe(detalhes, "endereco", campoFormulario(origem, "endereco", "enderecoContato", "enderecoContacto", "endereco_e_contacto"));
        colocarDetalhe(detalhes, "telefone", campoFormulario(origem, "telefone"));
        colocarDetalhe(detalhes, "link_foto", campoFormulario(origem, "link_foto", "linkFoto", "faceUrl", "face_url"));
        colocarDetalhe(detalhes, "profissao", campoFormulario(origem, "profissao"));
        colocarDetalhe(detalhes, "telemovel", campoFormulario(origem, "telemovel"));
        colocarDetalhe(detalhes, "observacoes", campoFormulario(origem, "observacoes", "observacoes_"));
        colocarDetalhe(detalhes, "estado_civil", campoFormulario(origem, "estado_civil", "estadoCivil"));
        colocarDetalhe(detalhes, "ilha_empresa", campoFormulario(origem, "ilha_empresa", "ilhaEmpresa"));
        colocarDetalhe(detalhes, "naturalidade", campoFormulario(origem, "naturalidade"));
        colocarDetalhe(detalhes, "zona_empresa", campoFormulario(origem, "zona_empresa", "zonaEmpresa"));
        colocarDetalhe(detalhes, "data_validade", campoFormulario(origem, "data_validade", "dataValidade"));
        colocarDetalhe(detalhes, "carta_conducao", campoFormulario(origem, "carta_conducao", "cartaConducao"));
        colocarDetalhe(detalhes, "zona_empresa_1", campoFormulario(origem, "zona_empresa_1", "zonaEmpresa1"));
        colocarDetalhe(detalhes, "concelho_empresa", campoFormulario(origem, "concelho_empresa", "concelhoEmpresa"));
        colocarDetalhe(detalhes, "local_de_emissao", campoFormulario(origem, "local_de_emissao", "localEmissao", "local_de_emissao_documento"));
        colocarDetalhe(detalhes, "o_que_deseja_criar", campoFormulario(origem, "o_que_deseja_criar", "oQueDesejaCriar"));
        colocarDetalhe(detalhes, "setor_de_atividade", campoFormulario(origem, "setor_de_atividade", "setorDeAtividade", "setor_atividade", "setorAtividade"));
        colocarDetalhe(detalhes, "o_que_deseja_criar_1", campoFormulario(origem, "o_que_deseja_criar_1", "area_de_trabalho__pretendida", "areaDeTrabalhoPretendida"));
        colocarDetalhe(detalhes, "setor_de_atividade_1", campoFormulario(origem, "setor_de_atividade_1", "setorDeAtividade1"));
        colocarDetalhe(detalhes, "tipo_servico_solicitado", primeiro(
                request.getTipoServico(),
                campoFormulario(origem, "tipo_servico_solicitado", "tipoServicoSolicitado", "tipo_servico", "tipoServico")
        ));
        colocarDetalhe(detalhes, "local_de_trabalho_preferencial", campoFormulario(origem, "local_de_trabalho_preferencial", "localTrabalhoPreferencial"));
        colocarDetalhe(detalhes, "situacao_face_ao_emprego", campoFormulario(
                origem,
                "situacao_face_ao_emprego",
                "situacaoFaceAoEmprego",
                "situacaoFaceEmprego",
                "situacao_emprego",
                "situacaoEmprego"
        ));
        colocarDetalhe(detalhes, "autoriza_a_divulgacao_dos_seus_dados_para_efeito__de_emprego", campoFormulario(
                origem,
                "autoriza_a_divulgacao_dos_seus_dados_para_efeito__de_emprego",
                "autoriza_a_divulgacao_dos_seus_dados_para_efeito_de_emprego",
                "autorizaDivulgacaoDadosEmprego"
        ));
        return detalhes;
    }

    private void colocarDetalhe(Map<String, Object> destino, String chave, Object valor) {
        destino.put(chave, valor == null ? "" : valor);
    }

    private Object campoFormulario(Map<String, Object> origem, String... chaves) {
        return primeiro(valor(origem, chaves), procurarProfundo(origem, chaves));
    }

    private List<Map<String, Object>> mapearAnexosDetalhes(Object origem) {
        List<Map<String, Object>> anexos = new ArrayList<>();
        for (Map<String, Object> itemOrigem : listaMapas(origem)) {
            Object tipoDocumento = primeiro(
                    campoFormulario(itemOrigem, "tipo_documento_anexo"),
                    campoFormulario(itemOrigem, "tipo_documento", "documento", "idTpDoc", "id_tp_doc")
            );
            if (tipoDocumento == null) {
                continue;
            }

            Object verDocumento = campoFormulario(itemOrigem, "ver_documento", "anexo", "path");
            Object descricaoDocumento = primeiro(
                    campoFormulario(itemOrigem, "tipo_documento_anexo_desc", "tipo_documento_desc", "documento_desc"),
                    campoFormulario(itemOrigem, "name", "nome", "fileName", "file_name"),
                    tipoDocumento
            );
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("id", valorOuVazio(valorOpcao(campoFormulario(itemOrigem, "id", "formlist_1_id"))));
            item.put("ver_documento", valorOuVazio(valorOpcao(verDocumento)));
            item.put("ver_documento_desc", valorOuVazio(valorOpcao(verDocumento)));
            item.put("tipo_documento_anexo", valorOuVazio(valorOpcao(tipoDocumento)));
            item.put("tipo_documento_anexo_desc", valorOuVazio(valorOpcao(descricaoDocumento)));
            anexos.add(item);
        }
        return anexos;
    }

    private List<Map<String, Object>> listaMapas(Object origem) {
        if (!(origem instanceof Collection<?> colecao)) {
            return new ArrayList<>();
        }
        List<Map<String, Object>> destino = new ArrayList<>();
        for (Object item : colecao) {
            Map<String, Object> mapa = mapa(item);
            if (!mapa.isEmpty()) {
                destino.add(mapa);
            }
        }
        return destino;
    }

    private Object valorOpcao(Object valor) {
        if (valor instanceof Map<?, ?> mapa) {
            Object chave = valor(mapa, "key", "codigo", "code", "id", "valor", "value");
            return chave == null ? texto(valor) : chave;
        }
        return valor;
    }

    private Object valorOuVazio(Object valor) {
        return valor == null ? "" : valor;
    }

    private Cefp resolverCefp(AcolhimentoRegistoRequest request, Map<String, Object> detalhes) {
        Integer cefpId = inteiro(primeiro(request.getCefpId(), procurarProfundo(detalhes, "cefpId", "cefp_id")));
        if (cefpId != null) {
            return acolhimentoBus.findCefpById(cefpId).orElse(null);
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
        return acolhimentoBus.findCefpByAreaAbrangida(ilha, concelho).orElse(null);
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

        acolhimentoBus.saveEmprego(emprego);
    }

    private String gerarNumeroInscricao() {
        String prefixo = "ACO" + LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE) + "/";
        Integer sequencia = acolhimentoBus.proximoNumeroInscricao();
        if (sequencia == null || sequencia < 1) {
            sequencia = 1;
        }
        return prefixo + sequencia;
    }

}
