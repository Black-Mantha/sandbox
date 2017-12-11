package nl.blackmantha.sandbox.pub;

/**
 * 
 * An abstract skeleton class for an Applet to be run in the sandbox.
 * 
 * @author github.com/Black-Mantha
 *
 */
public abstract class Applet implements IApplet {
	protected Applet()  { }

	protected ISandboxConnector connector;
	protected Applet(ISandboxConnector connector) {
		this.connector = connector;
	}

	@Override
	abstract public void init();
}
