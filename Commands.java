package ct;
import java.io.File;
import java.io.IOException;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.util.*;
import java.util.Map.Entry;
import java.io.FileReader;
import java.io.BufferedReader;

public class Commands {
	//objects for classes
	Object_functions obj_funcs = new Object_functions();
	Hash_for_ct hashing_obj = new Hash_for_ct();
	Indexing indexing_obj = new Indexing();
	Status_checks status_obj = new Status_checks();
	Local_file_operations local_file_ope_obj = new Local_file_operations();
	
	File ctdir = new File(".ct");
	File workingDir = ctdir.getAbsoluteFile().getParentFile();
	
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
				indexing_obj.staging_area(target.toPath().normalize().toString(), hash);
				
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
	
	public void log() {
		File currentCommitaccess = new File(ctdir, "refs/heads/main");
		if(!currentCommitaccess.exists()) {
			System.out.println("fatal: your current branch 'main' does not have any commits yet");
			return;
		}
		String currCommitHash = null;
		try {
			currCommitHash = Files.readString(currentCommitaccess.toPath());
		} catch (IOException e) {
			// TODO Auto-generated catch block
//			e.printStackTrace();
			System.out.println("Error while accessing the commit hash from refs/heads/main: "+e.getMessage());
		}
		while(currCommitHash != null && !currCommitHash.isEmpty()) {
			String commitContent = obj_funcs.extract_commit_content(ctdir, currCommitHash);
			if(commitContent == null) {
				System.out.println("Error: Corrupted commit object " + currCommitHash);
	            break;
			}
			String parentHash = null;
			String author = "";
			String date = "";
			StringBuilder message = new StringBuilder();
			boolean readingMessage = false;
			String[] lines = commitContent.split("\n");
			for(String line:lines) {
				if(readingMessage) {
					message.append(line).append("\n");
				}
				else if(line.isEmpty()) {
					readingMessage = true;
				}
				else if(line.startsWith("parent ")) {
					parentHash = line.substring("parent ".length()).trim();
				}
				else if(line.startsWith("author ")) {
					author = line.substring("author ".length()).trim();
				}
				else if(line.startsWith("date ")) {
					date = line.substring("date ".length()).trim();
				}
			}
			
			System.out.println("commit "+currCommitHash);
			if(!author.isEmpty()) System.out.println("Author:  "+author);
			if(!date.isEmpty()) System.out.println("Date:  "+date);
			System.out.println("\n   "+message.toString().trim()+"\n");
			
			currCommitHash = parentHash;
			
		}
	}
	
	public void checkout(File ctdir, String target_commit_hash) {
		//Extracting root tree hash from commit object's content
		String commit_content = obj_funcs.extract_commit_content(ctdir, target_commit_hash);
		if(commit_content == null) {
			System.out.println("The commits' content is empty!");
			return;
		}
		String lines[] = commit_content.split("\n");
		String rootTreeHash = null;
		
		for(String line : lines) {
			if(line.startsWith("tree ")) {
				rootTreeHash = line.substring(5).trim();
			}
		}
		if(rootTreeHash == null) {
			System.out.println("Root tree's hash is null!");
			return;
		}
		
		//Top-down recursion
		Map<String, String> target_files_map = obj_funcs.collectTreeFiles(ctdir, rootTreeHash, "", new HashMap<String, String>());
		//delete removed tracked files
		Map<String, String> index_file_map = indexing_obj.indexFileintoMap();
		for(String keys : index_file_map.keySet()) {
			if(!target_files_map.containsKey(keys)) {
				new File(workingDir, keys).delete();
			}
		}
		//restore or overwrite files
		for(Entry<String, String> entry : target_files_map.entrySet()) {
			String relativePath = entry.getKey();
			String blobHash = entry.getValue();
			String blob_content = obj_funcs.extract_commit_content(ctdir, blobHash);
			File targetFile = new File(workingDir, relativePath);
			if(targetFile.getParentFile() != null) {
				targetFile.getParentFile().mkdirs();
			}
			
			try {
				Files.writeString(targetFile.toPath(), blob_content);
			} catch (IOException e) {
				// TODO Auto-generated catch block
//				e.printStackTrace();
				System.out.print("Overwriting or updating file has met an error in checkout!"+e.getMessage());
			}	
		}
		indexing_obj.writeMaptoIndex(target_files_map);
		
		File mainref = new File(ctdir, "refs/heads/main");
		try {
			Files.writeString(mainref.toPath(), target_commit_hash);
			System.out.println("Switched to commit: "+target_commit_hash.substring(0,7));
			
		}catch(IOException e) {
			System.out.println("Error Updating branch ref: "+e.getMessage());
		}
		
	}
	
	public void status() {
		Map<String, String> index_file_map = indexing_obj.indexFileintoMap();
		Map<String, String> commit_file_map = obj_funcs.getCommitFilesToMap(ctdir);
		List<String> localFiles = local_file_ope_obj.scanLocalFiles(workingDir, "", new ArrayList<>());
		
		//comparison A
		List<String> new_file_arr = status_obj.new_file_check(index_file_map, commit_file_map);
		List<String> modified_file_arr = status_obj.modified_check(index_file_map, commit_file_map);
		List<String> deleted_file_arr = status_obj.deleted_check(index_file_map, commit_file_map);
		//comparison B
		List<String> untracked_arr = status_obj.untracked_check(index_file_map, localFiles);
		List<String> modified_local_file_arr = status_obj.modified_local_check(workingDir, index_file_map);
		List<String> deleted_local_file_arr = status_obj.deleted_local_check(workingDir, index_file_map);
		
		boolean clean = true;
		
		if(!new_file_arr.isEmpty() || !modified_file_arr.isEmpty() || !deleted_file_arr.isEmpty()) {
			clean = false;
			System.out.println("Changes to be committed");
			System.out.println(" (use \"ct commit -m <msg>\" to commit");
			for(String file : new_file_arr) {
				System.out.println("\tnew file:  "+file);
			}
			for(String file : modified_file_arr) {
				System.out.println("\tmodified:  "+file);
			}
			for(String file : modified_file_arr) {
				System.out.println("\tdeleted:  "+file);
			}
			System.out.println();
		}
		if(!new_file_arr.isEmpty() || !modified_file_arr.isEmpty() || !deleted_file_arr.isEmpty()) {
		    clean = false;
		    System.out.println("Changes to be committed:");
		    System.out.println("  (use \"ct commit -m <msg>\" to commit)");
		    for(String file : new_file_arr) {
		        System.out.println("\tnew file:   " + file);
		    }
		    for(String file : modified_file_arr) {
		        System.out.println("\tmodified:   " + file);
		    }
		    for(String file : deleted_file_arr) {   // <--- FIXED to deleted_file_arr
		        System.out.println("\tdeleted:    " + file);
		    }
		    System.out.println();
		}
		if(!untracked_arr.isEmpty()) {
			clean = false;
			System.out.println("Untracked file:");
			System.out.println(" (use\"ct add <file>...\" to include in what will be committed");
			for(String file : untracked_arr) {
				System.out.println("\t"+file);
			}
			System.out.println();
		}
		
		if(clean) {
			System.out.println("nothing to commit, working tree clean");
		}
		
	}
	
}
