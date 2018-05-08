#include <netinet/in.h>
#include <netdb.h>
#include <stdio.h>
#include <sys/types.h>
#include <unistd.h>
#include <sys/socket.h>
#include <stdlib.h>
#include <string.h>
#include <arpa/inet.h>
#include <iostream>
#include <signal.h>
#include <vector>

using namespace std;

#define max(a,b) (((a)>(b)) ? (a):(b))

volatile bool should_exit = false;

void sig_handler(int signo)
{
    if (signo == SIGTERM) {
        printf("received SIGTERM\n");
        should_exit = true;
    }
}

void process(int sock) {
    int n;
    char buffer[256];
    bzero(buffer, 256);

    fd_set set;
    struct timeval timeout;
    int rv;

    while(!should_exit) {
        FD_ZERO(&set); /* clear the set */
        FD_SET(sock, &set); /* add our file descriptor to the set */
        timeout.tv_sec = 2;
        timeout.tv_usec = 0;
        rv = select(sock + 1, &set, NULL, NULL, &timeout);

        if (rv == -1) {
            break;
        } else if (rv != 0) {
            bzero(buffer, 256);
            n = read(sock, buffer, 255);

            if (n == 0) {
                printf("no new data, closing\n");
                break;
            }

            if (n < 0) {
                perror("ERROR reading from socket");
                break;
            }

            cout<<"Message: "<<buffer<<endl;

            for(int i = 0; i < n; i++) {
                if (buffer[i] >= 'a' && buffer[i] <= 'z') {
                    buffer[i] -= 32;
                } else if (buffer[i] >= 'A' && buffer[i] <= 'Z') {
                    buffer[i] += 32;
                }
            }

            cout<<"Sending: "<<buffer<<endl;
            n = write(sock, buffer, n);
            cout<<"Sent "<<n<<" bytes"<<endl;
        }
    }

    cout<<"closed connection process"<<endl;

    close(sock);
}

void server() {
    if (signal(SIGTERM, sig_handler) == SIG_ERR)
        printf("\ncan't catch SIGTERM\n");

    int sock;
    unsigned int length;
    struct sockaddr_in server;
    int msgsock = -1;

    vector <pid_t> threads;

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
        rv = select(sock + 1, &set, NULL, NULL, &timeout);

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
                    process(msgsock);
                    exit(0);
                } else {
                    threads.push_back(pid);
                    close(msgsock);
                }
            }
        }
    } while(!should_exit);

    cout<<"closing all connections"<<endl;

    for(int i = 0; i < threads.size(); i++) {
        kill(threads[i], SIGTERM);
    }

    close(sock);

    cout<<"closed main server process"<<endl;
}

int main(int argc, char **argv) {
    int server_pid = fork();

    if (server_pid < 0) {
        perror("ERROR starting server");
    } else if (server_pid == 0) {
        server();
        exit(0);
    }

    string cmd;

    while(1) {
        cout<<"> "<<flush;
        cin>>cmd;

        if (cmd == "exit") {
            cout<<"closing server"<<endl;
            break;
        }
    }

    kill(server_pid, SIGTERM);

    cout<<"server closed"<<endl;
    return 0;
}