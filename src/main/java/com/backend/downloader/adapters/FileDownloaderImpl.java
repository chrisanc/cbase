package com.backend.downloader.adapters;

import com.backend.downloader.ports.FileDownloader;

import java.io.*;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpHeaders;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

/**
 * Logics for the file downloading of internet files.
 * Used for the model local downloads using commands.
 * */
public class FileDownloaderImpl implements FileDownloader {
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
            System.err.println("Error writing the changes...");
            System.exit(1);
        } finally {
            client.close();
        }
    }

    private HttpClient getHttpClient() {
        return HttpClient.newBuilder().followRedirects(HttpClient.Redirect.ALWAYS).build();
    }

    private HttpResponse<InputStream> createResponseObject(HttpClient client, HttpRequest request) {
        HttpResponse<InputStream> response;
        try {
            response = client.send(request, HttpResponse.BodyHandlers.ofInputStream());
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        return response;
    }
}
