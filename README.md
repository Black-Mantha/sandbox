# Sandbox

This package has code that allows a Java program to run certain other Java programs called applets. Those applets will have limited permissions, preventing untrusted code from causing damage or stealing information.

## Using the Project

Applets are limited by 2 things; firstly, they have a special [ClassLoader](https://docs.oracle.com/javase/7/docs/api/java/lang/ClassLoader.html) which specifies which classes are available to applets. By default, this is limited to their own classes and those from the `java.lang` and `nl.blackmantha.sandbox.pub` packages.

The second restriction is a [SecurityManager](https://docs.oracle.com/javase/7/docs/api/java/lang/SecurityManager.html), which limits the actions applets can perform with the library classes.

There are a number of known weaknesses of the sandbox:

- There is no way to limit the memory or processor time the applet uses, beyond setting limits for the whole Java VM.
- It might not be possible to force-close an Applet without shutting down the whole Java VM.
- This is not a professional product and is provided with no guarantees; it should not be used to protect important systems/data.

Although the Sandbox project has a Main class, it needs an applet to run. How you use the project depends on whether you're writing an applet or writing the host program. In both cases, the Sandbox project can be used as a module or, after compiling, as an external library.

### Writing Applets

Write a class extending `nl.blackmantha.sandbox.pub.Applet`. The entry point for the Applet is its `init()` method.

Add a text file called `applet.ini` and add it as a resource to your project under the path `nl/blackmantha/sandbox/pub`. This file must have the following line, pointing to your implementation of the `Applet` class:

```
Main-Class: <fully-qualified class name of your Applet>
```

Set the Java entry point to `nl.blackmantha.sandbox.Main`.

When exporting your Applet, do not include the Sandbox project. Those classes will be provided by whatever program will be hosting your Applet.

### Writing Applet Hosts

When you want to start an Applet in your program, you will first need an `ISandboxBytecodeSource`. This will probably be one of the build-in classes:

* `JarBytecodeSource` - reads from a .jar file.
* `ResourceBytecodeSource` - for when you have a test Applet included in the project.
* `DirectoryBytecodeSource` - reads from a directory tree with .class files.

You should have a `SwitchableSecurityManager` active, to limit the applet's actions.

```
System.setSecurityManager(SwitchableSecurityManager.getInstance());
```

Next, you will need to create a `ISandboxConnector`. This is an interface for calls from the sandbox to the host. Note that its methods will be run in a sandboxed thread, and you might need to use the `SwitchableSecurityManager` to temporarily obtain full rights.

Now you can create a `Sandbox` object, which is a child class of [Thread](https://docs.oracle.com/javase/7/docs/api/java/lang/Thread.html), and start it.

For an example, see the `nl.blackmantha.sandbox.Main` class.

You should include the Sandbox project when exporting your program.

## Acknowledgments

* Inspired by the discussion on https://stackoverflow.com/questions/1715036/how-do-i-create-a-java-sandbox
