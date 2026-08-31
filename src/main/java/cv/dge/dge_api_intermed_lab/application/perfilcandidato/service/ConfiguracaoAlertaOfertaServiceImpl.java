package cv.dge.dge_api_intermed_lab.application.perfilcandidato.service;

import cv.dge.dge_api_intermed_lab.application.geografia.service.GlobalGeografiaService;
import cv.dge.dge_api_intermed_lab.application.perfilcandidato.dto.AlertaOfertaDetalheResponse;
import cv.dge.dge_api_intermed_lab.application.perfilcandidato.dto.AlertaOfertaListaResponse;
import cv.dge.dge_api_intermed_lab.application.perfilcandidato.dto.AlertaOfertaOpcoesResponse;
import cv.dge.dge_api_intermed_lab.application.perfilcandidato.dto.AlertaOfertaRequest;
import cv.dge.dge_api_intermed_lab.application.perfilcandidato.dto.MinhaCandidaturaOpcaoResponse;
import cv.dge.dge_api_intermed_lab.application.perfilentidade.enums.EmpregoDominio;
import cv.dge.dge_api_intermed_lab.infrastructure.perfilcandidato.repository.ConfiguracaoAlertaOfertaRepository;
import cv.dge.dge_api_intermed_lab.infrastructure.perfilcandidato.repository.ConfiguracaoAlertaOfertaRepository.AlertaOfertaDetalheRegisto;
import cv.dge.dge_api_intermed_lab.infrastructure.perfilcandidato.repository.ConfiguracaoAlertaOfertaRepository.AlertaOfertaListaRegisto;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class ConfiguracaoAlertaOfertaServiceImpl implements ConfiguracaoAlertaOfertaService {

    private static final String ESTADO_ATIVO = "ATIVO";

    private final ConfiguracaoAlertaOfertaRepository alertaRepository;
    private final GlobalGeografiaService globalGeografiaService;

    @Override
    @Transactional(readOnly = true)
    public List<AlertaOfertaListaResponse> listar(Long pessoaId) {
        validarPessoa(pessoaId);
        return alertaRepository.listar(pessoaId).stream()
                .map(this::mapearLista)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public AlertaOfertaOpcoesResponse listarOpcoes(Long pessoaId, String ilha) {
        validarPessoa(pessoaId);
        String ilhaLimpa = textoOpcional(ilha);
        return new AlertaOfertaOpcoesResponse(
                pessoaId,
                alertaRepository.buscarNomePessoa(pessoaId).orElse(null),
                listarDominio(EmpregoDominio.DOMINIO_TIPO_OFERTA),
                alertaRepository.listarIlhas(),
                alertaRepository.listarConcelhos(ilhaLimpa),
                alertaRepository.listarEntidades(),
                listarDominio(EmpregoDominio.DOMINIO_HABILITACAO_LITERARIA),
                listarDominio(EmpregoDominio.DOMINIO_NIVEL_QUALIFICACAO)
        );
    }

    @Override
    @Transactional(readOnly = true)
    public AlertaOfertaDetalheResponse buscarPorId(Integer alertaId, Long pessoaId) {
        validarPessoa(pessoaId);
        validarAlerta(alertaId);
        return alertaRepository.buscarPorId(alertaId, pessoaId)
                .map(this::mapearDetalhe)
                .orElseThrow(this::alertaNaoEncontrado);
    }

    @Override
    @Transactional
    public AlertaOfertaDetalheResponse criar(Long pessoaId, AlertaOfertaRequest request) {
        validarPessoa(pessoaId);
        AlertaOfertaRequest dados = validarRequest(request);
        Integer alertaId = alertaRepository.inserir(pessoaId, dados, ESTADO_ATIVO);
        if (alertaId == null) {
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "A configuração foi gravada, mas não foi possível identificar o registo criado. Atualize a lista."
            );
        }
        return buscarPorId(alertaId, pessoaId);
    }

    @Override
    @Transactional
    public AlertaOfertaDetalheResponse atualizar(
            Integer alertaId,
            Long pessoaId,
            AlertaOfertaRequest request
    ) {
        validarPessoa(pessoaId);
        validarAlerta(alertaId);
        AlertaOfertaRequest dados = validarRequest(request);
        if (!alertaRepository.atualizar(alertaId, pessoaId, dados)) {
            throw alertaNaoEncontrado();
        }
        return buscarPorId(alertaId, pessoaId);
    }

    private AlertaOfertaRequest validarRequest(AlertaOfertaRequest request) {
        if (request == null) {
            throw erro("Preencha os dados da configuração antes de gravar.");
        }
        String tipoOferta = normalizarDominioOpcional(
                EmpregoDominio.DOMINIO_TIPO_OFERTA,
                request.tipoOferta(),
                "Selecione um tipo de oferta válido."
        );
        String habilitacaoLiteraria = normalizarDominioOpcional(
                EmpregoDominio.DOMINIO_HABILITACAO_LITERARIA,
                request.habilitacaoLiteraria(),
                "Selecione uma habilitação literária válida."
        );
        String nivelQualificacao = normalizarDominioOpcional(
                EmpregoDominio.DOMINIO_NIVEL_QUALIFICACAO,
                request.nivelQualificacao(),
                "Selecione um nível de qualificação válido."
        );
        String ilha = textoOpcional(request.ilha());
        String concelho = textoOpcional(request.concelho());
        String utilizador = obrigatorio(
                request.utilizador(),
                "Não foi possível identificar o utilizador. Inicie sessão novamente e tente de novo."
        );

        validarTamanho(ilha, 10, "O identificador da ilha não é válido.");
        validarTamanho(concelho, 10, "O identificador do concelho não é válido.");
        validarTamanho(utilizador, 50, "O email do utilizador excede o tamanho permitido.");
        if (concelho != null && ilha == null) {
            throw erro("Selecione primeiro a ilha correspondente ao concelho.");
        }
        if (request.entidadeId() != null && request.entidadeId() <= 0) {
            throw erro("Selecione uma entidade válida.");
        }
        if (ilha != null && !alertaRepository.existeIlha(ilha)) {
            throw erro("A ilha selecionada não é válida. Atualize as opções e tente novamente.");
        }
        if (concelho != null && !alertaRepository.existeConcelho(concelho, ilha)) {
            throw erro("O concelho selecionado não pertence à ilha informada.");
        }
        if (request.entidadeId() != null && !alertaRepository.existeEntidade(request.entidadeId())) {
            throw erro("A entidade selecionada não é válida. Atualize as opções e tente novamente.");
        }

        return new AlertaOfertaRequest(
                tipoOferta,
                ilha,
                concelho,
                request.entidadeId(),
                habilitacaoLiteraria,
                nivelQualificacao,
                utilizador
        );
    }

    private AlertaOfertaListaResponse mapearLista(AlertaOfertaListaRegisto alerta) {
        String tipoOferta = normalizarValor(EmpregoDominio.DOMINIO_TIPO_OFERTA, alerta.tipoOferta());
        String habilitacao = normalizarValor(
                EmpregoDominio.DOMINIO_HABILITACAO_LITERARIA,
                alerta.habilitacaoLiteraria()
        );
        String nivel = normalizarValor(
                EmpregoDominio.DOMINIO_NIVEL_QUALIFICACAO,
                alerta.nivelQualificacao()
        );
        return new AlertaOfertaListaResponse(
                alerta.alertaId(),
                tipoOferta,
                EmpregoDominio.descricao(EmpregoDominio.DOMINIO_TIPO_OFERTA, tipoOferta),
                habilitacao,
                EmpregoDominio.descricao(EmpregoDominio.DOMINIO_HABILITACAO_LITERARIA, habilitacao),
                nivel,
                EmpregoDominio.descricao(EmpregoDominio.DOMINIO_NIVEL_QUALIFICACAO, nivel),
                alerta.estado(),
                EmpregoDominio.descricao(EmpregoDominio.DOMINIO_ESTADO, alerta.estado()),
                alerta.dataConfiguracao()
        );
    }

    private AlertaOfertaDetalheResponse mapearDetalhe(AlertaOfertaDetalheRegisto alerta) {
        String tipoOferta = normalizarValor(EmpregoDominio.DOMINIO_TIPO_OFERTA, alerta.tipoOferta());
        String habilitacao = normalizarValor(
                EmpregoDominio.DOMINIO_HABILITACAO_LITERARIA,
                alerta.habilitacaoLiteraria()
        );
        String nivel = normalizarValor(
                EmpregoDominio.DOMINIO_NIVEL_QUALIFICACAO,
                alerta.nivelQualificacao()
        );
        return new AlertaOfertaDetalheResponse(
                alerta.alertaId(),
                alerta.pessoaId(),
                alertaRepository.buscarNomePessoa(alerta.pessoaId()).orElse(null),
                tipoOferta,
                EmpregoDominio.descricao(EmpregoDominio.DOMINIO_TIPO_OFERTA, tipoOferta),
                alerta.ilha(),
                descricaoGeografia(alerta.ilha()),
                alerta.concelho(),
                descricaoGeografia(alerta.concelho()),
                alerta.entidadeId(),
                alertaRepository.buscarDenominacaoEntidade(alerta.entidadeId()).orElse(null),
                habilitacao,
                EmpregoDominio.descricao(EmpregoDominio.DOMINIO_HABILITACAO_LITERARIA, habilitacao),
                nivel,
                EmpregoDominio.descricao(EmpregoDominio.DOMINIO_NIVEL_QUALIFICACAO, nivel),
                alerta.estado(),
                EmpregoDominio.descricao(EmpregoDominio.DOMINIO_ESTADO, alerta.estado()),
                alerta.dateCreate(),
                alerta.userCreate(),
                alerta.dateUpdate(),
                alerta.userUpdate()
        );
    }

    private List<MinhaCandidaturaOpcaoResponse> listarDominio(String dominio) {
        return EmpregoDominio.listarPorDominio(dominio).stream()
                .map(item -> new MinhaCandidaturaOpcaoResponse(item.getValor(), item.getDescricao()))
                .toList();
    }

    private String descricaoGeografia(String codigo) {
        if (!temTexto(codigo)) {
            return codigo;
        }
        return globalGeografiaService.buscarNomePorCodigo(codigo.trim()).orElse(codigo.trim());
    }

    private String normalizarDominioOpcional(String dominio, String valor, String mensagem) {
        if (!temTexto(valor)) {
            return null;
        }
        return EmpregoDominio.valorOficial(dominio, valor)
                .orElseThrow(() -> erro(mensagem));
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
            throw erro("Não foi possível identificar o candidato. Atualize a página, entre novamente e tente de novo.");
        }
    }

    private void validarAlerta(Integer alertaId) {
        if (alertaId == null || alertaId <= 0) {
            throw erro("Não foi possível identificar a configuração selecionada. Atualize a lista e tente novamente.");
        }
    }

    private void validarTamanho(String valor, int maximo, String mensagem) {
        if (valor != null && valor.length() > maximo) {
            throw erro(mensagem);
        }
    }

    private String obrigatorio(String valor, String mensagem) {
        String texto = textoOpcional(valor);
        if (texto == null) {
            throw erro(mensagem);
        }
        return texto;
    }

    private String textoOpcional(String valor) {
        return temTexto(valor) ? valor.trim() : null;
    }

    private boolean temTexto(String valor) {
        return valor != null && !valor.trim().isEmpty();
    }

    private ResponseStatusException alertaNaoEncontrado() {
        return new ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "A configuração selecionada não existe ou não pertence ao candidato. Atualize a lista e tente novamente."
        );
    }

    private ResponseStatusException erro(String mensagem) {
        return new ResponseStatusException(HttpStatus.BAD_REQUEST, mensagem);
    }
}
