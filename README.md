# felix-launcher
This repository contains the code for a simple launcher jar that can bootstrap an OSGI application using apache felix.

Run the launcher like so:

`java -classpath felix.jar:org.apache.felix.framework-5.6.10.jar com.eriklievaart.felix.boot.Main [root]`

This will start apache felix (presuming all required files are present).

The launcher install all of the bundles located in: `[root]/bundle`

OSGI properties can be configured in the optional file: `[root]/osgi.properties`

There are 3 ways to specify the application root:
* if omitted, the root directory is the parent directory of the launcher jar. Convenient way to ship felix with an application.
* if an argument with a forward slash '/' is specified, this will be interpreted as an absolute path to the application root.
* if none of the above rules match, the application is expected to be located in ${user.home}/Applications/[arg]
