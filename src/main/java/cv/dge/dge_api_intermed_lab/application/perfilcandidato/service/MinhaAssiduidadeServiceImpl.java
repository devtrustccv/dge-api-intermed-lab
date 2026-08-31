package cv.dge.dge_api_intermed_lab.application.perfilcandidato.service;

import cv.dge.dge_api_intermed_lab.application.document.dto.DocRelacaoDTO;
import cv.dge.dge_api_intermed_lab.application.document.service.ComboboxService;
import cv.dge.dge_api_intermed_lab.application.document.service.DocumentService;
import cv.dge.dge_api_intermed_lab.application.perfilcandidato.dto.MinhaAssiduidadeDetalheResponse;
import cv.dge.dge_api_intermed_lab.application.perfilcandidato.dto.MinhaAssiduidadeFiltro;
import cv.dge.dge_api_intermed_lab.application.perfilcandidato.dto.MinhaAssiduidadeListaResponse;
import cv.dge.dge_api_intermed_lab.application.perfilcandidato.dto.MinhaAssiduidadeOpcoesResponse;
import cv.dge.dge_api_intermed_lab.application.perfilcandidato.dto.MinhaAssiduidadeRequest;
import cv.dge.dge_api_intermed_lab.application.perfilcandidato.dto.MinhaCandidaturaOpcaoResponse;
import cv.dge.dge_api_intermed_lab.application.perfilentidade.enums.EmpregoDominio;
import cv.dge.dge_api_intermed_lab.infrastructure.perfilcandidato.repository.MinhaAssiduidadeRepository;
import cv.dge.dge_api_intermed_lab.infrastructure.perfilcandidato.repository.MinhaAssiduidadeRepository.AssiduidadeRegisto;
import cv.dge.dge_api_intermed_lab.infrastructure.perfilcandidato.repository.MinhaAssiduidadeRepository.ColocacaoAtiva;
import java.text.Normalizer;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
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
public class MinhaAssiduidadeServiceImpl implements MinhaAssiduidadeService {

    private static final String ESTADO_PENDENTE = "PENDENTE";
    private static final int TAMANHO_MAXIMO_COMPROVATIVO = 150;
    private static final int TAMANHO_MAXIMO_JUSTIFICACAO = 500;
    private static final int TAMANHO_MAXIMO_UTILIZADOR = 25;
    private static final DateTimeFormatter FORMATO_HORA = DateTimeFormatter.ofPattern("HH:mm");

    private final MinhaAssiduidadeRepository assiduidadeRepository;
    private final DocumentService documentService;
    private final ComboboxService comboboxService;

    @Value("${document.assiduidade.app-code:interm_laboral}")
    private String appCodeDocumento;

    @Value("${document.assiduidade.estado:A}")
    private String estadoDocumento;

    @Value("${document.assiduidade.tipo-relacao:EMPREGO_T_ASSIDUIDADE}")
    private String tipoRelacaoDocumento;

    @Value("${document.assiduidade.tipo-comprovativo-id:}")
    private String tipoComprovativoIdConfigurado;

