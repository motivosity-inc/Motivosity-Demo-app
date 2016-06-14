package com.motivosity.model;

import java.util.List;

public class MvApiResponse {

	private boolean success;

	private Object response;

	private List<MvMessage> mvMessages;

	public boolean isSuccess() {
		return success;
	}

	public void setSuccess(boolean success) {
		this.success = success;
	}

	public Object getResponse() {
		return response;
	}

	public void setResponse(Object response) {
		this.response = response;
	}

	public List<MvMessage> getMvMessages() {
		return mvMessages;
	}

	public void setMvMessages(List<MvMessage> mvMessages) {
		this.mvMessages = mvMessages;
	}
}
