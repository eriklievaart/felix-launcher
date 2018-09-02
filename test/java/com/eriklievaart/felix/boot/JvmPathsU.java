package com.eriklievaart.felix.boot;

import org.junit.Assert;
import org.junit.Test;

public class JvmPathsU {

	@Test
	public void getParentPath() throws Exception {
		String parent = JvmPaths.getParentPath("/erikl/Applications/q3/launcher.jar");
		Assert.assertEquals("/erikl/Applications/q3", parent);
	}
}
