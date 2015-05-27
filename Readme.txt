Whisper.com Project 3
 Vikram Desai

How to run the program:

1.	java whisper.RecoveryImpl <hostname>
2.	java whisper.BackEndServer <dbname><RecoveryHost>
3.	java whisper.BackEndServer <dbname><SlaveHostname><RecoveryHost>
4.	java whisper.BackendDispatcherImpl <backEndMaster><backEndSlave>
5.	java whisper.FrontEndServer1 <frontEndServer1><backEndServerMaster>    
      <backEndServerSlave><backEndDispatcher>
6.	java whisper.FrontEndServer2 <frontEndServer2><backendMaster><backendSlave> <backEndDispatcher>
7.	 java whisper.dispatcher<frontEndServer1><frontEndServer2>
8.	java whisper.client <dispatcherhost> <username>
 	

Note : <hostname>
