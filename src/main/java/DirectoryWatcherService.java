import com.google.api.client.http.FileContent;
import com.google.api.services.drive.Drive;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;

public class DirectoryWatcherService {
    Drive service;
    String PATH_TO_WATCH ;
    DirectoryWatcherService(Drive service, String path){
        this.service = service;
        PATH_TO_WATCH = path;
    }

    void startWatching(WatchService watchService) throws InterruptedException, IOException {

        Runnable runnable = () -> {

            Path path = Paths.get(PATH_TO_WATCH);

            try {
                path.register(
                        watchService,
                        StandardWatchEventKinds.ENTRY_CREATE,
                        StandardWatchEventKinds.ENTRY_DELETE,
                        StandardWatchEventKinds.ENTRY_MODIFY);


            WatchKey key;
            System.out.println("Started Watching: " + Paths.get(PATH_TO_WATCH).toAbsolutePath());
            while ((key = watchService.take()) != null) {
                for (WatchEvent<?> event : key.pollEvents()) {
                    System.out.println("[DEBUG] Event kind:" + event.kind() + ". File affected: " + event.context() + ".");
                    uploadFile(new File(getFileName(event.context().toString())));
                }
                key.reset();
            }
            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
            }
        };

        Thread t = new Thread(runnable);
        t.start();
 }

    String getFileName(String filename){

        return PATH_TO_WATCH+"/"+filename;
    }

     boolean uploadFile(File f)  {
        try {
            String MimeType = Files.probeContentType(f.toPath());
            com.google.api.services.drive.model.File file = service.files().create(new com.google.api.services.drive.model.File().setName(f.getName()), new FileContent(MimeType, f))
                    .setFields("id")
                    .execute();
            System.out.println("Successfully Uploaded File: " + f.getAbsolutePath() + " with File ID: " + file.getId());
        } catch (IOException ex){
            return false;
        }
        return true;
    }
}
