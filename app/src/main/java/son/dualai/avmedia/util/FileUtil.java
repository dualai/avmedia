package son.dualai.avmedia.util;

import android.content.Context;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;


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

}
