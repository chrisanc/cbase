import com.backend.downloader.adapters.FileDownloaderImpl;
import com.backend.downloader.domain.ModelURL;
import com.backend.downloader.ports.FileDownloader;
import org.junit.jupiter.api.Test;

import java.io.IOException;

public class FileDownloaderTest {
    @Test
    public void testDownloader() throws IOException {
        FileDownloader downloader = new FileDownloaderImpl();
        ModelURL.MINILM.download(downloader);
//        ModelURL.QWEN.download(downloader);
    }
}
