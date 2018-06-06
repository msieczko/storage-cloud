#include "main.h"
#include "utils.h"
#include "Client.h"
#include "Logger.h"
#include "Database.h"
#include "User.h"

list<connection*> connections;

using namespace std;

using namespace StorageCloud;

bool should_exit = false;

Logger logger(&should_exit);
Database db(&logger);

void process(int sock, connection* conn) {

    int optval = 1;
    socklen_t optlen = sizeof(optval);

    setsockopt(sock, SOL_SOCKET, SO_KEEPALIVE, &optval, optlen);

    optval = 2;

    setsockopt(sock, IPPROTO_TCP, TCP_KEEPCNT, &optval, optlen);

    optval = 10;

    setsockopt(sock, IPPROTO_TCP, TCP_KEEPIDLE, &optval, optlen);

    optval = 5;

    setsockopt(sock, IPPROTO_TCP, TCP_KEEPINTVL, &optval, optlen);

    Client client(sock, conn, &should_exit, &logger);

    client.loop();

    logger.info("PROCESS", "closed process, fd was " + to_string(sock));

    close(sock);

    conn->running = false;
}

void server() {
    int sock;
    unsigned int length;
    struct sockaddr_in server;

    sock = socket(AF_INET, SOCK_STREAM, 0);
    int optval = 1;
    socklen_t optlen = sizeof(optval);
    setsockopt(sock, SOL_SOCKET, SO_REUSEADDR, &optval, optlen);

    if (sock == -1) {
        logger.err("server", "error while opening stream socket", errno);
        should_exit = true;
        return;
    }

    /* dowiaz adres do gniazda */

    server.sin_family = AF_INET;
    server.sin_addr.s_addr = INADDR_ANY;
    server.sin_port = htons(52137);
    if (bind(sock, (struct sockaddr *) &server, sizeof server) == -1) {
        logger.err("server", "error while binding stream socket", errno);
        should_exit = true;
        return;
    }

    /* wydrukuj na konsoli przydzielony port */
    length = sizeof(server);
    if (getsockname(sock,(struct sockaddr *) &server, &length) == -1) {
        logger.err("server", "getting socket name", errno);
        should_exit = true;
        return;
    }
    logger.info("server", "Socket port #" + to_string(ntohs(server.sin_port)));

    listen(sock, 50);

    fd_set set;
    struct timeval timeout;
    int rv;

    logger.log("SERVER", "My fd is " + to_string(sock));

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

            int msgsock = accept(sock, (struct sockaddr *) &clientaddr, &len);

            logger.log("SERVER", "accepted " + to_string(sock) + " to " + to_string(msgsock));

            if (msgsock == -1)
                logger.err("server", "error while accepting connection", errno);
            else {
                string conn(inet_ntoa(clientaddr.sin_addr));
                logger.info("server", "accepted connection from " + conn + ":" + to_string(ntohs(clientaddr.sin_port)));

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
            }
        }

        for(auto it=connections.begin(); it != connections.end();) {
            if(!((*it)->running)) {
                (*it)->t.join();
                delete (*it);
                it = connections.erase(it);
                logger.log("server", "connection removed");
            } else {
                it++;
            }
        }

    } while(!should_exit);

    logger.info("server", "closing all connections");

    for(auto it=connections.begin(); it != connections.end();) {
        (*it)->t.join();
        delete (*it);
        it = connections.erase(it);
    }

    close(sock);

    logger.info("server", "closed main server process");
}

int main(int argc, char **argv) {
    thread server_t = std::thread(server);

    string cmd;
    char c;

    logger.set_input_string(&cmd);

    UserManager u_m = UserManager::getInstance(&db, &logger);

    std::condition_variable g_cond;

    auto garbageCollector = u_m.startGarbageCollector(g_cond, should_exit);

    while(!should_exit) {
        c = getch();

        if(c == 10 || c == 13) {
            if (cmd == "exit") {
                cmd = "";
                logger.set_input_string(nullptr);
                logger.info("main", "closing app");
                should_exit = true;
                break;
            } else if (cmd == "list") {
                logger.info("main", "There are " + to_string(connections.size()) + " active connections");
                for (auto &connection : connections) {
                    string conn(connection->addr);
                    conn += ":" + to_string(connection->port);
                    logger.info("main", conn);
                }
            } else if (cmd == "help") {
                logger.info("main", "Available commands:\n  exit - closes server\n  list - lists active connections\n  users - list registered users");
            } else if (cmd == "users") {
                logger.info("main", "All users:");
                vector<UDetails> users;
                u_m.listAllUsers(users);

                for(auto& usr: users) {
                    logger.info("main/users", usr.username);
                    logger.info("main/users", "|-> " + usr.name);
                    logger.info("main/users", "|-> " + usr.surname);
                    logger.info("main/users", string("|-> ") + (usr.role == USER_ADMIN ? "admin" : "regular user"));
                    logger.info("main/users", "|-> total space:" + to_string(usr.totalSpace) + "B");
                    logger.info("main/users", "'-> used space: " + to_string(usr.usedSpace) + "B");
                }
            } else {
                logger.warn("main", "Unknown command, try help");
            }

            cmd = "";
        } else if(c == 127) {
            cmd.pop_back();
        } else {
            cmd += c;
        }

        logger.notify();
    }

    server_t.join();

    logger.info("main", "closing database connection");
    logger.info("main", "joining garbage collector");
    g_cond.notify_one();
    garbageCollector.join();
    logger.info("main", "bye!");
    return 0;
}
