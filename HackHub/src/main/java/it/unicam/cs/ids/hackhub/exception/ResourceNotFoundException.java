package it.unicam.cs.ids.hackhub.exception;

public class ResourceNotFoundException extends RuntimeException {

	public ResourceNotFoundException(String resourceName, Long resourceId) {
		super(resourceName + " not found with id " + resourceId);
	}

	public ResourceNotFoundException(String resourceName, String identifier) {
		super(resourceName + " not found with identifier " + identifier);
	}

	public ResourceNotFoundException(String message) {
		super(message);
	}

}
