package Peer;

import java.io.File;
import java.util.LinkedList;
import java.util.List;

/**
 * Monitor the local shared directory
 * Keep updating the local regisDic in Server class
 *
 * Created by xuzhuchen on 11/1/17.
 */
public class DirMonitor extends Thread{
    private Thread t;
    private String threadName;
    private String sharedDir;
    private long timePeriod;

    DirMonitor(String sharedDir, long timePeriod){
        this.sharedDir = sharedDir;
        this.timePeriod = timePeriod;
    }

    /*
     * Method to read all the file in the shared directory,
     * get file name of all the files, put in a list and return
     */
    List<String> getLocalFileList() {
        List<String> fileURIList = new LinkedList<>();
        if (sharedDir == null || sharedDir.isEmpty()) return fileURIList;

        try {
            File f = new File(sharedDir);
            File[] files = f.listFiles(); // 得到f文件夹下面的所有文件。

            for (File file : files) {
                fileURIList.add(file.getName());
//                System.out.println(file.getName());
            }
        } catch (NullPointerException e) {
            System.out.println("File Path not found!");
        }

        return fileURIList;
    }

    @Override
    public void run() {
        while(true){
            try {
                sleep(timePeriod);
                getLocalFileList();
            }catch (InterruptedException e){
                e.printStackTrace();
            }
        }
    }

    @Override
    public void start() {

    }
}
