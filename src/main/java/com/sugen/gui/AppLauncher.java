package com.sugen.gui;

import javax.swing.JApplet;

/**
 * Launcher for an AppMainWindow. Collects a bunch of very trivial details, for
 * the purpose of standardization as much as utility. All
 * subclasses should define a <pre>public static void main(String[] argv)</pre>
 * method, which should call <pre>run()</pre>.
 *
 * <P>Designed to work as an applet, as a
 * stand-alone application, or as a process spawned by another Java program.
 *
 * @author Jonathan Bingham
 */
abstract public class AppLauncher
    extends JApplet implements Runnable {
    /** @serial */
    protected boolean isApplet = false;
    /** @serial */
    protected AppMainWindow mainWindow;

    public void init() {
        isApplet = true;
    }

    public boolean isApplet() {
        return isApplet;
    }

    public void start() {
        run();
    }

    /**
     * Threadable way to launch the app.
     *
     * <P>Separate thread allows call from another app
     * via new Thread(this).run(). In that case, you may want to also call
     * setRootThread(false); otherwise, exiting this app will exit the VM.
     *
     * <P>Call run() directly to spawn no new thread.
     */
    abstract public void run();

    public AppMainWindow getMainWindow() {
        return mainWindow;
    }

    public void setMainWindow(AppMainWindow app) {
        mainWindow = app;
        if(isApplet)
            app.setApplet(this);
    }
}
