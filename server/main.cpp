#include "main.h"
#include "utils.h"
#include "Client.h"

list<connection*> connections;

using namespace std;

using namespace StorageCloud;

bool should_exit = false;

void sig_handler(int signo)
{
    if (signo == SIGTERM) {
        printf("received SIGTERM %d\n", getpid());
        should_exit = true;
    }
    
    if (signo == SIGINT) {
        printf("received SIGUSR1\n");
    }
}

void process(int sock, connection* conn) {
    Client client(sock, conn, &should_exit);

    client.loop();

    cout<<"closed connection process "<<EncryptionAlgorithm_Name(conn->encryption)<<endl;

    close(sock);

    conn->running = false;
}

void server() {
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

                connection* new_connection = new connection;
                new_connection->encryption = DEFAULT_ENCRYPTION_ALGORITHM;
                new_connection->hash_algorithm = DEFAULT_HASHING_ALGORITHM;
                string tmp_addr;
                tmp_addr = inet_ntoa(clientaddr.sin_addr);
                tmp_addr.copy(new_connection->addr, tmp_addr.size());
                new_connection->addr[tmp_addr.size()] = 0;
                new_connection->port = (int) ntohs(clientaddr.sin_port);
                new_connection->running = true;

                connections.push_back(new_connection);

                new_connection->t = thread(process, msgsock, new_connection);
//                    close(msgsock); //needed?
            }
        }

        for(auto it=connections.begin(); it != connections.end();) {
            if(!((*it)->running)) {
                (*it)->t.join();
                delete (*it);
                it = connections.erase(it);
                cout<<"removed connection"<<endl;
            } else {
                it++;
            }
        }

    } while(!should_exit);

    cout<<"closing all connections"<<endl;

    for(auto it=connections.begin(); it != connections.end();) {
        (*it)->t.join();
        delete (*it);
        it = connections.erase(it);
    }

    close(sock);

    cout<<"closed main server process"<<endl;
}

int main(int argc, char **argv) {
    thread server_t = std::thread(server);

    string cmd;

    while(!should_exit) {
        cout<<"> "<<flush;
        cin>>cmd;

        if (cmd == "exit") {
            cout<<"closing server"<<endl;
            should_exit = true;
            break;
        }
        
        if (cmd == "list") {
            cout<<"There are "<<connections.size()<<" active connections"<<endl;
            for(auto it=connections.begin(); it != connections.end(); ++it) {
                cout<<(*it)->addr<<":"<<(*it)->port<<endl;
            }
        }
        
        if (cmd == "help") {
            cout<<"Available commands:\n  exit - closes server\n  list - lists active connections"<<endl;
        }
    }

    server_t.join();

    cout<<"server closed"<<endl;
    return 0;
}
