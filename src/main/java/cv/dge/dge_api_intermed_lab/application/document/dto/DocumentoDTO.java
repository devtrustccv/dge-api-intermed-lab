package cv.dge.dge_api_intermed_lab.application.document.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import org.springframework.web.multipart.MultipartFile;

@Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public class DocumentoDTO {
       @JsonProperty
        private Integer id;
    @JsonProperty
        private String nome;

        private Integer idTpDoc;
    private String urlArquivo;
    private String name;
    private MultipartFile file;
    private String nprocesso;
    public String getNprocesso() {
        return nprocesso;
    }

    public void setNprocesso(String nprocesso) {
        this.nprocesso = nprocesso;
    }




    public String getUrlArquivo() {
        return urlArquivo;
    }

    public void setUrlArquivo(String urlArquivo) {
        this.urlArquivo = urlArquivo;
    }

    public Integer getIdTpDoc() {
        return idTpDoc;
    }

    public void setIdTpDoc(Integer idTpDoc) {
        this.idTpDoc = idTpDoc;
    }

    public MultipartFile getFile() {
        return file;
    }

    public void setFile(MultipartFile file) {
        this.file = file;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }


    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private final DocumentoDTO instance = new DocumentoDTO();

        public Builder id(Integer id) {
            instance.setId(id);
            return this;
        }

        public Builder nome(String nome) {
            instance.setNome(nome);
            return this;
        }

        public Builder idTpDoc(Integer idTpDoc) {
            instance.setIdTpDoc(idTpDoc);
            return this;
        }

        public Builder urlArquivo(String urlArquivo) {
            instance.setUrlArquivo(urlArquivo);
            return this;
        }

        public Builder file(MultipartFile file) {
            instance.setFile(file);
            return this;
        }

        public DocumentoDTO build() {
            return instance;
        }
    }

    }


