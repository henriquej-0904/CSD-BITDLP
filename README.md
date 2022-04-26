# CSD-ITDLP

Grupo:
- Henrique Campos Ferreira - 55065
- Pedro Madeira - 52464

## Configurações do programa e compilação

- Configurar bft-smart (Novas configurações do bft-smart só ficam ativas depois de se executar [build.sh](https://github.com/pvpmadeira/CSD-ITDLP/blob/main/IT-DLP/build.sh)):
 
Por omissão o ficheiro de configuração [system.config](https://github.com/pvpmadeira/CSD-ITDLP/blob/main/IT-DLP/bft-smart/config/system.config) resulta em N=4, F=1.

Também é possível ativar a configuração de N=7 e F=2 através do ficheiro [system-7-replicas.config](https://github.com/pvpmadeira/CSD-ITDLP/blob/main/IT-DLP/bft-smart/config/system-7-replicas.config)
- Compilar e criar a imagem docker - [build.sh](https://github.com/pvpmadeira/CSD-ITDLP/blob/main/IT-DLP/build.sh)
- Gerar configurações TLS (keystores e certificados das réplicas e truststore para o cliente)  - [create-config.sh](https://github.com/pvpmadeira/CSD-ITDLP/blob/main/IT-DLP/create-config.sh) \<n_replicas\>
- Configurar o tipo de DB a utilizar, através do ficheiro [db-config.properties](https://github.com/pvpmadeira/CSD-ITDLP/blob/main/IT-DLP/db-config.properties):
 
IN_MEMORY = Sem persistência, em memória || MONGO = Com persistência, utilizando o MongoDB.

## Execução do programa

- Para inicializar os servidores Mongo DB (um por réplica) deve-se executar o script - [run-mongo-db.sh](https://github.com/pvpmadeira/CSD-ITDLP/blob/main/IT-DLP/run-mongo-db.sh) \<n_replicas\>.
- Iniciar os servidores - [run-bft-servers.sh](https://github.com/pvpmadeira/CSD-ITDLP/blob/main/IT-DLP/run-bft-servers.sh) \<n_replicas\>.
- Para testar todas as operações do sistema (e também casos de erro: criar contas com o mesmo id, sendTransaction() com o mesmo nonce ou assinaturas do cliente inválidas, etc) pode-se executar um workload através do script - [run-workload.sh](https://github.com/pvpmadeira/CSD-ITDLP/blob/main/IT-DLP/run-workload.sh) \<replicaId\> \<nUsers\> \<nAccounts\>
- Para aceder aos logs de uma réplica deve-se executar:
```bash
# Os ids começam em 0.
docker logs -f replica-id
```
- Para terminar a execução dos servidores deve-se executar - [stop-bft-servers.sh](https://github.com/pvpmadeira/CSD-ITDLP/blob/main/IT-DLP/stop-bft-servers.sh) \<n_replicas\>
- Para terminar a execução das múltiplas instâncias do Mongo DB deve-se executar - [stop-mongo-db.sh](https://github.com/pvpmadeira/CSD-ITDLP/blob/main/IT-DLP/stop-mongo-db.sh) \<n_replicas\>
