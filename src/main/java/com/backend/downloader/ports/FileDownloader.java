package com.backend.downloader.ports;

public interface FileDownloader {
    /**
     * Downloads files from external repositories (internet)
     * and put the files in targetPath
     * */
    void download(String from, String targetPath);
}
