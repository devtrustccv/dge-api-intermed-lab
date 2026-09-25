package cv.dge.dge_api_intermed_lab.application.perfilentidade.dto;

public record EmpregoDominioResponse(
        Integer id,
        String description,
        String domainType,
        String dominio,
        Integer ordem,
        String status,
        String valor,
        Integer envFk
) {

    public boolean ativo() {
        if (status == null || status.isBlank()) {
            return true;
        }
        return switch (status.trim().toUpperCase()) {
            case "ATIVE", "ACTIVE", "ATIVO", "A", "1" -> true;
            default -> false;
        };
    }
}
