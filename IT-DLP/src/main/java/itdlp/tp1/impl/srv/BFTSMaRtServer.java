package itdlp.tp1.impl.srv;

import java.net.InetAddress;
import java.net.URI;

import org.glassfish.jersey.jdkhttp.JdkHttpServerFactory;
import org.glassfish.jersey.server.ResourceConfig;

import bftsmart.tom.ServiceProxy;
import itdlp.tp1.impl.srv.resources.AccountsResourceWithBFTSMaRt;


public class BFTSMaRtServer
{
	public static final int PORT = 8080;

	public static void main(String[] args) {
		try
		{
            ServiceProxy proxy = new ServiceProxy(Integer.parseInt(args[0]));
            AccountsResourceWithBFTSMaRt.setProxy(proxy);
            
            String ip = InetAddress.getLocalHost().getHostAddress();
			URI uri = new URI(String.format("http://%s:%s/rest", ip, PORT));

			ResourceConfig config = new ResourceConfig();
			config.register(AccountsResourceWithBFTSMaRt.class);
            
			JdkHttpServerFactory.createHttpServer( uri, config);

			System.out.println("BFT SMaRt Server is running!!!");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
