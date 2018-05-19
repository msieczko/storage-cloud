#ifndef SERVER_MAIN_H
#define SERVER_MAIN_H

#include <cerrno>
#include <csignal>
#include <cstdio>
#include <cstdlib>
#include <cstring>
#include <iostream>
#include <netdb.h>
#include <netinet/in.h>
#include <sys/shm.h>
#include <sys/socket.h>
#include <sys/wait.h>
#include <unistd.h>
#include <arpa/inet.h>
#include <sys/types.h>
#include <sys/epoll.h>
#include <thread>
#include <list>
#include <ctime>
#include <queue>
#include <mutex>
#include <condition_variable>
#include <termios.h>

#include <openssl/sha.h>
#include <openssl/md5.h>

#include <google/protobuf/message.h>
#include <google/protobuf/descriptor.h>
#include <google/protobuf/io/zero_copy_stream_impl.h>
#include <google/protobuf/io/coded_stream.h>
#include <google/protobuf/io/zero_copy_stream_impl_lite.h>

#include "protbuf/messages.pb.h"

#define MAX_CONNECTIONS 10

#define MAX_PACKET_SIZE 4096

#define DEFAULT_ENCRYPTION_ALGORITHM StorageCloud::EncryptionAlgorithm::NOENCRYPTION
#define DEFAULT_HASHING_ALGORITHM StorageCloud::HashAlgorithm::H_SHA512

struct connection {
    std::thread t;
    char addr[25];
    int port;
    StorageCloud::EncryptionAlgorithm encryption;
    StorageCloud::HashAlgorithm hash_algorithm;
    bool running;
};

#endif //SERVER_MAIN_H
