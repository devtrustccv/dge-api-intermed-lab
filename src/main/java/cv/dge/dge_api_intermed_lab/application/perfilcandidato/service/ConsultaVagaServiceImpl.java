package cv.dge.dge_api_intermed_lab.application.perfilcandidato.service;

import cv.dge.dge_api_intermed_lab.application.document.dto.DocRelacaoDTO;
import cv.dge.dge_api_intermed_lab.application.document.service.ComboboxService;
import cv.dge.dge_api_intermed_lab.application.document.service.DocumentService;
import cv.dge.dge_api_intermed_lab.application.perfilcandidato.dto.CandidaturaDocumentoResponse;
import cv.dge.dge_api_intermed_lab.application.perfilcandidato.dto.CandidaturaVagaFormularioResponse;
import cv.dge.dge_api_intermed_lab.application.perfilcandidato.dto.CandidaturaVagaRequest;
import cv.dge.dge_api_intermed_lab.application.perfilcandidato.dto.CandidaturaVagaResponse;
import cv.dge.dge_api_intermed_lab.application.perfilcandidato.dto.ConsultaVagaDetalheResponse;
import cv.dge.dge_api_intermed_lab.application.perfilcandidato.dto.ConsultaVagaFiltro;
import cv.dge.dge_api_intermed_lab.application.perfilcandidato.dto.ConsultaVagaListaResponse;
import cv.dge.dge_api_intermed_lab.application.perfilcandidato.dto.ConsultaVagaMapaResponse;
import cv.dge.dge_api_intermed_lab.application.perfilcandidato.dto.ConsultaVagaOpcoesResponse;
import cv.dge.dge_api_intermed_lab.application.perfilcandidato.dto.ConsultaVagasResponse;
import cv.dge.dge_api_intermed_lab.application.perfilentidade.enums.EmpregoDominio;
import cv.dge.dge_api_intermed_lab.infrastructure.perfilcandidato.repository.ConsultaVagaRepository;
import cv.dge.dge_api_intermed_lab.infrastructure.perfilcandidato.repository.ConsultaVagaRepository.CandidaturaAnterior;
import cv.dge.dge_api_intermed_lab.infrastructure.perfilcandidato.repository.ConsultaVagaRepository.OfertaDetalhe;
import cv.dge.dge_api_intermed_lab.infrastructure.perfilcandidato.repository.ConsultaVagaRepository.OfertaResumo;
import java.text.Normalizer;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
@Slf4j
public class ConsultaVagaServiceImpl implements ConsultaVagaService {

    private static final String SITUACAO_ABERTA = "ABERTA";
    private static final String SITUACAO_A_TERMINAR = "A_TERMINAR";
    private static final String SITUACAO_ENCERRADA = "ENCERRADA";
    private static final String ESTADO_ATIVA = "ATIVA";
    private static final int DIAS_AVISO_TERMINO = 7;
    private static final String TIPO_DOCUMENTO_CURRICULO = "CURRICULO_VITAE";
    private static final String TIPO_DOCUMENTO_OUTRO = "OUTRO_DOCUMENTO";

    private final ConsultaVagaRepository vagaRepository;
    private final DocumentService documentService;
    private final ComboboxService comboboxService;

    @Value("${document.candidatura.app-code:interm_laboral}")
    private String appCodeDocumento;

    @Value("${document.candidatura.estado:A}")
    private String estadoDocumento;

    @Value("${document.candidatura.tipo-relacao:EMPREGO_T_CANDIDATURA_OFERTA}")
    private String tipoRelacaoDocumento;

    @Value("${document.candidatura.tipo-curriculo-id:}")
    private String tipoCurriculoIdConfigurado;

    @Value("${document.candidatura.tipo-outro-id:}")
    private String tipoOutroIdConfigurado;

    @Override
    @Transactional(readOnly = true)
    public ConsultaVagasResponse listar(ConsultaVagaFiltro filtro) {
        ConsultaVagaFiltro dados = normalizarFiltro(filtro);
        Map<String, String> geografias = new LinkedHashMap<>();
        List<ConsultaVagaListaResponse> ofertas = vagaRepository.listar(dados).stream()
                .map(oferta -> enriquecerResumo(oferta, geografias))
                .toList();

        long totalEmprego = ofertas.stream()
                .filter(item -> "OFERTA_EMPREGO".equals(item.tipoOferta()))
                .count();
        long totalEstagio = ofertas.stream()
                .filter(item -> "OFERTA_ESTAGIO".equals(item.tipoOferta()))
                .count();

        return new ConsultaVagasResponse(
                (long) ofertas.size(),
                totalEmprego,
                totalEstagio,
                ofertas,
                construirPontosMapa(ofertas)
        );
    }

