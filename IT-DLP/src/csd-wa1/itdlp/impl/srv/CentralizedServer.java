package itdlp.impl.srv;

import java.net.InetAddress;
import java.net.URI;

import org.glassfish.jersey.jdkhttp.JdkHttpServerFactory;
import org.glassfish.jersey.server.ResourceConfig;

import itdlp.impl.srv.resources.AccountsResourceCentralized;

public class CentralizedServer
{
	public static final int PORT = 8080;

	public static void main(String[] args) {
		try
		{
			String ip = InetAddress.getLocalHost().getHostAddress();
			URI uri = new URI(String.format("http://%s:%s/rest", ip, PORT));

			ResourceConfig config = new ResourceConfig();
			config.register(AccountsResourceCentralized.class);
            
			JdkHttpServerFactory.createHttpServer( uri, config);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
