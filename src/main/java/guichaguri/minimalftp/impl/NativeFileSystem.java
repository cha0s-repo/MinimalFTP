package guichaguri.minimalftp.impl;

import guichaguri.minimalftp.Utils;
import guichaguri.minimalftp.api.IFileSystem;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Native File System
 * Allows the manipulation of any file inside a directory
 * @author Guilherme Chaguri
 */
public class NativeFileSystem implements IFileSystem<File> {

    private final File rootDir;

    /**
     * Creates a native file system
     * @param rootDir The root directory
     */
    public NativeFileSystem(File rootDir) {
        this.rootDir = rootDir;
    }

    @Override
    public File getRoot() {
        return rootDir;
    }

    @Override
    public String getPath(File file) {
        return rootDir.toURI().relativize(file.toURI()).getPath();
    }

    @Override
    public boolean exists(File file) {
        return file.exists();
    }

    @Override
    public boolean isDirectory(File file) {
        return file.isDirectory();
    }

    @Override
    public int getPermissions(File file) {
        int perms = 0;
        perms = Utils.setPermission(perms, Utils.CAT_OWNER + Utils.TYPE_READ, file.canRead());
        perms = Utils.setPermission(perms, Utils.CAT_OWNER + Utils.TYPE_WRITE, file.canWrite());
        perms = Utils.setPermission(perms, Utils.CAT_OWNER + Utils.TYPE_EXECUTE, file.canExecute());
        return perms;
    }

    @Override
    public long getSize(File file) {
        return file.length();
    }

    @Override
    public long getLastModified(File file) {
        return file.lastModified();
    }

    @Override
    public int getHardLinks(File file) {
        return file.isDirectory() ? 3 : 1;
    }

    @Override
    public String getName(File file) {
        return file.getName();
    }

    @Override
    public String getOwner(File file) {
        return "-";
    }

    @Override
    public String getGroup(File file) {
        return "-";
    }

    @Override
    public File getParent(File file) throws IOException {
        if(file.equals(rootDir)) {
            throw new FileNotFoundException("No permission to access this file");
        }

        return file.getParentFile();
    }

    @Override
    public File[] listFiles(File dir) throws IOException {
        if(!dir.isDirectory()) throw new IOException("Not a directory");

        return dir.listFiles();
    }

    @Override
    public File findFile(String path) throws IOException {
        File file = new File(rootDir, path);

        if(!isInside(rootDir, file)) {
            throw new FileNotFoundException("No permission to access this file");
        }

        return file;
    }

    @Override
    public File findFile(File cwd, String path) throws IOException {
        File file = new File(cwd, path);

        if(!isInside(rootDir, file)) {
            throw new FileNotFoundException("No permission to access this file");
        }

        return file;
    }

    @Override
    public InputStream readFile(File file) throws IOException {
        return new FileInputStream(file);
    }

    @Override
    public OutputStream writeFile(File file, boolean append) throws IOException {
        return new FileOutputStream(file, append);
    }

    @Override
    public void mkdirs(File file) throws IOException {
        if(!file.mkdirs()) throw new IOException("Couldn't create the directory");
    }

    @Override
    public void delete(File file) throws IOException {
        if(!file.delete()) throw new IOException("Couldn't delete the file");
    }

    @Override
    public void rename(File from, File to) throws IOException {
        if(!from.renameTo(to)) throw new IOException("Couldn't rename the file");
    }

    @Override
    public void chmod(File file, int perms) throws IOException {
        file.setReadable(Utils.hasPermission(perms, Utils.CAT_OWNER + Utils.TYPE_READ), true);
        file.setWritable(Utils.hasPermission(perms, Utils.CAT_OWNER + Utils.TYPE_WRITE), true);
        file.setExecutable(Utils.hasPermission(perms, Utils.CAT_OWNER + Utils.TYPE_EXECUTE), true);
    }

    private boolean isInside(File dir, File file) {
        if(file.equals(dir)) return true;

        try {
            return file.getCanonicalPath().startsWith(dir.getCanonicalPath() + File.separator);
        } catch(IOException ex) {
            return false;
        }
    }

}
