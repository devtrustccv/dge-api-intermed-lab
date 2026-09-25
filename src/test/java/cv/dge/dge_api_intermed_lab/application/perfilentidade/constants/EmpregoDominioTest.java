package cv.dge.dge_api_intermed_lab.application.perfilentidade.constants;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class EmpregoDominioTest {

    @Test
    void deveConterSomenteIdentificadoresERegrasSemValoresDeCatalogo() {
        assertThat(EmpregoDominio.class.isEnum()).isFalse();
        assertThat(EmpregoDominio.class.getDeclaredFields())
                .extracting(field -> field.getName())
                .allMatch(nome -> nome.startsWith("DOMINIO_"));
    }

    @Test
    void deveNormalizarNomesDescricoesEAcentos() {
        assertThat(EmpregoDominio.normalizar(" Formação profissional "))
                .isEqualTo("FORMACAO_PROFISSIONAL");
        assertThat(EmpregoDominio.normalizar("Pré-selecionado"))
                .isEqualTo("PRE_SELECIONADO");
    }

    @Test
    void deveManterAliasesDeCompatibilidadeDaApi() {
        assertThat(EmpregoDominio.alias(EmpregoDominio.DOMINIO_TIPO_OFERTA, "EMPREGO"))
                .isEqualTo("OFERTA_EMPREGO");
        assertThat(EmpregoDominio.alias(EmpregoDominio.DOMINIO_HABILITACAO_LITERARIA, "12_ANO"))
                .isEqualTo("ENSINO_SECUNDARIO");
    }

    @Test
    void deveGerarNomesDeCamposAmigaveis() {
        assertThat(EmpregoDominio.nomeCampo(EmpregoDominio.DOMINIO_HABILITACAO_LITERARIA))
                .isEqualTo("Habilitação literária");
        assertThat(EmpregoDominio.mensagemCampoObrigatorio(EmpregoDominio.DOMINIO_ESTADO_SERVICO))
                .isEqualTo("O campo \"Estado do serviço\" é obrigatório.");
    }
}
