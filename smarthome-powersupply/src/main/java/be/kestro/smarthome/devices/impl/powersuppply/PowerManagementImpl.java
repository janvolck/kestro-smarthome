package be.kestro.smarthome.devices.impl.powersuppply;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;


import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;

import be.kestro.smarthome.core.api.PowerManagement;
import be.kestro.smarthome.devices.api.PowerSupply;

@Component
public class PowerManagementImpl implements PowerManagement {

	private AtomicInteger requestCount = new AtomicInteger(0);
	private AtomicReference<PowerSupply> powerSupply = new AtomicReference<>();
	private Timer switchOffDelayTimer;

	@Activate
	public void activate() {
		switchOffDelayTimer = new Timer("PowerManagementTimer", true);

	}

	@Deactivate
	public void deactivate() {
		switchOffDelayTimer.cancel();

	}

	@Reference(cardinality=ReferenceCardinality.MANDATORY, policy=ReferencePolicy.STATIC, unbind="unsetPowerSupply")
	public void setPowerSupply(PowerSupply powerSupply) {
		this.powerSupply.set(powerSupply);
	}
	
	public void unsetPowerSupply(PowerSupply powerSupply) {
		this.powerSupply.compareAndSet(powerSupply, null);
	}

	
	@Override
	public void requestPower(String id) {

		PowerSupply powerSupply = this.powerSupply.get();
		if (powerSupply == null)
			return;
		
		requestCount.incrementAndGet();
		if (powerSupply.isOff()) {
			powerSupply.switchOn();
			
			requestCount.incrementAndGet();
			switchOffDelayTimer.schedule(new TimerTask() {

				@Override
				public void run() {
					releasePower("switchOffDelay");
				}
			}, 5 * 60 * 1000);
		}
	}

	@Override
	public void releasePower(String id) {

		PowerSupply powerSupply = this.powerSupply.get();
		if (powerSupply == null)
			return;
		
		int count = requestCount.decrementAndGet();
		if (count <= 0) {
			powerSupply.switchOff();
		}
	}
}
