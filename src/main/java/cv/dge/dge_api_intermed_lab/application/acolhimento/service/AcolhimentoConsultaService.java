package cv.dge.dge_api_intermed_lab.application.acolhimento.service;

import cv.dge.dge_api_intermed_lab.application.acolhimento.dto.AcolhimentoEntidadeResponse;
import cv.dge.dge_api_intermed_lab.application.acolhimento.dto.AcolhimentoPessoaResponse;

public interface AcolhimentoConsultaService {

    AcolhimentoPessoaResponse buscarPorIdPessoa(Integer idPessoa);

    AcolhimentoEntidadeResponse buscarPorIdEntidade(Integer globalIdEntidade);
}
