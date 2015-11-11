package threads;
import java.net.*;
import java.io.*;
import java.util.*;

import models.ClientInfo;

public class Servidor {
	
	private static Servidor servidorObj;
	
	private Map<String, ClientInfo> mapClientes;
	public boolean stopAll = false;
	public static ServerSocket servidor;
	
	private Servidor()
	{
		// Inicializa lista de clientes
		mapClientes = new HashMap<String, ClientInfo>();
	}
	
	// Metodo Singleton
	public static Servidor GetServidor()
	{
		if(servidorObj == null)
		{
			servidorObj = new Servidor();
		}
		
		return servidorObj;
	}
	
	public void Start ()
	{
		try
		{	
			// Abrindo socket para comunica��o com clientes
			servidor = new ServerSocket(12345);
			
			System.out.println("Porta 12345 aberta!");
			
			// Enquanto nenhum cliente interromper
			while (!stopAll) 
			{
				// Aguardando conex�o com cliente
				Socket clienteSocket    = servidor.accept();
				
				// Setando informacoes do usuario
				ClientInfo clientInfo   = new ClientInfo ();
				clientInfo.ClientSocket = clienteSocket;
				clientInfo.ClientStream = new PrintStream(clienteSocket.getOutputStream());
							
				// Cria objeto da Thread para comunicacao com o cliente
				ClienteComunication clientCommunication = new ClienteComunication (clientInfo);
				Thread threadClientCommunication        = new Thread(clientCommunication);
				
				// Set Thread on client info
				clientInfo.ThreadClientCommunication = threadClientCommunication;
				
				// Mapeando cliente em um dicionario 
				mapClientes.put(clienteSocket.getRemoteSocketAddress().toString(), clientInfo);
				
				// Dispara uma thread que trata esse cliente e j� espera o pr�ximo
				clientInfo.ThreadClientCommunication.start();
			}
								
			// Fechando conex�o
			servidor.close();		
			
			System.out.println("O servidor terminou de executar!");
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	
	public void TrataMenssagem(ClientInfo origem, String menssagem)
	{
		// Inicializa lista com os clientes de destino (todos menos a origem)
		List<ClientInfo> destinos = new ArrayList<ClientInfo> ();
		
		for(Map.Entry<String, ClientInfo> cliente : mapClientes.entrySet())
		{
			if(cliente.getValue() != origem)
			{
				destinos.add(cliente.getValue());
			}
		}
		
		//Por hora somente distribui a menssagem, no futuro teremos que tratar a menssagem e decidir o que distribuir e para quem
		DistribuiMenssagem(destinos, menssagem);
	}
	
	public void DistribuiMenssagem(List<ClientInfo> destinos, String menssagem)
	{
		for(ClientInfo cliente : destinos)
		{
			cliente.ClientStream.println(menssagem);
		}
	}
	
}