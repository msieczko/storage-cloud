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
#include <sys/types.h>
#include <sys/wait.h>
#include <errno.h>

using namespace std;

#define max(a,b) (((a)>(b)) ? (a):(b))

volatile bool should_exit = false;
volatile int last_sig_pid = 0;

struct connection {
    pid_t pid;
    string addr;
    int port;
};

static void hdl (int sig, siginfo_t *siginfo, void *context)
{
    if(sig == SIGINT) {
        last_sig_pid = siginfo->si_pid;
    }
}

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

void process(int sock, int server_pid) {
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
    struct sigaction act;
    memset (&act, '\0', sizeof(act));
    act.sa_sigaction = &hdl;
    act.sa_flags = SA_RESTART | SA_SIGINFO;
    if (sigaction(SIGINT, &act, NULL) < 0) {
        perror ("sigaction");
    }
    
    int server_pid = getpid();
    if (signal(SIGTERM, sig_handler) == SIG_ERR)
        printf("\ncan't catch SIGTERM\n");
    
    int sock;
    unsigned int length;
    struct sockaddr_in server;
    int msgsock = -1;
    
    vector <connection> threads;

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
            if(errno == EINTR) {
                if(should_exit) {
                    break;
                } else {
                    if(last_sig_pid == getppid()) {
                        cout<<"There are "<<threads.size()<<" connections active:"<<endl;
                        for(int i = 0; i < threads.size(); i++) {
                            cout<<"["<<threads[i].pid<<"] "<<threads[i].addr<<":"<<threads[i].port<<endl;
                        }
                    }
                }
            } else {
                perror("select");
                break;
            }
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
                    connection tmp;
                    tmp.pid = pid;
                    tmp.addr = inet_ntoa(clientaddr.sin_addr);
                    tmp.port = (int) ntohs(clientaddr.sin_port);
                    threads.push_back(tmp);
                    close(msgsock);
                }
            }
        }
        
        int waitstatus;
        
        int died_pid = waitpid(-1, &waitstatus, WNOHANG);
        
        if(died_pid > 0) {
            for(int i=0; i<threads.size(); i++) {
                if(threads[i].pid == died_pid) {
                    threads.erase(threads.begin() + i);
                    cout<<"removed connection"<<endl;
                    break;
                }
            }
        }
        
    } while(!should_exit);

    cout<<"closing all connections "<<should_exit<<endl;

    for(int i = 0; i < threads.size(); i++) {
        kill(threads[i].pid, SIGTERM);
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
        
        if (cmd == "list") {
            kill(server_pid, SIGINT);
        }
        
        if (cmd == "help") {
            cout<<"Available commands:\n  exit - closes server\n  list - lists active connections"<<endl;
        }
    }

    kill(server_pid, SIGTERM);

    cout<<"server closed"<<endl;
    return 0;
}
