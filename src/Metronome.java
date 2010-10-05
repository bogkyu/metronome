import java.io.*;
import java.util.Vector;
import javax.microedition.lcdui.*;
import javax.microedition.media.*;
import javax.microedition.midlet.*;
//import javax.microedition.media.TimeBase;

public class Metronome extends MIDlet
implements CommandListener
{
	private static final String lbl_Start = "Start" ;
	private static final String lbl_Stop  = "Stop" ;
	private Vector al_drum_setup ;
	private Form f ;

	private volatile boolean b_metronome_on = false ;

	public void startApp(){
		try {
			// GUI
			al_drum_setup = new Vector(10) ;
			al_drum_setup.addElement(new DrumElement("/n.wav", "audio/x-wav", 1000L)) ;
			Display display = Display.getDisplay(this);
			//Form 
			f = new Form("Metronome") ;
			Command startCommand = new Command(lbl_Start, Command.SCREEN, 0) ;
			Command stopCommand  = new Command(lbl_Stop , Command.SCREEN, 0) ;
			Command exitCommand  = new Command("Exit"   , Command.EXIT  , 0) ;
			f.addCommand(exitCommand);
			f.addCommand(startCommand);
			f.addCommand(stopCommand);
			f.setCommandListener(this);
			display.setCurrent(f) ;
		} catch(IOException ioe) {
			f.append(ioe.getMessage()) ;
		} catch(MediaException me) {
			f.append(me.getMessage()) ;
		}
	}

	public void pauseApp(){}

	public void destroyApp(boolean unconditional){}

	public void commandAction(Command c, Displayable s) { 
		if (c.getCommandType() == Command.EXIT) 
			notifyDestroyed(); 
    	
		String label = c.getLabel() ;
	    if( label.equals(lbl_Start)) {
			Thread t = new Thread(new MetronomeThread(al_drum_setup)) ;
			b_metronome_on = true ;
			t.start() ;
		} else if( label.equals(lbl_Stop)) {
			b_metronome_on = false ;
		}
	}

	private class DrumElement {
		Player m_p ;
		long duration ;
		
		public DrumElement(String filename, String media_descr, long time) throws MediaException , IOException {
			m_p = Manager.createPlayer(
				getClass().getResourceAsStream(filename)
				, media_descr) ;
			m_p.realize() ;
			m_p.prefetch() ;
			duration = time ;
		}
		
		public void play() {
			try {
				m_p.start() ;
			} catch (MediaException _) {
				f.append(_.getMessage()) ;
			}
		}
		public long time() { return duration ; }
	}

	private class MetronomeThread implements Runnable {
		Vector al_drums ;
		DrumElement [] list ;
		public MetronomeThread(Vector a) {
			// shallow clone
			list = new DrumElement[a.size()] ;
			for( int i=a.size() ; --i>=0 ; )
				list[i] = (DrumElement) a.elementAt(i) ;
		}

		public void run() {
			try {
			while ( b_metronome_on ) {
				for( int i=list.length; --i>=0 ; ) {
					list[i].play() ;
					f.append(".") ;
					Thread.sleep(list[i].time()) ;
				}
			}
			} catch (InterruptedException _) {
				f.append(_.getMessage()) ;
			}
		}
	}
}
