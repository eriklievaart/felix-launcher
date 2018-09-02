package com.eriklievaart.felix.boot;

import java.io.File;

public class Main {

	public static void main(String[] args) throws Exception {
		new Launcher(getRootDir(args)).start();
	}

	private static File getRootDir(String[] args) {
		if (args.length == 0) {
			return new File(JvmPaths.getJarDirOrRunDir(Main.class));
		}
		File home = new File(System.getProperty("user.home"));
		File applications = new File(home, "Applications");
		return new File(applications, getProjectName(args));
	}

	private static String getProjectName(String[] args) {
		String name = "q";
		if (args.length > 0) {
			name = args[0];
		}
		return name;
	}
}
