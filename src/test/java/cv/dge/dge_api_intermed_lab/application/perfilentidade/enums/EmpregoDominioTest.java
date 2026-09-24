package cv.dge.dge_api_intermed_lab.application.perfilentidade.enums;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class EmpregoDominioTest {

    @Test
    void deveAceitarValorOficialAliasEDescricaoApresentadaNoFrontend() {
        assertThat(EmpregoDominio.valorOficial(EmpregoDominio.DOMINIO_TIPO_OFERTA, "OFERTA_EMPREGO"))
                .contains("OFERTA_EMPREGO");
        assertThat(EmpregoDominio.valorOficial(EmpregoDominio.DOMINIO_TIPO_OFERTA, "Emprego"))
                .contains("OFERTA_EMPREGO");
        assertThat(EmpregoDominio.valorOficial(EmpregoDominio.DOMINIO_TIPO_OFERTA, "Oferta Emprego"))
                .contains("OFERTA_EMPREGO");
        assertThat(EmpregoDominio.valorOficial(EmpregoDominio.DOMINIO_REGIME_CONTRATO, "Contrato a termo"))
                .contains("CONTRATO_TERMO");
    }

    @Test
    void deveExplicarCampoValorEOpcoesAceites() {
        String mensagem = EmpregoDominio.mensagemValorInvalido(
                EmpregoDominio.DOMINIO_TIPO_OFERTA,
                "OUTRA_OPCAO"
        );

        assertThat(mensagem)
                .contains("campo \"Tipo de oferta\"")
                .contains("valor \"OUTRA_OPCAO\"")
                .contains("OFERTA_ESTAGIO (Oferta estágio)")
                .contains("OFERTA_EMPREGO (Oferta Emprego)");
    }

    @Test
    void todosOsDominiosDevemTerNomeDeCampoAmigavel() {
        assertThat(EmpregoDominio.listarDominios())
                .allSatisfy(dominio -> assertThat(EmpregoDominio.nomeCampo(dominio))
                        .doesNotContain("_")
                        .isNotBlank());
    }
}
