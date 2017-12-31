package be.kestro.smarthome.devices.api;

public interface Doorbell {
	
	void ringDoorbell();
	
	void cancelDoorbell();
	
	boolean isRinging();
}
