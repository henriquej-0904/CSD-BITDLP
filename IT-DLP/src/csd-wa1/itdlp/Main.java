package itdlp;

import java.net.InetAddress;
import java.net.URI;

import org.glassfish.jersey.jdkhttp.JdkHttpServerFactory;
import org.glassfish.jersey.server.ResourceConfig;

public class Main
{
	public static final int PORT = 8080;

	public static void main(String[] args) {
		try
		{
			String ip = InetAddress.getLocalHost().getHostAddress();
			URI uri = new URI(String.format("http://%s:%s/rest", ip, PORT));

			ResourceConfig config = new ResourceConfig();
			//config.register(App.class);
            
			JdkHttpServerFactory.createHttpServer( uri, config);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
