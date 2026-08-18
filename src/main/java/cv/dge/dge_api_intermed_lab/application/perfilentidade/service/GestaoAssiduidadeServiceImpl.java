package cv.dge.dge_api_intermed_lab.application.perfilentidade.service;

import cv.dge.dge_api_intermed_lab.application.perfilentidade.dto.AssiduidadeEstagiarioDetalheResponse;
import cv.dge.dge_api_intermed_lab.application.perfilentidade.dto.AssiduidadeEstagiarioFiltro;
import cv.dge.dge_api_intermed_lab.application.perfilentidade.dto.AssiduidadeEstagiarioListaResponse;
import cv.dge.dge_api_intermed_lab.application.perfilentidade.dto.AssiduidadeEstagiarioSelectResponse;
import cv.dge.dge_api_intermed_lab.application.perfilentidade.dto.AssiduidadeOfertaSelectResponse;
import cv.dge.dge_api_intermed_lab.application.perfilentidade.dto.AssiduidadeValidacaoRequest;
import cv.dge.dge_api_intermed_lab.application.perfilentidade.enums.EmpregoDominio;
import cv.dge.dge_api_intermed_lab.infrastructure.perfilentidade.repository.GestaoAssiduidadeRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class GestaoAssiduidadeServiceImpl implements GestaoAssiduidadeService {

    private static final String DECISAO_APROVAR = "APROVAR";
    private static final String DECISAO_INDEFER = "INDEFER";
    private static final String ESTADO_APROVADO = "APROVADO";
    private static final String ESTADO_INDEFERIDO = "INDEFERIDO";

    private final GestaoAssiduidadeRepository assiduidadeRepository;

    @Override
    @Transactional(readOnly = true)
    public List<AssiduidadeEstagiarioListaResponse> listar(AssiduidadeEstagiarioFiltro filtro) {
        AssiduidadeEstagiarioFiltro dados = normalizarFiltro(filtro);
        return assiduidadeRepository.listar(dados).stream()
                .map(this::enriquecerLista)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public AssiduidadeEstagiarioDetalheResponse buscarPorId(Integer id, Integer entidadeId) {
        validarId(id);
        validarEntidadeId(entidadeId);
        return assiduidadeRepository.buscarPorId(id, entidadeId)
                .map(this::enriquecerDetalhe)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "O registo de assiduidade selecionado não foi encontrado. Atualize a página e tente novamente."));
    }

    @Override
    @Transactional
    public AssiduidadeEstagiarioDetalheResponse validar(
            Integer id,
            Integer entidadeId,
            AssiduidadeValidacaoRequest request
    ) {
        validarId(id);
        validarEntidadeId(entidadeId);
        if (request == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Selecione uma decisão antes de confirmar a validação.");
        }

        AssiduidadeEstagiarioDetalheResponse atual = buscarPorId(id, entidadeId);
        if (ESTADO_APROVADO.equals(valorDominio(EmpregoDominio.DOMINIO_ESTADO_ASSIDUIDADE, atual.estado()))) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Este registo de assiduidade já foi aprovado e não pode ser validado novamente.");
        }

        String decisao = normalizarDecisaoObrigatoria(request.decisao());
        String utilizador = utilizadorObrigatorio(request.utilizador());
        String novoEstado = estadoPorDecisao(decisao);
        String observacao = texto(request.observacao());

        if (DECISAO_INDEFER.equals(decisao)) {
            String motivo = textoOuPadrao(request.motivoIndeferimento(), observacao);
            if (motivo == null) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                    "Informe o motivo do indeferimento."
                );
            }
            observacao = motivo;
        }

        assiduidadeRepository.validar(id, entidadeId, novoEstado, observacao, utilizador);
        return buscarPorId(id, entidadeId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AssiduidadeEstagiarioSelectResponse> listarEstagiariosParaFiltro(Integer entidadeId) {
        validarEntidadeId(entidadeId);
        return assiduidadeRepository.listarEstagiariosParaFiltro(entidadeId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AssiduidadeOfertaSelectResponse> listarOfertasParaFiltro(Integer entidadeId) {
        validarEntidadeId(entidadeId);
        return assiduidadeRepository.listarOfertasParaFiltro(entidadeId);
    }

    private AssiduidadeEstagiarioFiltro normalizarFiltro(AssiduidadeEstagiarioFiltro filtro) {
        if (filtro == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Não foi possível identificar os dados necessários para consultar a assiduidade."
                            + " Atualize a página e tente novamente.");
        }
        validarEntidadeId(filtro.entidadeId());
        return new AssiduidadeEstagiarioFiltro(
                filtro.entidadeId(),
                filtro.estagiarioId(),
                filtro.ofertaId(),
                normalizarDominioOpcional(EmpregoDominio.DOMINIO_TIPO_ASSIDUIDADE, filtro.tipoAssiduidade()),
                normalizarDominioOpcional(EmpregoDominio.DOMINIO_ESTADO_ASSIDUIDADE, filtro.estado())
        );
    }

    private AssiduidadeEstagiarioListaResponse enriquecerLista(AssiduidadeEstagiarioListaResponse item) {
        String estado = valorDominio(EmpregoDominio.DOMINIO_ESTADO_ASSIDUIDADE, item.estado());
        return new AssiduidadeEstagiarioListaResponse(
                item.id(),
                item.colocacaoId(),
                item.ofertaId(),
                item.oferta(),
                item.estagiarioId(),
                item.estagiario(),
                valorDominio(EmpregoDominio.DOMINIO_TIPO_ASSIDUIDADE, item.tipoAssiduidade()),
                EmpregoDominio.descricao(EmpregoDominio.DOMINIO_TIPO_ASSIDUIDADE, item.tipoAssiduidade()),
                item.data(),
                item.horaEntrada(),
                item.horaSaida(),
                formatarHorario(item.horaEntrada(), item.horaSaida()),
                estado,
                EmpregoDominio.descricao(EmpregoDominio.DOMINIO_ESTADO_ASSIDUIDADE, item.estado()),
                !ESTADO_APROVADO.equals(estado)
        );
    }

    private AssiduidadeEstagiarioDetalheResponse enriquecerDetalhe(AssiduidadeEstagiarioDetalheResponse item) {
        return new AssiduidadeEstagiarioDetalheResponse(
                item.id(),
                item.colocacaoId(),
                item.ofertaId(),
                item.oferta(),
                item.entidadeId(),
                item.denominacaoEntidade(),
                item.estagiarioId(),
                item.estagiario(),
                item.data(),
                item.horaEntrada(),
                item.horaSaida(),
                valorDominio(EmpregoDominio.DOMINIO_TIPO_ASSIDUIDADE, item.tipoAssiduidade()),
                EmpregoDominio.descricao(EmpregoDominio.DOMINIO_TIPO_ASSIDUIDADE, item.tipoAssiduidade()),
                item.justificacao(),
                valorDominio(EmpregoDominio.DOMINIO_ESTADO_ASSIDUIDADE, item.estado()),
                EmpregoDominio.descricao(EmpregoDominio.DOMINIO_ESTADO_ASSIDUIDADE, item.estado()),
                item.observacao(),
                item.comprovativo(),
                item.dateCreate(),
                item.userCreate(),
                item.dateUpdate(),
                item.userUpdate()
        );
    }

    private String normalizarDecisaoObrigatoria(String decisao) {
        String texto = texto(decisao);
        if (texto == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Selecione uma decisão.");
        }
        String normalizado = EmpregoDominio.normalizar(texto);
        if ("INDEFERIR".equals(normalizado) || "INDEFERIDO".equals(normalizado)) {
            normalizado = DECISAO_INDEFER;
        }
        if ("APROVADO".equals(normalizado) || "APROVAR".equals(normalizado)) {
            normalizado = DECISAO_APROVAR;
        }
        return EmpregoDominio.valorOficial(EmpregoDominio.DOMINIO_DECISAO_ASSIDUIDADE, normalizado)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                    "A decisão selecionada não é válida. Atualize a página e tente novamente."
                ));
    }

    private String estadoPorDecisao(String decisao) {
        if (DECISAO_APROVAR.equals(decisao)) {
            return ESTADO_APROVADO;
        }
        if (DECISAO_INDEFER.equals(decisao)) {
            return ESTADO_INDEFERIDO;
        }
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                "A decisão selecionada não é válida. Atualize a página e tente novamente.");
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

    private String formatarHorario(java.time.LocalTime horaEntrada, java.time.LocalTime horaSaida) {
        if (horaEntrada == null && horaSaida == null) {
            return null;
        }
        if (horaEntrada == null) {
            return horaSaida.toString();
        }
        if (horaSaida == null) {
            return horaEntrada.toString();
        }
        return horaEntrada + " - " + horaSaida;
    }

    private void validarId(Integer id) {
        if (id == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Não foi possível identificar o registo de assiduidade. Atualize a página e tente novamente.");
        }
    }

    private void validarEntidadeId(Integer entidadeId) {
        if (entidadeId == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Não foi possível identificar a entidade selecionada. Selecione uma entidade e tente novamente.");
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

    private String textoOuPadrao(String valor, String padrao) {
        String texto = texto(valor);
        return texto == null ? texto(padrao) : texto;
    }

    private String texto(String valor) {
        if (valor == null || valor.trim().isEmpty()) {
            return null;
        }
        return valor.trim();
    }
}
