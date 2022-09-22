package com.example.opcuademo.websocket.message;

import lombok.Data;

@Data
public class OpcUaResponseMessage {

	String nodeId;
	String value;
}
