package anaws.Proxy.ProxyObserver;

public class ObserverState {
	private int originalMID;
	private boolean negotiationState;
	
	public ObserverState(int originalMID, boolean negotiationState) {
		this.originalMID = originalMID;
		this.negotiationState = negotiationState;
	}

	public int getOriginalMID() {
		return originalMID;
	}

	public void setOriginalMID(int originalMID) {
		this.originalMID = originalMID;
	}

	public boolean isNegotiationState() {
		return negotiationState;
	}

	public void setNegotiationState(boolean negotiationState) {
		this.negotiationState = negotiationState;
	}
}
