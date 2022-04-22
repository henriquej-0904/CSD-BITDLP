package itdlp.tp1.impl.srv;

import java.io.File;
import java.net.InetAddress;
import java.net.URI;
import java.security.KeyStore;

import javax.net.ssl.SSLContext;

import org.glassfish.jersey.jdkhttp.JdkHttpServerFactory;
import org.glassfish.jersey.server.ResourceConfig;

import bftsmart.tom.ServiceProxy;
import itdlp.tp1.impl.srv.resources.bft.AccountsResourceWithBFTSMaRt;
import itdlp.tp1.impl.srv.resources.bft.AccountsResourceWithBFTSMaRt.BFTSMaRtServerReplica;
import itdlp.tp1.util.Crypto;


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
			int replicaId = Integer.parseInt(args[0]);
			int proxyId = Integer.parseInt(args[1]);
			int port = Integer.parseInt(args[2]);

            AccountsResourceWithBFTSMaRt.setProxy(new ServiceProxy(proxyId));
			AccountsResourceWithBFTSMaRt.setReplica(new BFTSMaRtServerReplica(replicaId));
			
            String ip = InetAddress.getLocalHost().getHostAddress();
			URI uri = new URI(String.format("https://%s:%d/rest", ip, port));

			ResourceConfig config = new ResourceConfig();
			config.register(AccountsResourceWithBFTSMaRt.class);
            
			SSLContext sslContext = getSSLContext(replicaId);
			JdkHttpServerFactory.createHttpServer(uri, config, sslContext);

			//System.out.println("BFT SMaRt Server is running!!!");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static SSLContext getSSLContext(int replicaId)
	{
		File configFolder = new File("tls-config");
		File replicaConfigFolder = new File(configFolder, "replica-" + replicaId);

		KeyStore keystore = Crypto.getKeyStorePkcs12(new File(replicaConfigFolder, "keystore.pkcs12"), Crypto.KEYSTORE_PWD);
		KeyStore truststore = Crypto.getKeyStorePkcs12(new File(configFolder, "truststore.pkcs12"), Crypto.KEYSTORE_PWD);
		return Crypto.getSSLContext(keystore, truststore, Crypto.KEYSTORE_PWD);
	}
}
