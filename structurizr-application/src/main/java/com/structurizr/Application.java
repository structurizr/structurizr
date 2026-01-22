package com.structurizr;

import com.structurizr.command.*;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.LinkedHashMap;
import java.util.Map;

@SpringBootApplication
public class Application {

	private static final Map<String, AbstractCommand> COMMANDS = new LinkedHashMap<>();

	static {
		register(new PlaygroundCommand());
		register(new LocalCommand());
		register(new ServerCommand());

		register(new PushCommand());
		register(new PullCommand());
		register(new LockCommand());
		register(new UnlockCommand());
		register(new BranchesCommand());
		register(new DeleteCommand());

		register(new ExportCommand());
		register(new MergeCommand());
		register(new ValidateCommand());
		register(new InspectCommand());
		register(new ListCommand());
		register(new VersionCommand());
		register(new AbstractCommand("help") {
			@Override
			public void run(String... args) {
				printUsage();
				System.exit(0);
			}
		});
	}

	private static void register(AbstractCommand command) {
		COMMANDS.put(command.getName(), command);
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