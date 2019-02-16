package son.dualai.avmedia.util;

import android.content.Context;
import android.os.Environment;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;


public class FileUtil {

    private static final int BUFFER = 2048;

    public static boolean isEmptyDir(String path){
        if(isExist(path)){
            File file = new File(path);
            if(file.canRead()){
                int size = file.listFiles().length;
                if(size > 0){
                    return false;
                }
            }
        }
        return true;
    }

    /**
     *
     * @param filePath
     * @param newName
     * @return
     */
    public static boolean renameTarget(String filePath, String newName) {
        File src = new File(filePath);
        String ext = "";
        File dest;

        if(src.isFile())
			/*get file extension*/
            ext = filePath.substring(filePath.lastIndexOf("."), filePath.length());

        if(newName.length() < 1)
            return false;

        String temp = filePath.substring(0, filePath.lastIndexOf("/"));

        dest = new File(temp + "/" + newName + ext);
        if(src.renameTo(dest))
            return true;
        else
            return false;
    }


    public static boolean isExist(String path) {
        File target = new File(path);
        return target.exists();
    }

    /**
     * @param old    the file to be copied
     * @param newDir the directory to move the file to
     * @return
     */
    public static boolean copyToDirectory(String old, String newDir) {
        File old_file = new File(old);
        File temp_dir = new File(newDir);
        byte[] data = new byte[BUFFER];
        int read = 0;

        if (old_file.isFile() && temp_dir.isDirectory() && temp_dir.canWrite()) {
            String file_name = old.substring(old.lastIndexOf("/"), old.length());
            File cp_file = new File(newDir + file_name);

            try {
                BufferedOutputStream o_stream = new BufferedOutputStream(
                        new FileOutputStream(cp_file));
                BufferedInputStream i_stream = new BufferedInputStream(
                        new FileInputStream(old_file));

                while ((read = i_stream.read(data, 0, BUFFER)) != -1)
                    o_stream.write(data, 0, read);

                o_stream.flush();
                i_stream.close();
                o_stream.close();

                cp_file.setReadable(true, false);
                cp_file.setWritable(true, false);

            } catch (FileNotFoundException e) {
                Log.e("FileNotFoundException", e.getMessage());
                return false;

            } catch (IOException e) {
                Log.e("IOException", e.getMessage());
                return false;
            }

        } else if (old_file.isDirectory() && temp_dir.isDirectory() && temp_dir.canWrite()) {
            String files[] = old_file.list();
            String dir = newDir + old.substring(old.lastIndexOf("/"), old.length());
            int len = files.length;

            File newFile = new File(dir);
            if (newFile.mkdir()) {
                newFile.setReadable(true, false);
                newFile.setWritable(true, false);
            } else {
                return false;
            }

            for (int i = 0; i < len; i++)
                copyToDirectory(old + "/" + files[i], dir);

        } else if (!temp_dir.canWrite())
            return false;

        return true;
    }

    /**
     * @param path
     * @param name
     * @return
     */
    public static boolean createDir(String path, String name) {
        int len = path.length();

        if (len < 1 || len < 1)
            return false;

        if (path.charAt(len - 1) != '/')
            path += "/";

        File dir = new File(path + name);
        if (dir.mkdir()) {
            dir.setReadable(true, false);
            dir.setWritable(true, false);
            return true;
        }

        return false;
    }

    /**
     * The full path name of the file to delete.
     *
     * @param path name
     * @return
     */
    public static boolean deleteTarget(String path) {
        File target = new File(path);

        target.setReadable(true, false);
        target.setWritable(true, false);

        if (target.exists() && target.isFile() && target.canWrite()) {
            target.delete();
            return true;
        } else if (target.exists() && target.isDirectory() && target.canRead()) {
            String[] file_list = target.list();

            if (file_list != null && file_list.length == 0) {
                target.delete();
                return true;

            } else if (file_list != null && file_list.length > 0) {

                for (int i = 0; i < file_list.length; i++) {
                    File temp_f = new File(target.getAbsolutePath() + "/" + file_list[i]);

                    if (temp_f.isDirectory())
                        deleteTarget(temp_f.getAbsolutePath());
                    else if (temp_f.isFile())
                        temp_f.delete();
                }
            }
            if (target.exists())
                if (target.delete())
                    return true;
        }
        return false;
    }



    ////////third
    private static final String MAIN_DIR_NAME = "/android_records";
    private static final String BASE_VIDEO = "/video/";
    private static final String BASE_EXT = ".mp4";

    private SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy_MM_dd_HH_mm");
    private String currentFileName = "-";
    private String nextFileName;

    public boolean requestSwapFile() {
        return requestSwapFile(false);
    }

    public boolean requestSwapFile(boolean force) {
        //SD 卡可读写
        String fileName = getFileName();
        boolean isChanged = false;

        if (!currentFileName.equalsIgnoreCase(fileName)) {
            isChanged = true;
        }

        if (isChanged || force) {
            nextFileName = getSaveFilePath(fileName);
            return true;
        }

        return false;
    }

    public String getNextFileName() {
        return nextFileName;
    }

    private String getFileName() {
        String format = simpleDateFormat.format(System.currentTimeMillis());
        return format;
    }

    private String getSaveFilePath(String fileName) {
        currentFileName = fileName;
        StringBuilder fullPath = new StringBuilder();
        fullPath.append(getExternalStorageDirectory());
        //检查内置卡剩余空间容量,并清理
        checkSpace();
        fullPath.append(MAIN_DIR_NAME);
        fullPath.append(BASE_VIDEO);
        fullPath.append(fileName);
        fullPath.append(BASE_EXT);

        String string = fullPath.toString();
        File file = new File(string);
        File parentFile = file.getParentFile();
        if (!parentFile.exists()) {
            parentFile.mkdirs();
        }

        return string;
    }

    /**
     * 检查剩余空间
     */
    private void checkSpace() {
        StringBuilder fullPath = new StringBuilder();
        String checkPath = getExternalStorageDirectory();
        fullPath.append(checkPath);
        fullPath.append(MAIN_DIR_NAME);
        fullPath.append(BASE_VIDEO);

        if (checkCardSpace(checkPath)) {
            File file = new File(fullPath.toString());

            if (!file.exists()) {
                file.mkdirs();
            }

            String[] fileNames = file.list();
            if (fileNames.length < 1) {
                return;
            }

            List<String> fileNameLists = Arrays.asList(fileNames);
            Collections.sort(fileNameLists);

            for (int i = 0; i < fileNameLists.size() && checkCardSpace(checkPath); i++) {
                //清理视频
                String removeFileName = fileNameLists.get(i);
                File removeFile = new File(file, removeFileName);
                try {
                    removeFile.delete();
                    android.util.Log.e("angcyo-->", "删除文件 " + removeFile.getAbsolutePath());
                } catch (Exception e) {
                    e.printStackTrace();
                    android.util.Log.e("angcyo-->", "删除文件失败 " + removeFile.getAbsolutePath());
                }
            }
        }
    }

    private boolean checkCardSpace(String filePath) {
        File dir = new File(filePath);
        double totalSpace = dir.getTotalSpace();//总大小
        double freeSpace = dir.getFreeSpace();//剩余大小
        if (freeSpace < totalSpace * 0.2) {
            return true;
        }
        return false;
    }

    /**
     * 获取sdcard路径
     */
    public static String getExternalStorageDirectory() {
        return Environment.getExternalStorageDirectory().getPath();
    }


}
