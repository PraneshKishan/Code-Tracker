package ct;

import java.io.File;
import java.util.*;

public class Local_file_operations {
	public List<String> scanLocalFiles(File dir, String currentPath, List<String> localFiles) {
		File[] entries = dir.listFiles();
		if(entries == null) return localFiles;
		
		for(File entry : entries) {
			String name = entry.getName();
			if(name.equals(".ct")) continue;
			
			String relativePath = currentPath.isEmpty() ? name : currentPath+"/"+name;
			
			if(entry.isDirectory()) {
				scanLocalFiles(entry, relativePath, localFiles);
			}
			else if(entry.isFile()) {
				localFiles.add(relativePath);
			}
		}
		return localFiles;
	}
	
}
