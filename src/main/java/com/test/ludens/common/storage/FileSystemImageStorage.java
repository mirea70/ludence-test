package com.test.ludens.common.storage;

import com.test.ludens.common.error.exception.BusinessException;
import com.test.ludens.common.error.info.PostErrorInfo;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

@Component
public class FileSystemImageStorage implements ImageStorage {

    static final int MAX_IMAGE_SIZE = 2 * 1024 * 1024;
    private static final int BUFFER_SIZE = 8 * 1024;
    private static final byte[] PNG_SIGNATURE = {
            (byte) 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A
    };

    private final Path imageDirectory;

    public FileSystemImageStorage(@Value("${storage.image-directory}") String imageDirectory) {
        this.imageDirectory = Path.of(imageDirectory).toAbsolutePath().normalize();
        createDirectory();
    }

    @Override
    public StagedImage stage(InputStream inputStream) {
        Path temporaryFile = createTemporaryFile();
        try (inputStream; OutputStream outputStream = Files.newOutputStream(temporaryFile)) {
            validateAndCopy(inputStream, outputStream);
            return new StagedImage(createImageKey(), temporaryFile);
        } catch (BusinessException exception) {
            deletePath(temporaryFile);
            throw exception;
        } catch (IOException exception) {
            deletePath(temporaryFile);
            throw new BusinessException(PostErrorInfo.IMAGE_STORAGE_FAILED);
        }
    }

    @Override
    public void commit(StagedImage stagedImage) {
        try {
            Files.move(stagedImage.path(), imagePath(stagedImage.key()), StandardCopyOption.ATOMIC_MOVE);
        } catch (AtomicMoveNotSupportedException exception) {
            throw new BusinessException(PostErrorInfo.IMAGE_STORAGE_FAILED);
        } catch (IOException exception) {
            throw new BusinessException(PostErrorInfo.IMAGE_STORAGE_FAILED);
        }
    }

    @Override
    public void discard(StagedImage stagedImage) {
        if (stagedImage != null) {
            deletePath(stagedImage.path());
        }
    }

    @Override
    public Resource get(String imageKey) {
        Resource resource = new FileSystemResource(imagePath(imageKey));
        if (!resource.exists() || !resource.isReadable()) {
            throw new BusinessException(PostErrorInfo.IMAGE_NOT_FOUND);
        }
        return resource;
    }

    @Override
    public void delete(String imageKey) {
        deletePath(imagePath(imageKey));
    }

    private void validateAndCopy(InputStream inputStream, OutputStream outputStream) throws IOException {
        byte[] buffer = new byte[BUFFER_SIZE];
        byte[] signature = new byte[PNG_SIGNATURE.length];
        int totalBytes = 0;
        int signatureBytes = 0;
        int readBytes;

        while ((readBytes = inputStream.read(buffer)) != -1) {
            totalBytes += readBytes;
            if (totalBytes > MAX_IMAGE_SIZE) {
                throw new BusinessException(PostErrorInfo.IMAGE_TOO_LARGE);
            }
            if (signatureBytes < signature.length) {
                int copyLength = Math.min(readBytes, signature.length - signatureBytes);
                System.arraycopy(buffer, 0, signature, signatureBytes, copyLength);
                signatureBytes += copyLength;
            }
            outputStream.write(buffer, 0, readBytes);
        }

        if (signatureBytes < PNG_SIGNATURE.length || !Arrays.equals(signature, PNG_SIGNATURE)) {
            throw new BusinessException(PostErrorInfo.INVALID_IMAGE_FORMAT);
        }
    }

    private void createDirectory() {
        try {
            Files.createDirectories(imageDirectory);
        } catch (IOException exception) {
            throw new BusinessException(PostErrorInfo.IMAGE_STORAGE_FAILED);
        }
    }

    private Path createTemporaryFile() {
        try {
            return Files.createTempFile(imageDirectory, "upload-", ".tmp");
        } catch (IOException exception) {
            throw new BusinessException(PostErrorInfo.IMAGE_STORAGE_FAILED);
        }
    }

    private String createImageKey() {
        return UUID.randomUUID() + ".png";
    }

    private Path imagePath(String imageKey) {
        Path path = imageDirectory.resolve(imageKey).normalize();
        if (!path.getParent().equals(imageDirectory)) {
            throw new BusinessException(PostErrorInfo.IMAGE_STORAGE_FAILED);
        }
        return path;
    }

    private void deletePath(Path path) {
        try {
            Files.deleteIfExists(path);
        } catch (IOException exception) {
            throw new BusinessException(PostErrorInfo.IMAGE_STORAGE_FAILED);
        }
    }
}
