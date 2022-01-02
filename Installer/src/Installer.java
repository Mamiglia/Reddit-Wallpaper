import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.*;
import java.util.Scanner;

public class Installer {
	public static final File from = Paths.get(".").toAbsolutePath().normalize().toFile();
	public static final String INSTALLATION_PATH = "C:\\ProgramData\\Reddit-Wallpaper";
	public static final String DB_DIRECTORY_PATH = INSTALLATION_PATH + "\\.utility";
	public static final String SETTINGS_PATH = DB_DIRECTORY_PATH + "\\settings.txt";
	private static Scanner scan;

	public static final String mainExe= "RW.exe";
	public static final String batch = "autostartRW.bat";
	public static final String resDir = "resources\\";

	public static void main(String[] args) {
		scan = new Scanner(System.in);
		System.out.println(
						"  _____          _     _ _ _    __          __   _ _                             \n" +
						" |  __ \\        | |   | (_| |   \\ \\        / /  | | |                            \n" +
						" | |__) |___  __| | __| |_| |_   \\ \\  /\\  / __ _| | |_ __   __ _ _ __   ___ _ __ \n" +
						" |  _  // _ \\/ _` |/ _` | | __|   \\ \\/  \\/ / _` | | | '_ \\ / _` | '_ \\ / _ | '__|\n" +
						" | | \\ |  __| (_| | (_| | | |_     \\  /\\  | (_| | | | |_) | (_| | |_) |  __| |   \n" +
						" |_|  \\_\\___|\\__,_|\\__,_|_|\\__|     \\/  \\/ \\__,_|_|_| .__/ \\__,_| .__/ \\___|_|   \n" +
						"                                                    | |         | |              \n" +
						"                                                    |_|         |_|      "		);
		System.out.println(
				"Welcome to the command line installation utility of Reddit-Wallpaper.\n\n" +
				"Please select what you would like to do:\n" +
					"\t[1] - Install\n" +
					"\t[2] - Update\n" +
					"\t[3] - Uninstall\n" +
					"\t[0] - Cancel");

		while (scan.hasNext()) {
			switch (scan.nextLine()) {
				case "1":
					//scan.close();
					install();
					break;
				case "2":
					//scan.close();
					update();
					break;
				case "3":
					//scan.close();
					uninstall();
					break;
				case "0":
					//scan.close();
					abort();
					break;
				default:
					System.out.println("Input not recognized, please select a number from 0 to 3");
			}
		}

		scan.close();

		System.exit(0);
	}

	static boolean existFiles(String... strings) {
		boolean res = true;
		for (String s : strings) {
			res = res && existFiles(new File(s));
		}
		return res;
	}
	static boolean existFiles(File... files) {
		for (File f : files) {
			if (!f.exists()) return false;
		}
		return true;
	}

	static void install() {
		//moves files in the same directory to C:\Program Files\nameApplication
		print("Beginning Installation process");
		File exe = new File(mainExe);
		File bat = createAutostartFile();
		File resources = new File(resDir);
		if (!exe.exists()) {
			print(exe + " was not found, can't complete installation process");
			abort();
		}
		if (!bat.exists()) {
			print(bat + " was not found, can't complete installation process");
			abort();
		}
		if (!resources.exists()) {
			print(resources + " was not found, can't complete installation process");
			abort();
		}
		long filesSize = 0;
		try {
			filesSize = Files.size(exe.toPath()) + Files.size(resources.toPath()) + Files.size(bat.toPath());
		} catch (IOException e) {
			e.printStackTrace();
			abort();
		}
		print(filesSize/1024/1024 + "MB are being installed, are you sure you want to continue?\n" +
				"\t[1] - Yes \n" +
				"\t[2] - No");
		scan = new Scanner(System.in);
		var b = true;
		while (b) {
			switch (scan.nextLine()) {
				case "1":
					b=false;
					//scan.close();
					print("Starting...");
					break;
				case "2":
					//scan.close();
					main(new String[0]);
					System.exit(0);

				default:
					print("Input not recognized, please select a number from either 1 or 2");
			}
		}
		print("Current Directory: " + from);

		exe = move(mainExe, INSTALLATION_PATH);
		bat = move(batch, getStartupFolder());
		for (File f : new File(resDir).listFiles()) {
			move(f.toString(), INSTALLATION_PATH + File.separator + resDir);
		}

		File oldDbFile = new File(DB_DIRECTORY_PATH + "\\wallpaperDB.txt");
		File dbFile = new File(DB_DIRECTORY_PATH + "\\db.mv.db");
		if (oldDbFile.exists()) oldDbFile.delete();
		if (dbFile.exists()) dbFile.delete();

		if (existFiles(exe, bat, resources)) {
			print("\nInstallation Completed Successfully\nYou can delete these files and folders\n\nPress enter to start the program");
			pause();
			try {
				Process p = Runtime.getRuntime().exec(INSTALLATION_PATH+File.separator+mainExe);
			} catch (IOException e) {
				e.printStackTrace();
			}
		} else {
			print("\nInstallation failed");
			pause();
		}
	}

