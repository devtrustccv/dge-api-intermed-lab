package cv.dge.dge_api_intermed_lab.application.perfilcandidato.service;

import cv.dge.dge_api_intermed_lab.application.perfilcandidato.dto.MinhaAssiduidadeDetalheResponse;
import cv.dge.dge_api_intermed_lab.application.perfilcandidato.dto.MinhaAssiduidadeFiltro;
import cv.dge.dge_api_intermed_lab.application.perfilcandidato.dto.MinhaAssiduidadeListaResponse;
import cv.dge.dge_api_intermed_lab.application.perfilcandidato.dto.MinhaAssiduidadeOpcoesResponse;
import cv.dge.dge_api_intermed_lab.application.perfilcandidato.dto.MinhaAssiduidadeRequest;
import java.util.List;
import org.springframework.web.multipart.MultipartFile;

public interface MinhaAssiduidadeService {

    List<MinhaAssiduidadeListaResponse> listar(MinhaAssiduidadeFiltro filtro);

    MinhaAssiduidadeOpcoesResponse listarOpcoes();

    MinhaAssiduidadeDetalheResponse buscarPorId(Integer assiduidadeId, Long pessoaId);

    MinhaAssiduidadeDetalheResponse criar(
            Long pessoaId,
            MinhaAssiduidadeRequest request,
            MultipartFile comprovativo
    );

    MinhaAssiduidadeDetalheResponse atualizar(
            Integer assiduidadeId,
            Long pessoaId,
            MinhaAssiduidadeRequest request,
            MultipartFile novoComprovativo
    );
}