    @Override
    @Transactional(readOnly = true)
    public ConsultaVagaDetalheResponse buscarPorId(Integer ofertaId, Long pessoaId) {
        validarOfertaId(ofertaId);
        validarPessoa(pessoaId);
        OfertaDetalhe oferta = vagaRepository.buscarOferta(ofertaId, pessoaId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "A oferta selecionada não está disponível. Atualize a lista e escolha outra oferta."
                ));
        return mapearDetalhe(oferta);
    }

    @Override
    @Transactional(readOnly = true)
    public ConsultaVagaOpcoesResponse listarOpcoes(Long ilhaId) {
        if (ilhaId != null && ilhaId <= 0) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "A ilha selecionada não é válida. Selecione novamente e tente de novo."
            );
        }
        return new ConsultaVagaOpcoesResponse(
                vagaRepository.listarEntidades(),
                vagaRepository.listarIlhas(),
                vagaRepository.listarConcelhos(ilhaId)
        );
    }

    @Override
    @Transactional(readOnly = true)
    public CandidaturaVagaFormularioResponse buscarFormularioCandidatura(Integer ofertaId, Long pessoaId) {
        OfertaDetalhe oferta = buscarOferta(ofertaId, pessoaId);
        Optional<CandidaturaAnterior> candidaturaOferta =
                vagaRepository.buscarCandidaturaDaOferta(ofertaId, pessoaId);
        CandidaturaAnterior origem = candidaturaOferta
                .or(() -> vagaRepository.buscarUltimaCandidatura(pessoaId))
                .orElse(null);

        return new CandidaturaVagaFormularioResponse(
                oferta.id(),
                oferta.codigoReferencia(),
                oferta.titulo(),
                pessoaId,
                origem == null ? null : extrairCurriculo(origem.anexos()),
                origem == null ? null : origem.habilitacaoAcademica(),
                origem == null ? null : origem.areaFormacao(),
                candidaturaOferta.isPresent(),
                candidaturaOferta.map(CandidaturaAnterior::id).orElse(null)
        );
    }

    @Override
    @Transactional
    public CandidaturaVagaResponse candidatar(
            Integer ofertaId,
            Long pessoaId,
            CandidaturaVagaRequest request,
            MultipartFile curriculo,
            List<MultipartFile> outrosDocumentos
    ) {
        OfertaDetalhe oferta = buscarOferta(ofertaId, pessoaId);
        validarCandidaturaDisponivel(oferta);
        DadosCandidatura dados = validarRequest(request);

        CandidaturaDocumentoResponse curriculoAnterior = vagaRepository.buscarUltimaCandidatura(pessoaId)
                .map(CandidaturaAnterior::anexos)
                .map(this::extrairCurriculo)
                .orElse(null);
        if (!temFicheiro(curriculo) && curriculoAnterior == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Anexe o seu currículo para concluir a candidatura."
            );
        }

        List<MultipartFile> ficheirosValidos = outrosDocumentos == null
                ? List.of()
                : outrosDocumentos.stream().filter(this::temFicheiro).toList();
        String idTipoCurriculo = temFicheiro(curriculo)
                ? resolverIdTipoDocumento(TIPO_DOCUMENTO_CURRICULO)
                : null;
        String idTipoOutro = ficheirosValidos.isEmpty()
                ? null
                : resolverIdTipoDocumento(TIPO_DOCUMENTO_OUTRO);

        String nomeCandidato = vagaRepository.buscarNomePessoa(pessoaId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Não foi possível encontrar os dados do candidato. Atualize a página, entre novamente e tente de novo."
                ));

        Integer candidaturaId = vagaRepository.inserirCandidatura(
                pessoaId,
                nomeCandidato,
                oferta,
                dados.habilitacaoAcademica(),
                dados.areaFormacao(),
                dados.utilizador()
        );
        if (candidaturaId == null) {
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Não foi possível concluir a candidatura neste momento. Tente novamente mais tarde."
            );
        }

        CandidaturaDocumentoResponse curriculumVitae = temFicheiro(curriculo)
                ? guardarDocumento(
                        candidaturaId,
                        curriculo,
                        TIPO_DOCUMENTO_CURRICULO,
                        idTipoCurriculo,
                        1
                )
                : curriculoAnterior;

        List<CandidaturaDocumentoResponse> outros = new ArrayList<>();
        for (int indice = 0; indice < ficheirosValidos.size(); indice++) {
            outros.add(guardarDocumento(
                    candidaturaId,
                    ficheirosValidos.get(indice),
                    TIPO_DOCUMENTO_OUTRO,
                    idTipoOutro,
                    indice + 1
            ));
        }

        Map<String, Object> anexos = new LinkedHashMap<>();
        anexos.put("curriculumVitae", curriculumVitae);
        anexos.put("outrosDocumentos", outros);
        vagaRepository.atualizarAnexos(candidaturaId, anexos);

        return new CandidaturaVagaResponse(
                candidaturaId,
                pessoaId,
                nomeCandidato,
                oferta.id(),
                oferta.codigoReferencia(),
                oferta.titulo(),
                normalizarTipoOferta(oferta.tipoOferta()),
                oferta.entidadeId(),
                oferta.denominacaoEntidade(),
                "PORTAL",
                "TRIAGEM",
                dados.habilitacaoAcademica(),
                dados.areaFormacao(),
                curriculumVitae,
                outros,
                LocalDateTime.now()
        );
    }

    private ConsultaVagaFiltro normalizarFiltro(ConsultaVagaFiltro filtro) {
        if (filtro == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Não foi possível carregar as ofertas. Atualize a página e tente novamente."
            );
        }
        validarPessoa(filtro.pessoaId());
        if (filtro.dataInicio() != null && filtro.dataFim() != null
                && filtro.dataFim().isBefore(filtro.dataInicio())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "A data de fim não pode ser anterior à data de início."
            );
        }
        return new ConsultaVagaFiltro(
                filtro.pessoaId(),
                normalizarDominioOpcional(EmpregoDominio.DOMINIO_TIPO_OFERTA, filtro.tipoOferta(),
                        "Selecione um tipo de oferta válido."),
                filtro.entidadeId(),
                textoOpcional(filtro.ilha()),
                textoOpcional(filtro.concelho()),
                normalizarDominioOpcional(EmpregoDominio.DOMINIO_ESTADO_OFERTA, filtro.estado(),
                        "Selecione um estado de oferta válido."),
                textoOpcional(filtro.codigoReferencia()),
                filtro.dataInicio(),
                filtro.dataFim(),
                textoOpcional(filtro.pesquisa())
        );
    }

    private ConsultaVagaListaResponse enriquecerResumo(OfertaResumo oferta, Map<String, String> geografias) {
        String tipoOferta = normalizarTipoOferta(oferta.tipoOferta());
        String estado = normalizarEstado(oferta.estado());
        String situacao = situacao(oferta.estado(), oferta.dataFimCandidatura(), oferta.numVagas());
        String ilhaDesc = descricaoGeografia(oferta.ilha(), geografias);
        String concelhoDesc = descricaoGeografia(oferta.concelho(), geografias);
        boolean podeCandidatar = podeCandidatar(
                oferta.estado(),
                oferta.dataInicioCandidatura(),
                oferta.dataFimCandidatura(),
                oferta.numVagas(),
                Boolean.TRUE.equals(oferta.jaCandidatado())
        );
        return new ConsultaVagaListaResponse(
                oferta.id(),
                oferta.titulo(),
                tipoOferta,
                EmpregoDominio.descricao(EmpregoDominio.DOMINIO_TIPO_OFERTA, tipoOferta),
                oferta.ilha(),
                ilhaDesc,
                oferta.concelho(),
                concelhoDesc,
                juntarLocal(ilhaDesc, concelhoDesc),
                oferta.numVagas(),
                oferta.entidadeId(),
                oferta.denominacaoEntidade(),
                oferta.codigoReferencia(),
                estado,
                EmpregoDominio.descricao(EmpregoDominio.DOMINIO_ESTADO_OFERTA, estado),
                oferta.dataInicioCandidatura(),
                oferta.dataFimCandidatura(),
                diasRestantes(oferta.dataFimCandidatura()),
                situacao,
                descricaoSituacao(situacao),
                oferta.jaCandidatado(),
                podeCandidatar,
                motivoIndisponibilidade(oferta)
        );
    }

    private ConsultaVagaDetalheResponse mapearDetalhe(OfertaDetalhe oferta) {
        Map<String, String> geografias = new LinkedHashMap<>();
        String ilhaDesc = descricaoGeografia(oferta.ilha(), geografias);
        String concelhoDesc = descricaoGeografia(oferta.concelho(), geografias);
        String tipoOferta = normalizarTipoOferta(oferta.tipoOferta());
        String estado = normalizarEstado(oferta.estado());
        String situacao = situacao(oferta.estado(), oferta.dataFimCandidatura(), oferta.numVagas());
        boolean podeCandidatar = podeCandidatar(
                oferta.estado(),
                oferta.dataInicioCandidatura(),
                oferta.dataFimCandidatura(),
                oferta.numVagas(),
                Boolean.TRUE.equals(oferta.jaCandidatado())
        );
        return new ConsultaVagaDetalheResponse(
                oferta.id(),
                oferta.codigoReferencia(),
                tipoOferta,
                EmpregoDominio.descricao(EmpregoDominio.DOMINIO_TIPO_OFERTA, tipoOferta),
                oferta.titulo(),
                oferta.descricao(),
                oferta.dataInicioCandidatura(),
                oferta.dataFimCandidatura(),
                oferta.dataInicioPrevisto(),
                oferta.duracaoContrato(),
                oferta.regimeContrato(),
                EmpregoDominio.descricao(EmpregoDominio.DOMINIO_REGIME_CONTRATO, oferta.regimeContrato()),
                oferta.entidadeId(),
                oferta.denominacaoEntidade(),
                oferta.habilitacaoMinima(),
                EmpregoDominio.descricao(EmpregoDominio.DOMINIO_HABILITACAO_LITERARIA, oferta.habilitacaoMinima()),
                oferta.nivelQualificacao(),
                EmpregoDominio.descricao(EmpregoDominio.DOMINIO_NIVEL_QUALIFICACAO, oferta.nivelQualificacao()),
                oferta.numVagas(),
                oferta.habilitacaoMaxima(),
                EmpregoDominio.descricao(EmpregoDominio.DOMINIO_HABILITACAO_LITERARIA, oferta.habilitacaoMaxima()),
                oferta.conhecimentoLinguistico(),
                oferta.competenciasValorizadas(),
                oferta.horaInicio(),
                oferta.horaFim(),
                oferta.diasSemana(),
                oferta.cursosAreaFormacao(),
                oferta.experienciaProfissional(),
                oferta.ilha(),
                ilhaDesc,
                oferta.concelho(),
                concelhoDesc,
                juntarLocal(ilhaDesc, concelhoDesc),
                oferta.orientadorDenominacao(),
                oferta.coordenadorDenominacao(),
                oferta.emailContacto(),
                oferta.contacto(),
                oferta.observacao(),
                estado,
                EmpregoDominio.descricao(EmpregoDominio.DOMINIO_ESTADO_OFERTA, estado),
                diasRestantes(oferta.dataFimCandidatura()),
                situacao,
                descricaoSituacao(situacao),
                oferta.jaCandidatado(),
                podeCandidatar,
                motivoIndisponibilidade(oferta)
        );
    }

    private List<ConsultaVagaMapaResponse> construirPontosMapa(List<ConsultaVagaListaResponse> ofertas) {
        Map<String, PontoMapa> pontos = new LinkedHashMap<>();
        for (ConsultaVagaListaResponse oferta : ofertas) {
            String chave = String.valueOf(oferta.ilha()) + '|' + oferta.concelho();
            PontoMapa ponto = pontos.computeIfAbsent(chave, ignorado -> new PontoMapa(
                    oferta.ilha(),
                    oferta.ilhaDesc(),
                    oferta.concelho(),
                    oferta.concelhoDesc()
            ));
            ponto.adicionar(oferta);
        }
        return pontos.values().stream().map(PontoMapa::toResponse).toList();
    }

    private OfertaDetalhe buscarOferta(Integer ofertaId, Long pessoaId) {
        validarOfertaId(ofertaId);
        validarPessoa(pessoaId);
        return vagaRepository.buscarOferta(ofertaId, pessoaId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "A oferta selecionada não está disponível. Atualize a lista e escolha outra oferta."
                ));
    }

    private void validarCandidaturaDisponivel(OfertaDetalhe oferta) {
        if (Boolean.TRUE.equals(oferta.jaCandidatado())) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Já existe uma candidatura sua para esta oferta. Consulte as suas candidaturas para acompanhar o processo."
            );
        }
        String motivo = motivoIndisponibilidade(oferta);
        if (motivo != null) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, motivo);
        }
    }

    private DadosCandidatura validarRequest(CandidaturaVagaRequest request) {
        if (request == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Preencha os dados da candidatura antes de confirmar."
            );
        }
        String habilitacao = textoObrigatorio(
                request.habilitacaoAcademica(),
                "Selecione a sua habilitação literária."
        );
        habilitacao = EmpregoDominio.valorOficial(EmpregoDominio.DOMINIO_HABILITACAO_LITERARIA, habilitacao)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Selecione uma habilitação literária válida."
                ));
        return new DadosCandidatura(
                habilitacao,
                textoObrigatorio(request.areaFormacao(), "Informe a sua área de formação."),
                textoObrigatorio(
                        request.utilizador(),
                        "Não foi possível identificar o utilizador. Entre novamente e tente de novo."
                )
        );
    }

    private CandidaturaDocumentoResponse guardarDocumento(
            Integer candidaturaId,
            MultipartFile ficheiro,
            String tipo,
            String idTipoDocumento,
            int indice
    ) {
        String nomeOriginal = StringUtils.cleanPath(
                Optional.ofNullable(ficheiro.getOriginalFilename()).orElse("documento-" + indice)
        );
        String nomeBase = sanitizarSegmentoPath(removerExtensao(nomeOriginal));
        String nomeArmazenamento = sanitizarSegmentoPath(tipo)
                + "-" + indice + "-" + nomeBase;
        String pathDocumento = construirPathDocumento(
                candidaturaId,
                nomeArmazenamento,
                nomeOriginal
        );
        try {
            String path = documentService.save(DocRelacaoDTO.builder()
                    .idRelacao(candidaturaId)
                    .tipoRelacao(tipoRelacaoDocumento)
                    .estado(estadoDocumento)
                    .name(nomeOriginal)
                    .idTpDoc(idTipoDocumento)
                    .fileName(nomeArmazenamento)
                    .path(pathDocumento)
                    .appCode(appCodeDocumento)
                    .file(ficheiro)
                    .build());
            return new CandidaturaDocumentoResponse(
                    tipo,
                    nomeOriginal,
                    path,
                    documentService.gerarLinkPublico(path)
            );
        } catch (RuntimeException ex) {
            registarErroUpload(candidaturaId, tipo, idTipoDocumento, nomeOriginal, ex);
            throw new ResponseStatusException(
                    HttpStatus.BAD_GATEWAY,
                    "Não foi possível guardar os documentos da candidatura. Tente novamente mais tarde.",
                    ex
            );
        }
    }

    private String resolverIdTipoDocumento(String tipo) {
        String configurado = TIPO_DOCUMENTO_CURRICULO.equals(tipo)
                ? tipoCurriculoIdConfigurado
                : tipoOutroIdConfigurado;
        if (temTexto(configurado)) {
            return validarIdTipoDocumento(configurado, tipo);
        }

        return comboboxService.listarDocumentosAtivos().stream()
                .filter(item -> correspondeTipoDocumento(item, tipo))
                .map(item -> item.get("id"))
                .filter(this::idTipoDocumentoValido)
                .map(String::valueOf)
                .findFirst()
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.INTERNAL_SERVER_ERROR,
                        "Não foi encontrado no catálogo um tipo documental ativo para "
                                + descricaoTipoDocumento(tipo)
                                + ". Configure o respetivo ID antes de submeter a candidatura."
                ));
    }

    private boolean correspondeTipoDocumento(Map<String, Object> item, String tipo) {
        Object descricao = item == null ? null : item.get("tipo_documento_desc");
        String normalizado = normalizarParaPesquisa(descricao == null ? null : descricao.toString());
        if (!temTexto(normalizado)) {
            return false;
        }
        if (TIPO_DOCUMENTO_CURRICULO.equals(tipo)) {
            return normalizado.contains("CURRIC") || "CV".equals(normalizado);
        }
        return (normalizado.contains("OUTRO") && normalizado.contains("DOCUMENT"))
                || normalizado.contains("DOCUMENTO COMPLEMENTAR")
                || normalizado.startsWith("ANEX");
    }

    private String validarIdTipoDocumento(String valor, String tipo) {
        String limpo = valor.trim();
        if (idTipoDocumentoValido(limpo)) {
            return limpo;
        }
        throw new ResponseStatusException(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "O ID documental configurado para " + descricaoTipoDocumento(tipo) + " não é válido."
        );
    }

    private boolean idTipoDocumentoValido(Object valor) {
        if (valor == null) {
            return false;
        }
        try {
            return Long.parseLong(valor.toString().trim()) > 0;
        } catch (NumberFormatException ex) {
            return false;
        }
    }

    private String descricaoTipoDocumento(String tipo) {
        return TIPO_DOCUMENTO_CURRICULO.equals(tipo) ? "o currículo" : "outro documento";
    }

    private String construirPathDocumento(
            Integer candidaturaId,
            String nomeArmazenamento,
            String nomeOriginal
    ) {
        return appCodeDocumento
                + "/"
                + LocalDateTime.now().getYear()
                + "/modulos/"
                + sanitizarSegmentoPath(tipoRelacaoDocumento)
                + "/"
                + candidaturaId
                + "/"
                + nomeArmazenamento
                + extensao(nomeOriginal);
    }

    private String sanitizarSegmentoPath(String valor) {
        String limpo = textoOpcional(valor);
        if (limpo == null) {
            return "documento";
        }
        return limpo
                .replaceAll("[\\\\/:*?\"<>|]+", "_")
                .replace(' ', '_');
    }

    private String extensao(String nomeFicheiro) {
        if (!temTexto(nomeFicheiro)) {
            return "";
        }
        int indice = nomeFicheiro.lastIndexOf('.');
        return indice < 0 ? "" : nomeFicheiro.substring(indice);
    }

    private String normalizarParaPesquisa(String valor) {
        if (!temTexto(valor)) {
            return null;
        }
        return Normalizer.normalize(valor, Normalizer.Form.NFD)
                .replaceAll("\\p{M}+", "")
                .trim()
                .toUpperCase(Locale.ROOT);
    }

    private void registarErroUpload(
            Integer candidaturaId,
            String tipo,
            String idTipoDocumento,
            String nomeOriginal,
            RuntimeException ex
    ) {
        if (ex instanceof HttpStatusCodeException httpException) {
            log.error(
                    "Falha no serviço documental da candidatura: candidaturaId={}, tipo={}, idTpDoc={}, "
                            + "ficheiro={}, status={}, resposta={}",
                    candidaturaId,
                    tipo,
                    idTipoDocumento,
                    nomeOriginal,
                    httpException.getStatusCode(),
                    httpException.getResponseBodyAsString(),
                    ex
            );
            return;
        }
        log.error(
                "Falha no serviço documental da candidatura: candidaturaId={}, tipo={}, idTpDoc={}, ficheiro={}",
                candidaturaId,
                tipo,
                idTipoDocumento,
                nomeOriginal,
                ex
        );
    }

    private CandidaturaDocumentoResponse extrairCurriculo(Object anexos) {
        if (anexos instanceof Map<?, ?> mapa) {
            Object curriculo = primeiroValor(mapa, "curriculumVitae", "curriculoVitae", "curriculo", "cv");
            CandidaturaDocumentoResponse documento = converterDocumento(curriculo, TIPO_DOCUMENTO_CURRICULO);
            if (documento != null) {
                return documento;
            }
            for (Object valor : mapa.values()) {
                documento = procurarCurriculo(valor);
                if (documento != null) {
                    return documento;
                }
            }
        }
        return procurarCurriculo(anexos);
    }

    private CandidaturaDocumentoResponse procurarCurriculo(Object valor) {
        if (valor instanceof List<?> lista) {
            for (Object item : lista) {
                CandidaturaDocumentoResponse documento = converterDocumento(item, null);
                if (documento != null && documento.tipo() != null
                        && normalizarTexto(documento.tipo()).contains("CURRIC")) {
                    return documento;
                }
            }
        }
        return null;
    }

    private CandidaturaDocumentoResponse converterDocumento(Object valor, String tipoPadrao) {
        if (valor instanceof String path && temTexto(path)) {
            return new CandidaturaDocumentoResponse(
                    tipoPadrao,
                    nomeDoPath(path),
                    path,
                    documentService.gerarLinkPublico(path)
            );
        }
        if (!(valor instanceof Map<?, ?> mapa)) {
            return null;
        }
        String tipo = primeiroTexto(mapa, "tipo", "tipoDocumento", "idTpDoc", "id_tp_doc");
        String path = primeiroTexto(mapa, "path", "caminho", "ver_documento", "url");
        String url = primeiroTexto(mapa, "url", "previewUrl", "ver_documento");
        String nome = primeiroTexto(mapa, "nome", "name", "fileName", "ficheiro");
        if (!temTexto(path) && !temTexto(url)) {
            return null;
        }
        if (!temTexto(url) && temTexto(path)) {
            url = documentService.gerarLinkPublico(path);
        }
        if (!temTexto(nome)) {
            nome = nomeDoPath(path);
        }
        return new CandidaturaDocumentoResponse(
                temTexto(tipo) ? tipo : tipoPadrao,
                nome,
                path,
                url
        );
    }

    private Object primeiroValor(Map<?, ?> mapa, String... chaves) {
        for (String chave : chaves) {
            if (mapa.containsKey(chave)) {
                return mapa.get(chave);
            }
        }
        return null;
    }

    private String primeiroTexto(Map<?, ?> mapa, String... chaves) {
        Object valor = primeiroValor(mapa, chaves);
        return valor == null ? null : textoOpcional(String.valueOf(valor));
    }

    private String descricaoGeografia(String codigo, Map<String, String> cache) {
        if (!temTexto(codigo)) {
            return null;
        }
        return cache.computeIfAbsent(codigo, chave -> chave);
    }

    private boolean podeCandidatar(
            String estado,
            LocalDate dataInicio,
            LocalDate dataFim,
            Integer numVagas,
            boolean jaCandidatado
    ) {
        if (jaCandidatado || !ESTADO_ATIVA.equals(normalizarEstado(estado))) {
            return false;
        }
        LocalDate hoje = LocalDate.now();
        return (numVagas == null || numVagas > 0)
                && (dataInicio == null || !hoje.isBefore(dataInicio))
                && (dataFim == null || !hoje.isAfter(dataFim));
    }

    private String motivoIndisponibilidade(OfertaResumo oferta) {
        return motivoIndisponibilidade(
                oferta.estado(), oferta.dataInicioCandidatura(), oferta.dataFimCandidatura(),
                oferta.numVagas(), Boolean.TRUE.equals(oferta.jaCandidatado())
        );
    }

    private String motivoIndisponibilidade(OfertaDetalhe oferta) {
        return motivoIndisponibilidade(
                oferta.estado(), oferta.dataInicioCandidatura(), oferta.dataFimCandidatura(),
                oferta.numVagas(), Boolean.TRUE.equals(oferta.jaCandidatado())
        );
    }

    private String motivoIndisponibilidade(
            String estado,
            LocalDate dataInicio,
            LocalDate dataFim,
            Integer numVagas,
            boolean jaCandidatado
    ) {
        if (jaCandidatado) {
            return "Já se candidatou a esta oferta.";
        }
        if (!ESTADO_ATIVA.equals(normalizarEstado(estado))) {
            return "Esta oferta já não aceita candidaturas.";
        }
        if (numVagas != null && numVagas <= 0) {
            return "Esta oferta já não tem vagas disponíveis.";
        }
        LocalDate hoje = LocalDate.now();
        if (dataInicio != null && hoje.isBefore(dataInicio)) {
            return "O período de candidatura para esta oferta ainda não começou.";
        }
        if (dataFim != null && hoje.isAfter(dataFim)) {
            return "O prazo de candidatura para esta oferta já terminou.";
        }
        return null;
    }

    private String situacao(String estado, LocalDate dataFim, Integer numVagas) {
        LocalDate hoje = LocalDate.now();
        if (!ESTADO_ATIVA.equals(normalizarEstado(estado))
                || (dataFim != null && hoje.isAfter(dataFim))
                || (numVagas != null && numVagas <= 0)) {
            return SITUACAO_ENCERRADA;
        }
        if (dataFim != null) {
            long dias = ChronoUnit.DAYS.between(hoje, dataFim);
            if (dias >= 0 && dias <= DIAS_AVISO_TERMINO) {
                return SITUACAO_A_TERMINAR;
            }
        }
        return SITUACAO_ABERTA;
    }

    private String descricaoSituacao(String situacao) {
        return switch (situacao) {
            case SITUACAO_A_TERMINAR -> "A terminar";
            case SITUACAO_ENCERRADA -> "Encerrada";
            default -> "Aberta";
        };
    }

    private Long diasRestantes(LocalDate dataFim) {
        if (dataFim == null) {
            return null;
        }
        return Math.max(0L, ChronoUnit.DAYS.between(LocalDate.now(), dataFim));
    }

    private String normalizarTipoOferta(String valor) {
        return EmpregoDominio.valorOficial(EmpregoDominio.DOMINIO_TIPO_OFERTA, valor)
                .orElseGet(() -> normalizarTexto(valor));
    }

    private String normalizarEstado(String valor) {
        String normalizado = normalizarTexto(valor);
        if ("A".equals(normalizado) || "ATIVO".equals(normalizado)) {
            return ESTADO_ATIVA;
        }
        if ("F".equals(normalizado) || "FECHADO".equals(normalizado)) {
            return "FECHADA";
        }
        return EmpregoDominio.valorOficial(EmpregoDominio.DOMINIO_ESTADO_OFERTA, valor)
                .orElse(normalizado);
    }

    private String normalizarDominioOpcional(String dominio, String valor, String mensagem) {
        if (!temTexto(valor)) {
            return null;
        }
        return EmpregoDominio.valorOficial(dominio, valor)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, mensagem));
    }

    private String normalizarTexto(String valor) {
        return valor == null ? null : valor.trim().toUpperCase(Locale.ROOT);
    }

    private String textoObrigatorio(String valor, String mensagem) {
        if (!temTexto(valor)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, mensagem);
        }
        return valor.trim();
    }

    private String textoOpcional(String valor) {
        return temTexto(valor) ? valor.trim() : null;
    }

    private void validarPessoa(Long pessoaId) {
        if (pessoaId == null || pessoaId <= 0) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Não foi possível identificar o candidato. Atualize a página, entre novamente e tente de novo."
            );
        }
    }

    private void validarOfertaId(Integer ofertaId) {
        if (ofertaId == null || ofertaId <= 0) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Não foi possível identificar a oferta selecionada. Atualize a lista e tente novamente."
            );
        }
    }

    private boolean temFicheiro(MultipartFile ficheiro) {
        return ficheiro != null && !ficheiro.isEmpty();
    }

    private boolean temTexto(String valor) {
        return valor != null && !valor.trim().isEmpty();
    }

    private String removerExtensao(String nome) {
        int indice = nome.lastIndexOf('.');
        return indice <= 0 ? nome : nome.substring(0, indice);
    }

    private String nomeDoPath(String path) {
        if (!temTexto(path)) {
            return null;
        }
        int indice = Math.max(path.lastIndexOf('/'), path.lastIndexOf('\\'));
        return indice < 0 ? path : path.substring(indice + 1);
    }

    private String juntarLocal(String ilha, String concelho) {
        if (!temTexto(ilha)) {
            return concelho;
        }
        if (!temTexto(concelho)) {
            return ilha;
        }
        return ilha + " - " + concelho;
    }

    private record DadosCandidatura(
            String habilitacaoAcademica,
            String areaFormacao,
            String utilizador
    ) {
    }

    private static final class PontoMapa {
        private final String ilha;
        private final String ilhaDesc;
        private final String concelho;
        private final String concelhoDesc;
        private final List<Integer> ofertaIds = new ArrayList<>();
        private long abertas;
        private long aTerminar;
        private long encerradas;

        private PontoMapa(String ilha, String ilhaDesc, String concelho, String concelhoDesc) {
            this.ilha = ilha;
            this.ilhaDesc = ilhaDesc;
            this.concelho = concelho;
            this.concelhoDesc = concelhoDesc;
        }

        private void adicionar(ConsultaVagaListaResponse oferta) {
            ofertaIds.add(oferta.id());
            switch (oferta.situacao()) {
                case SITUACAO_A_TERMINAR -> aTerminar++;
                case SITUACAO_ENCERRADA -> encerradas++;
                default -> abertas++;
            }
        }

        private ConsultaVagaMapaResponse toResponse() {
            String situacao = aTerminar > 0
                    ? SITUACAO_A_TERMINAR
                    : abertas > 0 ? SITUACAO_ABERTA : SITUACAO_ENCERRADA;
            String descricao = switch (situacao) {
                case SITUACAO_A_TERMINAR -> "A terminar";
                case SITUACAO_ENCERRADA -> "Encerrada";
                default -> "Aberta";
            };
            return new ConsultaVagaMapaResponse(
                    ilha,
                    ilhaDesc,
                    concelho,
                    concelhoDesc,
                    (long) ofertaIds.size(),
                    abertas,
                    aTerminar,
                    encerradas,
                    situacao,
                    descricao,
                    List.copyOf(ofertaIds)
            );
        }
    }
}
