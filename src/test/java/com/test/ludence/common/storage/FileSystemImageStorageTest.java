package com.test.ludence.common.storage;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.test.ludence.common.error.exception.BusinessException;
import java.io.ByteArrayInputStream;
import java.nio.file.Files;
import java.nio.file.Path;

import com.test.ludence.common.storage.FileSystemImageStorage;
import com.test.ludence.common.storage.StagedImage;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

@DisplayName("FileSystemImageStorage 테스트")
class FileSystemImageStorageTest {

    private static final byte[] PNG_SIGNATURE = {
            (byte) 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A
    };

    @TempDir
    Path directory;

    @Test
    @DisplayName("PNG 원본 바이트를 임시 저장한 뒤 포스트 ID 파일로 이동한다")
    void preservesOriginalBytes_whenPngIsStored() throws Exception {
        // given
        FileSystemImageStorage storage = new FileSystemImageStorage(directory.toString());
        byte[] image = pngBytes(128);

        // when
        StagedImage stagedImage = storage.stage(new ByteArrayInputStream(image));
        storage.commit(stagedImage);

        // then
        assertThat(stagedImage.key()).matches("[0-9a-f-]{36}\\.png");
        assertThat(Files.readAllBytes(directory.resolve(stagedImage.key()))).isEqualTo(image);
        assertThat(Files.exists(stagedImage.path())).isFalse();
    }

    @Test
    @DisplayName("PNG 시그니처가 아니면 임시 파일을 제거하고 예외가 발생한다")
    void throwsBusinessException_whenSignatureIsInvalid() {
        // given
        FileSystemImageStorage storage = new FileSystemImageStorage(directory.toString());

        // when & then
        assertThatThrownBy(() -> storage.stage(new ByteArrayInputStream(new byte[100])))
                .isInstanceOf(BusinessException.class);
        assertThat(directory.toFile().list()).isEmpty();
    }

    @Test
    @DisplayName("이미지가 2 MiB를 초과하면 임시 파일을 제거하고 예외가 발생한다")
    void throwsBusinessException_whenImageExceedsMaximumSize() {
        // given
        FileSystemImageStorage storage = new FileSystemImageStorage(directory.toString());
        byte[] image = pngBytes(FileSystemImageStorage.MAX_IMAGE_SIZE + 1);

        // when & then
        assertThatThrownBy(() -> storage.stage(new ByteArrayInputStream(image)))
                .isInstanceOf(BusinessException.class);
        assertThat(directory.toFile().list()).isEmpty();
    }

    private byte[] pngBytes(int size) {
        byte[] bytes = new byte[size];
        System.arraycopy(PNG_SIGNATURE, 0, bytes, 0, PNG_SIGNATURE.length);
        return bytes;
    }
}
