package io.github.yhj118.groupware.electronicApproval;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

// 첨부파일 실제 저장, DB 등록 및 삭제 처리
@Slf4j
@Service
@RequiredArgsConstructor
public class EAServiceAttachment {

    private final EAMapper eaMapper;

    private static final Set<String> ALLOWED_EXTENSIONS = Set.of("jpg", "jpeg", "pdf", "zip");
    private static final Path UPLOAD_DIRECTORY = Paths.get("uploads/approval").toAbsolutePath().normalize();

    // 기존 문서 작성 서비스의 트랜잭션에 참여
    @Transactional(propagation = Propagation.MANDATORY)
    public void saveAttachments(int approvalId, List<MultipartFile> files) {
        if (files == null || files.isEmpty()) {
            return;
        }

        // 모든 파일을 먼저 검사하여 잘못된 파일이 섞여 있으면 저장 전 중단
        List<MultipartFile> selectedFiles = new ArrayList<>();
        for (MultipartFile file : files) {
            if (file != null && !file.isEmpty()) {
                findAllowedExtension(file);
                selectedFiles.add(file);
            }
        }
        if (selectedFiles.isEmpty()) {
            return;
        }

        createUploadDirectory();
        List<Path> savedPaths = new ArrayList<>();
        // 파일 일부 저장 후 INSERT 또는 문서 수정이 실패해도 새 파일을 정리
        registerRollbackCleanup(savedPaths);

        List<EARequestAttachment> attachments = new ArrayList<>();
        for (MultipartFile file : selectedFiles) {
            attachments.add(saveFile(approvalId, file, savedPaths));
        }
        eaMapper.insertAttachments(attachments);
    }

    // 삭제를 선택하지 않은 파일은 그대로 두고, 선택한 파일 삭제와 새 파일 추가만 처리
    @Transactional(propagation = Propagation.MANDATORY)
    public void updateAttachments(int approvalId,
                                  List<Integer> deleteAttachmentIds,
                                  List<MultipartFile> files) {
        List<Integer> ids = normalizeDeleteIds(deleteAttachmentIds);
        List<Path> pathsToDelete = new ArrayList<>();

        if (!ids.isEmpty()) {
            List<EAResponseAttachment> attachments = eaMapper.findAttachmentsToDelete(ids, approvalId);
            if (attachments.size() != ids.size()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "삭제할 첨부파일이 없거나 이 문서의 첨부파일이 아닙니다.");
            }
            for (EAResponseAttachment attachment : attachments) {
                pathsToDelete.add(findStoredPath(attachment));
            }
        }

        // 새 파일은 기존 INSERT 로직 재사용; 이후 실패 시 롤백 정리 대상에 포함됨
        saveAttachments(approvalId, files);

