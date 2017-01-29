package com.bearenterprises.Main;
import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import com.bearenterprises.ChatServer.Server;
import com.bearenterprises.ChatClient.ChatInterface;
import java.util.Scanner;

public class Main{
	
private static Options options; 
	
	public static void main(String[] args){
		options = new Options();
		
		options.addOption("start_server", false, "Start the Chat Server");
		options.addOption("start_client", false, "Start the Chat Client");
		options.addOption("a","address", true, "Set the the server's address(if not set defaults to localhost)");
		parse(options, args);
	}
	
	private static void parse(Options options, String[] args){
		CommandLineParser parser = new BasicParser();
		CommandLine cmd = null;
		String serverAddress = "localhost";
		try{
			cmd = parser.parse(options, args);
			if (cmd.hasOption("a")){
				serverAddress = cmd.getOptionValue("a");
			}
			
			if (cmd.hasOption("start_server")){
				System.out.println("Starting server...");
				Server server = new Server();
				server.start();
				Scanner sc = new Scanner(System.in);
				while(true){
					System.out.println("Press (y) to kill...");
					String command = sc.nextLine();
					if ("y".equals(command)){
						server.kill();
						System.out.println("Server killed");
						break;
					}
				}
				return;
			}
			
			if (cmd.hasOption("start_client")){
				ChatInterface gui = new ChatInterface(serverAddress);
				return;
			}
			help();
		}catch(ParseException e){
			help();
		}

	}
	
	private static void help() {
		HelpFormatter formater = new HelpFormatter();
		formater.printHelp("Main", options);
		System.exit(0);
	}
}