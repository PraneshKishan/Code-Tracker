package ct;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class Indexing {
	public void staging_area(String target, String hash) {
		Map<String, String> indexMap = new HashMap<>();
		File indexfile = new File(".ct","index.json");
		if(!indexfile.exists()) {
			try {
				indexfile.createNewFile();
			} catch (IOException e) {
				// TODO Auto-generated catch block
//				e.printStackTrace();
				System.out.println("Error while creating Index file "+e);
			}
		}
		else {
			List<String> lines = Collections.emptyList();
			try {
				lines = java.nio.file.Files.readAllLines(indexfile.toPath());
				
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			for(String line : lines) {
				line = line.trim();
				if(line.contains(":") && !line.startsWith("{") && !line.startsWith("}")) {
					String[] parts = line.split(":",2);
					String filePath = parts[0].replace("\"","").trim();
					String fileHash = parts[1].replace("\"","").replace(",","").trim();
					indexMap.put(filePath, fileHash);
				}
			}
		}
		String normalizedPath = target.replace("\\", "/");
		indexMap.put(normalizedPath, hash);
		StringBuilder json = new StringBuilder();
		json.append("{\n");
		int count = 0;
		for(Map.Entry<String, String> entry : indexMap.entrySet()) {
			json.append("  \"").append(entry.getKey()).append("\":\"").append(entry.getValue()).append("\"");
			count++;
			if(count < indexMap.size()) {
				json.append(",");
			}
			json.append("\n");
		}
		json.append("}\n");
		try {
			java.nio.file.Files.writeString(indexfile.toPath(), json.toString());
		} catch (IOException e) {
			// TODO Auto-generated catch block
//			e.printStackTrace();
			System.out.println("Error while writing json file: "+e);
		}
		
	}

}
