# CSD-Blockchained-ITDLP (Blockmess)

Nesta 2ª parte do trabalho (entrega a 21/07/2022) fui apenas eu que trabalhei:
- Henrique Campos Ferreira - 55065

## Configurações do programa e compilação

Configuração dos IPs e portas dos servidores ([hosts.config](https://github.com/henriquej-0904/CSD-BITDLP/blob/pa2-bitdlp-blockmess/Blockchain-IT-DLP/bft-smart/config/hosts.config)).

Por omissão o ficheiro de configuração [system.config](https://github.com/henriquej-0904/CSD-BITDLP/blob/pa2-bitdlp-blockmess/Blockchain-IT-DLP/bft-smart/config/system.config) resulta em N=4, F=1.

Também é possível ativar a configuração de N=7 e F=2 através do ficheiro [system-7-replicas.config](https://github.com/henriquej-0904/CSD-BITDLP/blob/pa2-bitdlp-blockmess/Blockchain-IT-DLP/bft-smart/config/system-7-replicas.config)

- Compilar e criar a imagem docker - [build.sh](https://github.com/henriquej-0904/CSD-BITDLP/blob/pa2-bitdlp-blockmess/Blockchain-IT-DLP/build.sh)
- Gerar configurações TLS (keystores e certificados das réplicas e truststore para o cliente)  - [create-config.sh](https://github.com/henriquej-0904/CSD-BITDLP/blob/pa2-bitdlp-blockmess/Blockchain-IT-DLP/create-config.sh) \<n_replicas\>
- Configurar o tipo de DB a utilizar, assim como configurações da blockchain, através do ficheiro [server-config.properties](https://github.com/henriquej-0904/CSD-BITDLP/blob/pa2-bitdlp-blockmess/Blockchain-IT-DLP/server-config.properties):
 
IN_MEMORY = Sem persistência, em memória || MONGO = Com persistência, utilizando o MongoDB.

## Execução do programa em múltiplas máquinas

- A cada máquina está associado um ID (0, 1, etc) que corresponde ao ID da réplica a executar. Nota: uma máquina pode executar mais do que uma réplica.
- Cada réplica deve ter os seus ficheiros de configuração: [blockmess](https://github.com/henriquej-0904/CSD-BITDLP/blob/pa2-bitdlp-blockmess/Blockchain-IT-DLP/blockmess), [tls-config](https://github.com/henriquej-0904/CSD-BITDLP/blob/pa2-bitdlp-blockmess/Blockchain-IT-DLP/tls-config) e [server-config.properties](https://github.com/henriquej-0904/CSD-BITDLP/blob/pa2-bitdlp-blockmess/Blockchain-IT-DLP/server-config.properties), assim como todos os scripts necessários à execução e paragem do serviço.
- Para iniciar e parar um conjunto de réplicas na mesma máquina juntamente com a base de dados, deve-se executar os scripts [run-all.sh](https://github.com/henriquej-0904/CSD-BITDLP/blob/pa2-bitdlp-blockmess/Blockchain-IT-DLP/run-all.sh) \<min_ID\> \<max_ID\> e [stop-all.sh](https://github.com/henriquej-0904/CSD-BITDLP/blob/pa2-bitdlp-blockmess/Blockchain-IT-DLP/stop-all.sh) \<min_ID\> \<max_ID\>, respectivamente.

## Execução do Workload (cliente)


- Para testar todas as operações do sistema pode-se executar um workload através do script - [run-workload.sh](https://github.com/henriquej-0904/CSD-BITDLP/blob/pa2-bitdlp-blockmess/Blockchain-IT-DLP/run-workload.sh) \<workload-config\> \<replicaId\> \<OPCIONAL: true para executar leituras\>.
- O workload pode ser configurado através de um ficheiro de configuração, exemplo em: [workload-config/default-config.properties](https://github.com/henriquej-0904/CSD-BITDLP/blob/pa2-bitdlp-blockmess/Blockchain-IT-DLP/workload-config/default-config.properties).
