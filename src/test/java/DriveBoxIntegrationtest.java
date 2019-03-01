import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;
import com.google.api.services.drive.model.FileList;
import org.junit.Assert;
import org.junit.Test;

import java.io.*;
import java.nio.file.FileSystems;
import java.security.GeneralSecurityException;
import java.util.Collections;
import java.util.List;

public class DriveBoxIntegrationtest {

    final String APPLICATION_NAME = "Google Drive API Java Quickstart";
    final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();
    final String TOKENS_DIRECTORY_PATH = "tokens";
    private final List<String> SCOPES = Collections.singletonList(DriveScopes.DRIVE);
    private String CREDENTIALS_FILE_PATH = "";

    @Test
    public void testUpload() throws GeneralSecurityException, IOException, InterruptedException {
        CREDENTIALS_FILE_PATH = "Credentials/credentials.json";


        final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
        Drive service = new Drive.Builder(HTTP_TRANSPORT, JSON_FACTORY, getCredentials(HTTP_TRANSPORT))
                .setApplicationName(APPLICATION_NAME)
                .build();
        DirectoryWatcherService dws = new DirectoryWatcherService(service, "SyncFolder");
        dws.startWatching(FileSystems.getDefault().newWatchService());
        Thread.sleep(5000);
        File fis = new File("SyncFolder/Test.file"+Math.random());
        FileOutputStream fos = new FileOutputStream(fis.getAbsoluteFile());
        fos.write("SOmething".getBytes());
        fos.flush();
        fos.close();
        if (fis.createNewFile()) {
            System.out.println("FIle exist");
        }

Thread.sleep(5000);
Assert.assertTrue(checkFile(service,fis));
fis.deleteOnExit();
    }

    private Credential getCredentials(final NetHttpTransport HTTP_TRANSPORT) throws IOException {
        // Load client secrets.
        GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(new FileInputStream(new java.io.File(CREDENTIALS_FILE_PATH))));

        // Build flow and trigger user authorization request.
        GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
                HTTP_TRANSPORT, JSON_FACTORY, clientSecrets, SCOPES)
                .setDataStoreFactory(new FileDataStoreFactory(new java.io.File(TOKENS_DIRECTORY_PATH)))
                .setAccessType("offline")
                .build();
        LocalServerReceiver receiver = new LocalServerReceiver.Builder().setPort(8888).build();
        return new AuthorizationCodeInstalledApp(flow, receiver).authorize("user");
    }


    private boolean checkFile(Drive service, File f) throws IOException {
        FileList result = service.files().list()
                .setPageSize(10)
                .setFields("nextPageToken, files(id, name)")
                .execute();
        List<com.google.api.services.drive.model.File> files = result.getFiles();
        if (files == null || files.isEmpty()) {
            System.out.println("No files found.");
            return false;
        } else {
            System.out.println("Files:");
            for (com.google.api.services.drive.model.File file : files) {
                System.out.printf("%s (%s)\n", file.getName(), file.getId());
                if(file.getName().equals(f.getName())){
                    return true;
                }
            }
            return false;
        }
    }
}