import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

public class Installer {
    public static final File from = Paths.get(".").toAbsolutePath().normalize().toFile();
    public static final String INSTALLATION_PATH = "C:/ProgramData/Reddit Wallpaper";
    public static final String mainJar = "Reddit-Wallpaper.jar";
    public static final String batch = "autostartRW.bat";
    public static final String resDir = ".resources";

    public static void main(String[] args) {
        //moves files in the same directory to C:\Program Files\nameApplication
        System.out.println("Current Directory: " + from.toString());

        createAutostartFile();

        File jarFile = move(mainJar, INSTALLATION_PATH);
        File batchFile = move(batch, getStartupFolder());
        File resources = move(resDir, INSTALLATION_PATH);



//        String[] s = from.list();
//        for (String n : s) {
//            System.out.println(n);
//        }

        if (jarFile.exists() && batchFile.exists() && resources.exists()) {
            System.out.println("Installation Completed Successfully");
        } else {
            System.out.println("Installation failed");
            //TODO delete junk files
        }
    }

    static File move(String name, String destination) {
        File to = new File(destination + "/" + name);
        File f = new File(from.toString() +"/"+ name);
        if (!f.exists()) {
            System.err.println(name + " not found in current directory");
            return to;
        }
        //System.out.println(f.toString());
//        System.out.println(to.toString());
        try {
            to.getParentFile().mkdirs();
            to.mkdir();
            Files.move(f.toPath(), to.toPath(), StandardCopyOption.REPLACE_EXISTING);
        }
        catch (IOException ex) {
            ex.printStackTrace();
        }
        return to;
    }

    public static String getStartupFolder() {
        return System.getProperty("java.io.tmpdir").replace("Local\\Temp\\", "Roaming\\Microsoft\\Windows\\Start Menu\\Programs\\Startup");
    }

    public static void createAutostartFile() {
        File f = new File(from.toString() + "/" + batch);
        String code =
                "@echo off\n" +
                "cd \"" + INSTALLATION_PATH + "\"\n" +
                "start javaw -Xmx200m -jar \"" + mainJar + "\"\n" +
                "exit";
        try (BufferedWriter fw = new BufferedWriter(new FileWriter(f))) {
            f.createNewFile();
            fw.write(code);
            fw.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
