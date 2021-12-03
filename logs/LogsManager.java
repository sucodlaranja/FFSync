import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Esta classe representa os logs de uma determinada pasta
 * e todas as operações que podemos fazer sobre eles.
 * */
public class LogsManager {

    private final String filepath; // Lugar onde está a pasta
    private Map<String, FileTime> logs; // contém um map com os nomes dos ficheiros com os seus timestamps

    public LogsManager(String filepath) throws IOException {
        this.filepath = filepath;
        logs = new HashMap<>();
        insertFileLogs(Paths.get(filepath),"");
        }

    public Map<String, FileTime> getLogs(){
        return new HashMap<>(logs);
    }

    private boolean insertFileLogs(Path folder, String prePath) throws IOException {
        boolean update = false;
        for(Path file: Files.list(folder).collect(Collectors.toList())) {
            if (Files.isDirectory(file)) insertFileLogs(file,prePath + file.getFileName().toString() + "/");
            else {
                BasicFileAttributes attr = Files.readAttributes(file, BasicFileAttributes.class);
                String filename = prePath + file.getFileName().toString();
                FileTime fl = attr.lastModifiedTime();
                if (!logs.containsKey(filename)) logs.put(filename, attr.lastModifiedTime());
                if (fl.compareTo(logs.get(filename)) != 0) {
                    logs.put(filename, attr.lastModifiedTime());
                    update = true;
                }
            }
        }
        return update;
    }

    public boolean updateFileLogs() throws IOException {
        return this.insertFileLogs(Paths.get(filepath),"");
    }

    public Queue<TransferLogs> compareLogs(Map<String, FileTime> otherLogs){
        Queue<TransferLogs> listOfTransfers = new LinkedList<>();

        for(Map.Entry<String, FileTime> file:  this.logs.entrySet()){
            if (otherLogs.containsKey(file.getKey())){
                int comp = otherLogs.remove(file.getKey()).compareTo(file.getValue());

                if (comp > 0) listOfTransfers.add(new TransferLogs(file.getKey(), true));
                else if (comp < 0) listOfTransfers.add(new TransferLogs(file.getKey(), false));
            }
            else listOfTransfers.add(new TransferLogs(file.getKey(), false));
        }
        for(String fileName:  otherLogs.keySet()) listOfTransfers.add(new TransferLogs(fileName, true));

        return listOfTransfers;
    }



    // TODO DEBUG REMOVE
    public void printTransfers(Queue<TransferLogs> listOfTransfers){
        for (TransferLogs t: listOfTransfers){
            System.out.println( t.getFileName() + " " + t.isSenderOrReceiver());
        }
    }

    public void printLogs(){
        logs.forEach((key, value) -> System.out.println(key + " -> " + value.toString()));
    }
}
