package ar.edu.uns.cs.thesisflow.backup.controller

import ar.edu.uns.cs.thesisflow.backup.service.BackupService
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/backup")
class BackupController(private val backupService: BackupService) {

    @GetMapping("/create")
    @PreAuthorize("hasRole('ADMIN')")
    fun createBackup(): ResponseEntity<String> {
        val backup = backupService.createBackup()
        return ResponseEntity.ok()
            .contentType(MediaType.APPLICATION_JSON)
            .header("Content-Disposition", "attachment; filename=thesis-flow-backup.json")
            .body(backup)
    }

    @PostMapping("/restore")
    @PreAuthorize("hasRole('ADMIN')")
    fun restoreBackup(@RequestBody backupJson: String): ResponseEntity<Map<String, String>> {
        backupService.restoreBackup(backupJson)
        return ResponseEntity.ok(mapOf("message" to "Backup restored successfully"))
    }
}
