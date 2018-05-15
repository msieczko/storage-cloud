#include "main.h"
#include "messages.h"

using namespace std;
using namespace StorageCloud;

void calculateHash(uint8_t algo, const uint8_t buf[], int len, uint8_t** digest, uint16_t* digest_len) {
    if(algo == HashAlgorithm::H_SHA512) {
        *digest_len = SHA512_DIGEST_LENGTH;
        *digest = new uint8_t[*digest_len];
        SHA512(buf, (size_t) len, *digest);
    } else if(algo == HashAlgorithm::H_NOHASH) {
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
    if(algo == EncryptionAlgorithm::NOENCRYPTION) {
        *out_len = in_len;
        *out = new uint8_t[*out_len];
        for (int i = 0; i < in_len; i++) {
            (*out)[i] = in[i];
        }
    } else if(algo == EncryptionAlgorithm::CAESAR) {
        *out_len = in_len;
        *out = new uint8_t[*out_len];
        for (int i = 0; i < in_len; i++) {
            (*out)[i] = (uint8_t) (in[i] + 1);
        }
    } else {
        *out_len = 0;
        cout<<"Error: unknown encryption algorithm"<<endl;
    }
}

void decrypt(const uint8_t algo, const uint8_t in[], const uint32_t in_len, uint8_t** out, uint32_t* out_len) {
     if(algo == EncryptionAlgorithm::NOENCRYPTION) {
         *out_len = in_len;
         *out = new uint8_t[*out_len];
         for (int i = 0; i < in_len; i++) {
             (*out)[i] = in[i];
         }
     } else if(algo == EncryptionAlgorithm::CAESAR) {
         *out_len = in_len;
         *out = new uint8_t[*out_len];
         for (int i = 0; i < in_len; i++) {
             (*out)[i] = (uint8_t) (in[i] - 1);
         }
     } else {
         *out_len = 0;
         cout<<"Error: unknown encryption algorithm"<<endl;
     }
}

bool prepareDataToSend(UserCtx* user_ctx, uint8_t in_buf[], uint32_t len, uint8_t** out_buf, uint32_t* out_len) {
    EncodedMessage msg;

    uint8_t* hash = nullptr;
    uint16_t hash_len;
    uint8_t* data = nullptr;
    uint32_t size=0;

    calculateHash(user_ctx->hashing_algorithm, in_buf, len, &hash, &hash_len);

    encrypt(user_ctx->encryption_algorithm, in_buf, len, &data, &size);

    msg.set_hash((char*)hash, hash_len);
    msg.set_datasize(len);
    msg.set_data((char*)data, size);
    msg.set_type(MessageType::SERVER_RESPONSE);
    msg.set_hashalgorithm(user_ctx->hashing_algorithm);

    *out_len = msg.ByteSize() + 4;

    if(*out_len > MAX_PACKET_SIZE - 4) {
        cout<<"Error: message too big"<<endl;
        delete hash;
        delete data;
        return false;
    }

    *out_buf = new uint8_t[*out_len];

    msg.SerializeToArray(out_buf + 4, *out_len - 4);

    cout<<"sending response with size: "<<*out_len<<" ("<<*out_len-4<<"+4)"<<endl;

    (*out_buf)[3] = *out_len & 0xFF;
    (*out_buf)[2] = (*out_len >> 8) & 0xFF;
    (*out_buf)[1] = (*out_len >> 16) & 0xFF;
    (*out_buf)[0] = (*out_len >> 24) & 0xFF;

    delete hash;
    delete data;
    return true;
}

bool prepareServerResponse(const ServerResponse* res, UserCtx* user_ctx, uint8_t** out_data, uint32_t* out_data_len) {
    uint32_t data_len = res->ByteSize();
    uint8_t* data = new uint8_t[data_len];
    res->SerializeToArray(data, data_len);
    bool ready = prepareDataToSend(user_ctx, data, data_len, out_data, out_data_len);
    delete data;
    return ready;
}

bool processCommand(Command* cmd, UserCtx* user_ctx, ServerResponse* res) {
    cout<<"received command '"<<CommandType_Name(cmd->type())<<"' ("<<cmd->type()<<"), with "<<cmd->params_size()<<" params"<<endl;

    if(cmd->type() == CommandType::LOGIN) {
        res->set_type(ResponseType::LOGGED);
        return true;
    }
}

bool processHandshake(Handshake* handshake, UserCtx* user_ctx, ServerResponse* res) {
    cout<<"Setting encryption to "<<EncryptionAlgorithm_Name(handshake->encryptionalgorithm())<<endl;

    user_ctx->encryption_algorithm =  handshake->encryptionalgorithm();

    res->set_type(ResponseType::OK);
    return true;
}

bool parseMessage(uint8_t buf[], int len, const UserCtx* user_ctx, uint8_t* msg_type, uint8_t** parsed_data, uint32_t* parsed_len) {
    EncodedMessage msg;
    msg.ParseFromArray(buf, len);
    cout<<"Parsing message"<<endl;
    cout<<"size: "<<msg.datasize()<<endl;
    cout<<"hash: "<<printHash(msg.hashalgorithm(), (uint8_t*) msg.hash().c_str())<<"("<<msg.hash().length()<<", "<<HASH_SIZE[msg.hashalgorithm()]<<")"<<endl;
    cout<<"data: "<<msg.data()<<"("<<msg.data().length()<<")"<<endl;

    if(!msg.data().length() || msg.hash().length() != HASH_SIZE[msg.hashalgorithm()]) {
        cout<<"wrong data or hash length"<<endl;
        return false;
    }

    *parsed_len = 0; //decrypted data size

    uint8_t decrypt_alg = user_ctx->encryption_algorithm;
    if(msg.type() == MessageType::HANDSHAKE) {
        decrypt_alg = EncryptionAlgorithm::NOENCRYPTION;
    }

    decrypt(decrypt_alg, (uint8_t*) msg.data().c_str(), msg.datasize(), parsed_data, parsed_len);

    if(msg.datasize() != *parsed_len) {
        cout<<"wrong data length"<<endl;
        return false;
    }

    cout<<"decrypted data: "<<flush;

    for(int i=0; i<*parsed_len; i++) {
        cout<<(char) (*parsed_data)[i]<<flush;
    }
    cout<<endl;

    uint8_t* hash = nullptr;
    uint16_t hash_size;

    calculateHash(msg.hashalgorithm(), *parsed_data, *parsed_len, &hash, &hash_size);

    bool hash_ok = compareHash(hash, hash_size, (uint8_t*) msg.hash().c_str(), msg.hash().length());

    if(!hash_ok) {
        cout<<"wrong hash"<<endl;
        cout<<"should be "<<printHash(msg.hashalgorithm(), (uint8_t*) msg.hash().c_str())<<endl;
        cout<<"got       "<<printHash(msg.hashalgorithm(), hash)<<endl;
        delete hash;
        return false;
    }

    cout<<"Received message type: "<<MessageType_Name(msg.type())<<" ("<<msg.type()<<")"<<endl;

    *msg_type = msg.type();

    delete hash;
    return true;
}

bool processMessage(uint8_t buf[], int len, UserCtx* user_ctx, uint8_t** out_data, uint32_t* out_data_len) {
    uint8_t msg_type;

    uint8_t* parsed_msg = nullptr;
    uint32_t parsed_len;

    bool parsed = parseMessage(buf, len, user_ctx, &msg_type, &parsed_msg, &parsed_len);

    if(!parsed || parsed_len == 0) {
        cout<<"There was an error during message parsing"<<endl;
        return false;
    }

    bool response = false;
    ServerResponse res;

    if(msg_type == MessageType::COMMAND) {
        Command cmd;
        cmd.ParseFromArray(parsed_msg, parsed_len);
        response = processCommand(&cmd, user_ctx, &res);
    } else if(msg_type == MessageType::HANDSHAKE) {
        Handshake handshake;
        handshake.ParseFromArray(parsed_msg, parsed_len);
        response = processHandshake(&handshake, user_ctx, &res);
    } else {
        cout<<"Error: unknown message type!"<<endl;
    }

    delete parsed_msg;

    if(response) {
        response = prepareServerResponse(&res, user_ctx, out_data, out_data_len);
    }
    return response;
}