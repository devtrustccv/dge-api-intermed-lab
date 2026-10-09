package cv.dge.dge_api_intermed_lab.application.perfilcandidato.service;

import cv.dge.dge_api_intermed_lab.application.perfilcandidato.dto.MinhaEntrevistaListaResponse;
import cv.dge.dge_api_intermed_lab.infrastructure.perfilcandidato.repository.MinhaEntrevistaRepository;
import cv.dge.dge_api_intermed_lab.infrastructure.perfilcandidato.repository.MinhaEntrevistaRepository.EntrevistaRegisto;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class MinhaEntrevistaServiceImpl implements MinhaEntrevistaService {

    private final MinhaEntrevistaRepository entrevistaRepository;

    @Override
    @Transactional(readOnly = true)
    public List<MinhaEntrevistaListaResponse> listar(Long pessoaId) {
        validarPessoa(pessoaId);
        return entrevistaRepository.listar(pessoaId).stream()
                .map(this::mapear)
                .toList();
    }

    private MinhaEntrevistaListaResponse mapear(EntrevistaRegisto entrevista) {
        return new MinhaEntrevistaListaResponse(
                entrevista.entrevistaId(),
                entrevista.acolhimentoId(),
                entrevista.dataEncaminhamento(),
                entrevista.dataEntrevista(),
                entrevista.horaInicio(),
                entrevista.horaFim(),
                entrevista.nomeTecnico(),
                entrevista.local(),
                entrevista.estado(),
                entrevista.cefpId(),
                entrevista.cefp(),
                entrevista.tipoServico(),
                entrevista.canal(),
                entrevista.localEntrevista()
        );
    }

    private void validarPessoa(Long pessoaId) {
        if (pessoaId == null || pessoaId <= 0) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Não foi possível identificar o candidato. Atualize a página, entre novamente e tente de novo."
            );
        }
    }
}
