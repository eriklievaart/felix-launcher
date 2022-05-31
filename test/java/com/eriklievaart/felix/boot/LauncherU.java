package com.eriklievaart.felix.boot;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

public class LauncherU {

	@Test
	public void sortVersionListBundlesFirst() {
		List<File> files = new ArrayList<>();

		files.add(new File("/home/eazy/Applications/blog/bundle/commons-lang3-3.5.jar"));
		files.add(new File("/home/eazy/Applications/blog/bundle/jl-bundle.jar"));
		files.add(new File("/home/eazy/Applications/blog/bundle/org.apache.felix.http.jetty-4.0.6.jar"));
		files.add(new File("/home/eazy/Applications/blog/bundle/toolkit-lang.jar"));

		Launcher.sortVersionedBundlesFirst(files);
		Assert.assertEquals(files.get(0).getName(), "commons-lang3-3.5.jar");
		Assert.assertEquals(files.get(1).getName(), "org.apache.felix.http.jetty-4.0.6.jar");
		Assert.assertEquals(files.get(2).getName(), "jl-bundle.jar");
		Assert.assertEquals(files.get(3).getName(), "toolkit-lang.jar");
	}

	@Test
	public void hasVersionNumber() {
		Assert.assertTrue(Launcher.hasVersionNumber("org.apache.felix.http.jetty-4.0.6.jar"));
		Assert.assertFalse(Launcher.hasVersionNumber("jl-bundle.jar"));
	}
}
