package cv.dge.dge_api_intermed_lab.application.perfilcandidato.service;

import cv.dge.dge_api_intermed_lab.application.perfilcandidato.dto.AdesaoJovemRequest;
import cv.dge.dge_api_intermed_lab.application.perfilcandidato.dto.AdesaoJovemResponse;
import cv.dge.dge_api_intermed_lab.infrastructure.perfilcandidato.repository.PerfilCandidatoAdesaoRepository;
import java.util.Locale;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class PerfilCandidatoAdesaoServiceImpl implements PerfilCandidatoAdesaoService {

    private final PerfilCandidatoAdesaoRepository adesaoRepository;

    @Override
    @Transactional(readOnly = true)
    public AdesaoJovemResponse carregarFormulario(Long pessoaId) {
        validarPessoa(pessoaId);
        return adesaoRepository.buscarFormulario(pessoaId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Não foi possível encontrar os seus dados pessoais. Atualize a página ou contacte o serviço de atendimento."
                ));
    }

    @Override
    @Transactional
    public AdesaoJovemResponse confirmarAdesao(Long pessoaId, AdesaoJovemRequest request) {
        validarPessoa(pessoaId);
        if (request == null) {
            throw erro("Selecione a sua situação profissional antes de confirmar a adesão.");
        }

        String situacaoProfissional = obrigatorio(
                request.situacaoProfissional(),
                "Selecione a sua situação profissional antes de confirmar a adesão."
        ).toUpperCase(Locale.ROOT);
        if (situacaoProfissional.length() > 25) {
            throw erro("A situação profissional selecionada não é válida. Atualize a página e tente novamente.");
        }

        String utilizador = obrigatorio(
                request.utilizador(),
                "Não foi possível identificar o utilizador. Inicie sessão novamente e repita a operação."
        );
        if (utilizador.length() > 50) {
            throw erro("Não foi possível identificar corretamente o utilizador. Inicie sessão novamente e tente de novo.");
        }

        Integer utenteId = adesaoRepository.buscarUtenteId(pessoaId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Não foi possível concluir a adesão porque os seus dados de utente ainda não estão registados. "
                                + "Contacte o serviço de atendimento."
                ));

        if (adesaoRepository.existeAdesao(pessoaId)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "A sua adesão já foi submetida e não precisa de ser registada novamente."
            );
        }

        adesaoRepository.inserir(pessoaId, utenteId, situacaoProfissional, utilizador);
        return carregarFormulario(pessoaId);
    }

    private void validarPessoa(Long pessoaId) {
        if (pessoaId == null || pessoaId <= 0) {
            throw erro("Não foi possível identificar o candidato. Atualize a página, entre novamente e tente de novo.");
        }
    }

    private String obrigatorio(String valor, String mensagem) {
        if (valor == null || valor.trim().isEmpty()) {
            throw erro(mensagem);
        }
        return valor.trim();
    }

    private ResponseStatusException erro(String mensagem) {
        return new ResponseStatusException(HttpStatus.BAD_REQUEST, mensagem);
    }
}
