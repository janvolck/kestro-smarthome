package be.kestro.smarthome.core.api;

public interface PowerManagement {

	void requestPower(String id);
	
	void releasePower(String id);
}
