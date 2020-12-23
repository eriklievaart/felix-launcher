package com.eriklievaart.felix.boot;

import java.io.File;

public class Main {

	public static void main(String[] args) throws Exception {
		new Launcher(getRootDir(args)).start();
	}

	private static File getRootDir(String[] args) {
		if (args.length == 0) {
			// run application packed with felix jar
			return new File(JvmPaths.getJarDirOrRunDir(Main.class));
		}
		if (args[0].contains("/")) {
			// absolute path for root
			return new File(args[0]);

		} else {
			// run application at [user.home]/Applications/[arg0]
			File home = new File(System.getProperty("user.home"));
			File applications = new File(home, "Applications");
			return new File(applications, args[0]);
		}
	}
}