	static void update() {
		File exe = new File(mainExe);
		File bat = createAutostartFile();
		File resources = new File(resDir);
		if (!existFiles(
				INSTALLATION_PATH,
				getStartupFolder() + File.separator + batch
				))
		{
			print("No old installation was found, do you want to Install this program?\n" +
					"\t[1] - Yes\n" +
					"\t[2] - No");
			// scan = new Scanner(System.in);
			var b = true;
			while (b) {
				switch (scan.nextLine()) {
					case "1":
						b = false;
						//scan.close();
						install();
						break;
					case "2":
						b = false;
						//scan.close();
						main(new String[0]);
						break;
					default:
						print("Input not recognized, please select a number from either 1 or 2");
				}
			}
			System.exit(0);
		}

		print("Old installation was found, do you want to update this program?\n" +
				"\t[1] - Yes\n" +
				"\t[2] - No");
		//Scanner scan = new Scanner(System.in);
		var b = true;
		while (b) {
			switch (scan.nextLine()) {
				case "1":
					b = false;
					//scan.close();
					break;
				case "2":
					b = false;
					//scan.close();
					main(new String[0]);
					System.exit(0);

				default:
					print("Input not recognized, please select a number from either 0 or 1");
			}
		}
		print("Current Directory: " + from);

		print("Please, now close RW if it's running. Then press enter");
		pause();

		exe = move(mainExe, INSTALLATION_PATH);
		bat = move(batch, getStartupFolder());
		for (File f : new File(resDir).listFiles()) {
			move(f.toString(), INSTALLATION_PATH + File.separator + resDir);
		}
		File oldJarFile = new File(INSTALLATION_PATH + "\\Reddit-Wallpaper.jar");
		File oldDbFile = new File(DB_DIRECTORY_PATH + "\\wallpaperDB.txt");
		File dbFile = new File(DB_DIRECTORY_PATH + "\\db.mv.db");

		try {
			Files.deleteIfExists(oldDbFile.toPath());
			Files.deleteIfExists(dbFile.toPath());
			Files.deleteIfExists(oldJarFile.toPath());
		} catch (IOException e) {
			print("Close RW first! Then press enter");
			print("In the tray menu (bottom right of your application bar) click the arrow, right click on the RW icon and then click \"close\"");
			pause();
		}


		if (existFiles(exe, bat, resources)) {
			print("\nInstallation Completed Successfully\nYou can delete these files and folders\n\nPress enter to start the program");
			pause();
			try {
				Process p = Runtime.getRuntime().exec(INSTALLATION_PATH+File.separator+mainExe);
			} catch (IOException e) {
				e.printStackTrace();
			}
		} else {
			print("\nInstallation failed");
			pause();
		}

	}

	static void uninstall() {
		File dir = new File(INSTALLATION_PATH);
		File bat = new File(getStartupFolder() + File.separator + batch);

		if (!dir.exists() && !bat.exists()) {
			print("Nothing to uninstall was found");
			main(new String[0]);
		}

		print("RW is being uninstalled, are you sure you want to continue?\n" +
				"\t[1] yes \n" +
				"\t[2] no");
		//scan = new Scanner(System.in);
		var b = true;
		while (b) {
			switch (scan.nextLine()) {
				case "1":
					b = false;
					//scan.close();
					print("Uninstalling...");
					break;
				case "2":
					b = false;
					//scan.close();
					main(new String[0]);
					System.exit(0);
				default:
					print("Input not recognized, please select a number from either 1 or 2");
			}
		}
		print("Please, now close RW if it's running. Then press enter");
		pause();

		while (true) {
			try {
				deleteFolder(dir);
				Files.deleteIfExists(bat.toPath());
				break;
			} catch (IOException e) {
				e.printStackTrace();
				print("Close RW first! Then press enter");
				print("In the tray menu (bottom right of your application bar) click the arrow, right click on the RW icon and then click \"close\"");
				pause();
			}
		}

		print("RW was successfully uninstalled");
		pause();
	}

	static void abort() {
		print("Aborting...\nPress enter");
		pause();
		System.exit(1);
	}

	static File move(String name, String destination) {
		File f = new File(from +File.separator+ name);
		File to = new File(destination);

		return move(f, to);
	}

	static File move(File f, File to) {
		if (!f.exists()) {
			System.err.println(f.getName() + " not found in current directory: " + f.getPath());
			return to;
		}
		System.out.print("Transferring this file: " + f);
		System.out.println(" to this location: "+ to.toString());

		to.mkdirs();
		to.mkdir();
		File dest = new File(to + File.separator + f.getName());
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

	public static void deleteFolder(File folder) throws IOException {
		if (folder == null) return;
		if (folder.listFiles() == null || folder.listFiles().length == 0) {
			Files.deleteIfExists(folder.toPath());
			return;
		}
		for (File f: folder.listFiles()) {
			if (f.isDirectory()) {
				deleteFolder(f);
			} else {
				print("Deleted " + f.toPath());
				Files.delete(f.toPath());
			}
		}
		Files.delete(folder.toPath());

	}

	public static File createAutostartFile() {
		File f = new File(from + "/" + batch);
		String code =
				"@echo off\n" +
						"cd \""+ INSTALLATION_PATH + "\"\n" +
						"start \"\" \"" + mainExe + "\"\n" +
						"exit";
		try (BufferedWriter fw = new BufferedWriter(new FileWriter(f))) {
			f.createNewFile();
			fw.write(code);
			fw.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return f;
	}

	public static void pause() {
		Scanner input = new Scanner(System.in);
		if (input.hasNextLine()) {
			input.nextLine();
			input.close();
		}

	}

	public static void print(String str) {
		System.out.println(str);
	}
}
