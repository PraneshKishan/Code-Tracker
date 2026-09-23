package ct;

import java.io.File;
import java.io.IOException;

//used in add method
public class Object_functions {
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

}
