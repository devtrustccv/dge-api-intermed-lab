package cv.dge.dge_api_intermed_lab.application.emprego.service;

import cv.dge.dge_api_intermed_lab.application.emprego.dto.CoordenadorOrientadorFiltro;
import cv.dge.dge_api_intermed_lab.application.emprego.dto.CoordenadorOrientadorListaResponse;
import cv.dge.dge_api_intermed_lab.application.emprego.dto.CoordenadorOrientadorRemoverRequest;
import cv.dge.dge_api_intermed_lab.application.emprego.dto.CoordenadorOrientadorRequest;
import cv.dge.dge_api_intermed_lab.application.emprego.dto.CoordenadorOrientadorResponse;
import cv.dge.dge_api_intermed_lab.application.emprego.dto.PessoaGlobalResponse;
import cv.dge.dge_api_intermed_lab.application.emprego.dto.VagaFiltro;
import cv.dge.dge_api_intermed_lab.application.emprego.dto.VagaListaResponse;
import cv.dge.dge_api_intermed_lab.application.emprego.enums.EmpregoDominio;
import cv.dge.dge_api_intermed_lab.infrastructure.emprego.repository.CoordenadorOrientadorRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class CoordenadorOrientadorServiceImpl implements CoordenadorOrientadorService {

    private static final String TIPO_ORIENTADOR = "ORIENTADOR";
    private static final String TIPO_COORDENADOR = "COORDENADOR";
    private static final String ESTADO_ATIVO = "A";
    private static final String ESTADO_INATIVO = "I";

    private final CoordenadorOrientadorRepository coordenadorOrientadorRepository;
    private final GestaoVagaService gestaoVagaService;

    @Override
    @Transactional(readOnly = true)
    public List<CoordenadorOrientadorListaResponse> listar(CoordenadorOrientadorFiltro filtro) {
        return coordenadorOrientadorRepository.listar(normalizarFiltro(filtro)).stream()
                .map(this::enriquecerLista)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public CoordenadorOrientadorResponse buscarPorId(Integer id) {
        validarId(id);
        return coordenadorOrientadorRepository.buscarPorId(id)
                .map(this::enriquecerDetalhe)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Orientador/coordenador nao encontrado."
                ));
    }

    @Override
    @Transactional(readOnly = true)
    public PessoaGlobalResponse buscarPessoa(String tipoDocumento, String numeroDocumento) {
        String numero = textoObrigatorio(numeroDocumento, "numeroDocumento e obrigatorio.");
        return coordenadorOrientadorRepository.buscarPessoaPorNumeroDocumento(numero)
                .map(pessoa -> new PessoaGlobalResponse(
                        pessoa.id(),
                        pessoa.nome(),
                        pessoa.email(),
                        pessoa.telemovel(),
                        texto(tipoDocumento),
                        pessoa.numeroDocumento()
                ))
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Pessoa nao encontrada na base global pelo numeroDocumento informado."
                ));
    }

    @Override
    @Transactional
    public CoordenadorOrientadorResponse criar(CoordenadorOrientadorRequest request) {
        validarRequest(request);
        String utilizador = utilizadorObrigatorio(request.utilizador());
        String tipo = normalizarTipoObrigatorio(request.tipo());
        CoordenadorOrientadorRequest dados = normalizarRequest(request, tipo);
        Integer id = coordenadorOrientadorRepository.inserir(dados, ESTADO_ATIVO, utilizador);
        return buscarPorId(id);
    }

    @Override
    @Transactional
    public CoordenadorOrientadorResponse atualizar(Integer id, CoordenadorOrientadorRequest request) {
        validarId(id);
        validarRequest(request);
        String utilizador = utilizadorObrigatorio(request.utilizador());
        String tipo = normalizarTipoObrigatorio(request.tipo());
        buscarPorId(id);

        coordenadorOrientadorRepository.atualizar(id, normalizarRequest(request, tipo), utilizador);
        return buscarPorId(id);
    }

    @Override
    @Transactional
    public CoordenadorOrientadorResponse remover(Integer id, CoordenadorOrientadorRemoverRequest request) {
        validarId(id);
        if (request == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Dados da remocao sao obrigatorios.");
        }
        String utilizador = utilizadorObrigatorio(request.utilizador());
        buscarPorId(id);
        coordenadorOrientadorRepository.remover(id, utilizador, ESTADO_INATIVO);
        return buscarPorId(id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<VagaListaResponse> listarOfertasAssociadas(Integer id) {
        CoordenadorOrientadorResponse colaborador = buscarPorId(id);
        Integer orientadorId = TIPO_ORIENTADOR.equalsIgnoreCase(String.valueOf(colaborador.tipo())) ? id : null;
        Integer coordenadorId = TIPO_COORDENADOR.equalsIgnoreCase(String.valueOf(colaborador.tipo())) ? id : null;

        if (orientadorId == null && coordenadorId == null) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "O colaborador nao possui tipo ORIENTADOR ou COORDENADOR."
            );
        }

        return gestaoVagaService.listar(new VagaFiltro(
                null,
                null,
                null,
                null,
                null,
                null,
                orientadorId,
                coordenadorId,
                null,
                null,
                null
        ));
    }

    private CoordenadorOrientadorFiltro normalizarFiltro(CoordenadorOrientadorFiltro filtro) {
        return new CoordenadorOrientadorFiltro(
                filtro.nome(),
                normalizarTipoOpcional(filtro.tipo()),
                normalizarEstadoOpcional(filtro.estado()),
                filtro.dataInicio(),
                filtro.dataFim()
        );
    }

    private void validarRequest(CoordenadorOrientadorRequest request) {
        if (request == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Dados do orientador/coordenador sao obrigatorios.");
        }
        utilizadorObrigatorio(request.utilizador());
        normalizarTipoObrigatorio(request.tipo());

        if (request.pessoaId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "pessoaId e obrigatorio.");
        }
        if (!temTexto(request.nome())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "nome e obrigatorio.");
        }
    }

    private CoordenadorOrientadorRequest normalizarRequest(CoordenadorOrientadorRequest request, String tipo) {
        return new CoordenadorOrientadorRequest(
                request.pessoaId(),
                texto(request.nome()),
                tipo,
                texto(request.cargo()),
                texto(request.email()),
                texto(request.telemovel()),
                texto(request.utilizador())
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

    private String normalizarTipoObrigatorio(String tipo) {
        String normalizado = normalizarTipoOpcional(tipo);
        if (normalizado == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "tipo e obrigatorio.");
        }
        return normalizado;
    }

    private String normalizarTipoOpcional(String tipo) {
        return normalizarDominioOpcional(EmpregoDominio.DOMINIO_TIPO_COLABORADOR, tipo);
    }

    private String normalizarEstadoOpcional(String estado) {
        return normalizarDominioOpcional(EmpregoDominio.DOMINIO_ESTADO, estado);
    }

    private CoordenadorOrientadorListaResponse enriquecerLista(CoordenadorOrientadorListaResponse item) {
        return new CoordenadorOrientadorListaResponse(
                item.id(),
                valorDominio(EmpregoDominio.DOMINIO_TIPO_COLABORADOR, item.tipo()),
                EmpregoDominio.descricao(EmpregoDominio.DOMINIO_TIPO_COLABORADOR, item.tipo()),
                item.nome(),
                item.email(),
                item.telemovel(),
                valorDominio(EmpregoDominio.DOMINIO_ESTADO, item.estado()),
                EmpregoDominio.descricao(EmpregoDominio.DOMINIO_ESTADO, item.estado()),
                item.dateCreate(),
                item.userCreate()
        );
    }

    private CoordenadorOrientadorResponse enriquecerDetalhe(CoordenadorOrientadorResponse item) {
        return new CoordenadorOrientadorResponse(
                item.id(),
                valorDominio(EmpregoDominio.DOMINIO_TIPO_COLABORADOR, item.tipo()),
                EmpregoDominio.descricao(EmpregoDominio.DOMINIO_TIPO_COLABORADOR, item.tipo()),
                item.nome(),
                item.pessoaId(),
                item.cargo(),
                item.email(),
                item.telemovel(),
                valorDominio(EmpregoDominio.DOMINIO_ESTADO, item.estado()),
                EmpregoDominio.descricao(EmpregoDominio.DOMINIO_ESTADO, item.estado()),
                item.dateCreate(),
                item.userCreate(),
                item.dateUpdate(),
                item.userUpdate()
        );
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

    private boolean temTexto(String valor) {
        return texto(valor) != null;
    }
}
