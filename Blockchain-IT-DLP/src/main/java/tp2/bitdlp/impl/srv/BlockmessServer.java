package tp2.bitdlp.impl.srv;

import java.net.InetAddress;
import java.net.URI;

import javax.net.ssl.SSLContext;

import org.glassfish.jersey.jdkhttp.JdkHttpServerFactory;
import org.glassfish.jersey.server.ResourceConfig;

import tp2.bitdlp.impl.srv.config.ServerConfig;
import tp2.bitdlp.impl.srv.resources.blockmess.AccountsResourceWithBlockmess;


public class BlockmessServer
{
	/**
	 * args[0] -> replicaId
	 * args[1] -> Server Port
	 * args[2] -> Blockmess Port
	 * @param args
	 */
	public static void main(String[] args) {
		try
		{
			if (args.length != 3)
			{
				System.err.println("Invalid parameters:\nUsage: <replicaId> <bind port> <Blockmess port>");
				System.exit(1);
			}

			int replicaId = Integer.parseInt(args[0]);
			int port = Integer.parseInt(args[1]);

			String ip = InetAddress.getLocalHost().getHostAddress();
			int blockmessPort = Integer.parseInt(args[2]);

			ServerConfig.init(replicaId);

			AccountsResourceWithBlockmess resource = new AccountsResourceWithBlockmess();

			AccountsResourceWithBlockmess.setReplica(resource.new BlockmessServerReplica(replicaId, blockmessPort));
            
			URI uri = new URI(String.format("https://%s:%d/rest", ip, port));

			ResourceConfig config = new ResourceConfig();
			config.register(resource);
            
			SSLContext sslContext = ServerConfig.getSSLContext();
			JdkHttpServerFactory.createHttpServer(uri, config, sslContext);

			System.out.println("Blockmess is running on " + uri.toString());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
