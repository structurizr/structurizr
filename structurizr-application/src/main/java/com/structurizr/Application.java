package com.structurizr;

import com.structurizr.command.*;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.LinkedHashMap;
import java.util.Map;

@SpringBootApplication
public class Application {

	private static final String PLAYGROUND_COMMAND = "playground";
	private static final String LOCAL_COMMAND = "local";
	private static final String PUSH_COMMAND = "push";
	private static final String PULL_COMMAND = "pull";
	private static final String LOCK_COMMAND = "lock";
	private static final String UNLOCK_COMMAND = "unlock";
	private static final String EXPORT_COMMAND = "export";
	private static final String MERGE_COMMAND = "merge";
	private static final String VALIDATE_COMMAND = "validate";
	private static final String INSPECT_COMMAND = "inspect";
	private static final String LIST_COMMAND = "list";
	private static final String VERSION_COMMAND = "version";
	private static final String HELP_COMMAND = "help";

	private static final Map<String, AbstractCommand> COMMANDS = new LinkedHashMap<>();

	static {
		COMMANDS.put(PLAYGROUND_COMMAND, new PlaygroundCommand());
		COMMANDS.put(LOCAL_COMMAND, new LocalCommand());
		COMMANDS.put(PUSH_COMMAND, new PushCommand());
		COMMANDS.put(PULL_COMMAND, new PullCommand());
		COMMANDS.put(LOCK_COMMAND, new LockCommand());
		COMMANDS.put(UNLOCK_COMMAND, new UnlockCommand());
		COMMANDS.put(EXPORT_COMMAND, new ExportCommand());
		COMMANDS.put(MERGE_COMMAND, new MergeCommand());
		COMMANDS.put(VALIDATE_COMMAND, new ValidateCommand());
		COMMANDS.put(INSPECT_COMMAND, new InspectCommand());
		COMMANDS.put(LIST_COMMAND, new ListCommand());
		COMMANDS.put(VERSION_COMMAND, new VersionCommand());
		COMMANDS.put(HELP_COMMAND, new AbstractCommand() {
			@Override
			public void run(String... args) {
				printUsage();
				System.exit(0);
			}
		});
	}

	public static void main(String[] args) {
		if (args == null || args.length == 0) {
			printUsage();
			System.exit(1);
		}

		try {
			String commandName = args[0];
			AbstractCommand command = COMMANDS.get(commandName);
			if (command != null) {
				command.run(args);
			} else {
				System.out.println("Unknown command: " + commandName);
				printUsage();
				System.exit(1);
			}
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(1);
		}
	}

	private static void printUsage() {
		String commandList = String.join("|", COMMANDS.keySet());
		System.out.println(String.format("Usage: %s [arguments]", commandList));
	}

}