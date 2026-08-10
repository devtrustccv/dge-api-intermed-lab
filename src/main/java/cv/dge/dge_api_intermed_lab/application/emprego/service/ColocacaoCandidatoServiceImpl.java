package cv.dge.dge_api_intermed_lab.application.emprego.service;

import cv.dge.dge_api_intermed_lab.application.emprego.dto.ColocacaoCandidatoFiltro;
import cv.dge.dge_api_intermed_lab.application.emprego.dto.ColocacaoCandidatoListaResponse;
import cv.dge.dge_api_intermed_lab.application.emprego.dto.ColocacaoCandidatoRemoverRequest;
import cv.dge.dge_api_intermed_lab.application.emprego.dto.ColocacaoCandidatoRequest;
import cv.dge.dge_api_intermed_lab.application.emprego.dto.ColocacaoCandidatoResponse;
import cv.dge.dge_api_intermed_lab.application.emprego.dto.ColocacaoCandidatoSelectResponse;
import cv.dge.dge_api_intermed_lab.application.emprego.dto.ColocacaoCandidatoVinculo;
import cv.dge.dge_api_intermed_lab.application.emprego.dto.ColocacaoOfertaSelectResponse;
import cv.dge.dge_api_intermed_lab.application.emprego.enums.EmpregoDominio;
import cv.dge.dge_api_intermed_lab.infrastructure.emprego.repository.ColocacaoCandidatoRepository;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class ColocacaoCandidatoServiceImpl implements ColocacaoCandidatoService {

    private static final String ESTADO_ATIVO = "A";
    private static final String ESTADO_INATIVO = "I";

    private final ColocacaoCandidatoRepository colocacaoRepository;

    @Override
    @Transactional(readOnly = true)
    public List<ColocacaoCandidatoListaResponse> listar(ColocacaoCandidatoFiltro filtro) {
        return colocacaoRepository.listar(normalizarFiltro(filtro)).stream()
                .map(this::enriquecerLista)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public ColocacaoCandidatoResponse buscarPorId(Integer id) {
        validarId(id);
        return colocacaoRepository.buscarPorId(id)
                .map(this::enriquecerDetalhe)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Colocacao nao encontrada."));
    }

    @Override
    @Transactional(readOnly = true)
    public List<ColocacaoOfertaSelectResponse> listarOfertasPorTipoEEntidade(String tipoOferta, Integer entidadeId) {
        String tipo = normalizarDominioObrigatorio(
                EmpregoDominio.DOMINIO_TIPO_OFERTA,
                tipoOferta,
                "tipoOferta e obrigatorio."
        );
        if (entidadeId == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "entidadeId e obrigatorio.");
        }
        return colocacaoRepository.listarOfertasPorTipoEEntidade(tipo, entidadeId).stream()
                .map(item -> new ColocacaoOfertaSelectResponse(
                        item.ofertaId(),
                        item.codigoReferencia(),
                        item.titulo(),
                        item.oferta(),
                        valorDominio(EmpregoDominio.DOMINIO_TIPO_OFERTA, item.tipoOferta()),
                        EmpregoDominio.descricao(EmpregoDominio.DOMINIO_TIPO_OFERTA, item.tipoOferta()),
                        item.entidadeId(),
                        item.denominacaoEntidade()
                ))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ColocacaoCandidatoSelectResponse> listarCandidatosPorOferta(Integer ofertaId) {
        if (ofertaId == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "ofertaId e obrigatorio.");
        }
        return colocacaoRepository.listarCandidatosPorOferta(ofertaId);
    }

    @Override
    @Transactional
    public ColocacaoCandidatoResponse criar(ColocacaoCandidatoRequest request) {
        ColocacaoCandidatoRequest dados = validarENormalizarRequest(request);
        String utilizador = utilizadorObrigatorio(dados.utilizador());
        ColocacaoCandidatoVinculo vinculo = resolverVinculoObrigatorio(dados);
        validarTipoOferta(dados.tipoOferta(), vinculo.tipoOferta());

        Integer id = colocacaoRepository.inserir(dados, vinculo, ESTADO_ATIVO, false, utilizador);
        return buscarPorId(id);
    }

    @Override
    @Transactional
    public ColocacaoCandidatoResponse atualizar(Integer id, ColocacaoCandidatoRequest request) {
        validarId(id);
        ColocacaoCandidatoResponse atual = buscarPorId(id);
        garantirAtivo(atual);

        ColocacaoCandidatoRequest dados = validarENormalizarRequest(request);
        String utilizador = utilizadorObrigatorio(dados.utilizador());
        ColocacaoCandidatoVinculo vinculo = resolverVinculoObrigatorio(dados);
        validarTipoOferta(dados.tipoOferta(), vinculo.tipoOferta());

        colocacaoRepository.atualizar(id, dados, vinculo, utilizador);
        return buscarPorId(id);
    }

    @Override
    @Transactional
    public ColocacaoCandidatoResponse remover(Integer id, ColocacaoCandidatoRemoverRequest request) {
        validarId(id);
        if (request == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Dados da remocao sao obrigatorios.");
        }
        String utilizador = utilizadorObrigatorio(request.utilizador());
        buscarPorId(id);
        colocacaoRepository.remover(id, ESTADO_INATIVO, utilizador);
        return buscarPorId(id);
    }

    private ColocacaoCandidatoFiltro normalizarFiltro(ColocacaoCandidatoFiltro filtro) {
        return new ColocacaoCandidatoFiltro(
                normalizarDominioOpcional(EmpregoDominio.DOMINIO_TIPO_OFERTA, filtro.tipoOferta()),
                texto(filtro.codigoReferencia()),
                filtro.pessoaId(),
                normalizarDominioOpcional(EmpregoDominio.DOMINIO_REGIME_CONTRATO, filtro.tipoContrato()),
                filtro.dataInicioPrevisto(),
                filtro.dataRegistoInicio(),
                filtro.dataRegistoFim(),
                filtro.entidadeId()
        );
    }

    private ColocacaoCandidatoRequest validarENormalizarRequest(ColocacaoCandidatoRequest request) {
        if (request == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Dados da colocacao sao obrigatorios.");
        }
        String tipoOferta = normalizarDominioObrigatorio(
                EmpregoDominio.DOMINIO_TIPO_OFERTA,
                request.tipoOferta(),
                "tipoOferta e obrigatorio."
        );
        if (request.ofertaId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "ofertaId e obrigatorio.");
        }
        String codigoReferencia = texto(request.codigoReferencia());
        if (request.pessoaId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "pessoaId e obrigatorio.");
        }
        String tipoContrato = normalizarDominioObrigatorio(
                EmpregoDominio.DOMINIO_REGIME_CONTRATO,
                request.tipoContrato(),
                "tipoContrato e obrigatorio."
        );
        if (request.dataInicioPrevisto() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "dataInicioPrevisto e obrigatorio.");
        }
        utilizadorObrigatorio(request.utilizador());
        validarNumeros(request.duracaoContrato());
        validarDatas(request.dataInicioPrevisto(), request.dataFimPrevisto());

        return new ColocacaoCandidatoRequest(
                tipoOferta,
                request.ofertaId(),
                codigoReferencia,
                request.pessoaId(),
                tipoContrato,
                request.duracaoContrato(),
                request.dataInicioPrevisto(),
                request.dataFimPrevisto(),
                texto(request.descricao()),
                texto(request.contratoPath()),
                texto(request.utilizador())
        );
    }

    private ColocacaoCandidatoVinculo resolverVinculoObrigatorio(ColocacaoCandidatoRequest request) {
        return colocacaoRepository.buscarVinculoCandidatura(request.ofertaId(), request.pessoaId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Candidatura nao encontrada para o ofertaId e pessoaId informados."
                ));
    }

    private void validarTipoOferta(String tipoOfertaRequest, String tipoOfertaOferta) {
        String tipoOferta = valorDominio(EmpregoDominio.DOMINIO_TIPO_OFERTA, tipoOfertaOferta);
        if (!String.valueOf(tipoOfertaRequest).equals(tipoOferta)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "tipoOferta nao corresponde ao tipo da oferta informada pelo ofertaId."
            );
        }
    }

    private void garantirAtivo(ColocacaoCandidatoResponse colocacao) {
        String estado = valorDominio(EmpregoDominio.DOMINIO_ESTADO, colocacao.estado());
        if (ESTADO_INATIVO.equals(estado)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Colocacoes inativas nao podem ser atualizadas.");
        }
    }

    private void validarNumeros(Integer duracaoContrato) {
        if (duracaoContrato != null && duracaoContrato < 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "duracaoContrato nao pode ser negativo.");
        }
    }

    private void validarDatas(LocalDate dataInicio, LocalDate dataFim) {
        if (dataInicio != null && dataFim != null && dataFim.isBefore(dataInicio)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "dataFimPrevisto nao pode ser anterior a dataInicioPrevisto."
            );
        }
    }

    private ColocacaoCandidatoListaResponse enriquecerLista(ColocacaoCandidatoListaResponse item) {
        return new ColocacaoCandidatoListaResponse(
                item.id(),
                valorDominio(EmpregoDominio.DOMINIO_TIPO_OFERTA, item.tipoOferta()),
                EmpregoDominio.descricao(EmpregoDominio.DOMINIO_TIPO_OFERTA, item.tipoOferta()),
                item.codigoReferencia(),
                item.pessoaId(),
                item.nomeCandidato(),
                valorDominio(EmpregoDominio.DOMINIO_REGIME_CONTRATO, item.tipoContrato()),
                EmpregoDominio.descricao(EmpregoDominio.DOMINIO_REGIME_CONTRATO, item.tipoContrato()),
                item.dataInicioPrevisto(),
                item.dataRegisto()
        );
    }

    private ColocacaoCandidatoResponse enriquecerDetalhe(ColocacaoCandidatoResponse item) {
        return new ColocacaoCandidatoResponse(
                item.id(),
                item.idOferta(),
                valorDominio(EmpregoDominio.DOMINIO_TIPO_OFERTA, item.tipoOferta()),
                EmpregoDominio.descricao(EmpregoDominio.DOMINIO_TIPO_OFERTA, item.tipoOferta()),
                item.codigoReferencia(),
                item.entidadeId(),
                item.denominacaoEntidade(),
                item.pessoaId(),
                item.nomeCandidato(),
                item.idCandidatura(),
                valorDominio(EmpregoDominio.DOMINIO_REGIME_CONTRATO, item.tipoContrato()),
                EmpregoDominio.descricao(EmpregoDominio.DOMINIO_REGIME_CONTRATO, item.tipoContrato()),
                item.duracaoContrato(),
                item.dataInicioPrevisto(),
                item.dataFimPrevisto(),
                item.descricao(),
                item.contratoPath(),
                valorDominio(EmpregoDominio.DOMINIO_ESTADO, item.estado()),
                EmpregoDominio.descricao(EmpregoDominio.DOMINIO_ESTADO, item.estado()),
                item.registadoCefp(),
                item.dateCreate(),
                item.userCreate(),
                item.dateUpdate(),
                item.userUpdate()
        );
    }

    private void validarId(Integer id) {
        if (id == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "id e obrigatorio.");
        }
    }

    private String utilizadorObrigatorio(String utilizador) {
        return textoObrigatorio(utilizador, "utilizador e obrigatorio para auditoria.");
    }

    private String textoObrigatorio(String valor, String mensagem) {
        String texto = texto(valor);
        if (texto == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, mensagem);
        }
        return texto;
    }

    private String normalizarDominioObrigatorio(String dominio, String valor, String mensagem) {
        String normalizado = normalizarDominioOpcional(dominio, valor);
        if (normalizado == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, mensagem);
        }
        return normalizado;
    }

    private String normalizarDominioOpcional(String dominio, String valor) {
        String texto = texto(valor);
        if (texto == null) {
            return null;
        }
        return EmpregoDominio.valorOficial(dominio, texto)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        dominio + " invalido: " + texto + "."
                ));
    }

    private String valorDominio(String dominio, String valor) {
        return EmpregoDominio.valorOficial(dominio, valor).orElse(valor);
    }

    private String texto(String valor) {
        if (valor == null || valor.trim().isEmpty()) {
            return null;
        }
        return valor.trim();
    }
}
