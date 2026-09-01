package cv.dge.dge_api_intermed_lab.application.perfilcandidato.service;

import cv.dge.dge_api_intermed_lab.application.document.service.DocumentService;
import cv.dge.dge_api_intermed_lab.application.geografia.service.GlobalGeografiaService;
import cv.dge.dge_api_intermed_lab.application.perfilcandidato.dto.MinhaCandidaturaOpcaoResponse;
import cv.dge.dge_api_intermed_lab.application.perfilcandidato.dto.ServicoCandidatoDetalheResponse;
import cv.dge.dge_api_intermed_lab.application.perfilcandidato.dto.ServicoCandidatoFiltro;
import cv.dge.dge_api_intermed_lab.application.perfilcandidato.dto.ServicoCandidatoListaResponse;
import cv.dge.dge_api_intermed_lab.application.perfilcandidato.dto.ServicoCandidatoOpcoesResponse;
import cv.dge.dge_api_intermed_lab.application.perfilcandidato.dto.ServicoContratanteAnexoResponse;
import cv.dge.dge_api_intermed_lab.application.perfilentidade.enums.EmpregoDominio;
import cv.dge.dge_api_intermed_lab.infrastructure.perfilcandidato.repository.ServicoContratanteRepository;
import cv.dge.dge_api_intermed_lab.infrastructure.perfilcandidato.repository.ServicoContratanteRepository.AnexoArmazenado;
import cv.dge.dge_api_intermed_lab.infrastructure.perfilcandidato.repository.ServicoContratanteRepository.ServicoCandidatoRegisto;
import cv.dge.dge_api_intermed_lab.infrastructure.perfilcandidato.repository.ServicoContratanteRepository.ServicoRegisto;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class ServicoCandidatoServiceImpl implements ServicoCandidatoService {

    private static final String ESTADO_SERVICO_ATIVO = "A";
    private static final String STATUS_ACEITACAO_PENDENTE = "PENDENTE";
    private static final String STATUS_ACEITACAO_ACEITE = "ACEITE";
    private static final String STATUS_ACEITACAO_RECUSADO = "RECUSADO";

    private final ServicoContratanteRepository servicoRepository;
    private final GlobalGeografiaService globalGeografiaService;
    private final DocumentService documentService;

    @Override
    @Transactional(readOnly = true)
    public List<ServicoCandidatoListaResponse> listar(ServicoCandidatoFiltro filtro) {
        ServicoCandidatoFiltro dados = normalizarFiltro(filtro);
        return servicoRepository.listarParaCandidato(dados).stream()
                .map(this::mapearLista)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public ServicoCandidatoOpcoesResponse listarOpcoes() {
        List<MinhaCandidaturaOpcaoResponse> estados = EmpregoDominio
                .listarPorDominio(EmpregoDominio.DOMINIO_ESTADO_SERVICO)
                .stream()
                .map(item -> new MinhaCandidaturaOpcaoResponse(item.getValor(), item.getDescricao()))
                .toList();
        return new ServicoCandidatoOpcoesResponse(estados);
    }

    @Override
    @Transactional(readOnly = true)
    public ServicoCandidatoDetalheResponse buscarPorId(Integer servicoId, Long pessoaId) {
        validarServico(servicoId);
        validarPessoa(pessoaId);
        return mapearDetalhe(buscarRegisto(servicoId, pessoaId));
    }

    @Override
    @Transactional
    public ServicoCandidatoDetalheResponse aceitar(Integer servicoId, Long pessoaId, String utilizador) {
        return alterarAceitacao(servicoId, pessoaId, utilizador, STATUS_ACEITACAO_ACEITE);
    }

    @Override
    @Transactional
    public ServicoCandidatoDetalheResponse recusar(Integer servicoId, Long pessoaId, String utilizador) {
        return alterarAceitacao(servicoId, pessoaId, utilizador, STATUS_ACEITACAO_RECUSADO);
    }

    private ServicoCandidatoDetalheResponse alterarAceitacao(
            Integer servicoId,
            Long pessoaId,
            String utilizador,
            String novoStatus
    ) {
        validarServico(servicoId);
        validarPessoa(pessoaId);
        String utilizadorLimpo = validarUtilizador(utilizador);
        ServicoCandidatoRegisto atual = buscarRegisto(servicoId, pessoaId);

        if (!ESTADO_SERVICO_ATIVO.equals(normalizarEstadoServico(atual.servico().estado()))) {
            throw erro(HttpStatus.CONFLICT, "Só é possível responder a um serviço ativo.");
        }
        if (!"S".equals(normalizarSimNao(atual.selecaoIefp()))) {
            throw erro(
                    HttpStatus.CONFLICT,
                    "Este serviço não foi indicado pelo IEFP para o candidato."
            );
        }

        String statusAtual = normalizarStatusAceitacao(atual.statusAceitacao());
        if (novoStatus.equals(statusAtual)) {
            return mapearDetalhe(atual);
        }
        if (!STATUS_ACEITACAO_PENDENTE.equals(statusAtual)) {
            throw erro(
                    HttpStatus.CONFLICT,
                    "A indicação já foi respondida e não pode ser alterada."
            );
        }
        if (!servicoRepository.atualizarAceitacaoCandidato(
                servicoId,
                atual.candidaturaId(),
                pessoaId,
                novoStatus,
                utilizadorLimpo
        )) {
            throw servicoNaoEncontrado();
        }
        return mapearDetalhe(buscarRegisto(servicoId, pessoaId));
    }

    private ServicoCandidatoFiltro normalizarFiltro(ServicoCandidatoFiltro filtro) {
        if (filtro == null) {
            throw erro(HttpStatus.BAD_REQUEST, "Não foi possível carregar os serviços. Atualize a página.");
        }
        validarPessoa(filtro.pessoaId());
        validarPeriodo(filtro.dataInicio(), filtro.dataFim());
        return new ServicoCandidatoFiltro(
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

    private ServicoCandidatoListaResponse mapearLista(ServicoCandidatoRegisto registo) {
        ServicoRegisto servico = registo.servico();
        String estado = normalizarEstadoServico(servico.estado());
        String selecaoIefp = normalizarSimNao(registo.selecaoIefp());
        String statusAceitacao = normalizarStatusAceitacao(registo.statusAceitacao());
        return new ServicoCandidatoListaResponse(
                servico.servicoId(),
                registo.candidaturaId(),
                servico.tipoServico(),
                servico.nomeContratante(),
                estado,
                EmpregoDominio.descricao(EmpregoDominio.DOMINIO_ESTADO_SERVICO, estado),
                servico.inicioCandidatura(),
                servico.fimCandidatura(),
                selecaoIefp,
                EmpregoDominio.descricao(EmpregoDominio.DOMINIO_SIM_NAO, selecaoIefp),
                statusAceitacao,
                EmpregoDominio.descricao(
                        EmpregoDominio.DOMINIO_STATUS_ACEITACAO_CANDIDATO,
                        statusAceitacao
                ),
                servico.dateCreate()
        );
    }

    private ServicoCandidatoDetalheResponse mapearDetalhe(ServicoCandidatoRegisto registo) {
        ServicoRegisto servico = registo.servico();
        String estado = normalizarEstadoServico(servico.estado());
        String selecaoIefp = normalizarSimNao(registo.selecaoIefp());
        String statusAceitacao = normalizarStatusAceitacao(registo.statusAceitacao());
        return new ServicoCandidatoDetalheResponse(
                servico.servicoId(),
                registo.candidaturaId(),
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
                selecaoIefp,
                EmpregoDominio.descricao(EmpregoDominio.DOMINIO_SIM_NAO, selecaoIefp),
                statusAceitacao,
                EmpregoDominio.descricao(
                        EmpregoDominio.DOMINIO_STATUS_ACEITACAO_CANDIDATO,
                        statusAceitacao
                ),
                servico.dateCreate()
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

    private ServicoCandidatoRegisto buscarRegisto(Integer servicoId, Long pessoaId) {
        return servicoRepository.buscarParaCandidato(servicoId, pessoaId)
                .orElseThrow(this::servicoNaoEncontrado);
    }

    private ResponseStatusException servicoNaoEncontrado() {
        return erro(
                HttpStatus.NOT_FOUND,
                "O serviço não existe ou não foi indicado para este candidato."
        );
    }

    private String normalizarEstadoServico(String estado) {
        if (!temTexto(estado)) {
            return estado;
        }
        return EmpregoDominio.valorOficial(EmpregoDominio.DOMINIO_ESTADO_SERVICO, estado)
                .orElseGet(() -> EmpregoDominio.normalizar(estado));
    }

    private String normalizarStatusAceitacao(String status) {
        if (!temTexto(status)) {
            return STATUS_ACEITACAO_PENDENTE;
        }
        return EmpregoDominio.valorOficial(EmpregoDominio.DOMINIO_STATUS_ACEITACAO_CANDIDATO, status)
                .orElseGet(() -> EmpregoDominio.normalizar(status));
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
                    "Não foi possível identificar o candidato. Entre novamente e tente de novo."
            );
        }
    }

    private void validarServico(Integer servicoId) {
        if (servicoId == null || servicoId <= 0) {
            throw erro(HttpStatus.BAD_REQUEST, "Não foi possível identificar o serviço selecionado.");
        }
    }

    private String validarUtilizador(String utilizador) {
        String valor = textoOpcional(utilizador);
        if (valor == null) {
            throw erro(
                    HttpStatus.BAD_REQUEST,
                    "Não foi possível identificar o utilizador. Entre novamente e tente de novo."
            );
        }
        if (valor.length() > 25) {
            throw erro(HttpStatus.BAD_REQUEST, "A identificação do utilizador não pode exceder 25 caracteres.");
        }
        return valor;
    }

    private String descricaoGeografia(String codigo) {
        if (!temTexto(codigo)) {
            return codigo;
        }
        return globalGeografiaService.buscarNomePorCodigo(codigo.trim()).orElse(codigo.trim());
    }

    private String textoOpcional(String valor) {
        return temTexto(valor) ? valor.trim() : null;
    }

    private boolean temTexto(String valor) {
        return valor != null && !valor.trim().isEmpty();
    }

    private ResponseStatusException erro(HttpStatus status, String mensagem) {
        return new ResponseStatusException(status, mensagem);
    }
}
