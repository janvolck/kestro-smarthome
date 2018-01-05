package be.kestro.smarthome.devices.impl.doorbell;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicReference;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.LineEvent;
import javax.sound.sampled.LineListener;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ConfigurationPolicy;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.Designate;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import be.kestro.io.core.api.InputEvent;
import be.kestro.io.core.api.InputEventListener;
import be.kestro.io.core.api.InputService;
import be.kestro.smarthome.core.api.PowerManagement;
import be.kestro.smarthome.devices.api.Doorbell;

@ObjectClassDefinition(name = "Kestro Smarthome Doorbell", description = "Configures a doorbell within Kestro Smarthome")
@interface DoorbellConfig {

	@AttributeDefinition(name = "Audio File Path", required = true, description = "Path to an audio file that will be played when the doorbell is rung.")
	String audioFile() default "";
}

@Component(configurationPolicy = ConfigurationPolicy.REQUIRE, service = { Doorbell.class }, immediate = true)
@Designate(ocd = DoorbellConfig.class)
public class DoorbellImpl implements Doorbell, InputEventListener {

	private Logger logger = LoggerFactory.getLogger(this.getClass());

	private AtomicReference<InputService> inputDoorbell = new AtomicReference<>();
	private AtomicReference<PowerManagement> powerManagement = new AtomicReference<>();

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

	@Reference(cardinality = ReferenceCardinality.MANDATORY, policy = ReferencePolicy.STATIC, unbind = "unsetInputDoorbell", target = "(name=doorbell)")
	public void setInputDoorbell(InputService inputDoorbell) {
		this.inputDoorbell.set(inputDoorbell);

		inputDoorbell.addInputEventListener(this);
	}

	public void unsetInputDoorbell(InputService inputDoorbell) {
		if (this.inputDoorbell.compareAndSet(inputDoorbell, null)) {
			inputDoorbell.removeInputEventListener(this);
		}
	}

	@Reference(cardinality = ReferenceCardinality.OPTIONAL, policy = ReferencePolicy.DYNAMIC, unbind = "unsetPowerManagement")
	public void setPowerManagement(PowerManagement powerManagement) {
		this.powerManagement.set(powerManagement);
	}

	public void unsetPowerManagement(PowerManagement powerManagement) {
		this.powerManagement.compareAndSet(powerManagement, null);
	}

	@Override
	public void ringDoorbell() {

		logger.info("Ringing doorbell [audioFile.exists={}]", audioFile.exists());
		
		if (audioFile.exists()) {
			PowerManagement powerManagement = this.powerManagement.get();
			if (powerManagement != null)
				powerManagement.requestPower("doorbell");

			
			Clip previousClip = this.clip.get();
			
			logger.debug(" ... is previous doorbell still running : {}",	previousClip == null ? false : previousClip.isActive());
			if (previousClip != null && previousClip.isActive())
				return;

			try {
				AudioInputStream stream = AudioSystem.getAudioInputStream(audioFile);
				Clip clip = AudioSystem.getClip();
				this.clip.set(clip);
				clip.addLineListener(new LineListener() {

					@Override
					public void update(LineEvent event) {
						
						logger.debug(" ... doorbell LineEvent: {}", event.getType());
						if (event.getType() == LineEvent.Type.STOP) {
							if (powerManagement != null)
								powerManagement.releasePower("doorbell");

							clip.close();
						}
					}
				});
				clip.open(stream);
				clip.start();
				
				logger.debug(" ... doorbell started");

			} catch (UnsupportedAudioFileException | IOException | LineUnavailableException e) {

				powerManagement.releasePower("doorbell");
				logger.error("Failed to ring doorbell.", e);
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

	@Override
	public void handleInputEventChanged(InputService service, InputEvent input) {

		logger.info("Received InputEvent from service {} [isHigh={}]", service.name(), input.isHigh());
		if (input.isHigh()) {
			ringDoorbell();
		}
	}

}
