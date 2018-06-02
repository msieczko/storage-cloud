#include <fstream>
#include "main.h"

#define HASH_SIZE SHA512_DIGEST_LENGTH

using namespace google::protobuf::io;

using namespace std;

void encrypt(const uint8_t in[], uint8_t out[], int len) {
    for(int i=0; i<len; i++) {
        out[i] = (uint8_t) (in[i]+1);
    }
}

int main(int argc, char *argv[])
{
    int sock;
    struct sockaddr_in server;
    struct hostent *hp, *gethostbyname(const char *);
    char buf[1024];

    /* Create socket. */
    sock = socket( AF_INET, SOCK_STREAM, 0 );
    if (sock == -1) {
        perror("opening stream socket");
        exit(1);
    }

    /* uzyskajmy adres IP z nazwy . */
    server.sin_family = AF_INET;
    hp = gethostbyname(argv[1] );

/* hostbyname zwraca strukture zawierajaca adres danego hosta */
    if (hp == (struct hostent *) 0) {
        fprintf(stderr, "%s: unknown host\n", argv[1]);
        exit(2);
    }
    memcpy((char *) &server.sin_addr, (char *) hp->h_addr,
        hp->h_length);
    server.sin_port = htons(atoi( argv[2]));
    if (connect(sock, (struct sockaddr *) &server, sizeof server)
        == -1) {
        perror("connecting stream socket");
        exit(1);
    }

    StorageCloud::EncodedMessage msg;

    StorageCloud::Handshake handshake;
    handshake.set_encryptionalgorithm(StorageCloud::CAESAR);
    uint8_t* hdata = new uint8_t[handshake.ByteSize()];
    handshake.SerializeToArray(hdata, handshake.ByteSize());
    uint8_t hash[HASH_SIZE];

    SHA512(hdata, handshake.ByteSize(), hash);

    msg.set_hash((char*)hash, HASH_SIZE);
    msg.set_datasize(handshake.ByteSize());
    msg.set_data((char*)hdata, handshake.ByteSize());
    msg.set_hashalgorithm(StorageCloud::H_SHA512);
    msg.set_type(StorageCloud::HANDSHAKE);

    msg.SerializeToArray(buf+4, 1020);

    uint32_t siz = msg.ByteSize() + 4;

    cout<<"handshake size: "<<siz<<" ("<<siz-4<<"+4)"<<endl;

    buf[3] = siz & 0xFF;
    buf[2] = (siz >> 8) & 0xFF;
    buf[1] = (siz >> 16) & 0xFF;
    buf[0] = (siz >> 24) & 0xFF;

    write( sock, buf, siz );

    uint8_t in_buf[1024];
    uint32_t rcv_siz = (uint32_t) recv(sock, in_buf, 1024, NULL);

    cout<<"received response size: "<<rcv_siz<<endl;

    //-------------------------------------

    sleep(1);

    StorageCloud::Command cmd;
    cmd.set_type(StorageCloud::LOGIN);
    StorageCloud::Param* tmp_param = cmd.add_params();
    tmp_param->set_paramid("username");
    tmp_param->set_sparamval("miloszXD");
    tmp_param = cmd.add_params();
    tmp_param->set_paramid("password");
    tmp_param->set_sparamval("nicepasswd");
    uint8_t* data = new uint8_t[cmd.ByteSize()];
    cmd.SerializeToArray(data, cmd.ByteSize());

    cout<<"cmd size: "<<cmd.ByteSize()<<endl;

    uint8_t* edata = new uint8_t[cmd.ByteSize()];

    SHA512(data, cmd.ByteSize(), hash);

    encrypt(data, edata, cmd.ByteSize());

    msg.set_hash((char*)hash, HASH_SIZE);
    msg.set_datasize(cmd.ByteSize());
    msg.set_data((char*)edata, cmd.ByteSize());
    msg.set_hashalgorithm(StorageCloud::H_SHA512);
    msg.set_type(StorageCloud::COMMAND);

    msg.SerializeToArray(buf+4, 1020);

    siz = msg.ByteSize() + 4;

    cout<<"size: "<<siz<<" ("<<siz-4<<"+4)"<<endl;

    buf[3] = siz & 0xFF;
    buf[2] = (siz >> 8) & 0xFF;
    buf[1] = (siz >> 16) & 0xFF;
    buf[0] = (siz >> 24) & 0xFF;

    if (write( sock, buf, siz ) == -1)

        perror("writing on stream socket");








    cmd.clear_params();
//    cmd.set_type(StorageCloud::LIST_FILES);
//    tmp_param = cmd.add_params();
//    tmp_param->set_paramid("path");
//    tmp_param->set_sparamval("/dir/dir23");

    cmd.set_type(StorageCloud::MKDIR);
    tmp_param = cmd.add_params();
    tmp_param->set_paramid("path");
    tmp_param->set_sparamval("/test_dirXD/dirXDD");
    delete data;
    data = new uint8_t[cmd.ByteSize()];
    cmd.SerializeToArray(data, cmd.ByteSize());

    cout<<"cmd size: "<<cmd.ByteSize()<<endl;

    delete edata;
    edata = new uint8_t[cmd.ByteSize()];

    SHA512(data, cmd.ByteSize(), hash);

    encrypt(data, edata, cmd.ByteSize());

    msg.set_hash((char*)hash, HASH_SIZE);
    msg.set_datasize(cmd.ByteSize());
    msg.set_data((char*)edata, cmd.ByteSize());
    msg.set_hashalgorithm(StorageCloud::H_SHA512);
    msg.set_type(StorageCloud::COMMAND);

    msg.SerializeToArray(buf+4, 1020);

    siz = msg.ByteSize() + 4;

    cout<<"size: "<<siz<<" ("<<siz-4<<"+4)"<<endl;

    buf[3] = siz & 0xFF;
    buf[2] = (siz >> 8) & 0xFF;
    buf[1] = (siz >> 16) & 0xFF;
    buf[0] = (siz >> 24) & 0xFF;

    if (write( sock, buf, siz ) == -1)

        perror("writing on stream socket");

//    if (write( sock, DATA, sizeof DATA ) == -1)
//
//        perror("writing on stream socket");
//
//    sleep(3);
//
//    write( sock, DATA2, sizeof DATA2 );
//

    rcv_siz = (uint32_t) recv(sock, in_buf, 1024, NULL);

    cout<<"received response size: "<<rcv_siz<<endl;







    uint8_t file_hash[SHA_DIGEST_LENGTH];
    SHA_CTX sha1;
    SHA1_Init(&sha1);
    const int bufSize = 32768;
    uint8_t* file_buffer = new uint8_t[bufSize];

    std::ifstream is ("/dev/shm/xd", ios::binary|ios::in);
    if(!is.is_open()) {
        return false;
    }

    is.seekg (0, is.end);
    uint64_t length = is.tellg();
    is.seekg (0, is.beg);

    cout<<"file size: "<<length<<endl;;

    int bytesRead = 0;

    do {
        is.read((char*) file_buffer, bufSize);
        if(is.bad()) {
            break;
        }
        cout<<"read size: "<<is.gcount()<<endl;;
        SHA1_Update(&sha1, file_buffer, is.gcount());
    } while(!is.eof() && is.good());

    int read_file_size = is.gcount();

    SHA1_Final(file_hash, &sha1);

    for(int i=0; i<SHA_DIGEST_LENGTH; i++){
        printf("%02x", (uint8_t) file_hash[i]);
    }
    std::cout<<std::endl;

//    if (write( sock, DATA, sizeof DATA ) == -1)
//
//        perror("writing on stream socket");
//
//    sleep(3);
//
//    write( sock, DATA2, sizeof DATA2 );
//

    rcv_siz = (uint32_t) recv(sock, in_buf, 1024, NULL);

    cout<<"received response size: "<<rcv_siz<<endl;







    cmd.clear_params();
//    cmd.set_type(StorageCloud::LIST_FILES);
//    tmp_param = cmd.add_params();
//    tmp_param->set_paramid("path");
//    tmp_param->set_sparamval("/dir/dir23");

    cmd.set_type(StorageCloud::METADATA);
    tmp_param = cmd.add_params();
    tmp_param->set_paramid("file_checksum");
    tmp_param->set_bparamval(file_hash, SHA_DIGEST_LENGTH);
    tmp_param = cmd.add_params();
    tmp_param->set_paramid("target_file_path");
    tmp_param->set_sparamval("/test_dirXD/test.xd");
    tmp_param = cmd.add_params();
    tmp_param->set_paramid("size");
    tmp_param->set_iparamval(length);
    delete data;
    data = new uint8_t[cmd.ByteSize()];
    cmd.SerializeToArray(data, cmd.ByteSize());

    cout<<"cmd size: "<<cmd.ByteSize()<<endl;

    delete edata;
    edata = new uint8_t[cmd.ByteSize()];

    SHA512(data, cmd.ByteSize(), hash);

    encrypt(data, edata, cmd.ByteSize());

    msg.set_hash((char*)hash, HASH_SIZE);
    msg.set_datasize(cmd.ByteSize());
    msg.set_data((char*)edata, cmd.ByteSize());
    msg.set_hashalgorithm(StorageCloud::H_SHA512);
    msg.set_type(StorageCloud::COMMAND);

    msg.SerializeToArray(buf+4, 1020);

    siz = msg.ByteSize() + 4;

    cout<<"size: "<<siz<<" ("<<siz-4<<"+4)"<<endl;

    buf[3] = siz & 0xFF;
    buf[2] = (siz >> 8) & 0xFF;
    buf[1] = (siz >> 16) & 0xFF;
    buf[0] = (siz >> 24) & 0xFF;

    if (write( sock, buf, siz ) == -1)

        perror("writing on stream socket");

//    if (write( sock, DATA, sizeof DATA ) == -1)
//
//        perror("writing on stream socket");
//
//    sleep(3);
//
//    write( sock, DATA2, sizeof DATA2 );
//

    rcv_siz = (uint32_t) recv(sock, in_buf, 1024, NULL);

    cout<<"received response size: "<<rcv_siz<<endl;











    cmd.clear_params();
//    cmd.set_type(StorageCloud::LIST_FILES);
//    tmp_param = cmd.add_params();
//    tmp_param->set_paramid("path");
//    tmp_param->set_sparamval("/dir/dir23");

    cmd.set_type(StorageCloud::USR_DATA);
    tmp_param = cmd.add_params();
    tmp_param->set_paramid("data");
    tmp_param->set_bparamval(file_buffer+2, read_file_size-2);
    delete data;
    data = new uint8_t[cmd.ByteSize()];
    cmd.SerializeToArray(data, cmd.ByteSize());

    cout<<"cmd size: "<<cmd.ByteSize()<<endl;

    delete edata;
    edata = new uint8_t[cmd.ByteSize()];

    SHA512(data, cmd.ByteSize(), hash);

    encrypt(data, edata, cmd.ByteSize());

    msg.set_hash((char*)hash, HASH_SIZE);
    msg.set_datasize(cmd.ByteSize());
    msg.set_data((char*)edata, cmd.ByteSize());
    msg.set_hashalgorithm(StorageCloud::H_SHA512);
    msg.set_type(StorageCloud::COMMAND);

    msg.SerializeToArray(buf+4, 1020);

    siz = msg.ByteSize() + 4;

    cout<<"size: "<<siz<<" ("<<siz-4<<"+4)"<<endl;

    buf[3] = siz & 0xFF;
    buf[2] = (siz >> 8) & 0xFF;
    buf[1] = (siz >> 16) & 0xFF;
    buf[0] = (siz >> 24) & 0xFF;

    if (write( sock, buf, siz ) == -1)

        perror("writing on stream socket");

//    if (write( sock, DATA, sizeof DATA ) == -1)
//
//        perror("writing on stream socket");
//
//    sleep(3);
//
//    write( sock, DATA2, sizeof DATA2 );
//

    rcv_siz = (uint32_t) recv(sock, in_buf, 1024, NULL);

    cout<<"received response size: "<<rcv_siz<<endl;

    close(sock);
    exit(0);
}
