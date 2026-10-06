package cv.dge.dge_api_intermed_lab.application.perfilentidade.service;

import cv.dge.dge_api_intermed_lab.application.perfilentidade.dto.EmpregoDominioResponse;
import cv.dge.dge_api_intermed_lab.application.perfilentidade.constants.EmpregoDominio;
import cv.dge.dge_api_intermed_lab.infrastructure.perfilentidade.repository.IgrpDominioRepository;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class EmpregoDominioService {

    private final IgrpDominioRepository repository;

    @Value("${igrp.domain.dad:interm_laboral}")
    private String dad;

    public List<EmpregoDominioResponse> consultarTodosCampos(String dominio) {
        String nome = validarNomeDominio(dominio);
        List<EmpregoDominioResponse> itens = carregarDominio(nome);
        if (itens.isEmpty()) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "O domínio \"" + nome + "\" não está registado no IGRP para a aplicação \"" + dad + "\"."
            );
        }
        return itens;
    }

    public List<EmpregoDominioResponse> listarPorDominio(String dominio) {
        String nome = validarNomeDominio(dominio);
        return carregarDominio(nome).stream()
                .filter(EmpregoDominioResponse::ativo)
                .toList();
    }

    public Optional<EmpregoDominioResponse> buscar(String dominio, String valor) {
        String nome = validarNomeDominio(dominio);
        String valorNormalizado = EmpregoDominio.normalizar(valor);
        if (valorNormalizado == null) {
            return Optional.empty();
        }
        String alias = EmpregoDominio.alias(nome, valorNormalizado);
        List<EmpregoDominioResponse> itensDoDad = repository.listarPorDominio(nome, dad);
        Optional<EmpregoDominioResponse> encontrado = buscarNosItens(
                itensDoDad.stream().filter(EmpregoDominioResponse::ativo).toList(),
                valorNormalizado,
                alias
        );
        if (encontrado.isPresent()) {
            return encontrado;
        }
        return buscarNosItens(
                repository.listarPorDominioGlobal(nome, dad).stream()
                        .filter(EmpregoDominioResponse::ativo)
                        .toList(),
                valorNormalizado,
                alias
        );
    }

    public Optional<String> valorOficial(String dominio, String valor) {
        return buscar(dominio, valor).map(EmpregoDominioResponse::valor);
    }

    public String descricao(String dominio, String valor) {
        if (valor == null || valor.isBlank()) {
            return valor;
        }
        String nome = validarNomeDominio(dominio);
        String valorNormalizado = EmpregoDominio.normalizar(valor);
        String alias = EmpregoDominio.alias(nome, valorNormalizado);
        Optional<EmpregoDominioResponse> encontrado = buscarDescricaoNosItens(
                repository.listarPorDominio(nome, dad),
                valorNormalizado,
                alias
        );
        if (encontrado.isEmpty()) {
            encontrado = buscarDescricaoNosItens(
                    repository.listarPorDominioGlobal(nome, dad),
                    valorNormalizado,
                    alias
            );
        }
        return encontrado
                .map(EmpregoDominioResponse::description)
                .filter(descricao -> descricao != null && !descricao.isBlank())
                .orElse(valor);
    }

    public String mensagemValorInvalido(String dominio, String valor) {
        return mensagemValorInvalido(dominio, valor, EmpregoDominio.nomeCampo(dominio));
    }

    public String mensagemValorInvalido(String dominio, String valor, String nomeCampo) {
        String campo = nomeCampo == null || nomeCampo.isBlank()
                ? EmpregoDominio.nomeCampo(dominio)
                : nomeCampo.trim();
        String opcoes = listarPorDominio(dominio).stream()
                .map(item -> item.valor() + " (" + item.description() + ")")
                .collect(Collectors.joining(", "));
        String mensagem = "O valor \"" + valorParaMensagem(valor) + "\" informado no campo \""
                + campo + "\" não é válido.";
        return opcoes.isBlank() ? mensagem : mensagem + " Valores aceites: " + opcoes + ".";
    }

    private String validarNomeDominio(String dominio) {
        if (dominio == null || dominio.isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Informe o nome do domínio que deve ser consultado no IGRP."
            );
        }
        String nome = dominio.trim();
        if (nome.length() > 100) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "O nome do domínio não pode exceder 100 caracteres."
            );
        }
        return nome;
    }

    private List<EmpregoDominioResponse> carregarDominio(String nome) {
        List<EmpregoDominioResponse> itens = repository.listarPorDominio(nome, dad);
        return itens.isEmpty() ? repository.listarPorDominioGlobal(nome, dad) : itens;
    }

    private Optional<EmpregoDominioResponse> buscarNosItens(
            List<EmpregoDominioResponse> itens,
            String valor,
            String alias
    ) {
        return itens.stream()
                .filter(item -> corresponde(item.valor(), valor, alias)
                        || corresponde(item.description(), valor, alias))
                .findFirst();
    }

    private Optional<EmpregoDominioResponse> buscarDescricaoNosItens(
            List<EmpregoDominioResponse> itens,
            String valor,
            String alias
    ) {
        return itens.stream()
                .filter(item -> item.description() != null && !item.description().isBlank())
                .filter(item -> corresponde(item.valor(), valor, alias)
                        || corresponde(item.description(), valor, alias))
                .findFirst();
    }

    private boolean corresponde(String candidato, String valor, String alias) {
        String normalizado = EmpregoDominio.normalizar(candidato);
        return normalizado != null && (normalizado.equals(valor) || normalizado.equals(alias));
    }

    private String valorParaMensagem(String valor) {
        if (valor == null || valor.isBlank()) {
            return "vazio";
        }
        String seguro = valor.trim()
                .replace('"', '\'')
                .replaceAll("[\\p{Cntrl}]", " ");
        return seguro.length() <= 100 ? seguro : seguro.substring(0, 100) + "…";
    }
}
