package com.lemo.client;

import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.transport.client.PreBuiltTransportClient;
import org.junit.After;
import org.junit.Before;

import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * @author 王兴岭
 * @create 2017-08-17 20:19
 */
public abstract class BaseTest {
  
  protected TransportClient client;

  @Before
  public void before() throws UnknownHostException {
    Settings settings = Settings.builder()
            .put("cluster.name", "my-application").build();

    client = new PreBuiltTransportClient(settings)
            .addTransportAddress(new InetSocketTransportAddress(InetAddress.getByName("192.168.50.130"), 9300));
  }

  @After
  public void after() {
    client.close();
  }
}
