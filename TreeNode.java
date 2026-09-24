package ct;

import java.util.*;

public class TreeNode {

	public Map<String, String> blobs = new HashMap<>();
	
	public Map<String, TreeNode> subtree = new HashMap<>();
	
	public TreeNode createTree(Map<String, String> indexMap) {
		TreeNode root = new TreeNode();
//		 = indexing_obj.indexFileintoMap();
		
		for(Map.Entry<String, String> entry : indexMap.entrySet()) {
			add_path(root, entry.getKey(),entry.getValue());
		}
		return root;
	}
	public void add_path(TreeNode root, String path,String hash) {
		String[] parts = path.split("/");
		TreeNode current = root;
		
		for(int i=0;i<parts.length;i++) {
			if(i == parts.length-1) {
				current.blobs.put(parts[i], hash);
			}
			else {
				String folderName = parts[i];
				if(!current.subtree.containsKey(folderName)) {
					current.subtree.put(folderName, new TreeNode());
				}
				current = current.subtree.get(folderName);
			}
		}
	}
	
	public String createTreeObject(TreeNode root) {
		Object_functions obj_funcs = new Object_functions();
		Hash_for_ct hashing = new Hash_for_ct();
		
		StringBuilder treeBuilder = new StringBuilder();
		for(Map.Entry<String, TreeNode> entry : root.subtree.entrySet()) {
			String folderName = entry.getKey();
			TreeNode childNode= entry.getValue();
			
			String childHash = createTreeObject(childNode);
			
			treeBuilder.append("tree ").append(childHash).append(" ").append(folderName).append("\n");
		}
		for(Map.Entry<String, String> entry : root.blobs.entrySet()) {
			String fileName = entry.getKey();
			String fileHash = entry.getValue();
			treeBuilder.append("blob ").append(fileHash).append(" ").append(fileName).append("\n");
		}
		byte[] contentBytes = treeBuilder.toString().getBytes(java.nio.charset.StandardCharsets.UTF_8);
		byte[] fullPayLoadByte = obj_funcs.treePayLoadGen(contentBytes);
		String treeHash = hashing.generate_hash(fullPayLoadByte);
		obj_funcs.storeInObjects(treeHash, fullPayLoadByte);
		return treeHash;
	}
}


