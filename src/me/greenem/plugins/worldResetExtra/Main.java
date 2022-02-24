package me.greenem.plugins.worldResetExtra;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import org.yaml.snakeyaml.Yaml;

public class Main {
	
	public static File configsDirectory;
	public static File mainConfigDirectory;
	
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
		
		AtomicReference<Process> ps = new AtomicReference<>();
		File exelutionDir = new File(System.getProperty("user.dir"));
		//System.out.println(exelutionDir.getPath());
		File mainServerDir = exelutionDir.getParentFile().getParentFile();
		System.out.println("Searching for server files in \"" + mainServerDir.getPath() + "\"...");
		
		configsDirectory = new File(mainServerDir, "configs");
		mainConfigDirectory = new File(configsDirectory, "ManhuntAndWorldReset");
		
		mainConfigDirectory.mkdirs();
		
		loadConfig();
		
		File world = new File(mainServerDir, "world");
		File world_nether = new File(mainServerDir, "world_nether");
		File world_the_end = new File(mainServerDir, "world_the_end");
		
		boolean deletionOk = false;
		
		if(!world.exists() && !world_nether.exists() && !world_the_end.exists()) {
			System.out.println("THE CODE WAS EXECUTED IN WORNG LOCATION!!! (Or wolrds are already deleted)");
		}
		else {
			while(!deletionOk) {
				if(deleteDirectory(world) && deleteDirectory(world_nether) && deleteDirectory(world_the_end)) {
					deletionOk = true;
				}
			}
		}
		
		System.out.println(deletionOk);
		
		if(new File(mainServerDir, "server.properties").exists()) {
			Thread serverThread = new Thread(new Runnable() {
				@Override
				public void run() {
					try {
						ps.set(Runtime.getRuntime().exec("cmd.exe /c \"cd \"" + mainServerDir.getPath() + "\" && start cmd /c " + launchServerFileName + "\""));
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			});
			serverThread.start();
		}
		
		/*try {
			Thread.currentThread().sleep(10000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		killer(ps.get().toHandle());*/
	}
	
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
            System.out.println(entry.toString());
            if(entry.getKey().equals("launch-server-file-name")) {
            	launchServerFileName = (String) entry.getValue();
            }
        }
	}
}
