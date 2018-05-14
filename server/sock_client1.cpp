#include "main.h"

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

    StorageCloud::Command cmd;
    cmd.set_type(StorageCloud::Command::LOGIN);
    StorageCloud::Param* tmp_param = cmd.add_params();
    tmp_param->set_paramid("param123");
    tmp_param->set_iparamval(1234567);
    uint8_t* data = new uint8_t[cmd.ByteSize()];
    cmd.SerializeToArray(data, cmd.ByteSize());

    cout<<"cmd size: "<<cmd.ByteSize()<<endl;

    StorageCloud::EncodedMessage msg;

    uint8_t hash[HASH_SIZE];

    uint8_t* edata = new uint8_t[cmd.ByteSize()];

    SHA512(data, cmd.ByteSize(), hash);

    encrypt(data, edata, cmd.ByteSize());

    msg.set_hash((char*)hash, HASH_SIZE);
    msg.set_datasize(cmd.ByteSize());
    msg.set_data((char*)edata, cmd.ByteSize());

    msg.SerializeToArray(buf+4, 1020);

    uint32_t siz = msg.ByteSize() + 4;

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
    close(sock);
    exit(0);
}
