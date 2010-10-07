/*
 *  Metronome MIDlet program
 *  Copyright (C) 2010 Bogdan Creanga
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see http://www.gnu.org/licenses/
 *
 *  Contact author at: bogdancreanga@yahoo.com
*/
import java.io.*;
import java.util.Vector;
import javax.microedition.lcdui.*;
import javax.microedition.media.*;
import javax.microedition.midlet.*;
//import javax.microedition.media.TimeBase;

public class Metronome extends MIDlet
implements CommandListener
{
	private static final String lbl_Exit  = "Exit" ;
	private static final String lbl_Start = "Start" ;
	private static final String lbl_Stop  = "Stop" ;
	private Vector al_drum_setup ;
	private Form f = new Form("Metronome") ;
	Command startCommand = new Command(lbl_Start, Command.SCREEN, 0) ;
	Command stopCommand  = new Command(lbl_Stop , Command.SCREEN, 0) ;
	Command exitCommand  = new Command(lbl_Exit , Command.EXIT  , 0) ;
	TextField tick       = new TextField("Tick:", "", 5, TextField.NUMERIC) ;

	private volatile boolean b_metronome_on = false ;

	public Metronome() {
		al_drum_setup = new Vector(10) ;
	}
	public void startApp(){
		displayGUI() ;
	}
	public void pauseApp(){}

	public void destroyApp(boolean unconditional){
		notifyDestroyed() ;
	}
	private void displayGUI() {
		Display display = Display.getDisplay(this);
		f.addCommand(exitCommand) ;
		f.addCommand(startCommand) ;
		tick.setString("") ;
		tick.insert("60", 0 ) ;
		f.append(tick) ;
		f.setCommandListener(this) ;
		display.setCurrent(f) ;
	}
	public void commandAction(Command c, Displayable s) {
		if (c.getCommandType() == Command.EXIT)
			notifyDestroyed() ;

		String label = c.getLabel() ;
	    if( label.equals(lbl_Start)) {
			constructList() ;
			Thread t = new Thread(new MetronomeThread(al_drum_setup)) ;
			b_metronome_on = true ;
			t.start() ;
			f.removeCommand(startCommand) ;
			f.addCommand(stopCommand);
		} else if( label.equals(lbl_Stop)) {
			b_metronome_on = false ;
			f.removeCommand(stopCommand) ;
			f.addCommand(startCommand) ;
		}
	}
	private class DrumElement {
		Player m_p ;
		long duration ;

		public DrumElement(String filename, String media_descr, long time)
		throws MediaException, IOException {
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
	private class MetronomeThread
	implements Runnable {
		Vector al_drums ;
		DrumElement [] list ;
		public MetronomeThread(Vector a) {
			list = new DrumElement[a.size()] ;
			for( int i=a.size() ; --i>=0 ; )
				list[i] = (DrumElement) a.elementAt(i) ;
		}

		public void run() {
			try {
			while ( b_metronome_on ) {
				for( int i=list.length; --i>=0 ; ) {
					list[i].play() ;
					Thread.sleep(list[i].time()) ;
				}
			}
			} catch (InterruptedException _) {
				f.append(_.getMessage()) ;
				_.printStackTrace() ;
			}
		}
	}
	private void constructList() {
		try {
			long t = Integer.parseInt(tick.getString()) ;
			if( t <= 0L || 250L < t ) {
				t = 60L ;
				tick.setString("") ;
				tick.insert("" + t, 0 ) ;
			}
			t = 60000L / t ;
			al_drum_setup.removeAllElements() ;
			al_drum_setup.addElement(new DrumElement("/n.wav" , "audio/x-wav", t)) ;
			al_drum_setup.addElement(new DrumElement("/n1.wav", "audio/x-wav", t)) ;
			al_drum_setup.addElement(new DrumElement("/n2.wav", "audio/x-wav", t)) ;
		} catch(IOException ioe) {
			ppp(ioe.getMessage()) ;
		} catch(MediaException me) {
			ppp(me.getMessage()) ;
		}
	}
	private void ppp(String s) {
		System.out.println(s);
	}
}
