#include "main.h"

#define MAX_CONNECTIONS 10

using namespace std;

volatile bool should_exit = false;

struct connection {
    pid_t pid;
    char addr[25];
    int port;
};

struct shm_data {
    connection connections[MAX_CONNECTIONS];
    int active_connections;
};

#define SHMSIZE sizeof(shm_data)

void sig_handler(int signo)
{
    if (signo == SIGTERM) {
        printf("received SIGTERM\n");
        should_exit = true;
    }
    
    if (signo == SIGINT) {
        printf("received SIGUSR1\n");
    }
}

void calculateHash(const uint8_t buf[], int len, uint8_t digest[]) {
    SHA512(buf, (size_t) len, digest);
}

bool compareHash(const uint8_t hash1[], const uint8_t hash2[]) {
    for(int i=0; i<HASH_SIZE; i++) {
        if(hash1[i] != hash2[i]) {
            return false;
        }
    }

    return true;
}

string printHash(const uint8_t hash[]) {
    string wyn;
    char buf[2];
    for(int i=0; i<HASH_SIZE; i++) {
        sprintf(buf, "%02x", hash[i]);
        wyn.push_back(buf[1]);
        wyn.push_back(buf[0]);
    }
    return wyn;
}

void encrypt(const uint8_t in[], const uint16_t in_len, uint8_t out[], uint16_t* out_len) {
    for(int i=0; i<in_len; i++) {
        out[i] = (uint8_t) (in[i]+1);
    }
    *out_len = in_len;
}

void decrypt(const uint8_t in[], const uint16_t in_len, uint8_t out[], uint16_t* out_len) {
    for(int i=0; i<in_len; i++) {
        out[i] = (uint8_t) (in[i]-1);
    }
    *out_len = in_len;
}

void parseMessage(uint8_t buf[], int len) {
    StorageCloud::EncodedMessage msg;
    msg.ParseFromArray(buf, len);
    cout<<"Parsing message"<<endl;
    cout<<"size: "<<msg.datasize()<<endl;
    cout<<"hash: "<<printHash((uint8_t*) msg.hash().c_str())<<"("<<msg.hash().length()<<", "<<HASH_SIZE<<")"<<endl;
    cout<<"data: "<<msg.data()<<"("<<msg.data().length()<<")"<<endl;

    if(!msg.data().length() || msg.hash().length() != HASH_SIZE) {
        cout<<"wrong data or hash length"<<endl;
        return;
    }

    uint8_t* data = new uint8_t [msg.datasize()];

    uint16_t data_size = 0; //decrypted data size

    decrypt((uint8_t*) msg.data().c_str(), msg.datasize(), data, &data_size);

    if(msg.datasize() != data_size) {
        cout<<"wrong data length"<<endl;
        return;
    }

    cout<<"decrypted data: "<<flush;

    for(int i=0; i<data_size; i++) {
        cout<<(char) data[i]<<flush;
    }
    cout<<endl;

    uint8_t hash[HASH_SIZE];

    calculateHash(data, data_size, hash);

    bool hash_ok = compareHash(hash, (uint8_t*) msg.hash().c_str());

    if(!hash_ok) {
        cout<<"wrong hash"<<endl;
        cout<<"should be "<<printHash((uint8_t*) msg.hash().c_str())<<endl;
        cout<<"got       "<<printHash(hash)<<endl;
        return;
    }
}

void process(int sock, int server_pid) {
    ssize_t n;
    uint8_t buffer[MAX_PACKET_SIZE];
    uint8_t size_buf[4];

    fd_set set;
    struct timeval timeout;
    int rv;

    while(!should_exit) {
        FD_ZERO(&set); /* clear the set */
        FD_SET(sock, &set); /* add our file descriptor to the set */
        timeout.tv_sec = 2;
        timeout.tv_usec = 0;
        rv = select(sock + 1, &set, nullptr, nullptr, &timeout);

        if (rv == -1) {
            break;
        } else if (rv != 0) {

            n = read(sock, size_buf, 4);

            if (n == 0) {
                printf("no new data, closing\n");
                break;
            }

            if (n < 0) {
                perror("ERROR reading from socket");
                break;
            }

            uint32_t size = ((size_buf[0]<<24)|(size_buf[1]<<16)|(size_buf[2]<<8)|(size_buf[3]));

            cout<<"Size: "<<size<<endl;

            if(size > MAX_PACKET_SIZE - 4) {
                cout<<"message too big"<<endl;
                break;
            }

            n = recv(sock, buffer, size - 4, MSG_WAITALL);

            if (n == size - 4) {
                cout<<"got all data ("<<n<<")"<<endl;

                parseMessage(buffer, size);
            }

//            for(int i = 0; i < n; i++) {
//                if (buffer[i] >= 'a' && buffer[i] <= 'z') {
//                    buffer[i] -= 32;
//                } else if (buffer[i] >= 'A' && buffer[i] <= 'Z') {
//                    buffer[i] += 32;
//                }
//            }



//            cout<<"Sending: "<<buffer<<endl;
//            n = write(sock, buffer, n);
//            cout<<"Sent "<<n<<" bytes"<<endl;
        }
    }

    cout<<"closed connection process"<<endl;

    close(sock);
}

