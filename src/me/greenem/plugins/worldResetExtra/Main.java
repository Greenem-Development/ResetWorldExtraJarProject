package me.greenem.plugins.worldResetExtra;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.Channel;
import java.nio.file.CopyOption;
import java.nio.file.FileSystemException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicReference;

import org.yaml.snakeyaml.Yaml;

public class Main {
	public static File serverDirectory;
	public static File configsDirectory;
	public static File mainConfigDirectory;
	public static File world;
	public static File worldNether;
	public static File worldTheEnd;
	public static String worldMainName = "world";
	
	// CURRENTLY WORKS ONLY ON WINDOWS (and difficult for me to test in other os)
	
	/*public static void killer(ProcessHandle ph) {
		ph.children().forEach(ph2-> {
			killer(ph2);
		});
		//System.out.println("Stage " + stage + ", " + ph.toString());
		ph.destroyForcibly();
	}*/
	//killer(ps.get().toHandle());
	
	public static String launchServerFileName = "Run.bat";
	public static boolean usedHalt = false;
	
	public static boolean deleteDirectory(File directoryToBeDeleted) {
	    File[] allContents = directoryToBeDeleted.listFiles();
	    if (allContents != null) {
	        for (File file : allContents) {
	            deleteDirectory(file);
	        }
	    }
	    return directoryToBeDeleted.delete();
	}
	
	public static void main(String[] args) {
		try {
			Thread.currentThread().sleep(500);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		System.out.println("This is extra java programm required to reset the server worlds");
		System.out.println("If it does show up (sometimes it doesn't), then everything is working fine");
		
		File exelutionDir = new File(System.getProperty("user.dir"));
		if(exelutionDir.getAbsolutePath().equals("C:\\Windows\\System32")) {
			System.out.println("You cannot start this application in your System32 directory!");
			return;
		}
		
		serverDirectory = exelutionDir.getParentFile().getParentFile();
//		serverDirectory = new File("G:\\SERVERS\\Dev Server 1.18");
		
		if(!loadConfig()) {
			System.out.println("Cannot find the config!");
			return;
		}
		if(!loadWorldName()) return;
		if(!checkConditions()) return;
		waitForTheServerToStopAndDeleteWorlds();
	}
	
	private static boolean loadWorldName() {
		File f = new File(serverDirectory, "server.properties");
		Properties pr = new Properties();
		FileInputStream is = null;
		
		try {
			is = new FileInputStream(f);
		} catch (FileNotFoundException e1) {
			System.out.println("\"server.properties\" file is missing");
			return false;
		}
		try {
			pr.load(is);
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				worldMainName = pr.getProperty("level-name");
				is.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return true;
	}
	
	private static boolean checkConditions() {
		System.out.println("Searching for server files in \"" + serverDirectory.getPath() + "\"");
		
		world = new File(serverDirectory, worldMainName);
		worldNether = new File(serverDirectory, worldMainName + "_nether");
		worldTheEnd = new File(serverDirectory, worldMainName + "_the_end");
		
		if(!world.exists() && !worldNether.exists() && !worldTheEnd.exists()) {
			System.out.println("THE CODE WAS EXECUTED IN WORNG LOCATION OR THE WORLDS ARE ALREADY DELETED!");
			return false;
		}
		if(!new File(serverDirectory, launchServerFileName).exists()) {
			System.out.println("The server launch script is missing");
			return false;
		}
		/*if(!new File(serverDirectory, "server.properties").exists()) {
			System.out.println("\"server.properties\" file is missing");
			return false;
		}*/
		return true;
	}
	
	private static boolean isServerOn() {
		File latestLog = new File(serverDirectory, "logs/latest.log");
		File inSubfolderForLatestLog = new File(latestLog.getParentFile(), "testfolder/latest.log");
		inSubfolderForLatestLog.getParentFile().mkdirs();
		boolean result = false;
		try {
			Files.move(latestLog.toPath(), inSubfolderForLatestLog.toPath(), StandardCopyOption.REPLACE_EXISTING);
			result = false;
			Files.move(inSubfolderForLatestLog.toPath(), latestLog.toPath(), StandardCopyOption.REPLACE_EXISTING);
		} catch (FileSystemException e) {
//			System.out.println(e.getMessage());
			result = true;
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			deleteDirectory(inSubfolderForLatestLog.getParentFile());
		}
		return result;
	}
	
	private static void waitForTheServerToStopAndDeleteWorlds() {
		//File f = new File(new File(System.getProperty("user.dir")).getParentFile().getParentFile(), "world/session.lock");
//		File f = new File(serverDirectory, worldMainName + "/session.lock");
//		boolean serverStopped = (!f.exists()) || f.delete();
		boolean serverStopped = false;
		int failedAtts = 0;
		
		int maxFailedAttempts = 10;
		if(!usedHalt) maxFailedAttempts = 60;
		
		while(!serverStopped && failedAtts < maxFailedAttempts) {
			try {
				Thread.currentThread().sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			serverStopped = !isServerOn();
			if(!serverStopped) {
				failedAtts++;
				if(failedAtts==1) {
					System.out.println("Waiting for the server to stop...");
				}
			} else {
				System.out.println("Deleting the files...");
				break;
			}
		}
		if(!serverStopped) {
			System.out.println("The server is still running (cannot delete files), so the app got the waiting limit");
		} else {
			deleteFiles();
			System.out.println("Starting the server back...");
			try {
				Thread.currentThread().sleep(400); // Maybe customize
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			startServer();
		}
	}
	
	private static void deleteFiles() {
		deleteDirectory(world);
		deleteDirectory(worldNether);
		deleteDirectory(worldTheEnd);
	}
	
	private static void startServer() {
		AtomicReference<Process> ps = new AtomicReference<>();
		Thread serverThread = new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					ps.set(Runtime.getRuntime().exec("cmd.exe /c \"pushd \"" + serverDirectory.getPath() + "\" && start cmd /c " + launchServerFileName + "\""));
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		});
		serverThread.start();
	}
	
	@SuppressWarnings("unchecked")
	public static boolean loadConfig() {
		configsDirectory = new File(serverDirectory, "plugins/configs");
		mainConfigDirectory = new File(configsDirectory, "ManhuntAndWorldReset");
		mainConfigDirectory.mkdirs();
		
		File f = new File(mainConfigDirectory, "mainconfig.yml");
//		f.getParentFile().mkdirs();
		
		if(!f.exists()) return false;
		
		Yaml yaml = new Yaml();
		HashMap<String, Object> yamlMap = null;
		try {
			yamlMap = yaml.load(new FileInputStream(f));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		for (Map.Entry<String, Object> entry : yamlMap.entrySet()) {
            if(entry.getKey().equals("settings")) {
            	Map<String, Object> settings = (HashMap<String, Object>) entry.getValue();
            	for (Map.Entry<String, Object> entry2 : settings.entrySet()) {
	            	if(entry2.getKey().equals("world-resetting")) {
	            		Map<String, Object> worldResetting = (HashMap<String, Object>) entry2.getValue();
	            		for (Map.Entry<String, Object> entry3 : worldResetting.entrySet()) {
	    	            	if(entry3.getKey().equals("launch-server-script-name")) {
	    	            		launchServerFileName = (String) entry3.getValue();
	    	            	}
	    	            	if(entry3.getKey().equals("use-halt-for-instant-server-stopping")) {
	    	            		usedHalt = (boolean) entry3.getValue();
	    	            	}
	            		}
	            	}	            	
            	}
            }
        }
		return true;
	}
}
