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
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "A colocação selecionada não foi encontrada. Atualize a página e tente novamente."));
    }

    @Override
    @Transactional(readOnly = true)
    public List<ColocacaoOfertaSelectResponse> listarOfertasPorTipoEEntidade(String tipoOferta, Integer entidadeId) {
        String tipo = normalizarDominioObrigatorio(
                EmpregoDominio.DOMINIO_TIPO_OFERTA,
                tipoOferta,
                "Selecione o tipo de oferta."
        );
        if (entidadeId == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Não foi possível identificar a entidade selecionada. Selecione uma entidade e tente novamente.");
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
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Selecione uma oferta.");
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
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Não foi possível confirmar a eliminação da colocação. Tente novamente.");
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
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Preencha os dados da colocação antes de gravar.");
        }
        String tipoOferta = normalizarDominioObrigatorio(
                EmpregoDominio.DOMINIO_TIPO_OFERTA,
                request.tipoOferta(),
                "Selecione o tipo de oferta."
        );
        if (request.ofertaId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Selecione uma oferta.");
        }
        String codigoReferencia = texto(request.codigoReferencia());
        if (request.pessoaId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Selecione o candidato.");
        }
        String tipoContrato = normalizarDominioObrigatorio(
                EmpregoDominio.DOMINIO_REGIME_CONTRATO,
                request.tipoContrato(),
                "Selecione o tipo de contrato."
        );
        if (request.dataInicioPrevisto() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Informe a data prevista para o início.");
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
                        "Não foi encontrada uma candidatura para o candidato e a oferta selecionados. "
                                + "Confirme os dados e tente novamente."
                ));
    }

    private void validarTipoOferta(String tipoOfertaRequest, String tipoOfertaOferta) {
        String tipoOferta = valorDominio(EmpregoDominio.DOMINIO_TIPO_OFERTA, tipoOfertaOferta);
        if (!String.valueOf(tipoOfertaRequest).equals(tipoOferta)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "O tipo de oferta selecionado não corresponde à oferta escolhida."
            );
        }
    }

    private void garantirAtivo(ColocacaoCandidatoResponse colocacao) {
        String estado = valorDominio(EmpregoDominio.DOMINIO_ESTADO, colocacao.estado());
        if (ESTADO_INATIVO.equals(estado)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Esta colocação já foi eliminada e não pode ser alterada.");
        }
    }

    private void validarNumeros(Integer duracaoContrato) {
        if (duracaoContrato != null && duracaoContrato < 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "A duração do contrato deve ser igual ou superior a zero.");
        }
    }

    private void validarDatas(LocalDate dataInicio, LocalDate dataFim) {
        if (dataInicio != null && dataFim != null && dataFim.isBefore(dataInicio)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "A data prevista para o fim não pode ser anterior à data de início."
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
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Não foi possível identificar a colocação selecionada. Atualize a página e tente novamente.");
        }
    }

    private String utilizadorObrigatorio(String utilizador) {
        return textoObrigatorio(utilizador,
                "Não foi possível identificar o utilizador. Inicie sessão novamente e repita a operação.");
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
                        "Uma das opções selecionadas não é válida. Atualize a página e tente novamente."
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