void server(shm_data* shm) {
    int server_pid = getpid();
    if (signal(SIGTERM, sig_handler) == SIG_ERR)
        printf("\ncan't catch SIGTERM\n");
    
    int sock;
    unsigned int length;
    struct sockaddr_in server;
    int msgsock = -1;

    sock = socket(AF_INET, SOCK_STREAM, 0);
    if (sock == -1) {
        perror("opening stream socket");
        exit(1);
    }

    /* dowiaz adres do gniazda */

    server.sin_family = AF_INET;
    server.sin_addr.s_addr = INADDR_ANY;
    server.sin_port = htons(52137);
    if (bind(sock, (struct sockaddr *) &server, sizeof server) == -1) {
        perror("binding stream socket");
        exit(1);
    }

    /* wydrukuj na konsoli przydzielony port */
    length = sizeof(server);
    if (getsockname(sock,(struct sockaddr *) &server, &length) == -1) {
        perror("getting socket name");
        exit(1);
    }
    printf("Socket port #%d\n", ntohs(server.sin_port));

    listen(sock, 5);

    fd_set set;
    struct timeval timeout;
    int rv;

    do {
        FD_ZERO(&set); /* clear the set */
        FD_SET(sock, &set); /* add our file descriptor to the set */
        timeout.tv_sec = 2;
        timeout.tv_usec = 0;
        rv = select(sock + 1, &set, nullptr, nullptr, &timeout);

        if (rv == -1) {
            break;
        } else if (rv != 0) {
            struct sockaddr_in clientaddr;
            unsigned int len = sizeof(clientaddr);

            msgsock = accept(sock, (struct sockaddr *) &clientaddr, &len);

            if (msgsock == -1)
                perror("accept");
            else {
                printf("accepted connection from %s:%d\n", inet_ntoa(clientaddr.sin_addr),
                       (int) ntohs(clientaddr.sin_port));
                int pid = fork();

                if (pid < 0) {
                    perror("ERROR on fork");
                } else if (pid == 0) {
                    close(sock);
                    process(msgsock, server_pid);
                    exit(0);
                } else {
                    string tmp_addr;
                    connection* new_connection = &shm->connections[shm->active_connections];
                    new_connection->pid = pid;
                    tmp_addr = inet_ntoa(clientaddr.sin_addr);
                    tmp_addr.copy(new_connection->addr, tmp_addr.size());
                    new_connection->addr[tmp_addr.size()] = 0;
                    new_connection->port = (int) ntohs(clientaddr.sin_port);
                    shm->active_connections++;
                    close(msgsock);
                }
            }
        }
        
        int waitstatus;
        
        int died_pid = waitpid(-1, &waitstatus, WNOHANG);
        
        if(died_pid > 0) {
            bool found = false;
            for(int i=0; i<shm->active_connections; i++) {
                if (shm->connections[i].pid == died_pid) {
                    found = true;
                    shm->active_connections--;
                }
                if (found) {
                    shm->connections[i] = shm->connections[i+1];
                }
            }
            if(found) {
                cout<<"removed connection"<<endl;
            }
        }
        
    } while(!should_exit);

    cout<<"closing all connections "<<should_exit<<endl;

    for(int i = 0; i < shm->active_connections; i++) {
        kill(shm->connections[i].pid, SIGTERM);
    }

    close(sock);

    cout<<"closed main server process"<<endl;
}

int main(int argc, char **argv) {
    int shmid;
    shm_data *shm;

    shmid = shmget(2010, SHMSIZE, 0666 | IPC_CREAT);
    shm = (shm_data *) shmat(shmid, nullptr, 0);

    shm->active_connections = 0;

    int server_pid = fork();

    if (server_pid < 0) {
        perror("ERROR starting server");
    } else if (server_pid == 0) {
        server(shm);
        exit(0);
    }

    string cmd;

    while(true) {
        cout<<"> "<<flush;
        cin>>cmd;

        if (cmd == "exit") {
            cout<<"closing server"<<endl;
            break;
        }
        
        if (cmd == "list") {
            cout<<"There are "<<shm->active_connections<<" active connections"<<endl;
            for(int i=0; i<shm->active_connections; i++) {
                cout<<"["<<shm->connections[i].pid<<"] ";
                cout<<shm->connections[i].addr<<":";
                cout<<shm->connections[i].port<<endl;
            }
        }
        
        if (cmd == "help") {
            cout<<"Available commands:\n  exit - closes server\n  list - lists active connections"<<endl;
        }
    }

    kill(server_pid, SIGTERM);

    waitpid(server_pid, nullptr, 0);

    shmdt(shm);
    shmctl(shmid, IPC_RMID, nullptr);

    cout<<"server closed"<<endl;
    return 0;
}
