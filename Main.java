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
//		System.out.println("DEBUG: args[0]=" + (args.length > 0 ? args[0] : "none"));
		Commands cmd = new Commands();
		
		
		//Guard against no input
		if(args.length == 0) {
			System.out.println("Please provide a command. Example: ct init");
			return;
		}
		
		if(args[0].equalsIgnoreCase("ct")) {
			args = Arrays.copyOfRange(args, 1, args.length);
			if(args.length == 0) {
				printHelp();
				return;
			}
		}
		
		String command = args[0].toLowerCase();
		switch(command) {
		case "init":
			cmd.init();
			break;
		
		case "add":
			if(args.length < 2) {
				System.out.println("fatal: pathspec required to add");
				System.out.println("usage: ct add <file>");
				return;
			}
			cmd.add(args[1]);
			break;
		case "commit":
			if(args.length >= 3 && args[1].equals("-m")) {
				cmd.commit(args[2]);
			}else {
				System.out.println("fatal: commit message required");
				System.out.println("usage: ct commit -m \"<message\"");
			}
			break;
		case "log":
			cmd.log();
			break;
		case "status":
			cmd.status();
			break;
		case "checkout":
			if(args.length < 2) {
				System.out.println("fatal: commit hash required");
				System.out.println("usage: ct checkout <commit-hash");
			}
			cmd.checkout(new File(".ct"), args[1]);
			break;
		case "--help":
		case "-h":
		case "help":
			printHelp();
			break;
		default:
			System.out.println("ct: '"+args[0]+"' is not a ct command. See ct --help'.");
			break;
		}
	}
	private static void printHelp() {
		System.out.println("usage: ct <command> [<args>]");
		System.out.println("\nAvailable commands: ");
		System.out.println("	init	Initialize an empty CodeTracker repo");
		System.out.println("	add	Add file contents to the staging area");
		System.out.println("	commit	Add file contents to the staging area");
		System.out.println("	status	Show the working tree status");
		System.out.println("	log	Show commit logs");
		System.out.println("	checkout	Switch to a past commit snapshot");
	}

}
