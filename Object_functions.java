package ct;
import java.util.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;


//used in add method
public class Object_functions {
	
	//objects creation for classes
	Indexing indexing_class_obj = new Indexing();
	
	public byte[] payLoadGen(File target) {
		byte[] fullPayloadBytes = null;
		try {
			byte[] contentBytes = java.nio.file.Files.readAllBytes(target.toPath());
			byte[] headerBytes = ("blob "+contentBytes.length+"\0").getBytes(java.nio.charset.StandardCharsets.UTF_8);
			java.io.ByteArrayOutputStream combined = new java.io.ByteArrayOutputStream();
			combined.write(headerBytes);
			combined.write(contentBytes);
			fullPayloadBytes = combined.toByteArray();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if(fullPayloadBytes == null) {
			System.out.println(fullPayloadBytes+" is null");
		}
		return fullPayloadBytes;
	}
	
	public byte[] treePayLoadGen(byte[] contentBytes) {
		
		byte[] fullPayLoadBytes = null;
		try {
			byte[] headerBytes = ("tree "+contentBytes.length + "\0").getBytes(java.nio.charset.StandardCharsets.UTF_8);
			java.io.ByteArrayOutputStream combined = new java.io.ByteArrayOutputStream();
			combined.write(headerBytes);
			combined.write(contentBytes);
			
			fullPayLoadBytes = combined.toByteArray();
			
		}catch(IOException e){
			System.out.println("Error while generating tree pay load "+e);
		}
		return fullPayLoadBytes;
	}
	
	public byte[] treeFileGen() {
		StringBuilder treeBuilder = new StringBuilder();
		Map<String, String> indexMap = indexing_class_obj.indexFileintoMap();
		for(Map.Entry<String, String> entry: indexMap.entrySet()) {
			String fileName = entry.getKey();
			String fileHash = entry.getValue();
			
			treeBuilder.append("blob ").append(fileHash).append(" ").append(fileName).append("\n");
		}
		byte[] contentBytes = treeBuilder.toString().getBytes(java.nio.charset.StandardCharsets.UTF_8);
		return contentBytes;
	}
	
	public byte[] commitPayLoadGen(byte[] contentBytes) {
		byte[] fullPayLoadBytes = null;
		try {
			byte[] headerBytes = ("commit "+contentBytes.length + "\0").getBytes(java.nio.charset.StandardCharsets.UTF_8);
			java.io.ByteArrayOutputStream combined = new java.io.ByteArrayOutputStream();
			combined.write(headerBytes);
			combined.write(contentBytes);
			
			fullPayLoadBytes = combined.toByteArray();
			
		}catch(IOException e){
			System.out.println("Error while generating tree pay load "+e);
		}
		return fullPayLoadBytes;
	}
	
	public byte[] commitFileGen(String treeHash, String parent_commit_hash, String commit_msg) {
		StringBuilder commitBuilder = new StringBuilder();
		
		commitBuilder.append("tree ").append(treeHash).append("\n");
		if(parent_commit_hash != null && !parent_commit_hash.isEmpty()) {
			commitBuilder.append("parent ").append(parent_commit_hash).append("\n");
		}
		long timestamp = System.currentTimeMillis() / 1000L;
		commitBuilder.append("author Dev <dev@example.com> ").append(timestamp).append("\n");
		commitBuilder.append("committer Dev <dev@example.com> ").append(timestamp).append("\n\n");
		commitBuilder.append(commit_msg).append("\n");
		
		byte[] contentByte = commitBuilder.toString().getBytes(java.nio.charset.StandardCharsets.UTF_8);
		return contentByte;
	}
	
	public void storeInObjects(String hash, byte[] fullPayLoadBytes) {
		String dirName = hash.substring(0,2);
		String objFileName = hash.substring(2);
		File objSubDir = new File(".ct/objects", dirName);
		
		if(!objSubDir.exists()) {
			objSubDir.mkdirs();
		}
		File objFile = new File(objSubDir, objFileName);
		if(!objFile.exists()) {
			try {
				java.nio.file.Files.write(objFile.toPath(), fullPayLoadBytes);
			} catch (IOException e) {
				// TODO Auto-generated catch block
//				e.printStackTrace();
				System.out.println("Error in File wriring in obj file: "+e);
			}
		}
	}
	
	public String checkParentCommit(File ctdir) {
		File parentCommitFile = new File(ctdir,"refs/heads/main");
		String parent_commit_hash = null;
		if(!parentCommitFile.exists()) {
			return null;
		}
		try {
			String content = Files.readString(parentCommitFile.toPath()).trim();
			if(!content.isEmpty()) {
				parent_commit_hash = content;
			}
			
		}catch(IOException e) {
			System.out.println("Couldn't read refs/heads/main "+e);
		}
		
		return parent_commit_hash;
	}
	
	public String extract_commit_content(File ctdir, String commitHash) {
		String folderName = commitHash.substring(0,2); 
		String fileName = commitHash.substring(2); 
		byte[] fullData = null;
		File myCommitFile = new File(ctdir, "objects/"+folderName+"/"+fileName);
		if(!myCommitFile.exists()) {
			return null;
		}
		try {
			//extracting the contents from the commit object and stroing it as binaryt file to not lose any data if the obejct is like png or .class file etc
			fullData = Files.readAllBytes(myCommitFile.toPath());
		} catch (IOException e) {
			// TODO Auto-generated catch block
//			e.printStackTrace();
			System.out.println("Error reading the commit file "+e.getMessage());
		}
		int nullIndex = -1;
		for(int i=0;i<fullData.length;i++) {
			if(fullData[i] == 0) {
				nullIndex = i;
				break;
			}
		}
		
		if(fullData == null || nullIndex == -1) {
			return null;
		}
		byte[] contentBytes = Arrays.copyOfRange(fullData, nullIndex+1, fullData.length);
		String content = new String(contentBytes, java.nio.charset.StandardCharsets.UTF_8);
		return content;
//		if(nullIndex != -1) {
//			String header = new String(fullData, 0, nullIndex, java.nio.charset.StandardCharsets.UTF_8);
//			String content = new String(fullData, nullIndex+1, fullData.length-(nullIndex+1),java.nio.charset.StandardCharsets.UTF_8);
//			
//		}
	}
	
	public Map<String, String> collectTreeFiles(File ctdir, String rootTreeHash, String currentPath, Map<String, String> targetFiles){
		String tree_content = extract_commit_content(ctdir, rootTreeHash);
		if(tree_content == null || tree_content.isEmpty()) {
			System.out.println("Root tree object's content is empty or null !!");
			return null;
		}
		String[] lines = tree_content.split("\n");
		String fullRelativePath = null;
		
		for(String line : lines) {
			line = line.trim();
			if(line.isEmpty()) continue;
			String[] parts = line.split(" ",3);
			if(currentPath.isEmpty()) {
				fullRelativePath = parts[2];
			}else {
				fullRelativePath = currentPath+"/"+parts[2];
			}
			if(parts[0].equals("blob")) {
				targetFiles.put(fullRelativePath, parts[1]);
			}
			else if(parts[0].equals("tree")) {
				collectTreeFiles(ctdir, parts[1], fullRelativePath, targetFiles);
			}
		}
		return targetFiles;
		
	}
}
