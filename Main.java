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
	}

}
