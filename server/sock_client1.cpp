#include "main.h"

#define DATA "Half a league, half a league . . ."

#define DATA2 "XDHalf a league, half a league . . .XDXDXDXDXDXDXDXDXDXDXDXDXDXDXDXDXDXDXDXDXDXDXDXDXDXDXDXDXDXDXDXDXDXDXDXDXDXDXDXDXDXDXDXDXDXDXDXDXDXDXDXDXDXDXDXDXDXD"

using namespace google::protobuf::io;

using namespace std;

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
    msg.set_hash("lel nice");
    msg.set_datasize(1234567890);
    msg.set_data("welp XD");

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
