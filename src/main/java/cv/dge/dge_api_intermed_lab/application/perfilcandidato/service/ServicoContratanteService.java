package cv.dge.dge_api_intermed_lab.application.perfilcandidato.service;

import cv.dge.dge_api_intermed_lab.application.perfilcandidato.dto.ServicoContratanteCandidatoFiltro;
import cv.dge.dge_api_intermed_lab.application.perfilcandidato.dto.ServicoContratanteCandidatoListaResponse;
import cv.dge.dge_api_intermed_lab.application.perfilcandidato.dto.ServicoContratanteCandidatosOpcoesResponse;
import cv.dge.dge_api_intermed_lab.application.perfilcandidato.dto.ServicoContratanteDetalheResponse;
import cv.dge.dge_api_intermed_lab.application.perfilcandidato.dto.ServicoContratanteFiltro;
import cv.dge.dge_api_intermed_lab.application.perfilcandidato.dto.ServicoContratanteListaResponse;
import cv.dge.dge_api_intermed_lab.application.perfilcandidato.dto.ServicoContratanteOpcoesResponse;
import cv.dge.dge_api_intermed_lab.application.perfilcandidato.dto.ServicoContratanteRequest;
import java.util.List;
import org.springframework.web.multipart.MultipartFile;

public interface ServicoContratanteService {

    List<ServicoContratanteListaResponse> listar(ServicoContratanteFiltro filtro);

    ServicoContratanteOpcoesResponse listarOpcoes(String ilha, String concelho);

    ServicoContratanteDetalheResponse buscarPorId(Integer servicoId, Long pessoaId);

    ServicoContratanteDetalheResponse criar(
            Long pessoaId,
            ServicoContratanteRequest request,
            List<MultipartFile> anexos,
            boolean rascunho
    );

    ServicoContratanteDetalheResponse atualizar(
            Integer servicoId,
            Long pessoaId,
            ServicoContratanteRequest request,
            List<MultipartFile> novosAnexos
    );

    ServicoContratanteDetalheResponse cancelar(Integer servicoId, Long pessoaId, String utilizador);

    ServicoContratanteDetalheResponse remover(Integer servicoId, Long pessoaId, String utilizador);

    List<ServicoContratanteCandidatoListaResponse> listarCandidatos(
            ServicoContratanteCandidatoFiltro filtro
    );

    ServicoContratanteCandidatosOpcoesResponse listarOpcoesCandidatos(
            Integer servicoId,
            Long pessoaId
    );

    ServicoContratanteCandidatoListaResponse selecionarCandidato(
            Integer servicoId,
            Integer candidaturaId,
            Long pessoaId,
            String utilizador
    );
}
