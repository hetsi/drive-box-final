import com.google.api.client.http.AbstractInputStreamContent;
import com.google.api.client.http.FileContent;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.internal.util.MockUtil;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.io.IOException;
import java.nio.file.*;

import static org.mockito.Matchers.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.verifyStatic;

@RunWith(PowerMockRunner.class)
@PrepareForTest(FileSystems.class)
public class DirectoryWatcherServiceTest {
    @Test
    public void testGetFileName() {

        com.google.api.services.drive.Drive sevice = mock(com.google.api.services.drive.Drive.class);
        DirectoryWatcherService dws = new DirectoryWatcherService(sevice, "Test");
        Assert.assertEquals(dws.getFileName("test.txt"), "Test/test.txt");
    }

    @Test
    public void testUploadFile() throws IOException {
        com.google.api.services.drive.Drive service = mock(com.google.api.services.drive.Drive.class);
        DirectoryWatcherService dws = new DirectoryWatcherService(service, ".");
        File mockFile = new File();
        mockFile.setId("id-10");
        /*when(sevice.files()).thenReturn(mock(Drive.Files.class));
        when(sevice.files().create(mockFile,
                new FileContent("text/plain", mock(java.io.File.class)))).thenReturn(mock(Drive.Files.Create.class));
        Drive.Files.Create cr = sevice.files().create(mockFile,
                new FileContent("text/plain", mock(java.io.File.class)));
        */
        Drive.Files.Create cr2 = mock(Drive.Files.Create.class);
        Drive.Files files = mock(Drive.Files.class);

        when(service.files()).thenReturn(files);
        //when(dr.files()).thenReturn(files);
        when(service.files().create(any(File.class), any(FileContent.class))).thenReturn(cr2);
        when(cr2.setFields(anyString())).thenReturn(cr2);
        when(cr2.execute()).thenReturn(mockFile);

        Assert.assertTrue(dws.uploadFile(new java.io.File(".")));
    }

    @Test(expected = InterruptedException.class)
    public void testStartWatching() throws IOException, InterruptedException {

        com.google.api.services.drive.Drive service = mock(com.google.api.services.drive.Drive.class);
        DirectoryWatcherService dws = new DirectoryWatcherService(service, ".");
       /* PowerMockito.mockStatic(FileSystems.class);
        FileSystem fs = mock(FileSystem.class);
        when(FileSystems.getDefault()).thenReturn(fs);
        when(fs.newWatchService()).thenThrow(new IOException("Test"));*/
        WatchService watch = mock(WatchService.class);
        Path p = mock(Path.class);
        when(p.register(watch,                 StandardWatchEventKinds.ENTRY_CREATE,
                StandardWatchEventKinds.ENTRY_DELETE,
                StandardWatchEventKinds.ENTRY_MODIFY)).thenReturn(mock(WatchKey.class));
        when(watch.take()).thenThrow(new InterruptedException("Test"));
        dws.startWatching(watch);
    }
}
