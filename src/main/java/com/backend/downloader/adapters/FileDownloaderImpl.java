package com.backend.downloader.adapters;

import com.backend.downloader.ports.FileDownloader;

import java.io.*;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpHeaders;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

/**
 * Implementation of {@link FileDownloader} utilizing Java 21 HTTP Client for streaming downloads
 * with real-time console progress output.
 */
public class FileDownloaderImpl implements FileDownloader {

    /**
     * Default constructor for FileDownloaderImpl.
     */
    public FileDownloaderImpl() {
    }

    /**
     * Downloads a file from remote URL over HTTP with progress indicator and saves to local target path.
     *
     * @param from remote source URL
     * @param targetPath local destination file path
     * @throws RuntimeException if an I/O error occurs during downloading or writing
     */
    @Override
    public void download(String from, String targetPath) {
        // Create the attributes for the HTTP request
        HttpClient client = this.getHttpClient();
        HttpRequest request = HttpRequest
                .newBuilder()
                .uri(URI.create(from))
                .GET()
                .build();
        HttpResponse<InputStream> response = this.createResponseObject(client, request);
        // Get the content length
        HttpHeaders headers = response.headers();
        double contentLength = Long.parseLong(headers.firstValue("content-length").orElse("-1"));
        // From bytes to megabytes
        contentLength = (contentLength / (1024 * 1024));
        double acumulator = 0;

        try (
                InputStream input = response.body();
                var output = new FileOutputStream(targetPath)
        ) {
            // Buffer to download from the server
            byte[] buffer = new byte[8192];
            System.out.printf("%s:\n", from);

            int read;
            while ((read = input.readNBytes(buffer, 0, buffer.length)) != 0) {
                acumulator += (double) read / (1024 * 1024);
                // Write a certain part of the buffer in the file
                output.write(buffer, 0, read);
                // Give feedback in MB
                System.out.printf(
                        "\rDownloaded %,.2fMB from %,.2fMB (%.2f%%)",
                        acumulator, contentLength, (acumulator * 100) / contentLength
                );
                System.out.flush();
            }
            System.out.printf(
                    "\rDownloaded %,.2fMB from %,.2fMB (%.2f%%)",
                    acumulator, contentLength, (acumulator * 100) / contentLength
            );
            System.out.println("\n");

        } catch (IOException e) {
            System.err.println("[ERROR] Error writing downloaded file contents: " + e.getMessage());
            throw new RuntimeException("Download failed", e);
        } finally {
            client.close();
        }
    }

    /**
     * Builds and configures an HTTP client instance following redirect policies.
     *
     * @return configured {@link HttpClient} instance
     */
    private HttpClient getHttpClient() {
        return HttpClient.newBuilder().followRedirects(HttpClient.Redirect.ALWAYS).build();
    }

    /**
     * Executes HTTP GET request and returns input stream response.
     *
     * @param client HTTP client instance
     * @param request HTTP GET request object
     * @return {@link HttpResponse} containing response input stream
     */
    private HttpResponse<InputStream> createResponseObject(HttpClient client, HttpRequest request) {
        try {
            return client.send(request, HttpResponse.BodyHandlers.ofInputStream());
        } catch (IOException | InterruptedException e) {
            if (e instanceof InterruptedException) {
                Thread.currentThread().interrupt();
            }
            throw new RuntimeException("HTTP request execution failed", e);
        }
    }
}
