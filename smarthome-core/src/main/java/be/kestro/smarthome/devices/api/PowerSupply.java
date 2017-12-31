package be.kestro.smarthome.devices.api;

import be.kestro.smarthome.devices.api.enums.PowerSupplyState;

public interface PowerSupply {

	void setState(PowerSupplyState state);
	
	PowerSupplyState getState();
	
	boolean isOn();
	
	boolean isOff();
	
	void switchOn();
	
	void switchOff();
}
