package ct;

import java.security.NoSuchAlgorithmException;

public class Hash_for_ct {
	public String generate_hash(byte[] fullPayLoadBytes) {
		String hash = null;
		try {
			java.security.MessageDigest md = java.security.MessageDigest.getInstance("SHA-1");
			byte[] digest = md.digest(fullPayLoadBytes);
			hash = java.util.HexFormat.of().formatHex(digest);
			
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if(hash == null) {
			System.out.println("Hash is null!!");
		}
		return hash;
	}
	

}