    @Override
    @Transactional(readOnly = true)
    public List<MinhaAssiduidadeListaResponse> listar(MinhaAssiduidadeFiltro filtro) {
        MinhaAssiduidadeFiltro dados = normalizarFiltro(filtro);
        return assiduidadeRepository.listar(dados).stream()
                .map(this::mapearLista)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public MinhaAssiduidadeOpcoesResponse listarOpcoes() {
        return new MinhaAssiduidadeOpcoesResponse(
                listarDominio(EmpregoDominio.DOMINIO_TIPO_ASSIDUIDADE),
                listarDominio(EmpregoDominio.DOMINIO_ESTADO_ASSIDUIDADE)
        );
    }

    @Override
    @Transactional(readOnly = true)
    public MinhaAssiduidadeDetalheResponse buscarPorId(Integer assiduidadeId, Long pessoaId) {
        validarAssiduidade(assiduidadeId);
        validarPessoa(pessoaId);
        return mapearDetalhe(buscarRegisto(assiduidadeId, pessoaId));
    }

    @Override
    @Transactional
    public MinhaAssiduidadeDetalheResponse criar(
            Long pessoaId,
            MinhaAssiduidadeRequest request,
            MultipartFile comprovativo
    ) {
        validarPessoa(pessoaId);
        MinhaAssiduidadeRequest dados = validarRequest(request);
        ColocacaoAtiva colocacao = assiduidadeRepository.buscarColocacaoAtiva(pessoaId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.CONFLICT,
                        "Não existe uma colocação ativa para registar a assiduidade."
                ));

        Integer assiduidadeId = assiduidadeRepository.inserir(colocacao, dados, ESTADO_PENDENTE);
        if (assiduidadeId == null) {
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Não foi possível registar a assiduidade neste momento. Tente novamente mais tarde."
            );
        }
        guardarComprovativoSeInformado(assiduidadeId, pessoaId, comprovativo);
        return mapearDetalhe(buscarRegisto(assiduidadeId, pessoaId));
    }

    @Override
    @Transactional
    public MinhaAssiduidadeDetalheResponse atualizar(
            Integer assiduidadeId,
            Long pessoaId,
            MinhaAssiduidadeRequest request,
            MultipartFile novoComprovativo
    ) {
        validarAssiduidade(assiduidadeId);
        validarPessoa(pessoaId);
        MinhaAssiduidadeRequest dados = validarRequest(request);
        buscarRegisto(assiduidadeId, pessoaId);

        if (!assiduidadeRepository.atualizar(assiduidadeId, pessoaId, dados)) {
            throw assiduidadeNaoEncontrada();
        }
        guardarComprovativoSeInformado(assiduidadeId, pessoaId, novoComprovativo);
        return mapearDetalhe(buscarRegisto(assiduidadeId, pessoaId));
    }

    private MinhaAssiduidadeFiltro normalizarFiltro(MinhaAssiduidadeFiltro filtro) {
        if (filtro == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Não foi possível carregar a assiduidade. Atualize a página e tente novamente."
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
        return new MinhaAssiduidadeFiltro(
                filtro.pessoaId(),
                normalizarDominioOpcional(
                        EmpregoDominio.DOMINIO_TIPO_ASSIDUIDADE,
                        filtro.tipoAssiduidade(),
                        "Selecione um tipo de assiduidade válido."
                ),
                normalizarDominioOpcional(
                        EmpregoDominio.DOMINIO_ESTADO_ASSIDUIDADE,
                        filtro.estado(),
                        "Selecione um estado de assiduidade válido."
                ),
                filtro.dataInicio(),
                filtro.dataFim()
        );
    }

    private MinhaAssiduidadeRequest validarRequest(MinhaAssiduidadeRequest request) {
        if (request == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Preencha os dados da assiduidade antes de guardar."
            );
        }
        String tipo = EmpregoDominio.valorOficial(
                        EmpregoDominio.DOMINIO_TIPO_ASSIDUIDADE,
                        request.tipoAssiduidade()
                )
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Selecione um tipo de assiduidade válido."
                ));
        if (request.data() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Informe a data da assiduidade.");
        }
        validarHorario(request.horaEntrada(), request.horaSaida());
        String justificacao = textoOpcional(request.justificacao());
        validarTamanho(
                justificacao,
                TAMANHO_MAXIMO_JUSTIFICACAO,
                "A justificação não pode exceder 500 caracteres."
        );
        String utilizador = textoObrigatorio(
                request.utilizador(),
                "Não foi possível identificar o utilizador. Entre novamente e tente de novo."
        );
        validarTamanho(
                utilizador,
                TAMANHO_MAXIMO_UTILIZADOR,
                "A identificação do utilizador não pode exceder 25 caracteres."
        );
        return new MinhaAssiduidadeRequest(
                tipo,
                request.data(),
                request.horaEntrada(),
                request.horaSaida(),
                justificacao,
                utilizador
        );
    }

    private void validarHorario(LocalTime horaEntrada, LocalTime horaSaida) {
        if (horaEntrada != null && horaSaida != null && horaSaida.isBefore(horaEntrada)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "A hora de saída não pode ser anterior à hora de entrada."
            );
        }
    }

    private AssiduidadeRegisto buscarRegisto(Integer assiduidadeId, Long pessoaId) {
        return assiduidadeRepository.buscarPorId(assiduidadeId, pessoaId)
                .orElseThrow(this::assiduidadeNaoEncontrada);
    }

    private ResponseStatusException assiduidadeNaoEncontrada() {
        return new ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "O registo de assiduidade não existe ou não pertence ao candidato. Atualize a lista e tente novamente."
        );
    }

    private MinhaAssiduidadeListaResponse mapearLista(AssiduidadeRegisto registo) {
        String tipo = normalizarValor(EmpregoDominio.DOMINIO_TIPO_ASSIDUIDADE, registo.tipoAssiduidade());
        String estado = normalizarValor(EmpregoDominio.DOMINIO_ESTADO_ASSIDUIDADE, registo.estado());
        return new MinhaAssiduidadeListaResponse(
                registo.assiduidadeId(),
                tipo,
                EmpregoDominio.descricao(EmpregoDominio.DOMINIO_TIPO_ASSIDUIDADE, tipo),
                registo.data(),
                registo.horaEntrada(),
                registo.horaSaida(),
                formatarHorario(registo.horaEntrada(), registo.horaSaida()),
                estado,
                EmpregoDominio.descricao(EmpregoDominio.DOMINIO_ESTADO_ASSIDUIDADE, estado)
        );
    }

    private MinhaAssiduidadeDetalheResponse mapearDetalhe(AssiduidadeRegisto registo) {
        String tipo = normalizarValor(EmpregoDominio.DOMINIO_TIPO_ASSIDUIDADE, registo.tipoAssiduidade());
        String estado = normalizarValor(EmpregoDominio.DOMINIO_ESTADO_ASSIDUIDADE, registo.estado());
        String comprovativo = textoOpcional(registo.comprovativo());
        return new MinhaAssiduidadeDetalheResponse(
                registo.assiduidadeId(),
                tipo,
                EmpregoDominio.descricao(EmpregoDominio.DOMINIO_TIPO_ASSIDUIDADE, tipo),
                registo.data(),
                registo.horaEntrada(),
                registo.horaSaida(),
                estado,
                EmpregoDominio.descricao(EmpregoDominio.DOMINIO_ESTADO_ASSIDUIDADE, estado),
                registo.justificacao(),
                comprovativo,
                comprovativo == null ? null : documentService.gerarLinkPublico(comprovativo),
                registo.observacao()
        );
    }

    private List<MinhaCandidaturaOpcaoResponse> listarDominio(String dominio) {
        return EmpregoDominio.listarPorDominio(dominio).stream()
                .map(item -> new MinhaCandidaturaOpcaoResponse(item.getValor(), item.getDescricao()))
                .toList();
    }

    private void guardarComprovativoSeInformado(
            Integer assiduidadeId,
            Long pessoaId,
            MultipartFile comprovativo
    ) {
        if (!temFicheiro(comprovativo)) {
            return;
        }
        String path = guardarComprovativo(assiduidadeId, comprovativo);
        if (!assiduidadeRepository.atualizarComprovativo(assiduidadeId, pessoaId, path)) {
            throw assiduidadeNaoEncontrada();
        }
    }

    private String guardarComprovativo(Integer assiduidadeId, MultipartFile ficheiro) {
        String idTipoDocumento = resolverIdTipoDocumento();
        String nomeOriginal = StringUtils.cleanPath(
                Optional.ofNullable(ficheiro.getOriginalFilename()).orElse("comprovativo")
        );
        String extensao = extensao(nomeOriginal);
        String nomeBase = sanitizarSegmentoPath(removerExtensao(nomeOriginal));
        String prefixoPath = appCodeDocumento
                + "/"
                + LocalDateTime.now().getYear()
                + "/modulos/"
                + sanitizarSegmentoPath(tipoRelacaoDocumento)
                + "/"
                + assiduidadeId
                + "/COMPROVATIVO-";
        int tamanhoDisponivel = Math.max(1, TAMANHO_MAXIMO_COMPROVATIVO - prefixoPath.length() - extensao.length());
        String nomeLimitado = limitar(nomeBase, tamanhoDisponivel);
        String nomeArmazenamento = "COMPROVATIVO-" + nomeLimitado;
        String pathDocumento = prefixoPath + nomeLimitado + extensao;

        try {
            String pathGuardado = documentService.save(DocRelacaoDTO.builder()
                    .idRelacao(assiduidadeId)
                    .tipoRelacao(tipoRelacaoDocumento)
                    .estado(estadoDocumento)
                    .name(nomeOriginal)
                    .idTpDoc(idTipoDocumento)
                    .fileName(nomeArmazenamento)
                    .path(pathDocumento)
                    .appCode(appCodeDocumento)
                    .file(ficheiro)
                    .build());
            if (!temTexto(pathGuardado) || pathGuardado.length() > TAMANHO_MAXIMO_COMPROVATIVO) {
                throw new IllegalStateException("O serviço documental devolveu um path inválido.");
            }
            return pathGuardado;
        } catch (RuntimeException ex) {
            registarErroUpload(assiduidadeId, idTipoDocumento, nomeOriginal, ex);
            throw new ResponseStatusException(
                    HttpStatus.BAD_GATEWAY,
                    "Não foi possível guardar o comprovativo da assiduidade. Tente novamente mais tarde.",
                    ex
            );
        }
    }

    private String resolverIdTipoDocumento() {
        if (temTexto(tipoComprovativoIdConfigurado)) {
            return validarIdTipoDocumento(tipoComprovativoIdConfigurado);
        }
        return comboboxService.listarDocumentosAtivos().stream()
                .filter(this::correspondeComprovativo)
                .map(item -> item.get("id"))
                .filter(this::idTipoDocumentoValido)
                .map(String::valueOf)
                .findFirst()
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.INTERNAL_SERVER_ERROR,
                        "Não foi encontrado no catálogo um tipo documental ativo para o comprovativo de assiduidade. "
                                + "Configure o respetivo ID antes de anexar o ficheiro."
                ));
    }

    private boolean correspondeComprovativo(Map<String, Object> item) {
        Object descricao = item == null ? null : item.get("tipo_documento_desc");
        String normalizado = normalizarParaPesquisa(descricao == null ? null : descricao.toString());
        return temTexto(normalizado)
                && (normalizado.contains("COMPROVAT")
                || normalizado.contains("JUSTIFICAT")
                || normalizado.contains("ASSIDUID"));
    }

    private String validarIdTipoDocumento(String valor) {
        String limpo = valor.trim();
        if (idTipoDocumentoValido(limpo)) {
            return limpo;
        }
        throw new ResponseStatusException(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "O ID documental configurado para o comprovativo de assiduidade não é válido."
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
            Integer assiduidadeId,
            String idTipoDocumento,
            String nomeOriginal,
            RuntimeException ex
    ) {
        if (ex instanceof HttpStatusCodeException httpException) {
            log.error(
                    "Falha no serviço documental da assiduidade: assiduidadeId={}, idTpDoc={}, "
                            + "ficheiro={}, status={}, resposta={}",
                    assiduidadeId,
                    idTipoDocumento,
                    nomeOriginal,
                    httpException.getStatusCode(),
                    httpException.getResponseBodyAsString(),
                    ex
            );
            return;
        }
        log.error(
                "Falha no serviço documental da assiduidade: assiduidadeId={}, idTpDoc={}, ficheiro={}",
                assiduidadeId,
                idTipoDocumento,
                nomeOriginal,
                ex
        );
    }

    private String formatarHorario(LocalTime horaEntrada, LocalTime horaSaida) {
        if (horaEntrada == null && horaSaida == null) {
            return null;
        }
        if (horaEntrada == null) {
            return horaSaida.format(FORMATO_HORA);
        }
        if (horaSaida == null) {
            return horaEntrada.format(FORMATO_HORA);
        }
        return horaEntrada.format(FORMATO_HORA) + " - " + horaSaida.format(FORMATO_HORA);
    }

    private String normalizarDominioOpcional(String dominio, String valor, String mensagem) {
        if (!temTexto(valor)) {
            return null;
        }
        return EmpregoDominio.valorOficial(dominio, valor)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, mensagem));
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

    private void validarAssiduidade(Integer assiduidadeId) {
        if (assiduidadeId == null || assiduidadeId <= 0) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Não foi possível identificar o registo de assiduidade. Atualize a lista e tente novamente."
            );
        }
    }

    private void validarTamanho(String valor, int tamanhoMaximo, String mensagem) {
        if (valor != null && valor.length() > tamanhoMaximo) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, mensagem);
        }
    }

    private String textoObrigatorio(String valor, String mensagem) {
        String texto = textoOpcional(valor);
        if (texto == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, mensagem);
        }
        return texto;
    }

    private String textoOpcional(String valor) {
        return temTexto(valor) ? valor.trim() : null;
    }

    private boolean temFicheiro(MultipartFile ficheiro) {
        return ficheiro != null && !ficheiro.isEmpty();
    }

    private boolean temTexto(String valor) {
        return valor != null && !valor.trim().isEmpty();
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

    private String removerExtensao(String nome) {
        int indice = nome.lastIndexOf('.');
        return indice <= 0 ? nome : nome.substring(0, indice);
    }

    private String extensao(String nomeFicheiro) {
        if (!temTexto(nomeFicheiro)) {
            return "";
        }
        int indice = nomeFicheiro.lastIndexOf('.');
        return indice < 0 ? "" : nomeFicheiro.substring(indice);
    }

    private String limitar(String valor, int tamanhoMaximo) {
        return valor.length() <= tamanhoMaximo ? valor : valor.substring(0, tamanhoMaximo);
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
}
