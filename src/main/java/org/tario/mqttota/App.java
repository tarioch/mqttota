package org.tario.mqttota;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@ComponentScan
@Configuration
@PropertySource("file:config.properties")
public class App {

	private static final String SINGLE_NODE_OPTION = "n";
	private static final String ALL_NODES_OPTION = "a";

	public static void main(String[] args) {
		try (ConfigurableApplicationContext context = new AnnotationConfigApplicationContext(App.class)) {
			Options options = new Options();
			options.addOption(Option.builder(SINGLE_NODE_OPTION)
					.desc("Update a specific node")
					.hasArg()
					.argName("NODE")
					.build());
			options.addOption(ALL_NODES_OPTION, false, "Update all nodes");

			CommandLineParser cliParser = new DefaultParser();
			try {
				CommandLine cli = cliParser.parse(options, args);
				OtaPusher otaPusher = context.getBean(OtaPusher.class);

				if (cli.hasOption(ALL_NODES_OPTION)) {
					otaPusher.updateAllNodes();
				} else if (cli.hasOption(SINGLE_NODE_OPTION)) {
					otaPusher.updateNode(cli.getOptionValue(SINGLE_NODE_OPTION));
				} else {
					otaPusher.readState();
				}
			} catch (ParseException e) {
				HelpFormatter help = new HelpFormatter();
				help.printHelp("java -jar mqttota.jar", options);
			}
		}
	}
}
