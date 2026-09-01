package cv.dge.dge_api_intermed_lab.application.perfilcandidato.service;

import cv.dge.dge_api_intermed_lab.application.document.dto.DocRelacaoDTO;
import cv.dge.dge_api_intermed_lab.application.document.service.ComboboxService;
import cv.dge.dge_api_intermed_lab.application.document.service.DocumentService;
import cv.dge.dge_api_intermed_lab.application.geografia.service.GlobalGeografiaService;
import cv.dge.dge_api_intermed_lab.application.perfilcandidato.dto.MinhaCandidaturaOpcaoResponse;
import cv.dge.dge_api_intermed_lab.application.perfilcandidato.dto.ServicoContratanteAnexoResponse;
import cv.dge.dge_api_intermed_lab.application.perfilcandidato.dto.ServicoContratanteCandidatoFiltro;
import cv.dge.dge_api_intermed_lab.application.perfilcandidato.dto.ServicoContratanteCandidatoListaResponse;
import cv.dge.dge_api_intermed_lab.application.perfilcandidato.dto.ServicoContratanteCandidatosOpcoesResponse;
import cv.dge.dge_api_intermed_lab.application.perfilcandidato.dto.ServicoContratanteDetalheResponse;
import cv.dge.dge_api_intermed_lab.application.perfilcandidato.dto.ServicoContratanteFiltro;
import cv.dge.dge_api_intermed_lab.application.perfilcandidato.dto.ServicoContratanteListaResponse;
import cv.dge.dge_api_intermed_lab.application.perfilcandidato.dto.ServicoContratanteOpcoesResponse;
import cv.dge.dge_api_intermed_lab.application.perfilcandidato.dto.ServicoContratanteRequest;
import cv.dge.dge_api_intermed_lab.application.perfilentidade.enums.EmpregoDominio;
import cv.dge.dge_api_intermed_lab.infrastructure.perfilcandidato.repository.ServicoContratanteRepository;
import cv.dge.dge_api_intermed_lab.infrastructure.perfilcandidato.repository.ServicoContratanteRepository.AnexoArmazenado;
import cv.dge.dge_api_intermed_lab.infrastructure.perfilcandidato.repository.ServicoContratanteRepository.CandidatoRegisto;
import cv.dge.dge_api_intermed_lab.infrastructure.perfilcandidato.repository.ServicoContratanteRepository.ServicoRegisto;
import java.text.Normalizer;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Pattern;
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
public class ServicoContratanteServiceImpl implements ServicoContratanteService {

    private static final String ESTADO_ATIVO = "A";
    private static final String ESTADO_RASCUNHO = "R";
    private static final String ESTADO_CANCELADO = "C";
    private static final String ESTADO_ELIMINADO = "E";
    private static final String ESTADO_CANDIDATURA_SELECIONADA = "SELECIONADO";
    private static final DateTimeFormatter SUFIXO_DOCUMENTO = DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS");
    private static final Pattern EMAIL = Pattern.compile("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$");

    private final ServicoContratanteRepository servicoRepository;
    private final GlobalGeografiaService globalGeografiaService;
    private final DocumentService documentService;
    private final ComboboxService comboboxService;

    @Value("${document.intermediacao.app-code:interm_laboral}")
    private String appCodeDocumento;

    @Value("${document.intermediacao.estado:A}")
    private String estadoDocumento;

    @Value("${document.intermediacao.tipo-relacao:EMPREGO_T_INTERMEDIACAO}")
    private String tipoRelacaoDocumento;

    @Value("${document.intermediacao.tipo-documento-id:}")
    private String tipoDocumentoIdConfigurado;

