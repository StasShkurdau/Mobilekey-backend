package com.mobilekey.backend.file.controller

import com.mobilekey.backend.file.dto.FileResponse
import com.mobilekey.backend.file.service.FileService
import org.springframework.core.io.InputStreamResource
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.http.codec.multipart.FilePart
import org.springframework.web.bind.annotation.*
import java.util.UUID

@RestController
@RequestMapping("/api/files")
class FileController(
    private val fileService: FileService,
) {

    @PostMapping(consumes = [MediaType.MULTIPART_FORM_DATA_VALUE])
    @ResponseStatus(HttpStatus.CREATED)
    suspend fun upload(@RequestPart("file") filePart: FilePart): FileResponse {
        return fileService.upload(filePart)
    }

    @GetMapping("/{id}")
    suspend fun download(@PathVariable id: UUID): ResponseEntity<InputStreamResource> {
        val (file, inputStream) = fileService.download(id)
        return ResponseEntity.ok()
            .contentType(MediaType.parseMediaType(file.contentType))
            .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"${file.name}\"")
            .contentLength(file.size)
            .body(InputStreamResource(inputStream))
    }
}
