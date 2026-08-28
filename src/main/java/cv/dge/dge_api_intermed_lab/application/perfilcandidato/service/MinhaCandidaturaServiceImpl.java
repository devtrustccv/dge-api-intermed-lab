package cv.dge.dge_api_intermed_lab.application.perfilcandidato.service;

import com.fasterxml.jackson.databind.JsonNode;
import cv.dge.dge_api_intermed_lab.application.document.service.DocumentService;
import cv.dge.dge_api_intermed_lab.application.geografia.service.GlobalGeografiaService;
import cv.dge.dge_api_intermed_lab.application.perfilcandidato.dto.CandidaturaDocumentoResponse;
import cv.dge.dge_api_intermed_lab.application.perfilcandidato.dto.ConsultaVagaOpcaoResponse;
import cv.dge.dge_api_intermed_lab.application.perfilcandidato.dto.MinhaCandidaturaDetalheResponse;
import cv.dge.dge_api_intermed_lab.application.perfilcandidato.dto.MinhaCandidaturaFiltro;
import cv.dge.dge_api_intermed_lab.application.perfilcandidato.dto.MinhaCandidaturaListaResponse;
import cv.dge.dge_api_intermed_lab.application.perfilcandidato.dto.MinhaCandidaturaOpcaoResponse;
import cv.dge.dge_api_intermed_lab.application.perfilcandidato.dto.MinhaCandidaturaOpcoesResponse;
import cv.dge.dge_api_intermed_lab.application.perfilentidade.enums.EmpregoDominio;
import cv.dge.dge_api_intermed_lab.infrastructure.perfilcandidato.repository.MinhaCandidaturaRepository;
import cv.dge.dge_api_intermed_lab.infrastructure.perfilcandidato.repository.MinhaCandidaturaRepository.CandidaturaRegisto;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class MinhaCandidaturaServiceImpl implements MinhaCandidaturaService {

    /* A base atual persiste o canal da candidatura com os valores do domínio CANAL_OFERTA. */
    private static final String DOMINIO_CANAL_CANDIDATURA = EmpregoDominio.DOMINIO_CANAL_OFERTA;
    private static final String TIPO_DOCUMENTO_CURRICULO = "CURRICULO_VITAE";
    private static final String TIPO_DOCUMENTO_OUTRO = "OUTRO_DOCUMENTO";

    private final MinhaCandidaturaRepository candidaturaRepository;
    private final GlobalGeografiaService globalGeografiaService;
    private final DocumentService documentService;

    @Override
    @Transactional(readOnly = true)
    public List<MinhaCandidaturaListaResponse> listar(MinhaCandidaturaFiltro filtro) {
        MinhaCandidaturaFiltro dados = normalizarFiltro(filtro);
        Map<String, String> geografias = new LinkedHashMap<>();
        return candidaturaRepository.listar(dados).stream()
                .map(candidatura -> mapearLista(candidatura, geografias))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public MinhaCandidaturaDetalheResponse buscarPorId(Integer candidaturaId, Long pessoaId) {
        validarPessoa(pessoaId);
        validarCandidatura(candidaturaId);

        CandidaturaRegisto candidatura = candidaturaRepository.buscarPorId(candidaturaId, pessoaId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "A candidatura selecionada não existe ou não pertence ao candidato. Atualize a lista e tente novamente."
                ));
        return mapearDetalhe(candidatura, new LinkedHashMap<>());
    }

    @Override
    @Transactional(readOnly = true)
    public MinhaCandidaturaOpcoesResponse listarOpcoes(Long pessoaId, String ilha) {
        validarPessoa(pessoaId);
        String ilhaLimpa = textoOpcional(ilha);
        Map<String, String> geografias = new LinkedHashMap<>();

        return new MinhaCandidaturaOpcoesResponse(
                listarDominio(EmpregoDominio.DOMINIO_TIPO_OFERTA),
                candidaturaRepository.listarEntidades(pessoaId),
                enriquecerGeografias(candidaturaRepository.listarIlhas(pessoaId), geografias),
                enriquecerGeografias(
                        candidaturaRepository.listarConcelhos(pessoaId, ilhaLimpa),
                        geografias
                ),
                listarDominio(EmpregoDominio.DOMINIO_STATUS_CANDIDATURA)
        );
    }

    private MinhaCandidaturaFiltro normalizarFiltro(MinhaCandidaturaFiltro filtro) {
        if (filtro == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Não foi possível carregar as candidaturas. Atualize a página e tente novamente."
            );
        }
        validarPessoa(filtro.pessoaId());
        if (filtro.entidadeId() != null && filtro.entidadeId() <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Selecione uma entidade válida.");
        }
        if (filtro.dataInicio() != null && filtro.dataFim() != null
                && filtro.dataFim().isBefore(filtro.dataInicio())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "A data de fim não pode ser anterior à data de início."
            );
        }

        return new MinhaCandidaturaFiltro(
                filtro.pessoaId(),
                normalizarDominioOpcional(
                        EmpregoDominio.DOMINIO_TIPO_OFERTA,
                        filtro.tipoOferta(),
                        "Selecione um tipo de oferta válido."
                ),
                filtro.entidadeId(),
                textoOpcional(filtro.ilha()),
                textoOpcional(filtro.concelho()),
                normalizarDominioOpcional(
                        EmpregoDominio.DOMINIO_STATUS_CANDIDATURA,
                        filtro.estado(),
                        "Selecione um estado de candidatura válido."
                ),
                textoOpcional(filtro.codigoReferencia()),
                filtro.dataInicio(),
                filtro.dataFim()
        );
    }

    private MinhaCandidaturaListaResponse mapearLista(
            CandidaturaRegisto candidatura,
            Map<String, String> geografias
    ) {
        String tipoOferta = normalizarValor(EmpregoDominio.DOMINIO_TIPO_OFERTA, candidatura.tipoOferta());
        String estado = normalizarValor(EmpregoDominio.DOMINIO_STATUS_CANDIDATURA, candidatura.estado());
        return new MinhaCandidaturaListaResponse(
                candidatura.candidaturaId(),
                tipoOferta,
                EmpregoDominio.descricao(EmpregoDominio.DOMINIO_TIPO_OFERTA, tipoOferta),
                candidatura.ofertaId(),
                candidatura.titulo(),
                candidatura.codigoReferencia(),
                candidatura.entidadeId(),
                candidatura.entidade(),
                candidatura.ilha(),
                descricaoGeografia(candidatura.ilha(), geografias),
                candidatura.concelho(),
                descricaoGeografia(candidatura.concelho(), geografias),
                estado,
                EmpregoDominio.descricao(EmpregoDominio.DOMINIO_STATUS_CANDIDATURA, estado),
                candidatura.dataCandidatura()
        );
    }

    private MinhaCandidaturaDetalheResponse mapearDetalhe(
            CandidaturaRegisto candidatura,
            Map<String, String> geografias
    ) {
        String tipoOferta = normalizarValor(EmpregoDominio.DOMINIO_TIPO_OFERTA, candidatura.tipoOferta());
        String estado = normalizarValor(EmpregoDominio.DOMINIO_STATUS_CANDIDATURA, candidatura.estado());
        String canal = normalizarValor(DOMINIO_CANAL_CANDIDATURA, candidatura.canal());
        return new MinhaCandidaturaDetalheResponse(
                candidatura.candidaturaId(),
                tipoOferta,
                EmpregoDominio.descricao(EmpregoDominio.DOMINIO_TIPO_OFERTA, tipoOferta),
                candidatura.ofertaId(),
                candidatura.titulo(),
                candidatura.codigoReferencia(),
                candidatura.entidadeId(),
                candidatura.entidade(),
                candidatura.ilha(),
                descricaoGeografia(candidatura.ilha(), geografias),
                candidatura.concelho(),
                descricaoGeografia(candidatura.concelho(), geografias),
                estado,
                EmpregoDominio.descricao(EmpregoDominio.DOMINIO_STATUS_CANDIDATURA, estado),
                candidatura.motivoRecusa(),
                canal,
                EmpregoDominio.descricao(DOMINIO_CANAL_CANDIDATURA, canal),
                candidatura.dataCandidatura(),
                converterAnexos(candidatura.anexos())
        );
    }

    private List<MinhaCandidaturaOpcaoResponse> listarDominio(String dominio) {
        return EmpregoDominio.listarPorDominio(dominio).stream()
                .map(item -> new MinhaCandidaturaOpcaoResponse(item.getValor(), item.getDescricao()))
                .toList();
    }

    private List<ConsultaVagaOpcaoResponse> enriquecerGeografias(
            List<ConsultaVagaOpcaoResponse> opcoes,
            Map<String, String> geografias
    ) {
        return opcoes.stream()
                .map(item -> new ConsultaVagaOpcaoResponse(
                        item.id(),
                        item.codigo(),
                        descricaoGeografia(item.codigo(), geografias)
                ))
                .sorted(Comparator.comparing(
                        item -> item.descricao() == null ? "" : item.descricao(),
                        String.CASE_INSENSITIVE_ORDER
                ))
                .toList();
    }

    private String descricaoGeografia(String codigo, Map<String, String> geografias) {
        if (!temTexto(codigo)) {
            return codigo;
        }
        String codigoLimpo = codigo.trim();
        return geografias.computeIfAbsent(codigoLimpo, this::buscarDescricaoGeografia);
    }

    private String buscarDescricaoGeografia(String codigo) {
        try {
            return globalGeografiaService.buscarNomePorCodigo(codigo).orElse(codigo);
        } catch (RuntimeException ex) {
            return codigo;
        }
    }

    private List<CandidaturaDocumentoResponse> converterAnexos(JsonNode anexos) {
        if (anexos == null || anexos.isNull() || anexos.isMissingNode()) {
            return List.of();
        }

        List<CandidaturaDocumentoResponse> documentos = new ArrayList<>();
        Set<String> documentosAdicionados = new LinkedHashSet<>();
        if (anexos.isObject()) {
            adicionarDocumento(
                    primeiroNo(anexos, "curriculumVitae", "curriculoVitae", "curriculo", "cv"),
                    TIPO_DOCUMENTO_CURRICULO,
                    documentos,
                    documentosAdicionados
            );
            adicionarDocumentos(
                    primeiroNo(anexos, "outrosDocumentos", "documentos", "outros", "anexos"),
                    TIPO_DOCUMENTO_OUTRO,
                    documentos,
                    documentosAdicionados
            );
        }
        if (documentos.isEmpty()) {
            adicionarDocumentos(anexos, null, documentos, documentosAdicionados);
        }
        return List.copyOf(documentos);
    }

    private void adicionarDocumentos(
            JsonNode valor,
            String tipoPadrao,
            List<CandidaturaDocumentoResponse> documentos,
            Set<String> documentosAdicionados
    ) {
        if (valor == null || valor.isNull() || valor.isMissingNode()) {
            return;
        }
        if (valor.isArray()) {
            valor.forEach(item -> adicionarDocumentos(item, tipoPadrao, documentos, documentosAdicionados));
            return;
        }
        CandidaturaDocumentoResponse documento = converterDocumento(valor, tipoPadrao);
        if (documento != null) {
            adicionarSemDuplicar(documento, documentos, documentosAdicionados);
            return;
        }
        if (valor.isObject()) {
            valor.elements().forEachRemaining(
                    item -> adicionarDocumentos(item, tipoPadrao, documentos, documentosAdicionados)
            );
        }
    }

    private void adicionarDocumento(
            JsonNode valor,
            String tipoPadrao,
            List<CandidaturaDocumentoResponse> documentos,
            Set<String> documentosAdicionados
    ) {
        CandidaturaDocumentoResponse documento = converterDocumento(valor, tipoPadrao);
        if (documento != null) {
            adicionarSemDuplicar(documento, documentos, documentosAdicionados);
        }
    }

    private void adicionarSemDuplicar(
            CandidaturaDocumentoResponse documento,
            List<CandidaturaDocumentoResponse> documentos,
            Set<String> documentosAdicionados
    ) {
        String identidade = primeiroTexto(documento.path(), documento.url(), documento.nome());
        if (identidade == null || documentosAdicionados.add(identidade)) {
            documentos.add(documento);
        }
    }

    private CandidaturaDocumentoResponse converterDocumento(JsonNode valor, String tipoPadrao) {
        if (valor == null || valor.isNull() || valor.isMissingNode()) {
            return null;
        }
        if (valor.isTextual()) {
            String path = textoOpcional(valor.asText());
            return path == null ? null : new CandidaturaDocumentoResponse(
                    tipoPadrao,
                    nomeDoPath(path),
                    path,
                    documentService.gerarLinkPublico(path)
            );
        }
        if (!valor.isObject()) {
            return null;
        }

        String tipo = primeiroTexto(valor, "tipo", "tipoDocumento", "idTpDoc", "id_tp_doc");
        String path = primeiroTexto(valor, "path", "caminho");
        String url = primeiroTexto(valor, "url", "previewUrl", "ver_documento");
        String nome = primeiroTexto(valor, "nome", "name", "fileName", "ficheiro");
        if (!temTexto(path) && !temTexto(url)) {
            return null;
        }
        if (!temTexto(url) && temTexto(path)) {
            url = documentService.gerarLinkPublico(path);
        }
        if (!temTexto(nome)) {
            nome = nomeDoPath(temTexto(path) ? path : url);
        }
        return new CandidaturaDocumentoResponse(
                temTexto(tipo) ? tipo : tipoPadrao,
                nome,
                path,
                url
        );
    }

    private JsonNode primeiroNo(JsonNode objeto, String... campos) {
        for (String campo : campos) {
            JsonNode valor = objeto.get(campo);
            if (valor != null && !valor.isNull() && !valor.isMissingNode()) {
                return valor;
            }
        }
        return null;
    }

    private String primeiroTexto(JsonNode objeto, String... campos) {
        JsonNode valor = primeiroNo(objeto, campos);
        return valor == null || valor.isContainerNode() ? null : textoOpcional(valor.asText());
    }

    private String primeiroTexto(String... valores) {
        for (String valor : valores) {
            if (temTexto(valor)) {
                return valor.trim();
            }
        }
        return null;
    }

    private String nomeDoPath(String pathOuUrl) {
        if (!temTexto(pathOuUrl)) {
            return null;
        }
        String valor = pathOuUrl.trim();
        try {
            URI uri = new URI(valor);
            if (temTexto(uri.getPath())) {
                valor = uri.getPath();
            }
        } catch (URISyntaxException ignored) {
            int query = valor.indexOf('?');
            if (query >= 0) {
                valor = valor.substring(0, query);
            }
        }
        valor = valor.replace('\\', '/');
        int separador = valor.lastIndexOf('/');
        return separador >= 0 ? valor.substring(separador + 1) : valor;
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

    private void validarCandidatura(Integer candidaturaId) {
        if (candidaturaId == null || candidaturaId <= 0) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Não foi possível identificar a candidatura selecionada. Atualize a lista e tente novamente."
            );
        }
    }

    private String textoOpcional(String valor) {
        return temTexto(valor) ? valor.trim() : null;
    }

    private boolean temTexto(String valor) {
        return valor != null && !valor.trim().isEmpty();
    }
}
