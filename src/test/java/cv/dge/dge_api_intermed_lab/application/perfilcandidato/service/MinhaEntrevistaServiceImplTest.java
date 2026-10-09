package cv.dge.dge_api_intermed_lab.application.perfilcandidato.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import cv.dge.dge_api_intermed_lab.application.perfilcandidato.dto.MinhaEntrevistaListaResponse;
import cv.dge.dge_api_intermed_lab.infrastructure.perfilcandidato.repository.MinhaEntrevistaRepository;
import cv.dge.dge_api_intermed_lab.infrastructure.perfilcandidato.repository.MinhaEntrevistaRepository.EntrevistaRegisto;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

@ExtendWith(MockitoExtension.class)
class MinhaEntrevistaServiceImplTest {

    @Mock
    private MinhaEntrevistaRepository entrevistaRepository;

    private MinhaEntrevistaServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new MinhaEntrevistaServiceImpl(entrevistaRepository);
    }

    @Test
    void deveListarAgendamentosDoCandidatoComDataDeEncaminhamento() {
        LocalDate dataEncaminhamento = LocalDate.of(2026, 10, 8);
        LocalDate dataEntrevista = LocalDate.of(2026, 10, 15);
        when(entrevistaRepository.listar(9001L)).thenReturn(List.of(new EntrevistaRegisto(
                25,
                12,
                dataEncaminhamento,
                dataEntrevista,
                LocalTime.of(9, 0),
                LocalTime.of(9, 30),
                "Técnico Exemplo",
                "Sala 2",
                "AGENDADA",
                4,
                "CEFP Praia",
                "ORIENTACAO_PROFISSIONAL",
                "PRESENCIAL",
                "CEFP Praia"
        )));

        List<MinhaEntrevistaListaResponse> resultado = service.listar(9001L);

        assertThat(resultado).singleElement().satisfies(entrevista -> {
            assertThat(entrevista.entrevistaId()).isEqualTo(25);
            assertThat(entrevista.dataEncaminhamento()).isEqualTo(dataEncaminhamento);
            assertThat(entrevista.dataEntrevista()).isEqualTo(dataEntrevista);
            assertThat(entrevista.estado()).isEqualTo("AGENDADA");
        });
        verify(entrevistaRepository).listar(9001L);
    }

    @Test
    void deveDevolverListaVaziaQuandoCandidatoNaoTemAgendamentos() {
        when(entrevistaRepository.listar(9001L)).thenReturn(List.of());

        assertThat(service.listar(9001L)).isEmpty();

        verify(entrevistaRepository).listar(9001L);
    }

    @Test
    void deveExigirIdentificacaoValidaDoCandidato() {
        assertThatThrownBy(() -> service.listar(null))
                .isInstanceOfSatisfying(ResponseStatusException.class, ex ->
                        assertThat(ex.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST));

        verify(entrevistaRepository, never()).listar(null);
    }
}
