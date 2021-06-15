import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.*;
import java.util.Scanner;

public class Installer {
    public static final File from = Paths.get(".").toAbsolutePath().normalize().toFile();
    public static final String INSTALLATION_PATH = "C:\\ProgramData\\Reddit Wallpaper";
    public static final String mainJar = "Reddit-Wallpaper.jar";
    public static final String batch = "autostartRW.bat";
    public static final String resDir = ".resources";

    public static void main(String[] args) {
        //moves files in the same directory to C:\Program Files\nameApplication
        System.out.println("Current Directory: " + from.toString());

        createAutostartFile();

//        OLD CODE, it detected if an old installation of RW was present, I don't think it's very useful
//        File dest = new File(INSTALLATION_PATH);
//        if (dest.exists()) {
//            System.out.print ("Detected an old installation, would you like to update it? [Y/N]  ");
//            Scanner scan = new Scanner(System.in);
//            if (scan.next().equalsIgnoreCase("n")) {
//                System.out.println("Aborting upgrade..");
//                return;
//            } else {
//                deleteFolder(dest);
//                File f = new File(getStartupFolder() + File.separator + batch);
//                f.delete();
//            }
//
//        }

        File jarFile = move(mainJar, INSTALLATION_PATH);
        File batchFile = move(batch, getStartupFolder());
        for (File f : new File(resDir).listFiles()) {
            move(f.toString(), INSTALLATION_PATH + File.separator + resDir);
        }


        if (jarFile.exists() && batchFile.exists()) {
            System.out.println("\nInstallation Completed Successfully\nYou can delete these files and folders");
        } else {
            System.out.println("\nInstallation failed");
        }
        //TODO add option to delete files

    }

    static File move(String name, String destination) {
        File f = new File(from.toString() +File.separator+ name);
        File to = new File(destination);

        return move(f, to);
    }

    static File move(File f, File to) {
        if (!f.exists()) {
            System.err.println(f.getName() + " not found in current directory: " + f.getPath());
            return to;
        }
        System.out.print("Transferring this file: " + f.toString());
        System.out.println(" to this location: "+ to.toString());

        to.mkdirs();
        to.mkdir();
        File dest = new File(to.toString() + File.separator + f.getName());
        try {
            Files.copy(f.toPath(), dest.toPath(), StandardCopyOption.REPLACE_EXISTING);
        } catch (FileSystemException e) {
            System.out.println("\n!! - RW is still running. Please close it first then press a button\n");
            pause();
            move(f, to);
        } catch (IOException e) {

            e.printStackTrace();
        }

        return to;
    }

    public static String getStartupFolder() {
        return System.getProperty("java.io.tmpdir").replace("Local\\Temp\\", "Roaming\\Microsoft\\Windows\\Start Menu\\Programs\\Startup");
    }

    public static void deleteFolder(File folder) {
        if (folder == null) return;
        if (folder.listFiles() == null || folder.listFiles().length == 0) {
            folder.delete();
            return;
        }
        for (File f: folder.listFiles()) {
            if (f.isDirectory()) {
                deleteFolder(f);
            } else {
                f.delete();
            }
        }
        folder.delete();

    }

    public static void createAutostartFile() {
        File f = new File(from.toString() + "/" + batch);
        String code =
                "@echo off\n" +
                "cd \"" + INSTALLATION_PATH + "\"\n" +
                "start javaw -jar \"" + mainJar + "\"\n" +
                "exit";
        try (BufferedWriter fw = new BufferedWriter(new FileWriter(f))) {
            f.createNewFile();
            fw.write(code);
            fw.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void pause() {
        Scanner input = new Scanner(System.in);
        String cont = input.nextLine();
        while(cont.equals(" ")) {

            cont = input.nextLine();

        }

    }
}
