#include "main.h"
#include "utils.h"

using namespace std;
using namespace StorageCloud;

void calculateHash(HashAlgorithm algo, const uint8_t buf[], int len, uint8_t** digest, uint16_t* digest_len) {
    if(algo == HashAlgorithm::H_SHA512) {
        *digest_len = SHA512_DIGEST_LENGTH;
        *digest = new uint8_t[*digest_len];
        SHA512(buf, (size_t) len, *digest);
    } else if(algo == HashAlgorithm::H_NOHASH) {
        *digest_len = 0;
        *digest = nullptr;
    } else {
        *digest_len = 0;
        cout<<"Error: unknown hashing algorithm ("<<HashAlgorithm_Name(algo)<<")"<<endl;
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

uint32_t parseSize(const uint8_t size_buf[]) {
    return ((size_buf[0]<<24u)|(size_buf[1]<<16u)|(size_buf[2]<<8u)|(size_buf[3]));
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

void encrypt(const EncryptionAlgorithm algo, const uint8_t in[], const uint32_t in_len, uint8_t** out, uint32_t* out_len) {
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

void decrypt(const EncryptionAlgorithm algo, const uint8_t in[], const uint32_t in_len, uint8_t** out, uint32_t* out_len) {
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