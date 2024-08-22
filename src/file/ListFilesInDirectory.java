package file;

import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class ListFilesInDirectory {

    /**
     * Returns a list of filenames in the specified directory.
     *
     * @param directoryPath the path of the directory to list files from
     * @return an ArrayList containing the names of the files in the directory
     */
    public static List<String> getFileNames(String directoryPath) {
        List<String> fileNames = new ArrayList<>();
        Path path = Paths.get(directoryPath);

        // Check if the path is a directory
        if (Files.isDirectory(path)) {
            try (DirectoryStream<Path> directoryStream = Files.newDirectoryStream(path)) {
                // Iterate through the directory entries
                for (Path entry : directoryStream) {
                    // Check if the entry is a regular file (not a subdirectory)
                    if (Files.isRegularFile(entry)) {
                        // Add the filename to the ArrayList
                        fileNames.add(entry.getFileName().toString());
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();  // Handle the exception as needed
            }
        } else {
            // System.out.println("The provided path is not a directory.");
        }

        return fileNames;
    }
}

