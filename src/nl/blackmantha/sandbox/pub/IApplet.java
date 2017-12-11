package nl.blackmantha.sandbox.pub;

/**
 * 
 * Interface of Applet. This is loaded with the default classloader, so classes outside the sandbox can call its functions.
 * 
 * @author github.com/Black-Mantha
 *
 */
public interface IApplet {
	
	abstract public void init();
	
}
