package ch.admin.bit.jeap.jme.processarchive.resource.web;

import ch.admin.bit.jeap.jme.processarchive.resource.domain.DeclarationDTO;
import ch.admin.bit.jeap.jme.processarchive.resource.domain.DeclarationService;
import ch.admin.bit.jeap.jme.processarchive.resource.domain.NewDeclarationDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Declaration", description = "Manage declarations")
@RestController
@RequestMapping("/api/declarations")
@RequiredArgsConstructor
public class DeclarationController {
    private final DeclarationService declarationService;

    @PutMapping("/{declarationId}")
    @Operation(summary = "Send a create declaration command.", responses = @ApiResponse(responseCode = "200", description = "sent"))
    public DeclarationDTO createDeclaration(@PathVariable("declarationId") String declarationId, @RequestBody NewDeclarationDTO newDeclarationDTO) {
        return declarationService.createDeclaration(declarationId, newDeclarationDTO.payload());
    }

}
