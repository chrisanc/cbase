package com.backend.downloader.ports;

/**
 * Port interface for downloading model files and remote resources from external URLs.
 */
public interface FileDownloader {
    /**
     * Downloads a file from a remote source URL and saves it to the specified local filesystem target path.
     *
     * @param from remote source URL to download from
     * @param targetPath local filesystem destination path for the downloaded file
     */
    void download(String from, String targetPath);
}
