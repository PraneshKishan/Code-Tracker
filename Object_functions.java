package ct;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

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

}
