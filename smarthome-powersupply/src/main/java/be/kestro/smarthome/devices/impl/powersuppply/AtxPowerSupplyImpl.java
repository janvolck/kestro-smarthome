package be.kestro.smarthome.devices.impl.powersuppply;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import be.kestro.io.core.api.InputService;
import be.kestro.io.core.api.OutputService;
import be.kestro.smarthome.devices.api.PowerSupply;
import be.kestro.smarthome.devices.api.enums.PowerSupplyState;

@Component
public class AtxPowerSupplyImpl implements PowerSupply {

	@Reference
	private OutputService outputPowerOn;

	@Reference
	private InputService inputPowerOk;

	@Override
	public void setState(PowerSupplyState state) {

		outputPowerOn.setState(state == PowerSupplyState.ON);
	}

	@Override
	public PowerSupplyState getState() {

		return inputPowerOk.isHigh() ? PowerSupplyState.ON : PowerSupplyState.OFF;
	}

	@Override
	public boolean isOn() {

		return inputPowerOk.isHigh();
	}

	@Override
	public boolean isOff() {

		return inputPowerOk.isLow();
	}

	@Override
	public void switchOn() {

		outputPowerOn.setHigh();
	}

	@Override
	public void switchOff() {

		outputPowerOn.setLow();
	}

}
