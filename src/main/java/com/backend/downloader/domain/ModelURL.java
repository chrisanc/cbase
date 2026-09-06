package com.backend.downloader.domain;

import com.backend.types.FilePath;
import com.backend.downloader.ports.FileDownloader;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

public enum ModelURL {
    QWEN(new String[]{
            "https://huggingface.co/chrisanc/cbase-models/resolve/main/qwen/config.json",
            "https://huggingface.co/chrisanc/cbase-models/resolve/main/qwen/generation_config.json",
            "https://huggingface.co/chrisanc/cbase-models/resolve/main/qwen/model_quantized.onnx",
            "https://huggingface.co/chrisanc/cbase-models/resolve/main/qwen/model_quantized.onnx_data",
            "https://huggingface.co/chrisanc/cbase-models/resolve/main/qwen/tokenizer.json",
            "https://huggingface.co/chrisanc/cbase-models/resolve/main/qwen/tokenizer_config.json",
            "https://huggingface.co/chrisanc/cbase-models/resolve/main/qwen/vocab.json"
    }),
    MINILM(new String[]{
            "https://huggingface.co/chrisanc/cbase-models/resolve/main/minilm/config.json",
            "https://huggingface.co/chrisanc/cbase-models/resolve/main/minilm/model.onnx",
            "https://huggingface.co/chrisanc/cbase-models/resolve/main/minilm/tokenizer.json",
            "https://huggingface.co/chrisanc/cbase-models/resolve/main/minilm/vocab.txt"
    });

    private final String[] values;

    ModelURL(String[] values) {
        this.values = values;
    }

    public void download(FileDownloader downloader) throws IOException {
        // Define the model directory path to save the files
        Path folderName = FilePath.LOCAL_CACHE.getValue("models", this.toString().toLowerCase());
        System.out.printf("About to download %d files for the %s model.\n", this.values.length, this.toString());

        for (String from : this.getValues()) {
            String fileName = from.substring(from.lastIndexOf('/') + 1);
            Path targetFile = Path.of(folderName.toString(), fileName);
            if (Files.exists(targetFile)) {
                System.out.printf("The file %s already exists at %s\n", fileName, folderName);
                continue;
            }
            // Create the directories path
            Files.createDirectories(folderName);
            // Create temp file to download
            Path filePath = Files.createTempFile(folderName, "", ".tmp");
            downloader.download(from, filePath.toString());

            Files.move(
                filePath,
                targetFile,
                StandardCopyOption.REPLACE_EXISTING,
                StandardCopyOption.ATOMIC_MOVE
            );
        }
    }

    public boolean isDownloaded() {
        Path folderName = FilePath.LOCAL_CACHE.getValue("models", this.toString().toLowerCase());
        for (String from : this.getValues()) {
            String fileName = from.substring(from.lastIndexOf('/') + 1);
            Path targetFile = Path.of(folderName.toString(), fileName);
            if (!Files.exists(targetFile)) {
                return false;
            }
        }
        return true;
    }

    public void verifyOrDownload(FileDownloader downloader) {
        if (!isDownloaded()) {
            System.out.printf("Model '%s' is missing or incomplete in local cache. Starting automatic download...\n", this.name());
            try {
                this.download(downloader);
                System.out.printf("Model '%s' downloaded successfully!\n", this.name());
            } catch (IOException e) {
                System.err.printf("Failed to download model '%s': %s\n", this.name(), e.getMessage());
            }
        }
    }

    private String[] getValues() {
        return this.values;
    }
}
