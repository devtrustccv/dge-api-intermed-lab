package cv.dge.dge_api_intermed_lab.web.perfilentidade;

import static org.assertj.core.api.Assertions.assertThat;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;

class PerfilEntidadeEntidadeIdContratoTest {

    private static final List<Class<?>> CONTROLLERS = List.of(
            ColocacaoCandidatoController.class,
            CoordenadorOrientadorController.class,
            GestaoAcompanhamentoController.class,
            GestaoAssiduidadeController.class,
            GestaoAvaliacaoEstagiarioController.class,
            GestaoCandidaturaController.class,
            GestaoRelatorioAcompanhamentoController.class,
            GestaoVagaController.class,
            GestaoVisitaTecnicaController.class,
            IntermediacaoLaboralDashboardController.class
    );

    @Test
    void todosOsEndpointsDoPerfilEntidadeDevemExigirEntidadeId() {
        for (Class<?> controller : CONTROLLERS) {
            for (Method metodo : controller.getDeclaredMethods()) {
                if (!ehEndpoint(metodo)) {
                    continue;
                }

                Parameter parametro = List.of(metodo.getParameters()).stream()
                        .filter(this::ehEntidadeId)
                        .findFirst()
                        .orElse(null);

                assertThat(parametro)
                        .as("%s.%s deve receber entidadeId", controller.getSimpleName(), metodo.getName())
                        .isNotNull();
                assertThat(parametro.getAnnotation(RequestParam.class).required())
                        .as("%s.%s deve exigir entidadeId", controller.getSimpleName(), metodo.getName())
                        .isTrue();
            }
        }
    }

    private boolean ehEndpoint(Method metodo) {
        return metodo.isAnnotationPresent(GetMapping.class)
                || metodo.isAnnotationPresent(PostMapping.class)
                || metodo.isAnnotationPresent(PutMapping.class)
                || metodo.isAnnotationPresent(PatchMapping.class)
                || metodo.isAnnotationPresent(DeleteMapping.class);
    }

    private boolean ehEntidadeId(Parameter parametro) {
        RequestParam requestParam = parametro.getAnnotation(RequestParam.class);
        if (requestParam == null) {
            return false;
        }
        String nome = !requestParam.value().isBlank()
                ? requestParam.value()
                : !requestParam.name().isBlank() ? requestParam.name() : parametro.getName();
        return "entidadeId".equals(nome);
    }
}
