package tp2.bitdlp.impl.srv;

import java.net.InetAddress;
import java.net.URI;

import javax.net.ssl.SSLContext;

import org.glassfish.jersey.jdkhttp.JdkHttpServerFactory;
import org.glassfish.jersey.server.ResourceConfig;

import bftsmart.tom.AsynchServiceProxy;
import bftsmart.tom.ServiceProxy;
import tp2.bitdlp.impl.srv.config.ServerConfig;
import tp2.bitdlp.impl.srv.resources.bft.bftsmart.AccountsResourceWithBFTSMaRt;
import tp2.bitdlp.impl.srv.resources.bft.bftsmart.ReplyWithSignatureComparator;


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
			int proxyId = replicaId*3254 + 10;
			int asyncProxyId = replicaId*3254 + 11;
			int port = Integer.parseInt(args[1]);

			ServerConfig.init(replicaId);

            AccountsResourceWithBFTSMaRt.setProxy(new ServiceProxy(proxyId));
			AccountsResourceWithBFTSMaRt.setAsyncProxy(new AsynchServiceProxy(asyncProxyId, null, new ReplyWithSignatureComparator(), null, null));

			AccountsResourceWithBFTSMaRt resource = new AccountsResourceWithBFTSMaRt();

			AccountsResourceWithBFTSMaRt.setReplica(resource.new BFTSMaRtServerReplica(replicaId));
			
            String ip = InetAddress.getLocalHost().getHostAddress();
			URI uri = new URI(String.format("https://%s:%d/rest", ip, port));

			ResourceConfig config = new ResourceConfig();
			config.register(resource);
            
			SSLContext sslContext = ServerConfig.getSSLContext();
			JdkHttpServerFactory.createHttpServer(uri, config, sslContext);

			//System.out.println("BFT SMaRt Server is running!!!");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
