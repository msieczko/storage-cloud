#include "main.h"
#include "messages.h"

using namespace std;
using namespace StorageCloud;

void calculateHash(uint8_t algo, const uint8_t buf[], int len, uint8_t** digest, uint16_t* digest_len) {
    if(algo == EncodedMessage::SHA512) {
        *digest_len = SHA512_DIGEST_LENGTH;
        *digest = new uint8_t[*digest_len];
        SHA512(buf, (size_t) len, *digest);
    } else if(algo == EncodedMessage::NOHASH) {
        *digest_len = 0;
        *digest = nullptr;
    } else {
        *digest_len = 0;
        cout<<"Error: unknown hashing algorithm"<<endl;
    }
}

bool compareHash(const uint8_t hash1[], const uint16_t hash1_len, const uint8_t hash2[], const uint16_t hash2_len) {
    if(hash1_len != hash2_len) {
        return false;
    }

    for(int i=0; i<hash1_len; i++) {
        if(hash1[i] != hash2[i]) {
            return false;
        }
    }

    return true;
}

string printHash(const uint8_t hash_type, const uint8_t hash[]) {

    string wyn;
    char buf[3];
    for(int i=0; i<HASH_SIZE[hash_type]; i++) {
        sprintf(buf, "%02x", hash[i]);
        wyn.push_back(buf[1]);
        wyn.push_back(buf[0]);
    }
    return wyn;
}

void encrypt(const uint8_t algo, const uint8_t in[], const uint32_t in_len, uint8_t** out, uint32_t* out_len) {
    if(algo == Handshake::CAESAR) {
        *out_len = in_len;
        *out = new uint8_t[*out_len];
        for (int i = 0; i < in_len; i++) {
            (*out)[i] = (uint8_t) (in[i] + 1);
        }
    } else if(algo == Handshake::NOENCRYPTION) {
        *out_len = in_len;
        *out = new uint8_t[*out_len];
        for (int i = 0; i < in_len; i++) {
            (*out)[i] = in[i];
        }
    } else {
        *out_len = 0;
        cout<<"Error: unknown encryption algorithm"<<endl;
    }
}

void decrypt(const uint8_t algo, const uint8_t in[], const uint32_t in_len, uint8_t** out, uint32_t* out_len) {
    if(algo == Handshake::CAESAR) {
        *out_len = in_len;
        *out = new uint8_t[*out_len];
        for (int i = 0; i < in_len; i++) {
            (*out)[i] = (uint8_t) (in[i] - 1);
        }
    } else if(algo == Handshake::NOENCRYPTION) {
        *out_len = in_len;
        *out = new uint8_t[*out_len];
        for (int i = 0; i < in_len; i++) {
            (*out)[i] = in[i];
        }
    } else {
        *out_len = 0;
        cout<<"Error: unknown encryption algorithm"<<endl;
    }
}

void processAndSend(uint8_t buf[], uint32_t len, int sock) {
    EncodedMessage msg;

    uint8_t* hash;
    uint16_t hash_len;
    uint8_t* out_buf;
    uint8_t* data; //max encrypted size
    uint32_t size=0;

    calculateHash(EncodedMessage::SHA512, buf, len, &hash, &hash_len);

//    SHA512(buf, 30, hash);

    encrypt(Handshake::CAESAR, buf, len, &data, &size);

    msg.set_hash((char*)hash, hash_len);
    msg.set_datasize(len);
    msg.set_data((char*)data, size);

    uint32_t siz = msg.ByteSize() + 4;

    if(siz > MAX_PACKET_SIZE - 4) {
        cout<<"Error: message too big"<<endl;
        delete hash;
        delete data;
        return;
    }

    out_buf = new uint8_t[siz];

    msg.SerializeToArray(out_buf + 4, siz - 4);

    cout<<"sending response with size: "<<siz<<" ("<<siz-4<<"+4)"<<endl;

    buf[3] = siz & 0xFF;
    buf[2] = (siz >> 8) & 0xFF;
    buf[1] = (siz >> 16) & 0xFF;
    buf[0] = (siz >> 24) & 0xFF;

    if (write(sock, out_buf, siz) == -1) {
        perror("writing on stream socket");
        delete out_buf;
        delete hash;
        delete data;
        return;
    }

    delete out_buf;
    delete hash;
    delete data;
}

void sendServerResponse(const ServerResponse& res, int sock) {
    uint8_t* buf = new uint8_t[res.ByteSize()];
    res.SerializeToArray(buf, res.ByteSize());
    processAndSend(buf, res.ByteSize(), sock);
}

void parseCommand(uint8_t buf[], uint32_t len, int sock) {
    Command cmd;

    cmd.ParseFromArray(buf, len);

    cout<<"received command '"<<Command::CommandType_Name(cmd.type())<<"' ("<<cmd.type()<<"), with "<<cmd.params_size()<<" params"<<endl;

    if(cmd.type() == Command::LOGIN) {
        ServerResponse res;
        res.set_type(ServerResponse::LOGGED);
        sendServerResponse(res, sock);
    }
}

void parseMessage(uint8_t buf[], int len, int sock) {
    EncodedMessage msg;
    msg.ParseFromArray(buf, len);
    cout<<"Parsing message"<<endl;
    cout<<"size: "<<msg.datasize()<<endl;
    cout<<"hash: "<<printHash(msg.hashalgorithm(), (uint8_t*) msg.hash().c_str())<<"("<<msg.hash().length()<<", "<<HASH_SIZE<<")"<<endl;
    cout<<"data: "<<msg.data()<<"("<<msg.data().length()<<")"<<endl;

    if(!msg.data().length() || msg.hash().length() != HASH_SIZE[msg.hashalgorithm()]) {
        cout<<"wrong data or hash length"<<endl;
        return;
    }

    uint8_t* data;

    uint32_t data_size = 0; //decrypted data size

    decrypt(Handshake::CAESAR, (uint8_t*) msg.data().c_str(), msg.datasize(), &data, &data_size);

    if(msg.datasize() != data_size) {
        cout<<"wrong data length"<<endl;
        return;
    }

    cout<<"decrypted data: "<<flush;

    for(int i=0; i<data_size; i++) {
        cout<<(char) data[i]<<flush;
    }
    cout<<endl;

    uint8_t* hash;
    uint16_t hash_size;

    calculateHash(EncodedMessage::SHA512, data, data_size, &hash, &hash_size);

    bool hash_ok = compareHash(hash, hash_size, (uint8_t*) msg.hash().c_str(), msg.hash().length());

    if(!hash_ok) {
        cout<<"wrong hash"<<endl;
        cout<<"should be "<<printHash(EncodedMessage::SHA512, (uint8_t*) msg.hash().c_str())<<endl;
        cout<<"got       "<<printHash(EncodedMessage::SHA512, hash)<<endl;
        return;
    }

    cout<<"hash ok"<<endl;

    parseCommand(data, data_size, sock);
}