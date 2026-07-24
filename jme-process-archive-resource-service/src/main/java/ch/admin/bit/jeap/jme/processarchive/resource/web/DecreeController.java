package ch.admin.bit.jeap.jme.processarchive.resource.web;

import ch.admin.bit.jeap.jme.processarchive.resource.domain.DecreeDTO;
import ch.admin.bit.jeap.jme.processarchive.resource.domain.DecreeService;
import ch.admin.bit.jeap.jme.processarchive.resource.domain.NewDecreeDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Tag(name = "Decree", description = "Manage decrees.")
@RestController
@RequestMapping("/api/decrees")
@RequiredArgsConstructor
public class DecreeController {

    private final DecreeService decreeService;

    @PostMapping
    @Operation(summary = "Issue a decree.", responses = @ApiResponse(responseCode = "200", description = "issued"))
    public DecreeDTO issueDecree(@RequestBody NewDecreeDTO newDecreeDTO) {
        return decreeService.issueDecree(newDecreeDTO, createNewProcessId());
    }

    // Typically, issuing a decree would be embedded in a business process. But this example does not set up a process,
    // instead we just create a new process id here for simplicity.
    // See the jme-process-context-example project for an example for modelling a process and tracking its progress.
    private String createNewProcessId() {
        return UUID.randomUUID().toString();
    }

}
