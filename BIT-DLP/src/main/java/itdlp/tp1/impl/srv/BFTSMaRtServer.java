package itdlp.tp1.impl.srv;

import java.net.InetAddress;
import java.net.URI;

import javax.net.ssl.SSLContext;

import org.glassfish.jersey.jdkhttp.JdkHttpServerFactory;
import org.glassfish.jersey.server.ResourceConfig;

import bftsmart.tom.ServiceProxy;
import itdlp.tp1.impl.srv.config.ServerConfig;
import itdlp.tp1.impl.srv.resources.bft.AccountsResourceWithBFTSMaRt;
import itdlp.tp1.impl.srv.resources.bft.AccountsResourceWithBFTSMaRt.BFTSMaRtServerReplica;


public class BFTSMaRtServer
{
	/**
	 * args[0] -> replicaId
	 * args[1] -> Server Port
	 * @param args
	 */
	public static void main(String[] args) {
		try
		{
			if (args.length != 2)
			{
				System.err.println("Invalid parameters:\nUsage: <replicaId> <bind port>");
				System.exit(1);
			}

			int replicaId = Integer.parseInt(args[0]);
			int proxyId = replicaId + 10;
			int port = Integer.parseInt(args[1]);

			ServerConfig.setReplicaId(replicaId);

            AccountsResourceWithBFTSMaRt.setProxy(new ServiceProxy(proxyId));
			AccountsResourceWithBFTSMaRt.setReplica(new BFTSMaRtServerReplica(replicaId));
			
            String ip = InetAddress.getLocalHost().getHostAddress();
			URI uri = new URI(String.format("https://%s:%d/rest", ip, port));

			ResourceConfig config = new ResourceConfig();
			config.register(AccountsResourceWithBFTSMaRt.class);
            
			SSLContext sslContext = ServerConfig.getSSLContext();
			JdkHttpServerFactory.createHttpServer(uri, config, sslContext);

			//System.out.println("BFT SMaRt Server is running!!!");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