    @Override
    @Transactional(readOnly = true)
    public List<ServicoContratanteListaResponse> listar(ServicoContratanteFiltro filtro) {
        ServicoContratanteFiltro dados = normalizarFiltro(filtro);
        return servicoRepository.listar(dados).stream()
                .map(this::mapearLista)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public ServicoContratanteOpcoesResponse listarOpcoes(String ilha, String concelho) {
        String ilhaLimpa = textoOpcional(ilha);
        String concelhoLimpo = textoOpcional(concelho);
        if (concelhoLimpo != null && ilhaLimpa == null) {
            throw erro(HttpStatus.BAD_REQUEST, "Selecione primeiro a ilha para consultar os concelhos e zonas.");
        }
        return new ServicoContratanteOpcoesResponse(
                listarDominio(EmpregoDominio.DOMINIO_ESTADO_SERVICO),
                servicoRepository.listarIlhas(),
                servicoRepository.listarConcelhos(ilhaLimpa),
                servicoRepository.listarZonas(concelhoLimpo)
        );
    }

    @Override
    @Transactional(readOnly = true)
    public ServicoContratanteDetalheResponse buscarPorId(Integer servicoId, Long pessoaId) {
        validarServico(servicoId);
        validarPessoa(pessoaId);
        return mapearDetalhe(buscarRegisto(servicoId, pessoaId));
    }

    @Override
    @Transactional
    public ServicoContratanteDetalheResponse criar(
            Long pessoaId,
            ServicoContratanteRequest request,
            List<MultipartFile> anexos,
            boolean rascunho
    ) {
        validarPessoa(pessoaId);
        ServicoContratanteRequest dados = validarRequest(request);
        String nomeContratante = servicoRepository.buscarNomePessoa(pessoaId)
                .orElseThrow(() -> erro(
                        HttpStatus.NOT_FOUND,
                        "Não foi possível encontrar os dados do contratante. Entre novamente e tente de novo."
                ));
        String estado = rascunho ? ESTADO_RASCUNHO : ESTADO_ATIVO;
        Integer servicoId = servicoRepository.inserir(pessoaId, nomeContratante, dados, estado);
        if (servicoId == null) {
            throw erro(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Não foi possível registar o serviço neste momento. Tente novamente mais tarde."
            );
        }

        List<AnexoArmazenado> documentos = guardarDocumentos(
                servicoId,
                ficheirosValidos(anexos),
                1
        );
        if (!documentos.isEmpty() && !servicoRepository.atualizarAnexos(servicoId, pessoaId, documentos)) {
            throw servicoNaoEncontrado();
        }
        return mapearDetalhe(buscarRegisto(servicoId, pessoaId));
    }

    @Override
    @Transactional
    public ServicoContratanteDetalheResponse atualizar(
            Integer servicoId,
            Long pessoaId,
            ServicoContratanteRequest request,
            List<MultipartFile> novosAnexos
    ) {
        validarServico(servicoId);
        validarPessoa(pessoaId);
        ServicoContratanteRequest dados = validarRequest(request);
        ServicoRegisto atual = buscarRegisto(servicoId, pessoaId);
        garantirEditavel(atual);

        List<AnexoArmazenado> mantidos = resolverAnexosMantidos(atual.anexos(), dados.anexosMantidos());
        List<MultipartFile> novos = ficheirosValidos(novosAnexos);
        List<AnexoArmazenado> resultadoAnexos = new ArrayList<>(mantidos);
        resultadoAnexos.addAll(guardarDocumentos(servicoId, novos, mantidos.size() + 1));

        if (!servicoRepository.atualizar(servicoId, pessoaId, dados)) {
            throw servicoNaoEncontrado();
        }
        if ((dados.anexosMantidos() != null || !novos.isEmpty())
                && !servicoRepository.atualizarAnexos(servicoId, pessoaId, resultadoAnexos)) {
            throw servicoNaoEncontrado();
        }
        return mapearDetalhe(buscarRegisto(servicoId, pessoaId));
    }

    @Override
    @Transactional
    public ServicoContratanteDetalheResponse cancelar(Integer servicoId, Long pessoaId, String utilizador) {
        return alterarEstado(servicoId, pessoaId, utilizador, ESTADO_CANCELADO);
    }

    @Override
    @Transactional
    public ServicoContratanteDetalheResponse remover(Integer servicoId, Long pessoaId, String utilizador) {
        return alterarEstado(servicoId, pessoaId, utilizador, ESTADO_ELIMINADO);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ServicoContratanteCandidatoListaResponse> listarCandidatos(
            ServicoContratanteCandidatoFiltro filtro
    ) {
        ServicoContratanteCandidatoFiltro dados = normalizarFiltroCandidato(filtro);
        buscarRegisto(dados.servicoId(), dados.contratanteId());
        return servicoRepository.listarCandidatos(dados).stream()
                .map(this::mapearCandidato)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public ServicoContratanteCandidatosOpcoesResponse listarOpcoesCandidatos(
            Integer servicoId,
            Long pessoaId
    ) {
        validarServico(servicoId);
        validarPessoa(pessoaId);
        buscarRegisto(servicoId, pessoaId);
        return new ServicoContratanteCandidatosOpcoesResponse(
                listarDominio(EmpregoDominio.DOMINIO_CANDIDATURA_STATUS),
                servicoRepository.listarCandidatosParaFiltro(servicoId, pessoaId)
        );
    }

    @Override
    @Transactional
    public ServicoContratanteCandidatoListaResponse selecionarCandidato(
            Integer servicoId,
            Integer candidaturaId,
            Long pessoaId,
            String utilizador
    ) {
        validarServico(servicoId);
        validarCandidatura(candidaturaId);
        validarPessoa(pessoaId);
        String utilizadorLimpo = validarUtilizador(utilizador);
        ServicoRegisto servico = buscarRegisto(servicoId, pessoaId);
        if (!ESTADO_ATIVO.equals(normalizarEstadoServico(servico.estado()))) {
            throw erro(HttpStatus.CONFLICT, "Só é possível selecionar candidatos de um serviço ativo.");
        }
        CandidatoRegisto candidato = buscarCandidato(servicoId, candidaturaId, pessoaId);
        if (!ESTADO_CANDIDATURA_SELECIONADA.equals(normalizarEstadoCandidatura(candidato.estado()))) {
            if (!servicoRepository.selecionarCandidato(
                    servicoId,
                    candidaturaId,
                    pessoaId,
                    ESTADO_CANDIDATURA_SELECIONADA,
                    utilizadorLimpo
            )) {
                throw candidaturaNaoEncontrada();
            }
            candidato = buscarCandidato(servicoId, candidaturaId, pessoaId);
        }
        return mapearCandidato(candidato);
    }

    private ServicoContratanteDetalheResponse alterarEstado(
            Integer servicoId,
            Long pessoaId,
            String utilizador,
            String novoEstado
    ) {
        validarServico(servicoId);
        validarPessoa(pessoaId);
        String utilizadorLimpo = validarUtilizador(utilizador);
        ServicoRegisto atual = buscarRegisto(servicoId, pessoaId);
        String estadoAtual = normalizarEstadoServico(atual.estado());
        if (novoEstado.equals(estadoAtual)) {
            return mapearDetalhe(atual);
        }
        if (ESTADO_ELIMINADO.equals(estadoAtual)) {
            throw erro(HttpStatus.CONFLICT, "O serviço já foi removido e não pode ser alterado.");
        }
        if (ESTADO_CANCELADO.equals(novoEstado) && ESTADO_CANCELADO.equals(estadoAtual)) {
            return mapearDetalhe(atual);
        }
        if (!servicoRepository.alterarEstado(servicoId, pessoaId, novoEstado, utilizadorLimpo)) {
            throw servicoNaoEncontrado();
        }
        return mapearDetalhe(buscarRegisto(servicoId, pessoaId));
    }

    private ServicoContratanteFiltro normalizarFiltro(ServicoContratanteFiltro filtro) {
        if (filtro == null) {
            throw erro(HttpStatus.BAD_REQUEST, "Não foi possível carregar os serviços. Atualize a página.");
        }
        validarPessoa(filtro.pessoaId());
        validarPeriodo(filtro.dataInicio(), filtro.dataFim());
        return new ServicoContratanteFiltro(
                filtro.pessoaId(),
                textoOpcional(filtro.tipoServico()),
                normalizarDominioOpcional(
                        EmpregoDominio.DOMINIO_ESTADO_SERVICO,
                        filtro.estado(),
                        "Selecione um estado de serviço válido."
                ),
                filtro.dataInicio(),
                filtro.dataFim()
        );
    }

    private ServicoContratanteCandidatoFiltro normalizarFiltroCandidato(
            ServicoContratanteCandidatoFiltro filtro
    ) {
        if (filtro == null) {
            throw erro(HttpStatus.BAD_REQUEST, "Não foi possível carregar os candidatos. Atualize a página.");
        }
        validarServico(filtro.servicoId());
        validarPessoa(filtro.contratanteId());
        if (filtro.candidatoId() != null && filtro.candidatoId() <= 0) {
            throw erro(HttpStatus.BAD_REQUEST, "O candidato selecionado não é válido.");
        }
        validarPeriodo(filtro.dataInicio(), filtro.dataFim());
        return new ServicoContratanteCandidatoFiltro(
                filtro.servicoId(),
                filtro.contratanteId(),
                filtro.candidatoId(),
                normalizarDominioOpcional(
                        EmpregoDominio.DOMINIO_CANDIDATURA_STATUS,
                        filtro.estado(),
                        "Selecione um estado de candidatura válido."
                ),
                filtro.dataInicio(),
                filtro.dataFim()
        );
    }

    private ServicoContratanteRequest validarRequest(ServicoContratanteRequest request) {
        if (request == null) {
            throw erro(HttpStatus.BAD_REQUEST, "Preencha os dados do serviço antes de guardar.");
        }
        String tipoServico = textoObrigatorio(request.tipoServico(), "Informe o tipo de serviço.");
        String titulo = textoObrigatorio(request.titulo(), "Informe o título do serviço.");
        validarTamanho(tipoServico, 150, "O tipo de serviço não pode exceder 150 caracteres.");
        validarTamanho(titulo, 250, "O título não pode exceder 250 caracteres.");
        if (request.dataPretendida() == null) {
            throw erro(HttpStatus.BAD_REQUEST, "Informe a data pretendida para o serviço.");
        }
        if (request.inicioCandidatura() == null || request.fimCandidatura() == null) {
            throw erro(HttpStatus.BAD_REQUEST, "Informe o período de candidatura.");
        }
        validarPeriodo(request.inicioCandidatura(), request.fimCandidatura());
        if (request.dataPretendida().isBefore(request.fimCandidatura())) {
            throw erro(
                    HttpStatus.BAD_REQUEST,
                    "A data pretendida do serviço não pode ser anterior ao fim das candidaturas."
            );
        }
        if (request.valorPrevisto() != null && request.valorPrevisto().signum() < 0) {
            throw erro(HttpStatus.BAD_REQUEST, "O valor previsto não pode ser negativo.");
        }

        String ilha = textoOpcional(request.ilha());
        String concelho = textoOpcional(request.concelho());
        String zona = textoOpcional(request.zona());
        validarGeografia(ilha, concelho, zona);
        String telefone = textoOpcional(request.telefone());
        String email = textoOpcional(request.email());
        validarTamanho(telefone, 50, "O telefone não pode exceder 50 caracteres.");
        validarTamanho(email, 150, "O email não pode exceder 150 caracteres.");
        if (email != null && !EMAIL.matcher(email).matches()) {
            throw erro(HttpStatus.BAD_REQUEST, "Informe um endereço de email válido.");
        }

        return new ServicoContratanteRequest(
                tipoServico,
                titulo,
                textoOpcional(request.descricao()),
                request.dataPretendida(),
                request.valorPrevisto(),
                textoOpcional(request.competenciasExigidas()),
                request.inicioCandidatura(),
                request.fimCandidatura(),
                ilha,
                concelho,
                zona,
                telefone,
                email,
                normalizarPaths(request.anexosMantidos()),
                validarUtilizador(request.utilizador())
        );
    }

    private void validarGeografia(String ilha, String concelho, String zona) {
        if (concelho != null && ilha == null) {
            throw erro(HttpStatus.BAD_REQUEST, "Selecione a ilha antes do concelho.");
        }
        if (zona != null && concelho == null) {
            throw erro(HttpStatus.BAD_REQUEST, "Selecione o concelho antes da zona.");
        }
        if (ilha != null && !servicoRepository.existeIlha(ilha)) {
            throw erro(HttpStatus.BAD_REQUEST, "A ilha selecionada não é válida.");
        }
        if (concelho != null && !servicoRepository.existeConcelho(concelho, ilha)) {
            throw erro(HttpStatus.BAD_REQUEST, "O concelho selecionado não pertence à ilha indicada.");
        }
        if (zona != null && !servicoRepository.existeZona(zona, concelho)) {
            throw erro(HttpStatus.BAD_REQUEST, "A zona selecionada não pertence ao concelho indicado.");
        }
    }

    private List<String> normalizarPaths(List<String> paths) {
        if (paths == null) {
            return null;
        }
        return paths.stream()
                .map(this::textoOpcional)
                .filter(path -> path != null)
                .distinct()
                .toList();
    }

    private List<AnexoArmazenado> resolverAnexosMantidos(
            List<AnexoArmazenado> atuais,
            List<String> pathsMantidos
    ) {
        List<AnexoArmazenado> anexosAtuais = atuais == null ? List.of() : atuais;
        if (pathsMantidos == null) {
            return anexosAtuais;
        }
        Map<String, AnexoArmazenado> porPath = new LinkedHashMap<>();
        anexosAtuais.forEach(anexo -> porPath.put(anexo.path(), anexo));
        List<AnexoArmazenado> mantidos = new ArrayList<>();
        for (String path : pathsMantidos) {
            AnexoArmazenado anexo = porPath.get(path);
            if (anexo == null) {
                throw erro(HttpStatus.BAD_REQUEST, "Um dos anexos indicados não pertence ao serviço.");
            }
            mantidos.add(anexo);
        }
        return List.copyOf(mantidos);
    }

    private List<AnexoArmazenado> guardarDocumentos(
            Integer servicoId,
            List<MultipartFile> ficheiros,
            int primeiroIndice
    ) {
        if (ficheiros.isEmpty()) {
            return List.of();
        }
        String idTipoDocumento = resolverIdTipoDocumento();
        List<AnexoArmazenado> resultado = new ArrayList<>();
        for (int i = 0; i < ficheiros.size(); i++) {
            resultado.add(guardarDocumento(
                    servicoId,
                    ficheiros.get(i),
                    idTipoDocumento,
                    primeiroIndice + i
            ));
        }
        return List.copyOf(resultado);
    }

    private AnexoArmazenado guardarDocumento(
            Integer servicoId,
            MultipartFile ficheiro,
            String idTipoDocumento,
            int indice
    ) {
        String nomeOriginal = StringUtils.cleanPath(
                Optional.ofNullable(ficheiro.getOriginalFilename()).orElse("documento-" + indice)
        );
        String extensao = extensao(nomeOriginal);
        String nomeBase = sanitizarSegmentoPath(removerExtensao(nomeOriginal));
        String nomeArmazenamento = "DOCUMENTO-" + indice + "-"
                + LocalDateTime.now().format(SUFIXO_DOCUMENTO) + "-" + nomeBase;
        String pathDocumento = appCodeDocumento
                + "/"
                + LocalDateTime.now().getYear()
                + "/modulos/"
                + sanitizarSegmentoPath(tipoRelacaoDocumento)
                + "/"
                + servicoId
                + "/"
                + nomeArmazenamento
                + extensao;
        try {
            String path = documentService.save(DocRelacaoDTO.builder()
                    .idRelacao(servicoId)
                    .tipoRelacao(tipoRelacaoDocumento)
                    .estado(estadoDocumento)
                    .name(nomeOriginal)
                    .idTpDoc(idTipoDocumento)
                    .fileName(nomeArmazenamento)
                    .path(pathDocumento)
                    .appCode(appCodeDocumento)
                    .file(ficheiro)
                    .build());
            if (!temTexto(path)) {
                throw new IllegalStateException("O serviço documental devolveu um path vazio.");
            }
            return new AnexoArmazenado(nomeOriginal, path);
        } catch (RuntimeException ex) {
            registarErroUpload(servicoId, idTipoDocumento, nomeOriginal, ex);
            throw new ResponseStatusException(
                    HttpStatus.BAD_GATEWAY,
                    "Não foi possível guardar os anexos do serviço. Tente novamente mais tarde.",
                    ex
            );
        }
    }

    private String resolverIdTipoDocumento() {
        if (temTexto(tipoDocumentoIdConfigurado)) {
            return validarIdTipoDocumento(tipoDocumentoIdConfigurado);
        }
        return comboboxService.listarDocumentosAtivos().stream()
                .filter(this::correspondeDocumentoServico)
                .map(item -> item.get("id"))
                .filter(this::idTipoDocumentoValido)
                .map(String::valueOf)
                .findFirst()
                .orElseThrow(() -> erro(
                        HttpStatus.INTERNAL_SERVER_ERROR,
                        "Não foi encontrado um tipo documental ativo para a prestação de serviços. "
                                + "Configure o respetivo ID antes de anexar documentos."
                ));
    }

    private boolean correspondeDocumentoServico(Map<String, Object> item) {
        Object descricao = item == null ? null : item.get("tipo_documento_desc");
        String normalizado = normalizarParaPesquisa(descricao == null ? null : descricao.toString());
        return temTexto(normalizado)
                && (normalizado.contains("PRESTACAO")
                || normalizado.contains("SERVICO")
                || normalizado.contains("OUTRO DOCUMENT"));
    }

    private String validarIdTipoDocumento(String valor) {
        String limpo = valor.trim();
        if (idTipoDocumentoValido(limpo)) {
            return limpo;
        }
        throw erro(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "O ID documental configurado para a prestação de serviços não é válido."
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

    private void registarErroUpload(
            Integer servicoId,
            String idTipoDocumento,
            String nomeOriginal,
            RuntimeException ex
    ) {
        if (ex instanceof HttpStatusCodeException httpException) {
            log.error(
                    "Falha no serviço documental da intermediação: servicoId={}, idTpDoc={}, "
                            + "ficheiro={}, status={}, resposta={}",
                    servicoId,
                    idTipoDocumento,
                    nomeOriginal,
                    httpException.getStatusCode(),
                    httpException.getResponseBodyAsString(),
                    ex
            );
            return;
        }
        log.error(
                "Falha no serviço documental da intermediação: servicoId={}, idTpDoc={}, ficheiro={}",
                servicoId,
                idTipoDocumento,
                nomeOriginal,
                ex
        );
    }

    private ServicoContratanteListaResponse mapearLista(ServicoRegisto servico) {
        String estado = normalizarEstadoServico(servico.estado());
        return new ServicoContratanteListaResponse(
                servico.servicoId(),
                servico.tipoServico(),
                servico.titulo(),
                estado,
                EmpregoDominio.descricao(EmpregoDominio.DOMINIO_ESTADO_SERVICO, estado),
                servico.inicioCandidatura(),
                servico.fimCandidatura(),
                servico.dateCreate()
        );
    }

    private ServicoContratanteDetalheResponse mapearDetalhe(ServicoRegisto servico) {
        String estado = normalizarEstadoServico(servico.estado());
        return new ServicoContratanteDetalheResponse(
                servico.servicoId(),
                servico.contratanteId(),
                servico.nomeContratante(),
                servico.tipoServico(),
                servico.titulo(),
                servico.descricao(),
                servico.dataPretendida(),
                servico.valorPrevisto(),
                servico.competenciasExigidas(),
                servico.inicioCandidatura(),
                servico.fimCandidatura(),
                servico.ilha(),
                descricaoGeografia(servico.ilha()),
                servico.concelho(),
                descricaoGeografia(servico.concelho()),
                servico.zona(),
                descricaoGeografia(servico.zona()),
                servico.telefone(),
                servico.email(),
                mapearAnexos(servico.anexos()),
                estado,
                EmpregoDominio.descricao(EmpregoDominio.DOMINIO_ESTADO_SERVICO, estado),
                servico.dateCreate(),
                servico.userCreate(),
                servico.dateUpdate(),
                servico.userUpdate()
        );
    }

    private List<ServicoContratanteAnexoResponse> mapearAnexos(List<AnexoArmazenado> anexos) {
        if (anexos == null) {
            return List.of();
        }
        return anexos.stream()
                .map(anexo -> new ServicoContratanteAnexoResponse(
                        anexo.nome(),
                        anexo.path(),
                        documentService.gerarLinkPublico(anexo.path())
                ))
                .toList();
    }

    private ServicoContratanteCandidatoListaResponse mapearCandidato(CandidatoRegisto candidato) {
        String estado = normalizarEstadoCandidatura(candidato.estado());
        String selecaoIefp = normalizarSimNao(candidato.selecaoIefp());
        return new ServicoContratanteCandidatoListaResponse(
                candidato.candidaturaId(),
                candidato.pessoaId(),
                candidato.nome(),
                candidato.tipoServico(),
                candidato.titulo(),
                estado,
                EmpregoDominio.descricao(EmpregoDominio.DOMINIO_CANDIDATURA_STATUS, estado),
                selecaoIefp,
                EmpregoDominio.descricao(EmpregoDominio.DOMINIO_SIM_NAO, selecaoIefp),
                candidato.dataCandidatura()
        );
    }

    private ServicoRegisto buscarRegisto(Integer servicoId, Long pessoaId) {
        return servicoRepository.buscarPorId(servicoId, pessoaId)
                .orElseThrow(this::servicoNaoEncontrado);
    }

    private CandidatoRegisto buscarCandidato(Integer servicoId, Integer candidaturaId, Long pessoaId) {
        return servicoRepository.buscarCandidato(servicoId, candidaturaId, pessoaId)
                .orElseThrow(this::candidaturaNaoEncontrada);
    }

    private ResponseStatusException servicoNaoEncontrado() {
        return erro(
                HttpStatus.NOT_FOUND,
                "O serviço não existe ou não pertence ao contratante. Atualize a lista e tente novamente."
        );
    }

    private ResponseStatusException candidaturaNaoEncontrada() {
        return erro(
                HttpStatus.NOT_FOUND,
                "A candidatura não existe ou não pertence ao serviço selecionado."
        );
    }

    private void garantirEditavel(ServicoRegisto servico) {
        String estado = normalizarEstadoServico(servico.estado());
        if (ESTADO_CANCELADO.equals(estado) || ESTADO_ELIMINADO.equals(estado)) {
            throw erro(HttpStatus.CONFLICT, "Um serviço cancelado ou removido não pode ser editado.");
        }
    }

    private String normalizarEstadoServico(String estado) {
        if (!temTexto(estado)) {
            return estado;
        }
        return EmpregoDominio.valorOficial(EmpregoDominio.DOMINIO_ESTADO_SERVICO, estado)
                .orElseGet(() -> EmpregoDominio.normalizar(estado));
    }

    private String normalizarEstadoCandidatura(String estado) {
        if (!temTexto(estado)) {
            return estado;
        }
        return EmpregoDominio.valorOficial(EmpregoDominio.DOMINIO_CANDIDATURA_STATUS, estado)
                .orElseGet(() -> EmpregoDominio.normalizar(estado));
    }

    private String normalizarSimNao(String valor) {
        String normalizado = EmpregoDominio.normalizar(valor);
        if (normalizado == null) {
            return null;
        }
        if (Set.of("S", "SIM", "TRUE", "1").contains(normalizado)) {
            return "S";
        }
        if (Set.of("N", "NAO", "FALSE", "0").contains(normalizado)) {
            return "N";
        }
        return normalizado;
    }

    private List<MinhaCandidaturaOpcaoResponse> listarDominio(String dominio) {
        return EmpregoDominio.listarPorDominio(dominio).stream()
                .map(item -> new MinhaCandidaturaOpcaoResponse(item.getValor(), item.getDescricao()))
                .toList();
    }

    private String normalizarDominioOpcional(String dominio, String valor, String mensagem) {
        if (!temTexto(valor)) {
            return null;
        }
        return EmpregoDominio.valorOficial(dominio, valor)
                .orElseThrow(() -> erro(HttpStatus.BAD_REQUEST, mensagem));
    }

    private void validarPeriodo(java.time.LocalDate inicio, java.time.LocalDate fim) {
        if (inicio != null && fim != null && fim.isBefore(inicio)) {
            throw erro(HttpStatus.BAD_REQUEST, "A data de fim não pode ser anterior à data de início.");
        }
    }

    private void validarPessoa(Long pessoaId) {
        if (pessoaId == null || pessoaId <= 0) {
            throw erro(
                    HttpStatus.BAD_REQUEST,
                    "Não foi possível identificar o contratante. Entre novamente e tente de novo."
            );
        }
    }

    private void validarServico(Integer servicoId) {
        if (servicoId == null || servicoId <= 0) {
            throw erro(HttpStatus.BAD_REQUEST, "Não foi possível identificar o serviço selecionado.");
        }
    }

    private void validarCandidatura(Integer candidaturaId) {
        if (candidaturaId == null || candidaturaId <= 0) {
            throw erro(HttpStatus.BAD_REQUEST, "Não foi possível identificar a candidatura selecionada.");
        }
    }

    private String validarUtilizador(String utilizador) {
        String valor = textoObrigatorio(
                utilizador,
                "Não foi possível identificar o utilizador. Entre novamente e tente de novo."
        );
        validarTamanho(valor, 25, "A identificação do utilizador não pode exceder 25 caracteres.");
        return valor;
    }

    private void validarTamanho(String valor, int limite, String mensagem) {
        if (valor != null && valor.length() > limite) {
            throw erro(HttpStatus.BAD_REQUEST, mensagem);
        }
    }

    private List<MultipartFile> ficheirosValidos(List<MultipartFile> ficheiros) {
        if (ficheiros == null) {
            return List.of();
        }
        return ficheiros.stream().filter(ficheiro -> ficheiro != null && !ficheiro.isEmpty()).toList();
    }

    private String descricaoGeografia(String codigo) {
        if (!temTexto(codigo)) {
            return codigo;
        }
        return globalGeografiaService.buscarNomePorCodigo(codigo.trim()).orElse(codigo.trim());
    }

    private String textoObrigatorio(String valor, String mensagem) {
        String texto = textoOpcional(valor);
        if (texto == null) {
            throw erro(HttpStatus.BAD_REQUEST, mensagem);
        }
        return texto;
    }

    private String textoOpcional(String valor) {
        return temTexto(valor) ? valor.trim() : null;
    }

    private boolean temTexto(String valor) {
        return valor != null && !valor.trim().isEmpty();
    }

    private String sanitizarSegmentoPath(String valor) {
        String limpo = textoOpcional(valor);
        return limpo == null
                ? "documento"
                : limpo.replaceAll("[\\\\/:*?\"<>|]+", "_").replace(' ', '_');
    }

    private String removerExtensao(String nome) {
        int indice = nome.lastIndexOf('.');
        return indice <= 0 ? nome : nome.substring(0, indice);
    }

    private String extensao(String nome) {
        int indice = nome == null ? -1 : nome.lastIndexOf('.');
        return indice < 0 ? "" : nome.substring(indice);
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

    private ResponseStatusException erro(HttpStatus status, String mensagem) {
        return new ResponseStatusException(status, mensagem);
    }
}
