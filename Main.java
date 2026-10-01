package ct;
import java.util.*;
import java.io.File;
import java.io.IOException;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.io.FileReader;
import java.io.BufferedReader;

public class Main {
	public static void main(String[] args) {
		System.out.println("DEBUG: args[0]=" + (args.length > 0 ? args[0] : "none"));
		Commands cmd = new Commands();
		
		
		//Guard against no input
		if(args.length == 0) {
			System.out.println("Please provide a command. Example: ct init");
			return;
		}
		
		if(args[0].equals("init")) {
			cmd.init();
		}
//		else {
//			System.out.println("Unknown command "+args[0]);
//		}
		
		if(args[0].equals("add") && args.length >= 2) {
			cmd.add(args[1]);
		}
		
		if(args[0].equals("commit")) {
			if(args.length >= 3 && args[1].equals("-m")) {
				cmd.commit(args[2]);
			}else {
				System.out.println("Usage: ct commit -m\"<commit-message>\"");
			}
		}
		
		if(args[0].equals("log")) {
			cmd.log();
		}
		
		if(args[0].equals("checkout")) {
			if(args.length < 2) {
				System.out.println("fatal: commit hash required!");
				return;
			}
			cmd.checkout(new File(".ct"), args[1]);
		}
	}

}
