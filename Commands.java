package ct;
import java.io.File;
import java.io.IOException;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.*;
import java.io.FileReader;
import java.io.BufferedReader;

public class Commands {
	//objects for classes
	Object_functions obj_funcs = new Object_functions();
	Hash_for_ct hashing_obj = new Hash_for_ct();
	Indexing indexing_obj = new Indexing();
	
	File ctdir = new File(".ct");
	
	public void init() {

//		File ctdir = new File(".ct");
		if(!ctdir.exists()) {
			boolean result = ctdir.mkdir();
			if(result) {
				System.out.println("Code Tracker initialized successfully!");
			}
			else {
				System.out.println("Code Tracker initialization Failed!");
			}
		}else {
			System.out.println("Code Tracker has been initialized already!");
			return;
		}
		
		String[] ct_folder_names = {"refs","objects"};
		for(String subfolder_names: ct_folder_names) {
			File subdirs = new File(ctdir, subfolder_names);
			if(!subdirs.exists()) {
				subdirs.mkdir();
			}
			if(subfolder_names.equals("refs")) {
				File refs_subfolder_heads = new File(subdirs, "heads");
				File refs_subfolder_tags = new File(subdirs, "tags");
				if(!refs_subfolder_heads.exists() && !refs_subfolder_tags.exists()) {
					refs_subfolder_heads.mkdir();
					refs_subfolder_tags.mkdir();
				}
				
			}
		}
		File HEAD_file = new File(ctdir, "HEAD");
		if(!HEAD_file.exists()) {
			try {
				HEAD_file.createNewFile();
			} catch (IOException e) {
//				e.printStackTrace();
				System.err.println("Could not create file "+ e.getMessage());
			}
		}
		
	}
	
	public void add(String fileName) {
		//Read the contents of the file
		//Create BLOB object from the content
		//Store the blob object in database
		//Update index to include the file
		if(!ctdir.exists()) {
			System.out.println("Fatal Error! Inititalize code tracker");
			return;
		}
		File target = new File(fileName);
		if(target.exists()) {
			if(target.isFile()) {
				byte[] fullPayloadBytes = obj_funcs.payLoadGen(target);
				if (fullPayloadBytes == null) return;
				String hash = hashing_obj.generate_hash(fullPayloadBytes);
				if (hash == null) return;
				obj_funcs.storeInObjects(hash, fullPayloadBytes);
				indexing_obj.staging_area(target.getPath(), hash);
				
			}
			else {
				System.out.println("The "+fileName+" is not a file");
			}
		}else {
			System.out.println("The "+fileName+" does not exists!");
		}
	}
	
	public String tree_obj() {
		Map<String, String> indexMap = indexing_obj.indexFileintoMap();
		if(indexMap.isEmpty()) {
			System.out.println("Nothing to commit, working tree clean");
			return null;
		}
		//Job A - creating a tree structure
		TreeNode treeNodeHelper = new TreeNode();
		TreeNode root = treeNodeHelper.createTree(indexMap);
		//Job B - creating the hashing and writing obejcts for the whole tree
		String treeHash = treeNodeHelper.createTreeObject(root);
		return treeHash;
		
	}
	
	public void commit(String commit_msg) {
		String treeHash = tree_obj();
		if(treeHash == null) return;
		
		String parent_commit_hash = obj_funcs.checkParentCommit(ctdir);
		byte[] contentBytes = obj_funcs.commitFileGen(treeHash, parent_commit_hash, commit_msg);
		byte[] fullPayLoadBytes = obj_funcs.commitPayLoadGen(contentBytes);
		String commitHash = hashing_obj.generate_hash(fullPayLoadBytes);
		obj_funcs.storeInObjects(commitHash, fullPayLoadBytes);
		File mainBranchFile = new File(ctdir, "refs/heads/main");
		try {
			if (mainBranchFile.getParentFile() != null) {
	            mainBranchFile.getParentFile().mkdirs();
	        }
			java.nio.file.Files.writeString(mainBranchFile.toPath(), commitHash);
		}catch(IOException e) {
			System.out.println("Error updating main branch: "+e.getMessage());
		}
		System.out.println("[" + commitHash.substring(0, 7) + "] " + commit_msg);
	}
}
