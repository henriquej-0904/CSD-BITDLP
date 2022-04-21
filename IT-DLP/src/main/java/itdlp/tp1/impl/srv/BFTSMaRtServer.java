package itdlp.tp1.impl.srv;

import java.net.InetAddress;
import java.net.URI;

import org.glassfish.jersey.jdkhttp.JdkHttpServerFactory;
import org.glassfish.jersey.server.ResourceConfig;

import bftsmart.tom.ServiceProxy;
import itdlp.tp1.impl.srv.resources.bft.AccountsResourceWithBFTSMaRt;
import itdlp.tp1.impl.srv.resources.bft.AccountsResourceWithBFTSMaRt.BFTSMaRtServerReplica;


public class BFTSMaRtServer
{
	/**
	 * args[0] -> replicaId
	 * args[1] -> proxyId
	 * args[2] -> Server Port
	 * @param args
	 */
	public static void main(String[] args) {
		try
		{
			try {
				ServiceProxy proxy = new ServiceProxy(Integer.parseInt(args[1]));
            	AccountsResourceWithBFTSMaRt.setProxy(proxy);

				AccountsResourceWithBFTSMaRt.setReplica(
				new BFTSMaRtServerReplica(Integer.parseInt(args[0])));		
			} catch (Exception e) {}
			

            String ip = InetAddress.getLocalHost().getHostAddress();
			URI uri = new URI(String.format("http://%s:%s/rest", ip, args[2]));

			ResourceConfig config = new ResourceConfig();
			config.register(AccountsResourceWithBFTSMaRt.class);
            
			JdkHttpServerFactory.createHttpServer( uri, config);

			System.out.println("BFT SMaRt Server is running!!!");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
