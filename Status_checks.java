package ct;
import java.io.File;
import java.util.*;

public class Status_checks {
	Object_functions obj_funcs = new Object_functions();
	Hash_for_ct hashing_obj = new Hash_for_ct();
	
	//Comparison A
	public List<String> new_file_check(Map<String, String> index_file_map, Map<String, String> commit_file_map){
		List<String> new_file_status_arr = new ArrayList<>();
		for(String key : index_file_map.keySet()) {
			boolean isPresent = commit_file_map.containsKey(key);
			if(!isPresent) {
				new_file_status_arr.add(key);
			}
		}
		return new_file_status_arr;
	}
	
	public List<String> modified_check(Map<String, String> index_file_map, Map<String, String> commit_file_map){
		List<String> modified_status_arr = new ArrayList<>();
		for(String key : index_file_map.keySet()) {
			boolean isPresent = commit_file_map.containsKey(key);
			if(isPresent) {
				if(!index_file_map.get(key).equals(commit_file_map.get(key))) {
					modified_status_arr.add(key);
				}
			}
		}
		return modified_status_arr;
	}
	
	public List<String> deleted_check(Map<String, String> index_file_map, Map<String, String> commit_file_map){
		List<String> deleted_status_arr = new ArrayList<>();
		for(String key : commit_file_map.keySet()) {
			if(!index_file_map.containsKey(key)) {
				deleted_status_arr.add(key);
			}
		}
		return deleted_status_arr;
	}
	
	//Comparison B
	public List<String> untracked_check(Map<String, String> index_file_map, List<String> localFiles) {
		List<String> untracked_status_arr = new ArrayList<>();
		for(String files : localFiles) {
			if(!index_file_map.containsKey(files)) {
				untracked_status_arr.add(files);
			}
		}
		return untracked_status_arr;
	}
	
	public List<String> modified_local_check(File workingDir, Map<String, String> index_file_map){
		List<String> modified_local_status_arr = new ArrayList<>();
		for(Map.Entry<String, String> entry : index_file_map.entrySet()){
			String path = entry.getKey();
			String stagedHash = entry.getValue();
			File fileOnDisk = new File(workingDir, path);
			if(fileOnDisk.exists()) {
				byte[] currentPayLoad = obj_funcs.payLoadGen(fileOnDisk);
				if(currentPayLoad != null) {
					String currentHash = hashing_obj.generate_hash(currentPayLoad);

					if(!currentHash.equals(stagedHash)) {
						modified_local_status_arr.add(path);
					}
				}
			}
		}
		return modified_local_status_arr;
	}
	public List<String> deleted_local_check(File workingDir, Map<String, String> index_file_map){
		List<String> deleted_local_status_arr = new ArrayList<>();
		for(Map.Entry<String, String> entry : index_file_map.entrySet()){
			String path = entry.getKey();
			String stagedHash = entry.getValue();
			File fileOnDisk = new File(workingDir, path);
			if(!fileOnDisk.exists()) {
				deleted_local_status_arr.add(path);
			}
			
		}
		return deleted_local_status_arr;
	}
	
	
}
