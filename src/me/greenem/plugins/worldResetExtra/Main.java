package me.greenem.plugins.worldResetExtra;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicReference;

import org.yaml.snakeyaml.Yaml;

public class Main {
	
	public static File serverDirectory;
	public static File configsDirectory;
	public static File mainConfigDirectory;
	public static File world;
	public static File worldNether;
	public static File worldTheEnd;
	
	// server.properties SHOULD BE IN FOLDER
	// USER HAS TO SET launchServerFileName IN CONFIG (later)
	// CURRENTLY WORKS ONLY ON WINDOWS (and difficult for me to test in other os)
	
	/*public static void killer(ProcessHandle ph) {
		ph.children().forEach(ph2-> {
			killer(ph2);
		});
		//System.out.println("Stage " + stage + ", " + ph.toString());
		ph.destroyForcibly();
	}*/
	
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
		System.out.println("If it does show up, everything is working fine");
		
		File exelutionDir = new File(System.getProperty("user.dir"));
		if(exelutionDir.getAbsolutePath().equals("C:\\Windows\\System32")) {
			System.out.println("You cannot start this application in your System32 directory!");
			return;
		}
		//serverDirectory = exelutionDir.getParentFile().getParentFile();
		serverDirectory = new File("G:\\SERVERS\\Dev Server 1.18");
		
		loadConfig();
		if(true) return;
		if(!checkConditions()) {
			System.out.println("Wrong location");
			return;
		}
		
		waitForTheServerToStopAndDeleteWorlds();
//		start();
	}
	
	private static boolean checkConditions() {
		System.out.println("Searching for server files in \"" + serverDirectory.getPath() + "\"...");
		
		configsDirectory = new File(serverDirectory, "plugins/configs");
		mainConfigDirectory = new File(configsDirectory, "ManhuntAndWorldReset");
		mainConfigDirectory.mkdirs();
		
		String worldMainName = "world"; //TODO
		world = new File(serverDirectory, worldMainName);
		worldNether = new File(serverDirectory, worldMainName + "_nether");
		worldTheEnd = new File(serverDirectory, worldMainName + "_the_end");
		
		if(!world.exists() && !worldNether.exists() && !worldTheEnd.exists()) {
			System.out.println("THE CODE WAS EXECUTED IN WORNG LOCATION OR THE WORLDS ARE ALREADY DELETED!");
			return false;
		}
		if(!new File(serverDirectory, "server.properties").exists()) {
			return false;
		}
		return true;
	}
	
	private static void waitForTheServerToStopAndDeleteWorlds() {
		//File f = new File(new File(System.getProperty("user.dir")).getParentFile().getParentFile(), "world/session.lock");
		File f = new File(serverDirectory, "world/session.lock");
		boolean serverStopped = (!f.exists()) || f.delete();
		int failedAtts = 0;
		
		if(!serverStopped) {
			System.out.println("Waiting for the server to stop...");
		} else {
			failedAtts = 100; // bad code but still
		}
		
		while(!serverStopped || failedAtts < 60) { // chenk config for halt
			try {
				Thread.currentThread().sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			serverStopped = deleteDirectory(world) && deleteDirectory(worldNether) && deleteDirectory(worldTheEnd);
			if(!serverStopped) {
				failedAtts++;
			} else {
				System.out.println("Successfully deleted the files");
			}
		}
		
//		System.out.println("Starting the world resetting");
	}
	
	private static void start() {
		boolean deletionOk = false;
		while(!deletionOk) {
			if(deleteDirectory(world) && deleteDirectory(worldNether) && deleteDirectory(worldTheEnd)) {
				deletionOk = true;
			}
		}
		
		if(!new File(serverDirectory, launchServerFileName).exists()) {
			System.out.println("The launch script is missing");
		}
		
		if(new File(serverDirectory, "server.properties").exists()) {
			startServer();
		} else {
			System.out.println("Wrong location");
		}
		//killer(ps.get().toHandle());
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
	
	private static boolean isUsed(File file) {
		boolean isLocked=false;
		RandomAccessFile fos=null;
		try {
		      if(file.exists())
		        fos=new RandomAccessFile(file,"rw");        
		}catch (FileNotFoundException e) {
		    isLocked = true;
		}catch (SecurityException e) {
		    isLocked = true;
		}catch (Exception e) {
		    // handle exception
			
		}finally {
		    try {
		        if(fos!=null) {
		            fos.close();
		        }
		    }catch(Exception e) {
		        //handle exception
		    	
		    }
		}
		return isLocked;
	}
	
	@SuppressWarnings("unchecked")
	public static void loadConfig() {
		File f = new File(mainConfigDirectory, "mainconfig.yml");
		
		f.getParentFile().mkdirs();
		
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
            System.out.println(launchServerFileName);
            System.out.println(usedHalt);
        }
	}
}
