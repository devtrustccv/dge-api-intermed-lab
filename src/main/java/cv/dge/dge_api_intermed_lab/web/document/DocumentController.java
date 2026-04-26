package cv.dge.dge_api_intermed_lab.web.document;

import cv.dge.dge_api_intermed_lab.application.document.service.ComboboxService;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/documentos")
public class DocumentController {

    private final ComboboxService comboboxService;

    @GetMapping("/combobox")
    public List<Map<String, Object>> listarAtivos() {
        return comboboxService.listarDocumentosAtivos();
    }
}
