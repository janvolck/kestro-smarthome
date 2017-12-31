package be.kestro.smarthome.devices.impl.doorbell;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicReference;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ConfigurationPolicy;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.Designate;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;

import be.kestro.io.core.api.InputService;
import be.kestro.smarthome.devices.api.Doorbell;

@ObjectClassDefinition(name = "Kestro Smarthome Doorbell", description = "Configures a doorbell within Kestro Smarthome")
@interface DoorbellConfig {

	@AttributeDefinition(name = "Audio File Path", required = true, description = "Path to an audio file that will be played when the doorbell is rung.")
	String audioFile() default "";
}

@Component(configurationPolicy = ConfigurationPolicy.REQUIRE, service = { Doorbell.class })
@Designate(ocd = DoorbellConfig.class)
public class DoorbellImpl implements Doorbell {

	@Reference
	private InputService inputDoorbell;
	private File audioFile;
	private AtomicReference<Clip> clip = new AtomicReference<>();

	@Activate
	public void activate(DoorbellConfig config) {

		audioFile = new File(config.audioFile());
	}
	
	@Deactivate
	public void deactivate() {
		Clip clip = this.clip.get();
		if (clip != null && clip.isActive()) {
			clip.stop();
			clip.close();
		}
	}

	@Override
	public void ringDoorbell() {

		if (audioFile.exists()) {
			try {
				AudioInputStream stream = AudioSystem.getAudioInputStream(audioFile);
				AudioFormat format = stream.getFormat();
				DataLine.Info info = new DataLine.Info(Clip.class, format);
				Clip clip = (Clip) AudioSystem.getLine(info);
				this.clip.set(clip);
				clip.open(stream);
				clip.start();

			} catch (UnsupportedAudioFileException e) {

				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {

				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (LineUnavailableException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	@Override
	public void cancelDoorbell() {

		Clip clip = this.clip.get();
		if (clip != null && clip.isActive()) {
			clip.stop();
			clip.close();
		}
	}

	@Override
	public boolean isRinging() {

		Clip clip = this.clip.get();
		if (clip != null && clip.isActive())
			return true;

		return false;
	}

}
