#ifndef SERVER_USER_H
#define SERVER_USER_H

#include <openssl/rand.h>
#include <ftw.h>

#include "main.h"
#include "Database.h"

#define FILE_REGULAR 1
#define FILE_DIR 2

#define ADD_FILE_OK 0
#define ADD_FILE_WRONG_DIR 1
#define ADD_FILE_NO_SPACE 2
#define ADD_FILE_INTERNAL_ERROR 3
#define ADD_FILE_FILE_EXISTS 4
#define ADD_FILE_EMPTY_NAME 5
#define ADD_FILE_CONTINUE_OK 6

#define FILE_HASH_SIZE SHA_DIGEST_LENGTH

#define USER_USER 1
#define USER_ADMIN 2

using bsoncxx::oid;
using std::vector;

class UserManager;

// user file
struct UFile {
    oid id;
    string filename;
    string hash;
    uint64_t size;
    uint64_t creation_date;
    bool isValid;
    uint64_t lastValid;
    string owner_name;
    uint8_t type;
    string realPath;
};

struct UDetails {
    string name;
    string surname;
    string username;
    uint8_t role;
    uint64_t totalSpace;
    uint64_t usedSpace;
};

class User {
private:
    oid id;
    UserManager& user_manager;
    bool authorized;
    bool valid;
    bool currentInFileValid;
    UFile currentInFile;

    bool checkPassword(string&);

public:
    User(const string&, UserManager&);
    User(oid&, UserManager&);
    User(UserManager&);
    bool getName(string&);
    bool getHomeDir(string&);
    bool getSurname(string&);
    bool setName(string&);
    bool setPassword(string&);
    bool loginByPassword(string&, string&);
    bool loginBySid(string&);
    bool logout(string&);
    bool addUsername(const string&);
    bool isValid() { return valid; };
    bool isAuthorized() { return authorized; };
    bool listFilesinPath(const string&, vector<UFile>&);
    const UFile& getCurrentInFileMetadata();
    bool isCurrentInFileValid();
    uint8_t addFile(UFile&);
    bool addFileChunk(const string&);
    bool isAdmin();
    bool getRole(uint8_t&);
    bool getCapacity(uint64_t&);
    bool getFreeSpace(uint64_t&);
    bool getYourStats(UDetails&);
    bool deleteFile(const string&);

    bool listUserFiles(string&, string&, vector<UFile>&);
};

class UserManager {
private:
    Database& db;
    Logger& logger;
    string root_path = "/storage";
    uint64_t defaultStorage = 10*1024*1024*1024ull;

    string l_id = "UserManager";

    explicit UserManager(Database&, Logger&);
    bool parseUserDetails(std::map<string, bsoncxx::types::value>&, UDetails&);
    bool getPasswdHash(oid&, string&);

    bsoncxx::types::b_utf8 toUTF8(string&);
    bsoncxx::types::b_int64 toINT64(uint64_t i);
    bsoncxx::types::b_date currDate();
    bsoncxx::types::b_oid toOID(oid);
    bsoncxx::types::b_bool toBool(bool);
    bsoncxx::types::b_binary toBinary(string&);

//    string mapToString(std::map<string, bsoncxx::document::element>&);
public:
    bool getName(oid&, string&);
    bool getSurname(oid&, string&);
    bool getHomeDir(oid&, string&);
    bool checkPasswd(oid&, string&);
    bool setPasswd(oid&, const string&);
    bool setName(oid&, string&);
    bool addSid(oid&, string&);
    bool checkSid(oid&, string&);
    bool yourFileExists(oid &, const string &);
    bool yourFileIsDir(oid &, const string &);
    bool getUserId(const string& username, oid& id);
    bool removeSid(oid&, string&);
    bool listAllUsers(std::vector<UDetails>&);
    bool getUserDetails(oid, UDetails&);
    bool getUserRole(oid&, uint8_t&);
    bool getTotalSpace(oid&, uint64_t&);
    bool getFreeSpace(oid&, uint64_t&);
    bool registerUser(UDetails&, const string&, bool&);
    bool deleteFile(oid&, const string&);
    bool deletePath(oid&, const string&);

    bool listFilesinPath(oid&, const string&, vector<UFile>&);
    bool addNewFile(oid&, UFile&, string&, oid&);
    bool getYourFileMetadata(oid&, const string&, UFile&);
    bool addFileChunk(UFile&, const string&);
    bool validateFile(UFile&);

    bool runAsUser(string&, std::function<bool(oid&)>);

    bool deleteUser(const string&);

    static UserManager& getInstance(Database* db = nullptr, Logger* logger = nullptr)
    {
        static UserManager instance(*db, *logger);
        return instance;
    }
};

#endif //SERVER_USER_H