        if (!ids.isEmpty()) {
            int deletedCount = eaMapper.deleteAttachments(ids, approvalId);
            if (deletedCount != ids.size()) {
                throw new ResponseStatusException(HttpStatus.CONFLICT,
                        "첨부파일이 변경되었습니다. 수정 화면을 다시 열어주세요.");
            }
            registerDeleteAfterCommit(pathsToDelete);
        }
    }

    // 문서 삭제 시 해당 문서의 모든 첨부파일 DB 행과 실제 파일 정리
    @Transactional(propagation = Propagation.MANDATORY)
    public void deleteAllAttachments(int approvalId) {
        List<EAResponseAttachment> attachments = eaMapper.viewAttachmentList(approvalId);
        if (attachments.isEmpty()) {
            return;
        }
        List<Path> paths = new ArrayList<>();
        for (EAResponseAttachment attachment : attachments) {
            paths.add(findStoredPath(attachment));
        }
        if (eaMapper.deleteAllAttachments(approvalId) != attachments.size()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "첨부파일이 변경되었습니다. 문서를 다시 열어주세요.");
        }
        registerDeleteAfterCommit(paths);
    }

    private List<Integer> normalizeDeleteIds(List<Integer> ids) {
        if (ids == null || ids.isEmpty()) {
            return List.of();
        }
        for (Integer id : ids) {
            if (id == null || id <= 0) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "첨부파일 번호가 올바르지 않습니다.");
            }
        }
        return new ArrayList<>(new LinkedHashSet<>(ids));
    }

    private Path findStoredPath(EAResponseAttachment attachment) {
        if (!StringUtils.hasText(attachment.getFilePath())
                || !StringUtils.hasText(attachment.getStoredFileName())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "첨부파일 저장 경로를 확인할 수 없습니다.");
        }
        Path path = Path.of(attachment.getFilePath()).toAbsolutePath().normalize();
        // DB에 있는 경로라도 uploads/approval 바로 아래의 해당 파일만 삭제
        if (!UPLOAD_DIRECTORY.equals(path.getParent())
                || !path.getFileName().toString().equals(attachment.getStoredFileName())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "첨부파일 저장 경로가 올바르지 않습니다.");
        }
        return path;
    }

    private void registerDeleteAfterCommit(List<Path> paths) {
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                // DB 저장이 확정된 후에만 기존 파일을 삭제
                for (Path path : paths) {
                    deletePhysicalFile(path);
                }
            }
        });
    }

    private void registerRollbackCleanup(List<Path> paths) {
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCompletion(int status) {
                if (status == STATUS_ROLLED_BACK) {
                    for (Path path : paths) {
                        deletePhysicalFile(path);
                    }
                }
            }
        });
    }

    private void deletePhysicalFile(Path path) {
        try {
            // 이미 없는 실제 파일은 삭제 완료 상태로 간주
            Files.deleteIfExists(path);
        } catch (IOException e) {
            // 커밋 후 파일 삭제 실패로 이미 저장된 DB 작업을 실패로 표시하지 않음
            log.warn("첨부파일 실제 삭제 실패: {}", path, e);
        }
    }

    private void createUploadDirectory() {
        try {
            Files.createDirectories(UPLOAD_DIRECTORY);
        } catch (IOException e) {
            throw new IllegalStateException("첨부파일 저장 폴더를 만들 수 없습니다.", e);
        }
    }

    private String findAllowedExtension(MultipartFile file) {
        String name = file.getOriginalFilename();
        if (!StringUtils.hasText(name)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "첨부파일 이름이 없습니다.");
        }
        String originalFileName = StringUtils.cleanPath(name);
        if (originalFileName.contains("..")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "올바르지 않은 파일 이름입니다.");
        }
        String extension = StringUtils.getFilenameExtension(originalFileName);
        if (extension == null || !ALLOWED_EXTENSIONS.contains(extension.toLowerCase(Locale.ROOT))) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "JPG, JPEG, PDF, ZIP 파일만 첨부할 수 있습니다.");
        }
        return extension.toLowerCase(Locale.ROOT);
    }

    private EARequestAttachment saveFile(int approvalId,
                                         MultipartFile file,
                                         List<Path> savedPaths) {
        String originalFileName = StringUtils.cleanPath(file.getOriginalFilename());
        String storedFileName = UUID.randomUUID() + "." + findAllowedExtension(file);
        Path targetPath = UPLOAD_DIRECTORY.resolve(storedFileName);

        // 전송 도중 부분 파일이 남아도 롤백 때 제거할 수 있도록 먼저 기록
        savedPaths.add(targetPath);
        try {
            file.transferTo(targetPath);
        } catch (IOException e) {
            throw new IllegalStateException("첨부파일 저장에 실패했습니다.", e);
        }

        EARequestAttachment attachment = new EARequestAttachment();
        attachment.setApprovalId(approvalId);
        attachment.setOriginalFileName(originalFileName);
        attachment.setStoredFileName(storedFileName);
        attachment.setFilePath("uploads/approval/" + storedFileName);
        attachment.setFileType(file.getContentType());
        attachment.setFileSize(file.getSize());
        return attachment;
    }
}
