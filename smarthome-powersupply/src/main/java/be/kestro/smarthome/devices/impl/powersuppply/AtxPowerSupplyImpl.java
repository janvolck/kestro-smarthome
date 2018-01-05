package be.kestro.smarthome.devices.impl.powersuppply;

import java.util.concurrent.atomic.AtomicReference;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;

import be.kestro.io.core.api.InputService;
import be.kestro.io.core.api.OutputService;
import be.kestro.smarthome.devices.api.PowerSupply;
import be.kestro.smarthome.devices.api.enums.PowerSupplyState;

@Component(service = { PowerSupply.class })
public class AtxPowerSupplyImpl implements PowerSupply {

	private AtomicReference<OutputService> outputPowerOn = new AtomicReference<>();
	private AtomicReference<InputService> inputPowerOk = new AtomicReference<>();

	@Reference(cardinality = ReferenceCardinality.MANDATORY, policy = ReferencePolicy.STATIC, unbind = "unsetOutputPowerOn", target="(name=PowerOn)")
	public void setOutputPowerOn(OutputService outputPowerOn) {
		this.outputPowerOn.set(outputPowerOn);
	}

	public void unsetOutputPowerOn(OutputService outputPowerOn) {
		this.outputPowerOn.compareAndSet(outputPowerOn, null);
	}
	
	@Reference(cardinality=ReferenceCardinality.MANDATORY, policy = ReferencePolicy.STATIC, unbind="unsetInputPowerOk", target="(name=PowerOk)")
	public void setInputPowerOk(InputService inputService) {
		this.inputPowerOk.set(inputService);
	}

	public void unsetInputPowerOk(InputService inputService) {
		this.inputPowerOk.compareAndSet(inputService, null);
	}
	
	@Override
	public void setState(PowerSupplyState state) {

		OutputService outputPowerOn = this.outputPowerOn.get();
		if (outputPowerOn != null)
			outputPowerOn.setState(state != PowerSupplyState.ON);
	}

	@Override
	public PowerSupplyState getState() {

		InputService inputPowerOk = this.inputPowerOk.get();
		if (inputPowerOk == null)
			return PowerSupplyState.OFF;
		
		return inputPowerOk.isHigh() ? PowerSupplyState.ON : PowerSupplyState.OFF;
	}

	@Override
	public boolean isOn() {

		InputService inputPowerOk = this.inputPowerOk.get();
		if (inputPowerOk == null)
			return false;
		
		return inputPowerOk.isHigh();
	}

	@Override
	public boolean isOff() {

		InputService inputPowerOk = this.inputPowerOk.get();
		if (inputPowerOk == null)
			return true;
		
		return inputPowerOk.isLow();
	}

	@Override
	public void switchOn() {

		OutputService outputPowerOn = this.outputPowerOn.get();
		if (outputPowerOn != null)
			outputPowerOn.setLow();
	}

	@Override
	public void switchOff() {

		OutputService outputPowerOn = this.outputPowerOn.get();
		if (outputPowerOn != null)
			outputPowerOn.setHigh();
	}

}
