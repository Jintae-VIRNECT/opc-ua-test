package com.example.opcuademo.common;

import java.util.List;
import java.util.Optional;

import org.eclipse.milo.opcua.sdk.client.OpcUaClient;
import org.eclipse.milo.opcua.sdk.client.api.UaClient;
import org.eclipse.milo.opcua.sdk.client.api.config.OpcUaClientConfig;
import org.eclipse.milo.opcua.sdk.client.api.config.OpcUaClientConfigBuilder;
import org.eclipse.milo.opcua.stack.client.DiscoveryClient;
import org.eclipse.milo.opcua.stack.core.UaException;
import org.eclipse.milo.opcua.stack.core.types.builtin.LocalizedText;
import org.eclipse.milo.opcua.stack.core.types.structured.EndpointDescription;
import org.eclipse.milo.opcua.stack.core.util.EndpointUtil;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.core.env.Environment;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import sun.net.www.http.HttpClient;

import static org.eclipse.milo.opcua.stack.core.types.builtin.unsigned.Unsigned.*;

import com.example.opcuademo.config.properties.OpcUaProperties;

@Slf4j
@Component
public class OpcUaClientConnectionPool implements InitializingBean {
	public static OpcUaClient opcUaClient;

	private static OpcUaProperties opcUaProperties;


	@Autowired
	private OpcUaClientConnectionPool(OpcUaProperties opcUaProperties){
		OpcUaClientConnectionPool.opcUaProperties = opcUaProperties;
	}


	@Override
	public void afterPropertiesSet() throws Exception {

		connect();

	}

	@SneakyThrows
	private static void connect(){
		OpcUaClient client = createClient();
		OpcUaClientConfig config = client.getConfig();
		createClient().connect().get();


	}


	public static OpcUaClient createClient() {

		final String endpoint = String.format("opc.tcp://%s:%s", opcUaProperties.getHost(), 1234);
		System.out.println("endpoint = " + endpoint);

		try {

			List<EndpointDescription> endpointDescriptionList = DiscoveryClient.getEndpoints(endpoint).get();
			EndpointDescription EndpointDescription = EndpointUtil.updateUrl(endpointDescriptionList.get(0), "1234", 1234);


			OpcUaClientConfig config = OpcUaClientConfig.builder()
				.setEndpoint(EndpointDescription)
				.setRequestTimeout(uint(5000))
				.build();

			return OpcUaClient.create(config);

		}catch (Exception e){
			throw new RuntimeException("dpfj");
		}
	}

}
